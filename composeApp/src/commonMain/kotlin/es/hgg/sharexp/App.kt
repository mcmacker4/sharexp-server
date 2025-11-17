package es.hgg.sharexp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import es.hgg.sharexp.ui.theme.SharexpTheme
import es.hgg.sharexp.view.Route
import es.hgg.sharexp.view.groups.GroupsPage
import es.hgg.sharexp.view.landing.LandingPage
import es.hgg.sharexp.view.login.LoginPage
import es.hgg.sharexp.view.register.RegisterPage
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    SharexpTheme {
        val controller = rememberNavController()

        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            NavHost(
                navController = controller,
                startDestination = Route.LandingPage,
            ) {
                composable<Route.LandingPage> {
                    LandingPage(
                        onLoggedIn = {
                            controller.navigate(Route.GroupsPage) {
                                popUpTo<Route.LandingPage> { inclusive = true }
                            }
                        },
                        onLoggedOut = {
                            controller.navigate(Route.LoginPage) {
                                popUpTo<Route.LandingPage> { inclusive = true }
                            }
                        },
                    )
                }

                composable<Route.LoginPage> {
                    LoginPage(
                        onLoggedIn = {
                            controller.navigate(Route.GroupsPage) {
                                popUpTo<Route.LoginPage> { inclusive = true }
                            }
                        },
                        onRegisterRequest = { controller.navigate(Route.RegisterPage) }
                    )
                }

                composable<Route.RegisterPage> {
                    RegisterPage(
                        onRegistered = { controller.navigateUp() },
                        onCancel = { controller.navigateUp() },
                    )
                }

                composable<Route.GroupsPage> {
                    GroupsPage(
                        onNewGroupRequest = {}
                    )
                }
            }
        }
    }
}