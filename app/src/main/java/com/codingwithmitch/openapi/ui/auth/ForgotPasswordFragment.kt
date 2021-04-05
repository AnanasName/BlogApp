package com.codingwithmitch.openapi.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.di.auth.state.AuthStateEvent
import com.codingwithmitch.openapi.di.auth.state.ResetPasswordFields
import kotlinx.android.synthetic.main.fragment_forgot_password.*

class ForgotPasswordFragment : BaseAuthFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeObservers()

        sent_message_button.setOnClickListener {
            resetPassword()
        }
    }

    fun subscribeObservers(){
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            it.resetPasswordFields?.let { resetPasswordFields ->
                resetPasswordFields.reset_email?.let { input_email.setText(it) }
            }
        })
    }

    fun resetPassword(){
        viewModel.setStateEvent(
            AuthStateEvent.ResetPasswordAttemptEvent(
                input_email.text.toString()
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.setResetPasswordFields(
            ResetPasswordFields(
                input_email.text.toString()
            )
        )

    }
}