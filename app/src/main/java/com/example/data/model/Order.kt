package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val orderDate: Long = System.currentTimeMillis(),
    val totalAmount: Double,
    val status: String = "Pending",
    val itemsSummary: String
)
