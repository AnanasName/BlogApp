package com.codingwithmitch.openapi.di.auth

import com.codingwithmitch.openapi.api.auth.AuthService
import com.codingwithmitch.openapi.persistence.AccountPropertiesDao
import com.codingwithmitch.openapi.persistence.AuthTokenDao
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
        firebaseFirestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): AuthService{
        return AuthService(
            firebaseAuth,
            firebaseFirestore
        )
    }

    @AuthScope
    @Provides
    fun provideAuthRepository(
        sessionManager: SessionManager,
        authTokenDao: AuthTokenDao,
        accountPropertiesDao: AccountPropertiesDao,
        authService: AuthService
    ): AuthRepository{
        return AuthRepository(
            authTokenDao,
            accountPropertiesDao,
            authService,
            sessionManager
        )
    }
}