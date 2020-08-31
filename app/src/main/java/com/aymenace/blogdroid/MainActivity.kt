package com.aymenace.blogdroid

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private var userPref: SharedPreferences ?= null
    private var pref: SharedPreferences ?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            pref = getSharedPreferences("onBoard", Context.MODE_PRIVATE)
            if(pref!!.getBoolean("isFirstTime", true)){
                val editor = pref!!.edit()
                editor.putBoolean("isFirstTime", false)
                editor.apply()
                startActivity( Intent(this, OnBoardActivity::class.java))
            } else {
                loggedIn()
            }
            finish()
        }, 1500)

    }

    private fun loggedIn() {
        userPref = getSharedPreferences("user", Context.MODE_PRIVATE)
        val request = object : StringRequest(Method.POST, ALIVE, {
            try {
                val data = JSONObject(it)
                Log.d("MainActivity", "data: $data")
                if(data.getBoolean("success")){
                    startActivity(Intent(this, HomeActivity::class.java))
                } else {
                    startActivity(Intent(this, AuthActivity::class.java))
                }
            } catch (e: JSONException){
                e.printStackTrace()
            }
        }, {
            it.printStackTrace()
        }){
            override fun getHeaders(): MutableMap<String, String> {
                val map = HashMap<String, String>()
                map[AUTH] = "Bearer "+userPref!!.getString("token", "")
                Log.d("MainActivity", "data: "+map[AUTH])
                return map
            }
        }
        val queue = Volley.newRequestQueue(this)
        queue.add(request)
    }
}

// TODO make app check if is still auth instead of saving that you're logged in in sharedPref
// TODO fix bottomappbar for version below 26
