package com.zando.app.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zando.app.ui.components.CartIconWithBadge
import com.zando.app.ui.theme.*
import com.zando.app.viewmodel.ProductDetailViewModel

@Composable
fun ProductDetailScreen(
    productId: Int,
    viewModel: ProductDetailViewModel,
    onBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToFaq: () -> Unit,
    language: String
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSizeGuide by remember { mutableStateOf(false) }
    var showAddedSnackbar by remember { mutableStateOf(false) }
    var expandModelInfo by remember { mutableStateOf(true) }
    var expandDetails by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(productId) { viewModel.loadProduct(productId) }

    LaunchedEffect(showAddedSnackbar) {
        if (showAddedSnackbar) {
            snackbarHostState.showSnackbar("${uiState.product?.name} added to your bag!")
            showAddedSnackbar = false
        }
    }

    val product = uiState.product ?: return

    // Translations
    val t = when (language) {
        "km" -> mapOf(
            "select_size" to "ជ្រើសរើសទំហំ",
            "size_guide" to "មគ្គុទ្ទេសក៍ទំហំ",
            "color" to "ពណ៌",
            "fast_delivery" to "ការដឹកជញ្ជូនរហ័ស",
            "delivery_desc" to "ពី ១ - ៣ ថ្ងៃ",
            "returns" to "ការប្តូរវិញ",
            "returns_desc" to "ក្នុងរយៈពេល ១៤ ថ្ងៃ",
            "model_info" to "ព័ត៌មានម៉ូដែល",
            "product_details" to "ព័ត៌មានលម្អិតផលិតផល",
            "online_policy" to "គោលការណ៍ប្តូរតាមអនឡាញ",
            "similar_items" to "ទំនិញប្រហាក់ប្រហែល",
            "add_to_bag" to "បន្ថែមទៅកន្ត្រក",
            "select_size_first" to "សូមជ្រើសរើសទំហំជាមុនសិន"
        )
        "zh" -> mapOf(
            "select_size" to "选择尺码",
            "size_guide" to "尺码指南",
            "color" to "颜色",
            "fast_delivery" to "快速送达",
            "delivery_desc" to "1 - 3 天内",
            "returns" to "退货",
            "returns_desc" to "14 天内",
            "model_info" to "模特信息",
            "product_details" to "产品详情",
            "online_policy" to "线上换货政策",
            "similar_items" to "相似单品",
            "add_to_bag" to "加入购物袋",
            "select_size_first" to "请先选择尺码"
        )
        else -> mapOf(
            "select_size" to "Select Size",
            "size_guide" to "Size guide",
            "color" to "Color",
            "fast_delivery" to "Fast Delivery",
            "delivery_desc" to "From 1 - 3 days",
            "returns" to "RETURNS",
            "returns_desc" to "Within 14 days",
            "model_info" to "Model info",
            "product_details" to "Product details",
            "online_policy" to "Online exchange policy",
            "similar_items" to "Similar items",
            "add_to_bag" to "Add to Bag",
            "select_size_first" to "Select Size First"
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                CartIconWithBadge(count = uiState.cartCount, onClick = onNavigateToCart)
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.toggleWishlist() },
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = if (uiState.isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = if (uiState.isWishlisted) ZandoSaleBadge else MaterialTheme.colorScheme.onSurface
                    )
                }
                Button(
                    onClick = {
                        val success = viewModel.addToCart()
                        if (success) showAddedSnackbar = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (uiState.selectedSize == null) t["select_size_first"] ?: "" else t["add_to_bag"] ?: "",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = product.imageEmoji, fontSize = 120.sp)
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Brand + Name
                Text(
                    text = product.brand.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(text = product.name, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(6.dp))

                // Price row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$${product.price}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (product.isSale) ZandoSaleBadge else MaterialTheme.colorScheme.onBackground
                    )
                    product.oldPrice?.let {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "$${it}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = TextDecoration.LineThrough
                            ),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                // Size selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(t["select_size"] ?: "", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("XS", "S", "M", "L", "XL").forEach { size ->
                        SizeChip(
                            size = size,
                            selected = uiState.selectedSize == size,
                            onClick = { viewModel.selectSize(size) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Color selection
                Text(t["color"] ?: "", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ColorOption("Black", Color.Black, uiState.selectedColor == "Black") { viewModel.selectColor("Black") }
                    ColorOption("Brown", Color(0xFF8B4513), uiState.selectedColor == "Brown") { viewModel.selectColor("Brown") }
                    ColorOption("Beige", Color(0xFFF5F5DC), uiState.selectedColor == "Beige") { viewModel.selectColor("Beige") }
                }

                Divider(modifier = Modifier.padding(vertical = 24.dp))

                // Delivery and Returns Row
                Row(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(t["fast_delivery"] ?: "", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(t["delivery_desc"] ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(t["returns"] ?: "", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(t["returns_desc"] ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                // Model Info Expandable
                ExpandableSection(
                    title = t["model_info"] ?: "",
                    content = product.modelInfo,
                    expanded = expandModelInfo,
                    onToggle = { expandModelInfo = !expandModelInfo }
                )
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Product Details Expandable
                ExpandableSection(
                    title = t["product_details"] ?: "",
                    content = product.description,
                    expanded = expandDetails,
                    onToggle = { expandDetails = !expandDetails }
                )
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Size Guide Row
                DetailRow(title = t["size_guide"] ?: "") { showSizeGuide = true }
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Online exchange policy Row
                DetailRow(title = t["online_policy"] ?: "") { onNavigateToFaq() }
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                Spacer(Modifier.height(24.dp))
                Text(t["similar_items"] ?: "", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showSizeGuide) {
        SizeGuideDialog(
            onDismiss = { showSizeGuide = false },
            language = language
        )
    }
}

@Composable
private fun DetailRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun ExpandableSection(title: String, content: String, expanded: Boolean, onToggle: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        if (expanded) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun SizeChip(size: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 56.dp, height = 44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color.Black else MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = size,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ColorOption(name: String, color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .border(if (selected) 2.dp else 1.dp,
                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    CircleShape)
            .clickable { onClick() }
    )
}

// ─── Size Guide Dialog (Updated with 3x3 logic) ──────────────────────────────

@Composable
private fun SizeGuideDialog(onDismiss: () -> Unit, language: String) {
    var heightInput by remember { mutableStateOf("") }
    var weightInput by remember { mutableStateOf("") }
    var focalHeightIndex by remember { mutableStateOf<Int?>(null) }
    var focalWeightIndex by remember { mutableStateOf<Int?>(null) }

    val t = when (language) {
        "km" -> mapOf(
            "title" to "មគ្គុទ្ទេសក៍ទំហំ",
            "height" to "កម្ពស់",
            "weight" to "ទម្ងន់",
            "apply" to "អនុវត្ត",
            "view_all" to "មើលទាំងអស់",
            "measurement" to "ការវាស់វែង"
        )
        "zh" -> mapOf(
            "title" to "尺码指南",
            "height" to "身高",
            "weight" to "体重",
            "apply" to "应用",
            "view_all" to "查看全部",
            "measurement" to "测量"
        )
        else -> mapOf(
            "title" to "Size guide",
            "height" to "Height",
            "weight" to "Weight",
            "apply" to "Apply",
            "view_all" to "View all",
            "measurement" to "Measurement"
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    t["title"]!!,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Input Row: Height, Weight, Apply
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { heightInput = it },
                        placeholder = { Text(t["height"]!!, style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(2.dp)
                    )
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        placeholder = { Text(t["weight"]!!, style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(2.dp)
                    )
                    Button(
                        onClick = {
                            val h = heightInput.toIntOrNull()
                            val w = weightInput.toIntOrNull()
                            if (h != null && w != null) {
                                focalHeightIndex = findHeightIndex(h)
                                focalWeightIndex = findWeightIndex(w)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                    ) {
                        Text(t["apply"]!!, color = Color.White)
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Height Label and View all
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(t["height"]!!, style = MaterialTheme.typography.bodyLarge)
                    OutlinedButton(
                        onClick = {
                            focalHeightIndex = null
                            focalWeightIndex = null
                        },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text(t["view_all"]!!, style = MaterialTheme.typography.labelMedium, color = Color.Black)
                    }
                }

                // Size Grid Matrix
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    SizeMatrixGrid(focalHeightIndex, focalWeightIndex)
                }

                Text(
                    t["weight"]!!,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(40.dp))

                // Measurement Section
                Text(
                    t["measurement"]!!,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                MeasurementTable()
                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun SizeMatrixGrid(focalHeightIndex: Int?, focalWeightIndex: Int?) {
    val heightLabels = listOf("191-195", "186-190", "181-185", "176-180", "171-175", "160-170", "155-159")
    val weightLabels = listOf("45-49", "50-60", "61-70", "71-80", "81-90", "91-95", "96-100")
    val gridData = listOf(
        listOf("", "", "", "", "XL", "XL", "XL"),
        listOf("", "", "", "L", "XL", "XL", "XL"),
        listOf("", "", "M", "L", "XL", "XL", "XL"),
        listOf("", "S", "M", "L", "L", "XL", "XL"),
        listOf("S", "M", "M", "L", "L", "XL", "XL"),
        listOf("S", "S", "M", "L", "L", "XL", "XL"),
        listOf("S", "S", "M", "M", "L", "XL", "XL"),
    )

    // Calculate indices to show
    val heightIndices = if (focalHeightIndex != null) {
        (focalHeightIndex - 1..focalHeightIndex + 1).filter { it in 0 until heightLabels.size }
    } else {
        heightLabels.indices.toList()
    }

    val weightIndices = if (focalWeightIndex != null) {
        (focalWeightIndex - 1..focalWeightIndex + 1).filter { it in 0 until weightLabels.size }
    } else {
        weightLabels.indices.toList()
    }

    Column {
        heightIndices.forEach { hIdx ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Height label (left)
                Box(
                    modifier = Modifier.size(width = 60.dp, height = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = heightLabels[hIdx],
                        fontSize = 10.sp,
                        modifier = Modifier.rotate(-45f),
                        textAlign = TextAlign.Center
                    )
                }

                weightIndices.forEach { wIdx ->
                    val size = gridData[hIdx][wIdx]
                    val isFocal = hIdx == focalHeightIndex && wIdx == focalWeightIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f)
                            .border(0.5.dp, Color.LightGray)
                            .background(if (isFocal) Color.Black.copy(alpha = 0.05f) else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        if (size.isNotEmpty()) {
                            Text(
                                text = size,
                                fontSize = if (isFocal) 13.sp else 11.sp,
                                fontWeight = if (isFocal) FontWeight.ExtraBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Weight labels (bottom)
        Row {
            Spacer(Modifier.width(60.dp))
            weightIndices.forEach { wIdx ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = weightLabels[wIdx],
                        fontSize = 10.sp,
                        modifier = Modifier.rotate(-45f).padding(top = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun findHeightIndex(h: Int): Int {
    return when {
        h in 191..195 -> 0
        h in 186..190 -> 1
        h in 181..185 -> 2
        h in 176..180 -> 3
        h in 171..175 -> 4
        h in 160..170 -> 5
        h in 155..159 -> 6
        h > 195 -> 0
        else -> 6
    }
}

private fun findWeightIndex(w: Int): Int {
    return when {
        w in 45..49 -> 0
        w in 50..60 -> 1
        w in 61..70 -> 2
        w in 71..80 -> 3
        w in 81..90 -> 4
        w in 91..95 -> 5
        w in 96..100 -> 6
        w > 100 -> 6
        else -> 0
    }
}

@Composable
private fun MeasurementTable() {
    val headers = listOf("Brand\u0027s Size", "S", "M", "L", "XL")
    val values = listOf("", "76", "81", "89", "97")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(0.5.dp, Color.LightGray)
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEEEEEE))
                .height(IntrinsicSize.Min)
        ) {
            headers.forEachIndexed { index, text ->
                Box(
                    modifier = Modifier
                        .weight(if (index == 0) 1.5f else 1f)
                        .fillMaxHeight()
                        .border(0.25.dp, Color.LightGray)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Divider()
        // Data Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            values.forEachIndexed { index, text ->
                Box(
                    modifier = Modifier
                        .weight(if (index == 0) 1.5f else 1f)
                        .fillMaxHeight()
                        .border(0.25.dp, Color.LightGray)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
