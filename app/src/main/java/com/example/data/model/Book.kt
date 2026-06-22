package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val category: String,
    val description: String,
    val price: Double,
    val rating: Float,
    val featured: Boolean = false,
    val customCoverColorHex: String = "#1E2A38"
)
