package com.aymenace.blogdroid.models

class Comment(var id: Int, var user: User, var date: String, var comment: String) {
    override fun toString(): String {
        return "id: $id, user: $user, date: $date, comment: $comment"
    }
}