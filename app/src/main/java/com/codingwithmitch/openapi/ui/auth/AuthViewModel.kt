package com.codingwithmitch.openapi.ui.auth

import androidx.lifecycle.*
import com.codingwithmitch.openapi.di.auth.state.AuthStateEvent
import com.codingwithmitch.openapi.di.auth.state.AuthStateEvent.*
import com.codingwithmitch.openapi.di.auth.state.AuthViewState
import com.codingwithmitch.openapi.di.auth.state.LoginFields
import com.codingwithmitch.openapi.di.auth.state.RegistrationFields
import com.codingwithmitch.openapi.models.AuthToken
import com.codingwithmitch.openapi.repository.auth.AuthRepository
import com.codingwithmitch.openapi.ui.BaseViewModel
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.util.AbsentLiveData
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthViewModel
@Inject
constructor(
    private val authRepository: AuthRepository
) : BaseViewModel<AuthStateEvent, AuthViewState>() {

    override fun handleStateEvent(stateEvent: AuthStateEvent): LiveData<DataState<AuthViewState>> {
        when (stateEvent) {

            is LoginAttemptEvent -> {
                var result: DataState<AuthViewState>? = null
                viewModelScope.launch {
                    result = authRepository.attemptLogin(stateEvent.email, stateEvent.password)
                }
                return MutableLiveData(result)
            }

            is RegisterAttemptEvent -> {
                var result: DataState<AuthViewState>? = null
                viewModelScope.launch {
                    result = authRepository.attemptRegister(
                        stateEvent.email,
                        stateEvent.username,
                        stateEvent.password
                    )
                }
                return MutableLiveData(result)
            }

            is CheckPreviousAuthEvent -> {
                return AbsentLiveData.create()
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

    fun setAuthToken(authToken: AuthToken) {
        val update = getCurrentViewStateOrNew()
        if (update.authToken == authToken)
            return
        update.authToken = authToken
        _viewState.value = update
    } 
}