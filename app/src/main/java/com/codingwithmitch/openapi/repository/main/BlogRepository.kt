package com.codingwithmitch.openapi.repository.main

import com.codingwithmitch.openapi.api.main.MainService
import com.codingwithmitch.openapi.models.BlogPost
import com.codingwithmitch.openapi.persistence.BlogPostDao
import com.codingwithmitch.openapi.persistence.returnOrderedBlogQuery
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

    suspend fun getBlogPostsFromNetwork(
        query: String?
    ): DataState<BlogViewState> {
        if (query.isNullOrBlank()) {
            return mainService.getAllBlogPosts()
        }

        return mainService.searchBlogPosts(query)
    }

    suspend fun getBlogPostsFromDatabase(
        query: String?,
        page: Int,
        filterAndOrder: String
    ): List<BlogPost> {

        if (query.isNullOrBlank())
            return blogPostDao.returnOrderedBlogQuery("", filterAndOrder, page)

        return blogPostDao.returnOrderedBlogQuery(query, filterAndOrder, page)
    }

    suspend fun insertBlogPostToDatabase(
        blogPost: BlogPost
    ){
        blogPostDao.insert(blogPost)
    }

    suspend fun deleteBlogPostFromDatabase(
        blogPost: BlogPost
    ){
        blogPostDao.deleteBlogPost(blogPost)
    }

    suspend fun deleteBlogPostFromNetwork(
        blogPost: BlogPost
    ): DataState<BlogViewState> {
        return mainService.deleteBlogPost(blogPost.blogPk)
    }
}