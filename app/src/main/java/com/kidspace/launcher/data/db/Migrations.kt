package com.kidspace.launcher.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE child_tiles ADD COLUMN webLaunchMode TEXT NOT NULL DEFAULT 'EXTERNAL'")
        db.execSQL("ALTER TABLE child_tiles ADD COLUMN cameraPolicy TEXT NOT NULL DEFAULT 'GRANT'")
        db.execSQL("ALTER TABLE child_tiles ADD COLUMN microphonePolicy TEXT NOT NULL DEFAULT 'GRANT'")
        db.execSQL("ALTER TABLE child_tiles ADD COLUMN locationPolicy TEXT NOT NULL DEFAULT 'GRANT'")
    }
}
