package com.codingwithmitch.openapi.repository.auth

import com.codingwithmitch.openapi.api.auth.AuthService
import com.codingwithmitch.openapi.di.auth.state.AuthViewState
import com.codingwithmitch.openapi.models.AccountProperties
import com.codingwithmitch.openapi.persistence.AccountPropertiesDao
import com.codingwithmitch.openapi.persistence.AuthTokenDao
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.DataState
import javax.inject.Inject

class AuthRepository
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val authService: AuthService,
    val sessionManager: SessionManager
)
{

    suspend fun attemptRegister(email: String, password: String, username: String): DataState<AuthViewState>{
        return authService.registerUser(email, password, username)
    }

    suspend fun attemptLogin(email: String, password: String): DataState<AuthViewState>{
        return authService.loginUser(email, password)
    }

}