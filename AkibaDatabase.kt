package com.akiba.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.akiba.app.data.model.Member
import com.akiba.app.data.model.TontineGroup
import com.akiba.app.data.model.TontineTransaction

@Database(
    entities = [TontineGroup::class, Member::class, TontineTransaction::class],
    version = 1,
    exportSchema = false
)
abstract class AkibaDatabase : RoomDatabase() {
    abstract fun akibaDao(): AkibaDao

    companion object {
        @Volatile
        private var INSTANCE: AkibaDatabase? = null

        fun getDatabase(context: Context): AkibaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AkibaDatabase::class.java,
                    "akiba_offline_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
