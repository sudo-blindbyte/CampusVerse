package com.campusverse.app.ui.screens.student.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campusverse.app.data.model.MarketplaceProduct
import com.campusverse.app.ui.components.CampusVerseButton
import com.campusverse.app.ui.components.CampusVerseTopBar
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit = {},
    onNavigateToCreateListing: () -> Unit = {},
    onNavigateToRoute: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: MarketplaceViewModel = viewModel()
) {
    val listState by viewModel.listState.collectAsStateWithLifecycle()
    var selectedFilterChip by remember { mutableStateOf("All") }
    var showCreateListingSheet by remember { mutableStateOf(false) }
    var selectedProductForDetail by remember { mutableStateOf<MarketplaceProduct?>(null) }
    var newListingTitle by remember { mutableStateOf("") }
    var newListingDescription by remember { mutableStateOf("") }
    var newListingPrice by remember { mutableStateOf("") }
    var newListingCategory by remember { mutableStateOf("Books") }
    var newListingCondition by remember { mutableStateOf("GOOD") }

    val filterOptions = listOf("All", "Books", "Electronics", "Study Materials", "Lab Equipment", "Furniture")

    Scaffold(
        topBar = {
            CampusVerseTopBar(
                title = "Marketplace",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        bottomBar = {
            com.campusverse.app.ui.screens.student.components.StudentBottomBar(
                currentRoute = com.campusverse.app.navigation.Screen.Marketplace.route,
                onNavigate = { route -> onNavigateToRoute?.invoke(route) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (onNavigateToCreateListing != {}) {
                        onNavigateToCreateListing()
                    }
                    showCreateListingSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Sell Item",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        when (val state = listState) {
            is MarketplaceListUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is MarketplaceListUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        CampusVerseButton(text = "Retry", onClick = { viewModel.loadMarketplace() })
                    }
                }
            }
            is MarketplaceListUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Search Bar
                    item {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.search(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search textbooks, electronics, calculators...") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // 2. Filter Chips
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filterOptions.forEach { filter ->
                                FilterChip(
                                    selected = selectedFilterChip == filter,
                                    onClick = {
                                        selectedFilterChip = filter
                                        if (filter == "All") viewModel.setCategory("ALL")
                                        else viewModel.setCategory(filter.uppercase().replace(" ", "_"))
                                    },
                                    label = { Text(filter) },
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }
                        }
                    }

                    // 3. Hero Marketplace Banner (Stitch Spec)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                                                MaterialTheme.colorScheme.surface
                                            )
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Student Marketplace",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Buy and sell used study materials, electronics, and campus essentials.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            if (onNavigateToCreateListing != {}) {
                                                onNavigateToCreateListing()
                                            }
                                            showCreateListingSheet = true
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                    ) {
                                        Text("Sell Item")
                                    }
                                }
                            }
                        }
                    }

                    // 4. Section Title
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recommended For You (${state.items.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // 5. Products List
                    if (state.items.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = "No marketplace listings found. Sell your textbooks or devices!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        }
                    } else {
                        items(state.items) { product ->
                            MarketplaceCardItem(
                                product = product,
                                onClick = {
                                    if (onNavigateToProductDetail != {}) {
                                        onNavigateToProductDetail(product.id)
                                    }
                                    selectedProductForDetail = product
                                }
                            )
                        }
                    }
                }
            }
        }

        // Create Listing Sheet
        if (showCreateListingSheet) {
            androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = { showCreateListingSheet = false },
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Create Listing",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = newListingTitle,
                        onValueChange = { newListingTitle = it },
                        label = { Text("Item Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newListingDescription,
                        onValueChange = { newListingDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newListingPrice,
                        onValueChange = { newListingPrice = it },
                        label = { Text("Price (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text("Category", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Books", "Electronics", "Study Materials", "Furniture", "Other").forEach { cat ->
                            FilterChip(
                                selected = newListingCategory == cat,
                                onClick = { newListingCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                    Text("Condition", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("NEW", "LIKE_NEW", "GOOD", "FAIR").forEach { cond ->
                            FilterChip(
                                selected = newListingCondition == cond,
                                onClick = { newListingCondition = cond },
                                label = { Text(cond) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    CampusVerseButton(
                        text = "Publish Listing",
                        onClick = {
                            val priceVal = newListingPrice.toDoubleOrNull() ?: 0.0
                            if (newListingTitle.isNotBlank() && priceVal > 0.0) {
                                viewModel.createListing(
                                    title = newListingTitle.trim(),
                                    description = newListingDescription.trim(),
                                    price = priceVal,
                                    category = newListingCategory,
                                    condition = newListingCondition,
                                    onSuccess = {
                                        showCreateListingSheet = false
                                        newListingTitle = ""
                                        newListingDescription = ""
                                        newListingPrice = ""
                                    }
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Product Detail Sheet
        if (selectedProductForDetail != null) {
            val prod = selectedProductForDetail!!
            androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = { selectedProductForDetail = null },
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = prod.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "₹${prod.price.toInt()}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Condition: ${prod.condition} • Category: ${prod.category}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = prod.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (prod.isOwnedByCurrentUser) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CampusVerseButton(
                                text = if (prod.status == "SOLD") "Item Sold" else "Mark as Sold",
                                enabled = prod.status != "SOLD",
                                onClick = {
                                    viewModel.markAsSold(prod.id)
                                    selectedProductForDetail = null
                                },
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    viewModel.deleteListing(prod.id) {
                                        selectedProductForDetail = null
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Delete Listing", color = MaterialTheme.colorScheme.onError)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CampusVerseButton(
                                text = "Chat Seller",
                                onClick = {
                                    selectedProductForDetail = null
                                },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedButton(
                                onClick = {
                                    viewModel.reportProduct(prod.id, "Inappropriate listing")
                                    selectedProductForDetail = null
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Report")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun MarketplaceCardItem(
    product: MarketplaceProduct,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Student Verified",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = product.condition,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "₹${product.price.toInt()}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${product.sellerName} • Campus Hub",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (product.isOwnedByCurrentUser) {
                    OutlinedButton(
                        onClick = onClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Your Listing")
                    }
                } else {
                    OutlinedButton(
                        onClick = onClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Chat Seller")
                    }
                }
                CampusVerseButton(
                    text = "View Details",
                    onClick = onClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
