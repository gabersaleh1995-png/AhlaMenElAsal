package com.gaber.ahlamenelasal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gaber.ahlamenelasal.data.model.LibraryItem
import com.gaber.ahlamenelasal.data.repository.LibraryRepository
import com.gaber.ahlamenelasal.data.repository.LibraryRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LibraryUiState(
    val items: List<LibraryItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class LibraryViewModel(
    private val repository: LibraryRepository = LibraryRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        fetchPdfs()
    }

    private fun fetchPdfs() {
        viewModelScope.launch {
            repository.getPdfs().collect { items ->
                _uiState.value = LibraryUiState(items = items, isLoading = false)
            }
        }
    }
}
