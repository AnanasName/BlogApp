package com.codingwithmitch.openapi.ui.main.blog

import android.content.SharedPreferences
import com.bumptech.glide.RequestManager
import com.codingwithmitch.openapi.models.BlogPost
import com.codingwithmitch.openapi.persistence.BlogPostDao
import com.codingwithmitch.openapi.repository.main.BlogRepository
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.BaseViewModel
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import com.codingwithmitch.openapi.ui.main.account.state.AccountViewState
import com.codingwithmitch.openapi.ui.main.blog.state.BlogStateEvent
import com.codingwithmitch.openapi.ui.main.blog.state.BlogStateEvent.*
import com.codingwithmitch.openapi.ui.main.blog.state.BlogViewState
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.reflect.jvm.internal.impl.util.Check

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

            }
        }
    }

    fun setQuery(query: String) {
        val update = getCurrentViewStateOrNew()
//        if (query.equals(update.blogFields.searchQuery))
//            return
        update.blogFields.searchQuery = query
        _viewState.value = update
    }

    fun setBlogListData(blogList: List<BlogPost>) {
        val update = getCurrentViewStateOrNew()
        update.blogFields.blogList = blogList
        _viewState.value = update
    }

    fun setBlogPost(blogPost: BlogPost){
        val update = getCurrentViewStateOrNew()
        update.viewBlogFields.blogPost = blogPost
        _viewState.value = update
    }

    fun setIsAuthorOfBlogPost(isAuthorBlogPost: Boolean){
        val update = getCurrentViewStateOrNew()
        update.viewBlogFields.isAuthorOfBlogPost = isAuthorBlogPost
        _viewState.value = update
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
            var cachingData = blogRepository.getBlogPostsFromDatabase(viewState.value!!.blogFields.searchQuery)
            result = DataState.data(BlogViewState(BlogViewState.BlogFields(cachingData, viewState.value!!.blogFields.searchQuery)), Response("Data retrieved success", ResponseType.None))

            if (!sessionManager.isConnectedToTheInternet()) {
                _dataState.value = result
                return@launch
            }

            result.data?.let { data ->
                data.data?.let { event ->
                    _dataState.value = DataState.loading(true, event.peekContent())
                }
            }

            result = blogRepository.searchBlogPosts(viewState.value!!.blogFields.searchQuery)

            result.data?.data?.let { event ->
                event.peekContent().blogFields.blogList.forEach { blogPost ->
                    launch {
                        blogRepository.insertBlogPostToDatabase(blogPost)
                    }
                }
            }

            cachingData = blogRepository.getBlogPostsFromDatabase(viewState.value!!.blogFields.searchQuery)

            _dataState.value = DataState.data(BlogViewState(BlogViewState.BlogFields(cachingData)), Response("Data retrieved success", ResponseType.None))
        }
    }

}
