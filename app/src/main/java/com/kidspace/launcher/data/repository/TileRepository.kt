package com.kidspace.launcher.data.repository

import com.kidspace.launcher.data.db.ChildTileDao
import com.kidspace.launcher.data.db.toEntity
import com.kidspace.launcher.data.db.toModel
import com.kidspace.launcher.data.model.ChildTile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TileRepository(private val dao: ChildTileDao) {
    fun observeTiles(): Flow<List<ChildTile>> =
        dao.observeAll().map { entities -> entities.map { it.toModel() } }

    suspend fun addTile(tile: ChildTile): Long {
        val nextOrder = dao.maxSortOrder() + 1
        return dao.insert(tile.copy(sortOrder = nextOrder).toEntity())
    }

    suspend fun removeTile(id: Long) = dao.deleteById(id)

    suspend fun reorderTiles(tiles: List<ChildTile>) {
        tiles.forEachIndexed { index, tile ->
            dao.update(tile.copy(sortOrder = index).toEntity())
        }
    }
}
