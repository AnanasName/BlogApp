package com.codingwithmitch.openapi.api.main

import android.util.Log
import com.codingwithmitch.openapi.api.auth.USERS_COLLECTION
import com.codingwithmitch.openapi.di.auth.state.AuthViewState
import com.codingwithmitch.openapi.models.AccountProperties
import com.codingwithmitch.openapi.models.AuthToken
import com.codingwithmitch.openapi.repository.util.safeApiCall
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import com.codingwithmitch.openapi.ui.main.account.state.AccountViewState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class MainService(
    private val firebaseFirestore: FirebaseFirestore
) {

    suspend fun getAccountProperties(
        id: String
    ): DataState<AccountViewState> {
        return safeApiCall {
            var result: DataState<AccountViewState> =
                DataState.error(Response("Error occurred", ResponseType.Dialog))

            val accountProperties: AccountProperties? = firebaseFirestore
                .collection(USERS_COLLECTION)
                .document(id)
                .get()
                .addOnFailureListener {
                    result = DataState.error(Response(it.message, ResponseType.Dialog))
                }
                .await()
                .toObject(AccountProperties::class.java)

            if (accountProperties != null) {
                result = DataState.data(
                    data = AccountViewState(
                        accountProperties
                    ),
                    response = Response("Success", ResponseType.None)
                )
            }
            result
        }
    }
}