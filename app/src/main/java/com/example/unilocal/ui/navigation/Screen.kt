package com.example.unilocal.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object DetalleLugar : Screen("detalle/{lugarId}") {
        fun createRoute(lugarId: String) = "detalle/$lugarId"
    }
    object CrearLugar : Screen("crear_lugar")
    object Perfil : Screen("perfil")
    object MisLugares : Screen("mis_lugares")
    object Favoritos : Screen("favoritos")
    object ModeradorHome : Screen("moderador_home")
}