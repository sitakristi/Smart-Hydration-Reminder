package com.rensy.smarthydration.navigation

import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rensy.smarthydration.controller.HydrationController
import com.rensy.smarthydration.controller.ProgressController
import com.rensy.smarthydration.controller.UserController
import com.rensy.smarthydration.database.AppDatabase
import com.rensy.smarthydration.database.repository.DailyProgressRepository
import com.rensy.smarthydration.database.repository.HydrationLogRepository
import com.rensy.smarthydration.database.repository.UserRepository
import com.rensy.smarthydration.ui.screen.DashboardScreen
import com.rensy.smarthydration.ui.view.HydrationScreen
import com.rensy.smarthydration.ui.view.ProfileScreen
import com.rensy.smarthydration.ui.view.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val PROFILE = "profile"
    const val DASHBOARD = "dashboard"
    const val HYDRATION = "hydration"
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val activity = context as? Activity
    val navController: NavHostController = rememberNavController()

    val db = remember { AppDatabase.getInstance(context) }

    val userRepository = remember { UserRepository(db.userDao()) }
    val hydrationLogRepository = remember { HydrationLogRepository(db.hydrationLogDao()) }
    val dailyProgressRepository = remember { DailyProgressRepository(db.dailyProgressDao()) }

    val userController = remember { UserController(userRepository) }
    val progressController = remember { ProgressController(dailyProgressRepository, hydrationLogRepository) }
    val hydrationController = remember { HydrationController(hydrationLogRepository, dailyProgressRepository) }

    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        // Show splash for 2 seconds, then navigate to appropriate screen
        kotlinx.coroutines.delay(2000)
        startDestination = if (userController.hasUser()) Routes.DASHBOARD else Routes.PROFILE
    }

    if (startDestination == null) {
        // Show splash screen while loading
        SplashScreen(onGetStarted = {})
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {
        composable(Routes.PROFILE) {
            ProfileScreen(
                userController = userController,
                onProfileSaved = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.PROFILE) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                userController = userController,
                progressController = progressController,
                onNavigateToHydration = {
                    navController.navigate(Routes.HYDRATION)
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.PROFILE)
                },
                onBack = {
                    // Close the app when back is pressed on the root screen
                    activity?.finish()
                }
            )
        }

        composable(Routes.HYDRATION) {
            HydrationScreen(
                userController = userController,
                hydrationController = hydrationController,
                progressController = progressController,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}