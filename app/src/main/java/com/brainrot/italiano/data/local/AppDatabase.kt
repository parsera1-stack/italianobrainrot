package com.brainrot.italiano.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.brainrot.italiano.data.local.dao.DefaultWordDao
import com.brainrot.italiano.data.local.dao.WordAnswerHistoryDao
import com.brainrot.italiano.data.local.dao.WordDao
import com.brainrot.italiano.data.local.entity.DefaultWordEntity
import com.brainrot.italiano.data.local.entity.WordAnswerHistoryEntity
import com.brainrot.italiano.data.local.entity.WordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        WordEntity::class,
        WordAnswerHistoryEntity::class,
        DefaultWordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao
    abstract fun wordAnswerHistoryDao(): WordAnswerHistoryDao
    abstract fun defaultWordDao(): DefaultWordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "italiano_brain_rot_db"
                )
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Инициализация стартового словаря при первом запуске
            CoroutineScope(Dispatchers.IO).launch {
                val database = getDatabase(context)
                populateDefaultWords(database.defaultWordDao())
            }
        }

        private suspend fun populateDefaultWords(defaultWordDao: DefaultWordDao) {
            val defaultWords = listOf(
                DefaultWordEntity(russian = "волк", english = "wolf"),
                DefaultWordEntity(russian = "маленький мальчик", english = "baby boy"),
                DefaultWordEntity(russian = "мальчики", english = "boys"),
                DefaultWordEntity(russian = "тарелка, блюдо", english = "dish", specialNote = "принимаются оба варианта"),
                DefaultWordEntity(russian = "осёл", english = "donkey"),
                DefaultWordEntity(russian = "обезьяна", english = "monkey"),
                DefaultWordEntity(russian = "помидоры", english = "tomatoes"),
                DefaultWordEntity(russian = "картофель", english = "potatoes"),
                DefaultWordEntity(russian = "герой", english = "hero"),
                DefaultWordEntity(russian = "герои", english = "heroes"),
                DefaultWordEntity(russian = "фото", english = "photo"),
                DefaultWordEntity(russian = "фотографии", english = "photos"),
                DefaultWordEntity(russian = "пианино", english = "piano", specialNote = "перевод всегда пианино"),
                DefaultWordEntity(russian = "пианино (мн.ч.)", english = "pianos", specialNote = "перевод всегда пианино"),
                DefaultWordEntity(russian = "зеркало", english = "mirror")
            )
            defaultWordDao.insertDefaultWords(defaultWords)
        }
    }
}
