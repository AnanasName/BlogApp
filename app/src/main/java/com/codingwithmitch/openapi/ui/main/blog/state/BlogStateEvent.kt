package com.codingwithmitch.openapi.ui.main.blog.state

sealed class BlogStateEvent {

    object BlogSearchEvent : BlogStateEvent()

    object CheckAuthorOfBlogPost : BlogStateEvent()

    object None : BlogStateEvent()
}