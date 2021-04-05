package com.codingwithmitch.openapi.ui

import android.util.Log
import com.codingwithmitch.openapi.session.SessionManager
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseActivity : DaggerAppCompatActivity(), DataStateChangeListener {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onDataStateChange(dataState: DataState<*>?) {
        dataState?.let {
            GlobalScope.launch(Main){
                displayProgressBar(it.loading.isLoading)

                it.error?.let { errorEvent ->
                    handleStateError(errorEvent)
                }

                it.data?.let {
                    it.response?.let { responseEvent ->
                        handleStateResponse(responseEvent)
                    }
                }
            }
        }
    }

    private fun handleStateResponse(event: Event<Response>){
            event.getContentIfNotHandled()?.let {

                when (it.responseType){

                    is ResponseType.Toast -> {
                        it.message?.let { msg ->
                            displayToast(msg)
                        }
                    }

                    is ResponseType.Dialog -> {
                        it.message?.let { msg ->
                            displayErrorDialog(msg)
                        }
                    }

                    is ResponseType.None -> {
                        Log.d("LOGGING", "handleStateError: ${it.message}")
                    }
                }
            }
    }

    private fun handleStateError(errorEvent: Event<StateError>){
        errorEvent.getContentIfNotHandled()?.let {

            when (it.response.responseType){

                is ResponseType.Toast -> {
                    it.response.message?.let { msg ->
                        displayToast(msg)
                    }
                }

                is ResponseType.Dialog -> {
                    it.response.message?.let { msg ->
                        displayErrorDialog(msg)
                    }
                }

                is ResponseType.None -> {
                    Log.e("LOGGING", "handleStateError: ${it.response.message}")
                }
            }
        }
    }

    abstract fun displayProgressBar(boolean: Boolean)

}