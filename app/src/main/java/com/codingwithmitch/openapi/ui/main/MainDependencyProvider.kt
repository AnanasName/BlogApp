package com.codingwithmitch.openapi.ui.main

import com.bumptech.glide.RequestManager
import com.codingwithmitch.openapi.viewmodels.AuthViewModelFactory

interface MainDependencyProvider {

    fun getViewModelProviderFactory(): AuthViewModelFactory

    fun getGlideRequestManager(): RequestManager
}