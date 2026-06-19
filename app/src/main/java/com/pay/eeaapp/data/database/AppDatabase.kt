package com.pay.eeaapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pay.eeaapp.data.dao.DocumentDao
import com.pay.eeaapp.data.dao.ProjectDao
import com.pay.eeaapp.data.dao.ReviewDao
import com.pay.eeaapp.data.dao.UserDao
import com.pay.eeaapp.data.entities.ProjectDocumentEntity
import com.pay.eeaapp.data.entities.ProjectEntity
import com.pay.eeaapp.data.entities.ReviewCommentEntity
import com.pay.eeaapp.data.entities.UserEntity

@Database(
    entities = [UserEntity::class, ProjectEntity::class, ProjectDocumentEntity::class, ReviewCommentEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun projectDao(): ProjectDao
    abstract fun documentDao(): DocumentDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "eea_database"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}