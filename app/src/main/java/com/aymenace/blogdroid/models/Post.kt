package com.aymenace.blogdroid.models

class Post(var id: Int, var user: User, var date: String, var desc: String, var photo: String,
           var likes: Int, var comments: Int,  var selfLike: Boolean) {

    override fun toString(): String {
        return "id: $id, user: $user, date: $date: desc: $desc, photo: $photo, likes: $likes, selfLike: $selfLike"
    }
}