package com.codingwithmitch.openapi.api.auth

import android.content.SharedPreferences
import com.codingwithmitch.openapi.di.auth.AuthScope
import com.codingwithmitch.openapi.ui.auth.state.AuthViewState
import com.codingwithmitch.openapi.models.AccountProperties
import com.codingwithmitch.openapi.models.AuthToken
import com.codingwithmitch.openapi.persistence.AccountPropertiesDao
import com.codingwithmitch.openapi.repository.util.safeApiCall
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import com.codingwithmitch.openapi.util.PreferencesKeys
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

const val USERS_COLLECTION = "users"
const val USER_EMAIL_ERROR = "The email address is already in use by another account."

@AuthScope
class AuthService
@Inject
constructor(
    private val auth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore,
    private val accountPropertiesDao: AccountPropertiesDao,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor
) {

    suspend fun loginUser(
        email: String,
        password: String
    ): DataState<AuthViewState> {

        return safeApiCall {
            var result: DataState<AuthViewState> =
                DataState.error(Response("Error occurred", ResponseType.Dialog))
            val accountProperties: AccountProperties?

            auth.signInWithEmailAndPassword(email, password)
                .addOnFailureListener {
                    result = DataState.error(Response(it.message, ResponseType.Dialog))
                }.await()

            val uid = auth.uid

            if (uid != null) {
                accountProperties = firebaseFirestore
                    .collection(USERS_COLLECTION)
                    .document(uid)
                    .get()
                    .addOnFailureListener {
                        result = DataState.error(Response(it.message, ResponseType.Dialog))
                    }
                    .await()
                    .toObject(AccountProperties::class.java)

                if (accountProperties != null) {
                    result = DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(
                                accountProperties.pk,
                                UUID.randomUUID().toString()
                            )
                        ),
                        response = Response("Success", ResponseType.None)
                    )

                    accountPropertiesDao.insertOrIgnore(accountProperties)

                    saveAuthenticatedUserToPrefs(email)
                }
            }
            result
        }
    }

    suspend fun registerUser(
        email: String,
        password: String,
        username: String
    ): DataState<AuthViewState> {

        var hasErrors: Boolean = false

        return safeApiCall {
            var result: DataState<AuthViewState> =
                DataState.error(Response("Error occurred", ResponseType.Dialog))
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    it.exception?.let { stroke ->
                        if (stroke.toString().contains("another account")) {
                            result = DataState.error(
                                Response(
                                    "The email address is already in use by another account.",
                                    ResponseType.Dialog
                                )
                            )
                            hasErrors = true
                        }
                    }
                }
                .addOnFailureListener {
                    it.message?.let { stroke ->
                        if (stroke.contains("another account")) {
                            result = DataState.error(
                                Response(
                                    "The email address is already in use by another account.",
                                    ResponseType.Dialog
                                )
                            )
                        }
                        hasErrors = true
                    }

                }.await()

            if (!hasErrors) {
                val uid = auth.uid ?: " "
                val user = AccountProperties(uid, email, username)
                firebaseFirestore
                    .collection(USERS_COLLECTION)
                    .document(uid)
                    .set(user)
                    .addOnSuccessListener {
                        result = DataState.data(
                            data = AuthViewState(
                                authToken = AuthToken(
                                    user.pk,
                                    UUID.randomUUID().toString()
                                )
                            ),
                            response = Response("Success", ResponseType.None)
                        )
                    }
                    .addOnFailureListener {
                        result = DataState.error(Response(it.message, ResponseType.Dialog))
                    }
                    .await()


                accountPropertiesDao.insertOrIgnore(user)


                saveAuthenticatedUserToPrefs(email)
            }

            result
        }
    }

    suspend fun resetPassword(email: String): DataState<AuthViewState> {
        var result: DataState<AuthViewState> =
            DataState.error(Response("Error occurred", ResponseType.Dialog))

        return safeApiCall {
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    result = DataState.data(
                        data = AuthViewState(),
                        response = Response(
                            "Check your email for reset password",
                            ResponseType.Toast
                        )
                    )
                }
                .addOnFailureListener {
                    result = DataState.error(Response(it.message, ResponseType.Dialog))
                }
                .await()

            result
        }
    }

    private fun saveAuthenticatedUserToPrefs(email: String) {
        sharedPrefsEditor.putString(PreferencesKeys.PREVIOUS_AUTH_USER, email)
        sharedPrefsEditor.apply()
    }
//
//    suspend fun checkPreviousAuthUser(): DataState<AuthViewState> {
//
//        val previousAuthUserEmail: String? =
//            sharedPreferences.getString(PreferencesKeys.PREVIOUS_AUTH_USER, null)
//
//        if (previousAuthUserEmail.isNullOrBlank()) {
//            return returnNoTokenFound()
//        }
//
//        val accountProperties = accountPropertiesDao.searchByEmail(previousAuthUserEmail)
//
//        if (accountProperties != null) {
//            return DataState.data(
//                AuthViewState(
//                    authToken = AuthToken(accountProperties.pk, UUID.randomUUID().toString())
//                )
//            )
//        }
//        return DataState.data(
//            data = null,
//            response = Response("Check previous auth user done", ResponseType.None)
//        )
//
//    }
//
//    private fun returnNoTokenFound(): DataState<AuthViewState> {
//        return DataState.data(
//            data = null,
//            response = Response("Check previous auth user done", ResponseType.None)
//        )
//    }
}