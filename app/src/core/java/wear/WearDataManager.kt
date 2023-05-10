/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2023.
 *
 */

package wear

import android.content.Context
import com.blazecode.tsviewer.data.TsClient
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import data.WearDataPackage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.lang.reflect.Type

class WearDataManager(context: Context) {

    private val messageClient by lazy { Wearable.getMessageClient(context) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(context) }

    companion object {
        private const val WEAR_CAPABILITY = "wear"
        private const val CLIENTS_PATH = "/clients"
        private const val TEST_PATH = "/test"
    }

    fun sendClientList(list: MutableList<TsClient>){
        val gson = GsonBuilder().create()
        val gsonType: Type = object : TypeToken<WearDataPackage>() {}.type

        val json = gson.toJson(WearDataPackage(list, System.currentTimeMillis()), gsonType)
        send(CLIENTS_PATH, json)
        Timber.d("Sent data to wear: $json")
    }

    fun sendTestMessage() {
        send(TEST_PATH, "This is a test message")
    }

    suspend fun areNodesAvailable(): Boolean{
        val nodes = capabilityClient
            .getCapability(WEAR_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
            .await()
            .nodes
        return nodes.isNotEmpty()
    }

    private fun send(path: String, data: String) {
        GlobalScope.launch {
            try {
                val nodes = capabilityClient
                    .getCapability(WEAR_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
                    .await()
                    .nodes

                // Send a message to all nodes in parallel
                nodes.map { node ->
                    async {
                        messageClient.sendMessage(node.id, path, data.toByteArray())
                            .await()
                    }
                }.awaitAll()

                Timber.i("Starting activity requests sent successfully")
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Timber.i("Error sending start activity requests: $exception")
            }
        }
    }
}