package com.kidspace.launcher.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ChildTileEntity::class], version = 4, exportSchema = false)
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
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
