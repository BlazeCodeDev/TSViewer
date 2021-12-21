package com.blazecode.tsviewer.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.work.*
import com.blazecode.tsviewer.R
import com.blazecode.tsviewer.databinding.MainFragmentAdvancedLayoutBinding
import com.blazecode.tsviewer.databinding.MainFragmentBinding
import com.blazecode.tsviewer.databinding.MainFragmentScheduleLayoutBinding
import com.blazecode.tsviewer.util.ClientsWorker
import com.blazecode.tsviewer.util.ErrorHandler
import java.util.concurrent.TimeUnit


class MainFragment : Fragment() {

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var advancedLayoutBinding: MainFragmentAdvancedLayoutBinding
    private lateinit var scheduleLayoutBinding: MainFragmentScheduleLayoutBinding

    private val errorHandler = context?.let { ErrorHandler(it) }
    private lateinit var workManager: WorkManager
    private var isWorkScheduled : Boolean = false

    private var IP_ADRESS : String = ""
    private var USERNAME : String = ""
    private var PASSWORD : String = ""
    private var NICKNAME : String = "TSViewer"
    private var RANDOMIZE_NICKNAME : Boolean = true
    private var INCLUDE_QUERY_CLIENTS : Boolean = false
    private var SCHEDULE_TIME : Float = 15f
    private var RUN_ONLY_WIFI : Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        advancedLayoutBinding = MainFragmentAdvancedLayoutBinding.bind(_binding!!.root)
        scheduleLayoutBinding = MainFragmentScheduleLayoutBinding.bind(_binding!!.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        workManager = context?.let { WorkManager.getInstance(it) }!!

        binding.inputEditTextIp.addTextChangedListener(){
            IP_ADRESS = it.toString()
            savePreferences()
        }

        binding.inputEditTextUsername.addTextChangedListener(){
            USERNAME = it.toString()
            savePreferences()
        }

        binding.inputEditTextPassword.addTextChangedListener(){
            PASSWORD = it.toString()
            savePreferences()
        }

        binding.buttonAdvanced.setOnClickListener {
            binding.advancedLayout.isVisible = !binding.advancedLayout.isVisible
            if (binding.buttonAdvanced.text == getString(R.string.more)) binding.buttonAdvanced.text = getString(R.string.less)
            else binding.buttonAdvanced.text = getString(R.string.more)
        }

        advancedLayoutBinding.inputEditTextNickname.addTextChangedListener(){
            NICKNAME = it.toString()
            savePreferences()
        }

        advancedLayoutBinding.switchNicknameRandomize.setOnCheckedChangeListener { compoundButton, isChecked ->
            RANDOMIZE_NICKNAME = isChecked
            savePreferences()
        }

        advancedLayoutBinding.buttonInfoRandomize.setOnClickListener {
            TODO("Randomize Info Button")
        }

        advancedLayoutBinding.switchIncludeQueryClients.setOnCheckedChangeListener { compoundButton, isChecked ->
            INCLUDE_QUERY_CLIENTS = isChecked
            savePreferences()
        }

        advancedLayoutBinding.buttonInfoIncludeQueryClients.setOnClickListener {
            TODO("Include Query Clients Info Button")
        }

        advancedLayoutBinding.switchOnlyWiFi.setOnCheckedChangeListener { compoundButton, isChecked ->
            RUN_ONLY_WIFI = isChecked
            savePreferences()
        }

        scheduleLayoutBinding.timeSlider.setLabelFormatter { "${scheduleLayoutBinding.timeSlider.value.toString().split(".")[0]} min" }
        scheduleLayoutBinding.timeSlider.addOnChangeListener { slider, value, fromUser ->
            SCHEDULE_TIME = value
            scheduleLayoutBinding.textViewScheduleTime.text = "${scheduleLayoutBinding.timeSlider.value.toString().split(".")[0]} min"
            savePreferences()
        }

        binding.buttonLogIn.setOnClickListener {
            if(isAllInfoProvided()){
                val clientWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<ClientsWorker>()
                    .build()
                workManager.enqueue(clientWorkRequest)
            }
        }

        scheduleLayoutBinding.buttonStartSchedule.setOnClickListener {
            val clientWorkRequest: PeriodicWorkRequest = PeriodicWorkRequestBuilder<ClientsWorker>(
                SCHEDULE_TIME.toLong(),                                                                                 //GIVE NEW WORK TIME
                TimeUnit.MINUTES,
                1, TimeUnit.MINUTES)                                                                      //FLEX TIME INTERVAL
                .build()

            if(isAllInfoProvided() && !isWorkScheduled){
                val oneTimeclientWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<ClientsWorker>().build()          //RUN ONE TIME
                workManager.enqueue(oneTimeclientWorkRequest)

                workManager.enqueueUniquePeriodicWork("scheduleClients", ExistingPeriodicWorkPolicy.REPLACE, clientWorkRequest)     //SCHEDULE THE NEXT RUNS
                isWorkScheduled = true
                scheduleLayoutBinding.buttonStartSchedule.text = getString(R.string.stop_schedule)
            } else if (isWorkScheduled) {
                workManager.cancelWorkById(clientWorkRequest.id)
                isWorkScheduled = false
                scheduleLayoutBinding.buttonStartSchedule.text = getString(R.string.start_schedule)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        savePreferences()
    }

    override fun onResume() {
        super.onResume()
        loadPreferences()
    }

    fun savePreferences(){
        val preferences : SharedPreferences = context?.getSharedPreferences("preferences", AppCompatActivity.MODE_PRIVATE)!!
        val editor : SharedPreferences.Editor = preferences.edit()
        editor.putString("ip", IP_ADRESS)
        editor.putString("user", USERNAME)
        editor.putString("pass", PASSWORD)
        editor.putString("nick", NICKNAME)
        editor.putBoolean("randNick", RANDOMIZE_NICKNAME)
        editor.putBoolean("includeQuery", INCLUDE_QUERY_CLIENTS)
        editor.putFloat("scheduleTime", SCHEDULE_TIME)
        editor.putBoolean("run_only_wifi", RUN_ONLY_WIFI)
        editor.putBoolean("isWorkScheduled", isWorkScheduled)
        editor.commit()
    }

    fun loadPreferences(){
        val preferences = context?.getSharedPreferences("preferences", AppCompatActivity.MODE_PRIVATE)!!
        IP_ADRESS = preferences.getString("ip", "").toString()
        USERNAME = preferences.getString("user", "").toString()
        PASSWORD = preferences.getString("pass", "").toString()
        NICKNAME = preferences.getString("nick", getString(R.string.app_name)).toString()
        RANDOMIZE_NICKNAME = preferences.getBoolean("randNick", true)
        INCLUDE_QUERY_CLIENTS = preferences.getBoolean("includeQuery", false)
        SCHEDULE_TIME = preferences.getFloat("scheduleTime", 15f)
        RUN_ONLY_WIFI = preferences.getBoolean("run_only_wifi", true)
        isWorkScheduled = preferences.getBoolean("isWorkScheduled", false)

        loadViews()
    }

    fun loadViews(){
        binding.inputEditTextIp.setText(IP_ADRESS)
        binding.inputEditTextUsername.setText(USERNAME)
        binding.inputEditTextPassword.setText(PASSWORD)
        advancedLayoutBinding.inputEditTextNickname.setText(NICKNAME)
        if(isWorkScheduled) scheduleLayoutBinding.buttonStartSchedule.text = getString(R.string.stop_schedule)
        if(!isWorkScheduled) scheduleLayoutBinding.buttonStartSchedule.text = getString(R.string.start_schedule)
        advancedLayoutBinding.switchNicknameRandomize.isChecked = RANDOMIZE_NICKNAME
        advancedLayoutBinding.switchIncludeQueryClients.isChecked = INCLUDE_QUERY_CLIENTS
        advancedLayoutBinding.switchOnlyWiFi.isChecked = RUN_ONLY_WIFI
        scheduleLayoutBinding.timeSlider.value = SCHEDULE_TIME
        scheduleLayoutBinding.textViewScheduleTime.text = "${SCHEDULE_TIME.toString().split(".")[0]} min"
    }

    fun isAllInfoProvided() : Boolean {
        if (binding.inputEditTextIp.text.isNullOrEmpty() || binding.inputEditTextUsername.text.isNullOrEmpty() || binding.inputEditTextPassword.text.isNullOrEmpty()){
            if(binding.inputEditTextIp.text.isNullOrEmpty()) binding.inputEditTextIp.error = getString(R.string.mustBeProvided)
            if(binding.inputEditTextUsername.text.isNullOrEmpty()) binding.inputEditTextUsername.error = getString(R.string.mustBeProvided)
            if(binding.inputEditTextPassword.text.isNullOrEmpty()) binding.inputEditTextPassword.error = getString(R.string.mustBeProvided)

            return false
        } else {
            return true
        }
    }
}