package com.codingwithmitch.openapi.repository.auth

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.codingwithmitch.openapi.api.auth.AuthService
import com.codingwithmitch.openapi.di.auth.state.AuthViewState
import com.codingwithmitch.openapi.di.auth.state.LoginFields
import com.codingwithmitch.openapi.di.auth.state.RegistrationFields
import com.codingwithmitch.openapi.di.auth.state.ResetPasswordFields
import com.codingwithmitch.openapi.models.AccountProperties
import com.codingwithmitch.openapi.persistence.AccountPropertiesDao
import com.codingwithmitch.openapi.repository.util.safeApiCall
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import javax.inject.Inject

class AuthRepository
@Inject
constructor(
    val authService: AuthService,
    val sessionManager: SessionManager
) {

    suspend fun attemptRegister(
        email: String,
        username: String,
        password: String,
        passwordConfirm: String
    ): DataState<AuthViewState> {

        val registrationFieldsErrors =
            RegistrationFields(email, username, password, passwordConfirm).isValidForRegistration()
        if (!registrationFieldsErrors.equals(RegistrationFields.RegistrationError.none())) {
            return DataState.error(
                Response(
                    registrationFieldsErrors,
                    ResponseType.Dialog
                )
            )
        }

        return if (sessionManager.isConnectedToTheInternet()) {
            authService.registerUser(email, password, username)
        }else{
            DataState.error(Response("Check internet connection", ResponseType.Dialog))
        }

    }

    suspend fun attemptLogin(email: String, password: String): DataState<AuthViewState> {

        val loginFieldErrors = LoginFields(email, password).isValidLogin()
        if (!loginFieldErrors.equals(LoginFields.LoginError.none()))
            return DataState.error(
                Response(
                    loginFieldErrors,
                    ResponseType.Dialog
                )
            )

        return if (sessionManager.isConnectedToTheInternet()) {
            authService.loginUser(email, password)
        } else {
            DataState.error(Response("Check internet connection", ResponseType.Dialog))
        }
    }

    suspend fun attemptResetPassword(email: String): DataState<AuthViewState>{

        val resetPasswordErrors = ResetPasswordFields(email).isValidLogin()
        if (!resetPasswordErrors.equals(ResetPasswordFields.ResetPasswordError.none()))
            return DataState.error(
                Response(
                    resetPasswordErrors,
                    ResponseType.Dialog
                )
            )

        return if (sessionManager.isConnectedToTheInternet()){
            authService.resetPassword(email)
        }else{
            DataState.error(Response("Check internet connection", ResponseType.Dialog))
        }
    }
}