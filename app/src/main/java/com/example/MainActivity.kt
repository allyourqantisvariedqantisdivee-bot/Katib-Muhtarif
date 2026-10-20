package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.model.Book
import com.example.data.repository.BookRepository
import com.example.ui.editor.EditorScreen
import com.example.ui.editor.EditorViewModel
import com.example.ui.library.LibraryScreen
import com.example.ui.library.LibraryViewModel
import com.example.ui.security.BookLockScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.splash.SplashScreen
import com.example.ui.theme.MyApplicationTheme

sealed interface ScreenState {
    data object Splash : ScreenState
    data object Library : ScreenState
    data class Lock(val book: Book) : ScreenState
    data class Editor(val book: Book) : ScreenState
    data object Settings : ScreenState
}

class MainActivity : ComponentActivity() {

    private val repository: BookRepository by lazy {
        val database = AppDatabase.getDatabase(applicationContext)
        BookRepository(database.bookDao(), database.settingsDao())
    }

    private val libraryViewModel: LibraryViewModel by viewModels {
        LibraryViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.util.RemoteConfigManager.initialize()

        setContent {
            val appSettings by repository.appSettings.collectAsState(initial = null)
            val currentTheme = appSettings?.themeMode ?: "LIGHT"

            MyApplicationTheme(themeMode = currentTheme) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        MainAppContent(
                            libraryViewModel = libraryViewModel,
                            repository = repository
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppContent(
    libraryViewModel: LibraryViewModel,
    repository: BookRepository,
    modifier: Modifier = Modifier
) {
    var screenState by remember { mutableStateOf<ScreenState>(ScreenState.Splash) }

    Crossfade(
        targetState = screenState,
        animationSpec = tween(durationMillis = 350),
        modifier = modifier.fillMaxSize(),
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            is ScreenState.Splash -> {
                SplashScreen(
                    onSplashFinished = {
                        screenState = ScreenState.Library
                    }
                )
            }
            is ScreenState.Library -> {
                LibraryScreen(
                    viewModel = libraryViewModel,
                    onBookClick = { book ->
                        if (book.isLocked) {
                            screenState = ScreenState.Lock(book)
                        } else {
                            screenState = ScreenState.Editor(book)
                        }
                    },
                    onOpenSettings = {
                        screenState = ScreenState.Settings
                    }
                )
            }
            is ScreenState.Settings -> {
                SettingsScreen(
                    repository = repository,
                    onNavigateBack = {
                        screenState = ScreenState.Library
                    }
                )
            }
            is ScreenState.Lock -> {
                BookLockScreen(
                    bookId = screen.book.id,
                    repository = repository,
                    onUnlocked = { unlockedBook ->
                        screenState = ScreenState.Editor(unlockedBook)
                    },
                    onNavigateBack = {
                        screenState = ScreenState.Library
                    },
                    onBookDeleted = {
                        screenState = ScreenState.Library
                    }
                )
            }
            is ScreenState.Editor -> {
                val editorViewModel: EditorViewModel = viewModel(
                    key = "editor_${screen.book.id}",
                    factory = EditorViewModel.Factory(screen.book, repository)
                )

                EditorScreen(
                    viewModel = editorViewModel,
                    onNavigateBack = {
                        screenState = ScreenState.Library
                    }
                )
            }
        }
    }
}
