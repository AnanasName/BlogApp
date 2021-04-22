package com.codingwithmitch.openapi.ui.main.create_blog

import android.content.Context
import android.util.Log
import com.codingwithmitch.openapi.ui.DataStateChangeListener
import dagger.android.support.DaggerFragment
import java.lang.ClassCastException
import java.lang.Exception

abstract class BaseCreateBlogFragment : DaggerFragment(){

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