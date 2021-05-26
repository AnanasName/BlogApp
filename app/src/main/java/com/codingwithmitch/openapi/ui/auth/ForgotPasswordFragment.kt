package com.codingwithmitch.openapi.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.di.auth.AuthScope
import com.codingwithmitch.openapi.ui.auth.state.AuthStateEvent
import com.codingwithmitch.openapi.ui.auth.state.ResetPasswordFields
import kotlinx.android.synthetic.main.fragment_forgot_password.*
import javax.inject.Inject

@AuthScope
class ForgotPasswordFragment
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : Fragment(R.layout.fragment_forgot_password) {

    val viewModel: AuthViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.cancelJobs()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeObservers()

        sent_message_button.setOnClickListener {
            resetPassword()
        }
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            it.resetPasswordFields?.let { resetPasswordFields ->
                resetPasswordFields.reset_email?.let { input_email.setText(it) }
            }
        })
    }

    private fun resetPassword() {
        viewModel.setStateEvent(
            AuthStateEvent.ResetPasswordAttemptEvent(
                input_email.text.toString()
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setResetPasswordFields(
            ResetPasswordFields(
                input_email.text.toString()
            )
        )
    }
}