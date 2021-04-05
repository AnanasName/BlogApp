package com.codingwithmitch.openapi.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.ui.BaseActivity
import com.codingwithmitch.openapi.ui.auth.AuthActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity() {

    lateinit var authListener: AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        subscribeObservers()

        checkIsLogged()

        tool_bar.setOnClickListener {
            sessionManager.logout()
        }
    }

    private fun checkIsLogged() {
        if (!sessionManager.isLogged())
            navAuthActivity()
    }

    fun subscribeObservers() {
        setAuthListener()
    }

    private fun navAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setAuthListener() {
        authListener = AuthStateListener { auth ->
            if (auth.currentUser == null)
                navAuthActivity()
        }

        sessionManager.setAuthListener(authListener)
    }

    override fun displayProgressBar(boolean: Boolean) {
        if (boolean)
            progress_bar.visibility = View.VISIBLE
        else
            progress_bar.visibility = View.INVISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionManager.removeAuthListener(authListener)
    }

}