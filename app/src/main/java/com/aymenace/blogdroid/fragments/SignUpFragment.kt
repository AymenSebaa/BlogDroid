package com.aymenace.blogdroid.fragments

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.aymenace.blogdroid.Progress
import com.aymenace.blogdroid.R
import com.aymenace.blogdroid.REGISTER
import com.aymenace.blogdroid.UserInfoActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONException
import org.json.JSONObject

class SignUpFragment : Fragment() {
    private var root: View ?= null
    private var layoutEmail: TextInputLayout?= null
    private var layoutPassword: TextInputLayout?= null
    private var layoutConfirm: TextInputLayout?= null
    private var email: TextInputEditText?= null
    private var password: TextInputEditText?= null
    private var confirm: TextInputEditText?= null
    private var btnSignUp: Button?= null
    private var signIn: TextView?= null
    private var dialog: Progress?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.fragment_sign_up, container, false)
        init()
        return root
    }

    private fun init() {
        layoutEmail = root!!.findViewById(R.id.layoutEmail)
        layoutPassword = root!!.findViewById(R.id.layoutPassword)
        layoutConfirm = root!!.findViewById(R.id.layoutConfirm)
        email = root!!.findViewById(R.id.email)
        password = root!!.findViewById(R.id.password)
        confirm = root!!.findViewById(R.id.confirm)
        btnSignUp = root!!.findViewById(R.id.btnSignUp)
        signIn = root!!.findViewById(R.id.signIn)

        signIn!!.setOnClickListener{
            activity!!.supportFragmentManager.beginTransaction().replace(R.id.frameAuthContainer, SignInFragment()).commit()
        }

        btnSignUp!!.setOnClickListener {
            if(validate()){
                register()
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

        confirm!!.addTextChangedListener {
            if(confirm!!.text.toString().contentEquals(password!!.text.toString())){
                layoutConfirm!!.isErrorEnabled = false
            }
        }

    }

    private fun register() {
        dialog = Progress(context!!, "Registering")
        dialog!!.show()
        val request = object : StringRequest(Method.POST, REGISTER, {
            try {
                val data = JSONObject(it)
                Log.d("SignUpFragment", "data: $data")
                if(data.getBoolean("success")){
                    val user = data.getJSONObject("user")
                    val userPref = activity!!.getSharedPreferences("user", Context.MODE_PRIVATE)
                    val editor = userPref.edit()
                    editor.putString("token", data.getString("token"))
                    editor.putInt("id", user.getInt("id"))
                    editor.putString("email", user.getString("email"))
                    editor.apply()
                    Toast.makeText(context, "Register Success", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(context, UserInfoActivity::class.java))
                    activity!!.finish()
                } else {
                    if(data.getBoolean("email")){
                        layoutEmail!!.isErrorEnabled = true
                        layoutEmail!!.error = data.getString("message")
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
        if(!confirm!!.text.toString().contentEquals(password!!.text.toString())){
            layoutConfirm!!.isErrorEnabled = true
            layoutConfirm!!.error = "Password doesn't match"
            return false
        }
        return true
    }

}