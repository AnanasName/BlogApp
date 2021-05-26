  package com.codingwithmitch.openapi.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Parcelable
import androidx.annotation.IdRes
import androidx.annotation.NavigationRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.fragments.main.account.AccountNavHostFragment
import com.codingwithmitch.openapi.fragments.main.blog.BlogNavHostFragment
import com.codingwithmitch.openapi.fragments.main.create_blog.CreateBlogNavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.parcel.Parcelize

const val BOTTOM_NAV_BACKSTACK_KEY = "openapi.util.BottomNavController.BackStack"

class BottomNavController(
    val context: Context,
    @IdRes val containerId: Int,
    @IdRes val appStartDestinationId: Int,
    val graphChangeListener: OnNavigationGraphChanged?
) {

    lateinit var activity: Activity
    lateinit var fragmentManager: FragmentManager
    lateinit var navItemChangeListener: OnNavigationItemChanged
    lateinit var navigationBackStack: BackStack

    init {
        if (context is FragmentActivity){
            activity = context
            fragmentManager = (activity as FragmentActivity).supportFragmentManager
        }
    }

    fun setupBottomNavigationBackStack(previousBackStack: BackStack?){
        navigationBackStack = previousBackStack?.let {
            it
        } ?: BackStack.of(appStartDestinationId)
    }

    fun onNavigationItemSelected(itemId: Int = navigationBackStack.last()): Boolean{

        val fragment = fragmentManager.findFragmentByTag(itemId.toString())
            ?: createNavHost(itemId)

        fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out
            )
            .replace(containerId, fragment, itemId.toString())
            .addToBackStack(null)
            .commit()

        navigationBackStack.moveLast(itemId)

        navItemChangeListener.onItemChanged(itemId)

        graphChangeListener?.onGraphChange()

        return true
    }

    private fun createNavHost(menuItemId: Int) =
        when(menuItemId){

            R.id.menu_nav_account -> {
                AccountNavHostFragment.create(R.navigation.nav_account)
            }

            R.id.menu_nav_blog -> {
                BlogNavHostFragment.create(R.navigation.nav_blog)
            }

            R.id.menu_nav_create_blog -> {
                CreateBlogNavHostFragment.create(R.navigation.nav_create_blog)
            }

            else -> {
                BlogNavHostFragment.create(R.navigation.nav_blog)
            }
        }

    @SuppressLint("RestrictedApi")
    fun onBackPressed(){
        val navController: NavController = fragmentManager.findFragmentById(containerId)!!
            .findNavController()

        when{
            navController.backStack.size > 2 -> {
                navController.popBackStack()
            }

            navigationBackStack.size > 1 -> {

                navigationBackStack.removeLast()

                onNavigationItemSelected()
            }

            navigationBackStack.last() != appStartDestinationId -> {
                navigationBackStack.removeLast()
                navigationBackStack.add(0, appStartDestinationId)
                onNavigationItemSelected()
            }

            else -> activity.finish()
        }
    }

    @Parcelize
    class BackStack: ArrayList<Int>(), Parcelable {

        companion object{

            fun of(vararg elements: Int): BackStack{
                val b = BackStack()
                b.addAll(elements.toTypedArray())
                return b
            }
        }

        fun removeLast() = removeAt(size - 1)

        fun moveLast(item: Int){
            remove(item)
            add(item)
        }
    }

    interface OnNavigationItemChanged{
        fun onItemChanged(itemId: Int)
    }

    fun setOnItemNavigationChanged(listener: (itemId: Int) -> Unit){
        this.navItemChangeListener = object : OnNavigationItemChanged {
            override fun onItemChanged(itemId: Int) {
                listener.invoke(itemId)
            }
        }
    }

    interface OnNavigationGraphChanged{
        fun onGraphChange()
    }

    interface OnNavigationReselectedListener{

        fun onReselectItem(navController: NavController, fragment: Fragment)
    }
}

fun BottomNavigationView.setupNavigation(
    bottomNavController: BottomNavController,
    onReselectListener: BottomNavController.OnNavigationReselectedListener
){
    setOnNavigationItemSelectedListener {
        bottomNavController.onNavigationItemSelected(it.itemId)
    }

    setOnNavigationItemReselectedListener {
        bottomNavController
            .fragmentManager
            .findFragmentById(bottomNavController.containerId)!!
            .childFragmentManager
            .fragments[0]?.let { fragment ->

            onReselectListener.onReselectItem(
                bottomNavController.activity.findNavController(bottomNavController.containerId),
                fragment
            )
        }
    }

    bottomNavController.setOnItemNavigationChanged { itemId ->
        menu.findItem(itemId).isChecked = true
    }

}