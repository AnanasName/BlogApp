package com.codingwithmitch.openapi.ui.main.blog.state

sealed class BlogStateEvent {

    object BlogSearchEvent : BlogStateEvent()

    object None : BlogStateEvent()
}