/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2023.
 *
 */

package com.blazecode.eventtool.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blazecode.tsviewer.R

@Composable
fun PreferenceGroup(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit){
    Box(modifier = modifier){
        Column {
            Box(modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.medium_padding), dimensionResource(R.dimen.medium_padding), 0.dp, 0.dp)){
                Text(text = title, color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
            }
            content()
        }
    }
}

@Preview
@Composable
private fun Preview(){
    Surface {
        PreferenceGroup(title = "GroupName"){
            DefaultPreference(icon = painterResource(R.drawable.ic_settings), title = "title") {}
        }
    }
}