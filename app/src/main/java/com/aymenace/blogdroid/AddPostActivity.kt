package com.aymenace.blogdroid

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.aymenace.blogdroid.fragments.AccountFragment
import com.aymenace.blogdroid.fragments.HomeFragment
import com.aymenace.blogdroid.models.Post
import com.aymenace.blogdroid.models.User
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

class AddPostActivity : AppCompatActivity() {

    private var photo: ImageView ?= null
    private var desc: EditText ?= null
    private var btnPost: Button ?= null
    private var bitmap: Bitmap ?= null
    private var dialog: Progress ?= null
    private var userPref: SharedPreferences ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)
        init()
    }

    private fun init() {
        userPref = getSharedPreferences("user", Context.MODE_PRIVATE)
        photo = findViewById(R.id.photo)
        desc = findViewById(R.id.desc)
        btnPost = findViewById(R.id.btnPost)

        photo!!.setImageURI(intent.data)
        try {
            bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(this.contentResolver, intent.data!!)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, intent.data!!)
            }
        } catch (e: IOException){
            e.printStackTrace()
        }

        btnPost!!.setOnClickListener {
            post()
        }
    }

    private fun post() {
        dialog = Progress(this, "Posting")
        dialog!!.show()
        val request = object : StringRequest(Method.POST, CREATE_POST, {
            val data = JSONObject(it)
            Log.d("AddPostActivity", "data: $data")
            try {
                if(data.getBoolean("success")){
                    val postJSON = data.getJSONObject("post")
                    val userJSON = postJSON.getJSONObject("user")
                    val post = Post(
                        postJSON.getInt("id"),
                        User(userJSON.getInt("id"),
                            userJSON.getString("firstname")+" "+userJSON.getString("lastname"),
                            userJSON.getString("photo")),
                        postJSON.getString("created_at"),
                        postJSON.getString("desc"),
                        postJSON.getString("photo"),
                        0,
                        0,
                        false)

                    HomeFragment.posts!!.add(0, post)
                    HomeFragment.recyclerView!!.adapter!!.notifyItemInserted(0)
                    HomeFragment.recyclerView!!.adapter!!.notifyDataSetChanged()
                    /*
                    AccountFragment.posts!!.add(0, post)
                    AccountFragment.recyclerView!!.adapter!!.notifyItemInserted(0)
                    AccountFragment.recyclerView!!.adapter!!.notifyDataSetChanged()
                     */
                    Toast.makeText(this, "Posted", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: JSONException){
                e.printStackTrace()
                dialog!!.dismiss()
            }
            dialog!!.dismiss()
        }, {
            it.printStackTrace()
        }){
            override fun getHeaders(): MutableMap<String, String> {
                val map = HashMap<String, String>()
                val token = userPref!!.getString("token", "")
                map[AUTH] = "Bearer $token"
                return map
            }

            override fun getParams(): MutableMap<String, String> {
                val map = HashMap<String, String>()
                map["desc"] = desc!!.text.toString().trim()
                map["photo"] = bitmapToString(bitmap!!)
                return map
            }
        }

        val queue = Volley.newRequestQueue(this)
        queue.add(request)
    }

    private fun bitmapToString(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
    }

    fun cancelPost() {
        onBackPressed()
    }

    fun changePhoto(view: View) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLEY_CHANGE_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK && requestCode == GALLEY_CHANGE_PHOTO){
            val uriImage = data!!.data
            photo!!.setImageURI(uriImage)
            try {
                bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(this.contentResolver, uriImage!!)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(contentResolver, uriImage)
                }
            } catch (e: IOException){
                e.printStackTrace()
            }
        }

    }
}