package com.zando.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zando.app.ui.components.*
import com.zando.app.ui.theme.*
import com.zando.app.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSearch: (category: String?, brand: String?) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToProduct: (Int) -> Unit,
    language: String
) {
    val cartCount by viewModel.cartItemCount.collectAsState()
    val trendingProducts by viewModel.trendingProducts.collectAsState()
    val newArrivals by viewModel.newArrivals.collectAsState()
    val scope = rememberCoroutineScope()

    // Translations
    val t = when (language) {
        "km" -> mapOf(
            "hello" to "សួស្តី 👋",
            "shop_brand" to "ទិញតាមម៉ាក",
            "shop_cat" to "ទិញតាមប្រភេទ",
            "trending" to "កំពុងពេញនិយម",
            "new_arrivals" to "ទំនិញមកដល់ថ្មី & ការបញ្ចុះតម្លៃ",
            "explore_all" to "ស្វែងរកផលិតផលទាំងអស់",
            "sale_season" to "រដូវកាលបញ្ចុះតម្លៃ",
            "up_to_50" to "បញ្ចុះតម្លៃរហូតដល់ ៥០%",
            "shop_sale" to "ទិញទំនិញបញ្ចុះតម្លៃ",
            "see_all" to "មើលទាំងអស់"
        )
        "zh" -> mapOf(
            "hello" to "你好 👋",
            "shop_brand" to "按品牌选购",
            "shop_cat" to "按类别选购",
            "trending" to "热门推荐",
            "new_arrivals" to "新品上市与促销",
            "explore_all" to "浏览所有产品",
            "sale_season" to "打折季",
            "up_to_50" to "高达 50% 折扣",
            "shop_sale" to "选购促销",
            "see_all" to "查看全部"
        )
        else -> mapOf(
            "hello" to "Hello 👋",
            "shop_brand" to "SHOP BY BRAND",
            "shop_cat" to "SHOP BY CATEGORY",
            "trending" to "TRENDING NOW",
            "new_arrivals" to "NEW ARRIVALS",
            "explore_all" to "EXPLORE ALL PRODUCTS",
            "sale_season" to "SALE SEASON",
            "up_to_50" to "Up to 50% Off\nSelected Styles",
            "shop_sale" to "Shop Sale",
            "see_all" to "See All"
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Header with Sync Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(t["hello"] ?: "", style = MaterialTheme.typography.bodyMedium,
                         color = MaterialTheme.colorScheme.outline)
                    Text("ZANDO", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
                         color = MaterialTheme.colorScheme.primary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { 
                        scope.launch { viewModel.refreshProducts() }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync", tint = MaterialTheme.colorScheme.primary)
                    }
                    CartIconWithBadge(count = cartCount, onClick = onNavigateToCart)
                }
            }
        }

        // Sale Banner
        item {
            SaleBanner(t, onShopSale = { onNavigateToSearch(null, null) })
        }

        // 1. SHOP BY BRAND
        item {
            Spacer(Modifier.height(24.dp))
            SectionHeader(
                title = t["shop_brand"] ?: "",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(12.dp))
            BrandRow { brand -> onNavigateToSearch(null, brand) }
        }

        // 2. SHOP BY CATEGORY
        item {
            Spacer(Modifier.height(24.dp))
            SectionHeader(
                title = t["shop_cat"] ?: "",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(12.dp))
            CategoryRow(language) { cat -> onNavigateToSearch(cat, null) }
        }

        // Trending Live Data
        item {
            Spacer(Modifier.height(24.dp))
            SectionHeader(
                title = t["trending"] ?: "",
                onSeeAll = { onNavigateToSearch(null, null) },
                seeAllLabel = t["see_all"] ?: "See All",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(10.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(trendingProducts) { product ->
                    TrendingCard(
                        product = product,
                        onClick = { onNavigateToProduct(product.id) }
                    )
                }
            }
        }

        // New Arrivals Header
        item {
            Spacer(Modifier.height(24.dp))
            SectionHeader(
                title = t["new_arrivals"] ?: "",
                onSeeAll = { onNavigateToSearch(null, null) },
                seeAllLabel = t["see_all"] ?: "See All",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(10.dp))
        }

        // New Arrivals Grid from Firestore
        items(newArrivals.chunked(2)) { rowProducts ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowProducts.forEach { product ->
                    ProductCard(
                        product = product,
                        isWishlisted = viewModel.isWishlisted(product),
                        onWishlistClick = { viewModel.toggleWishlist(product) },
                        onAddToCart = { viewModel.addToCart(product) },
                        onClick = { onNavigateToProduct(product.id) },
                        language = language,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowProducts.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
        }

        // Explore Button
        item {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = { onNavigateToSearch(null, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(t["explore_all"] ?: "", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun SaleBanner(t: Map<String, String>, onShopSale: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFDCB01))
            .padding(20.dp)
    ) {
        Column {
            Text(t["sale_season"] ?: "", color = Color.Black.copy(alpha = 0.7f),
                 style = MaterialTheme.typography.labelLarge)
            Text(t["up_to_50"] ?: "", color = Color.Black,
                 fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 26.sp)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onShopSale,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor   = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(t["shop_sale"] ?: "", fontWeight = FontWeight.Bold)
            }
        }
        Text("🛍️", fontSize = 70.sp, modifier = Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
private fun CategoryRow(language: String, onCategoryClick: (String) -> Unit) {
    val categories = listOf("Women", "Men", "Kids", "Shoes", "Accessories", "Z.Home", "Lifestyle", "New In")
    
    fun translate(cat: String): String = when(language) {
        "km" -> when(cat) {
            "Women" -> "នារី"
            "Men" -> "បុរស"
            "Kids" -> "កុមារ"
            "Shoes" -> "ស្បែកជើង"
            "Accessories" -> "គ្រឿងបន្សំ"
            "Z.Home" -> "របស់ប្រើប្រាស់ក្នុងផ្ទះ"
            "Lifestyle" -> "របៀបរស់នៅ"
            "New In" -> "មកថ្មី"
            else -> cat
        }
        "zh" -> when(cat) {
            "Women" -> "女装"
            "Men" -> "男装"
            "Kids" -> "童装"
            "Shoes" -> "鞋履"
            "Accessories" -> "配饰"
            "Z.Home" -> "家居"
            "Lifestyle" -> "生活方式"
            "New In" -> "新品"
            else -> cat
        }
        else -> cat
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { cat ->
            CategoryChip(label = translate(cat), selected = false, onClick = { onCategoryClick(cat) })
        }
    }
}

@Composable
private fun BrandRow(onBrandClick: (String) -> Unit) {
    val brands = listOf("ZANDO.", "TEN+ELEVEN", "GATONI", "TAG SPACE", "361°", "ROUTINE", "POMELO")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        brands.forEach { brand ->
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 90.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black)
                    .clickable { onBrandClick(brand) }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = brand,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 13.sp
                )
            }
        }
    }
}
