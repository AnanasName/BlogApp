package com.codingwithmitch.openapi.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.codingwithmitch.openapi.models.AccountProperties
import com.codingwithmitch.openapi.models.AuthToken

@Database(entities = [AccountProperties::class], version = 3)
abstract class AppDatabase: RoomDatabase() {

    abstract fun getAccountPropertiesDao(): AccountPropertiesDao

    companion object{

        const val DATABASE_NAME = "app_db"
    }

}