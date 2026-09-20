package ru.netology.myappwithmaps.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.myappwithmaps.db.entity.PointEntity

@Dao
interface PointDao {
    @Query("SELECT * FROM PointEntity")
    fun getAll(): Flow<List<PointEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveOrUpdate(point: PointEntity): Long

    @Delete
    suspend fun delete(point: PointEntity)
}