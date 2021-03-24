package com.codingwithmitch.openapi.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.ui.BaseActivity
import com.codingwithmitch.openapi.ui.ResponseType
import com.codingwithmitch.openapi.ui.main.MainActivity
import com.codingwithmitch.openapi.viewmodels.ViewModelProviderFactory
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class AuthActivity : BaseActivity() {

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        viewModel = ViewModelProvider(this, providerFactory).get(AuthViewModel::class.java)

        subscribeObservers()

        checkIsLogged()
    }

    fun subscribeObservers() {
        viewModel.dataState.observe(this, Observer { dataState ->

            dataState.data?.let { data ->

                data.data?.let { event ->
                    event.getContentIfNotHandled()?.let {
                        it.authToken?.let {
                            viewModel.setAuthToken(it)
                            checkIsLogged()
                        }
                    }
                }

                data.response?.let { event ->
                    event.getContentIfNotHandled()?.let {
                        when (it.responseType) {

                            is ResponseType.Dialog -> {

                            }

                            is ResponseType.Toast -> {

                            }

                            is ResponseType.None -> {

                            }
                        }
                    }
                }
            }
        })

    }

    fun checkIsLogged() {
        if (sessionManager.isLogged())
            navMainActivity()
    }

    private fun navMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}