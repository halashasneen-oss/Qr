package com.halashasneen.nova

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.halashasneen.nova.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        applyDarkModePreference()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav: BottomNavigationView = binding.bottomNavigation
        bottomNav.setupWithNavController(navController)

        // إخفاء شريط التنقل السفلي في شاشات لا تحتاجه (النتيجة، الإعدادات التفصيلية...)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNav.visibility = if (destination.id == R.id.resultFragment ||
                destination.id == R.id.settingsFragment
            ) android.view.View.GONE else android.view.View.VISIBLE
        }
    }

    private fun applyDarkModePreference() {
        val app = application as QRProApplication
        // القراءة هنا بسيطة ومتزامنة عبر SharedPreferences (سريعة جدًا، لا تحتاج Coroutine)
        val mode = try {
            app.settingsRepository.getSettings().darkMode
        } catch (e: Exception) {
            "system"
        }
        val nightMode = when (mode) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
