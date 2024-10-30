/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

package com.blazecode.tsviewer.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import com.blazecode.tsviewer.data.ErrorCode

class ErrorHandler(val context: Context) {

    private var cutException: String = ""

    fun reportError(exception: String) {

        var code : ErrorCode = ErrorCode.NO_ERROR

        if (isAirplaneMode(context)){
            // AIRPLANE MODE IS ACTIVE
            code = ErrorCode.AIRPLANE_MODE
        } else if (!hasCellReception()){
            // NO INTERNET CONNECTION
            code = ErrorCode.NO_NETWORK
        }

        if(code != ErrorCode.NO_ERROR) {
            // DISPLAY ERROR MESSAGE
            //USED FOR DISPLAYING THE TOAST WITHOUT A VIEW
            //CANNOT CREATE MULTIPLE LOOPERS
            if (Looper.myLooper() == null) {
                Looper.prepare()
            }

            //DISPLAY EXCEPTION
            cutException = when {
                exception.contains(">>") -> exception.split(">>")[1].trim()
                exception.contains("Exception:") -> exception.split("Exception:")[1].trim()
                else -> exception
            }
            Toast.makeText(context, cutException, Toast.LENGTH_LONG).show()
            println(exception)
        }
    }

    private fun hasCellReception() : Boolean{
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    fun isAirplaneMode(context: Context): Boolean {
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON, 0
        ) != 0
    }
}