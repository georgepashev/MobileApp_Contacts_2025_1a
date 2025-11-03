package com.example.contacts

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE contacts ADD COLUMN firebaseId TEXT")
        db.execSQL("ALTER TABLE contacts ADD COLUMN version INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE contacts ADD COLUMN updatedAt INTEGER NOT NULL    DEFAULT 0")
                db.execSQL("ALTER TABLE contacts ADD COLUMN dirty INTEGER NOT NULL  DEFAULT 1")
                db.execSQL("ALTER TABLE contacts ADD COLUMN deleted INTEGER NOT NULL      DEFAULT 0")

// backfill updatedAt with current time for existing rows (optional but helpful)
                db.execSQL("UPDATE contacts SET updatedAt = strftime('%s','now')*1000 WHERE                 updatedAt = 0")

        // Trigger to increment version after update
        db.execSQL("""
    CREATE TRIGGER update_contact_version
    AFTER UPDATE ON contacts
    FOR EACH ROW
    WHEN 
        OLD.name IS NOT NEW.name OR
        OLD.phone IS NOT NEW.phone OR
        OLD.email IS NOT NEW.email OR
        OLD.address IS NOT NEW.address OR
        OLD.photoUrl IS NOT NEW.photoUrl OR
        OLD.photoPath IS NOT NEW.photoPath
    BEGIN
        UPDATE contacts SET version = OLD.version + 1 WHERE id = OLD.id;
    END;
    """.trimIndent())
    }
}

@Database(
    entities = [Contact::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    companion object {
        fun get(ctx: Context): AppDatabase = Room.databaseBuilder(
            ctx.applicationContext,
            AppDatabase::class.java,
            "contacts.db"
        ).addMigrations(MIGRATION_1_2).build()
    }
}
