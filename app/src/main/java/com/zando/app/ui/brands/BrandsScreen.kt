package com.zando.app.ui.brands

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zando.app.ui.components.CartIconWithBadge
import com.zando.app.viewmodel.SearchViewModel

@Composable
fun BrandsScreen(
    viewModel: SearchViewModel,
    onNavigateToCart: () -> Unit,
    onBrandClick: (String) -> Unit,
    language: String
) {
    var selectedTab by remember { mutableStateOf("ALL") }
    val tabs = listOf("ALL", "SALE", "WOMEN", "MEN", "KIDS", "Z.HOME", "LIFESTYLE")
    
    // Translations
    val t = when (language) {
        "km" -> mapOf(
            "brands" to "ម៉ាក",
            "ALL" to "ទាំងអស់",
            "SALE" to "បញ្ចុះតម្លៃ",
            "WOMEN" to "នារី",
            "MEN" to "បុរស",
            "KIDS" to "កុមារ",
            "Z.HOME" to "ក្នុងផ្ទះ",
            "LIFESTYLE" to "រស់នៅ"
        )
        "zh" -> mapOf(
            "brands" to "品牌",
            "ALL" to "全部",
            "SALE" to "促销",
            "WOMEN" to "女装",
            "MEN" to "男装",
            "KIDS" to "童装",
            "Z.HOME" to "家居",
            "LIFESTYLE" to "生活方式"
        )
        else -> mapOf(
            "brands" to "Brands",
            "ALL" to "ALL",
            "SALE" to "SALE",
            "WOMEN" to "WOMEN",
            "MEN" to "MEN",
            "KIDS" to "KIDS",
            "Z.HOME" to "Z.HOME",
            "LIFESTYLE" to "LIFESTYLE"
        )
    }

    // Exact lists strictly matching user's provided images
    val displayedBrands = when (selectedTab) {
        "SALE" -> listOf("TAG SPACE", "SISBURMA", "Pomelo.", "ZANDO.HOME", "Hygge")
        "WOMEN" -> listOf("ZANDO.", "ROUTINE", "TEN+ELEVEN", "GATONI", "361°")
        "MEN" -> listOf("ZANDO.", "ROUTINE", "TEN+ELEVEN", "GATONI", "361°", "TAG SPACE", "SISBURMA")
        "KIDS" -> listOf("ZANDO.", "GATONI", "361°")
        "Z.HOME" -> listOf("ZANDO.HOME")
        "LIFESTYLE" -> listOf("Hygge")
        else -> listOf(
            "ZANDO.", "ROUTINE", "TEN+ELEVEN", "GATONI", "361°",
            "TAG SPACE", "SISBURMA", "Pomelo.", "ZANDO.HOME", "Hygge"
        )
    }

    val cartCount by viewModel.cartItemCount.collectAsState()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        "ZANDO.",
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                        CartIconWithBadge(count = cartCount, onClick = onNavigateToCart)
                    }
                }
                
                Text(
                    t["brands"]!!,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                ScrollableTabRow(
                    selectedTabIndex = tabs.indexOf(selectedTab),
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    divider = {},
                    indicator = { tabPositions ->
                        val index = tabs.indexOf(selectedTab)
                        if (index in tabPositions.indices) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[index]),
                                color = Color.Red
                            )
                        }
                    }
                ) {
                    tabs.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    t[tab] ?: tab,
                                    color = if (selectedTab == tab) Color.Red else MaterialTheme.colorScheme.outline,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 8.dp
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(displayedBrands.size) { index ->
                BrandItem(brand = displayedBrands[index], onClick = { onBrandClick(displayedBrands[index]) })
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun BrandItem(brand: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.align(Alignment.CenterStart).size(22.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = brand,
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = 1.sp
        )
    }
}
