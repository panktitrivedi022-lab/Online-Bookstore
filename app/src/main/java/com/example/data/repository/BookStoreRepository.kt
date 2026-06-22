package com.example.data.repository

import com.example.data.local.BookDao
import com.example.data.local.CartDao
import com.example.data.local.OrderDao
import com.example.data.local.UserDao
import com.example.data.local.WishlistDao
import com.example.data.model.Book
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.User
import com.example.data.model.WishlistItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class BookStoreRepository(
    private val userDao: UserDao,
    private val bookDao: BookDao,
    private val cartDao: CartDao,
    private val wishlistDao: WishlistDao,
    private val orderDao: OrderDao
) {
    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()
    val featuredBooks: Flow<List<Book>> = bookDao.getFeaturedBooks()
    val allCategories: Flow<List<String>> = bookDao.getAllCategories()

    suspend fun initializeDatabase() {
        // 1. Seed standard administrators and test users
        val existingAdmin = userDao.getUserByEmail("admin@bookstore.com")
        if (existingAdmin == null) {
            userDao.insertUser(
                User(
                    username = "admin",
                    email = "admin@bookstore.com",
                    passwordHash = "admin123",
                    name = "Store Administrator",
                    isAdmin = true
                )
            )
        }

        val existingUser = userDao.getUserByEmail("user@bookstore.com")
        if (existingUser == null) {
            userDao.insertUser(
                User(
                    username = "johndoe",
                    email = "user@bookstore.com",
                    passwordHash = "user123",
                    name = "John Doe",
                    isAdmin = false
                )
            )
        }

        // 2. Seed standard books if empty
        val currentBooks = allBooks.firstOrNull() ?: emptyList()
        if (currentBooks.isEmpty()) {
            val sampleBooks = listOf(
                Book(
                    title = "Atomic Habits",
                    author = "James Clear",
                    category = "Self-Help",
                    description = "An easy & proven way to build good habits & break bad ones. Learn the tiny changes that yield remarkable results.",
                    price = 16.99,
                    rating = 4.8f,
                    featured = true,
                    customCoverColorHex = "#1D4130"
                ),
                Book(
                    title = "Clean Code",
                    author = "Robert C. Martin",
                    category = "Science & Tech",
                    description = "A handbook of agile software craftsmanship. Even bad code can function. But if code isn't clean, it can bring a development organization to its knees.",
                    price = 39.99,
                    rating = 4.7f,
                    featured = true,
                    customCoverColorHex = "#1C2F59"
                ),
                Book(
                    title = "Zero to One",
                    author = "Peter Thiel",
                    category = "Business & Finance",
                    description = "Notes on startups, or how to build the future. The next Bill Gates will not build an operating system. The next Larry Page or Sergey Brin won't make a search engine.",
                    price = 15.50,
                    rating = 4.5f,
                    featured = false,
                    customCoverColorHex = "#2E3B4E"
                ),
                Book(
                    title = "To Kill a Mockingbird",
                    author = "Harper Lee",
                    category = "Fiction",
                    description = "The unforgettable novel of a childhood in a sleepy Southern town and the crisis of conscience that rocked it, Compassionate, dramatic, and deeply moving.",
                    price = 12.99,
                    rating = 4.9f,
                    featured = true,
                    customCoverColorHex = "#6E2323"
                ),
                Book(
                    title = "Steve Jobs",
                    author = "Walter Isaacson",
                    category = "Biography",
                    description = "Based on more than forty interviews with Jobs conducted over two years—as well as interviews with more than a hundred family members, friends, adversaries, competitors, and colleagues.",
                    price = 18.99,
                    rating = 4.6f,
                    featured = false,
                    customCoverColorHex = "#2D3E50"
                ),
                Book(
                    title = "The Midnight Library",
                    author = "Matt Haig",
                    category = "Fiction",
                    description = "Between life and death there is a library, and within that library, the shelves go on forever. Every book provides a chance to try another life you could have lived.",
                    price = 14.25,
                    rating = 4.4f,
                    featured = false,
                    customCoverColorHex = "#3F265D"
                ),
                Book(
                    title = "Thinking, Fast and Slow",
                    author = "Daniel Kahneman",
                    category = "Self-Help",
                    description = "Kahneman takes us on a groundbreaking tour of the mind and explains the two systems that drive the way we think.",
                    price = 18.00,
                    rating = 4.6f,
                    featured = false,
                    customCoverColorHex = "#2C3D4D"
                ),
                Book(
                    title = "The Intelligent Investor",
                    author = "Benjamin Graham",
                    category = "Business & Finance",
                    description = "The classic text on value investing. To invest successfully over a lifetime does not require a stratospheric IQ, unusual business insights, or inside information.",
                    price = 22.00,
                    rating = 4.7f,
                    featured = true,
                    customCoverColorHex = "#1D4742"
                ),
                Book(
                    title = "Brief History of Time",
                    author = "Stephen Hawking",
                    category = "Science & Tech",
                    description = "A landmark volume in science writing by one of the great minds of our time, Stephen Hawking explores the frontiers of space, time and gravity.",
                    price = 14.99,
                    rating = 4.8f,
                    featured = false,
                    customCoverColorHex = "#0B0F17"
                )
            )

            for (book in sampleBooks) {
                bookDao.insertBook(book)
            }
        }
    }

    // Users
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    suspend fun getUserById(userId: Int): User? = userDao.getUserById(userId)
    suspend fun registerUser(user: User): Long = userDao.insertUser(user)

    // Books
    suspend fun getBookById(id: Int): Book? = bookDao.getBookById(id)
    suspend fun insertBook(book: Book): Long = bookDao.insertBook(book)
    suspend fun updateBook(book: Book) = bookDao.updateBook(book)
    suspend fun deleteBook(book: Book) = bookDao.deleteBook(book)

    // Cart
    fun getCartItems(userId: Int): Flow<List<CartItem>> = cartDao.getCartItems(userId)
    suspend fun addToCart(userId: Int, bookId: Int, quantity: Int) {
        cartDao.insertOrUpdateCartItem(CartItem(userId, bookId, quantity))
    }
    suspend fun removeFromCart(userId: Int, bookId: Int) {
        cartDao.deleteCartItem(userId, bookId)
    }
    suspend fun clearCart(userId: Int) {
        cartDao.clearCart(userId)
    }

    // Wishlist
    fun getWishlistItems(userId: Int): Flow<List<WishlistItem>> = wishlistDao.getWishlistItems(userId)
    suspend fun addToWishlist(userId: Int, bookId: Int) {
        wishlistDao.insertWishlistItem(WishlistItem(userId, bookId))
    }
    suspend fun removeFromWishlist(userId: Int, bookId: Int) {
        wishlistDao.deleteWishlistItem(userId, bookId)
    }
    fun isWishlisted(userId: Int, bookId: Int): Flow<Boolean> = wishlistDao.isWishlisted(userId, bookId)

    // Orders
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()
    fun getOrdersForUser(userId: Int): Flow<List<Order>> = orderDao.getOrdersForUser(userId)
    suspend fun createOrder(userId: Int, totalAmount: Double, itemsSummary: String): Long {
        val o = Order(userId = userId, totalAmount = totalAmount, itemsSummary = itemsSummary)
        val orderId = orderDao.insertOrder(o)
        cartDao.clearCart(userId)
        return orderId
    }
    suspend fun updateOrderStatus(order: Order) {
        orderDao.updateOrder(order)
    }
}
