package com.codingwithmitch.openapi.ui

import androidx.lifecycle.*
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

abstract class BaseViewModel<StateEvent, ViewState> : ViewModel() {

    protected val _stateEvent: MutableLiveData<StateEvent> = MutableLiveData()
    protected val _viewState: MutableLiveData<ViewState> = MutableLiveData()

    protected var job: CompletableJob = Job()
    lateinit var coroutineScope: CoroutineScope

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

    fun setViewState(viewState: ViewState){
        _viewState.value = viewState
    }

    protected fun initNewJob() {
        job = Job()
        coroutineScope = CoroutineScope(viewModelScope.coroutineContext + job)
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