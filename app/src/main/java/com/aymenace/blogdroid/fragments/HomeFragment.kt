package com.aymenace.blogdroid.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.aymenace.blogdroid.AUTH
import com.aymenace.blogdroid.HomeActivity
import com.aymenace.blogdroid.POSTS
import com.aymenace.blogdroid.R
import com.aymenace.blogdroid.adapters.PostAdapter
import com.aymenace.blogdroid.models.Post
import com.aymenace.blogdroid.models.User
import com.google.android.material.appbar.MaterialToolbar
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.MutableMap
import kotlin.collections.set

class HomeFragment : Fragment() {
    companion object {
        var recyclerView: RecyclerView?= null
        var posts: ArrayList<Post> ?= null
    }
    private var userPref: SharedPreferences ?= null
    private var postAdapter: PostAdapter ?= null
    private var root: View ?= null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.fragment_home, container, false)
        init()
        return root
    }

    private fun init() {
        userPref = context!!.getSharedPreferences("user", Context.MODE_PRIVATE)
        recyclerView = root!!.findViewById(R.id.recycler_home)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)

        getPosts()
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
                        Log.d("HomeFragment", "user_id: $post")
                        posts!!.add(post)
                    }

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
                map["userOnly"] = ""
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

}