package com.codingwithmitch.openapi.ui.main.create_blog.state

import android.net.Uri

sealed class CreateBlogStateEvent{

    data class CreateNewBlogEvent(
        val title: String,
        val body: String,
        val image: Uri
    ): CreateBlogStateEvent()

    object None : CreateBlogStateEvent()
}