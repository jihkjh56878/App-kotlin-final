package com.zando.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zando.app.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        refreshStats()
    }

    fun refreshStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val stats = firestoreService.getStats()
            val report = firestoreService.getSalesReport()
            _uiState.update { it.copy(
                totalProducts = stats["totalProducts"] as? Int ?: 0,
                totalOrders = stats["totalOrders"] as? Int ?: 0,
                totalUsers = stats["totalUsers"] as? Int ?: 0,
                revenue = stats["revenue"] as? Double ?: 0.0,
                salesReport = report,
                isLoading = false
            ) }
        }
    }
}

// ─── Manage Products ViewModel ───────────────────────────────────────────────

data class ManageProductsUiState(
    val isUploading: Boolean = false,
    val uploadError: String? = null,
    val searchQuery: String = ""
)

class ManageProductsViewModel(
    private val firestoreService: FirestoreService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageProductsUiState())
    val uiState: StateFlow<ManageProductsUiState> = _uiState.asStateFlow()

    private val _allProducts = firestoreService.getProductsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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

    fun saveProduct(product: Product, imageUri: Uri? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, uploadError = null) }
            try {
                var finalProduct = product
                if (imageUri != null) {
                    val downloadUrl = firestoreService.uploadProductImage(imageUri)
                    finalProduct = product.copy(imageUrl = downloadUrl)
                }

                if (finalProduct.id == 0) firestoreService.addProduct(finalProduct)
                else firestoreService.updateProduct(finalProduct)
            } catch (e: Exception) {
                _uiState.update { it.copy(uploadError = e.message) }
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
