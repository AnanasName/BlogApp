package com.codingwithmitch.openapi.ui.main.blog.viewmodel

import android.content.SharedPreferences
import com.bumptech.glide.RequestManager
import com.codingwithmitch.openapi.models.BlogPost
import com.codingwithmitch.openapi.repository.main.BlogRepository
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.BaseViewModel
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import com.codingwithmitch.openapi.ui.main.blog.state.BlogStateEvent
import com.codingwithmitch.openapi.ui.main.blog.state.BlogStateEvent.*
import com.codingwithmitch.openapi.ui.main.blog.state.BlogViewState
import com.codingwithmitch.openapi.util.Constants.Companion.PAGINATION_PAGE_SIZE
import kotlinx.coroutines.launch
import javax.inject.Inject

class BlogViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val blogRepository: BlogRepository,
    private val sharedPreferences: SharedPreferences,
    private val requestManager: RequestManager
) : BaseViewModel<BlogStateEvent, BlogViewState>() {

    override fun initViewState(): BlogViewState {
        return BlogViewState()
    }

    override fun handleStateEvent(stateEvent: BlogStateEvent) {
        when (stateEvent) {

            is BlogSearchEvent -> {
                performGetBlogPosts(stateEvent)
            }

            is CheckAuthorOfBlogPost -> {

            }

            is None -> {
                _dataState.value = DataState.data(null, Response(null, ResponseType.None))
            }
        }
    }

    fun cancelActiveJobs() {
        blogRepository.cancelActiveJobs()
        handlePendingData()
    }

    private fun handlePendingData() {
        setStateEvent(None)
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

    private fun performGetBlogPosts(stateEvent: BlogSearchEvent) {
        initNewJob()

        blogRepository.addJob("performGetBlogPosts", job)

        var result: DataState<BlogViewState>

        coroutineScope.launch {
            var cachingData = blogRepository.getBlogPostsFromDatabase(getSearchQuery(), getPage())
            result = DataState.data(BlogViewState(BlogViewState.BlogFields(cachingData, getSearchQuery())), Response("Data retrieved success", ResponseType.None))

            if (!sessionManager.isConnectedToTheInternet()) {
                _dataState.value = result
                return@launch
            }

            result.data?.let { data ->
                data.data?.let { event ->
                    _dataState.value = DataState.loading(true, event.peekContent())
                }
            }

            result = blogRepository.searchBlogPosts(getSearchQuery())

            result.data?.data?.let { event ->
                event.peekContent().blogFields.blogList.forEach { blogPost ->
                    launch {
                        blogRepository.insertBlogPostToDatabase(blogPost)
                    }
                }
            }

            setQueryInProgress(true)
            cachingData = blogRepository.getBlogPostsFromDatabase(getSearchQuery(), getPage())
            setQueryInProgress(false)

            _dataState.value = DataState.data(BlogViewState(BlogViewState.BlogFields(cachingData)), Response("Data retrieved success", ResponseType.None))
            if (getPage() * PAGINATION_PAGE_SIZE > viewState.value!!.blogFields.blogList.size)
                setQueryExhausted(true)
        }
    }

}
