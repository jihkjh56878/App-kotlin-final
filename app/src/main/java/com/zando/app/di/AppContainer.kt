package com.zando.app.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zando.app.model.CartRepository
import com.zando.app.model.FirestoreService
import com.zando.app.model.OrdersRepository
import com.zando.app.model.WishlistRepository
import com.zando.app.viewmodel.*

/**
 * Simple manual dependency injection container.
 */
object AppContainer {
    val cartRepository: CartRepository by lazy { CartRepository() }
    val wishlistRepository: WishlistRepository by lazy { WishlistRepository() }
    val ordersRepository: OrdersRepository by lazy { OrdersRepository() }
    val firestoreService: FirestoreService by lazy { FirestoreService() }
}

// ─── ViewModelFactory ─────────────────────────────────────────────────────────

class ZandoViewModelFactory(
    private val cart: CartRepository = AppContainer.cartRepository,
    private val wishlist: WishlistRepository = AppContainer.wishlistRepository,
    private val orders: OrdersRepository = AppContainer.ordersRepository,
    private val firestore: FirestoreService = AppContainer.firestoreService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(AuthViewModel::class.java)    -> AuthViewModel(firestore) as T
        modelClass.isAssignableFrom(CartViewModel::class.java)    -> CartViewModel(cart, firestore) as T
        modelClass.isAssignableFrom(WishlistViewModel::class.java)-> WishlistViewModel(wishlist, cart) as T
        modelClass.isAssignableFrom(HomeViewModel::class.java)    -> HomeViewModel(cart, wishlist, firestore) as T
        modelClass.isAssignableFrom(SearchViewModel::class.java)  -> SearchViewModel(cart, wishlist, firestore) as T
        modelClass.isAssignableFrom(ProductDetailViewModel::class.java) -> ProductDetailViewModel(cart, wishlist, firestore) as T
        modelClass.isAssignableFrom(OrdersViewModel::class.java)  -> OrdersViewModel(firestore) as T
        
        // Admin ViewModels
        modelClass.isAssignableFrom(AdminDashboardViewModel::class.java) -> AdminDashboardViewModel(firestore) as T
        modelClass.isAssignableFrom(ManageProductsViewModel::class.java) -> ManageProductsViewModel(firestore) as T
        modelClass.isAssignableFrom(ManageOrdersViewModel::class.java)   -> ManageOrdersViewModel(firestore) as T
        modelClass.isAssignableFrom(ManageUsersViewModel::class.java)    -> ManageUsersViewModel(firestore) as T

        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
