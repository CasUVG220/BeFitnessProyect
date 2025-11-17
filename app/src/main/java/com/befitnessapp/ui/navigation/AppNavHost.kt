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
        startDestination = Onboarding
    ) {
        composable<Onboarding> {
            OnboardingScreen(
                onTryDemo = {
                    nav.navigate(Home) {
                        popUpTo(Onboarding) { inclusive = true }
                    }
                },
                onLogin = { nav.navigate(Login) },
                onRegister = { nav.navigate(Register) }
            )
        }

        composable<Login> {
            LoginScreen(
                onLoggedIn = {
                    nav.navigate(Home) {
                        popUpTo(Onboarding) { inclusive = true }
                    }
                },
                onBack = { nav.popBackStack() }
            )
        }

        composable<Register> {
            RegisterScreen(
                onRegistered = {
                    nav.navigate(Home) {
                        popUpTo(Onboarding) { inclusive = true }
                    }
                },
                onBack = { nav.popBackStack() }
            )
        }

        composable<Home> {
            HomeScreen(
                goDashboard = { nav.navigate(Dashboard) },
                goLibrary = { nav.navigate(Library) },
                goMuscleMap = { nav.navigate(MuscleMap) },
                goLog = { nav.navigate(WorkoutLog) },
                goAddLog = { nav.navigate(AddLog()) },
                goRecommendations = { nav.navigate(Recommendations) },
                goRoutines = { nav.navigate(Routines) },
                goCalendar = { nav.navigate(Calendar) },
                goProfile = { nav.navigate(Profile) },
                goSettings = { nav.navigate(Settings) }
            )
        }

        composable<Dashboard> {
            DashboardScreen(onBack = { nav.popBackStack() })
        }

        composable<Library> {
            LibraryScreen(onBack = { nav.popBackStack() })
        }

        composable<MuscleMap> {
            MuscleMapScreen(onBack = { nav.popBackStack() })
        }

        composable<WorkoutLog> {
            WorkoutLogScreen(onBack = { nav.popBackStack() })
        }

        composable<Recommendations> {
            RecommendationsScreen(onBack = { nav.popBackStack() })
        }

        composable<Routines> {
            RoutinesScreen(onBack = { nav.popBackStack() })
        }

        composable<Calendar> {
            CalendarScreen(
                onBack = { nav.popBackStack() },
                onAddWorkoutForDate = { date: LocalDate ->
                    nav.navigate(AddLog(date = date.toString()))
                }
            )
        }

        composable<Profile> {
            ProfileScreen(
                onBack = { nav.popBackStack() },
                onLogout = { nav.navigate(Onboarding) }
            )
        }


        composable<Settings> {
            SettingsScreen(onBack = { nav.popBackStack() })
        }

        composable<AddLog> { entry ->
            val args = entry.toRoute<AddLog>()
            val initialDate = args.date?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()
            }
            AddWorkoutScreen(
                onBack = { nav.popBackStack() },
                initialDate = initialDate
            )
        }
    }
}
