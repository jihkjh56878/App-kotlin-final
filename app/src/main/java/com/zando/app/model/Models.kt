package com.zando.app.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

// ─── Domain Models ────────────────────────────────────────────────────────────

@IgnoreExtraProperties
data class Product(
    val id: Int = 0,
    val name: String = "",
    val brand: String = "Zando",
    val price: Double = 0.0,
    val oldPrice: Double? = null,
    val category: String = "",
    val imageEmoji: String = "",
    val imageUrl: String? = null,
    val badge: String? = null,
    val isNew: Boolean = false,
    val isSale: Boolean = false,
    val description: String = "Premium quality product from our latest collection. Carefully crafted for style and comfort.",
    val modelInfo: String = "Model is 170cm, wearing size S.",
    val stock: Int = 10
)

data class CartItem(
    val product: Product = Product(),
    val quantity: Int = 1,
    val size: String = "M",
    val color: String = "Standard"
)

data class Order(
    val id: String = "",
    val date: String = "",
    val timestamp: Long = 0L,
    val items: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val status: OrderStatus = OrderStatus.PROCESSING,
    val userId: String = "",
    val userName: String = "John Doe"
)

enum class OrderStatus { PROCESSING, SHIPPED, DELIVERED, ACCEPTED, REJECTED }

@IgnoreExtraProperties
data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.USER,
    @get:PropertyName("blocked")
    @set:PropertyName("blocked")
    @PropertyName("blocked")
    var isBlocked: Boolean = false
)

enum class UserRole { USER, ADMIN }

// ─── In-memory Product Catalogue ─────────────────────────────────────────────

object ProductRepository {
    val allProducts = listOf(
        // Women
        Product(1,  "Floral Wrap Dress",      "Zando",       45.0, 65.0,  "Women",      "🌸", null, "SALE", isSale = true),
        Product(3,  "Silk Ruffle Blouse",      "Gatoni",      38.0, 55.0,  "Women",      "🌹", null, "NEW",  isNew = true),
        Product(6,  "Summer Sundress",         "Pomelo",      42.0, null,  "Women",      "☀️", null, "NEW",  isNew = true),
        Product(10, "High-Waist Skinny Jeans", "Zando",       58.0, 75.0,  "Women",      "👖", null, "SALE", isSale = true),
        Product(101, "Beige Pleated Skirt",    "Zando",       3.0,  5.0,   "Women",      "👗", null, "SALE", isSale = true),
        Product(102, "Blue Plaid Skirt",       "Zando",       3.0,  6.0,   "Women",      "👗", null, "BEST"),
        Product(103, "Pink Mini Skirt",        "Routine",     15.0, 20.0,  "Women",      "👗", null, "NEW", isNew = true),
        Product(104, "Tennis White Skirt",     "361°",        12.0, null,  "Women",      "👗", null, "NEW"),
        Product(105, "Denim Short Skirt",      "Tag Space",   18.0, 25.0,  "Women",      "👗", null, "SALE", isSale = true),
        
        // Men
        Product(4,  "Classic Denim Jacket",    "Routine",     65.0, 90.0,  "Men",        "🧵", null, "SALE", isSale = true),
        Product(8,  "Slim-Fit Blazer",         "TEN+ELEVEN",  95.0, 130.0, "Men",        "🧥", null, "SALE", isSale = true),
        Product(11, "Relaxed Linen Trousers",  "Tag Space",   68.0, null,  "Men",        "👕"),
        
        // Kids
        Product(13, "Cartoon Graphic Tee",     "361°",        15.0, null,  "Kids",       "🦖", null, "NEW",  isNew = true),
        Product(14, "Denim Bib Overalls",      "Zando",       32.0, 45.0,  "Kids",       "🧒", null, "SALE", isSale = true),
        Product(15, "Puffer Jacket",           "Gatoni",      48.0, null,  "Kids",       "🌈"),
        
        // Shoes
        Product(2,  "Classic White Sneakers",  "361°",        89.0, null,  "Shoes",      "👟"),
        Product(7,  "Premium Leather Loafers", "Zando",       72.0, 103.0, "Shoes",      "👞", null, "-30%", isSale = true),
        Product(12, "Strappy Block Heels",     "Gatoni",      85.0, 110.0, "Shoes",      "👠", null, "-23%", isSale = true),
        
        // Accessories
        Product(5,  "Structured Tote Bag",     "Zando",       120.0, null, "Accessories","👜"),
        Product(9,  "Gold Hoop Earrings",      "Pomelo",      28.0, null,  "Accessories","💛", null, "NEW",  isNew = true),
        
        // Z.Home
        Product(16, "Velvet Throw Pillow",     "Zando",       25.0, null,  "Z.Home",     "🛋️", null, "NEW",  isNew = true),
        Product(17, "Soy Wax Candle",          "Tag Space",   18.0, null,  "Z.Home",     "🕯️"),
        Product(18, "Luxury Bedding Set",      "Zando",       85.0, 120.0, "Z.Home",     "🛏️", null, "SALE", isSale = true),

        // Lifestyle
        Product(19, "Yoga Mat Pro",            "Routine",     35.0, null,  "Lifestyle",  "🧘"),
        Product(20, "Insulated Water Bottle",  "361°",        22.0, 30.0,  "Lifestyle",  "🍼", null, "SALE", isSale = true),
        Product(21, "Premium Headphones",      "Tag Space",   150.0, null, "Lifestyle",  "🎧", null, "NEW",  isNew = true),
    )
}
