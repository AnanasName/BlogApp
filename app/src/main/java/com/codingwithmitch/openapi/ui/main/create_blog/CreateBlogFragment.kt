package com.codingwithmitch.openapi.ui.main.create_blog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.ui.main.account.BaseAccountFragment
import com.codingwithmitch.openapi.ui.main.blog.BaseBlogFragment

class CreateBlogFragment : BaseAccountFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}