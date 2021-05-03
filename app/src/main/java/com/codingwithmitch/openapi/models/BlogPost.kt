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
    @ColumnInfo(name = "pk")
    var pk: String,

    @ColumnInfo(name = "title")
    var title: String,

    @ColumnInfo(name = "body")
    var body: String,

    @ColumnInfo(name = "image")
    var image: String,

    @ColumnInfo(name = "date_updated")
    var date_updated: Long,

    @ColumnInfo(name = "username")
    var username: String
) : Parcelable {
    constructor(): this(
        "",
        "",
        "",
        "",
        0L,
        ""
    )

    override fun toString(): String {
        return "BlogPost(pk=$pk, " +
                "title='$title', " +
                "image='$image', " +
                "date_updated=$date_updated, " +
                "username='$username')"
    }
}