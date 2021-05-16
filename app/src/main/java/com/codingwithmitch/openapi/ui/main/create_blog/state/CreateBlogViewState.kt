package com.codingwithmitch.openapi.ui.main.create_blog.state

import android.net.Uri
import com.codingwithmitch.openapi.models.BlogPost

class CreateBlogViewState(

    var blogFields: NewBlogFields = NewBlogFields(null, null)
) {
    data class NewBlogFields(
        var blogPost: BlogPost?,
        var image: Uri?
    )
}