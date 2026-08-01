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
        dao.incrementAllSortOrders()
        return dao.insert(tile.copy(sortOrder = 0).toEntity())
    }

    suspend fun addTilesAtFront(tiles: List<ChildTile>): List<Long> {
        if (tiles.isEmpty()) return emptyList()
        dao.incrementAllSortOrdersBy(tiles.size)
        return tiles.mapIndexed { index, tile ->
            dao.insert(tile.copy(sortOrder = index).toEntity())
        }
    }

    suspend fun updateTile(tile: ChildTile) {
        dao.update(tile.toEntity())
    }

    suspend fun removeTile(id: Long) = dao.deleteById(id)

    suspend fun getAllTiles(): List<ChildTile> =
        dao.getAll().map { it.toModel() }

    suspend fun replaceAllTiles(tiles: List<ChildTile>) {
        dao.deleteAll()
        tiles.sortedBy { it.sortOrder }.forEachIndexed { index, tile ->
            dao.insert(tile.copy(id = 0, sortOrder = index).toEntity())
        }
    }

    suspend fun reorderTiles(tiles: List<ChildTile>) {
        tiles.forEachIndexed { index, tile ->
            dao.update(tile.copy(sortOrder = index).toEntity())
        }
    }
}
