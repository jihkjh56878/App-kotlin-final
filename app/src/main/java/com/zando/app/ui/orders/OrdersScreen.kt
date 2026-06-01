package com.zando.app.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zando.app.model.Order
import com.zando.app.model.OrderStatus
import com.zando.app.ui.components.CategoryChip
import com.zando.app.ui.components.OrderStatusBadge
import com.zando.app.viewmodel.OrdersViewModel

@Composable
fun OrdersScreen(
    viewModel: OrdersViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var orderToCancel by remember { mutableStateOf<Order?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Orders", style = MaterialTheme.typography.headlineSmall)
        }

        // Status filter tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip("All",        uiState.statusFilter == null,                    { viewModel.setFilter(null) })
            CategoryChip("Processing", uiState.statusFilter == OrderStatus.PROCESSING, { viewModel.setFilter(OrderStatus.PROCESSING) })
            CategoryChip("Shipped",    uiState.statusFilter == OrderStatus.SHIPPED,    { viewModel.setFilter(OrderStatus.SHIPPED) })
            CategoryChip("Delivered",  uiState.statusFilter == OrderStatus.DELIVERED,  { viewModel.setFilter(OrderStatus.DELIVERED) })
        }

        Spacer(Modifier.height(8.dp))

        if (uiState.orders.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📦", fontSize = androidx.compose.ui.unit.TextUnit(64f, androidx.compose.ui.unit.TextUnitType.Sp))
                    Spacer(Modifier.height(8.dp))
                    Text("No orders found", style = MaterialTheme.typography.titleLarge)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.orders, key = { it.id }) { order ->
                    OrderCard(
                        order = order,
                        onCancel = { orderToCancel = order }
                    )
                }
            }
        }
    }

    // Cancel confirmation dialog
    orderToCancel?.let { order ->
        AlertDialog(
            onDismissRequest = { orderToCancel = null },
            title = { Text("Cancel Order") },
            text  = { Text("Are you sure you want to cancel order ${order.id}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.cancelOrder(order.id)
                    orderToCancel = null
                }) { Text("Yes, cancel", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { orderToCancel = null }) { Text("No") }
            }
        )
    }
}

@Composable
private fun OrderCard(order: Order, onCancel: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(order.id, style = MaterialTheme.typography.titleMedium)
                OrderStatusBadge(status = order.status)
            }
            Spacer(Modifier.height(4.dp))
            Text(order.date, style = MaterialTheme.typography.bodySmall,
                 color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(8.dp))
            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${item.product.imageEmoji} ${item.product.name} × ${item.quantity}",
                         style = MaterialTheme.typography.bodyMedium)
                    Text("$${(item.product.price * item.quantity).toInt()}",
                         style = MaterialTheme.typography.bodyMedium)
                }
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total: $${String.format("%.1f", order.total)}", fontWeight = FontWeight.Bold)
                if (order.status == OrderStatus.PROCESSING) {
                    TextButton(onClick = onCancel) {
                        Text("Cancel Order", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
