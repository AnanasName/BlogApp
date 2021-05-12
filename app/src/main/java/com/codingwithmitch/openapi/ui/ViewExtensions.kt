package com.codingwithmitch.openapi.ui

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import com.afollestad.materialdialogs.MaterialDialog
import com.codingwithmitch.openapi.R

fun Context.displayToast(@StringRes message: Int){
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.displayToast(message: String){
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.displaySuccessDialog(message: String?){
    MaterialDialog(this)
        .show {
            title(R.string.text_success)
            message(text = message)
            positiveButton(R.string.text_ok)
        }
}

fun Context.displayErrorDialog(message: String?){
    MaterialDialog(this)
        .show {
            title(R.string.text_error)
            message(text = message)
            positiveButton(R.string.text_ok)
        }
}

fun Activity.displayInfoDialog(message: String?){
    MaterialDialog(this)
        .show {
            title(R.string.text_info)
            message(text = message)
            positiveButton(R.string.text_ok)
        }
}

fun Activity.areYouSureDialog(message: String?, callback: AreYouSureCallback){
    MaterialDialog(this)
        .show {
            title(R.string.are_you_sure)
            message(text = message)
            negativeButton(R.string.text_cancel) {
                callback.cancel()
            }
            positiveButton(R.string.text_yes) {
                callback.proceed()
            }
        }
}


interface AreYouSureCallback{

    fun proceed()

    fun cancel()
}