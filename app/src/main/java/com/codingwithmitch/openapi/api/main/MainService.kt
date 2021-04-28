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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

const val CHANGES_APPLIED = "Changes applied"

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

    suspend fun saveAccountProperties(
        accountProperties: AccountProperties
    ): DataState<AccountViewState> {

        return safeApiCall {

            var result: DataState<AccountViewState> =
                DataState.error(Response("Error occurred", ResponseType.Dialog))

            firebaseFirestore
                .collection(USERS_COLLECTION)
                .document(accountProperties.pk)
                .set(accountProperties)
                .addOnSuccessListener {
                    result = DataState.data(
                        data = AccountViewState(
                            accountProperties = accountProperties
                        ),
                        response = Response("Account update success", ResponseType.Toast)
                    )
                }
                .addOnFailureListener {
                    result = DataState.error(Response(it.message, ResponseType.Dialog))
                }
                .await()

            result
        }
    }

    suspend fun changePassword(
        email: String,
        oldPassword: String,
        newPassword: String,
        user: FirebaseUser
    ): DataState<AccountViewState> {
        return safeApiCall {
            var result: DataState<AccountViewState> =
                DataState.error(Response("Error occurred", ResponseType.Dialog))

            val credential = EmailAuthProvider.getCredential(email, oldPassword)

            var isCorrectionInfo = false

            user
                .reauthenticate(credential)
                .addOnCompleteListener{
                   isCorrectionInfo = true
                }.await()

            if (isCorrectionInfo) {
                user.updatePassword(newPassword).addOnCompleteListener {
                    result = DataState.data(
                        data = null,
                        response = Response(CHANGES_APPLIED, ResponseType.Toast)
                    )
                }.await()
            }

            result
        }
    }

}