package com.kidspace.launcher.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.kidspace.launcher.data.model.ChildTile
import com.kidspace.launcher.data.model.TileType
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "child_tiles")
data class ChildTileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val label: String,
    val target: String,
    val iconKey: String,
    val sortOrder: Int,
)

fun ChildTileEntity.toModel() = ChildTile(
    id = id,
    type = TileType.valueOf(type),
    label = label,
    target = target,
    iconKey = iconKey,
    sortOrder = sortOrder,
)

fun ChildTile.toEntity() = ChildTileEntity(
    id = id,
    type = type.name,
    label = label,
    target = target,
    iconKey = iconKey,
    sortOrder = sortOrder,
)

@Dao
interface ChildTileDao {
    @Query("SELECT * FROM child_tiles ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<ChildTileEntity>>

    @Query("SELECT * FROM child_tiles ORDER BY sortOrder ASC")
    suspend fun getAll(): List<ChildTileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tile: ChildTileEntity): Long

    @Update
    suspend fun update(tile: ChildTileEntity)

    @Query("DELETE FROM child_tiles WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM child_tiles")
    suspend fun maxSortOrder(): Int
}
