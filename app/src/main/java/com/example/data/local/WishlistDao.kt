package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.WishlistItem
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist_items WHERE userId = :userId")
    fun getWishlistItems(userId: Int): Flow<List<WishlistItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWishlistItem(wishlistItem: WishlistItem)

    @Query("DELETE FROM wishlist_items WHERE userId = :userId AND bookId = :bookId")
    suspend fun deleteWishlistItem(userId: Int, bookId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist_items WHERE userId = :userId AND bookId = :bookId LIMIT 1)")
    fun isWishlisted(userId: Int, bookId: Int): Flow<Boolean>
}
