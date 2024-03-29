/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2023.
 *
 */

package com.blazecode.scrapguidev2.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import timber.log.Timber

// TUTORIAL
// https://www.baeldung.com/kotlin/builder-pattern

private var aboutLinkFailed = false

class LinkUtil private constructor(
    val context: Context,
    val link: String
) {

    data class Builder(
        var context: Context,
        var link: String = "null"
    ) {

        fun context(context: Context) = apply { this.context = context }
        fun link(link: String) = apply { this.link = link }
        fun open() = LinkUtil(context, link).openLink(this)
    }

    fun openLink(builder: Builder) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(builder.link))
            browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            builder.context.startActivity(browserIntent)
        } catch (e: Exception) {
            Timber.log(Log.ERROR, "LinkUtil Link Failed: $e")
            if (!aboutLinkFailed) {
                Toast.makeText(
                    builder.context,
                    "No Browser found\nTap again to see Error Log",
                    Toast.LENGTH_SHORT
                ).show()
                aboutLinkFailed = true
            } else {
                Toast.makeText(builder.context, "No Browser found\n\n$e", Toast.LENGTH_LONG).show()
            }
        }
    }
}