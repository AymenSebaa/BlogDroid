package com.aymenace.blogdroid

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.aymenace.blogdroid.adapters.ViewPageAdapter

class OnBoardActivity : AppCompatActivity() {

    private var viewPager: ViewPager ?= null
    private var btnLeft: Button ?= null
    private var btnRight: Button ?= null
    private var adapter: ViewPageAdapter ?= null
    private var dotsLayout: LinearLayout ?= null
    private var dots: Array<TextView?> ?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboard)
        init()
    }

    private fun init() {
        viewPager = findViewById(R.id.view_pager)
        btnLeft = findViewById(R.id.btn_left)
        btnRight = findViewById(R.id.btn_right)
        dotsLayout = findViewById(R.id.dotsLayout)
        adapter = ViewPageAdapter(this)
        viewPager!!.adapter = adapter
        addDots(0)
        viewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                addDots(position)

                when(position){
                    0 -> {
                        btnLeft!!.visibility = View.VISIBLE
                        btnLeft!!.isEnabled = true
                        btnRight!!.text = "next"
                    }
                    1 -> {
                        btnLeft!!.visibility = View.GONE
                        btnLeft!!.isEnabled = false
                        btnRight!!.text = "next"
                    }
                    else -> {
                        btnLeft!!.visibility = View.GONE
                        btnLeft!!.isEnabled = false
                        btnRight!!.text = "finish"
                    }
                }
            }
            override fun onPageScrollStateChanged(state: Int) {}

        })

        btnRight!!.setOnClickListener {
            if (btnRight!!.text.toString().contentEquals("next")) {
                viewPager!!.setCurrentItem(viewPager!!.currentItem + 1, true)
            } else {
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }
        }

        btnLeft!!.setOnClickListener{
            viewPager!!.setCurrentItem(viewPager!!.currentItem + 2, true)
        }
    }

    private fun addDots(position: Int){
        dotsLayout!!.removeAllViews()
        dots = arrayOfNulls(3)

        for (i in dots!!.indices){
            dots!![i] = TextView(this)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dots!![i]?.text = Html.fromHtml("&#8226", Html.FROM_HTML_MODE_COMPACT)
            } else {
                dots!![i]?.text = Html.fromHtml("&#8226")
            }
            dots!![i]?.textSize = 54f
            dots!![i]?.setTextColor(ContextCompat.getColor(this, R.color.colorLightGray))
            dotsLayout!!.addView(dots!![i])
        }
        if(!dots!!.isEmpty()){
            dots!![position]!!.setTextColor(ContextCompat.getColor(this, R.color.colorGray))
        }
    }

}