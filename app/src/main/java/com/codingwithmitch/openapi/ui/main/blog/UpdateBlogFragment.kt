package com.codingwithmitch.openapi.ui.main.blog

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.api.main.SUCCESS_UPDATED
import com.codingwithmitch.openapi.models.BlogPost
import com.codingwithmitch.openapi.ui.main.blog.state.BlogStateEvent
import com.codingwithmitch.openapi.ui.main.blog.viewmodel.getBlogPost
import com.codingwithmitch.openapi.ui.main.blog.viewmodel.setUpdatedBlogPost
import kotlinx.android.synthetic.main.fragment_create_blog.*
import kotlinx.android.synthetic.main.fragment_update_blog.*
import kotlinx.android.synthetic.main.fragment_update_blog.blog_body
import kotlinx.android.synthetic.main.fragment_update_blog.blog_image
import kotlinx.android.synthetic.main.fragment_update_blog.blog_title
import kotlinx.android.synthetic.main.fragment_update_blog.update_textview

class UpdateBlogFragment : BaseBlogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_update_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

//        blog_image.setOnClickListener {
//            if (stateChangeListener.isStoragePermissionGranted()){
//                pickFromGallery()
//            }
//        }
//
//        update_textview.setOnClickListener {
//            if (stateChangeListener.isStoragePermissionGranted()){
//                pickFromGallery()
//            }
//        }

        subscribeObservers()
    }

    private fun subscribeObservers(){
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->

            stateChangeListener.onDataStateChange(dataState)

            dataState.data?.let { data ->
                data.response?.let { event ->
                    if (event.peekContent().message.equals(SUCCESS_UPDATED)){
                        findNavController().popBackStack()
                    }
                }
            }

            viewModel.viewState.observe(viewLifecycleOwner, Observer{ viewState ->
                viewState.updateBlogFields.let { updateBlogFields ->
                    setBlog(
                        updateBlogFields.blogPost!!
                    )
                }
            })
        })
    }

    private fun setBlog(blogPost: BlogPost) {
        requestManager
            .load(blogPost.image)
            .into(blog_image)
        blog_title.setText(blogPost.title)
        blog_body.setText(blogPost.body)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save -> {
                saveChanges()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun saveChanges() {
        viewModel.setStateEvent(
            BlogStateEvent.UpdatedBlogPostEvent(
                blog_title.text.toString(),
                blog_body.text.toString(),
                null
            )
        )
    }

    override fun onPause() {
        super.onPause()
        val blogPost = viewModel.getBlogPost().copy()
        blogPost.title = blog_title.text.toString()
        blogPost.body = blog_body.text.toString()
        viewModel.setUpdatedBlogPost(
            blogPost
        )
    }
}