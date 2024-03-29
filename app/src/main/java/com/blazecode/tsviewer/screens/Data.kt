/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2023.
 *
 */

package com.blazecode.tsviewer.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.blazecode.tsviewer.R
import com.blazecode.tsviewer.data.Entry
import com.blazecode.tsviewer.data.TsClient
import com.blazecode.tsviewer.data.TsServerInfo
import com.blazecode.tsviewer.navigation.NavRoutes
import com.blazecode.tsviewer.ui.theme.TSViewerTheme
import com.blazecode.tsviewer.ui.theme.Typography
import com.blazecode.tsviewer.util.graphmarker.rememberMarker
import com.blazecode.tsviewer.viewmodels.DataViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.ChartScrollSpec
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.marker.MarkerLabelFormatter
import com.patrykandpatrick.vico.core.scroll.AutoScrollCondition
import com.patrykandpatrick.vico.core.scroll.InitialScroll
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Data(viewModel: DataViewModel = viewModel(), navController: NavController) {
    TSViewerTheme {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
        Scaffold (
            contentWindowInsets = WindowInsets(0.dp),
            topBar = { TopAppBar(scrollBehavior, navController) },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            content = { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()){
                    MainLayout(viewModel)
                }
            }
        )
    }
}

@Composable
private fun MainLayout(viewModel: DataViewModel) {
    val uiState = viewModel.uiState.collectAsState()

    val currentView = remember { mutableStateOf("loading") }
    if(uiState.value.clientList?.isNotEmpty() == true){
        currentView.value = "done"
    } else if(uiState.value.clientList != null && uiState.value.clientList?.isEmpty() == true){
        currentView.value = "no_data"
    } else {
        currentView.value = "loading"
    }

    Crossfade(currentView, label = ""){
        when(it.value){
            "loading" -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                    CircularProgressIndicator()
                }
            }
            "no_data" -> {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_empty_database))
                val progress by animateLottieCompositionAsState(composition = composition)
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                        )
                        Text(text = stringResource(R.string.no_data))
                    }
                }
            }
            "done" -> {
                Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text(text = stringResource(R.string.utilization), style = Typography.titleMedium)
                    ChartView(uiState.value.serverInfoList)
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(text = stringResource(R.string.clients), style = Typography.titleMedium)
                    ClientListView(
                        list = uiState.value.clientList!!,
                        onClick = { client ->
                            viewModel.openClientInfoSheet(client)
                        }
                    )
                }
            }
        }
    }

    if (uiState.value.isClientInfoSheetVisible) {
        ClientInfoDialog(
            viewModel = viewModel,
            client = uiState.value.clientInfoSheetClient!!
        )
    }
}

@Composable
private fun ChartView(inputList: List<TsServerInfo>){
    val list = inputList
    val chartEntryModelProducer = list.mapIndexed { index, tsServerInfo ->
        Entry(index.toFloat(), tsServerInfo.clients.size.toFloat(), tsServerInfo) }
        .let { ChartEntryModelProducer(it) }

    val xAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, chartValues ->
        chartValues.chartEntryModel.entries.firstOrNull()?.getOrNull(value.toInt())?.let { entry ->

            val date = Date((entry as Entry).tsServerInfo.timestamp)
            val simpleDateFormat = SimpleDateFormat("HH")
            simpleDateFormat.timeZone = TimeZone.getDefault()
            simpleDateFormat.format(date)

        }.toString()
    }

    val yAxisValueFormatter = AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
        value.roundToInt().toString()
    }

    val markerLabelFormatter = MarkerLabelFormatter { value, chartValues ->
        chartValues.chartEntryModel.entries.firstOrNull()?.getOrNull(value[0].index)?.let { entry ->
            val entry = ((entry as Entry).tsServerInfo)

            val date = Date(entry.timestamp)
            val simpleDateFormat = SimpleDateFormat("dd.MM.yy, HH:mm")
            simpleDateFormat.timeZone = TimeZone.getDefault()
            val dateString = simpleDateFormat.format(date)

            val builder: StringBuilder = StringBuilder()
            builder.append("$dateString\n")
            entry.clients.forEachIndexed { index, it ->
                if(index == entry.clients.size - 1)
                    builder.append(it.nickname)
                else
                    builder.append("${it.nickname}, ")
                if(index % 3 == 0){
                    builder.append("\n")
                }
            }
            builder.toString()
        }.toString()
    }

    Chart(
        chart = lineChart(
            lines = listOf(
                LineChart.LineSpec(
                    pointSizeDp = 22f,
                )
            )
        ),
        model = chartEntryModelProducer.getModel(),
        marker = rememberMarker(
            formatter = markerLabelFormatter
        ),
        startAxis = startAxis(
            valueFormatter = yAxisValueFormatter,
        ),
        bottomAxis = bottomAxis(
            valueFormatter = xAxisValueFormatter,
            guideline = null,
        ),
        chartScrollSpec = ChartScrollSpec(
            initialScroll = InitialScroll.Start,
            isScrollEnabled = true,
            autoScrollCondition = AutoScrollCondition.OnModelSizeIncreased,
            autoScrollAnimationSpec = spring()
        )
    )
}

@Composable
private fun ClientListView(list: List<TsClient>, onClick: (TsClient) -> Unit){
    LazyColumn {
        items(list.size) { index ->
            ClientItemView(list[index], onClick = onClick)
        }
    }
}

@Composable
private fun ClientItemView(client: TsClient, onClick: (TsClient) -> Unit){
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick(client) }.padding(horizontal = 4.dp, vertical = 2.dp)) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.background)))) {
            Text(text = client.nickname, modifier = Modifier.padding(4.dp))
        }
    }
}

@Composable
private fun ClientInfoDialog(viewModel: DataViewModel, client: TsClient){
    val nickname = client.nickname
    val dateTime = LocalDateTime.ofEpochSecond(client.lastSeen.toInstant().epochSecond, 0, TimeZone.getDefault().toZoneId().rules.getOffset(LocalDateTime.now()))
    val formatter = DateTimeFormatter.ofPattern("HH:mm, dd.MM.yyyy")
    val lastSeen = dateTime.format(formatter).toString()
    val activeConnectionTime = if(client.activeConnectionTime < 60) {
        "${client.activeConnectionTime} min"
    } else {
        val time = client.activeConnectionTime / 60 + if(client.activeConnectionTime % 60 > 0) 1 else 0
        "${time} h"
    }

    AlertDialog(
        title = { Text(stringResource(R.string.client_info)) },
        text = {
            Column {
                Text(text = stringResource(R.string.nickname), style = Typography.titleSmall, modifier = Modifier.padding(8.dp))
                Text(text= nickname, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

                Text(text = stringResource(R.string.last_seen), style = Typography.titleSmall, modifier = Modifier.padding(8.dp))
                Text(text= lastSeen, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

                Text(text = stringResource(R.string.active_connection_time), style = Typography.titleSmall, modifier = Modifier.padding(8.dp))
                Text(text= activeConnectionTime.toString(), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        },
        confirmButton = {},
        dismissButton = { OutlinedButton(onClick = { viewModel.closeClientInfoSheet() }) { Text(stringResource(R.string.close)) } },
        onDismissRequest = { viewModel.closeClientInfoSheet() }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(scrollBehavior: TopAppBarScrollBehavior, navController: NavController){
    LargeTopAppBar(
        title = { Text(text = stringResource(R.string.data)) },
        navigationIcon = {
            Box (modifier = Modifier.size(dimensionResource(R.dimen.icon_button_size)).clickable { navController.navigate(NavRoutes.Home.route) },
                contentAlignment = Alignment.Center){
                Icon(painterResource(R.drawable.ic_back), "back")
            }
        },
        scrollBehavior = scrollBehavior
    )
}