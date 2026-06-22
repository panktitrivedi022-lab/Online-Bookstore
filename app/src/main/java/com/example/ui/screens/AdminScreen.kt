package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Book
import com.example.data.model.Order
import com.example.ui.viewmodel.BookStoreViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: BookStoreViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("catalog") } // catalog, orders, add_book

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Console", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Administrative Custom Navigation Pill Rows (Tabs)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Tab: catalog
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == "catalog") MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedTab = "catalog" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Catalog",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (selectedTab == "catalog") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tab: orders
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == "orders") MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedTab = "orders" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Fulfillment",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (selectedTab == "orders") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tab: add_book
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == "add_book") MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedTab = "add_book" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add Book",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (selectedTab == "add_book") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Display contents depending on active state
            when (selectedTab) {
                "catalog" -> AdminCatalogTab(viewModel = viewModel)
                "orders" -> AdminOrdersTab(viewModel = viewModel)
                "add_book" -> AdminAddBookTab(viewModel = viewModel, onBookAdded = { selectedTab = "catalog" })
            }
        }
    }
}

@Composable
fun AdminCatalogTab(viewModel: BookStoreViewModel) {
    val books by viewModel.allBooks.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_catalog_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (books.isEmpty()) {
            item {
                Text(
                    text = "No books found in the repository. Head over to additive tabs to submit custom entries.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            items(books, key = { it.id }) { book ->
                AdminBookItem(
                    book = book,
                    onDelete = { viewModel.deleteBook(book) }
                )
            }
        }
    }
}

@Composable
fun AdminOrdersTab(viewModel: BookStoreViewModel) {
    val orders by viewModel.adminAllOrders.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_orders_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (orders.isEmpty()) {
            item {
                Text(
                    text = "No shopping orders have been processed yet. Log out and place test transactions on store accounts.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            items(orders) { order ->
                AdminOrderRow(
                    order = order,
                    onStatusUpdate = { status -> viewModel.updateOrderStatus(order, status) }
                )
            }
        }
    }
}

@Composable
fun AdminBookItem(
    book: Book,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_book_item_${book.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle Category/Graphic icon
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(android.graphics.Color.parseColor(book.customCoverColorHex))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Book, contentDescription = null, tint = Color.White)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "By ${book.author} | ${book.category}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = String.format("$%.2f", book.price),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Row {
                IconButton(onClick = onDelete, modifier = Modifier.testTag("admin_delete_book_${book.id}")) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AdminOrderRow(
    order: Order,
    onStatusUpdate: (String) -> Unit
) {
    val formatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val dateString = formatter.format(Date(order.orderDate))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_order_row_${order.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Order Receipt #100${order.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Date: $dateString", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }

                Text(
                    text = order.status.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            when (order.status.lowercase()) {
                                "delivered" -> Color(0xFF2E7D32)
                                "processing" -> MaterialTheme.colorScheme.primary
                                "shipped" -> Color(0xFFE65100)
                                else -> Color(0xFFC62828)
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Summary Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = order.itemsSummary, fontSize = 12.sp, lineHeight = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Fulfillment controls:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(text = String.format("Paid: $%.2f", order.totalAmount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Colored controller buttons to instantly transition DB statuses
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { onStatusUpdate("Processing") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f).height(32.dp).testTag("set_processing_${order.id}")
                ) {
                    Text("Processing", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onStatusUpdate("Shipped") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCC80), contentColor = Color(0xFF3E2723)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f).height(32.dp).testTag("set_shipped_${order.id}")
                ) {
                    Text("Shipped", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onStatusUpdate("Delivered") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA5D6A7), contentColor = Color(0xFF1B5E20)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f).height(32.dp).testTag("set_delivered_${order.id}")
                ) {
                    Text("Delivered", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminAddBookTab(
    viewModel: BookStoreViewModel,
    onBookAdded: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var ratingText by remember { mutableStateOf("") }
    var featured by remember { mutableStateOf(false) }
    var customCoverColor by remember { mutableStateOf("#1a3c40") }

    var errorMsg by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("admin_add_book_tab")
    ) {
        Text(
            text = "Catalogue Product Submission",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (errorMsg.isNotEmpty()) {
            Text(
                text = errorMsg,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            )
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it; errorMsg = "" },
            label = { Text("Book Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).testTag("field_add_title")
        )

        OutlinedTextField(
            value = author,
            onValueChange = { author = it; errorMsg = "" },
            label = { Text("Author / Writer") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).testTag("field_add_author")
        )

        OutlinedTextField(
            value = category,
            onValueChange = { category = it; errorMsg = "" },
            label = { Text("Category (e.g. Science & Tech, Biography, Fiction)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).testTag("field_add_category")
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it; errorMsg = "" },
            label = { Text("Description Syndicate Synopsis") },
            modifier = Modifier.fillMaxWidth().height(100.dp).padding(bottom = 10.dp).testTag("field_add_description")
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it; errorMsg = "" },
                label = { Text("Price ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("field_add_price")
            )

            OutlinedTextField(
                value = ratingText,
                onValueChange = { ratingText = it; errorMsg = "" },
                label = { Text("Rating (1-5)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("field_add_rating")
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = customCoverColor,
                onValueChange = { customCoverColor = it; errorMsg = "" },
                label = { Text("Cover hex color code") },
                placeholder = { Text("#1C2F59") },
                singleLine = true,
                modifier = Modifier.weight(1.5f).padding(bottom = 10.dp).testTag("field_add_color")
            )

            Row(
                modifier = Modifier.weight(1f).padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = featured,
                    onCheckedChange = { featured = it },
                    modifier = Modifier.testTag("field_add_featured")
                )
                Text("Featured", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val price = priceText.toDoubleOrNull()
                val rating = ratingText.toFloatOrNull()

                if (title.isBlank() || author.isBlank() || category.isBlank() || description.isBlank()) {
                    errorMsg = "Please complete all field requirements."
                } else if (price == null || price <= 0.0) {
                    errorMsg = "Please supply a valid numeric price."
                } else if (rating == null || rating < 1.0 || rating > 5.0) {
                    errorMsg = "Please supply a rating between 1.0 and 5.0."
                } else if (!customCoverColor.startsWith("#") || customCoverColor.length < 4) {
                    errorMsg = "Please supply a valid cover hex color starting with #."
                } else {
                    viewModel.addBook(
                        title = title.trim(),
                        author = author.trim(),
                        category = category.trim(),
                        description = description.trim(),
                        price = price,
                        rating = rating,
                        featured = featured,
                        hexColor = customCoverColor.trim()
                    )
                    onBookAdded()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("admin_submit_book_btn"),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Publish Book on catalog", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}
