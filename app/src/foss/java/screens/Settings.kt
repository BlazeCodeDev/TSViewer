/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2023.
 *
 */

package screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.blazecode.eventtool.views.DefaultPreference
import com.blazecode.eventtool.views.EditTextPreference
import com.blazecode.eventtool.views.PreferenceGroup
import com.blazecode.eventtool.views.SliderPreference
import com.blazecode.eventtool.views.SwitchPreference
import com.blazecode.tsviewer.R
import com.blazecode.tsviewer.navigation.NavRoutes
import com.blazecode.tsviewer.ui.theme.TSViewerTheme
import viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(viewModel: SettingsViewModel = viewModel(), navController: NavController) {
    TSViewerTheme {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
        Scaffold (
            contentWindowInsets = WindowInsets(0.dp),
            topBar = { TopAppBar(scrollBehavior, navController) },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            content = { paddingValues ->
                Column(modifier = Modifier.padding(paddingValues).fillMaxSize().verticalScroll(rememberScrollState())){
                    MainLayout(viewModel, navController)
                }
            }
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun MainLayout(viewModel: SettingsViewModel, navController: NavController) {
    val uiState = viewModel.uiState.collectAsState()

    Column {
        PreferenceGroup(title = stringResource(R.string.general)) {
            SliderPreference(
                title = stringResource(R.string.update_interval),
                icon = painterResource(R.drawable.ic_update),
                value = uiState.value.scheduleTime,
                steps = 6,
                valueRange = 15f..120f,
                unitSuffix = "min",
                onValueChange = { viewModel.setScheduleTime(it) }
            )
            SwitchPreference(
                title = stringResource(R.string.only_wifi),
                icon = painterResource(R.drawable.ic_wifi),
                checked = uiState.value.executeOnlyOnWifi,
                onCheckChanged = { viewModel.setExecuteOnlyOnWifi(it) }
            )
            SwitchPreference(
                title = stringResource(R.string.include_query_clients),
                icon = painterResource(R.drawable.ic_query_client),
                checked = uiState.value.includeQueryClients,
                onCheckChanged = { viewModel.setIncludeQueryClients(it) }
            )
        }
        PreferenceGroup(title = stringResource(R.string.connection)) {
            EditTextPreference(
                title = stringResource(R.string.ip_address),
                text = uiState.value.ip,
                placeholder = stringResource(R.string.default_ip_address),
                icon = painterResource(R.drawable.ic_ip),
                singleLine = true,
                onTextChange = { viewModel.setIp(it) }
            )
            EditTextPreference(
                title = stringResource(R.string.query_username),
                text = uiState.value.username,
                placeholder = stringResource(R.string.default_query_user),
                icon = painterResource(R.drawable.ic_user),
                singleLine = true,
                onTextChange = { viewModel.setUsername(it) }
            )
            EditTextPreference(
                title = stringResource(R.string.query_password),
                text = uiState.value.password,
                placeholder = stringResource(R.string.default_query_password),
                icon = painterResource(R.drawable.ic_password),
                singleLine = true,
                isPassword = true,
                onTextChange = { viewModel.setPassword(it) }
            )
            EditTextPreference(
                title = stringResource(R.string.query_port),
                text = uiState.value.queryPort.toString(),
                icon = painterResource(R.drawable.ic_port),
                singleLine = true,
                isNumber = true,
                onTextChange = { viewModel.setQueryPort(it.toInt()) }
            )
            EditTextPreference(
                title = stringResource(R.string.virtual_server_id),
                text = uiState.value.virtualServerId.toString(),
                icon = painterResource(R.drawable.ic_virtual_server_id),
                singleLine = true,
                isNumber = true,
                onTextChange = { viewModel.setVirtualServerId(it.toInt()) }
            )
            Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                OutlinedButton(
                    onClick = { if(uiState.value.connectionSuccessful == null) viewModel.testConnection() },
                    modifier = Modifier.padding(dimensionResource(R.dimen.small_padding)),
                    content = {
                        val text = if(uiState.value.connectionSuccessful == null) stringResource(R.string.test_connection)
                                        else if (uiState.value.connectionSuccessful!!) stringResource(R.string.connection_successful)
                                        else stringResource(R.string.connection_failed)

                        AnimatedContent (
                            targetState = text,
                            transitionSpec = { fadeIn() with fadeOut()
                            }){ targetText ->
                            Text(text = targetText)
                        }
                    }
                )
            }
            PreferenceGroup(title = stringResource(R.string.other), modifier = Modifier.padding(bottom = dimensionResource(R.dimen.medium_padding))) {
                DefaultPreference(
                    title = stringResource(R.string.about),
                    icon = painterResource(R.drawable.ic_info),
                    onClick = {
                        navController.navigate(NavRoutes.About.route)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(scrollBehavior: TopAppBarScrollBehavior, navController: NavController){
    LargeTopAppBar(
        title = { Text(text = stringResource(R.string.settings)) },
        navigationIcon = {
            Box (modifier = Modifier.size(dimensionResource(R.dimen.icon_button_size)).clickable { navController.navigate(NavRoutes.Home.route) },
                contentAlignment = Alignment.Center){
                Icon(painterResource(R.drawable.ic_back), "back")
            }
        },
        scrollBehavior = scrollBehavior
    )
}