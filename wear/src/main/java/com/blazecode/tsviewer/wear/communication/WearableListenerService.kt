/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2023.
 *
 */

package com.blazecode.tsviewer.wear.communication

import android.widget.Toast
import data.DataHolder
import com.blazecode.tsviewer.wear.complication.ComplicationProvider
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.google.gson.GsonBuilder
import data.WearDataPackage

class WearableListenerService: WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        when (messageEvent.path) {
            CLIENTS_PATH -> {
                val gson = GsonBuilder().create()
                val data = gson.fromJson(String(messageEvent.data), WearDataPackage::class.java)

                if(!data.clients.isNullOrEmpty()){
                    DataHolder.list = data.clients.toMutableList()
                    ComplicationProvider().update(this)
                } else {
                    DataHolder.list = mutableListOf()
                    ComplicationProvider().update(this)
                }
                DataHolder.time = data.timestamp
            }
            TEST_PATH -> {
                Toast.makeText(this, "TESTING\n${String(messageEvent.data)}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val CLIENTS_PATH = "/clients"
        private const val TEST_PATH = "/test"
    }
}