package com.example.alarmkotlin

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.alarmkotlin.alarmList.AlarmListFragment
import com.example.alarmkotlin.databinding.ActivityMainBinding
import com.example.alarmkotlin.stopwatch.StopwatchFragment
import com.example.alarmkotlin.timer.TimerFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()

        // Загружаем стартовый фрагмент
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameAlarm, AlarmListFragment())
                .commit()
        }
    }

    private fun setupBottomNavigation() {
        binding.navBottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.stopwatch-> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameAlarm, StopwatchFragment())
                        .commit()
                    true
                }
                R.id.alarmList-> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameAlarm, AlarmListFragment())
                        .commit()
                    true
                }
                R.id.timer-> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameAlarm, TimerFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }
}