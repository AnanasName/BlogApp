package com.codingwithmitch.openapi.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

abstract class BaseViewModel<StateEvent, ViewState> : ViewModel() {

    protected val _stateEvent: MutableLiveData<StateEvent> = MutableLiveData()
    protected val _viewState: MutableLiveData<ViewState> = MutableLiveData()

    val viewState: LiveData<ViewState>
        get() = _viewState

    protected val _dataState: MutableLiveData<DataState<ViewState>> = MutableLiveData()

    val dataState: LiveData<DataState<ViewState>>
        get() = _dataState

    fun setStateEvent(event: StateEvent) {
        _stateEvent.value = event
        event?.let {
            handleStateEvent(event)
        }
    }

    fun getCurrentViewStateOrNew(): ViewState {
        val value = viewState.value?.let {
            it
        } ?: initViewState()
        return value
    }

    abstract fun initViewState(): ViewState

    abstract fun handleStateEvent(stateEvent: StateEvent)
}