package com.aymenace.blogdroid

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.aymenace.blogdroid.fragments.HomeFragment
import org.json.JSONException
import org.json.JSONObject

class EditPostActivity : AppCompatActivity() {
    private var id: Int ?= null
    private var position: Int ?= null
    private var btnSave: Button ?= null
    private var dialog: Progress ?= null
    private var desc: EditText ?= null
    private var userPref: SharedPreferences ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_post)
        init()
    }

    private fun init() {
        userPref = getSharedPreferences("user", Context.MODE_PRIVATE)
        id = intent.getIntExtra("id", 0)
        position = intent.getIntExtra("position", 0)
        btnSave = findViewById(R.id.btnSave)
        desc = findViewById(R.id.edit_desc)
        desc!!.setText(intent.getStringExtra("desc"))

        btnSave!!.setOnClickListener {
            if(desc.toString().trim().isNotBlank()){
               savePost()
            }
        }
    }

    private fun savePost() {
        dialog = Progress(this, "Saving")
        dialog!!.show()
        val request = object: StringRequest(Method.POST, UPDATE_POST, {
            val data = JSONObject(it)
            Log.d("EditPostActivity", "data $data")
            try {
                if(data.getBoolean("success")){
                    val post = HomeActivity.currentPosts!![position!!]
                    post.desc = desc!!.text.toString()
                    HomeActivity.currentPosts!![position!!] = post
                    HomeActivity.currentRecyclerView!!.adapter!!.notifyItemInserted(position!!)
                    HomeActivity.currentRecyclerView!!.adapter!!.notifyDataSetChanged()
                    finish()
                }
            } catch (e: JSONException){
                e.printStackTrace()
            }
            dialog!!.dismiss()
        },  {
            it.printStackTrace()
            dialog!!.dismiss()
        }){
            override fun getHeaders(): MutableMap<String, String> {
                val map = HashMap<String, String>()
                val token = userPref!!.getString("token", "")
                map[AUTH] = "Bearer $token"
                return map
            }

            override fun getParams(): MutableMap<String, String> {
                val map = HashMap<String, String>()
                map["id"] = "$id"
                map["desc"] = desc!!.text.toString()
                Log.d("EditPostActivity", "id: "+map["id"]+", desc: "+map["desc"])
                return map
            }
        }
        val queue = Volley.newRequestQueue(this)
        queue.add(request)
    }

    fun cancel(view: View) {
        onBackPressed()
    }
}