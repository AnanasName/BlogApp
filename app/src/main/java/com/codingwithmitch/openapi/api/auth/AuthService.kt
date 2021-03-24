package com.codingwithmitch.openapi.api.auth

import com.codingwithmitch.openapi.di.auth.AuthScope
import com.codingwithmitch.openapi.di.auth.state.AuthViewState
import com.codingwithmitch.openapi.models.AccountProperties
import com.codingwithmitch.openapi.models.AuthToken
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

const val USERS_COLLECTION = "users"

@AuthScope
class AuthService
@Inject
constructor(
    private val auth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore
) {

    suspend fun loginUser(
        email: String,
        password: String
    ): DataState<AuthViewState> {
        var result: DataState<AuthViewState> =
            DataState.error(Response("Error occurred", ResponseType.Dialog))
        var accountProperties: AccountProperties?

            auth.signInWithEmailAndPassword(email, password)
                .addOnFailureListener {
                    result = DataState.error(Response(it.message, ResponseType.Dialog))
                }.await()

        val uid = auth.uid

        if (uid != null){
            accountProperties = firebaseFirestore
                .collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .addOnFailureListener{
                    result = DataState.error(Response(it.message, ResponseType.Dialog))
                }
                .await()
                .toObject(AccountProperties::class.java)

            if (accountProperties != null){
                return DataState.data(
                    data = AuthViewState(authToken = AuthToken(accountProperties.pk, UUID.randomUUID().toString())),
                    response = Response("Success", ResponseType.None)
                )
            }
        }

        return result
    }

    suspend fun registerUser(
        email: String,
        password: String,
        username: String
    ): DataState<AuthViewState> {
        var result: DataState<AuthViewState> =
            DataState.error(Response("Error occurred", ResponseType.Dialog))
        auth.createUserWithEmailAndPassword(email, password)
            .addOnFailureListener {
                result = DataState.error(Response(it.message, ResponseType.Dialog))
            }.await()

        val uid = auth.uid ?: " "
        val user = AccountProperties(uid, email, username)
        firebaseFirestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .set(user)
            .addOnSuccessListener {
                result = DataState.data(
                    data = AuthViewState(authToken = AuthToken(user.pk, UUID.randomUUID().toString())),
                    response = Response("Success", ResponseType.None)
                )
            }
            .addOnFailureListener {
                result = DataState.error(Response(it.message, ResponseType.Dialog))
            }
            .await()

        return result
    }
}