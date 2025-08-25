package com.aliJafari.bbarq.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import com.aliJafari.bbarq.ForegroundService
import com.aliJafari.bbarq.R
import com.aliJafari.bbarq.adapters.OutagesAdapter
import com.aliJafari.bbarq.data.local.AuthStorage
import com.aliJafari.bbarq.data.repository.OutageRepository
import com.aliJafari.bbarq.databinding.ActivityMainBinding
import com.aliJafari.bbarq.isServiceRunning
import com.aliJafari.bbarq.ui.auth.LoginActivity
import com.aliJafari.bbarq.utils.BillIDNot13Chars
import com.aliJafari.bbarq.utils.BillIDNotFoundException
import com.aliJafari.bbarq.utils.RequestUnsuccessful
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences

    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = applicationContext.getSharedPreferences("my_prefs", MODE_PRIVATE)
        super.onCreate(savedInstanceState)

        testToken()
        binding = ActivityMainBinding.inflate(layoutInflater)

        val canPostNotifications =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        binding.main.reminderSwitch.isChecked =
            canPostNotifications && prefs.getBoolean("reminder", false)
        binding.main.reminderSwitch.setOnCheckedChangeListener { _, isChecked ->

            if (canPostNotifications) {
                prefs.edit(commit = true) { putBoolean("reminder", isChecked) }
            } else {
                @SuppressLint("InlinedApi") requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1002
                )
            }
        }
        checkPermissions()
        binding.main.billIdInput.addTextChangedListener { text ->
            val length = text?.length ?: 0
            if (length != 13) {
                binding.main.billId.error = "Must be 13 chars long"
                binding.main.billId.isErrorEnabled = true

            } else {
                binding.main.billId.isErrorEnabled = false
                binding.main.billId.error = null
                prefs.edit(commit = true) {
                    putString("billId", text.toString())
                }
                binding.main.refresh.performClick()
            }
        }
        binding.main.billIdInput.setText(prefs.getString("billId", "").toString())
        binding.main.refresh.setOnClickListener {
            requestCurrentData()
        }
        fun updateFab() {
            val intent = Intent(this, ForegroundService::class.java)
            if (isServiceRunning(this, ForegroundService::class.java)) {
                binding.fab.icon = ContextCompat.getDrawable(this, R.drawable.ic_pause)
                binding.fab.text = getString(R.string.stop_service_fab)
                binding.fab.setOnClickListener {
                    stopService(intent)
                    Handler(Looper.getMainLooper()).postDelayed({
                        updateFab()
                    }, 100)
                }
            } else {
                binding.fab.icon = ContextCompat.getDrawable(this, R.drawable.ic_play)
                binding.fab.text = getString(R.string.start_service_fab)
                binding.fab.setOnClickListener {
                    if (binding.main.billId.isErrorEnabled || isLoading) return@setOnClickListener
                    if (canPostNotifications) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(intent)
                        } else {
                            startService(intent)
                        }
                        Handler(Looper.getMainLooper()).postDelayed({
                            updateFab()
                        }, 100)
                    }
                }
            }
        }
        updateFab()
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.main.progress.visibility = View.GONE
        binding.main.progress.isActivated = false
        if (prefs.getString("billId", "")?.length == 13) {
            requestCurrentData()
        }
    }

    private fun testToken() {
        if (AuthStorage(this).getToken()==null){
            startActivity(
                Intent(this, LoginActivity::class.java)
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun requestCurrentData() {
        if (isLoading) return
        isLoading = true
        binding.main.emptyList.visibility = View.GONE
        binding.main.progress.visibility = View.VISIBLE
        binding.main.progress.isActivated = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                OutageRepository(applicationContext).sendRequest(prefs.getString("billId", "").toString()) {
                    runOnUiThread {
                        binding.main.progress.visibility = View.GONE
                        binding.main.progress.isActivated = false
                        binding.main.networkError.root.visibility = View.GONE
                        binding.main.billId.error = ""
                        binding.main.billId.isErrorEnabled = false
                        if (it.isEmpty()) {
                            binding.main.emptyList.visibility = View.VISIBLE
                            val messages = resources.getStringArray(R.array.no_power_cut_messages)
                            binding.main.emptyList.text = messages.random()
                        } else {
                            binding.main.recyclerView.adapter = OutagesAdapter(
                                it
                            )
                        }
                        isLoading = false
                    }
                }
            } catch (e: BillIDNot13Chars) {
                runOnUiThread {
                    binding.main.billId.error = getString(R.string.field_error_invalid_id_count)
                    binding.main.billId.isErrorEnabled = true
                    binding.main.progress.visibility = View.GONE
                    binding.main.progress.isActivated = false
                    isLoading = false
                }
            } catch (e: BillIDNotFoundException) {
                runOnUiThread {
                    binding.main.progress.visibility = View.GONE
                    binding.main.progress.isActivated = false
                    binding.main.billId.error = getString(R.string.field_error_invalid_id)
                    binding.main.billId.isErrorEnabled = true
                    binding.main.networkError.root.visibility = View.VISIBLE
                    binding.main.networkError.error.text =
                        getString(R.string.field_error_invalid_id_sub)
                    isLoading = false
                }
            } catch (e: RequestUnsuccessful) {
                runOnUiThread {
                    binding.main.progress.visibility = View.GONE
                    binding.main.progress.isActivated = false
                    binding.main.networkError.root.visibility = View.VISIBLE
                    binding.main.networkError.error.text = e.details
                    isLoading = false
                }
            }
        }
    }

    @SuppressLint("BatteryLife")
    private fun checkPermissions() {
        binding.main.batteryError.visibility = View.GONE
        binding.main.alarmsAndRemindersError.visibility = View.GONE
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val canSchedule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true // older versions allow exact alarms by default
        }
        if (canSchedule.not()) {
            binding.main.alarmsAndRemindersError.visibility = View.VISIBLE
            binding.main.alarmsAndRemindersButton.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this, Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.setData(uri)
                        binding.main.alarmsAndRemindersError.visibility = View.GONE

                    }
                }
            }
            binding.main.alarmsAndRemindersError.setOnClickListener { binding.main.alarmsAndRemindersButton.performClick() }
        }

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val isIgnoring = powerManager.isIgnoringBatteryOptimizations(packageName)
        if (!isIgnoring) {
            binding.main.batteryError.visibility = View.VISIBLE
            binding.main.batteryOptimizationButton.setOnClickListener {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                binding.main.batteryError.visibility = View.GONE
                intent.data = "package:$packageName".toUri()
                startActivity(intent)
                binding.main.batteryError.visibility = View.GONE
            }
            binding.main.batteryError.setOnClickListener { binding.main.batteryOptimizationButton.performClick() }
        } else {
            binding.main.batteryError.visibility = View.GONE
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> {
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, "https://github.com/alijafari-gd/B-Barq".toUri())
                startActivity(browserIntent)
                true
            }
            R.id.action_logout -> {
                AuthStorage(this).clearToken()
                startActivity(
                    Intent(this, LoginActivity::class.java)
                )
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}