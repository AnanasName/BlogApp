package com.codingwithmitch.openapi.persistence

import androidx.room.*
import com.codingwithmitch.openapi.models.BlogPost
import com.codingwithmitch.openapi.util.Constants.Companion.PAGINATION_PAGE_SIZE

@Dao
interface BlogPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blogPost: BlogPost): Long

    @Query("""
        SELECT * FROM blog_post
        LIMIT (:page * :pageSize)
        """
    )
    suspend fun getAllBlogPosts(
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): List<BlogPost>

    @Query("""
        UPDATE blog_post SET title = :title,
        body = :body,
        image = :image
        WHERE blogPk = :blogPk
    """)
    suspend fun updateBlogPost(blogPk: String, title: String, body: String, image: String)

    @Query(
        """
        SELECT * FROM blog_post 
        WHERE title LIKE '%' || :query || '%'
        OR body LIKE '%' || :query || '%'
        OR username LIKE '%' || :query || '%'
        LIMIT (:page * :pageSize)
        """
    )
    suspend fun searchBlogPosts(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): List<BlogPost>

    @Delete
    suspend fun deleteBlogPost(blogPost: BlogPost)

    @Query(
        """
        SELECT * FROM blog_post 
        WHERE title LIKE '%' || :query || '%'
        OR body LIKE '%' || :query || '%'
        OR username LIKE '%' || :query || '%'
        ORDER BY date_updated DESC  
        LIMIT (:page * :pageSize)
        """
    )
    suspend fun searchBlogPostsOrderByDateDESC(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): List<BlogPost>

    @Query(
        """
        SELECT * FROM blog_post 
        WHERE title LIKE '%' || :query || '%'
        OR body LIKE '%' || :query || '%'
        OR username LIKE '%' || :query || '%'
        ORDER BY date_updated ASC  
        LIMIT (:page * :pageSize)
        """
    )
    suspend fun searchBlogPostsOrderByDateASC(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): List<BlogPost>

    @Query(
        """
        SELECT * FROM blog_post 
        WHERE title LIKE '%' || :query || '%'
        OR body LIKE '%' || :query || '%'
        OR username LIKE '%' || :query || '%'
        ORDER BY username DESC  
        LIMIT (:page * :pageSize)
        """
    )
    suspend fun searchBlogPostsOrderByUsernameDESC(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): List<BlogPost>

    @Query(
        """
        SELECT * FROM blog_post 
        WHERE title LIKE '%' || :query || '%'
        OR body LIKE '%' || :query || '%'
        OR username LIKE '%' || :query || '%'
        ORDER BY username ASC  
        LIMIT (:page * :pageSize)
        """
    )
    suspend fun searchBlogPostsOrderByUsernameASC(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): List<BlogPost>

    @Query("DELETE FROM blog_post")
    suspend fun deleteAllBlogPosts()

}