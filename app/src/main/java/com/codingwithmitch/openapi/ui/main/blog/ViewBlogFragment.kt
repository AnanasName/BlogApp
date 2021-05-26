package com.codingwithmitch.openapi.ui.main.blog

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.api.main.SUCCESS_DELETED
import com.codingwithmitch.openapi.di.main.MainScope
import com.codingwithmitch.openapi.models.BlogPost
import com.codingwithmitch.openapi.ui.AreYouSureCallback
import com.codingwithmitch.openapi.ui.UIMessage
import com.codingwithmitch.openapi.ui.UIMessageType
import com.codingwithmitch.openapi.ui.main.blog.state.BLOG_VIEW_STATE_BUNDLE_KEY
import com.codingwithmitch.openapi.ui.main.blog.state.BlogStateEvent.CheckAuthorOfBlogPost
import com.codingwithmitch.openapi.ui.main.blog.state.BlogStateEvent.DeleteBlogPostEvent
import com.codingwithmitch.openapi.ui.main.blog.state.BlogViewState
import com.codingwithmitch.openapi.ui.main.blog.viewmodel.*
import kotlinx.android.synthetic.main.fragment_view_blog.*
import javax.inject.Inject

@MainScope
class ViewBlogFragment
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager
) : BaseBlogFragment(R.layout.fragment_view_blog) {

    val viewModel: BlogViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cancelActiveJobs()

        savedInstanceState?.let { inState ->
            (inState[BLOG_VIEW_STATE_BUNDLE_KEY] as BlogViewState)?.let { viewState ->
                viewModel.setViewState(viewState)
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {

        val viewState = viewModel.viewState.value

        viewState?.blogFields?.blogList = ArrayList()

        outState.putParcelable(
            BLOG_VIEW_STATE_BUNDLE_KEY,
            viewState
        )

        super.onSaveInstanceState(outState)
    }

    override fun cancelActiveJobs() {
        viewModel.cancelActiveJobs()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
        checkIsAuthorOfBlogPost()
        stateChangeListener.expandAppbar()

        delete_button.setOnClickListener {
            confirmDeleteRequest()
        }
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->

            if (dataState != null) {

                stateChangeListener.onDataStateChange(dataState)
                dataState.data?.let { data ->
                    data.data?.peekContent()?.let { viewState ->
                        viewModel.setIsAuthorOfBlogPost(
                            viewState.viewBlogFields.isAuthorOfBlogPost
                        )
                    }

                    data.response?.peekContent()?.let { response ->
                        if (response.message.equals(SUCCESS_DELETED)) {
                            viewModel.removeDeletedBlogPost()
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.viewBlogFields.blogPost?.let { blogPost ->
                setBlogProperties(blogPost)
            }

            if (viewState.viewBlogFields.isAuthorOfBlogPost) {
                adaptViewToAuthorMode()
            }
        })
    }

    private fun checkIsAuthorOfBlogPost() {
        viewModel.setStateEvent(CheckAuthorOfBlogPost)
    }

    private fun setBlogProperties(blogPost: BlogPost) {
        requestManager
            .load(blogPost.image)
            .into(blog_image)

        blog_title.setText(blogPost.title)
        blog_author.setText(blogPost.username)
        blog_update_date.setText(blogPost.date_updated.toString())
        blog_body.setText(blogPost.body)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (viewModel.isAuthorOfBlogPost())
            inflater.inflate(R.menu.edit_view_menu, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun confirmDeleteRequest() {
        val callback: AreYouSureCallback = object : AreYouSureCallback {
            override fun proceed() {
                deleteBlogPost()
            }

            override fun cancel() {

            }
        }

        uiCommunicationListener.onUIMessageReceived(
            UIMessage(
                getString(R.string.are_you_sure_delete),
                UIMessageType.AreYouSureDialog(callback)
            )
        )
    }

    private fun deleteBlogPost() {
        viewModel.setStateEvent(
            DeleteBlogPostEvent
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (viewModel.isAuthorOfBlogPost()) {
            when (item.itemId) {
                R.id.edit -> {
                    navUpdateBlogFragment()
                    return true
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun navUpdateBlogFragment() {
        try {
            viewModel.setUpdatedBlogPost(
                viewModel.getBlogPost()
            )
            findNavController().navigate(R.id.action_viewBlogFragment_to_updateBlogFragment)
        } catch (e: Exception) {
            Log.e(TAG, "Exception:${e.message}")
        }
    }

    private fun adaptViewToAuthorMode() {
        activity?.invalidateOptionsMenu()
        delete_button.visibility = View.VISIBLE
    }
}