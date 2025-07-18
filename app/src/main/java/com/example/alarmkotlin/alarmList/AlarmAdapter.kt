package com.example.alarmkotlin.alarmList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmkotlin.R
import com.example.alarmkotlin.alarmList.data.AlarmItem

class AlarmAdapter(
    private var alarms: List<AlarmItem>,
    private val onToggle: (AlarmItem) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    inner class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textTime: TextView = itemView.findViewById(R.id.textTime)
        val switchEnabled: Switch = itemView.findViewById(R.id.switchEnabled)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[position]
        holder.textTime.text = alarm.time
        holder.switchEnabled.isChecked = alarm.isEnabled

        holder.switchEnabled.setOnCheckedChangeListener { _, isChecked ->
            onToggle(alarm.copy(isEnabled = isChecked))
        }
    }

    override fun getItemCount(): Int = alarms.size

    fun updateList(newList: List<AlarmItem>) {
        alarms = newList
        notifyDataSetChanged()
    }
}

/*
Объяснение:
- Адаптер связывает список будильников с RecyclerView.
- AlarmViewHolder содержит ссылки на элементы item_alarm.xml.
- onToggle вызывается при переключении Switch — для обновления состояния в БД.
- updateList позволяет обновить список будильников в адаптере и перерисовать UI.
*/