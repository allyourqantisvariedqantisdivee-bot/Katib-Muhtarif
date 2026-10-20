package com.example.ui.editor

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Book
import com.example.data.model.BookPage
import com.example.data.repository.BookRepository
import com.example.util.BookExporter
import com.example.util.ExportFormat
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditorViewModel(
    initialBook: Book,
    private val repository: BookRepository
) : ViewModel() {

    private val _book = MutableStateFlow(initialBook)
    val book: StateFlow<Book> = _book.asStateFlow()

    val pages = repository.getPagesForBook(initialBook.id).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            if (pages.value.isEmpty()) {
                repository.insertPage(BookPage(bookId = initialBook.id, pageNumber = 1, content = ""))
            }
        }
    }

    fun updatePageContent(page: BookPage, newContent: String) {
        viewModelScope.launch {
            repository.updatePage(page.copy(content = newContent))
        }
    }

    fun addPage() {
        viewModelScope.launch {
            val nextNumber = (pages.value.maxOfOrNull { it.pageNumber } ?: 0) + 1
            repository.insertPage(BookPage(bookId = _book.value.id, pageNumber = nextNumber, content = ""))
        }
    }

    fun exportBook(context: Context, format: ExportFormat) {
        BookExporter.exportAndShare(context, _book.value, pages.value, format)
    }

    class Factory(private val book: Book, private val repository: BookRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EditorViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EditorViewModel(book, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
