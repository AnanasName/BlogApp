package com.codingwithmitch.openapi.ui.main.create_blog

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.codingwithmitch.openapi.api.main.SUCCESS_CREATE
import com.codingwithmitch.openapi.api.main.SUCCESS_UPDATED
import com.codingwithmitch.openapi.models.BlogPost
import com.codingwithmitch.openapi.repository.main.CreateBlogRepository
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.BaseViewModel
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import com.codingwithmitch.openapi.ui.main.blog.state.BlogStateEvent
import com.codingwithmitch.openapi.ui.main.blog.state.BlogViewState
import com.codingwithmitch.openapi.ui.main.create_blog.state.CreateBlogStateEvent
import com.codingwithmitch.openapi.ui.main.create_blog.state.CreateBlogStateEvent.CreateNewBlogEvent
import com.codingwithmitch.openapi.ui.main.create_blog.state.CreateBlogViewState
import com.codingwithmitch.openapi.ui.main.create_blog.state.CreateBlogViewState.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class CreateBlogViewModel
@Inject
constructor(
    val createBlogRepository: CreateBlogRepository,
    val sessionManager: SessionManager
) : BaseViewModel<CreateBlogStateEvent, CreateBlogViewState>() {

    override fun initViewState(): CreateBlogViewState {
        return CreateBlogViewState()
    }

    override fun handleStateEvent(stateEvent: CreateBlogStateEvent) {
        when (stateEvent) {

            is CreateNewBlogEvent -> {
                performCreateNewBlog(stateEvent)
            }

            is CreateBlogStateEvent.None -> {
                _dataState.value = DataState.data(null, Response(null, ResponseType.None))
            }
        }
    }

    private fun performCreateNewBlog(stateEvent: CreateNewBlogEvent) {
        initNewJob()

        createBlogRepository.addJob("performCreateNewBlog", job)

        var result: DataState<CreateBlogViewState> =
            DataState.error(Response("Error occurred", ResponseType.Toast))

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

            //Set New image

            result = createBlogRepository.createNewBlogPost(
                stateEvent.title,
                stateEvent.body,
                stateEvent.image
            )

            result.data?.let { data ->
                if (data.response?.peekContent()?.message.equals(SUCCESS_CREATE)) {
                    viewState.value?.let { viewState ->
                        viewState.blogFields.blogPost?.let {
                            createBlogRepository.insertNewBlog(it)
                            clearNewBlogFields()
                        }
                    }
                }
            }
        }

        _dataState.value =
            DataState.data(data = null, response = Response(SUCCESS_CREATE, ResponseType.Toast))
    }

    fun setNewTitle(title: String?) {
        val update = getCurrentViewStateOrNew()
        val newBlogFields = update.blogFields
        title?.let {
            newBlogFields.title = title
        }
        setViewState(update)
    }

    fun setNewBody(body: String?) {
        val update = getCurrentViewStateOrNew()
        val newBlogFields = update.blogFields
        body?.let {
            newBlogFields.body = body
        }
        setViewState(update)
    }

    fun setNewImage(image: Uri?) {
        val update = getCurrentViewStateOrNew()
        val newBlogFields = update.blogFields
        image?.let {
            newBlogFields.image = it
        }
        setViewState(update)
    }

    fun getTitle(): String? {
        return viewState.value?.blogFields?.title
    }

    fun getBody(): String? {
        return viewState.value?.blogFields?.body
    }

    fun getImage(): Uri? {
        return viewState.value?.blogFields?.image
    }

    fun setNewBlogPost(blogPost: BlogPost?) {
        val update = getCurrentViewStateOrNew()
        val newBlogFields = update.blogFields
        blogPost?.let {
            newBlogFields.blogPost = blogPost
        }
        setViewState(update)
    }

    fun clearNewBlogFields() {
        val update = getCurrentViewStateOrNew()
        update.blogFields = NewBlogFields(null, null, null, null)
        setViewState(update)
    }

    fun cancelActiveJobs() {
        createBlogRepository.cancelActiveJobs()
        handlePendingData()
    }

    private fun handlePendingData() {
        setStateEvent(CreateBlogStateEvent.None)
    }

    public fun setNewBlogFields(title: String?, body: String?, uri: Uri?) {
        val update = getCurrentViewStateOrNew()
        val newBlogFields = update.blogFields
        title?.let { newBlogFields.title = it }
        body?.let { newBlogFields.body = it }
        uri?.let { newBlogFields.image = it }
        update.blogFields = newBlogFields
        _viewState.value = update
    }


    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }
}