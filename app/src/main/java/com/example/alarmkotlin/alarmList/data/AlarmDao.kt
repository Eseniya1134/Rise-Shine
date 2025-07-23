package com.example.alarmkotlin.alarmList.data

import androidx.room.*

@Dao
interface AlarmDao {

    @Query("SELECT * FROM alarms")
    suspend fun getAllAlarms(): List<AlarmItem>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Int): AlarmItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmItem): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmItem)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmItem)
}

/**
Объяснение:
- AlarmDao — это интерфейс для работы с таблицей AlarmItem.
- @Dao сообщает Room, что этот интерфейс содержит SQL-запросы.
- @Insert, @Delete, @Update — готовые аннотации для базовых операций.
- @Query позволяет писать собственный SQL — например, чтобы получить все будильники.
- suspend используется для работы с корутинами (выполняется асинхронно).
*/