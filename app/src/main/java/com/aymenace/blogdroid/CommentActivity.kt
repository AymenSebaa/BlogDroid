package com.aymenace.blogdroid

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.aymenace.blogdroid.adapters.CommentAdapter
import com.aymenace.blogdroid.models.Comment
import com.aymenace.blogdroid.models.User
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class CommentActivity : AppCompatActivity() {
    private var userPref: SharedPreferences?= null
    private var recyclerView: RecyclerView ?= null
    private var comments: ArrayList<Comment> ?= null
    private var adapter: CommentAdapter ?= null
    private var comment: EditText ?= null
    private var btnAddComment: ImageButton?= null
    private var postId = 0
    private var position = 0
    private var dialog: Progress ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        init()
    }

    private fun init() {
        userPref = getSharedPreferences("user", Context.MODE_PRIVATE)
        recyclerView = findViewById(R.id.recyclerComments)
        recyclerView!!.setHasFixedSize(true)
        comment = findViewById(R.id.add_comment)
        btnAddComment = findViewById(R.id.btnAddComment)
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        postId = intent.getIntExtra("postId", 0)
        position = intent.getIntExtra("position", -1)
        btnAddComment!!.setOnClickListener {
            if(comment!!.text.toString().isNotBlank()){
                addComment()
            }
        }
        getComments()
    }

    private fun getComments() {
        comments = ArrayList()
        val request = object : StringRequest(Method.POST, COMMENTS, {
            try {
                val data = JSONObject(it)
                if(data.getBoolean("success")){
                    val commentArray = JSONArray(data.getString("comments"))
                    for(i in 0 until commentArray.length()){
                        val commentJSON = commentArray.getJSONObject(i)
                        val userJSON = commentJSON.getJSONObject("user")

                        val comment = Comment( commentJSON.getInt("id"),
                            User(
                                userJSON.getInt("id"),
                                userJSON.getString("firstname")+" "+userJSON.getString("lastname"),
                                userJSON.getString("photo")),
                            commentJSON.getString("created_at"),
                            commentJSON.getString("comment"))

                        comments!!.add(comment)
                    }
                }
            } catch (e: JSONException){
                e.printStackTrace()
            }
            adapter = CommentAdapter(this, position, comments!!)
            recyclerView!!.adapter = adapter
        }, {
            it.printStackTrace()
        }){
            override fun getHeaders(): MutableMap<String, String> {
                val map = HashMap<String, String>()
                map[AUTH] = "Bearer "+userPref!!.getString("token", "")
                return map
            }

            override fun getParams(): MutableMap<String, String> {
                val map = HashMap<String, String>()
                map["post_id"] = "$postId"
                return map
            }
        }
        val queue = Volley.newRequestQueue(this)
        queue.add(request)
    }

    fun goBack() {
        onBackPressed()
    }

    private fun addComment() {
        dialog = Progress(this, "Adding comment")
        dialog!!.show()
        val request = object : StringRequest(Method.POST, CREATE_COMMENT, {
            try {
                val data = JSONObject(it)
                Log.d("CommentActivity", "data: $data")
                if(data.getBoolean("success")){
                    val commentJSON = data.getJSONObject("comment")
                    val userJSON = data.getJSONObject("user")
                    val comment = Comment(
                        commentJSON.getInt("id"),
                        User(userJSON.getInt("id"),
                            userJSON.getString("firstname")+" "+userJSON.getString("lastname"),
                            userJSON.getString("photo")),
                        commentJSON.getString("created_at"),
                        commentJSON.getString("comment")
                    )

                    val post = HomeActivity.currentPosts!![position]
                    post.comments += 1
                    HomeActivity.currentPosts!![position] = post

                    comments!!.add(0, comment)
                    recyclerView!!.adapter!!.notifyItemInserted(0)
                    recyclerView!!.adapter!!.notifyDataSetChanged()

                    HomeActivity.currentPosts!![position]
                    HomeActivity.currentRecyclerView!!.adapter!!.notifyItemChanged(position)
                    HomeActivity.currentRecyclerView!!.adapter!!.notifyDataSetChanged()

                    this.comment!!.text.clear()
                }
            } catch (e: JSONException){
                e.printStackTrace()
            }
            dialog!!.dismiss()
        }, {
            it.printStackTrace()
            dialog!!.dismiss()
        }){
            override fun getHeaders(): MutableMap<String, String> {
                val map = HashMap<String, String>()
                map[AUTH] = "Bearer "+userPref!!.getString("token", "")
                return map
            }

            override fun getParams(): MutableMap<String, String> {
                val map = HashMap<String, String>()
                map["post_id"] = "$postId"
                map["comment"] = comment!!.text.toString()
                return map
            }
        }
        val queue = Volley.newRequestQueue(this)
        queue.add(request)
    }

    fun goBack(view: View) {
        onBackPressed()
    }
}