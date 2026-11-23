package com.phoneguardian.security.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val titleArabic: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "الرئيسية", Icons.Default.Shield)
    object Findings : Screen("findings", "المخاطر", Icons.Default.Security)
    object ActionLog : Screen("action_log", "سجل الإجراءات", Icons.Default.List)
    object History : Screen("history", "سجل الفحص", Icons.Default.History)
    object Chat : Screen("chat", "المستشار الذكي", Icons.Default.Chat)
    object Settings : Screen("settings", "الإعدادات", Icons.Default.Settings)
}
