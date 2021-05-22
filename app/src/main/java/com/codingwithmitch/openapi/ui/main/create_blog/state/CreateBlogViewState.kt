package com.codingwithmitch.openapi.ui.main.create_blog.state

import android.net.Uri
import android.os.Parcelable
import com.codingwithmitch.openapi.models.BlogPost
import kotlinx.android.parcel.Parcelize

const val CREATE_BLOG_VIEW_STATE_BUNDLE_KEY = "openapi.state.CreateBlogViewState"

@Parcelize
class CreateBlogViewState(

    var blogFields: NewBlogFields = NewBlogFields(null, null, null, null)
) : Parcelable {
    @Parcelize
    data class NewBlogFields(
        var blogPost: BlogPost?,
        var image: Uri?,
        var title: String?,
        var body: String?
    ) : Parcelable
}