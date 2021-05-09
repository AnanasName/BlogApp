package com.codingwithmitch.openapi.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.codingwithmitch.openapi.models.AccountProperties
import com.codingwithmitch.openapi.models.AuthToken
import com.codingwithmitch.openapi.models.BlogPost

@Database(entities = [AccountProperties::class, BlogPost::class], version = 6)
abstract class AppDatabase: RoomDatabase() {

    abstract fun getAccountPropertiesDao(): AccountPropertiesDao

    abstract fun getBlogDao(): BlogPostDao

    companion object{

        const val DATABASE_NAME = "app_db"
    }

}