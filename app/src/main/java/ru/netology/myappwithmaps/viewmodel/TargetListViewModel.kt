package ru.netology.myappwithmaps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.netology.myappwithmaps.db.AppDb
import ru.netology.myappwithmaps.db.entity.PointEntity

class TargetListViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDb.getInstance(application).pointDao()

    val allPoints: StateFlow<List<PointEntity>> = dao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun savePoint(point: PointEntity) {
        viewModelScope.launch {
            dao.saveOrUpdate(point)
        }
    }

    fun deletePoint(point: PointEntity) {
        viewModelScope.launch {
            dao.delete(point)
        }
    }
}