/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2023.
 *
 */

package viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.blazecode.tsviewer.util.ConnectionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uistate.SettingsUiState
import util.SettingsManager
import wear.WearDataManager

class SettingsViewModel(val app: Application) : AndroidViewModel(app){

    private val settingsManager = SettingsManager(app)

    // UI STATE
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // INIT
    init {
        if(!settingsManager.isDemoModeActive()){
            // NORMAL OPERATION
            loadSettings()
            checkWearableConnection()
        } else {
            // DEMO MODE
            _uiState.value = settingsManager.getSettingsDemoUiState()
            _uiState.value = _uiState.value.copy(
                connectionSuccessful = true,
                foundWearable = true
            )
        }
    }

    // NETWORK
    fun testConnection(){
        val connectionManager = ConnectionManager(app)
        val connectionResult = connectionManager.testConnection(
            ip = uiState.value.ip,
            username = uiState.value.username,
            password = uiState.value.password,
            queryPort = uiState.value.queryPort,
            virtualServerId = uiState.value.virtualServerId)

        _uiState.value = _uiState.value.copy(connectionSuccessful = connectionResult)
    }

    // SETTERS
    fun setScheduleTime(scheduleTime: Float){
        _uiState.value = _uiState.value.copy(scheduleTime = scheduleTime)
        saveSettings()
    }

    fun setExecuteOnlyOnWifi(executeOnlyOnWifi: Boolean){
        _uiState.value = _uiState.value.copy(executeOnlyOnWifi = executeOnlyOnWifi)
        saveSettings()
    }

    fun setIncludeQueryClients(includeQueryClients: Boolean){
        _uiState.value = _uiState.value.copy(includeQueryClients = includeQueryClients)
        saveSettings()
    }

    fun setSyncWearable(syncWearable: Boolean){
        _uiState.value = _uiState.value.copy(syncWearable = syncWearable)
        saveSettings()
    }

    fun setIp(ip: String){
        _uiState.value = _uiState.value.copy(ip = ip, connectionSuccessful = null)
        saveSettings()
    }

    fun setUsername(username: String){
        _uiState.value = _uiState.value.copy(username = username, connectionSuccessful = null)
        saveSettings()
    }

    fun setPassword(password: String){
        _uiState.value = _uiState.value.copy(password = password, connectionSuccessful = null)
        saveSettings()
    }

    fun setQueryPort(queryPort: Int){
        _uiState.value = _uiState.value.copy(queryPort = queryPort, connectionSuccessful = null)
        saveSettings()
    }

    fun setVirtualServerId(virtualServerId: Int){
        _uiState.value = _uiState.value.copy(virtualServerId = virtualServerId, connectionSuccessful = null)
        saveSettings()
    }

    private fun checkWearableConnection(){
        viewModelScope.launch {
            val wearDataManager = WearDataManager(app)
            _uiState.value = _uiState.value.copy(
                foundWearable = wearDataManager.areNodesAvailable()
            )
        }
    }

    // SETTINGS
    private fun loadSettings(){
        val settingsManager = SettingsManager(app)
        _uiState.value = settingsManager.getSettingsUiState()
    }

    private fun saveSettings(){
        val settingsManager = SettingsManager(app)
        settingsManager.saveSettingsUiState(_uiState.value)
    }
}