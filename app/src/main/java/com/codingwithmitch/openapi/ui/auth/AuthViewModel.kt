package com.codingwithmitch.openapi.ui.auth

import android.util.Log
import androidx.lifecycle.*
import com.codingwithmitch.openapi.di.auth.state.*
import com.codingwithmitch.openapi.di.auth.state.AuthStateEvent.*
import com.codingwithmitch.openapi.models.AuthToken
import com.codingwithmitch.openapi.repository.auth.AuthRepository
import com.codingwithmitch.openapi.repository.util.safeApiCall
import com.codingwithmitch.openapi.ui.BaseViewModel
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.util.AbsentLiveData
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import javax.inject.Inject

class AuthViewModel
@Inject
constructor(
    private val authRepository: AuthRepository
) : BaseViewModel<AuthStateEvent, AuthViewState>() {

    private var job: CompletableJob = Job()
    lateinit var coroutineScope: CoroutineScope

    override fun handleStateEvent(stateEvent: AuthStateEvent){
        when (stateEvent) {

            is LoginAttemptEvent -> {
                _dataState.value = DataState.loading(true, null)

                initNewJob()

                var result: DataState<AuthViewState>?

                coroutineScope.launch {
                    result = authRepository.attemptLogin(stateEvent.email, stateEvent.password)

                    _dataState.value = result
                }
            }

            is RegisterAttemptEvent -> {

                initNewJob()

                var result: DataState<AuthViewState>?
                coroutineScope.launch {
                    _dataState.value = DataState.loading(true, null)

                    result = authRepository.attemptRegister(
                        stateEvent.email,
                        stateEvent.username,
                        stateEvent.password,
                        stateEvent.confirm_password
                    )

                    _dataState.value = result
                }
            }

            is ResetPasswordAttemptEvent -> {

                initNewJob()

                var result: DataState<AuthViewState>?
                coroutineScope.launch {
                    result = authRepository.attemptResetPassword(
                        stateEvent.email
                    )

                    _dataState.value = result
                }
            }

            is CheckPreviousAuthEvent -> {
                _dataState.value = null
            }
        }
    }

    override fun initViewState(): AuthViewState {
        return AuthViewState()
    }

    fun setRegistrationFields(registrationFields: RegistrationFields) {
        val update = getCurrentViewStateOrNew()
        if (update.registrationFields == registrationFields)
            return
        update.registrationFields = registrationFields
        _viewState.value = update
    }

    fun setLoginFields(loginFields: LoginFields) {
        val update = getCurrentViewStateOrNew()
        if (update.loginFields == loginFields)
            return
        update.loginFields = loginFields
        _viewState.value = update
    }

    fun setResetPasswordFields(resetPasswordFields: ResetPasswordFields){
        val update = getCurrentViewStateOrNew()
        if (update.resetPasswordFields == resetPasswordFields)
            return
        update.resetPasswordFields = resetPasswordFields
        _viewState.value = update
    }

    fun setAuthToken(authToken: AuthToken) {
        val update = getCurrentViewStateOrNew()
        if (update.authToken == authToken)
            return
        update.authToken = authToken
        _viewState.value = update
    }

    private fun initNewJob() {
        job = Job()
        coroutineScope = CoroutineScope(viewModelScope.coroutineContext + job)
    }

    fun cancelJobs() {
        job.cancel()
    }


    override fun onCleared() {
        super.onCleared()
        cancelJobs()
    }
}