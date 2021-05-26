package com.codingwithmitch.openapi.ui.main.blog.state

import android.net.Uri

sealed class BlogStateEvent {

    object BlogSearchEvent : BlogStateEvent()

    object RestoreBlogList: BlogStateEvent()

    object CheckAuthorOfBlogPost : BlogStateEvent()

    object DeleteBlogPostEvent: BlogStateEvent()

    data class UpdatedBlogPostEvent(
        var title: String,
        var body: String,
        val image: Uri?
    ): BlogStateEvent( )

    object None : BlogStateEvent()
}