package com.campusverse.app.ui.screens.student.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusverse.app.data.model.MarketplaceProduct
import com.campusverse.app.data.repository.NetworkStudentRepository
import com.campusverse.app.domain.student.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MarketplaceListUiState {
    data object Loading : MarketplaceListUiState
    data class Success(
        val items: List<MarketplaceProduct>,
        val selectedCategory: String = "ALL",
        val searchQuery: String = ""
    ) : MarketplaceListUiState
    data class Error(val message: String) : MarketplaceListUiState
}

sealed interface ProductDetailUiState {
    data object Loading : ProductDetailUiState
    data class Success(
        val product: MarketplaceProduct,
        val actionToast: String? = null
    ) : ProductDetailUiState
    data class Error(val message: String) : ProductDetailUiState
}

class MarketplaceViewModel(
    private val repository: StudentRepository = NetworkStudentRepository.instance
) : ViewModel() {

    private val _listState = MutableStateFlow<MarketplaceListUiState>(MarketplaceListUiState.Loading)
    val listState: StateFlow<MarketplaceListUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val detailState: StateFlow<ProductDetailUiState> = _detailState.asStateFlow()

    init {
        loadMarketplace()
    }

    fun loadMarketplace(category: String = "ALL", search: String? = null, showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading && _listState.value !is MarketplaceListUiState.Success) {
                _listState.value = MarketplaceListUiState.Loading
            }
            try {
                val catParam = if (category == "ALL") null else category
                val result = repository.getMarketplaceListings(search = search, category = catParam)
                val list = result.getOrDefault(emptyList())
                _listState.value = MarketplaceListUiState.Success(
                    items = list,
                    selectedCategory = category,
                    searchQuery = search ?: ""
                )
            } catch (e: Exception) {
                if (_listState.value !is MarketplaceListUiState.Success) {
                    _listState.value = MarketplaceListUiState.Error(e.message ?: "Failed to load marketplace items.")
                }
            }
        }
    }

    fun loadProductDetail(productId: String) {
        viewModelScope.launch {
            _detailState.value = ProductDetailUiState.Loading
            try {
                val result = repository.getMarketplaceProductById(productId)
                val item = result.getOrNull()
                if (item != null) {
                    _detailState.value = ProductDetailUiState.Success(product = item)
                } else {
                    _detailState.value = ProductDetailUiState.Error("Product not found.")
                }
            } catch (e: Exception) {
                _detailState.value = ProductDetailUiState.Error(e.message ?: "Failed to load product details.")
            }
        }
    }

    fun createListing(
        title: String,
        description: String,
        price: Double,
        category: String,
        condition: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.createMarketplaceListing(title, description, price, category, condition)
                loadMarketplace()
                onSuccess()
            } catch (_: Exception) {}
        }
    }

    fun markAsSold(productId: String) {
        viewModelScope.launch {
            repository.updateMarketplaceListing(productId, null, "SOLD")
            loadProductDetail(productId)
            loadMarketplace()
        }
    }

    fun deleteListing(productId: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteMarketplaceListing(productId)
            loadMarketplace()
            onDeleted()
        }
    }

    fun reportProduct(productId: String, reason: String) {
        viewModelScope.launch {
            repository.reportContent("MARKETPLACE_ITEM", productId, reason)
            val curr = _detailState.value as? ProductDetailUiState.Success
            if (curr != null) {
                _detailState.value = curr.copy(actionToast = "Item reported to campus security.")
            }
        }
    }

    fun setCategory(category: String) {
        val currSearch = (_listState.value as? MarketplaceListUiState.Success)?.searchQuery
        loadMarketplace(category = category, search = currSearch)
    }

    fun search(query: String) {
        val currState = _listState.value as? MarketplaceListUiState.Success
        if (currState != null) {
            _listState.value = currState.copy(searchQuery = query)
        }
        val currCat = currState?.selectedCategory ?: "ALL"
        loadMarketplace(category = currCat, search = query.ifBlank { null }, showLoading = false)
    }

    fun clearToast() {
        val curr = _detailState.value as? ProductDetailUiState.Success ?: return
        _detailState.value = curr.copy(actionToast = null)
    }
}
