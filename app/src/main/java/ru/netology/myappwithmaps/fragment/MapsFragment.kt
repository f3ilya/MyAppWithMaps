package ru.netology.myappwithmaps.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.MapWindow
import com.yandex.mapkit.mapview.MapView
import dev.androidbroadcast.vbpd.viewBinding
import kotlinx.coroutines.launch
import ru.netology.myappwithmaps.R
import ru.netology.myappwithmaps.databinding.FragmentMapsBinding
import ru.netology.myappwithmaps.db.entity.PointEntity
import ru.netology.myappwithmaps.extensions.DrawableImageProvider
import ru.netology.myappwithmaps.extensions.ImageInfo
import ru.netology.myappwithmaps.extensions.showEditDialog
import ru.netology.myappwithmaps.viewmodel.MapsViewModel

class MapsFragment : Fragment(R.layout.fragment_maps) {

    private val viewModel: MapsViewModel by activityViewModels()
    private val binding by viewBinding(FragmentMapsBinding::bind)
    private val placemarkTapListener = MapObjectTapListener { mapObject, _ ->
        val pointData = mapObject.userData as? PointEntity
        if (pointData != null) {
            showEditDialog(
                point = pointData,
                onSave = { viewModel.savePoint(it) },
                onDelete = { viewModel.deletePoint(it) }
            )
        }
        true
    }

    private val mapInputListener = object : InputListener {
        override fun onMapTap(map: Map, point: Point) {}

        override fun onMapLongTap(map: Map, point: Point) {
            val newPoint = PointEntity(
                latitude = point.latitude,
                longitude = point.longitude,
                title = "",
                description = "",
            )
            showEditDialog(
                point = newPoint,
                onSave = { viewModel.savePoint(it) },
                onDelete = { viewModel.deletePoint(it) }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapView = binding.map
        val mapWindow = mapView.mapWindow
        val yandexMap = mapWindow.map

        subscribeToLifecycle(mapView)
        checkPermissions(mapWindow)

        val imageProvider = DrawableImageProvider(
            requireContext(),
            ImageInfo(R.drawable.ic_netology_48dp)
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allPoints.collect { pointEntities ->
                    yandexMap.mapObjects.clear()
                    pointEntities.forEach { mapPoint ->
                        val target = Point(mapPoint.latitude, mapPoint.longitude)
                        yandexMap.mapObjects.addPlacemark { placemarkMapObject ->
                            placemarkMapObject.geometry = target
                            placemarkMapObject.setIcon(imageProvider)
                            placemarkMapObject.setText(mapPoint.title)
                            placemarkMapObject.addTapListener(placemarkTapListener)
                            placemarkMapObject.userData = mapPoint
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigateToPoint.collect { chosenPoint ->
                    if (chosenPoint != null) {
                        val target = Point(chosenPoint.latitude, chosenPoint.longitude)
                        moveToMarker(yandexMap, target)
                        viewModel.clearNavigation()
                    }
                }
            }
        }

        yandexMap.addInputListener(mapInputListener)

        binding.apply {
            btnShowList.setOnClickListener {
                findNavController().navigate(R.id.action_mapsFragment_to_targetListFragment)
            }

            btnZoomIn.setOnClickListener {
                changeZoom(1F, yandexMap)
            }

            btnZoomOut.setOnClickListener {
                changeZoom(-1F, yandexMap)
            }
        }
    }

    private fun changeZoom(step: Float, yandexMap: Map) {
        val currentPosition = yandexMap.cameraPosition
        val newPosition = CameraPosition(
            currentPosition.target,
            currentPosition.zoom + step,
            currentPosition.azimuth,
            currentPosition.tilt
        )
        yandexMap.move(
            newPosition,
            Animation(Animation.Type.SMOOTH, 0.2F),
            null
        )
    }

    private fun moveToMarker(
        yandexMap: Map,
        target: Point
    ) {
        val currentPosition = yandexMap.cameraPosition
        yandexMap.move(
            CameraPosition(
                target,
                15F,
                currentPosition.azimuth,
                currentPosition.tilt
            ),
            Animation(Animation.Type.SMOOTH, 3F),
            null,
        )
    }

    private fun checkPermissions(mapWindow: MapWindow) {
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    enableUserLocation(mapWindow)
                } else {
                    if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        showSettingsSnackbar()
                    } else {
                        Snackbar.make(
                            requireView(),
                            getString(R.string.access_to_geolocation_is_denied),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                enableUserLocation(mapWindow)

                MapKitFactory.getInstance()
                    .createLocationManager()
                    .requestSingleUpdate(
                        object : LocationListener {
                            override fun onLocationUpdated(location: Location) {
                                println(location)
                            }

                            override fun onLocationStatusUpdated(locationStatus: LocationStatus) {
                                println(locationStatus)
                            }
                        }
                    )
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.access_to_geolocation))
                    .setMessage(getString(R.string.the_permission_is_needed_to))
                    .setPositiveButton(getString(R.string.provide)) { _, _ ->
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun showSettingsSnackbar() {
        Snackbar.make(
            requireView(),
            getString(R.string.geolocation_is_disabled),
            Snackbar.LENGTH_INDEFINITE
        ).setAction(getString(R.string.settings)) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", requireContext().packageName, null)
            }
            startActivity(intent)
        }.show()
    }

    private fun enableUserLocation(mapWindow: MapWindow) {
        val userLocation = MapKitFactory.getInstance().createUserLocationLayer(mapWindow)
        userLocation.isVisible = true
        userLocation.isHeadingModeActive = true
    }

    private fun subscribeToLifecycle(mapView: MapView) {
        viewLifecycleOwner.lifecycle.addObserver(
            object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        Lifecycle.Event.ON_START -> {
                            MapKitFactory.getInstance().onStart()
                            mapView.onStart()
                        }

                        Lifecycle.Event.ON_STOP -> {
                            mapView.onStop()
                            MapKitFactory.getInstance().onStop()
                        }

                        Lifecycle.Event.ON_DESTROY -> source.lifecycle.removeObserver(this)

                        else -> Unit
                    }
                }
            }
        )
    }
}