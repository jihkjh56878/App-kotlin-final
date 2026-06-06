package com.zando.app.model

import android.util.Base64
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.WriteBatch
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// ─── Scraper API Response Models ──────────────────────────────────────────────

data class ScraperResponse(
    val success: Boolean,
    val products: List<ScrapedProduct>? = null
)

data class ScrapedProduct(
    val id: Int,
    val brand: String? = null,
    val name: String,
    @SerializedName("sale_price") val salePrice: String,
    @SerializedName("original_price") val originalPrice: String? = null,
    @SerializedName("image_url") val imageUrl: String
)

// ─── Sales Report Models ─────────────────────────────────────────────────────

data class SalesReport(
    val dailySales: Map<String, Double> = emptyMap(),
    val monthlySales: Map<String, Double> = emptyMap(),
    val topSellingProducts: List<Pair<String, Int>> = emptyList()
)

// ─── Firestore Service ────────────────────────────────────────────────────────

class FirestoreService {
    private val db = FirebaseFirestore.getInstance()
    private val rtdb = FirebaseDatabase.getInstance().reference
    private val productsCollection = db.collection("products")
    private val usersCollection = db.collection("users")
    private val ordersCollection = db.collection("orders")
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    private fun parsePrice(priceStr: String): Double {
        return try {
            priceStr.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
        } catch (e: Exception) { 0.0 }
    }

    private fun mapCategory(endpoint: String): String {
        return when {
            endpoint.contains("women") -> "Women"
            endpoint.contains("men") -> "Men"
            endpoint.contains("shoe") -> "Shoes"
            endpoint.contains("accessories") -> "Accessories"
            endpoint.contains("girl") || endpoint.contains("boy") -> "Kids"
            else -> "Z.Home"
        }
    }

    fun getProductsFlow(): Flow<List<Product>> = callbackFlow {
        val listener = productsCollection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("FirestoreService", "Snapshot error", e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                // Move heavy reflection/parsing to background thread to avoid ANR
                launch(Dispatchers.Default) {
                    try {
                        val products = snapshot.toObjects(Product::class.java)
                        trySend(products)
                    } catch (ex: Exception) {
                        Log.e("FirestoreService", "Parsing error", ex)
                    }
                }
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun getAllProducts(): List<Product> = 
        productsCollection.get().await().toObjects(Product::class.java)

    // ─── Realtime Database (Image Upload via Base64) ─────────────────────────

    suspend fun uploadProductImage(imageBytes: ByteArray): String {
        val imageId = UUID.randomUUID().toString()
        val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        rtdb.child("product_images").child(imageId).setValue(base64Image).await()
        return "data:image/jpeg;base64,$base64Image"
    }

    // ─── User Management ─────────────────────────────────────────────────────

    suspend fun saveUserProfile(user: UserProfile) {
        usersCollection.document(user.uid).set(user).await()
    }

    suspend fun getUserProfile(uid: String): UserProfile? {
        return usersCollection.document(uid).get().await().toObject(UserProfile::class.java)
    }

    fun getUsersFlow(): Flow<List<UserProfile>> = callbackFlow {
        val listener = usersCollection.addSnapshotListener { snapshot, e ->
            if (snapshot != null) trySend(snapshot.toObjects(UserProfile::class.java))
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateUserStatus(uid: String, isBlocked: Boolean) {
        usersCollection.document(uid).update("blocked", isBlocked).await()
    }

    suspend fun deleteUser(uid: String) {
        usersCollection.document(uid).delete().await()
    }

    // ─── Product Management (Admin) ──────────────────────────────────────────

    suspend fun addProduct(product: Product) {
        val id = if (product.id == 0) {
            val snapshot = productsCollection.get().await()
            val numericIds = snapshot.documents.mapNotNull { it.id.toIntOrNull() }
            // Consider sequential IDs as those less than 1000
            val maxSequentialId = numericIds.filter { it < 1000 }.maxOrNull() ?: 21
            maxSequentialId + 1
        } else {
            product.id
        }
        productsCollection.document(id.toString()).set(product.copy(id = id)).await()
    }

    /**
     * Finds products with large random IDs and re-assigns them sequential IDs
     * starting from the current maximum sequential ID.
     */
    suspend fun fixRandomProductIds() {
        try {
            val snapshot = productsCollection.get().await()
            val allProducts = snapshot.documents.mapNotNull { doc ->
                val p = doc.toObject(Product::class.java)
                p?.copy(id = doc.id.toIntOrNull() ?: 0)
            }
            
            // Sequential IDs are usually small. Random ones are large.
            val sequentialProducts = allProducts.filter { it.id in 1..999 }.sortedBy { it.id }
            val randomProducts = allProducts.filter { it.id >= 1000 }.sortedBy { it.id }
            
            if (randomProducts.isEmpty()) return

            var nextId = (sequentialProducts.lastOrNull()?.id ?: 21) + 1
            
            randomProducts.forEach { product ->
                val oldId = product.id
                val newId = nextId++
                
                // 1. Create new document with sequential ID
                val newProduct = product.copy(id = newId)
                productsCollection.document(newId.toString()).set(newProduct).await()
                
                // 2. Delete the old document with the random ID
                productsCollection.document(oldId.toString()).delete().await()
                
                Log.d("FirestoreService", "Replaced random ID $oldId with sequential ID $newId")
            }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error fixing random IDs", e)
        }
    }

    suspend fun updateProduct(product: Product) {
        productsCollection.document(product.id.toString()).set(product).await()
    }

    suspend fun deleteProduct(productId: Int) {
        productsCollection.document(productId.toString()).delete().await()
    }

    // ─── Order Management ────────────────────────────────────────────────────

    suspend fun placeOrder(order: Order) {
        ordersCollection.document(order.id).set(order).await()
    }

    fun getAllOrdersFlow(): Flow<List<Order>> = callbackFlow {
        val listener = ordersCollection.orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) trySend(snapshot.toObjects(Order::class.java))
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus) {
        ordersCollection.document(orderId).update("status", status).await()
    }

    // ─── Statistics & Reports ────────────────────────────────────────────────

    suspend fun getStats(): Map<String, Any> {
        val products = productsCollection.get().await().size()
        val orders = ordersCollection.get().await()
        val users = usersCollection.get().await().size()
        
        var revenue = 0.0
        orders.forEach { doc ->
            val order = doc.toObject(Order::class.java)
            if (order.status != OrderStatus.REJECTED) {
                revenue += order.total
            }
        }

        return mapOf(
            "totalProducts" to products,
            "totalOrders" to orders.size(),
            "totalUsers" to users,
            "revenue" to revenue
        )
    }

    suspend fun getSalesReport(): SalesReport {
        val orders = ordersCollection.get().await().toObjects(Order::class.java)
        
        val dailyMap = mutableMapOf<String, Double>()
        val monthlyMap = mutableMapOf<String, Double>()
        val productCounts = mutableMapOf<String, Int>()

        orders.forEach { order ->
            if (order.status == OrderStatus.REJECTED) return@forEach

            dailyMap[order.date] = (dailyMap[order.date] ?: 0.0) + order.total
            
            try {
                val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).parse(order.date)
                if (date != null) {
                    val monthKey = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(date)
                    monthlyMap[monthKey] = (monthlyMap[monthKey] ?: 0.0) + order.total
                }
            } catch (e: Exception) { }

            order.items.forEach { item ->
                productCounts[item.product.name] = (productCounts[item.product.name] ?: 0) + item.quantity
            }
        }

        return SalesReport(
            dailySales = dailyMap.toList().sortedBy { it.first }.takeLast(7).toMap(),
            monthlySales = monthlyMap.toList().sortedBy { it.first }.takeLast(6).toMap(),
            topSellingProducts = productCounts.toList().sortedByDescending { it.second }.take(5)
        )
    }

    // ─── Scraper Sync ────────────────────────────────────────────────────────

    suspend fun syncAllCategories() = withContext(Dispatchers.IO) {
        val categories = listOf(
            "women-clothing", "women-accessories", "women-shoes", "women-shop-by-collection",
            "men-clothing", "men-accessories", "men-shoes", "men-shop-by-collection",
            "hygge-accessories", "tableware", "basket-organizer", "home-decoration",
            "slippers", "mat", "towels", "pillowcase", "quilt", "mattress",
            "cushions", "pillow", "zhome-accessories", "girls-shoes",
            "girls-accessories", "girl-clothing", "boys-shoes", "boy-accessories", "boy-clothing"
        )
        // Process categories in batches to avoid overloading
        categories.chunked(3).forEach { batch ->
            batch.map { async { syncFromApi(it) } }.awaitAll()
            delay(500) // Breather for system
        }
    }

    private suspend fun syncFromApi(endpoint: String) = withContext(Dispatchers.IO) {
        try {
            val url = "http://10.0.2.2:3000/api/$endpoint"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext
                val body = response.body?.string() ?: return@withContext
                val data = gson.fromJson(body, ScraperResponse::class.java)
                if (data?.success == true && data.products != null) {
                    val appCategory = mapCategory(endpoint)
                    // Use a batch write for each category to reduce snapshot events
                    val batch = db.batch()
                    data.products.forEach { sp ->
                        val uniqueId = (appCategory + sp.id + sp.name).hashCode()
                        val product = Product(
                            id = uniqueId,
                            brand = sp.brand ?: "Zando",
                            name = sp.name,
                            price = parsePrice(sp.salePrice),
                            oldPrice = sp.originalPrice?.let { parsePrice(it) },
                            imageUrl = sp.imageUrl,
                            category = appCategory,
                            isSale = !sp.originalPrice.isNullOrBlank()
                        )
                        batch.set(productsCollection.document(product.id.toString()), product)
                    }
                    batch.commit().await()
                }
            }
        } catch (e: Exception) { 
            Log.e("FirestoreService", "Error syncing $endpoint: ${e.message}") 
        }
    }
}
