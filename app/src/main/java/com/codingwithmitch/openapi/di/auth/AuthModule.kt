package com.codingwithmitch.openapi.di.auth

import android.content.SharedPreferences
import com.codingwithmitch.openapi.api.auth.AuthService
import com.codingwithmitch.openapi.persistence.AccountPropertiesDao
import com.codingwithmitch.openapi.repository.auth.AuthRepository
import com.codingwithmitch.openapi.session.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides

@Module
class AuthModule {

    @AuthScope
    @Provides
    fun provideApiService(
        accountPropertiesDao: AccountPropertiesDao,
        firebaseFirestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        sharedPreferences: SharedPreferences,
        sharedPrefsEditor: SharedPreferences.Editor
    ): AuthService{
        return AuthService(
            firebaseAuth,
            firebaseFirestore,
            accountPropertiesDao,
            sharedPreferences,
            sharedPrefsEditor
        )
    }

    @AuthScope
    @Provides
    fun provideAuthRepository(
        sessionManager: SessionManager,
        authService: AuthService
    ): AuthRepository{
        return AuthRepository(
            authService,
            sessionManager
        )
    }
}