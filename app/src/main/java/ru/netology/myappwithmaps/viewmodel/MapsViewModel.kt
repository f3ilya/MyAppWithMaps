package ru.netology.myappwithmaps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.netology.myappwithmaps.db.AppDb
import ru.netology.myappwithmaps.db.entity.PointEntity

class MapsViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDb.getInstance(application).pointDao()

    val allPoints: StateFlow<List<PointEntity>> = dao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _navigateToPoint = MutableStateFlow<PointEntity?>(null)
    val navigateToPoint: StateFlow<PointEntity?> = _navigateToPoint.asStateFlow()

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

    fun selectPoint(point: PointEntity) {
        _navigateToPoint.value = point
    }

    fun clearNavigation() {
        _navigateToPoint.value = null
    }
}