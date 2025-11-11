package es.hgg.sharexp.view

import kotlinx.serialization.Serializable


sealed interface Route {
    @Serializable object LandingPage : Route

    @Serializable object LoginPage : Route
    @Serializable object RegisterPage : Route

    @Serializable object GroupsPage : Route
}