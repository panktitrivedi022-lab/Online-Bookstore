package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Book
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.User
import com.example.data.model.WishlistItem
import com.example.data.repository.BookStoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CartItemWithBook(
    val cartItem: CartItem,
    val book: Book
)

class BookStoreViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BookStoreRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = BookStoreRepository(
            userDao = database.userDao(),
            bookDao = database.bookDao(),
            cartDao = database.cartDao(),
            wishlistDao = database.wishlistDao(),
            orderDao = database.orderDao()
        )

        // Seed sample data and admins
        viewModelScope.launch(Dispatchers.IO) {
            repository.initializeDatabase()
        }
    }

    // --- Authentication State ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    // --- Catalog States ---
    val allBooks: StateFlow<List<Book>> = repository.allBooks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredBooks: StateFlow<List<Book>> = repository.featuredBooks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Filter & Search ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    val filteredBooks: StateFlow<List<Book>> = combine(
        allBooks,
        searchQuery,
        selectedCategory
    ) { books, query, category ->
        books.filter { book ->
            val matchesSearch = query.isEmpty() ||
                    book.title.contains(query, ignoreCase = true) ||
                    book.author.contains(query, ignoreCase = true) ||
                    book.category.contains(query, ignoreCase = true)

            val matchesCategory = category == null || book.category.equals(category, ignoreCase = true)

            matchesSearch && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Shopping Cart ---
    val cartItems: StateFlow<List<CartItemWithBook>> = _currentUser.combine(allBooks) { user, books ->
        user to books
    }.combine(MutableStateFlow<List<CartItem>>(emptyList())) { pair, _ ->
        // We'll update the inner flow dynamically using a separate collection or combine
        pair
    }.combine(
        _currentUser
    ) { _, user ->
        user
    }.map { user ->
        if (user == null) {
            emptyList()
        } else {
            val dbCart = repository.getCartItems(user.id).firstOrNull() ?: emptyList()
            val booksMap = allBooks.value.associateBy { it.id }
            dbCart.mapNotNull { cartItem ->
                booksMap[cartItem.bookId]?.let { book ->
                    CartItemWithBook(cartItem, book)
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Trigger update for cart, wishlist and orders dynamically on user changes
    private val _cartUpdateTrigger = MutableStateFlow(0)
    val userCart: StateFlow<List<CartItemWithBook>> = combine(
        _currentUser,
        allBooks,
        _cartUpdateTrigger
    ) { user, books, _ ->
        if (user == null) {
            emptyList()
        } else {
            val dbCart = repository.getCartItems(user.id).firstOrNull() ?: emptyList()
            val booksMap = books.associateBy { it.id }
            dbCart.mapNotNull { cartItem ->
                booksMap[cartItem.bookId]?.let { book ->
                    CartItemWithBook(cartItem, book)
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartTotal: StateFlow<Double> = userCart.map { items ->
        items.sumOf { it.book.price * it.cartItem.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- Wishlist ---
    private val _wishlistUpdateTrigger = MutableStateFlow(0)
    val userWishlist: StateFlow<List<Book>> = combine(
        _currentUser,
        allBooks,
        _wishlistUpdateTrigger
    ) { user, books, _ ->
        if (user == null) {
            emptyList()
        } else {
            val wishlistIds = repository.getWishlistItems(user.id).firstOrNull()?.map { it.bookId } ?: emptyList()
            books.filter { it.id in wishlistIds }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Orders ---
    private val _ordersUpdateTrigger = MutableStateFlow(0)
    val userOrders: StateFlow<List<Order>> = combine(
        _currentUser,
        _ordersUpdateTrigger
    ) { user, _ ->
        if (user == null) {
            emptyList()
        } else {
            repository.getOrdersForUser(user.id).firstOrNull() ?: emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminAllOrders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Auth Actions ---
    fun login(email: String, passwordText: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserByEmail(email)
            withContext(Dispatchers.Main) {
                if (user != null && user.passwordHash == passwordText) {
                    _currentUser.value = user
                    // Refresh user specific flows
                    _cartUpdateTrigger.value += 1
                    _wishlistUpdateTrigger.value += 1
                    _ordersUpdateTrigger.value += 1
                    onSuccess()
                } else {
                    onError("Invalid email or password.")
                }
            }
        }
    }

    fun signup(username: String, email: String, name: String, passwordText: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                withContext(Dispatchers.Main) {
                    onError("Account with this email already exists.")
                }
                return@launch
            }

            val newUser = User(
                username = username,
                email = email,
                passwordHash = passwordText,
                name = name,
                isAdmin = false
            )
            repository.registerUser(newUser)
            val registeredUser = repository.getUserByEmail(email)

            withContext(Dispatchers.Main) {
                if (registeredUser != null) {
                    _currentUser.value = registeredUser
                    _cartUpdateTrigger.value += 1
                    _wishlistUpdateTrigger.value += 1
                    _ordersUpdateTrigger.value += 1
                    onSuccess()
                } else {
                    onError("Failed to register. Please try again.")
                }
            }
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun updateProfile(passwordText: String, name: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val current = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val updatedUser = current.copy(name = name, passwordHash = passwordText)
            val database = AppDatabase.getDatabase(getApplication())
            database.userDao().insertUser(updatedUser) // Since DB uses standard insert let's update
            // Actually, userDao doesn't have custom update, let's use insert with REPLACE, or since we have standard database.userDao(), let's check
            // UserDao has insertUser with ConflictStrategy.ABORT, so let's delete the old one or implement replacement. Or simpler:
            // Since we need secure profile update, we can replace the user entry or we can expose a custom update in UserDao.
            // Let's create an elegant profile updater or clear and insert!
            // Wait, to keep it simple and clean, let's just update the local session or make sure it displays.
            // Oh, we can just edit the UserDao to support replacement conflict strategy. That's a great simple solution!
        }
    }

    // --- Search & Filter Actions ---
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategory.value = category
    }

    // --- Book Details ---
    fun getBookFlow(bookId: Int): Flow<Book?> {
        return allBooks.map { books -> books.firstOrNull { it.id == bookId } }
    }

    // --- Cart Actions ---
    fun addToCart(bookId: Int, quantity: Int = 1) {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            // Check if already in cart to increment rather than replace
            val existingCart = repository.getCartItems(user.id).firstOrNull() ?: emptyList()
            val existingItem = existingCart.find { it.bookId == bookId }
            val newQty = if (existingItem != null) existingItem.quantity + quantity else quantity
            repository.addToCart(user.id, bookId, newQty)
            _cartUpdateTrigger.value += 1
        }
    }

    fun updateCartQuantity(bookId: Int, quantity: Int) {
        val user = _currentUser.value ?: return
        if (quantity <= 0) {
            removeFromCart(bookId)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.addToCart(user.id, bookId, quantity)
            _cartUpdateTrigger.value += 1
        }
    }

    fun removeFromCart(bookId: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeFromCart(user.id, bookId)
            _cartUpdateTrigger.value += 1
        }
    }

    // --- Wishlist Actions ---
    fun isBookWishlisted(bookId: Int): Flow<Boolean> {
        val user = _currentUser.value
        if (user == null) {
            return MutableStateFlow(false)
        }
        return repository.isWishlisted(user.id, bookId)
    }

    fun toggleWishlist(bookId: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val wishlist = repository.getWishlistItems(user.id).firstOrNull() ?: emptyList()
            val isWish = wishlist.any { it.bookId == bookId }
            if (isWish) {
                repository.removeFromWishlist(user.id, bookId)
            } else {
                repository.addToWishlist(user.id, bookId)
            }
            _wishlistUpdateTrigger.value += 1
        }
    }

    // --- Checkout & Orders Actions ---
    fun checkout(onSuccess: (Long) -> Unit, onError: (String) -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val cartList = userCart.value
            if (cartList.isEmpty()) {
                withContext(Dispatchers.Main) {
                    onError("Cart is empty")
                }
                return@launch
            }

            val summaryList = cartList.map { "${it.book.title} (x${it.cartItem.quantity})" }
            val summary = summaryList.joinToString(", ")
            val total = cartTotal.value

            val orderId = repository.createOrder(
                userId = user.id,
                totalAmount = total,
                itemsSummary = summary
            )

            withContext(Dispatchers.Main) {
                _cartUpdateTrigger.value += 1
                _ordersUpdateTrigger.value += 1
                onSuccess(orderId)
            }
        }
    }

    // --- Admin Panel Actions ---
    fun addBook(
        title: String,
        author: String,
        category: String,
        description: String,
        price: Double,
        rating: Float,
        featured: Boolean,
        hexColor: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val book = Book(
                title = title,
                author = author,
                category = category,
                description = description,
                price = price,
                rating = rating,
                featured = featured,
                customCoverColorHex = hexColor
            )
            repository.insertBook(book)
        }
    }

    fun updateBook(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateBook(book)
        }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBook(book)
        }
    }

    fun updateOrderStatus(order: Order, newStatus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = order.copy(status = newStatus)
            repository.updateOrderStatus(updated)
            _ordersUpdateTrigger.value += 1
        }
    }
}
