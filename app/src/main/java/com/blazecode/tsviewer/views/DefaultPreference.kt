/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2023.
 *
 */

package com.blazecode.eventtool.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blazecode.tsviewer.R

@Composable
fun DefaultPreference(
    modifier: Modifier = Modifier,
    title: String,
    icon: Painter? = null,
    summary: String? = null,
    onClick: () -> Unit){
    Box(modifier = modifier){
        Card (modifier = Modifier.padding(dimensionResource(R.dimen.medium_padding), dimensionResource(R.dimen.small_padding), dimensionResource(R.dimen.medium_padding), 0.dp).clickable(onClick = onClick)){
            Row (modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.small_padding)), verticalAlignment = Alignment.CenterVertically){
                if(icon != null){
                    Box (modifier = Modifier.size(dimensionResource(R.dimen.icon_button_size)).weight(1f), contentAlignment = Alignment.Center){
                        Icon(icon, "")
                    }
                }
                Column (modifier = Modifier.weight(6f, true)){
                    Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                    if(!summary.isNullOrEmpty()) Text(summary, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview(){
    Column{
        DefaultPreference(icon = painterResource(R.drawable.ic_settings), title = "title"){}
        DefaultPreference(icon = painterResource(R.drawable.ic_settings), title = "title", summary = "summary"){}
    }
}
