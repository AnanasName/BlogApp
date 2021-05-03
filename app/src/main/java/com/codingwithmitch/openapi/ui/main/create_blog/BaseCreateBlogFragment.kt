package com.codingwithmitch.openapi.ui.main.create_blog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.ui.DataStateChangeListener
import dagger.android.support.DaggerFragment
import java.lang.ClassCastException
import java.lang.Exception

abstract class BaseCreateBlogFragment : DaggerFragment(){

    lateinit var stateChangeListener: DataStateChangeListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBarWithNavController(R.id.createBlogFragment, activity as AppCompatActivity)

        cancelActiveJobs()
    }

    fun cancelActiveJobs(){

    }

    fun setupActionBarWithNavController(fragmentId: Int, activity: AppCompatActivity){
        val appBarConfiguration = AppBarConfiguration(setOf(0))
        NavigationUI.setupActionBarWithNavController(
            activity,
            findNavController(),
            appBarConfiguration
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            stateChangeListener = context as DataStateChangeListener
        }catch (e: ClassCastException){
            Log.e("DEBUG", "$context must implement DataStateChangeListener")
        }
    }


}