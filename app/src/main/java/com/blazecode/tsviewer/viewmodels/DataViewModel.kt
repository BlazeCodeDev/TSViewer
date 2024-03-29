/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2023.
 *
 */

package com.blazecode.tsviewer.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.blazecode.tsviewer.data.TsClient
import com.blazecode.tsviewer.data.TsServerInfo
import com.blazecode.tsviewer.database.ClientRepository
import com.blazecode.tsviewer.database.ServerRepository
import com.blazecode.tsviewer.uistate.DataUiState
import com.blazecode.tsviewer.util.DemoModeValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import util.SettingsManager

class DataViewModel(val app: Application): AndroidViewModel(app){

    private val settingsManager = SettingsManager(app)

    // UI STATE
    private val _uiState = MutableStateFlow(DataUiState())
    val uiState: StateFlow<DataUiState> = _uiState.asStateFlow()

    init {
        if(!settingsManager.isDemoModeActive()){
            // NORMAL OPERATION
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(
                    serverInfoList = getServerInfoList(),
                    clientList = getClientList()
                )
            }
        } else {
            // DEMO MODE
            _uiState.value = _uiState.value.copy(
                serverInfoList = DemoModeValues.serverInfoList(),
                clientList = DemoModeValues.clientList()
            )
        }
    }

    fun openClientInfoSheet(client: TsClient) {
        _uiState.value = _uiState.value.copy(
            clientInfoSheetClient = client,
            isClientInfoSheetVisible = true
        )
    }

    fun closeClientInfoSheet() {
        _uiState.value = _uiState.value.copy(
            isClientInfoSheetVisible = false,
            clientInfoSheetClient = null
        )
    }

    private suspend fun getServerInfoList(): MutableList<TsServerInfo> {
        var list = mutableListOf<TsServerInfo>()
        val job = viewModelScope.launch(Dispatchers.IO) {
            val repository = ServerRepository(app)
            list = repository.getLast3Days()
        }
        job.join()
        return list
    }

    private suspend fun getClientList(): MutableList<TsClient> {
        var list = mutableListOf<TsClient>()
        val job = viewModelScope.launch(Dispatchers.IO) {
            val repository = ClientRepository(app)
            list = repository.getAllClients()
            list.sortByDescending { it.activeConnectionTime }
        }
        job.join()
        return list
    }
}