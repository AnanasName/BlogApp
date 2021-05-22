package com.codingwithmitch.openapi.api.main

import android.net.Uri
import androidx.core.net.toUri
import com.codingwithmitch.openapi.api.auth.USERS_COLLECTION
import com.codingwithmitch.openapi.models.AccountProperties
import com.codingwithmitch.openapi.models.BlogPost
import com.codingwithmitch.openapi.repository.util.safeApiCall
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import com.codingwithmitch.openapi.ui.main.account.state.AccountViewState
import com.codingwithmitch.openapi.ui.main.blog.state.BlogViewState
import com.codingwithmitch.openapi.ui.main.blog.state.BlogViewState.*
import com.codingwithmitch.openapi.ui.main.create_blog.CreateBlogViewModel
import com.codingwithmitch.openapi.ui.main.create_blog.state.CreateBlogViewState
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

const val CHANGES_APPLIED = "Changes Applied"
const val BLOG_POSTS_COLLECTION = "blog_posts"
const val SUCCESS_DELETED = "Success Deleted"
const val SUCCESS_UPDATED = "Success Updated"
const val SUCCESS_CREATE = "Success Create"

class MainService(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseReference: StorageReference
) {

    suspend fun getAccountProperties(
        id: String
    ): DataState<AccountViewState> {
        return safeApiCall {
            var result: DataState<AccountViewState> =
                DataState.error(Response("Error occurred", ResponseType.Dialog))

            val accountProperties: AccountProperties? = firebaseFirestore
                .collection(USERS_COLLECTION)
                .document(id)
                .get()
                .addOnFailureListener {
                    result = DataState.error(Response(it.message, ResponseType.Dialog))
                }
                .await()
                .toObject(AccountProperties::class.java)

            if (accountProperties != null) {
                result = DataState.data(
                    data = AccountViewState(
                        accountProperties
                    ),
                    response = Response("Success", ResponseType.None)
                )
            }


            result
        }
    }

    suspend fun saveAccountProperties(
        accountProperties: AccountProperties
    ): DataState<AccountViewState> {

        return safeApiCall {

            var result: DataState<AccountViewState> =
                DataState.error(Response("Error occurred", ResponseType.Dialog))

            firebaseFirestore
                .collection(USERS_COLLECTION)
                .document(accountProperties.pk)
                .set(accountProperties)
                .addOnSuccessListener {
                    result = DataState.data(
                        data = AccountViewState(
                            accountProperties = accountProperties
                        ),
                        response = Response("Account update success", ResponseType.Toast)
                    )
                }
                .addOnFailureListener {
                    result = DataState.error(Response(it.message, ResponseType.Dialog))
                }
                .await()

            result
        }
    }

    suspend fun changePassword(
        email: String,
        oldPassword: String,
        newPassword: String,
        user: FirebaseUser
    ): DataState<AccountViewState> {
        return safeApiCall {
            var result: DataState<AccountViewState> =
                DataState.error(Response("Error occurred", ResponseType.Dialog))

            val credential = EmailAuthProvider.getCredential(email, oldPassword)

            var isCorrectionInfo = false

            user
                .reauthenticate(credential)
                .addOnCompleteListener {
                    isCorrectionInfo = true
                }.await()

            if (isCorrectionInfo) {
                user.updatePassword(newPassword).addOnCompleteListener {
                    result = DataState.data(
                        data = null,
                        response = Response(CHANGES_APPLIED, ResponseType.Toast)
                    )
                }.await()
            }

            result
        }
    }

    suspend fun searchBlogPosts(
        query: String
    ): DataState<BlogViewState> {
        return safeApiCall {

            var queryList: List<BlogPost> = ArrayList()

            var result: DataState<BlogViewState> =
                DataState.error(Response("Error occurred", ResponseType.Dialog))

            var hasErrors = false

            val blogList = firebaseFirestore
                .collection(BLOG_POSTS_COLLECTION)
                .get()
                .addOnFailureListener {
                    hasErrors = true
                    result = DataState.error(Response(it.message, ResponseType.Dialog))
                }
                .await()
                .toObjects(BlogPost::class.java)

            if (!hasErrors) {
                queryList = sortResultWithQuery(query, blogList)
                result = DataState.data(
                    BlogViewState(BlogFields(queryList, query)),
                    Response("Data retrieved success", ResponseType.None)
                )
            }

            queryList.forEach { blogPost ->
                blogPost.authorPk.let { authorPk ->
                    val networkUsername = checkUsername(authorPk)
                    if (!blogPost.username.equals(networkUsername)) {
                        if (networkUsername != null) {
                            blogPost.username = networkUsername
                            changeBlogPostUsername(networkUsername, authorPk)
                        }
                    }
                }
            }


            result
        }
    }

    suspend fun getAllBlogPosts(): DataState<BlogViewState> {
        return safeApiCall {

            var result: DataState<BlogViewState> =
                DataState.error(Response("Error occurred", ResponseType.Dialog))

            var hasErrors = false

            val blogList = firebaseFirestore
                .collection(BLOG_POSTS_COLLECTION)
                .get()
                .addOnFailureListener {
                    hasErrors = true
                    result = DataState.error(Response(it.message, ResponseType.Dialog))
                }
                .await()
                .toObjects(BlogPost::class.java)

            blogList.forEach { blogPost ->
                blogPost.authorPk.let { authorPk ->
                    val networkUsername = checkUsername(authorPk)
                    if (!blogPost.username.equals(networkUsername)) {
                        if (networkUsername != null) {
                            blogPost.username = networkUsername
                            changeBlogPostUsername(networkUsername, authorPk)
                        }
                    }
                }
            }

            if (!hasErrors) {
                result = DataState.data(
                    BlogViewState(BlogFields(blogList)),
                    Response("Data retrieved success", ResponseType.None)
                )
            }

            result
        }
    }

    suspend fun deleteBlogPost(blogPk: String): DataState<BlogViewState> {
        return safeApiCall {

            var result: DataState<BlogViewState> =
                DataState.data(null, Response(SUCCESS_DELETED, ResponseType.Toast))

            firebaseFirestore
                .collection(BLOG_POSTS_COLLECTION)
                .document(blogPk)
                .delete()
                .addOnFailureListener {
                    result = DataState.error(Response("Unsuccessful deleting", ResponseType.Toast))
                }
                .await()


            result
        }
    }

    suspend fun updateBlog(blogPost: BlogPost): DataState<BlogViewState> {
        return safeApiCall {

            var result: DataState<BlogViewState> =
                DataState.data(null, Response(SUCCESS_UPDATED, ResponseType.Toast))

            //Upload New Image
            val url = uploadImage(blogPost.blogPk, blogPost.image.toUri())

            val copyBlogPost = blogPost.copy()

            copyBlogPost.image = url

            firebaseFirestore
                .collection(BLOG_POSTS_COLLECTION)
                .document(blogPost.blogPk)
                .set(copyBlogPost)
                .addOnFailureListener {
                    result = DataState.error(Response("Unsuccessful updating", ResponseType.Toast))
                }
                .await()

            result
        }
    }

    suspend fun createBlog(
        authorId: String,
        title: String,
        body: String,
        image: Uri
    ): DataState<CreateBlogViewState> {
        return safeApiCall {

            val blogPk = UUID.randomUUID().toString()

            //upload Image
            val url = uploadImage(blogPk, image)

            var username = "None"

            username = firebaseFirestore
                .collection(USERS_COLLECTION)
                .document(authorId)
                .get()
                .await()
                .toObject(AccountProperties::class.java)!!.username

            val blogPost = BlogPost(
                blogPk,
                authorId,
                title,
                body,
                url,
                getCurrentDate(),
                username
            )

            var result: DataState<CreateBlogViewState> =
                DataState.data(
                    CreateBlogViewState(
                        CreateBlogViewState.NewBlogFields(
                            blogPost,
                            null,
                            null,
                            null
                        )
                    ),
                    Response(SUCCESS_CREATE, ResponseType.Toast)
                )

            firebaseFirestore
                .collection(BLOG_POSTS_COLLECTION)
                .document(blogPost.blogPk)
                .set(blogPost)
                .addOnFailureListener {
                    result = DataState.error(Response("Unsuccessful creating", ResponseType.Toast))
                }
                .await()

            result
        }
    }

    private suspend fun checkUsername(authorPk: String): String? {

        return firebaseFirestore
            .collection(USERS_COLLECTION)
            .document(authorPk)
            .get()
            .await()
            .toObject(AccountProperties::class.java)?.username

    }

    private suspend fun changeBlogPostUsername(newUsername: String, authorPk: String) {

        val accountProperties = firebaseFirestore
            .collection(USERS_COLLECTION)
            .document(authorPk)
            .get()
            .await()
            .toObject(AccountProperties::class.java)

        accountProperties?.username = newUsername

        firebaseFirestore
            .collection(USERS_COLLECTION)
            .document(authorPk)
            .set(accountProperties!!)
            .await()

    }

    private suspend fun uploadImage(blogPk: String, imageUri: Uri): String {
        val ref = firebaseReference.child("images/$blogPk")
        ref.putFile(imageUri)
            .await()

        return ref.downloadUrl.await().toString()
    }

    private fun sortResultWithQuery(query: String, blogList: List<BlogPost>): List<BlogPost> {
        return blogList.filter {
            it.body.contains(query) || it.title.contains(query) || it.username.contains(query)
        }
    }

    private fun getCurrentDate(): String {
        val currentDate = Date()
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        return sdf.format(currentDate)
    }

}