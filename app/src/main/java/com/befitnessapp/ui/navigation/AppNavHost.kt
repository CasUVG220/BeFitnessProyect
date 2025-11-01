package com.befitnessapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.befitnessapp.ui.screens.addlog.AddWorkoutScreen
import com.befitnessapp.ui.screens.auth.LoginScreen
import com.befitnessapp.ui.screens.auth.RegisterScreen
import com.befitnessapp.ui.screens.calendar.CalendarScreen
import com.befitnessapp.ui.screens.dashboard.DashboardScreen
import com.befitnessapp.ui.screens.home.HomeScreen
import com.befitnessapp.ui.screens.library.LibraryScreen
import com.befitnessapp.ui.screens.log.WorkoutLogScreen
import com.befitnessapp.ui.screens.musclemap.MuscleMapScreen
import com.befitnessapp.ui.screens.onboarding.OnboardingScreen
import com.befitnessapp.ui.screens.profile.ProfileScreen
import com.befitnessapp.ui.screens.recommendations.RecommendationsScreen
import com.befitnessapp.ui.screens.routines.RoutinesScreen   // <- IMPORT CORRECTO
import com.befitnessapp.ui.screens.settings.SettingsScreen
import java.time.LocalDate

@Composable
fun AppNavHost() {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = Route.Onboarding.path
    ) {
        // Pre-login
        composable(Route.Onboarding.path) {
            OnboardingScreen(
                onTryDemo = {
                    nav.navigate(Route.Home.path) {
                        popUpTo(Route.Onboarding.path) { inclusive = true }
                    }
                },
                onLogin = { nav.navigate(Route.Login.path) },
                onRegister = { nav.navigate(Route.Register.path) }
            )
        }
        composable(Route.Login.path) {
            LoginScreen(
                onLoggedIn = {
                    nav.navigate(Route.Home.path) {
                        popUpTo(Route.Onboarding.path) { inclusive = true }
                    }
                },
                onBack = { nav.popBackStack() }
            )
        }
        composable(Route.Register.path) {
            RegisterScreen(
                onRegistered = {
                    nav.navigate(Route.Home.path) {
                        popUpTo(Route.Onboarding.path) { inclusive = true }
                    }
                },
                onBack = { nav.popBackStack() }
            )
        }

        // Home
        composable(Route.Home.path) {
            HomeScreen(
                goDashboard = { nav.navigate(Route.Dashboard.path) },
                goLibrary = { nav.navigate(Route.Library.path) },
                goMuscleMap = { nav.navigate(Route.MuscleMap.path) },
                goLog = { nav.navigate(Route.WorkoutLog.path) },
                goAddLog = { nav.navigate(Route.AddLog.path) },
                goRecommendations = { nav.navigate(Route.Recommendations.path) },
                goRoutines = { nav.navigate(Route.Routines.path) },
                goCalendar = { nav.navigate(Route.Calendar.path) },
                goProfile = { nav.navigate(Route.Profile.path) },
                goSettings = { nav.navigate(Route.Settings.path) }
            )
        }

        // Otras pantallas
        composable(Route.Dashboard.path) { DashboardScreen(onBack = { nav.popBackStack() }) }
        composable(Route.Library.path) { LibraryScreen(onBack = { nav.popBackStack() }) }
        composable(Route.MuscleMap.path) { MuscleMapScreen(onBack = { nav.popBackStack() }) }
        composable(Route.WorkoutLog.path) { WorkoutLogScreen(onBack = { nav.popBackStack() }) }
        composable(Route.Recommendations.path) { RecommendationsScreen(onBack = { nav.popBackStack() }) }
        composable(Route.Profile.path) { ProfileScreen(onBack = { nav.popBackStack() }) }
        composable(Route.Settings.path) { SettingsScreen(onBack = { nav.popBackStack() }) }

        // RUTINAS (usar la entrada pública)
        composable(Route.Routines.path) {
            RoutinesScreen(onBack = { nav.popBackStack() })
        }

        // Calendario → abrir AddLog con fecha
        composable(Route.Calendar.path) {
            CalendarScreen(
                onBack = { nav.popBackStack() },
                onAddWorkoutForDate = { date ->
                    nav.navigate(addLogWithDate(date))   // construye "addlog?date=YYYY-MM-DD"
                }
            )
        }

        // AddLog con fecha opcional ?date=YYYY-MM-DD
        composable(
            route = "${Route.AddLog.path}?date={date}",
            arguments = listOf(
                navArgument("date") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val initialDate = backStackEntry.arguments
                ?.getString("date")
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

            AddWorkoutScreen(
                onBack = { nav.popBackStack() },
                initialDate = initialDate
            )
        }
    }
}

/** Helper para construir la ruta con fecha ISO. */
private fun addLogWithDate(date: LocalDate): String =
    "${Route.AddLog.path}?date=$date"
