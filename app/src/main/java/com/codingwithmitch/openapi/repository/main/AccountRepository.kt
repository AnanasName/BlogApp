package com.codingwithmitch.openapi.repository.main

import android.util.Log
import com.codingwithmitch.openapi.api.main.MainService
import com.codingwithmitch.openapi.models.AccountProperties
import com.codingwithmitch.openapi.persistence.AccountPropertiesDao
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import com.codingwithmitch.openapi.ui.main.account.state.AccountViewState
import javax.inject.Inject

class AccountRepository
@Inject
constructor(
    private val mainService: MainService,
    private val accountPropertiesDao: AccountPropertiesDao,
    private val sessionManager: SessionManager
) {

    suspend fun getAccountProperties(
        id: String
    ): DataState<AccountViewState> {
        return mainService.getAccountProperties(id)
    }

    suspend fun saveAccountProperties(
        accountProperties: AccountProperties
    ): DataState<AccountViewState>{
        return mainService.saveAccountProperties(accountProperties)
    }

    suspend fun updatePassword(
        oldPassword: String,
        newPassword: String
    ): DataState<AccountViewState>{
        return mainService.changePassword(sessionManager.getCurrentUser()!!.email!!, oldPassword, newPassword, sessionManager.getCurrentUser()!!)
    }

    suspend fun getAccountPropertiesFromDatabase(
        id: String
    ): DataState<AccountViewState> {
        return DataState.data<AccountViewState>(
            data = AccountViewState(accountPropertiesDao.searchByPk(id)),
            response = Response(null, ResponseType.None)
        )
    }

    suspend fun updateLocalDatabase(
        pk: String,
        email: String,
        username: String
    ) {
        accountPropertiesDao.updateAccountProperties(
            pk,
            email,
            username
        )
    }

    suspend fun insertAndReplaceAccount(
        accountProperties: AccountProperties
    ){
        accountPropertiesDao.insertAndReplace(accountProperties)
    }

}