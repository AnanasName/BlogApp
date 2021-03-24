package com.codingwithmitch.openapi.util

import retrofit2.Response

@Suppress("unused")
sealed class GenericApiResponse<T> {

    class ApiEmptyResponse<T>: GenericApiResponse<T>()

    data class ApiSuccessResponse<T>(val body: T): GenericApiResponse<T>() {}

    data class ApiErrorResponse<T>(val errorMessage: String): GenericApiResponse<T>()

    companion object{

        fun <T> create(error: Throwable): ApiErrorResponse<T>{
            return ApiErrorResponse(
                error.message ?: "unknown error"
            )
        }

        fun <T> create(response: Response<T>): GenericApiResponse<T> {

            if (response.isSuccessful){
                val body = response.body()
                return if (body == null || response.code() == 204){
                    ApiEmptyResponse()
                }else if (response.code() == 401){
                    ApiErrorResponse("401 Unathorized. Token may be invalid.")
                }else{
                    ApiSuccessResponse(body = body)
                }
            }else{
                val msg = response.errorBody()?.string()
                val errorMsg = if (msg.isNullOrEmpty()){
                    response.message()
                }else{
                    msg
                }

                return ApiErrorResponse(
                    errorMsg ?: "unknown error"
                )
            }
        }
    }
}
