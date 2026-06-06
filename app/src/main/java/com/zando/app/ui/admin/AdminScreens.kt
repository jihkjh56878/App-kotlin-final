package com.zando.app.ui.admin

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zando.app.model.*
import com.zando.app.util.getBase64Bitmap
import com.zando.app.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel,
    onNavigateToProducts: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Stats Grid
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard("Products", uiState.totalProducts.toString(), Icons.Default.Inventory, Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                StatCard("Orders", uiState.totalOrders.toString(), Icons.AutoMirrored.Filled.ListAlt, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard("Users", uiState.totalUsers.toString(), Icons.Default.Group, Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                StatCard("Revenue", "$${"%.2f".format(uiState.revenue)}", Icons.Default.AttachMoney, Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))
            Text("Sales Report", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            SalesReportSection(uiState.salesReport)

            Spacer(Modifier.height(24.dp))
            Text("Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            ManagementItem("Manage Products", Icons.Default.ShoppingCart, onNavigateToProducts)
            ManagementItem("Manage Orders", Icons.Default.LocalShipping, onNavigateToOrders)
            ManagementItem("Manage Users", Icons.Default.Person, onNavigateToUsers)
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun SalesReportSection(report: SalesReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Monthly Sales", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            report.monthlySales.forEach { (month, amount) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(month, style = MaterialTheme.typography.bodySmall)
                    Text("$${"%.2f".format(amount)}", fontWeight = FontWeight.Medium)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text("Daily Sales (Last 7 Days)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            report.dailySales.forEach { (date, amount) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(date, style = MaterialTheme.typography.bodySmall)
                    Text("$${"%.2f".format(amount)}", fontWeight = FontWeight.Medium)
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            Text("Top Selling Products", fontWeight = FontWeight.Bold)
            report.topSellingProducts.forEach { (name, qty) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    Text("$qty sold", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(Modifier.fillMaxSize().padding(12.dp), Arrangement.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                text = title, 
                style = MaterialTheme.typography.labelMedium, 
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ManagementItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() }, RoundedCornerShape(8.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Text(title, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProductsScreen(viewModel: ManageProductsViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val products by viewModel.filteredProducts.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    val context = LocalContext.current

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            showAddDialog = false
            viewModel.resetSaveState()
            Toast.makeText(context, "Product saved successfully", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Products") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = { IconButton(onClick = { 
                    selectedProduct = null
                    viewModel.resetSaveState()
                    showAddDialog = true 
                }) { Icon(Icons.Default.Add, "Add") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search products...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            LazyColumn(Modifier.fillMaxSize()) {
                items(products) { product ->
                    ListItem(
                        headlineContent = { Text(product.name) },
                        supportingContent = { Text("${product.brand} | $${product.price} | Stock: ${product.stock}") },
                        leadingContent = {
                            Box(Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray)) {
                                val bitmap = remember(product.imageUrl) { getBase64Bitmap(product.imageUrl) }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else if (product.imageUrl != null && product.imageUrl.startsWith("http")) {
                                    AsyncImage(model = product.imageUrl, contentDescription = null, contentScale = ContentScale.Crop)
                                } else {
                                    Text(product.imageEmoji, Modifier.align(Alignment.Center))
                                }
                            }
                        },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { 
                                    selectedProduct = product
                                    viewModel.resetSaveState()
                                    showAddDialog = true 
                                }) { Icon(Icons.Default.Edit, "Edit", tint = Color.Blue) }
                                IconButton(onClick = { viewModel.deleteProduct(product.id) }) { Icon(Icons.Default.Delete, "Delete", tint = Color.Red) }
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showAddDialog) {
        ProductEditDialog(
            product = selectedProduct,
            isUploading = uiState.isUploading,
            uploadError = uiState.uploadError,
            onDismiss = { showAddDialog = false },
            onSave = { p, uri -> 
                val imageBytes = uri?.let { context.contentResolver.openInputStream(it)?.use { stream -> stream.readBytes() } }
                viewModel.saveProduct(p, imageBytes)
            }
        )
    }
}

@Composable
fun ProductEditDialog(
    product: Product?,
    isUploading: Boolean,
    uploadError: String?,
    onDismiss: () -> Unit,
    onSave: (Product, Uri?) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var brand by remember { mutableStateOf(product?.brand ?: "Zando") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "") }
    var stock by remember { mutableStateOf(product?.stock?.toString() ?: "10") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = if (isUploading) ({}) else onDismiss,
        title = { Text(if (product == null) "Add Product" else "Edit Product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (isUploading) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Text("Uploading product...", style = MaterialTheme.typography.labelSmall)
                }
                
                if (uploadError != null) {
                    Text(uploadError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                
                // Image Picker
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray).clickable(enabled = !isUploading) { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(model = selectedImageUri, contentDescription = null, contentScale = ContentScale.Crop)
                    } else if (product?.imageUrl != null) {
                        val bitmap = remember(product.imageUrl) { getBase64Bitmap(product.imageUrl) }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (product.imageUrl.startsWith("http")) {
                            AsyncImage(model = product.imageUrl, contentDescription = null, contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Default.AddPhotoAlternate, null)
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, null)
                            Text("Pick Product Image", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                OutlinedTextField(name, { name = it }, label = { Text("Name") }, enabled = !isUploading)
                OutlinedTextField(brand, { brand = it }, label = { Text("Brand") }, enabled = !isUploading)
                OutlinedTextField(price, { price = it }, label = { Text("Price") }, enabled = !isUploading)
                OutlinedTextField(category, { category = it }, label = { Text("Category") }, enabled = !isUploading)
                OutlinedTextField(stock, { stock = it }, label = { Text("Stock") }, enabled = !isUploading)
            }
        },
        confirmButton = {
            Button(enabled = !isUploading && name.isNotBlank() && price.isNotBlank(), onClick = {
                onSave(Product(
                    id = product?.id ?: 0,
                    name = name,
                    brand = brand,
                    price = price.toDoubleOrNull() ?: 0.0,
                    category = category,
                    stock = stock.toIntOrNull() ?: 0,
                    imageUrl = product?.imageUrl,
                    imageEmoji = "📦"
                ), selectedImageUri)
            }) { Text("Save") }
        },
        dismissButton = { 
            TextButton(enabled = !isUploading, onClick = onDismiss) { Text("Cancel") } 
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageOrdersScreen(viewModel: ManageOrdersViewModel, onBack: () -> Unit) {
    val orders by viewModel.orders.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Manage Orders") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }) }) { padding ->
        LazyColumn(Modifier.padding(padding)) {
            items(orders) { order ->
                val backgroundColor = if (order.status == OrderStatus.REJECTED) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant
                Card(
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Order ID: ${order.id}", fontWeight = FontWeight.Bold)
                        Text("Date: ${order.date}", style = MaterialTheme.typography.bodySmall)
                        Text("User: ${order.userName} | Total: $${"%.2f".format(order.total)}")
                        Text("Status: ${order.status}", color = if (order.status == OrderStatus.REJECTED) Color.Red else Color.Blue)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { viewModel.updateStatus(order.id, OrderStatus.ACCEPTED) }) { Text("Accept") }
                            Button(onClick = { viewModel.updateStatus(order.id, OrderStatus.SHIPPED) }) { Text("Ship") }
                            Button(onClick = { viewModel.updateStatus(order.id, OrderStatus.DELIVERED) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) { Text("Deliver") }
                            Button(onClick = { viewModel.updateStatus(order.id, OrderStatus.REJECTED) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Reject") }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsersScreen(viewModel: ManageUsersViewModel, onBack: () -> Unit) {
    val users by viewModel.users.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Manage Users") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }) }) { padding ->
        LazyColumn(Modifier.padding(padding)) {
            items(users) { user ->
                ListItem(
                    headlineContent = { Text(user.name) },
                    supportingContent = { Text("${user.email} | Role: ${user.role}") },
                    trailingContent = {
                        Row {
                            TextButton(onClick = { viewModel.toggleBlock(user) }) { Text(if (user.isBlocked) "Unblock" else "Block", color = if (user.isBlocked) Color.Green else Color.Red) }
                            IconButton(onClick = { viewModel.deleteUser(user.uid) }) { Icon(Icons.Default.Delete, "Delete", tint = Color.Red) }
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
