/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2023.
 *
 */

package com.blazecode.tsviewer.database

import android.content.Context
import com.blazecode.tsviewer.data.TsServerInfo

class ServerRepository(context: Context) {

    val database = ServerDatabase.build(context)
    val serverDao = database.serverDao()

    fun getServerInfo(): MutableList<TsServerInfo> {
        return serverDao.getAll()
    }

    fun getLast3Days(): MutableList<TsServerInfo> {
        return serverDao.getLast3Days()
    }

    fun insertServerInfo(serverInfo: TsServerInfo) {
        serverDao.insertServerInfo(serverInfo)
    }

    fun getTimestampOfLastUpdate(): Long {
        return serverDao.getTimestampOfLastUpdate()
    }
}