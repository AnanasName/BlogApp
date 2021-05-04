package com.codingwithmitch.openapi.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.codingwithmitch.openapi.models.BlogPost

@Dao
interface BlogPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blogPost: BlogPost): Long

    @Query("SELECT * FROM blog_post")
    suspend fun getAllBlogPosts(): List<BlogPost>

    @Query("""
        SELECT * FROM blog_post 
        WHERE title LIKE '%' || :query || '%' 
        OR username LIKE '%' || :query || '%'
        OR body LIKE '%' || :query || '%'
        """)
    suspend fun searchBlogPosts(
        query: String
    ): List<BlogPost>

    @Query("DELETE FROM blog_post")
    suspend fun deleteAllBlogPosts()

}