package com.phoneguardian.security.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.phoneguardian.security.PhoneGuardianApp
import com.phoneguardian.security.domain.model.SecurityFinding
import com.phoneguardian.security.ui.navigation.Screen
import com.phoneguardian.security.ui.screens.actionlog.ActionLogScreen
import com.phoneguardian.security.ui.screens.chat.AiChatViewModel
import com.phoneguardian.security.ui.screens.chat.AiSecurityChatScreen
import com.phoneguardian.security.ui.screens.dashboard.DashboardScreen
import com.phoneguardian.security.ui.screens.dashboard.DashboardViewModel
import com.phoneguardian.security.ui.screens.findings.FindingsScreen
import com.phoneguardian.security.ui.screens.history.ScanHistoryScreen
import com.phoneguardian.security.ui.screens.settings.SettingsScreen
import com.phoneguardian.security.ui.theme.CardDark
import com.phoneguardian.security.ui.theme.GuardEmerald
import com.phoneguardian.security.ui.theme.PhoneGuardianTheme
import com.phoneguardian.security.ui.theme.TextMuted
import com.phoneguardian.security.ui.theme.TextPrimary

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // Notification permission handled
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            PhoneGuardianTheme {
                MainAppScaffold(
                    onOpenSettingIntent = { finding -> openSettingForFinding(finding) }
                )
            }
        }
    }

    private fun openSettingForFinding(finding: SecurityFinding) {
        val action = finding.deepLinkAction ?: return
        try {
            val intent = Intent(action).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                finding.deepLinkData?.let { dataUri ->
                    data = Uri.parse(dataUri)
                }
            }
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to application details if specific setting fails
            finding.packageName?.let { pkg ->
                try {
                    val fallbackIntent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:$pkg")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(fallbackIntent)
                } catch (ex: Exception) {
                    // Ignore
                }
            }
        }
    }
}

@Composable
fun MainAppScaffold(
    onOpenSettingIntent: (SecurityFinding) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val chatViewModel: AiChatViewModel = viewModel()
    val dashboardViewModel: DashboardViewModel = viewModel()

    val navScreens = listOf(
        Screen.Dashboard,
        Screen.Findings,
        Screen.ActionLog,
        Screen.History,
        Screen.Chat,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = CardDark) {
                navScreens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(imageVector = screen.icon, contentDescription = screen.titleArabic)
                        },
                        label = {
                            Text(text = screen.titleArabic, fontSize = 10.sp)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = GuardEmerald,
                            indicatorColor = GuardEmerald,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToFindings = { navController.navigate(Screen.Findings.route) },
                    onNavigateToChatWithFinding = { finding ->
                        chatViewModel.initiateChatForFinding(finding)
                        navController.navigate(Screen.Chat.route)
                    },
                    onOpenSettingsIntent = onOpenSettingIntent
                )
            }

            composable(Screen.Findings.route) {
                FindingsScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToChatWithFinding = { finding ->
                        chatViewModel.initiateChatForFinding(finding)
                        navController.navigate(Screen.Chat.route)
                    },
                    onOpenSettingsIntent = onOpenSettingIntent
                )
            }

            composable(Screen.ActionLog.route) {
                ActionLogScreen()
            }

            composable(Screen.History.route) {
                ScanHistoryScreen()
            }

            composable(Screen.Chat.route) {
                AiSecurityChatScreen(viewModel = chatViewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
