package com.codingwithmitch.openapi.ui.auth

import com.codingwithmitch.openapi.models.AuthToken
import com.codingwithmitch.openapi.repository.auth.AuthRepository
import com.codingwithmitch.openapi.ui.BaseViewModel
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import com.codingwithmitch.openapi.ui.auth.state.*
import com.codingwithmitch.openapi.ui.auth.state.AuthStateEvent.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthViewModel
@Inject
constructor(
    private val authRepository: AuthRepository
) : BaseViewModel<AuthStateEvent, AuthViewState>() {

    override fun handleStateEvent(stateEvent: AuthStateEvent){
        when (stateEvent) {

            is LoginAttemptEvent -> {
                performLogin(stateEvent)
            }

            is RegisterAttemptEvent -> {
                performRegister(stateEvent)
            }

            is ResetPasswordAttemptEvent -> {
                performResetPassword(stateEvent)
            }

            is CheckPreviousAuthEvent -> {
                _dataState.value = null
            }

            is None -> {
                _dataState.value = DataState.data(null, Response(null, ResponseType.None))
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

    fun cancelJobs() {
        handlePendingData()
        authRepository.cancelActiveJobs()
    }

    fun handlePendingData(){
        setStateEvent(None)
    }

    override fun onCleared() {
        super.onCleared()
        cancelJobs()
    }

    private fun performLogin(stateEvent: LoginAttemptEvent){
        _dataState.value = DataState.loading(true, null)

        initNewJob()

        authRepository.addJob("performLogin", job)

        var result: DataState<AuthViewState>?

        coroutineScope.launch {
            result = authRepository.attemptLogin(stateEvent.email, stateEvent.password)

            _dataState.value = result
        }
    }

    private fun performRegister(stateEvent: RegisterAttemptEvent){

        initNewJob()

        authRepository.addJob("performRegister", job)

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

    private fun performResetPassword(stateEvent: ResetPasswordAttemptEvent){

        initNewJob()

        authRepository.addJob("performResetPassword", job)

        var result: DataState<AuthViewState>?
        coroutineScope.launch {
            result = authRepository.attemptResetPassword(
                stateEvent.email
            )

            _dataState.value = result
        }
    }
}