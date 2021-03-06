package com.codingwithmitch.openapi.di.main

import com.codingwithmitch.openapi.api.main.MainService
import com.codingwithmitch.openapi.persistence.AccountPropertiesDao
import com.codingwithmitch.openapi.persistence.AppDatabase
import com.codingwithmitch.openapi.persistence.BlogPostDao
import com.codingwithmitch.openapi.repository.main.AccountRepository
import com.codingwithmitch.openapi.repository.main.BlogRepository
import com.codingwithmitch.openapi.repository.main.CreateBlogRepository
import com.codingwithmitch.openapi.session.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.Module
import dagger.Provides

@Module
object MainModule {

    @JvmStatic
    @MainScope
    @Provides
    fun provideMainApiService(
        firebaseFirestore: FirebaseFirestore,
        firebaseReference: StorageReference
    ): MainService {
        return MainService(firebaseFirestore, firebaseReference)
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideAccountRepository(
        mainService: MainService,
        accountPropertiesDao: AccountPropertiesDao,
        sessionManager: SessionManager
    ): AccountRepository {
        return AccountRepository(
            mainService,
            accountPropertiesDao,
            sessionManager
        )
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideStorageReference(
        firebaseStorage: FirebaseStorage
    ): StorageReference{
        return firebaseStorage.getReference()
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideBlogPostDao(db: AppDatabase): BlogPostDao {
        return db.getBlogDao()
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideBlogRepository(
        mainService: MainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): BlogRepository {
        return BlogRepository(mainService, blogPostDao, sessionManager)
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideCreateBlogRepository(
        mainService: MainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): CreateBlogRepository {
        return CreateBlogRepository(
            mainService,
            blogPostDao,
            sessionManager
        )
    }
}