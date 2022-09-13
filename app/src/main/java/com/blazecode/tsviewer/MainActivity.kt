/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.tsviewer

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.work.*
import com.blazecode.scrapguidev2.util.LinkUtil
import com.blazecode.scrapguidev2.util.MailUtil
import com.blazecode.tsviewer.databinding.ActivityMainBinding
import com.blazecode.tsviewer.ui.GraphFragment
import com.blazecode.tsviewer.ui.MainFragment
import com.blazecode.tsviewer.util.notification.ClientNotificationManager
import com.blazecode.tsviewer.util.tile.ClientTileService
import com.blazecode.tsviewer.util.updater.GitHubUpdater
import com.blazecode.tsviewer.util.updater.UpdateCheckWorker
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.aboutlibraries.LibsBuilder
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.coroutines.EmptyCoroutineContext


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var workManager: WorkManager
    private lateinit var gitHubUpdater: GitHubUpdater
    private val TAG = "updater"

    private lateinit var preferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.commit { replace(R.id.fragment_container, MainFragment()) }

        // START LOGGING
        Timber.plant(Timber.DebugTree())

        // PREFERENCES
        preferences = getSharedPreferences("preferences", MODE_PRIVATE)!!

        // INITIALIZE WORK MANAGER
        workManager = this.let { WorkManager.getInstance(it) }

        // INITIALIZE UPDATER
        gitHubUpdater = GitHubUpdater(this)

        // AUTO UPDATE CHECK
        val autoUpdateMenuItem = binding.toolbar.menu.findItem(R.id.action_update_check)
        autoUpdateMenuItem.isChecked = preferences.getBoolean("autoUpdateCheck", true)

        // DEBUG AUTO UPDATE CHECK
        val debugAutoUpdateMenuItem = binding.toolbar.menu.findItem(R.id.action_update_check_debug)
        debugAutoUpdateMenuItem.isChecked = preferences.getBoolean("debugUpdateCheck", false)
        if (BuildConfig.DEBUG) debugAutoUpdateMenuItem.isVisible = true

        // DEMO MODE
        val demoModeMenuItem = binding.toolbar.menu.findItem(R.id.action_demo_mode)
        demoModeMenuItem.isChecked = preferences.getBoolean("demoMode", false)
        if (BuildConfig.DEBUG) demoModeMenuItem.isVisible = true

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_source -> {
                    LinkUtil.Builder(this)
                        .context(this)
                        .link(getString(R.string.github_source_url))
                        .open()
                    return@setOnMenuItemClickListener true
                }

                R.id.action_licenses -> {
                    LibsBuilder()
                        .withLicenseShown(true)
                        .withAboutIconShown(true)
                        .withVersionShown(true)
                        .withActivityTitle(getString(R.string.licenses))
                        .withAboutDescription("test description")
                        .start(this)

                    return@setOnMenuItemClickListener true
                }

                R.id.action_send_email -> {
                    MailUtil.Builder(this)
                        .subject("Report")
                        .includeDeviceInfo(true)
                        .send()
                    return@setOnMenuItemClickListener true
                }

                R.id.action_update_check -> {
                    autoUpdateMenuItem.isChecked = !autoUpdateMenuItem.isChecked
                    setAutoUpdateCheck(autoUpdateMenuItem.isChecked)
                    startUpdateCheckSchedule(autoUpdateMenuItem.isChecked)
                    return@setOnMenuItemClickListener true
                }

                R.id.action_update_check_debug -> {
                    debugAutoUpdateMenuItem.isChecked = !debugAutoUpdateMenuItem.isChecked
                    setDebugUpdateCheck(debugAutoUpdateMenuItem.isChecked)
                    return@setOnMenuItemClickListener true
                }

                R.id.action_demo_mode -> {
                    demoModeMenuItem.isChecked = !demoModeMenuItem.isChecked
                    demoMode(demoModeMenuItem.isChecked )
                    return@setOnMenuItemClickListener true
                }
                else -> false
            }
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.action_start -> {
                    supportFragmentManager.commit { replace(R.id.fragment_container, MainFragment()) }
                    true
                }
                R.id.action_graph -> {
                    supportFragmentManager.commit { replace(R.id.fragment_container, GraphFragment(EmptyCoroutineContext)) }
                    true
                }
                else -> false
            }
        }

        //CHECK FOR NOTIFICATION PERMISSION
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            val requestPermissionLauncher =
                registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    if (!isGranted) {
                        Snackbar.make(binding.appBarLayout, R.string.permissionInSettings, Snackbar.LENGTH_INDEFINITE)
                            .show()
                    }
                }

            when {
                ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED -> {
                    requestPermissionLauncher.launch(POST_NOTIFICATIONS)
                }
            }
        }

        //CREATE NOTIFICATION CHANNEL IF FIRST START
        if (isFirstStart()) {
            val clientNotificationManager = ClientNotificationManager(this)
            clientNotificationManager.createChannel()
            gitHubUpdater.createNotificationChannel()
            startUpdateCheckSchedule(true)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                placeQsTile()
        }

        //OPTIMIZE TOOLBAR HEIGHT
        val layoutParams = binding.appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.height = resources.configuration.densityDpi

        //CHECK FOR UPDATE
        val autoUpdateCheck = preferences.getBoolean("autoUpdateCheck", true)
        val debugUpdateCheck = preferences.getBoolean("debugUpdateCheck", false)

        if((autoUpdateCheck && !BuildConfig.DEBUG) || (BuildConfig.DEBUG && debugUpdateCheck)){
            val extras = intent.extras
            //CHECK IF NOTIFICATION WAS TAPPED
            if (extras == null) {
                checkForUpdate()
            } else {
                //START UPDATE DIALOG
                gitHubUpdater.downloadDialog(
                    intent.getStringExtra("releaseName")!!,
                    intent.getStringExtra("releaseBody")!!,
                    intent.getStringExtra("releaseLink")!!,
                    intent.getStringExtra("releaseFileName")!!
                )
            }
        }
        checkBatteryOptimization()
    }

    private fun checkForUpdate(){
        val updateCheckWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<UpdateCheckWorker>().build()
        workManager.enqueue(updateCheckWorkRequest)
    }

    private fun startUpdateCheckSchedule(enable: Boolean){
        if(enable){
            val updateCheckPeriodicWorkRequest: PeriodicWorkRequest = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
                12,
                TimeUnit.HOURS,
                10, TimeUnit.MINUTES)
                .build()

            workManager.enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.REPLACE, updateCheckPeriodicWorkRequest)
        } else {
            workManager.cancelUniqueWork(TAG)
        }
    }

    private fun isFirstStart() : Boolean {
        val preferences = getSharedPreferences("preferences", MODE_PRIVATE)!!
        return if(preferences.getBoolean("isFirstStart", true)){
            val prefs : SharedPreferences = getSharedPreferences("preferences", MODE_PRIVATE)!!
            val editor : SharedPreferences.Editor = prefs.edit()
            editor.putBoolean("isFirstStart", false)
            editor.commit()
            true
        } else false
    }

    private fun checkBatteryOptimization(){
        val intent = Intent()
        val powerManager : PowerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        if(!powerManager.isIgnoringBatteryOptimizations(packageName)){
            Snackbar.make(binding.appBarLayout, R.string.batt_optimization, Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(getColor(R.color.text))
                .setAction(R.string.batt_disable){
                    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                }
                .show()
        }
    }

    @SuppressLint("NewApi")
    private fun placeQsTile(){
        val statusBarManager = getSystemService(STATUS_BAR_SERVICE) as StatusBarManager
        statusBarManager.requestAddTileService(
            ComponentName(
                this,
                ClientTileService::class.java
            ),
            getString(R.string.app_name),
            Icon.createWithResource(this, R.drawable.ic_notification_icon),
            {},
            {}
        )
    }

    private fun setAutoUpdateCheck(isEnabled: Boolean){
        val editor : SharedPreferences.Editor = preferences.edit()
        editor.putBoolean("autoUpdateCheck", isEnabled)
        editor.commit()
    }

    private fun setDebugUpdateCheck(isEnabled: Boolean) {
        val editor : SharedPreferences.Editor = preferences.edit()
        editor.putBoolean("debugUpdateCheck", isEnabled)
        editor.commit()
    }

    private fun demoMode(demoMode: Boolean) {
        val editor : SharedPreferences.Editor = preferences.edit()
        editor.putBoolean("demoMode", demoMode)
        editor.commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}