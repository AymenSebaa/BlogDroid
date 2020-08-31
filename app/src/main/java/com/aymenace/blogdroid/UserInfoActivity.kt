package com.aymenace.blogdroid

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

class UserInfoActivity : AppCompatActivity() {

    private var userPref: SharedPreferences ?= null
    private var avatar: CircleImageView?= null
    private var selectPhoto: TextView ?= null
    private var layoutFirstName: TextInputLayout ?= null
    private var layoutLastName: TextInputLayout ?= null
    private var firstName: TextInputEditText ?= null
    private var lastName: TextInputEditText ?= null
    private var btnContinue: Button?= null
    private var bitmap: Bitmap ?= null
    private var dialog: ProgressDialog ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)
        init()
    }

    private fun init() {
        dialog = ProgressDialog(this)
        dialog!!.setCancelable(false)
        userPref = getSharedPreferences("user", Context.MODE_PRIVATE)
        avatar = findViewById(R.id.avatar)
        selectPhoto = findViewById(R.id.selectPhoto)
        layoutFirstName = findViewById(R.id.layoutFirstName)
        layoutLastName = findViewById(R.id.layoutLastName)
        firstName = findViewById(R.id.firstName)
        lastName = findViewById(R.id.lastName)
        btnContinue = findViewById(R.id.btnContinue)

        if(intent.getStringExtra("updateUserInfo") != null){
            Picasso.get().load(intent.getStringExtra("avatar")).into(avatar)
            firstName!!.setText(intent.getStringExtra("firstname"))
            lastName!!.setText(intent.getStringExtra("lastname"))
            btnContinue!!.text = "Save changes"
        }

        firstName!!.addTextChangedListener {
            if(firstName!!.text.toString().isNotBlank()){
                layoutFirstName!!.isErrorEnabled = false
            }
        }
        lastName!!.addTextChangedListener {
            if(lastName!!.text.toString().isNotBlank()){
                layoutLastName!!.isErrorEnabled = false
            }
        }

        selectPhoto!!.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, GALLEY_PICK_AVATAR)
        }

        btnContinue!!.setOnClickListener {
            if(validate()){
                saveUserInfo()
            }
        }
    }

    private fun saveUserInfo() {
        dialog!!.setMessage("Saving User info")
        dialog!!.show()
        val firstName = firstName!!.text.toString().trim()
        val lastName = lastName!!.text.toString().trim()
        val request = object : StringRequest(Method.POST, SAVE_USER_INFO, {
            try{
                val data = JSONObject(it)
                if(data.getBoolean("success")){
                    val user = data.getJSONObject("user")
                    val editor = userPref!!.edit()
                    editor.putString("avatar", user.getString("photo"))
                    editor.putString("firstName", user.getString("firstname"))
                    editor.putString("lastName", user.getString("lastname"))
                    editor.apply()
                    if(intent.getStringExtra("updateUserInfo") == null){
                        startActivity(Intent(this, HomeActivity::class.java))
                    }
                    finish()
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
                map[AUTH] = "Bearer "+userPref!!.getString("token", "")!!
                return map
            }

            override fun getParams(): MutableMap<String, String> {
                val map = HashMap<String, String>()
                map["photo"] = bitmapToString()
                map["firstname"] = firstName
                map["lastname"] = lastName
                return map
            }
        }

        val queue = Volley.newRequestQueue(this)
        queue.add(request)
    }

    private fun bitmapToString(): String {
        if(bitmap != null){
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
        }
        return ""
    }

    private fun validate(): Boolean {
        if(firstName!!.text.toString().isBlank()){
            layoutFirstName!!.isErrorEnabled = true
            layoutFirstName!!.error = "First name is required"
            return false
        }
        if(lastName!!.text.toString().isBlank()){
            layoutLastName!!.isErrorEnabled = true
            layoutLastName!!.error = "Last name is required"
            return false
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if( resultCode == RESULT_OK && requestCode == GALLEY_PICK_AVATAR){
            val imageUri = data!!.data
            avatar!!.setImageURI(imageUri)
            try {
                bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(this.contentResolver, imageUri!!)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                }
            } catch (e: IOException){
                e.printStackTrace()
            }
        }
    }
}