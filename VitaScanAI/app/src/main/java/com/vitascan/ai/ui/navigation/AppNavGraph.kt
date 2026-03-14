package com.vitascan.ai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vitascan.ai.ui.analytics.AnalyticsScreen
import com.vitascan.ai.ui.dashboard.DashboardScreen
import com.vitascan.ai.ui.login.LoginScreen
import com.vitascan.ai.ui.login.LoginViewModel
import com.vitascan.ai.ui.recommendations.RecommendationsScreen
import com.vitascan.ai.ui.reportview.ReportViewScreen
import com.vitascan.ai.ui.signup.SignupScreen
import com.vitascan.ai.ui.upload.UploadScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.LOGIN) {
            val vm: LoginViewModel = hiltViewModel()
            LaunchedEffect(vm) {
                if (vm.isAlreadyLoggedIn()) {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            }
            LoginScreen(
                viewModel   = vm,
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToSignup = { navController.navigate(Routes.SIGNUP) }
            )
        }

        composable(Routes.SIGNUP) {
            SignupScreen(
                viewModel          = hiltViewModel(),
                onSignupSuccess    = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin  = { navController.popBackStack() }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                viewModel          = hiltViewModel(),
                onNavigateToUpload = { navController.navigate(Routes.UPLOAD) },
                onNavigateToReport = { id -> navController.navigate(Routes.reportView(id)) },
                onNavigateToAnalytics = { navController.navigate(Routes.ANALYTICS) },
                onLogout           = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.UPLOAD) {
            UploadScreen(
                viewModel = hiltViewModel(),
                onDone    = { reportId ->
                    navController.navigate(Routes.reportView(reportId)) {
                        popUpTo(Routes.UPLOAD) { inclusive = true }
                    }
                },
                onBack    = { navController.popBackStack() }
            )
        }

        composable(
            Routes.REPORT_VIEW,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) {
            val reportId = it.arguments?.getString("reportId") ?: return@composable
            ReportViewScreen(
                reportId            = reportId,
                viewModel           = hiltViewModel(),
                onNavigateToRecs    = { navController.navigate(Routes.recommendations(reportId)) },
                onNavigateToAnalytics = { navController.navigate(Routes.ANALYTICS) },
                onBack              = { navController.popBackStack() }
            )
        }

        composable(Routes.ANALYTICS) {
            AnalyticsScreen(
                viewModel = hiltViewModel(),
                onBack    = { navController.popBackStack() }
            )
        }

        composable(
            Routes.RECOMMENDATIONS,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) {
            val reportId = it.arguments?.getString("reportId") ?: return@composable
            RecommendationsScreen(
                reportId  = reportId,
                viewModel = hiltViewModel(),
                onBack    = { navController.popBackStack() }
            )
        }
    }
}
