package com.example.alarmkotlin.alarmList.data
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val time: String,                     // например, "07:30"
    val daysOfWeek: String,              // например, "Mon,Tue,Wed" или JSON: "[1,2,3]"
    val ringtoneUri: String?,            // путь к выбранной мелодии
    val difficultyLevel: Int = 1,        // 1 — лёгкий, 2 — средний, 3 — сложный
    val isEnabled: Boolean = true
)




/**
 * Объяснение:
 * - AlarmItem — это Entity, то есть описание таблицы в базе данных Room.
 * - @Entity указывает, что класс представляет таблицу с именем "alarms".
 * - @PrimaryKey(autoGenerate = true) создаёт уникальный ID, который увеличивается автоматически.
 * - Поля time и isEnabled — это колонки таблицы.
val time: String — поле, где ты временно хранишь время как строку (потом можно сделать LocalTime)
val isEnabled: Boolean = true — включён/выключен будильник
 */