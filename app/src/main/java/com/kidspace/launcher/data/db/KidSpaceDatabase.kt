package com.kidspace.launcher.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ChildTileEntity::class], version = 1, exportSchema = false)
abstract class KidSpaceDatabase : RoomDatabase() {
    abstract fun childTileDao(): ChildTileDao

    companion object {
        @Volatile
        private var instance: KidSpaceDatabase? = null

        fun getInstance(context: Context): KidSpaceDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    KidSpaceDatabase::class.java,
                    "kidspace.db",
                ).build().also { instance = it }
            }
        }
    }
}
