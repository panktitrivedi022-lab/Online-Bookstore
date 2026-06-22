package com.example.data.model

import androidx.room.Entity

@Entity(
    tableName = "cart_items",
    primaryKeys = ["userId", "bookId"]
)
data class CartItem(
    val userId: Int,
    val bookId: Int,
    val quantity: Int
)
