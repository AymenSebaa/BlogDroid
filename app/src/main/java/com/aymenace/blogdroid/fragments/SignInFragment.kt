package com.aymenace.blogdroid.fragments

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.aymenace.blogdroid.HomeActivity
import com.aymenace.blogdroid.LOGIN
import com.aymenace.blogdroid.Progress
import com.aymenace.blogdroid.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONException
import org.json.JSONObject

class SignInFragment : Fragment() {
    private var root: View ?= null
    private var layoutEmail: TextInputLayout ?= null
    private var layoutPassword: TextInputLayout ?= null
    private var email: TextInputEditText ?= null
    private var password: TextInputEditText ?= null
    private var btnSignIn: Button ?= null
    private var signUp: TextView ?= null
    private var dialog: Progress ?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.fragment_sign_in, container, false)
        init()
        return root
    }

    private fun init() {
        layoutEmail = root!!.findViewById(R.id.layoutEmail)
        layoutPassword = root!!.findViewById(R.id.layoutPassword)
        email = root!!.findViewById(R.id.email)
        password = root!!.findViewById(R.id.password)
        btnSignIn = root!!.findViewById(R.id.btnSignIn)
        signUp = root!!.findViewById(R.id.signUp)

        signUp!!.setOnClickListener{
            activity!!.supportFragmentManager.beginTransaction().replace(R.id.frameAuthContainer, SignUpFragment()).commit()
        }

        btnSignIn!!.setOnClickListener {
            if(validate()){
                login()
            }
        }

        email!!.addTextChangedListener {
            if(email!!.text.toString().isNotBlank()){
                layoutEmail!!.isErrorEnabled = false
            }
        }
        password!!.addTextChangedListener {
            if(password!!.text.toString().length > 7){
                layoutPassword!!.isErrorEnabled = false
            }
        }

    }

    private fun login() {
        dialog = Progress(context!!, "Logging in")
        dialog!!.show()
        val request = object : StringRequest(Method.POST, LOGIN, {
            try {
                val data = JSONObject(it)
                if(data.getBoolean("success")){
                    val user = data.getJSONObject("user")
                    val userPref = activity!!.getSharedPreferences("user", Context.MODE_PRIVATE)
                    val editor = userPref.edit()
                    editor.putString("token", data.getString("token"))
                    editor.putInt("id", user.getInt("id"))
                    editor.putString("email", user.getString("email"))
                    editor.putString("avatar", user.getString("photo"))
                    editor.putString("firstname", user.getString("firstname"))
                    editor.putString("lastname", user.getString("lastname"))
                    editor.putString("photo", user.getString("photo"))
                    editor.apply()

                    Toast.makeText(context, "Login Success", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(context, HomeActivity::class.java))
                    activity!!.finish()
                } else {
                    if(data.getBoolean("email")){
                        layoutEmail!!.isErrorEnabled = true
                        layoutEmail!!.error = data.getString("message")
                    } else if(data.getBoolean("password")){
                        layoutPassword!!.isErrorEnabled = true
                        layoutPassword!!.error = data.getString("message")
                    }
                }
            } catch (e: JSONException){
                e.printStackTrace()
            }
            dialog!!.dismiss()
        }, {
            it.printStackTrace()
            dialog!!.dismiss()
        }){
            override fun getParams(): MutableMap<String, String> {
                val map = HashMap<String, String>()
                map["email"] = email!!.text.toString().trim()
                map["password"] = password!!.text.toString().trim()
                 return map
            }
        }
        val queue = Volley.newRequestQueue(context)
        queue.add(request)
    }

    private fun validate(): Boolean {
        if(email!!.text.toString().isBlank()){
            layoutEmail!!.isErrorEnabled = true
            layoutEmail!!.error = "Email is required"
            return false
        }
        if(password!!.text.toString().length < 8){
            layoutPassword!!.isErrorEnabled = true
            layoutPassword!!.error = "Require at least 8 characters"
            return false
        }
        return true
    }

}