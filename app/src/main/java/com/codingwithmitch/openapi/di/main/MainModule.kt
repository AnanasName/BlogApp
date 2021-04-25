package com.codingwithmitch.openapi.di.main

import com.codingwithmitch.openapi.api.main.MainService
import com.codingwithmitch.openapi.persistence.AccountPropertiesDao
import com.codingwithmitch.openapi.repository.main.AccountRepository
import com.codingwithmitch.openapi.session.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides

@Module
class MainModule {

    @MainScope
    @Provides
    fun provideMainApiService(
        firebaseFirestore: FirebaseFirestore
    ): MainService{
        return MainService(firebaseFirestore)
    }

    @MainScope
    @Provides
    fun provideMainRepository(
        mainService: MainService,
        accountPropertiesDao: AccountPropertiesDao,
        sessionManager: SessionManager
    ): AccountRepository{
        return AccountRepository(
            mainService,
            accountPropertiesDao,
            sessionManager
        )
    }
}