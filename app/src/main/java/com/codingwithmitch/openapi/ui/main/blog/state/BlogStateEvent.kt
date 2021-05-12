package com.codingwithmitch.openapi.ui.main.blog.state

import okhttp3.MultipartBody

sealed class BlogStateEvent {

    object BlogSearchEvent : BlogStateEvent()

    object CheckAuthorOfBlogPost : BlogStateEvent()

    object DeleteBlogPostEvent: BlogStateEvent()

    data class UpdatedBlogPostEvent(
        var title: String,
        var body: String,
        val image: MultipartBody.Part?
    ): BlogStateEvent( )

    object None : BlogStateEvent()
}