package com.aymenace.blogdroid.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.aymenace.blogdroid.*
import com.aymenace.blogdroid.adapters.PostAdapter
import com.aymenace.blogdroid.models.Post
import com.aymenace.blogdroid.models.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user_info.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class AccountFragment : Fragment() {
    companion object {
        var recyclerView: RecyclerView?= null
        var posts: ArrayList<Post> ?= null
    }

    private var userPref: SharedPreferences?= null
    private var recyclerView: RecyclerView?= null
    private var postAdapter: PostAdapter ?= null
    private var root: View ?= null
    private var avatar: ImageView ?= null
    private var fullName: TextView ?= null
    private var postCount: TextView ?= null
    private var btnEditAccount: Button?= null
    private var btnLogout: ImageView ?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.fragment_account, container, false)
        init()
        return root
    }

    private fun init() {
        userPref = context!!.getSharedPreferences("user", Context.MODE_PRIVATE)
        recyclerView = root!!.findViewById(R.id.account_recycler)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)

        avatar = root!!.findViewById(R.id.account_avatar)
        fullName = root!!.findViewById(R.id.account_fullName)
        postCount = root!!.findViewById(R.id.account_postCount)
        btnEditAccount = root!!.findViewById(R.id.btnEditAccount)
        btnLogout = root!!.findViewById(R.id.btnLogout)
        fillUserInfo()

        btnEditAccount!!.setOnClickListener {
            var intent = Intent(context, UserInfoActivity::class.java)
            intent.putExtra("updateUserInfo", AccountFragment::class.simpleName)
            intent.putExtra("avatar", STORAGE_PROFILES+userPref!!.getString("photo", ""))
            intent.putExtra("firstname", userPref!!.getString("firstname", ""))
            intent.putExtra("lastname", userPref!!.getString("lastname", ""))
            startActivityForResult(intent, UPDATE_USER_INFO)
        }

        btnLogout!!.setOnClickListener {
            logout()
        }

        getPosts()
    }

    private fun logout() {
        val request = object : StringRequest(Method.POST, LOGOUT, {
            try {
                val data = JSONObject(it)
                if(data.getBoolean("success")){
                    val editor = userPref!!.edit()
                    editor.clear()
                    editor.apply()
                    startActivity(Intent(context, AuthActivity::class.java))
                    (context as HomeActivity ).finish()
                }
            } catch (e: JSONException){

            }
        }, {
            it.printStackTrace()
        }){
            override fun getHeaders(): MutableMap<String, String> {
                val map = HashMap<String, String>()
                map[AUTH] = "Bearer "+userPref!!.getString("token", "")
                return map
            }
        }
        val queue = Volley.newRequestQueue(context)
        queue.add(request)
    }


    fun getPosts() {
        posts = ArrayList()
        HomeActivity.refreshLayout!!.isRefreshing = true
        val request = object : StringRequest(Method.POST, POSTS, {
            try {
                val data = JSONObject(it)
                if(data.getBoolean("success")){
                    val postArray = JSONArray(data.getString("posts"))
                    for (i in 0 until postArray.length()){
                        val postJSON = postArray.getJSONObject(i)
                        val userJSON = postJSON.getJSONObject("user")
                        val post = Post(
                            postJSON.getInt("id"),
                            User(userJSON.getInt("id"),
                                userJSON.getString("firstname")+" "+userJSON.getString("lastname"),
                                userJSON.getString("photo")),
                            postJSON.getString("created_at"),
                            postJSON.getString("desc"),
                            postJSON.getString("photo"),
                            postJSON.getInt("likesCount"),
                            postJSON.getInt("commentsCount"),
                            postJSON.getBoolean("selfLike")
                        )
                        posts!!.add(post)
                    }
                    postCount!!.text = "${posts!!.size}"
                    postAdapter = PostAdapter(context!!, posts!!)
                    recyclerView!!.adapter = postAdapter

                    HomeActivity.currentPosts = posts
                    HomeActivity.currentRecyclerView = recyclerView
                }
            } catch (e: JSONException){
                e.printStackTrace()
            }
            HomeActivity.refreshLayout!!.isRefreshing = false
        }, {
            it.printStackTrace()
            HomeActivity.refreshLayout!!.isRefreshing = false
        }){
            override fun getHeaders(): MutableMap<String, String> {
                val map = HashMap<String, String>()
                val token = userPref!!.getString("token", "")
                map[AUTH] = "Bearer $token"
                return map
            }

            override fun getParams(): MutableMap<String, String> {
                val map = HashMap<String, String>()
                map["userOnly"] = "yes"
                map["search"] = ""
                return map
            }
        }
        val queue = Volley.newRequestQueue(context)
        queue.add(request)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if(!hidden){
            getPosts()
        }
    }

    override fun onResume() {
        super.onResume()
        fillUserInfo()
    }

    private fun fillUserInfo(){
        Picasso.get().load(STORAGE_PROFILES +userPref!!.getString("avatar", "")).into(avatar)
        fullName!!.text = "${userPref!!.getString("firstname", "")} ${userPref!!.getString("lastname", "")}"
    }

}