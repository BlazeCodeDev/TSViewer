/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2023.
 *
 */

package com.blazecode.tsviewer.database

import androidx.room.*
import com.blazecode.tsviewer.data.TsClient

@Dao
interface ClientDAO{
    @Query("SELECT * FROM tsclient")
    fun getAll(): MutableList<TsClient>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertClientData(vararg client: TsClient)

    @Query("SELECT * FROM tsclient WHERE nickname = :nickname")
    fun getClientByName(nickname: String): TsClient?

    @Delete
    fun delete(client: TsClient)
}
