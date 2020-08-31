package com.aymenace.blogdroid.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.aymenace.blogdroid.*
import com.aymenace.blogdroid.models.Comment
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONException
import org.json.JSONObject

class CommentAdapter(var context: Context, var postPosition: Int, var comments: ArrayList<Comment>) : RecyclerView.Adapter<CommentAdapter.CommentHolder>() {

    var userPref: SharedPreferences ?= null
    var dialog: Progress ?= null

    init {
        userPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    }

    class CommentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var avatar: CircleImageView ?= null
        var fullName: TextView ?= null
        var date: TextView ?= null
        var comment: TextView ?= null
        var btnDeleteComment: ImageButton ?= null

        init {
            avatar = itemView.findViewById(R.id.comment_avatar)
            fullName = itemView.findViewById(R.id.comment_fullName)
            date = itemView.findViewById(R.id.comment_date)
            comment = itemView.findViewById(R.id.comment)
            btnDeleteComment = itemView.findViewById(R.id.btnDeleteComment)

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        return CommentHolder(LayoutInflater.from(context).inflate(R.layout.layout_comment, parent, false))
    }

    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        val comment =  comments[position]
        Picasso.get().load(STORAGE_PROFILES+comment.user.avatar).into(holder.avatar)
        holder.fullName!!.text = comment.user.fullName
        holder.date!!.text = comment.date
        holder.comment!!.text = comment.comment

        if(comment.user.id != userPref!!.getInt("id", 0)){
            holder.btnDeleteComment!!.visibility = View.GONE
        } else {
            holder.btnDeleteComment!!.setOnClickListener {
                val confirm = AlertDialog.Builder(context)
                confirm.setTitle("Delete Comment")
                confirm.setMessage("Are you sure?")
                confirm.setPositiveButton("Delete"){_, _-> deleteComment(comment.id, position) }
                confirm.setNegativeButton("Cancel"){_,_->}
                confirm.show()
            }
        }


    }

     private fun deleteComment(id: Int, position: Int) {
         dialog = Progress(context, "Deleting Comment")
         dialog!!.show()

         val request = object : StringRequest(Method.POST, DELETE_COMMENT, {
            try {
                val data = JSONObject(it)
                if(data.getBoolean("success")){
                    comments.removeAt(position)
                    val post = HomeActivity.currentPosts!![postPosition]
                    post.comments -= 1
                    HomeActivity.currentPosts!![postPosition]
                    HomeActivity.currentRecyclerView!!.adapter!!.notifyItemChanged(postPosition)
                    HomeActivity.currentRecyclerView!!.adapter!!.notifyDataSetChanged()

                    notifyItemRemoved(position)
                    notifyDataSetChanged()

                    dialog!!.dismiss()
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

    override fun getItemCount(): Int {
        return comments.size
    }

}