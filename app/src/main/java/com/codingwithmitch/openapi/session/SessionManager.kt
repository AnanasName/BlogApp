package com.codingwithmitch.openapi.session

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager
@Inject
constructor(
    val application: Application,
    private val firebaseAuth: FirebaseAuth
)
{

    fun logout(){
        firebaseAuth.signOut()
    }

    fun isLogged(): Boolean{
        return firebaseAuth.uid != null
    }

    fun isConnectedToTheInternet(): Boolean{
        val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try{
            return cm.activeNetworkInfo.isConnected
        }catch (e: java.lang.Exception){
            Log.e("DEBUG", "isConnectedToInternet: ${e.message}")
        }
        return false
    }

    fun setAuthListener(listener: FirebaseAuth.AuthStateListener){
        firebaseAuth.addAuthStateListener(listener)
    }

    fun removeAuthListener(listener: FirebaseAuth.AuthStateListener){
        firebaseAuth.removeAuthStateListener(listener)
    }

}
