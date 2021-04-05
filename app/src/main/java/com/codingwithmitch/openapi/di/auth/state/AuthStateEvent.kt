package com.codingwithmitch.openapi.di.auth.state

sealed class AuthStateEvent {

    data class LoginAttemptEvent(
        val email: String,
        val password: String
    ): AuthStateEvent()

    data class RegisterAttemptEvent(
        val email: String,
        val username: String,
        val password: String,
        val confirm_password: String
    ): AuthStateEvent()

    data class ResetPasswordAttemptEvent(
        val email: String
    ): AuthStateEvent()

    object CheckPreviousAuthEvent : AuthStateEvent()

}