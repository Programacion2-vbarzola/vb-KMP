package com.vbarzola.shared.catalog

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class CatalogViewModel(
    private val catalogRepository: CatalogRepository,
    scope: CoroutineScope
) {
    val categories: List<CategoryItem> = catalogRepository.getCategories()

    private val _query = MutableStateFlow("")
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)

    val query: StateFlow<String> = _query
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId

    val professionals: StateFlow<List<ProfessionalItem>> = combine(_query, _selectedCategoryId) { query, categoryId ->
        catalogRepository.searchProfessionals(enabledOnly = true)
            .filter { p ->
                val matchesName = query.isBlank() ||
                    p.firstName.contains(query, ignoreCase = true) ||
                    p.lastName.contains(query, ignoreCase = true)
                val matchesCategory = categoryId == null || p.categoryId == categoryId
                matchesName && matchesCategory
            }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun setQuery(q: String) { _query.value = q }
    fun setCategory(id: Long?) { _selectedCategoryId.value = id }
}
