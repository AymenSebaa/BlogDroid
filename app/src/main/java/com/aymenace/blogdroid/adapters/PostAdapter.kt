package com.aymenace.blogdroid.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.aymenace.blogdroid.*
import com.aymenace.blogdroid.models.Post
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONException
import org.json.JSONObject


class PostAdapter(var context: Context, var posts: ArrayList<Post>) : RecyclerView.Adapter<PostAdapter.PostHolder>() {
    var postsAll: ArrayList<Post> ?= null
    private var userPref: SharedPreferences ?= null
    private var dialog: Progress ?= null

    init {
        postsAll = ArrayList(posts);
        userPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    }

     class PostHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var fullName: TextView ?= null
        var date: TextView ?= null
        var desc: TextView ?= null
        var likes: TextView ?= null
        var comments: TextView ?= null
        var avatar: CircleImageView ?= null
        var photo: ImageView ?= null
        var btnLike: ImageButton ?= null
        var btnComment: ImageButton ?= null
        var btnOptions: ImageButton ?= null

        init {
            fullName = itemView.findViewById(R.id.post_full_name)
            date = itemView.findViewById(R.id.post_date)
            desc = itemView.findViewById(R.id.post_desc)
            likes = itemView.findViewById(R.id.post_like_count)
            comments = itemView.findViewById(R.id.post_comment_count)
            avatar = itemView.findViewById(R.id.post_avatar)
            photo = itemView.findViewById(R.id.post_photo)
            btnLike = itemView.findViewById(R.id.post_like)
            btnComment = itemView.findViewById(R.id.post_comment)
            btnOptions = itemView.findViewById(R.id.post_options)
            btnOptions!!.visibility = View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        return PostHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_post, parent, false))
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        val post = posts[position]
        Picasso.get().load(STORAGE_PROFILES+post.user.avatar).into(holder.avatar)
        Picasso.get().load(STORAGE_POSTS+post.photo).into(holder.photo)
        holder.fullName!!.text = post.user.fullName
        holder.date!!.text = post.date
        holder.desc!!.text = post.desc
        holder.comments!!.text = if(post.comments>0) "View all ${post.comments}" else "No comments"
        holder.likes!!.text = if(post.likes>0) "${post.likes} Likes" else "No likes"

        holder.comments!!.setOnClickListener {
            val intent = Intent(context, CommentActivity::class.java)
            intent.putExtra("postId", post.id)
            intent.putExtra("position", position)
            context.startActivity(intent)
        }

        holder.btnComment!!.setOnClickListener {
            val intent = Intent(context, CommentActivity::class.java)
            intent.putExtra("postId", post.id)
            intent.putExtra("position", position)
            context.startActivity(intent)
        }

        holder.btnLike!!.setImageResource(if(post.selfLike)
            R.drawable.ic_baseline_favorite_red_24  else R.drawable.ic_baseline_favorite_outline_24)

        holder.btnLike!!.setOnClickListener {
            holder.btnLike!!.setImageResource(if(post.selfLike)
                R.drawable.ic_baseline_favorite_outline_24 else R.drawable.ic_baseline_favorite_red_24)
            val request = object : StringRequest(Method.POST, LIKE_POST, {
                try{
                    val data = JSONObject(it)
                    if(data.getBoolean("success")){
                        post.selfLike = !post.selfLike
                        if(post.selfLike) post.likes += 1 else post.likes -= 1
                        notifyItemChanged(position)
                        notifyDataSetChanged()
                    } else {
                        holder.btnLike!!.setImageResource(if(post.selfLike)
                            R.drawable.ic_baseline_favorite_red_24  else R.drawable.ic_baseline_favorite_outline_24)
                    }
                } catch (e: JSONException){

                }
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
                    map["post_id"] = "${post.id}"
                    return map
                }
            }
            val queue = Volley.newRequestQueue(context)
            queue.add(request)
        }

        if(userPref!!.getInt("id", 0) == post.user.id){
            holder.btnOptions!!.visibility = View.VISIBLE
        } else {
            holder.btnOptions!!.visibility = View.GONE
        }

        holder.btnOptions!!.setOnClickListener {
            val popupMenu = PopupMenu(context, holder.btnOptions)
            popupMenu.inflate(R.menu.menu_post_options)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.post_edit -> {
                        val intent = Intent(context, EditPostActivity::class.java)
                        intent.putExtra("id", post.id)
                        intent.putExtra("position", position)
                        intent.putExtra("desc", post.desc)
                        context.startActivity(intent)
                        true
                    }
                    R.id.post_delete -> {
                        deletePost(post.id, position)
                        true
                    }
                }
                false
            }
            popupMenu.show()
        }
    }

    private fun deletePost(id: Int, position: Int) {
        var confirm = AlertDialog.Builder(context)
        confirm.setTitle("Confirm")
        confirm.setMessage("Delete Post?")
        confirm.setPositiveButton("Delete") { _, _ ->

            dialog = Progress(context, "Deleting")
            dialog!!.show()
            val request = object : StringRequest(Method.POST, DELETE_POST, {
                try {
                    val data = JSONObject(it)
                    if(data.getBoolean("success")){
                        posts.removeAt(position)
                        notifyItemRemoved(position)
                        notifyDataSetChanged()
                        postsAll!!.clear()
                        postsAll!!.addAll(posts)
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
                    map["id"] = "$id"
                    return map
                }
            }

            val queue = Volley.newRequestQueue(context)
            queue.add(request)
        }
        confirm.setNegativeButton("Cancel") { _, _ ->}
        confirm.show()
    }

    override fun getItemCount(): Int {
        return posts.size
    }

}