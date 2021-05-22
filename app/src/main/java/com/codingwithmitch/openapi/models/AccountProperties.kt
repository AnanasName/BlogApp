package com.codingwithmitch.openapi.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "account_properties")
data class AccountProperties(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "pk")
    var pk: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "username")
    var username: String
) : Parcelable {
    constructor(): this(
        "",
        "",
        ""
    )
}