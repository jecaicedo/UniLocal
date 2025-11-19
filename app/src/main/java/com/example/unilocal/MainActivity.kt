package com.example.unilocal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.unilocal.ui.navigation.Screen
import com.example.unilocal.ui.screens.*
import com.example.unilocal.ui.theme.UniLocalTheme
import com.example.unilocal.viewmodel.AuthViewModel
import com.example.unilocal.viewmodel.LugarViewModel
import com.example.unilocal.viewmodel.ModeradorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UniLocalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UniLocalApp()
                }
            }
        }
    }
}

@Composable
fun UniLocalApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val lugarViewModel: LugarViewModel = viewModel()
    val moderadorViewModel: ModeradorViewModel = viewModel()

    val currentUser by authViewModel.currentUser.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (currentUser != null) {
            if (currentUser?.rol == "moderador") Screen.ModeradorHome.route else Screen.Home.route
        } else {
            Screen.Login.route
        }
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToModeradorHome = {
                    navController.navigate(Screen.ModeradorHome.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                lugarViewModel = lugarViewModel,
                authViewModel = authViewModel,
                onNavigateToDetalle = { lugarId ->
                    navController.navigate(Screen.DetalleLugar.createRoute(lugarId))
                },
                onNavigateToCrear = { navController.navigate(Screen.CrearLugar.route) },
                onNavigateToPerfil = { navController.navigate(Screen.Perfil.route) },
                onNavigateToMisLugares = { navController.navigate(Screen.MisLugares.route) },
                onNavigateToFavoritos = { navController.navigate(Screen.Favoritos.route) },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.DetalleLugar.route,
            arguments = listOf(navArgument("lugarId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lugarId = backStackEntry.arguments?.getString("lugarId") ?: return@composable
            DetalleLugarScreen(
                lugarId = lugarId,
                lugarViewModel = lugarViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CrearLugar.route) {
            CrearLugarScreen(
                lugarViewModel = lugarViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Perfil.route) {
            PerfilScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MisLugares.route) {
            MisLugaresScreen(
                lugarViewModel = lugarViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Favoritos.route) {
            FavoritosScreen(
                lugarViewModel = lugarViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetalle = { lugarId ->
                    navController.navigate(Screen.DetalleLugar.createRoute(lugarId))
                }
            )
        }

        composable(Screen.ModeradorHome.route) {
            ModeradorHomeScreen(
                moderadorViewModel = moderadorViewModel,
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}