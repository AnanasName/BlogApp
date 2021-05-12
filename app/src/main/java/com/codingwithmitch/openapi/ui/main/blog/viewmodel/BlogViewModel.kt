package com.codingwithmitch.openapi.ui.main.blog.viewmodel

import android.content.SharedPreferences
import com.codingwithmitch.openapi.api.main.SUCCESS_DELETED
import com.codingwithmitch.openapi.api.main.SUCCESS_UPDATED
import com.codingwithmitch.openapi.persistence.BlogQueryUtils
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
import com.codingwithmitch.openapi.util.PreferencesKeys.Companion.BLOG_FILTER
import com.codingwithmitch.openapi.util.PreferencesKeys.Companion.BLOG_ORDER
import kotlinx.coroutines.launch
import javax.inject.Inject

class BlogViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val blogRepository: BlogRepository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor
) : BaseViewModel<BlogStateEvent, BlogViewState>() {

    init {
        setBlogFilter(
            sharedPreferences.getString(
                BLOG_FILTER,
                BlogQueryUtils.BLOG_FILTER_DATE_UPDATED
            )
        )

        setBlogOrder(
            sharedPreferences.getString(
                BLOG_ORDER,
                BlogQueryUtils.BLOG_ORDER_ASC
            )
        )
    }

    override fun initViewState(): BlogViewState {
        return BlogViewState()
    }

    fun saveFilterOptions(filter: String, order: String) {
        editor.putString(BLOG_FILTER, filter)
        editor.apply()

        editor.putString(BLOG_ORDER, order)
        editor.apply()
    }

    override fun handleStateEvent(stateEvent: BlogStateEvent) {
        when (stateEvent) {

            is BlogSearchEvent -> {
                performGetBlogPosts(stateEvent)
            }

            is CheckAuthorOfBlogPost -> {
                performCheckingIsAuthor()
            }

            is DeleteBlogPostEvent -> {
                performDeleteBlogPost()
            }

            is UpdatedBlogPostEvent -> {
                performUpdateBlogPost(stateEvent)
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
            var cachingData = blogRepository.getBlogPostsFromDatabase(
                getSearchQuery(),
                getPage(),
                getOrder() + getFilter()
            )
            result = DataState.data(
                BlogViewState(
                    BlogViewState.BlogFields(
                        cachingData,
                        getSearchQuery()
                    )
                ), Response("Data retrieved success", ResponseType.None)
            )

            if (!sessionManager.isConnectedToTheInternet()) {
                _dataState.value = result
                return@launch
            }

            result.data?.let { data ->
                data.data?.let { event ->
                    _dataState.value = DataState.loading(true, event.peekContent())
                }
            }

            result = blogRepository.getBlogPostsFromNetwork(getSearchQuery())

            result.data?.data?.let { event ->
                event.peekContent().blogFields.blogList.forEach { blogPost ->
                    launch {
                        blogRepository.insertBlogPostToDatabase(blogPost)
                    }
                }
            }

            setQueryInProgress(true)
            cachingData = blogRepository.getBlogPostsFromDatabase(
                getSearchQuery(),
                getPage(),
                getOrder() + getFilter()
            )
            setQueryInProgress(false)

            if (getPage() * PAGINATION_PAGE_SIZE > viewState.value!!.blogFields.blogList.size) {
                _dataState.value = DataState.data(
                    BlogViewState(
                        BlogViewState.BlogFields(
                            cachingData,
                            isQueryExhausted = true
                        )
                    ), Response("Data retrieved success", ResponseType.None)
                )
            } else
                _dataState.value = DataState.data(
                    BlogViewState(
                        BlogViewState.BlogFields(
                            cachingData,
                            isQueryExhausted = false
                        )
                    ), Response("Data retrieved success", ResponseType.None)
                )
        }
    }

    private fun performDeleteBlogPost() {
        initNewJob()

        blogRepository.addJob("performDeleteBlogPost", job)

        var result: DataState<BlogViewState>

        coroutineScope.launch {

            if (!sessionManager.isConnectedToTheInternet()) {
                _dataState.value = DataState.error(
                    Response(
                        "Can't do that operation without internet",
                        ResponseType.Dialog
                    )
                )
                return@launch
            }

            _dataState.value = DataState.loading(true, null)

            result = blogRepository.deleteBlogPostFromNetwork(getBlogPost())

            result.data?.let { data ->
                if (data.response?.peekContent()?.message.equals(SUCCESS_DELETED)) {
                    blogRepository.deleteBlogPostFromDatabase(getBlogPost())
                    result = DataState.data(null, Response(SUCCESS_DELETED, ResponseType.Toast))
                }
            }

            _dataState.value = result

        }
    }

    private fun performUpdateBlogPost(stateEvent: UpdatedBlogPostEvent) {
        initNewJob()

        blogRepository.addJob("performUpdateBlogPost", job)

        var result: DataState<BlogViewState>

        coroutineScope.launch {

            if (!sessionManager.isConnectedToTheInternet()) {
                _dataState.value = DataState.error(
                    Response(
                        "Can't do that operation without internet",
                        ResponseType.Dialog
                    )
                )
                return@launch
            }

            _dataState.value = DataState.loading(true, null)

            val blogPost = getBlogPost().copy()
            blogPost.title = stateEvent.title
            blogPost.body = stateEvent.body

            //Set New image

            result = blogRepository.updateBlogInNetwork(blogPost)

            result.data?.let { data ->
                if (data.response?.peekContent()?.message.equals(SUCCESS_UPDATED)) {
                    viewState.value?.let { viewState ->
                        viewState.updateBlogFields.blogPost?.let {
                            blogRepository.updateBlogInDatabase(it)
                            result = DataState.data(null, Response(SUCCESS_UPDATED, ResponseType.Toast))
                            onBlogPostUpdateSuccess(blogPost)
                        }
                    }
                }
            }

            _dataState.value = result
        }
    }

    private fun performCheckingIsAuthor() {
        var isAuthor = false
        if (sessionManager.getId() == getBlogPost().authorPk) {
            isAuthor = true
        }
        _dataState.value = DataState.data(
            BlogViewState(
                BlogViewState.BlogFields(),
                BlogViewState.ViewBlogFields(
                    isAuthorOfBlogPost = isAuthor
                )
            )
        )
    }

}
