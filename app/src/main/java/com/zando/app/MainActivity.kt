package com.zando.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.zando.app.di.ZandoViewModelFactory
import com.zando.app.model.UserRole
import com.zando.app.ui.admin.AdminDashboardScreen
import com.zando.app.ui.admin.ManageOrdersScreen
import com.zando.app.ui.admin.ManageProductsScreen
import com.zando.app.ui.admin.ManageUsersScreen
import com.zando.app.ui.brands.BrandsScreen
import com.zando.app.ui.cart.CartScreen
import com.zando.app.ui.detail.ProductDetailScreen
import com.zando.app.ui.home.HomeScreen
import com.zando.app.ui.login.LoginScreen
import com.zando.app.ui.orders.OrdersScreen
import com.zando.app.ui.profile.ProfileScreen
import com.zando.app.ui.search.SearchScreen
import com.zando.app.ui.theme.ZandoTheme
import com.zando.app.ui.wishlist.WishlistScreen
import com.zando.app.viewmodel.*

// ─── Routes ───────────────────────────────────────────────────────────────────

object Routes {
    const val LOGIN          = "login"
    const val HOME           = "home"
    const val SEARCH         = "search?category={category}&brand={brand}"
    const val BRANDS         = "brands"
    const val CART           = "cart"
    const val DETAIL         = "detail/{productId}"
    const val ORDERS         = "orders"
    const val WISHLIST       = "wishlist"
    const val PROFILE        = "profile"
    const val FAQ            = "faq"
    const val PRIVACY_POLICY = "privacy_policy"
    const val CONTACT_US     = "contact_us"
    
    // Admin Routes
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val MANAGE_PRODUCTS = "manage_products"
    const val MANAGE_ORDERS   = "manage_orders"
    const val MANAGE_USERS    = "manage_users"

    fun search(category: String? = null, brand: String? = null): String {
        val cat = category?.let { "category=$it" } ?: ""
        val br  = brand?.let { "brand=$it" } ?: ""
        val params = listOf(cat, br).filter { it.isNotBlank() }.joinToString("&")
        return if (params.isBlank()) "search?category=&brand=" else "search?$params"
    }
    fun detail(productId: Int) = "detail/$productId"
}

// ─── Bottom Nav Items ─────────────────────────────────────────────────────────

enum class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    HOME    (Routes.HOME,     "Home",      Icons.Default.Home),
    MENU    ("search?category=&brand=", "Menu", Icons.Default.Search),
    BRANDS  (Routes.BRANDS,   "Brands",    Icons.Default.Sell),
    WISHLIST(Routes.WISHLIST, "Wish List", Icons.Default.Favorite),
    PROFILE (Routes.PROFILE,  "Me",        Icons.Default.Person),
}

// ─── MainActivity ─────────────────────────────────────────────────────────────

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var darkTheme by remember { mutableStateOf(false) }
            var language by remember { mutableStateOf("en") }

            ZandoTheme(darkTheme = darkTheme) {
                ZandoApp(
                    darkTheme = darkTheme,
                    onToggleDarkTheme = { darkTheme = it },
                    language = language,
                    onLanguageChange = { language = it }
                )
            }
        }
    }
}

// ─── App Composable ───────────────────────────────────────────────────────────

@Composable
fun ZandoApp(
    darkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    language: String,
    onLanguageChange: (String) -> Unit
) {
    val navController = rememberNavController()
    val factory       = remember { ZandoViewModelFactory() }

    // Shared ViewModels
    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val cartViewModel: CartViewModel    = viewModel(factory = factory)
    val wishlistViewModel: WishlistViewModel = viewModel(factory = factory)
    val homeViewModel: HomeViewModel    = viewModel(factory = factory)
    val searchViewModel: SearchViewModel = viewModel(factory = factory)
    val detailViewModel: ProductDetailViewModel = viewModel(factory = factory)
    val ordersViewModel: OrdersViewModel = viewModel(factory = factory)

    val authState by authViewModel.uiState.collectAsState()
    val userProfile = authState.userProfile

    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    val isAdminRoute = currentRoute == Routes.ADMIN_DASHBOARD ||
            currentRoute == Routes.MANAGE_PRODUCTS ||
            currentRoute == Routes.MANAGE_ORDERS ||
            currentRoute == Routes.MANAGE_USERS

    val showBottomNav = currentRoute != Routes.LOGIN &&
        currentRoute != Routes.CART &&
        currentRoute?.startsWith("detail") == false &&
        currentRoute != Routes.ORDERS &&
        currentRoute != Routes.FAQ &&
        currentRoute != Routes.PRIVACY_POLICY &&
        currentRoute != Routes.CONTACT_US &&
        !isAdminRoute

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    BottomNavItem.entries.forEach { item ->
                        val selected = currentRoute == item.route ||
                            (item == BottomNavItem.MENU && currentRoute?.startsWith("search") == true)

                        val translatedLabel = when(language) {
                            "km" -> when(item) {
                                BottomNavItem.HOME -> "ទំព័រដើម"
                                BottomNavItem.MENU -> "ម៉ឺនុយ"
                                BottomNavItem.BRANDS -> "ម៉ាក"
                                BottomNavItem.WISHLIST -> "បញ្ជីដែលចង់បាន"
                                BottomNavItem.PROFILE -> "ខ្ញុំ"
                            }
                            "zh" -> when(item) {
                                BottomNavItem.HOME -> "首页"
                                BottomNavItem.MENU -> "菜单"
                                BottomNavItem.BRANDS -> "品牌"
                                BottomNavItem.WISHLIST -> "愿望清单"
                                BottomNavItem.PROFILE -> "我的"
                            }
                            else -> item.label
                        }

                        NavigationBarItem(
                            selected = selected,
                            onClick  = {
                                navController.navigate(item.route) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon  = {
                                if (item == BottomNavItem.HOME) {
                                    Text("Z.", fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold, fontSize = 20.sp)
                                } else {
                                    Icon(item.icon, contentDescription = translatedLabel)
                                }
                            },
                            label = { Text(translatedLabel) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Routes.LOGIN,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        val profile = authViewModel.uiState.value.userProfile
                        if (profile?.role == UserRole.ADMIN) {
                            navController.navigate(Routes.ADMIN_DASHBOARD) { popUpTo(Routes.LOGIN) { inclusive = true } }
                        } else {
                            navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                        }
                    }
                )
            }

            composable(Routes.HOME) {
                HomeScreen(
                    viewModel              = homeViewModel,
                    onNavigateToSearch     = { cat, brand -> navController.navigate(Routes.search(cat, brand)) },
                    onNavigateToCart       = { navController.navigate(Routes.CART) },
                    onNavigateToProduct    = { id -> navController.navigate(Routes.detail(id)) },
                    language               = language
                )
            }

            composable(
                route     = "search?category={category}&brand={brand}",
                arguments = listOf(
                    navArgument("category") { type = NavType.StringType; defaultValue = "" },
                    navArgument("brand")    { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStack ->
                val cat   = backStack.arguments?.getString("category")?.takeIf { it.isNotBlank() }
                val brand = backStack.arguments?.getString("brand")?.takeIf { it.isNotBlank() }
                SearchScreen(
                    viewModel            = searchViewModel,
                    initialCategory      = cat,
                    initialBrand         = brand,
                    onBack               = { navController.navigateUp() },
                    onNavigateToCart     = { navController.navigate(Routes.CART) },
                    onNavigateToProduct  = { id -> navController.navigate(Routes.detail(id)) },
                    language             = language
                )
            }

            composable(Routes.BRANDS) {
                BrandsScreen(
                    viewModel = searchViewModel,
                    onNavigateToCart = { navController.navigate(Routes.CART) },
                    onBrandClick = { brand ->
                        navController.navigate(Routes.search(brand = brand))
                    },
                    language = language
                )
            }

            composable(Routes.CART) {
                CartScreen(
                    viewModel        = cartViewModel,
                    onBack           = { navController.navigateUp() },
                    onCheckoutSuccess = {
                        navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } }
                    }
                )
            }

            composable(
                route     = Routes.DETAIL,
                arguments = listOf(navArgument("productId") { type = NavType.IntType })
            ) { backStack ->
                val productId = backStack.arguments?.getInt("productId") ?: return@composable
                ProductDetailScreen(
                    productId          = productId,
                    viewModel          = detailViewModel,
                    onBack             = { navController.navigateUp() },
                    onNavigateToCart   = { navController.navigate(Routes.CART) },
                    onNavigateToFaq    = { navController.navigate(Routes.FAQ) },
                    onNavigateToProduct = { id -> 
                        navController.navigate(Routes.detail(id)) {
                            // popUpTo(Routes.HOME) // Optional: avoid deep stack
                        }
                    },
                    language           = language
                )
            }

            composable(Routes.WISHLIST) {
                WishlistScreen(
                    viewModel          = wishlistViewModel,
                    onNavigateToProduct = { id -> navController.navigate(Routes.detail(id)) },
                    language           = language
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    userProfile              = userProfile,
                    onNavigateToOrders       = { navController.navigate(Routes.ORDERS) },
                    onNavigateToAdmin        = { navController.navigate(Routes.ADMIN_DASHBOARD) },
                    onNavigateToPrivacyPolicy = { navController.navigate(Routes.PRIVACY_POLICY) },
                    onNavigateToFaq          = { navController.navigate(Routes.FAQ) },
                    onNavigateToContactUs    = { navController.navigate(Routes.CONTACT_US) },
                    onLogout = {
                        authViewModel.signOut()
                        navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                    },
                    darkTheme         = darkTheme,
                    onToggleDarkTheme = onToggleDarkTheme,
                    language          = language,
                    onLanguageChange  = onLanguageChange
                )
            }

            composable(Routes.ORDERS) {
                OrdersScreen(viewModel = ordersViewModel, onBack = { navController.navigateUp() })
            }
            
            // Admin Composable
            composable(Routes.ADMIN_DASHBOARD) {
                AdminDashboardScreen(
                    viewModel = viewModel(factory = factory),
                    onNavigateToProducts = { navController.navigate(Routes.MANAGE_PRODUCTS) },
                    onNavigateToOrders   = { navController.navigate(Routes.MANAGE_ORDERS) },
                    onNavigateToUsers    = { navController.navigate(Routes.MANAGE_USERS) },
                    onLogout = {
                        authViewModel.signOut()
                        navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                    }
                )
            }
            
            composable(Routes.MANAGE_PRODUCTS) {
                ManageProductsScreen(
                    viewModel = viewModel(factory = factory),
                    onBack = { navController.navigateUp() }
                )
            }
            
            composable(Routes.MANAGE_ORDERS) {
                ManageOrdersScreen(
                    viewModel = viewModel(factory = factory),
                    onBack = { navController.navigateUp() }
                )
            }
            
            composable(Routes.MANAGE_USERS) {
                ManageUsersScreen(
                    viewModel = viewModel(factory = factory),
                    onBack = { navController.navigateUp() }
                )
            }

            composable(Routes.FAQ) {
                val title = when(language) {
                    "km" -> "សំណួរដែលសួរញឹកញាប់"
                    "zh" -> "常见问题"
                    else -> "FAQs"
                }
                InfoScreen(title = title, emoji = "❓",
                    content = when(language) {
                        "km" -> """
                            សំណួរ៖ តើខ្ញុំត្រូវប្រគល់ទំនិញវិញដោយរបៀបណា?
                            ចម្លើយ៖ ការត្រឡប់មកវិញត្រូវបានទទួលយកក្នុងរយៈពេល ៣០ ថ្ងៃបន្ទាប់ពីការដឹកជញ្ជូន។ ទំនិញត្រូវតែស្ថិតក្នុងស្ថានភាពដើមជាមួយនឹងស្លាកដែលបានភ្ជាប់។

                            សំណួរ៖ តើការដឹកជញ្ជូនចំណាយពេលប៉ុន្មាន?
                            ចម្លើយ៖ ការដឹកជញ្ជូនស្តង់ដារ៖ ៣-៥ ថ្ងៃធ្វើការ។ រហ័ស៖ ១-២ ថ្ងៃធ្វើការ។
                        """.trimIndent()
                        "zh" -> """
                            问：我该如何退货？
                            答：交货后 30 天内接受退货。物品必须处于原始状态并附带标签。

                            问：送货需要多长时间？
                            答：标准送货：3–5 个工作日。快递：1–2 个工作日。
                        """.trimIndent()
                        else -> """
                            Q: How do I return an item?
                            A: Returns are accepted within 30 days of delivery. Items must be in original condition with tags attached.

                            Q: How long does delivery take?
                            A: Standard delivery: 3–5 business days. Express: 1–2 business days.

                            Q: Can I exchange for a different size?
                            A: Yes! Contact us within 14 days of delivery for a size exchange.

                            Q: What payment methods are accepted?
                            A: We accept all major credit cards, PayPal, and bank transfer.
                        """.trimIndent()
                    },
                    onBack = { navController.navigateUp() }
                )
            }

            composable(Routes.PRIVACY_POLICY) {
                InfoScreen(title = "Privacy Policy", emoji = "🔒",
                    content = "ZANDO Privacy Policy...",
                    onBack = { navController.navigateUp() }
                )
            }

            composable(Routes.CONTACT_US) {
                val title = when(language) {
                    "km" -> "ទាក់ទងមកយើង"
                    "zh" -> "联系我们"
                    else -> "Contact Us"
                }
                val phone = "972519746"
                val content = when(language) {
                    "km" -> """
                        យើងនៅទីនេះដើម្បីជួយ!

                        📧 អ៊ីមែល: support@zando.com
                        📱 ទូរស័ព្ទ: $phone
                        🕐 ម៉ោង: ចន្ទ-សុក្រ, 9AM–6PM

                        💬 ការជជែកផ្ទាល់
                        មាននៅក្នុងកម្មវិធីក្នុងម៉ោងធ្វើការ។

                        📍 អាសយដ្ឋាន
                        Zando HQ, ភ្នំពេញ, កម្ពុជា។
                    """.trimIndent()
                    "zh" -> """
                        我们在这里为您提供帮助！

                        📧 电子邮件: support@zando.com
                        📱 电话: $phone
                        🕐 营业时间: 周一至周五, 9AM–6PM

                        💬 在线聊天
                        营业时间内可在应用内使用。

                        📍 地址
                        Zando HQ, 金边, 柬埔寨。
                    """.trimIndent()
                    else -> """
                        We're here to help!

                        📧 Email: support@zando.com
                        📱 Phone: $phone
                        🕐 Hours: Mon–Fri, 9AM–6PM

                        💬 Live Chat
                        Available in-app during business hours.

                        📍 Address
                        Zando HQ, Phnom Penh, Cambodia.
                    """.trimIndent()
                }
                InfoScreen(title = title, emoji = "📞", content = content, onBack = { navController.navigateUp() }
                )
            }
        }
    }
}

@Composable
fun InfoScreen(title: String, emoji: String, content: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(title, style = MaterialTheme.typography.headlineSmall)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(emoji, fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(content, style = MaterialTheme.typography.bodyMedium, lineHeight = 24.sp)
        }
    }
}
