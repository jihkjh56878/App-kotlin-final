package com.zando.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zando.app.model.UserProfile
import com.zando.app.model.UserRole

@Composable
fun ProfileScreen(
    userProfile: UserProfile?,
    onNavigateToOrders: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToFaq: () -> Unit,
    onNavigateToContactUs: () -> Unit,
    onLogout: () -> Unit,
    darkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    language: String,
    onLanguageChange: (String) -> Unit
) {
    val t = when (language) {
        "km" -> mapOf(
            "admin" to "បន្ទះគ្រប់គ្រង",
            "manage" to "ចូលទៅកាន់ Dashboard",
            "settings" to "ការកំណត់",
            "lang" to "ភាសា",
            "support" to "ការគាំទ្រ",
            "logout" to "ចាកចេញ"
        )
        "zh" -> mapOf(
            "admin" to "管理面板",
            "manage" to "打开仪表板",
            "settings" to "设置",
            "lang" to "语言",
            "support" to "支持",
            "logout" to "登出"
        )
        else -> mapOf(
            "admin" to "Admin Panel",
            "manage" to "Open Admin Dashboard",
            "settings" to "Settings",
            "lang" to "Language",
            "support" to "Support",
            "logout" to "Log Out"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) { Text(if (userProfile?.role == UserRole.ADMIN) "👑" else "👤", fontSize = 30.sp) }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(userProfile?.name ?: "User", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(userProfile?.email ?: "", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Conditional Admin Section
        if (userProfile?.role == UserRole.ADMIN) {
            SectionCard(title = t["admin"]!!) {
                ProfileMenuItem(
                    title = "Dashboard & Reports",
                    subtitle = t["manage"]!!,
                    icon = Icons.Default.AdminPanelSettings,
                    onClick = onNavigateToAdmin
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        SectionCard(title = "Account") {
            ProfileMenuItem("My Orders", "Track your purchases") { onNavigateToOrders() }
        }

        Spacer(Modifier.height(12.dp))

        SectionCard(title = t["settings"]!!) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🌙 Dark Mode", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = darkTheme, onCheckedChange = onToggleDarkTheme)
            }
        }

        Spacer(Modifier.height(12.dp))

        SectionCard(title = t["lang"]!!) {
            listOf("en" to "English 🇬🇧", "km" to "Khmer 🇰🇭", "zh" to "中文 🇨🇳").forEach { (code, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageChange(code) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = language == code, onClick = { onLanguageChange(code) })
                    Spacer(Modifier.width(8.dp))
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        SectionCard(title = t["support"]!!) {
            ProfileMenuItem("FAQs", "") { onNavigateToFaq() }
            ProfileMenuItem("Privacy Policy", "") { onNavigateToPrivacyPolicy() }
            ProfileMenuItem("Contact Us", "") { onNavigateToContactUs() }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) { Text(t["logout"]!!) }
        
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            content = content
        )
    }
}

@Composable
private fun ProfileMenuItem(title: String, subtitle: String, icon: ImageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            if (subtitle.isNotEmpty()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
    }
}
