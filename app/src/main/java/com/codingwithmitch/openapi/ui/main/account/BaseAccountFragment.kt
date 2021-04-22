package com.codingwithmitch.openapi.ui.main.account

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.ui.DataStateChangeListener
import com.codingwithmitch.openapi.ui.main.create_blog.BaseCreateBlogFragment
import dagger.android.support.DaggerFragment
import java.lang.ClassCastException

abstract class BaseAccountFragment : DaggerFragment(){

    lateinit var stateChangeListener: DataStateChangeListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            stateChangeListener = context as DataStateChangeListener
        }catch (e: ClassCastException){
            Log.e("DEBUG", "$context must implement DataStateChangeListener")
        }
    }
}