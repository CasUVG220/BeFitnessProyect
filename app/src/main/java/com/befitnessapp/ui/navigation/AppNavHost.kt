package com.befitnessapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
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
import com.befitnessapp.ui.screens.routines.RoutinesScreen
import com.befitnessapp.ui.screens.settings.SettingsScreen
import java.time.LocalDate

@Composable
fun AppNavHost() {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = AppRoute.Onboarding
    ) {
        // Onboarding / Auth

        composable<AppRoute.Onboarding> {
            OnboardingScreen(
                onTryDemo = {
                    nav.navigate(AppRoute.Home) {
                        popUpTo(AppRoute.Onboarding) { inclusive = true }
                    }
                },
                onLogin = { nav.navigate(AppRoute.Login) },
                onRegister = { nav.navigate(AppRoute.Register) }
            )
        }

        composable<AppRoute.Login> {
            LoginScreen(
                onLoggedIn = {
                    nav.navigate(AppRoute.Home) {
                        popUpTo(AppRoute.Onboarding) { inclusive = true }
                    }
                },
                onBack = { nav.popBackStack() }
            )
        }

        composable<AppRoute.Register> {
            RegisterScreen(
                onRegistered = {
                    nav.navigate(AppRoute.Home) {
                        popUpTo(AppRoute.Onboarding) { inclusive = true }
                    }
                },
                onBack = { nav.popBackStack() }
            )
        }

        // Home / Secciones
        composable<AppRoute.Home> {
            HomeScreen(
                goDashboard = { nav.navigate(AppRoute.Dashboard) },
                goLibrary = { nav.navigate(AppRoute.Library) },
                goMuscleMap = { nav.navigate(AppRoute.MuscleMap) },
                goLog = { nav.navigate(AppRoute.WorkoutLog) },
                goAddLog = { nav.navigate(AppRoute.AddLog()) },
                goRecommendations = { nav.navigate(AppRoute.Recommendations) },
                goRoutines = { nav.navigate(AppRoute.Routines) },
                goCalendar = { nav.navigate(AppRoute.Calendar) },
                goProfile = { nav.navigate(AppRoute.Profile) },
                goSettings = { nav.navigate(AppRoute.Settings) }
            )
        }

        composable<AppRoute.Dashboard> { DashboardScreen(onBack = { nav.popBackStack() }) }
        composable<AppRoute.Library> { LibraryScreen(onBack = { nav.popBackStack() }) }
        composable<AppRoute.MuscleMap> { MuscleMapScreen(onBack = { nav.popBackStack() }) }
        composable<AppRoute.WorkoutLog> { WorkoutLogScreen(onBack = { nav.popBackStack() }) }
        composable<AppRoute.Recommendations> { RecommendationsScreen(onBack = { nav.popBackStack() }) }
        composable<AppRoute.Profile> { ProfileScreen(onBack = { nav.popBackStack() }) }
        composable<AppRoute.Settings> { SettingsScreen(onBack = { nav.popBackStack() }) }

        //Rutinas

        composable<AppRoute.Routines> {
            RoutinesScreen(onBack = { nav.popBackStack() })
        }

        // Calendario  AddLog

        composable<AppRoute.Calendar> {
            CalendarScreen(
                onBack = { nav.popBackStack() },
                onAddWorkoutForDate = { date: LocalDate ->
                    // Pasamos la fecha en ISO-8601 como String en la ruta tipada
                    nav.navigate(AppRoute.AddLog(date = date.toString()))
                }
            )
        }

        // date

        composable<AppRoute.AddLog> { backStackEntry ->
            val args = backStackEntry.toRoute<AppRoute.AddLog>()
            val initialDate: LocalDate? = args.date?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()
            }

            AddWorkoutScreen(
                onBack = { nav.popBackStack() },
                initialDate = initialDate
            )
        }
    }
}
