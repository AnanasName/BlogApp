package com.codingwithmitch.openapi.repository.main

import android.net.Uri
import com.codingwithmitch.openapi.api.main.MainService
import com.codingwithmitch.openapi.models.AuthToken
import com.codingwithmitch.openapi.models.BlogPost
import com.codingwithmitch.openapi.persistence.BlogPostDao
import com.codingwithmitch.openapi.repository.JobManager
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.main.blog.state.BlogViewState
import com.codingwithmitch.openapi.ui.main.create_blog.state.CreateBlogViewState
import javax.inject.Inject

class CreateBlogRepository
@Inject
constructor(
    val mainService: MainService,
    val blogPostPostDao: BlogPostDao,
    val sessionManager: SessionManager
) : JobManager("CreateBlogRepository") {

    suspend fun createNewBlogPost(
        title: String,
        body: String,
        image: Uri
    ): DataState<CreateBlogViewState> {
        return mainService.createBlog(
            sessionManager.getId()!!,
            title,
            body,
            image
        )
    }

    suspend fun insertNewBlog(
        blogPost: BlogPost
    ){
        blogPostPostDao.insert(blogPost)
    }

}