package com.codingwithmitch.openapi.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "blog_post")
data class BlogPost(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name="blogPk")
    var blogPk: String,

    @ColumnInfo(name = "authorPk")
    var authorPk: String,

    @ColumnInfo(name = "title")
    var title: String,

    @ColumnInfo(name = "body")
    var body: String,

    @ColumnInfo(name = "image")
    var image: String,

    @ColumnInfo(name = "date_updated")
    var date_updated: String,

    @ColumnInfo(name = "username")
    var username: String
) : Parcelable {
    constructor(): this(
        "",
        "",
        "",
        "",
        "",
        "",
        ""
    )

    override fun toString(): String {
        return "BlogPost(authorPk=$authorPk, " +
                "blogPk='$blogPk', " +
                "title='$title', " +
                "image='$image', " +
                "date_updated=$date_updated, " +
                "username='$username')"
    }
}