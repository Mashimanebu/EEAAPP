package com.pay.eeaapp.app.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pay.eeaapp.ui.admin.AdminDashboardScreen
import com.pay.eeaapp.ui.admin.AdminProjectReviewScreen
import com.pay.eeaapp.ui.admin.AnalyticsScreen
import com.pay.eeaapp.ui.admin.MapScreen
import com.pay.eeaapp.ui.auth.AuthState
import com.pay.eeaapp.ui.auth.LoginScreen
import com.pay.eeaapp.ui.auth.LoginViewModel
import com.pay.eeaapp.ui.auth.SignUpScreen
import com.pay.eeaapp.ui.detail.ProjectDetailScreen
import com.pay.eeaapp.ui.proponent.ApplyProjectScreen
import com.pay.eeaapp.ui.proponent.ProponentDashboardScreen
import com.pay.eeaapp.ui.welcome.WelcomeScreen


@Composable
fun EeaNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    val loginViewModel: LoginViewModel = viewModel()
    NavHost(
        navController   = navController,
        startDestination = startDestination,
        modifier        = modifier
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess  = { user ->
                    val dest = if (user.role.name == "ADMIN") Routes.ADMIN_DASHBOARD
                    else Routes.PROPONENT_DASHBOARD
                    navController.navigate(dest) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoToSignUp    = { navController.navigate(Routes.SIGN_UP) }
            )
        }
        composable(Routes.SIGN_UP) {
            SignUpScreen(
                onSignUpSuccess = { navController.navigate(Routes.PROPONENT_DASHBOARD) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }},
                onGoToLogin     = { navController.popBackStack() }
            )
        }

        composable(Routes.PROPONENT_DASHBOARD) {
            ProponentDashboardScreen(
                onApplyClick        = { navController.navigate(Routes.APPLY_PROJECT) },
                onProjectClick      = { navController.navigate(Routes.projectDetail(it)) },
                onSignOut           = { navController.navigate(Routes.LOGIN) { popUpTo(0) } }
            )
        }
        composable(Routes.APPLY_PROJECT) {
            val authState by loginViewModel.state.collectAsState()
            val loggedInUser = (authState as? AuthState.Success)?.user

            val initials = loggedInUser?.fullName
                ?.split(" ")
                ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
                ?.take(2)
                ?.joinToString("") ?: ""

            ApplyProjectScreen(
                onSubmitted         = { navController.popBackStack() },
                onBack              = { navController.popBackStack() },
                currentUserName     = loggedInUser?.fullName ?: "",
                currentUserInitials = initials
            )
        }
        composable(
            route     = Routes.PROJECT_DETAIL,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStack ->
            ProjectDetailScreen(
                projectId = backStack.arguments!!.getString("projectId")!!,
                onBack    = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(
                onProjectClick  = { navController.navigate(Routes.adminReview(it)) },
                onAnalyticsClick = { navController.navigate(Routes.ANALYTICS) },
                onMapClick      = { navController.navigate(Routes.MAP) },
                onSignOut       = { navController.navigate(Routes.LOGIN) { popUpTo(0) } }
            )
        }
        composable(
            route     = Routes.ADMIN_REVIEW,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStack ->
            AdminProjectReviewScreen(
                projectId = backStack.arguments!!.getString("projectId")!!,
                onBack    = { navController.popBackStack() }
            )
        }
        composable(Routes.ANALYTICS) {
            AnalyticsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.MAP) {
            MapScreen(
                onProjectClick = { navController.navigate(Routes.adminReview(it)) },
                onBack         = { navController.popBackStack() }
            )
        }
    }
}

