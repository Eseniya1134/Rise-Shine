package com.example.alarmkotlin.alarmList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmkotlin.R
import com.example.alarmkotlin.alarmList.data.AlarmDao
import com.example.alarmkotlin.alarmList.data.AlarmDatabase
import com.example.alarmkotlin.alarmList.data.AlarmItem
import com.example.alarmkotlin.timer.TimerFragment

/**
 * Адаптер для отображения списка будильников в RecyclerView.
 * @param alarms - список объектов AlarmItem, каждый из которых содержит данные одного будильника.
 * @param onToggle - функция обратного вызова, вызывается при включении/выключении будильника пользователем.
 */
class AlarmAdapter(
    private val fragmentManager: FragmentManager,
    private var alarms: List<AlarmItem>,
    private val onItemLongClick: (AlarmItem) -> Unit,
    private val onItemClick: (AlarmItem) -> Unit,
    private val onToggle: (AlarmItem) -> Unit,

    ) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    private val selectedItems = mutableSetOf<AlarmItem>() // выбранные будильники
    var isSelectionMode = false                       // включен ли режим выбора
    private lateinit var db: AlarmDatabase // База данных


    /**
     * ViewHolder для одного элемента списка будильников.
     * Содержит ссылки на TextView с временем и Switch для включения/выключения будильника.
     */
    inner class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textTime: TextView = itemView.findViewById(R.id.textTime) // отображает время будильника
        val switchEnabled: Switch = itemView.findViewById(R.id.switchEnabled) // переключатель включён/выключен
    }

    /**
     * Создаёт новый ViewHolder. Вызывается, когда RecyclerView нужно создать новый элемент.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm, parent, false) // загружаем layout для одного будильника
        return AlarmViewHolder(view)
    }

    /**
     * Привязывает данные из списка alarms к конкретному ViewHolder'у.
     */
    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {

        val alarm = alarms[position]
        holder.textTime.text = alarm.time // отображаем время будильника
        holder.switchEnabled.setOnCheckedChangeListener(null) // убираем старый слушатель

        holder.switchEnabled.isChecked = alarm.isEnabled // устанавливаем текущее состояние переключателя

        // Назначаем новый слушатель на переключатель
        holder.switchEnabled.setOnCheckedChangeListener { _, isChecked ->
            // Вызываем onToggle с обновлённым будильником (копия с изменённым флагом isEnabled)
            onToggle(alarm.copy(isEnabled = isChecked))
        }


        //переход для редактирования будильника
        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                onItemClick(alarm)
            } else {
                // обычное поведение — открыть фрагмент редактирования
                val fragment = AddItemAlarmFragment().apply {
                    arguments = Bundle().apply {
                        putInt("alarm_id", alarm.id)
                    }
                }

                fragmentManager.beginTransaction()
                    .replace(R.id.frameAlarm, fragment)
                    .addToBackStack(null)
                    .commit()


            }
        }


        // Подсвечиваем элемент, если он выбран
        holder.itemView.isSelected = selectedItems.contains(alarm)

        // Долгое нажатие запускает режим выбора
        holder.itemView.setOnLongClickListener {
            onItemLongClick(alarm)
            true
        }

    }

    /**
     * Возвращает общее количество будильников в списке.
     */
    override fun getItemCount(): Int = alarms.size

    /**
     * Обновляет список будильников и перерисовывает адаптер.
     * Используется, если в список добавили, удалили или изменили элементы.
     */
    fun updateList(newList: List<AlarmItem>) {
        alarms = newList
        notifyDataSetChanged() // сообщаем адаптеру, что данные обновились
    }

    /**
     * Функция выбора/отмены выбора будильника.
     * Если будильник уже выбран — убираем из списка, иначе — добавляем.
     */
    fun toggleSelection(alarm: AlarmItem) {
        if (selectedItems.contains(alarm)) {
            selectedItems.remove(alarm)
        } else {
            selectedItems.add(alarm)
        }
        notifyDataSetChanged() // обновить список (чтобы перерисовались подсветки)
    }

    /**
     * Очищаем все выделения.
     */
    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<AlarmItem> = selectedItems.toList()

}
