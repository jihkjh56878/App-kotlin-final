package com.zando.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zando.app.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// ─── Admin Dashboard ViewModel ───────────────────────────────────────────────

data class AdminDashboardUiState(
    val totalProducts: Int = 0,
    val totalOrders: Int = 0,
    val totalUsers: Int = 0,
    val revenue: Double = 0.0,
    val salesReport: SalesReport = SalesReport(),
    val isLoading: Boolean = false
)

class AdminDashboardViewModel(
    private val firestoreService: FirestoreService
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)

    // Combine all database flows so the dashboard updates automatically
    val uiState: StateFlow<AdminDashboardUiState> = combine(
        firestoreService.getProductsFlow(),
        firestoreService.getAllOrdersFlow(),
        firestoreService.getUsersFlow(),
        _isLoading
    ) { products, orders, users, loading ->
        
        var revenue = 0.0
        val dailyMap = mutableMapOf<String, Double>()
        val monthlyMap = mutableMapOf<String, Double>()
        val productCounts = mutableMapOf<String, Int>()

        orders.forEach { order ->
            if (order.status != OrderStatus.REJECTED) {
                revenue += order.total
                
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
        }

        val report = SalesReport(
            dailySales = dailyMap.toList().sortedBy { it.first }.takeLast(7).toMap(),
            monthlySales = monthlyMap.toList().sortedBy { it.first }.takeLast(6).toMap(),
            topSellingProducts = productCounts.toList().sortedByDescending { it.second }.take(5)
        )

        AdminDashboardUiState(
            totalProducts = products.size,
            totalOrders = orders.size,
            totalUsers = users.size,
            revenue = revenue,
            salesReport = report,
            isLoading = loading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AdminDashboardUiState(isLoading = true)
    )

    fun refreshStats() {
        viewModelScope.launch {
            _isLoading.value = true
            // Also attempt to fix any products with large random IDs
            try {
                firestoreService.fixRandomProductIds()
            } catch (e: Exception) { }
            kotlinx.coroutines.delay(500) 
            _isLoading.value = false
        }
    }

    fun seedProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                ProductRepository.allProducts.forEach { product ->
                    firestoreService.addProduct(product)
                }
            } catch (e: Exception) {
                // Error handled in UI or logs
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// ─── Manage Products ViewModel ───────────────────────────────────────────────

data class ManageProductsUiState(
    val isUploading: Boolean = false,
    val uploadError: String? = null,
    val searchQuery: String = "",
    val saveSuccess: Boolean = false,
    val totalCount: Int = 0
)

class ManageProductsViewModel(
    private val firestoreService: FirestoreService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageProductsUiState())
    val uiState: StateFlow<ManageProductsUiState> = _uiState.asStateFlow()

    private val _allProducts = firestoreService.getProductsFlow()
        .onEach { list -> _uiState.update { it.copy(totalCount = list.size) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val filteredProducts: StateFlow<List<Product>> = combine(_allProducts, _uiState) { products, state ->
        if (state.searchQuery.isBlank()) {
            products
        } else {
            val query = state.searchQuery.lowercase()
            products.filter { 
                it.name.lowercase().contains(query) || 
                it.brand.lowercase().contains(query) ||
                it.category.lowercase().contains(query)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun resetSaveState() {
        _uiState.update { it.copy(saveSuccess = false, uploadError = null, isUploading = false) }
    }

    fun saveProduct(product: Product, imageBytes: ByteArray? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, uploadError = null, saveSuccess = false) }
            try {
                var finalProduct = product
                if (imageBytes != null) {
                    val downloadUrl = firestoreService.uploadProductImage(imageBytes)
                    finalProduct = product.copy(imageUrl = downloadUrl)
                }

                if (finalProduct.id == 0) {
                    // Let FirestoreService handle sequential ID generation
                    firestoreService.addProduct(finalProduct)
                } else {
                    firestoreService.updateProduct(finalProduct)
                }
                
                _uiState.update { it.copy(saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(uploadError = e.message ?: "Unknown error occurred") }
            } finally {
                _uiState.update { it.copy(isUploading = false) }
            }
        }
    }

    fun deleteProduct(productId: Int) {
        viewModelScope.launch { firestoreService.deleteProduct(productId) }
    }
}

// ─── Manage Orders ViewModel ─────────────────────────────────────────────────

class ManageOrdersViewModel(
    private val firestoreService: FirestoreService
) : ViewModel() {

    val orders: StateFlow<List<Order>> = firestoreService.getAllOrdersFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch { firestoreService.updateOrderStatus(orderId, status) }
    }
}

// ─── Manage Users ViewModel ──────────────────────────────────────────────────

class ManageUsersViewModel(
    private val firestoreService: FirestoreService
) : ViewModel() {

    val users: StateFlow<List<UserProfile>> = firestoreService.getUsersFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun toggleBlock(user: UserProfile) {
        viewModelScope.launch {
            firestoreService.updateUserStatus(user.uid, !user.isBlocked)
        }
    }

    fun deleteUser(uid: String) {
        viewModelScope.launch { firestoreService.deleteUser(uid) }
    }
}
