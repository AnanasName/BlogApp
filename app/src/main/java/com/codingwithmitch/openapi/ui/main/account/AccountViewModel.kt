package com.codingwithmitch.openapi.ui.main.account

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.codingwithmitch.openapi.models.AccountProperties
import com.codingwithmitch.openapi.repository.main.AccountRepository
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.BaseViewModel
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.main.account.state.AccountStateEvent
import com.codingwithmitch.openapi.ui.main.account.state.AccountStateEvent.*
import com.codingwithmitch.openapi.ui.main.account.state.AccountViewState
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import javax.inject.Inject

class AccountViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    val accountRepository: AccountRepository
) : BaseViewModel<AccountStateEvent, AccountViewState>() {

    private var job: CompletableJob = Job()
    lateinit var coroutineScope: CoroutineScope

    override fun handleStateEvent(stateEvent: AccountStateEvent) {
        when (stateEvent) {

            is GetAccountPropertiesEvent -> {
                performGetAccountProperties(stateEvent)
            }

            is UpdateAccountPropertiesEvent -> {

            }

            is ChangePasswordEvent -> {

            }

            is None -> {

            }
        }
    }

    override fun initViewState(): AccountViewState {
        return AccountViewState()
    }

    private fun initNewJob() {
        job = Job()
        coroutineScope = CoroutineScope(viewModelScope.coroutineContext + job)
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
        job.cancel()
    }


    override fun onCleared() {
        super.onCleared()
        cancelJobs()
    }

    private fun performGetAccountProperties(stateEvent: GetAccountPropertiesEvent) {
        initNewJob()

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

                result = accountRepository.getAccountProperties(id)

                result.data?.data?.let { event ->
                    event.peekContent().accountProperties?.let { accountProp ->
                        accountRepository.updateLocalDatabase(
                            accountProp.pk,
                            accountProp.email,
                            accountProp.username
                        )
                    }
                }

                _dataState.value = accountRepository.getAccountPropertiesFromDatabase(id)
            }
        }
    }
}
