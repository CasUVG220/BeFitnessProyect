package com.befitnessapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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

@Composable
fun AppNavHost() {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = Route.Onboarding.path // luego cambiaremos según sesión/demo
    ) {
        // Pre-login
        composable(Route.Onboarding.path) {
            OnboardingScreen(
                onTryDemo = { nav.navigate(Route.Home.path) { popUpTo(Route.Onboarding.path) { inclusive = true } } },
                onLogin = { nav.navigate(Route.Login.path) },
                onRegister = { nav.navigate(Route.Register.path) }
            )
        }
        composable(Route.Login.path) { LoginScreen(onLoggedIn = {
            nav.navigate(Route.Home.path) { popUpTo(Route.Onboarding.path) { inclusive = true } }
        }, onBack = { nav.popBackStack() }) }
        composable(Route.Register.path) { RegisterScreen(onRegistered = {
            nav.navigate(Route.Home.path) { popUpTo(Route.Onboarding.path) { inclusive = true } }
        }, onBack = { nav.popBackStack() }) }

        // Post-login
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
        composable(Route.Dashboard.path) { DashboardScreen(onBack = { nav.popBackStack() }) }
        composable(Route.Library.path) { LibraryScreen(onBack = { nav.popBackStack() }) }
        composable(Route.MuscleMap.path) { MuscleMapScreen(onBack = { nav.popBackStack() }) }
        composable(Route.WorkoutLog.path) { WorkoutLogScreen(onBack = { nav.popBackStack() }) }
        composable(Route.AddLog.path) { AddWorkoutScreen(onBack = { nav.popBackStack() }) }
        composable(Route.Recommendations.path) { RecommendationsScreen(onBack = { nav.popBackStack() }) }
        composable(Route.Routines.path) { RoutinesScreen(onBack = { nav.popBackStack() }) }
        composable(Route.Calendar.path) { CalendarScreen(onBack = { nav.popBackStack() }) }
        composable(Route.Profile.path) { ProfileScreen(onBack = { nav.popBackStack() }) }
        composable(Route.Settings.path) { SettingsScreen(onBack = { nav.popBackStack() }) }
    }
}
