package com.codingwithmitch.openapi.ui.main.create_blog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.api.main.SUCCESS_CREATE
import com.codingwithmitch.openapi.di.main.MainScope
import com.codingwithmitch.openapi.ui.*
import com.codingwithmitch.openapi.ui.main.create_blog.state.CREATE_BLOG_VIEW_STATE_BUNDLE_KEY
import com.codingwithmitch.openapi.ui.main.create_blog.state.CreateBlogStateEvent.CreateNewBlogEvent
import com.codingwithmitch.openapi.ui.main.create_blog.state.CreateBlogViewState
import com.codingwithmitch.openapi.util.Constants.Companion.GALLERY_REQUEST_CODE
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_create_blog.*
import javax.inject.Inject

const val ERROR_SOMETHING_WRONG_WITH_IMAGE = "Something Wrong With Image"

@MainScope
class CreateBlogFragment
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager
) : BaseCreateBlogFragment(R.layout.fragment_create_blog) {

    val viewModel: CreateBlogViewModel by viewModels {
        viewModelFactory
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        blog_image.setOnClickListener {
            if (stateChangeListener.isStoragePermissionGranted()) {
                pickFromGallery()
            }
        }

        update_textview.setOnClickListener {
            if (stateChangeListener.isStoragePermissionGranted()) {
                pickFromGallery()
            }
        }

        subscribeObservers()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cancelActiveJobs()

        savedInstanceState?.let { inState ->
            (inState[CREATE_BLOG_VIEW_STATE_BUNDLE_KEY] as CreateBlogViewState)?.let { viewState ->
                viewModel.setViewState(viewState)
            }
        }
    }

    override fun cancelActiveJobs() {
        viewModel.cancelActiveJobs()
    }


    override fun onSaveInstanceState(outState: Bundle) {

        outState.putParcelable(
            CREATE_BLOG_VIEW_STATE_BUNDLE_KEY,
            viewModel.viewState.value
        )

        super.onSaveInstanceState(outState)
    }


    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->

            if (dataState != null) {

                stateChangeListener.onDataStateChange(dataState)

                dataState.data?.let { data ->
                    data.response?.let { event ->
                        event.peekContent().let { response ->
                            response.message?.let { message ->
                                if (message.equals(SUCCESS_CREATE)) {
                                    viewModel.clearNewBlogFields()
                                }
                            }
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->

            viewState.blogFields.let { newBlogFields ->
                setBlogProperties(
                    newBlogFields.title,
                    newBlogFields.body,
                    newBlogFields.image.toString()
                )
            }
        })
    }

    private fun setBlogProperties(title: String?, body: String?, image: String?) {
        image?.let {
            requestManager
                .load(image)
                .into(blog_image)
        } ?: setDefaultImage()

        blog_title.setText(title)
        blog_body.setText(body)
    }

    private fun setDefaultImage() {
        requestManager
            .load(R.drawable.default_image)
            .into(blog_image)
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun launchImageCrop(uri: Uri?) {
        context?.let {
            CropImage
                .activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(it, this)
        }
    }

    private fun showErrorDialog(errorMessage: String) {
        stateChangeListener.onDataStateChange(
            DataState(
                Event(
                    StateError(
                        Response(
                            errorMessage, ResponseType.Dialog
                        )
                    )
                ),
                Loading(false),
                Data(Event.dataEvent(null), null)
            )
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                GALLERY_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        activity?.let {
                            launchImageCrop(uri)
                        }
                    } ?: showErrorDialog(ERROR_SOMETHING_WRONG_WITH_IMAGE)
                }

                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    viewModel.setNewImage(resultUri)
                }

                CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> {
                    showErrorDialog(ERROR_SOMETHING_WRONG_WITH_IMAGE)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.setNewBlogFields(
            blog_title.text.toString(),
            blog_body.text.toString(),
            null
        )
    }

    private fun publishNewBlog() {
        viewModel.getImage()?.let { image ->
            viewModel.setStateEvent(
                CreateNewBlogEvent(
                    blog_title.text.toString(),
                    blog_body.text.toString(),
                    image
                )
            )

            stateChangeListener.hideSoftKeyboard()
        } ?: showErrorDialog("Error must select image")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.publish_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.publish -> {
                val callback: AreYouSureCallback = object : AreYouSureCallback {
                    override fun proceed() {
                        publishNewBlog()
                    }

                    override fun cancel() {

                    }
                }
                uiCommunicationListener.onUIMessageReceived(
                    UIMessage(
                        getString(R.string.are_you_sure_publish),
                        UIMessageType.AreYouSureDialog(callback)
                    )
                )
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}