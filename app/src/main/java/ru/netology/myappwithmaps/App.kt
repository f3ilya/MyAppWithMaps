package ru.netology.myappwithmaps

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import ru.netology.myappwithmaps.BuildConfig

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(BuildConfig.MAPS_API_KEY)
    }
}