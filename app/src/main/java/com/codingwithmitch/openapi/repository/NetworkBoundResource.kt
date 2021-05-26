package com.codingwithmitch.openapi.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import com.codingwithmitch.openapi.util.GenericApiResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

@InternalCoroutinesApi
abstract class NetworkBoundResource<ResponseObject, ViewStateType>(
    isNetworkAvailable: Boolean
) {

    protected val result = MediatorLiveData<DataState<ViewStateType>>()
    protected lateinit var job: CompletableJob
    protected lateinit var coroutineScope: CoroutineScope

    init {
        setJob(initNewJob())
        setValue(DataState.loading(isLoading = true, cachedData = null ))

        if (isNetworkAvailable){
            coroutineScope.launch {

                withContext(Main){
                    val apiResponse = createCall()
                    result.addSource(apiResponse){ response ->
                        result.removeSource(apiResponse)

                        coroutineScope.launch {
                            handleNetworkCall(response)
                        }
                    }
                }
            }
            GlobalScope.launch(IO) {
                delay(3000L)

                if(!job.isCompleted){
                    job.cancel(CancellationException("No internet connection"))
                }
            }
        }else{
            onErrorReturn("Unable to do operation without internet", shouldUseDialog = true, shouldUseToast = false)
        }
    }

    private suspend fun handleNetworkCall(response: GenericApiResponse<ResponseObject>?) {
        when(response){
            is GenericApiResponse.ApiSuccessResponse -> {
                handleApiSuccessResponse((response))
            }
            is GenericApiResponse.ApiErrorResponse -> {
                onErrorReturn(response.errorMessage, true, false)
            }
            is GenericApiResponse.ApiEmptyResponse -> {
                onErrorReturn("HTTP 204. Returned nothing", true, false)
            }
        }
    }

    fun onCompleteJob(dataState: DataState<ViewStateType>){
        GlobalScope.launch(Main){
            job.complete()
            setValue(dataState)
        }
    }

    private fun setValue(dataState: DataState<ViewStateType>){
        result.value = dataState
    }

    fun onErrorReturn(errorMessage: String?, shouldUseDialog: Boolean, shouldUseToast: Boolean){
        var msg = errorMessage
        val useDialog = shouldUseDialog
        var responseType: ResponseType = ResponseType.None
        if (msg == null)
            msg = "ERROR UNKNOWN"
        if (shouldUseToast)
            responseType = ResponseType.Toast
        if (useDialog)
            responseType = ResponseType.Dialog

        onCompleteJob(DataState.error(
            response = Response(
                message = msg,
                responseType = responseType
            )
        ))
    }

    @InternalCoroutinesApi
    private fun initNewJob(): Job{
        job = Job()
        job.invokeOnCompletion(onCancelling = true, invokeImmediately = true, handler = object :
            CompletionHandler {
            override fun invoke(cause: Throwable?) {
                if (job.isCancelled){
                    cause?.let {
                        onErrorReturn(it.message, false, true)
                    } ?: onErrorReturn("ERROR UNKNOWN", false, true)
                }else if(job.isCompleted){

                }
            }
        })
        coroutineScope = CoroutineScope(IO + job)
        return job
    }

    fun asLiveData() = result as LiveData<DataState<ViewStateType>>

    abstract suspend fun handleApiSuccessResponse(response: GenericApiResponse.ApiSuccessResponse<ResponseObject>)

    abstract fun createCall(): LiveData<GenericApiResponse<ResponseObject>>

    abstract fun setJob(job: Job)
}