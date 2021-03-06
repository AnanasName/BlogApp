package com.codingwithmitch.openapi.ui.main.blog.viewmodel

import com.codingwithmitch.openapi.ui.main.blog.state.BlogStateEvent
import com.codingwithmitch.openapi.ui.main.blog.state.BlogStateEvent.BlogSearchEvent
import com.codingwithmitch.openapi.ui.main.blog.state.BlogViewState

fun BlogViewModel.resetPage(){
    val update = getCurrentViewStateOrNew()
    update.blogFields.page = 1
    setViewState(update)
}

fun BlogViewModel.refreshBlogList(){
    setQueryInProgress(true)
    setQueryExhausted(false)
    setStateEvent(BlogStateEvent.RestoreBlogList)
}

fun BlogViewModel.loadFirstPage(){
    setQueryInProgress(true)
    setQueryExhausted(false)
    resetPage()
    setStateEvent(BlogSearchEvent)
}

fun BlogViewModel.incrementPageNumber(){
    val update = getCurrentViewStateOrNew()
    val page = update.copy().blogFields.page
    update.blogFields.page = page + 1
    setViewState(update)
}

fun BlogViewModel.nextPage(){
    if (!getIsQueryExhausted() && !getIsQueryInProgress()){
        incrementPageNumber()
        setQueryInProgress(true)
        setStateEvent(BlogSearchEvent)
    }
}

fun BlogViewModel.handleIncomingBlogListData(viewState: BlogViewState){
    setQueryExhausted(viewState.blogFields.isQueryExhausted)
    setQueryInProgress(viewState.blogFields.isQueryInProgress)
    setBlogListData(viewState.blogFields.blogList)
}