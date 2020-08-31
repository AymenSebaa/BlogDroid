package com.aymenace.blogdroid

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.aymenace.blogdroid.adapters.PostAdapter
import com.aymenace.blogdroid.fragments.AccountFragment
import com.aymenace.blogdroid.fragments.HomeFragment
import com.aymenace.blogdroid.fragments.SearchFragment
import com.aymenace.blogdroid.models.Post
import com.aymenace.blogdroid.models.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class HomeActivity : AppCompatActivity() {
    companion object {
        var refreshLayout: SwipeRefreshLayout?= null
        var currentRecyclerView: RecyclerView ?= null
        var currentPosts: ArrayList<Post> ?= null
    }

    private var userPref: SharedPreferences?= null
    private var fragmentManager: FragmentManager ?= null
    private var navigationView: BottomNavigationView?= null
    private var toolbar: MaterialToolbar?= null
    private var fab: FloatingActionButton ?= null

    private var homeFragment: HomeFragment ?= null
    private var searchFragment: SearchFragment ?= null
    private var accountFragment: AccountFragment ?= null
    private var currentFragment: Fragment ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        homeFragment = HomeFragment()
        searchFragment = SearchFragment()
        accountFragment = AccountFragment()
        currentFragment = homeFragment

        fragmentManager = supportFragmentManager
        fragmentManager!!.beginTransaction().add(R.id.frameHomeContainer, homeFragment!!, HomeFragment::class.simpleName).commit()
        fragmentManager!!.beginTransaction().add(R.id.frameHomeContainer, searchFragment!!, SearchFragment::class.simpleName).hide(searchFragment!!).commit()
        fragmentManager!!.beginTransaction().add(R.id.frameHomeContainer, accountFragment!!, AccountFragment::class.simpleName).hide(accountFragment!!).commit()
        init()
    }

    private fun init() {
        userPref = getSharedPreferences("user", Context.MODE_PRIVATE)
        fab = findViewById(R.id.fab)
        navigationView = findViewById(R.id.bottom_nav)
        refreshLayout = findViewById(R.id.swipeHome)
        toolbar = findViewById(R.id.toolbarHome)
        setSupportActionBar(toolbar)

        fab!!.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, GALLEY_ADD_POST)
        }

        navigationView!!.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.item_home -> {
                    switchFragment(homeFragment!!)
                    refreshLayout!!.setOnRefreshListener {
                        homeFragment!!.getPosts()
                    }
                }
                R.id.item_search -> {
                    switchFragment(searchFragment!!)
                    currentRecyclerView = SearchFragment.recyclerView
                    refreshLayout!!.setOnRefreshListener {
                        searchFragment!!.getPosts()
                    }
                }
                R.id.item_account -> {
                    switchFragment(accountFragment!!)
                    currentRecyclerView = AccountFragment.recyclerView
                    refreshLayout!!.setOnRefreshListener {
                        accountFragment!!.getPosts()
                    }
                }
            }
            true
        }

    }

    private fun switchFragment(currentFragment: Fragment){
        Log.d("HomeActivity", "before currentFragment: ${this.currentFragment}, currentPosts: $currentPosts, currentRecyclerView: $currentRecyclerView, ")
        fragmentManager!!.beginTransaction().hide(this.currentFragment!!).commit()
        fragmentManager!!.beginTransaction().show(currentFragment).commit()
        this.currentFragment = currentFragment
        Log.d("HomeActivity",  "after currentFragment: ${this.currentFragment}, currentPosts: $currentPosts, currentRecyclerView: $currentRecyclerView, ")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("HomeActivity", "resultCode: $resultCode == $RESULT_OK, requestCode: $requestCode == $ACTIVITY_ADD_POST")
        if(resultCode == RESULT_OK && requestCode == GALLEY_ADD_POST){
            val uriImage = data!!.data
            val intent = Intent(this, AddPostActivity::class.java)
            intent.data = uriImage
            startActivityForResult(intent, ACTIVITY_ADD_POST)
        } else if(resultCode == RESULT_OK && requestCode == ACTIVITY_ADD_POST){
            Log.d("HomeActivity", "switchFragment to homeFragment")
        }
        navigationView!!.selectedItemId =  R.id.item_home

    }
}