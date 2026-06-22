package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Home : Screen("home")
    object BookDetail : Screen("book_detail/{bookId}") {
        fun createRoute(bookId: Int) = "book_detail/$bookId"
    }
    object Cart : Screen("cart")
    object Wishlist : Screen("wishlist")
    object Checkout : Screen("checkout")
    object OrderConfirmation : Screen("order_confirmation/{orderId}") {
        fun createRoute(orderId: Long) = "order_confirmation/$orderId"
    }
    object Dashboard : Screen("dashboard")
    object Admin : Screen("admin")
}
