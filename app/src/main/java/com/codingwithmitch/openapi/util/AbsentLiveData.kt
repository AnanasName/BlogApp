package com.codingwithmitch.openapi.util

import androidx.lifecycle.LiveData
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.auth.state.AuthViewState

class AbsentLiveData <T : Any?>
private constructor(): LiveData<T>()
{

    init {
        postValue(null)
    }

    companion object{
        fun <T> create(): LiveData<T>? {
            return AbsentLiveData()
        }
    }



}