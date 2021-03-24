package com.codingwithmitch.openapi.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account_properties")
data class AccountProperties(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "pk")
    var pk: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "username")
    var username: String
)