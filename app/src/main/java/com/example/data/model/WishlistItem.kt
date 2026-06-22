package com.example.data.model

import androidx.room.Entity

@Entity(
    tableName = "wishlist_items",
    primaryKeys = ["userId", "bookId"]
)
data class WishlistItem(
    val userId: Int,
    val bookId: Int
)
