package com.codingwithmitch.openapi.repository.main

import com.codingwithmitch.openapi.api.main.MainService
import com.codingwithmitch.openapi.models.BlogPost
import com.codingwithmitch.openapi.persistence.BlogPostDao
import com.codingwithmitch.openapi.repository.JobManager
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.main.blog.state.BlogViewState
import javax.inject.Inject

class BlogRepository
@Inject
constructor(
    val mainService: MainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
) : JobManager("BlogRepository") {

    suspend fun searchBlogPosts(
        query: String?
    ): DataState<BlogViewState> {
        if (query.isNullOrBlank())
            return mainService.getAllBlogPosts()

        return mainService.searchBlogPosts(query)
    }

    suspend fun getBlogPostsFromDatabase(
        query: String?
    ): List<BlogPost> {

        if (query.isNullOrBlank())
            return blogPostDao.getAllBlogPosts()

        return blogPostDao.searchBlogPosts(query)
    }

    suspend fun insertBlogPostToDatabase(
        blogPost: BlogPost
    ){
        blogPostDao.insert(blogPost)
    }
}