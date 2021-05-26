package com.codingwithmitch.openapi.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.navigation.NavController
import com.bumptech.glide.RequestManager
import com.codingwithmitch.openapi.BaseApplication
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.ui.BaseActivity
import com.codingwithmitch.openapi.ui.auth.AuthActivity
import com.codingwithmitch.openapi.ui.main.account.BaseAccountFragment
import com.codingwithmitch.openapi.ui.main.account.ChangePasswordFragment
import com.codingwithmitch.openapi.ui.main.account.UpdateAccountFragment
import com.codingwithmitch.openapi.ui.main.blog.BaseBlogFragment
import com.codingwithmitch.openapi.ui.main.blog.UpdateBlogFragment
import com.codingwithmitch.openapi.ui.main.blog.ViewBlogFragment
import com.codingwithmitch.openapi.ui.main.create_blog.BaseCreateBlogFragment
import com.codingwithmitch.openapi.util.BOTTOM_NAV_BACKSTACK_KEY
import com.codingwithmitch.openapi.util.BottomNavController
import com.codingwithmitch.openapi.util.BottomNavController.*
import com.codingwithmitch.openapi.util.setupNavigation
import com.codingwithmitch.openapi.viewmodels.AuthViewModelFactory
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject
import javax.inject.Named


class MainActivity : BaseActivity(),
    OnNavigationGraphChanged,
    OnNavigationReselectedListener{

    @Inject
    @Named("AccountFragmentFactory")
    lateinit var accountFragmentFactory: FragmentFactory

    @Inject
    @Named("BlogFragmentFactory")
    lateinit var blogFragmentFactory: FragmentFactory

    @Inject
    @Named("CreateBlogFragmentFactory")
    lateinit var createBlogFragmentFactory: FragmentFactory

    override fun expandAppbar() {
        findViewById<AppBarLayout>(R.id.app_bar).setExpanded(true)
    }

    private lateinit var bottomNavigationView: BottomNavigationView

    private val bottomNavController by lazy(LazyThreadSafetyMode.NONE) {
        BottomNavController(
            this,
            R.id.main_nav_host_fragment,
            R.id.menu_nav_blog,
            this
        )
    }

    lateinit var authListener: AuthStateListener

    override fun inject() {
        (application as BaseApplication).mainComponent()
            .inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBar()
        setupBottomNavigationView(savedInstanceState)

        subscribeObservers()

        checkIsLogged()
    }

    private fun setupBottomNavigationView(savedInstanceState: Bundle?){
        bottomNavigationView = findViewById(R.id.bottom_navigation_view)
        bottomNavigationView.setupNavigation(bottomNavController, this)
        if (savedInstanceState == null) {
            bottomNavController.setupBottomNavigationBackStack(null)
            bottomNavController.onNavigationItemSelected()
        }else{
            (savedInstanceState[BOTTOM_NAV_BACKSTACK_KEY] as IntArray?)?.let {items ->
                val backstack = BackStack()
                backstack.addAll(items.toTypedArray())
                bottomNavController.setupBottomNavigationBackStack(backstack)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putIntArray(BOTTOM_NAV_BACKSTACK_KEY, bottomNavController.navigationBackStack.toIntArray())
        super.onSaveInstanceState(outState)
    }

    private fun checkIsLogged() {
        if (!sessionManager.isLogged())
            navAuthActivity()
    }

    fun subscribeObservers() {
        setAuthListener()
    }

    private fun navAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
        (application as BaseApplication).releaseMainComponent()
    }

    private fun setAuthListener() {
        authListener = AuthStateListener { auth ->
            if (auth.currentUser == null)
                navAuthActivity()
        }

        sessionManager.setAuthListener(authListener)
    }

    override fun displayProgressBar(boolean: Boolean) {
        if (boolean)
            progress_bar.visibility = View.VISIBLE
        else
            progress_bar.visibility = View.INVISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionManager.removeAuthListener(authListener)
    }

    private fun setupActionBar(){
        setSupportActionBar(tool_bar)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() = bottomNavController.onBackPressed()

    override fun onGraphChange() {
        expandAppbar()
        cancelActiveJobs()
    }

    private fun cancelActiveJobs(){
        val fragments = bottomNavController.fragmentManager
            .findFragmentById(bottomNavController.containerId)
            ?.childFragmentManager
            ?.fragments

        if (fragments != null){
            for (fragment in fragments){
                when(fragment){
                    is BaseBlogFragment -> fragment.cancelActiveJobs()
                    is BaseAccountFragment -> fragment.cancelActiveJobs()
                    is BaseCreateBlogFragment -> fragment.cancelActiveJobs()
                }
            }
        }
        displayProgressBar(false)
    }

    override fun onReselectItem(navController: NavController, fragment: Fragment) = when(fragment){

        is ViewBlogFragment -> {
            navController.navigate(R.id.action_viewBlogFragment_to_blogFragment)
        }

        is UpdateBlogFragment -> {
            navController.navigate(R.id.action_updateBlogFragment_to_blogFragment)
        }

        is UpdateAccountFragment -> {
            navController.navigate(R.id.action_updateAccountFragment_to_accountFragment)
        }

        is ChangePasswordFragment -> {
            navController.navigate(R.id.action_changePasswordFragment_to_accountFragment)
        }

        else -> {

        }
    }

}