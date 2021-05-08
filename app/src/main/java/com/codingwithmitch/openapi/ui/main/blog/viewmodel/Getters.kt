package com.codingwithmitch.openapi.ui.main.blog.viewmodel

fun BlogViewModel.getFilter(): String{
    getCurrentViewStateOrNew().let {
        return it.blogFields.filter
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