package com.codingwithmitch.openapi.repository.util

import android.util.Log
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.CoroutineContext

suspend fun <T> safeApiCall(
    apiCall: suspend () -> DataState<T>
): DataState<T> {
    return try {
        apiCall.invoke()
    } catch (throwable: Throwable) {
        when (throwable) {
            is TimeoutCancellationException -> {
                DataState.error(
                    response = Response(
                        "Check Network connection",
                        ResponseType.Toast
                    )
                )
            }
            is IOException -> {
                DataState.error(
                    response = Response(
                        "Network Error",
                        ResponseType.Toast
                    )
                )
            }
            is HttpException -> {
                DataState.error(
                    response = Response(
                        "Check Internet connection",
                        ResponseType.Toast
                    )
                )
            }

            is FirebaseAuthWeakPasswordException -> {
                DataState.error<T>(
                    Response(
                        "The password is too simple",
                        ResponseType.Dialog
                    )
                )
            }

            is FirebaseAuthUserCollisionException -> {
                DataState.error<T>(
                    Response(
                        "The email address is already in use by another account.",
                        ResponseType.Dialog
                    )
                )
            }

            is FirebaseAuthInvalidUserException -> {
                DataState.error<T>(
                    Response(
                        "There is no user record corresponding to this email or password doesn't match the email address",
                        ResponseType.Dialog
                    )
                )
            }

            is FirebaseAuthInvalidCredentialsException -> {
                DataState.error<T>(
                    Response(
                        "There is no user record corresponding to this email or password doesn't match the email address",
                        ResponseType.Dialog
                    )
                )
            }

            is FirebaseFirestoreException -> {
                DataState.error<T>(
                    Response(
                        throwable.localizedMessage,
                        ResponseType.Dialog
                    )
                )
            }
            else -> {
                Log.d("DEBUG", throwable.message)
                DataState.error<T>(
                    response = Response(
                        "Unknown error",
                        ResponseType.Toast
                    )
                )
            }
        }
    }
}

//suspend fun <T> safeCacheCall(
//    cacheCall: suspend () -> T?
//): DataState<T> {
//    return try {
//        withTimeout(3000L) {
//            DataState.data(cacheCall.invoke(), Response("none", ResponseType.None))
//        }
//    } catch (throwable: Throwable) {
//        when (throwable) {
//
//            is TimeoutCancellationException -> {
//                DataState.error<T>(Response("Timeout", ResponseType.Toast))
//            }
//            else -> {
//                DataState.error(Response("Unable to retrieve data", ResponseType.Dialog))
//            }
//        }
//    }
//}
