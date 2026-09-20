package ru.netology.myappwithmaps.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dev.androidbroadcast.vbpd.viewBinding
import kotlinx.coroutines.launch
import ru.netology.myappwithmaps.R
import ru.netology.myappwithmaps.adapter.PointsAdapter
import ru.netology.myappwithmaps.adapter.OnInteractionListener
import ru.netology.myappwithmaps.databinding.FragmentTargetListBinding
import ru.netology.myappwithmaps.db.entity.PointEntity
import ru.netology.myappwithmaps.extensions.showEditDialog
import ru.netology.myappwithmaps.viewmodel.MapsViewModel

class TargetListFragment : Fragment(R.layout.fragment_target_list) {
    private val viewModel: MapsViewModel by activityViewModels()
    private val binding by viewBinding(FragmentTargetListBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PointsAdapter(object : OnInteractionListener {
            override fun onPoint(point: PointEntity) {
                viewModel.selectPoint(point)
                findNavController().popBackStack()
            }

            override fun onEdit(point: PointEntity) {
                showEditDialog(
                    point = point,
                    onSave = { viewModel.savePoint(it) },
                    onDelete = { viewModel.deletePoint(it) }
                )
            }
        })
        binding.recyclerView.adapter = adapter

        binding.btnShowMaps.setOnClickListener {
            findNavController().popBackStack()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allPoints.collect { pointEntities ->
                    adapter.submitList(pointEntities)
                }
            }
        }
    }
}