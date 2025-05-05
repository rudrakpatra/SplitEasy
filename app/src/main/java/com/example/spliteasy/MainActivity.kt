package com.example.spliteasy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.spliteasy.ai.AIScreen
import com.example.spliteasy.ai.AIViewModel
import com.example.spliteasy.auth.AuthManager
import com.example.spliteasy.auth.LoginScreen
import com.example.spliteasy.auth.LogoutDialog
import com.example.spliteasy.ui.theme.SplitEasyTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthManager.init(this@MainActivity)
        // Enable edge-to-edge rendering
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            // Set up system UI controller for full screen
            FullScreenSetup()

            SplitEasyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val mainViewModel: MainViewModel = viewModel()
                    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

                    val navController = rememberNavController()

                    // Set up navigation
                    AppNavigation(
                        navController = navController,
                        mainViewModel = mainViewModel,
                        uiState = uiState
                    )

                    // Show logout dialog if requested
                    if (uiState.showLogoutDialog) {
                        LogoutDialog(
                            onDismiss = { mainViewModel.hideLogoutDialog() },
                            onConfirm = {
                                mainViewModel.signOut()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    uiState: MainUiState
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            // Home is just a router - it checks auth and redirects
            if (uiState.currentUser != null) {
                // If user is logged in, navigate to AI screen
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.AI.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            } else {
                // If user is not logged in, navigate to Login screen
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            }
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onSignInSuccess = { user ->
                    mainViewModel.updateUser(user)
                    navController.navigate(Screen.AI.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AI.route) {
            // Back press is handled by the viewModel to show logout dialog
            BackHandler {
                mainViewModel.showLogoutDialog()
            }
            val aiViewModel: AIViewModel = remember{AIViewModel()}
            AIScreen(
                viewModel = aiViewModel,
                onBackClick = { mainViewModel.showLogoutDialog() }
            )
        }
    }
}

@Composable
fun FullScreenSetup() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()

    DisposableEffect(systemUiController, useDarkIcons) {
        // Make status bar visible but transparent
        systemUiController.isStatusBarVisible = true

        // Set navigation bar color to transparent
        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )

        // Set status bar color to transparent
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )

        // Make the system bars draw over your content
        systemUiController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        onDispose {}
    }
}

// Define screens for navigation
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Login : Screen("login")
    data object AI : Screen("ai")
}