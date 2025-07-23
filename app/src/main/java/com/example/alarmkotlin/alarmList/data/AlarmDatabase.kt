package com.example.alarmkotlin.alarmList.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [AlarmItem::class], version = 2, exportSchema = false)
abstract class AlarmDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile
        private var INSTANCE: AlarmDatabase? = null

        fun getDatabase(context: Context): AlarmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlarmDatabase::class.java,
                    "alarm_database"
                )
                    .fallbackToDestructiveMigration() // Пересоздаёт БД при изменении схемы
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Объяснение:
 * - Этот класс описывает базу данных Room, хранящую будильники (AlarmItem).
 * - @Database связывает таблицы (entities) и указывает версию БД.
 * - version = 2 — версия обновлена, так как схема была изменена.
 * - fallbackToDestructiveMigration() — временное решение на этапе разработки, пересоздаёт БД при несовпадении схемы.
 * - RoomDatabase — абстрактный класс, от которого мы наследуемся.
 * - Метод alarmDao() позволяет получить доступ к DAO-объекту.
 * - В companion object реализован Singleton, чтобы использовать одну копию БД в приложении.
 * - Room.databaseBuilder создаёт/открывает файл "alarm_database" внутри приложения.
 */
