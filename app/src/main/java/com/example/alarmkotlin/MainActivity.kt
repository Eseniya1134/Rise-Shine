package com.example.alarmkotlin

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import android.view.animation.AnimationUtils
import androidx.core.view.WindowInsetsCompat
import com.example.alarmkotlin.alarmList.AlarmListFragment
import com.example.alarmkotlin.databinding.ActivityMainBinding
import com.example.alarmkotlin.stopwatch.StopwatchFragment
import com.example.alarmkotlin.timer.TimerFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

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


    /**
     * Настраивает нижнюю панель навигации
     * Устанавливает обработчик выбора пунктов меню
     */
    private fun setupBottomNavigation() {
        // Устанавливаем слушатель выбора элементов в BottomNavigationView
        binding.navBottom.setOnItemSelectedListener { item ->
            // Находим view иконки для анимации
            val menuView = binding.navBottom.findViewById<BottomNavigationView>(R.id.nav_bottom)
            val iconView = menuView.findViewById<View>(item.itemId)

            // Запускаем анимацию
            val animation = AnimationUtils.loadAnimation(this, R.anim.bottom_nav_anim)
            iconView.startAnimation(animation)

            // В зависимости от выбранного пункта меню заменяем фрагмент
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