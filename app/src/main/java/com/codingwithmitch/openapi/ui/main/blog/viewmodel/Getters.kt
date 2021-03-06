package com.codingwithmitch.openapi.ui.main.blog.viewmodel

import com.codingwithmitch.openapi.models.BlogPost

fun BlogViewModel.getFilter(): String{
    getCurrentViewStateOrNew().let {
        return it.blogFields.filter
    }
}

fun BlogViewModel.getBlogPost(): BlogPost{
    getCurrentViewStateOrNew().let {
        return it.viewBlogFields.blogPost!!
    }
}

fun BlogViewModel.isAuthorOfBlogPost(): Boolean{
    getCurrentViewStateOrNew().let {
        return it.viewBlogFields.isAuthorOfBlogPost
    }
}

fun BlogViewModel.getOrder(): String{
    getCurrentViewStateOrNew().let {
        return it.blogFields.order
    }
}

fun BlogViewModel.getPage(): Int{
    return getCurrentViewStateOrNew().blogFields.page
}

fun BlogViewModel.getIsQueryExhausted(): Boolean{
    return getCurrentViewStateOrNew().blogFields.isQueryExhausted
}

fun BlogViewModel.getSearchQuery(): String{
    return getCurrentViewStateOrNew().blogFields.searchQuery
}

fun BlogViewModel.getIsQueryInProgress(): Boolean{
    return getCurrentViewStateOrNew().blogFields.isQueryInProgress
}

fun BlogViewModel.getUpdatedBlogPost(): BlogPost? {
    getCurrentViewStateOrNew().let {
        it.updateBlogFields.blogPost?.let {
            return it
        }
    }
    return null
}
