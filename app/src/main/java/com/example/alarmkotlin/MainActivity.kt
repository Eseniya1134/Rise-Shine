package com.example.alarmkotlin

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
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
        checkAndRequestPermissions()
        // Устанавливаем второй пункт меню как выбранный
        binding.navBottom.selectedItemId = R.id.alarmList

        // Этот код теперь не нужен, так как выбор меню сам загрузит фрагмент
        // if (savedInstanceState == null) {
        //     supportFragmentManager.beginTransaction()
        //         .replace(R.id.frameAlarm, AlarmListFragment())
        //         .commit()
        // }
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

    private fun checkAndRequestPermissions() {
        // 1. Разрешение на точные будильники (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }

        // 2. Разрешение на полноэкранные уведомления (Android 14+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34
            val notificationManager = getSystemService(NotificationManager::class.java)
            if (!notificationManager.canUseFullScreenIntent()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                    data = Uri.parse("package:$packageName")
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    // Fallback для устройств где этот экран недоступен
                    showToast("Откройте настройки приложения и разрешите полноэкранные уведомления")
                }
            }
        }

        // 3. Разрешение на показ поверх других приложений
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
                overlayPermissionLauncher.launch(intent)
            }
        }

        // 4. Отключение оптимизации батареи
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                // Если не получается, открываем общие настройки батареи
                val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                startActivity(fallbackIntent)
            }
        }

        // 5. Разрешения на уведомления (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(
                android.Manifest.permission.POST_NOTIFICATIONS
            ), 1001)
        }
    }

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                // Разрешение получено
                showToast("Разрешение на показ поверх других приложений получено")
            } else {
                // Разрешение не получено
                showToast("Для корректной работы будильника необходимо разрешение")
            }
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1001 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showToast("Разрешение на уведомления получено")
                }
            }
        }
    }
}