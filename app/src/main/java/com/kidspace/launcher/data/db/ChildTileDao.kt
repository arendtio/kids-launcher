package com.kidspace.launcher.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.kidspace.launcher.data.model.ChildTile
import com.kidspace.launcher.data.model.PermissionPolicy
import com.kidspace.launcher.data.model.TileType
import com.kidspace.launcher.data.model.WebLaunchMode
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "child_tiles")
data class ChildTileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val label: String,
    val target: String,
    val iconKey: String,
    val sortOrder: Int,
    val webLaunchMode: String = WebLaunchMode.EXTERNAL.name,
    val cameraPolicy: String = PermissionPolicy.GRANT.name,
    val microphonePolicy: String = PermissionPolicy.GRANT.name,
    val locationPolicy: String = PermissionPolicy.GRANT.name,
)

fun ChildTileEntity.toModel() = ChildTile(
    id = id,
    type = TileType.valueOf(type),
    label = label,
    target = target,
    iconKey = iconKey,
    sortOrder = sortOrder,
    webLaunchMode = WebLaunchMode.valueOf(webLaunchMode),
    cameraPolicy = PermissionPolicy.valueOf(cameraPolicy),
    microphonePolicy = PermissionPolicy.valueOf(microphonePolicy),
    locationPolicy = PermissionPolicy.valueOf(locationPolicy),
)

fun ChildTile.toEntity() = ChildTileEntity(
    id = id,
    type = type.name,
    label = label,
    target = target,
    iconKey = iconKey,
    sortOrder = sortOrder,
    webLaunchMode = webLaunchMode.name,
    cameraPolicy = cameraPolicy.name,
    microphonePolicy = microphonePolicy.name,
    locationPolicy = locationPolicy.name,
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

    @Query("DELETE FROM child_tiles")
    suspend fun deleteAll()

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM child_tiles")
    suspend fun maxSortOrder(): Int

    @Query("UPDATE child_tiles SET sortOrder = sortOrder + 1")
    suspend fun incrementAllSortOrders()

    @Query("UPDATE child_tiles SET sortOrder = sortOrder + :count")
    suspend fun incrementAllSortOrdersBy(count: Int)
}
