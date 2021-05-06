package com.codingwithmitch.openapi.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    @Query("DELETE FROM blog_post")
    suspend fun deleteAllBlogPosts()

}