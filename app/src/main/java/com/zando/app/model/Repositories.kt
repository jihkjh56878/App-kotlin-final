package com.zando.app.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ─── Cart Repository ──────────────────────────────────────────────────────────

class CartRepository {

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    fun addItem(product: Product, size: String = "M", color: String = "Standard") {
        _items.update { current ->
            val existing = current.find { it.product.id == product.id && it.size == size && it.color == color }
            if (existing != null) {
                current.map { if (it === existing) it.copy(quantity = it.quantity + 1) else it }
            } else {
                current + CartItem(product = product, quantity = 1, size = size, color = color)
            }
        }
    }

    fun removeItem(cartItem: CartItem) {
        _items.update { it.filterNot { item -> item === cartItem } }
    }

    fun increaseQty(cartItem: CartItem) {
        _items.update { current ->
            current.map { if (it === cartItem) it.copy(quantity = it.quantity + 1) else it }
        }
    }

    fun decreaseQty(cartItem: CartItem) {
        _items.update { current ->
            if (cartItem.quantity <= 1) {
                current.filterNot { it === cartItem }
            } else {
                current.map { if (it === cartItem) it.copy(quantity = it.quantity - 1) else it }
            }
        }
    }

    fun updateQty(cartItem: CartItem, newQty: Int) {
        _items.update { current ->
            current.map { if (it === cartItem) it.copy(quantity = newQty) else it }
        }
    }

    fun updateSize(cartItem: CartItem, newSize: String) {
        _items.update { current ->
            current.map { if (it === cartItem) it.copy(size = newSize) else it }
        }
    }

    fun getTotalItems(): Int = _items.value.sumOf { it.quantity }

    fun getSubtotal(): Double = _items.value.sumOf { it.product.price * it.quantity }

    fun clear() { _items.value = emptyList() }
}

// ─── Wishlist Repository ──────────────────────────────────────────────────────

class WishlistRepository {

    private val _items = MutableStateFlow<Set<Product>>(emptySet())
    val items: StateFlow<Set<Product>> = _items.asStateFlow()

    fun toggle(product: Product) {
        _items.update { current ->
            if (current.contains(product)) current - product else current + product
        }
    }

    fun isWishlisted(product: Product): Boolean = _items.value.contains(product)
}

// ─── Orders Repository ────────────────────────────────────────────────────────

class OrdersRepository {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    fun addOrder(order: Order) {
        _orders.update { listOf(order) + it }
    }

    fun removeOrder(orderId: String) {
        _orders.update { it.filterNot { o -> o.id == orderId } }
    }

    fun getByStatus(status: OrderStatus): List<Order> =
        _orders.value.filter { it.status == status }
}
