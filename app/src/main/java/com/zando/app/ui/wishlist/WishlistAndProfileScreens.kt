package com.zando.app.ui.wishlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zando.app.ui.components.ProductCard
import com.zando.app.viewmodel.WishlistViewModel

@Composable
fun WishlistScreen(
    viewModel: WishlistViewModel,
    onNavigateToProduct: (Int) -> Unit,
    language: String
) {
    val items by viewModel.items.collectAsState()

    val t = when (language) {
        "km" -> mapOf(
            "title" to "បញ្ជីដែលចង់បាន",
            "empty" to "បញ្ជីដែលចង់បានរបស់អ្នកគឺទទេ"
        )
        "zh" -> mapOf(
            "title" to "愿望清单",
            "empty" to "您的愿望清单是空的"
        )
        else -> mapOf(
            "title" to "Wishlist",
            "empty" to "Your wishlist is empty"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = t["title"]!!,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("♡", fontSize = 64.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(t["empty"]!!, style = MaterialTheme.typography.titleLarge)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        isWishlisted = true,
                        onWishlistClick = { viewModel.removeFromWishlist(product) },
                        onAddToCart    = { viewModel.addToCart(product) },
                        onClick        = { onNavigateToProduct(product.id) },
                        language       = language
                    )
                }
            }
        }
    }
}
