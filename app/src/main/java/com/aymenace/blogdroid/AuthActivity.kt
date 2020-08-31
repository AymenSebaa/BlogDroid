package com.aymenace.blogdroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.aymenace.blogdroid.fragments.SignInFragment

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        supportFragmentManager.beginTransaction().replace(R.id.frameAuthContainer, SignInFragment()).commit()
    }
}