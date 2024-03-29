/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2023.
 *
 */

package com.blazecode.tsviewer.util.tile

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blazecode.tsviewer.MainActivity
import com.blazecode.tsviewer.R


class ClientTileService : TileService() {

    override fun onClick() {
        super.onClick()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if(Build.VERSION.SDK_INT >= 34){
                val intent = Intent(applicationContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                startActivityAndCollapse(pi)
            } else {
                startActivityAndCollapse(Intent(applicationContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        } else {
            Toast.makeText(this, resources.getString(R.string.not_supported_below_android_ten), Toast.LENGTH_LONG).show()
        }
    }

    override fun onStartListening() {
        super.onStartListening()

        //GET SHARED PREFERENCES
        val tileSetting = this.getSharedPreferences("tile", AppCompatActivity.MODE_PRIVATE)!!
        val tileActive = tileSetting.getBoolean("stateActive", false)

        //SET STATE
        if(tileActive) qsTile.state = Tile.STATE_ACTIVE
        else qsTile.state = Tile.STATE_INACTIVE

        //SET SUBTITLE
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            qsTile.subtitle = tileSetting.getString("subtitle", "")
        }

        //APPLY
        qsTile.updateTile()
    }

    override fun onTileAdded() {
        super.onTileAdded()

        //ENABLE TILE IF DEVICE IS NEWER OR EQUAL ANDROID 10
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            qsTile.state = Tile.STATE_INACTIVE
            qsTile.updateTile()
        } else {
            qsTile.label = "Not supported"
            qsTile.updateTile()
        }
    }
}