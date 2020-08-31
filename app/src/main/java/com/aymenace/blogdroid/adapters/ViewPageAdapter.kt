package com.aymenace.blogdroid.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.aymenace.blogdroid.R

class ViewPageAdapter(var context: Context) : PagerAdapter() {
    private var inflater: LayoutInflater? = null;

    private val photos = arrayOf(R.drawable.desk, R.drawable.desk, R.drawable.desk)
    private val titles = arrayOf("Learn", "Create", "Enjoy")
    private val descs = arrayOf("I'm just", "a description", "so plz Enjoy")

    override fun getCount(): Int {
        return titles.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object` as LinearLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater!!.inflate(R.layout.view_pager, container, false)
        val photo = view.findViewById(R.id.photo) as ImageView
        val title = view.findViewById(R.id.title) as TextView
        val desc = view.findViewById(R.id.desc) as TextView

        photo.setImageResource(photos[position])
        title.text = titles[position]
        desc.text = descs[position]

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

}