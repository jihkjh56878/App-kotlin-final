package com.zando.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zando.app.ui.components.*
import com.zando.app.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    initialCategory: String? = null,
    initialBrand: String? = null,
    onBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToProduct: (Int) -> Unit,
    language: String
) {
    val uiState by viewModel.uiState.collectAsState()
    val cartCount by viewModel.cartItemCount.collectAsState()

    LaunchedEffect(initialCategory, initialBrand) {
        when {
            initialBrand != null   -> viewModel.setBrandFilter(initialBrand)
            initialCategory != null-> viewModel.setCategoryFilter(initialCategory.uppercase())
        }
    }

    val categories = listOf("ALL", "WOMEN", "MEN", "SHOES", "KIDS", "Z.HOME", "LIFESTYLE")

    val t = when (language) {
        "km" -> mapOf(
            "search_placeholder" to "ស្វែងរកផលិតផល ម៉ាក...",
            "items_found" to "ទំនិញត្រូវបានរកឃើញ",
            "ALL" to "ទាំងអស់",
            "WOMEN" to "នារី",
            "MEN" to "បុរស",
            "SHOES" to "ស្បែកជើង",
            "KIDS" to "កុមារ",
            "Z.HOME" to "ក្នុងផ្ទះ",
            "LIFESTYLE" to "រស់នៅ"
        )
        "zh" -> mapOf(
            "search_placeholder" to "搜索产品、品牌...",
            "items_found" to "找到商品",
            "ALL" to "全部",
            "WOMEN" to "女装",
            "MEN" to "男装",
            "SHOES" to "鞋履",
            "KIDS" to "童装",
            "Z.HOME" to "家居",
            "LIFESTYLE" to "生活方式"
        )
        else -> mapOf(
            "search_placeholder" to "Search products, brands…",
            "items_found" to "items found",
            "ALL" to "ALL",
            "WOMEN" to "WOMEN",
            "MEN" to "MEN",
            "SHOES" to "SHOES",
            "KIDS" to "KIDS",
            "Z.HOME" to "Z.HOME",
            "LIFESTYLE" to "LIFESTYLE"
        )
    }

    fun translateCat(cat: String) = t[cat.uppercase()] ?: cat

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = if (uiState.brandFilter != null) uiState.brandFilter!! else translateCat(uiState.categoryFilter),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            CartIconWithBadge(count = cartCount, onClick = onNavigateToCart)
        }

        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::setQuery,
            placeholder = { Text(t["search_placeholder"] ?: "Search...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                CategoryChip(
                    label = translateCat(cat),
                    selected = uiState.brandFilter == null && uiState.categoryFilter == cat,
                    onClick = { viewModel.setCategoryFilter(cat) }
                )
            }
        }

        Text(
            text = "${uiState.products.size} ${t["items_found"] ?: "items"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(uiState.products, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    isWishlisted = viewModel.isWishlisted(product),
                    onWishlistClick = { viewModel.toggleWishlist(product) },
                    onAddToCart = { viewModel.addToCart(product) },
                    onClick = { onNavigateToProduct(product.id) },
                    language = language
                )
            }
        }
    }
}
