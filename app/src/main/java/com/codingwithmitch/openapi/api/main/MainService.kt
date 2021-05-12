package com.codingwithmitch.openapi.api.main

import android.util.Log
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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.tasks.await

const val CHANGES_APPLIED = "Changes Applied"
const val BLOG_POSTS_COLLECTION = "blog_posts"
const val SUCCESS_DELETED = "Success Deleted"
const val SUCCESS_UPDATED = "Success Updated"

class MainService(
    private val firebaseFirestore: FirebaseFirestore
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

            val queryList: List<BlogPost>

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
                .addOnFailureListener{
                    result = DataState.error(Response("Unsuccessful deleting", ResponseType.Toast))
                }
                .await()


            result
        }
    }

    suspend fun updateBlog(blogPost: BlogPost): DataState<BlogViewState>{
        return safeApiCall {

            //Upload New Image

            var result: DataState<BlogViewState> =
                DataState.data(null, Response(SUCCESS_UPDATED, ResponseType.Toast))

            firebaseFirestore
                .collection(BLOG_POSTS_COLLECTION)
                .document(blogPost.blogPk)
                .set(blogPost)
                .addOnFailureListener{
                    result = DataState.error(Response("Unsuccessful updating", ResponseType.Toast))
                }
                .await()

            result
        }
    }

    private fun sortResultWithQuery(query: String, blogList: List<BlogPost>): List<BlogPost> {
        return blogList.filter {
            it.body.contains(query) || it.title.contains(query) || it.username.contains(query)
        }
    }

}