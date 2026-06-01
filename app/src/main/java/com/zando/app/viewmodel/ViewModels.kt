package com.zando.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.zando.app.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── Home ViewModel ───────────────────────────────────────────────────────────

class HomeViewModel(
    private val cartRepo: CartRepository,
    private val wishlistRepo: WishlistRepository,
    private val firestoreService: FirestoreService
) : ViewModel() {

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    val cartItemCount: StateFlow<Int> = cartRepo.items
        .map { it.sumOf { item -> item.quantity } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val trendingProducts: StateFlow<List<Product>> = _allProducts
        .map { it.take(10) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val newArrivals: StateFlow<List<Product>> = _allProducts
        .map { it.filter { p -> p.isNew || p.isSale }.take(8) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            firestoreService.getProductsFlow().collect { _allProducts.value = it }
        }
    }

    fun refreshProducts() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                firestoreService.syncAllCategories()
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun isWishlisted(product: Product) = wishlistRepo.isWishlisted(product)
    fun toggleWishlist(product: Product) { wishlistRepo.toggle(product) }
    fun addToCart(product: Product) { viewModelScope.launch { cartRepo.addItem(product) } }
}

// ─── Search ViewModel ─────────────────────────────────────────────────────────

data class SearchUiState(
    val query: String = "",
    val categoryFilter: String = "ALL",
    val brandFilter: String? = null,
    val products: List<Product> = emptyList()
)

class SearchViewModel(
    private val cartRepo: CartRepository,
    private val wishlistRepo: WishlistRepository,
    private val firestoreService: FirestoreService
) : ViewModel() {

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    private val _uiState = MutableStateFlow(SearchUiState())
    
    val uiState: StateFlow<SearchUiState> = combine(_uiState, _allProducts) { state, all ->
        var result = when {
            state.brandFilter != null -> all
                .filter { it.brand.equals(state.brandFilter, ignoreCase = true) }
            state.categoryFilter == "ALL" -> all
            state.categoryFilter == "NEW IN" -> all.filter { it.isNew }
            else -> all
                .filter { it.category.uppercase() == state.categoryFilter.uppercase() }
        }
        if (state.query.isNotBlank()) {
            val q = state.query.lowercase()
            result = result.filter {
                it.name.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.brand.lowercase().contains(q)
            }
        }
        state.copy(products = result)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SearchUiState())

    init {
        viewModelScope.launch {
            firestoreService.getProductsFlow().collect { _allProducts.value = it }
        }
    }

    val cartItemCount: StateFlow<Int> = cartRepo.items
        .map { it.sumOf { item -> item.quantity } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun setQuery(q: String) { _uiState.update { it.copy(query = q) } }
    fun setCategoryFilter(cat: String) { _uiState.update { it.copy(categoryFilter = cat, brandFilter = null) } }
    fun setBrandFilter(brand: String) { _uiState.update { it.copy(brandFilter = brand, categoryFilter = "ALL") } }
    fun isWishlisted(product: Product) = wishlistRepo.isWishlisted(product)
    fun toggleWishlist(product: Product) { wishlistRepo.toggle(product) }
    fun addToCart(product: Product) { viewModelScope.launch { cartRepo.addItem(product) } }
}

// ─── Product Detail ViewModel ─────────────────────────────────────────────────

data class ProductDetailUiState(
    val product: Product? = null,
    val selectedSize: String? = null,
    val selectedColor: String = "Standard",
    val isWishlisted: Boolean = false,
    val cartCount: Int = 0
)

class ProductDetailViewModel(
    private val cartRepo: CartRepository,
    private val wishlistRepo: WishlistRepository,
    private val firestoreService: FirestoreService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            cartRepo.items.collect { items ->
                _uiState.update { it.copy(cartCount = items.sumOf { i -> i.quantity }) }
            }
        }
    }

    fun loadProduct(productId: Int) {
        viewModelScope.launch {
            val product = firestoreService.getAllProducts().find { it.id == productId }
            _uiState.update { it.copy(
                product = product,
                isWishlisted = product?.let { p -> wishlistRepo.isWishlisted(p) } ?: false
            ) }
        }
    }

    fun selectSize(size: String) { _uiState.update { it.copy(selectedSize = size) } }
    fun selectColor(color: String) { _uiState.update { it.copy(selectedColor = color) } }
    fun toggleWishlist() {
        val product = _uiState.value.product ?: return
        wishlistRepo.toggle(product)
        _uiState.update { it.copy(isWishlisted = wishlistRepo.isWishlisted(product)) }
    }
    fun addToCart(): Boolean {
        val state = _uiState.value
        val product = state.product ?: return false
        if (state.selectedSize == null) return false
        viewModelScope.launch { cartRepo.addItem(product, state.selectedSize, state.selectedColor) }
        return true
    }
}

// ─── Cart ViewModel ───────────────────────────────────────────────────────────

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val total: Double = 0.0,
    val isProcessing: Boolean = false
)

class CartViewModel(
    private val cartRepo: CartRepository,
    private val firestoreService: FirestoreService
) : ViewModel() {
    
    private val _isProcessing = MutableStateFlow(false)
    
    val uiState: StateFlow<CartUiState> = combine(cartRepo.items, _isProcessing) { items, processing ->
        val subtotal = items.sumOf { it.product.price * it.quantity }
        val delivery = if (items.isNotEmpty()) 1.0 else 0.0
        CartUiState(items, subtotal, delivery, subtotal + delivery, processing)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CartUiState())

    fun increase(item: CartItem) { cartRepo.increaseQty(item) }
    fun decrease(item: CartItem) { cartRepo.decreaseQty(item) }
    fun remove(item: CartItem) { cartRepo.removeItem(item) }
    fun updateSize(item: CartItem, size: String) { cartRepo.updateSize(item, size) }
    fun updateQty(item: CartItem, qty: Int) { cartRepo.updateQty(item, qty) }

    fun checkout(onSuccess: (String) -> Unit) {
        val state = uiState.value
        if (state.items.isEmpty()) return
        
        viewModelScope.launch {
            _isProcessing.value = true
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
            val orderId = "#ZND-${System.currentTimeMillis().toString().takeLast(8)}"
            val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
            
            val order = Order(
                id = orderId,
                date = date,
                items = state.items.toList(),
                total = state.total,
                status = OrderStatus.PROCESSING,
                userId = userId,
                userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "User"
            )
            
            try {
                firestoreService.placeOrder(order)
                cartRepo.clear()
                onSuccess(orderId)
            } finally {
                _isProcessing.value = false
            }
        }
    }
}

// ─── Wishlist ViewModel ───────────────────────────────────────────────────────

class WishlistViewModel(
    private val wishlistRepo: WishlistRepository,
    private val cartRepo: CartRepository
) : ViewModel() {
    val items: StateFlow<List<Product>> = wishlistRepo.items
        .map { it.toList() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    fun removeFromWishlist(product: Product) { wishlistRepo.toggle(product) }
    fun addToCart(product: Product) { viewModelScope.launch { cartRepo.addItem(product) } }
}

// ─── Orders ViewModel ────────────────────────────────────────────────────────

data class OrdersUiState(
    val orders: List<Order> = emptyList(),
    val statusFilter: OrderStatus? = null,
    val isLoading: Boolean = false
)

class OrdersViewModel(
    private val firestoreService: FirestoreService
) : ViewModel() {
    
    private val _statusFilter = MutableStateFlow<OrderStatus?>(null)

    val uiState: StateFlow<OrdersUiState> = combine(
        firestoreService.getAllOrdersFlow(),
        _statusFilter
    ) { orders, filter ->
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        var myOrders = if (userId != null) orders.filter { it.userId == userId } else emptyList()
        
        if (filter != null) {
            myOrders = myOrders.filter { it.status == filter }
        }
        
        OrdersUiState(orders = myOrders, statusFilter = filter, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, OrdersUiState(isLoading = true))

    fun setFilter(status: OrderStatus?) {
        _statusFilter.value = status
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            firestoreService.updateOrderStatus(orderId, OrderStatus.REJECTED)
        }
    }
}
