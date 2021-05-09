package com.codingwithmitch.openapi.ui.main.account

import android.util.Log
import com.codingwithmitch.openapi.models.AccountProperties
import com.codingwithmitch.openapi.repository.main.AccountRepository
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.BaseViewModel
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import com.codingwithmitch.openapi.ui.main.account.state.AccountStateEvent
import com.codingwithmitch.openapi.ui.main.account.state.AccountStateEvent.*
import com.codingwithmitch.openapi.ui.main.account.state.AccountViewState
import kotlinx.coroutines.*
import javax.inject.Inject

class AccountViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    val accountRepository: AccountRepository
) : BaseViewModel<AccountStateEvent, AccountViewState>() {

    override fun handleStateEvent(stateEvent: AccountStateEvent) {
        when (stateEvent) {

            is GetAccountPropertiesEvent -> {
                performGetAccountProperties(stateEvent)
            }

            is UpdateAccountPropertiesEvent -> {
                performUpdateAccountProperties(stateEvent)
            }

            is ChangePasswordEvent -> {
                performChangePassword(stateEvent)
            }

            is None -> {
                _dataState.value = DataState.data(AccountViewState(), Response(null, ResponseType.None))
            }
        }
    }

    override fun initViewState(): AccountViewState {
        return AccountViewState()
    }

    fun setAccountPropertiesData(accountProperties: AccountProperties) {
        val update = getCurrentViewStateOrNew()
        if (update.accountProperties == accountProperties) {
            return
        }
        update.accountProperties = accountProperties
        _viewState.value = update
    }

    fun logout() {
        sessionManager.logout()
    }

    fun cancelJobs() {
        handlePendingData()
        accountRepository.cancelActiveJobs()
    }

    fun handlePendingData(){
        setStateEvent(None)
    }

    override fun onCleared() {
        super.onCleared()
        cancelJobs()
    }

    private fun performGetAccountProperties(stateEvent: GetAccountPropertiesEvent) {
        initNewJob()

        accountRepository.addJob("performGetAccountProperties", job)

        if (sessionManager.getId() == null) {
            _dataState.value =
                DataState.error(response = Response("No active user", ResponseType.Dialog))
            return
        }

        var result: DataState<AccountViewState>

        coroutineScope.launch {
            sessionManager.getId()?.let { id ->
                result = accountRepository.getAccountPropertiesFromDatabase(id)

                if (!sessionManager.isConnectedToTheInternet()) {
                    _dataState.value = result
                    return@launch
                }

                result.data?.let { data ->
                    data.data?.let { event ->
                        _dataState.value = DataState.loading(true, event.peekContent())
                    }
                }

                result = accountRepository.getAccountPropertiesFromNetwork(id)



                result.data?.data?.let { event ->
                    event.peekContent().accountProperties?.let { accountProp ->
                        accountRepository.updateLocalDatabase(
                            accountProp.pk,
                            accountProp.email,
                            accountProp.username
                        )
                    }
                }

                val data = accountRepository.getAccountPropertiesFromDatabase(id)

                if (data.data?.data?.peekContent()?.accountProperties == null)
                    Log.d("DEBUG", "Here")

                _dataState.value = data
            }
        }
    }

    private fun performUpdateAccountProperties(stateEvent: UpdateAccountPropertiesEvent) {
        initNewJob()

        accountRepository.addJob("performUpdateAccountProperties", job)

        if (sessionManager.getId() == null) {
            _dataState.value =
                DataState.error(response = Response("No active user", ResponseType.Dialog))
            return
        }

        var result: DataState<AccountViewState>

        coroutineScope.launch {
            sessionManager.getId()?.let { id ->

                if (!sessionManager.isConnectedToTheInternet()) {
                    _dataState.value = DataState.error(Response("Can't do that operation without internet", ResponseType.Dialog))
                    return@launch
                }

                _dataState.value = DataState.loading(true)

                result = accountRepository.saveAccountPropertiesToNetwork(
                    AccountProperties(
                        id,
                        stateEvent.email,
                        stateEvent.username
                    )
                )

                result.data?.data?.let { event ->
                    event.peekContent().accountProperties?.let { accountProp ->
                        accountRepository.updateLocalDatabase(
                            accountProp.pk,
                            accountProp.email,
                            accountProp.username
                        )
                    }
                }

                _dataState.value = result
            }
        }

    }

    private fun performChangePassword(stateEvent: ChangePasswordEvent) {
        initNewJob()

        accountRepository.addJob("performChangePassword", job)

        if (sessionManager.getCurrentUser() == null) {
            _dataState.value =
                DataState.error(response = Response("No active user", ResponseType.Dialog))
            return
        }

        var result: DataState<AccountViewState>

        coroutineScope.launch {
            sessionManager.getCurrentUser()?.let { user ->
                result = DataState.error(Response("Error occurred", ResponseType.Toast))

                if (!stateEvent.newPassword.equals(stateEvent.confirmNewPassword)){
                    _dataState.value = DataState.error(Response("Passwords must match", ResponseType.Dialog))
                    return@launch
                }

                if (stateEvent.newPassword.equals(stateEvent.currentPassword) || stateEvent.confirmNewPassword.equals(stateEvent.currentPassword)){
                    _dataState.value = DataState.error(Response("New and old password must be different", ResponseType.Dialog))
                }

                if (!sessionManager.isConnectedToTheInternet()) {
                    _dataState.value = DataState.error(Response("Can't do that operation without internet", ResponseType.Dialog))
                    return@launch
                }

                _dataState.value = DataState.loading(true, null)

                result = accountRepository.updatePasswordInNetwork(stateEvent.currentPassword, stateEvent.newPassword)

                _dataState.value = result
            }
        }
    }
}
