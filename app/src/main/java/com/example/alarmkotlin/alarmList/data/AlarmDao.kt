package com.example.alarmkotlin.alarmList.data

import androidx.room.*

@Dao
interface AlarmDao {

    @Insert
    suspend fun insert(alarm: AlarmItem) //Вставить объект в базу

    @Query("SELECT * FROM alarms")
    suspend fun getAll(): List<AlarmItem> //	Выполнить SQL-запрос вручную

    @Delete
    suspend fun delete(alarm: AlarmItem) //Удалить объект


    @Update
    suspend fun update(alarm: AlarmItem) // Обновить объект
}

/**
Объяснение:
- AlarmDao — это интерфейс для работы с таблицей AlarmItem.
- @Dao сообщает Room, что этот интерфейс содержит SQL-запросы.
- @Insert, @Delete, @Update — готовые аннотации для базовых операций.
- @Query позволяет писать собственный SQL — например, чтобы получить все будильники.
- suspend используется для работы с корутинами (выполняется асинхронно).
*/