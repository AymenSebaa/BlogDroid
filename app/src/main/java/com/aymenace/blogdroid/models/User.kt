package com.aymenace.blogdroid.models

class User(var id: Int, var fullName: String, var avatar: String) {

    override fun toString(): String {
        return "id: $id, fullName: $fullName, avatar: $avatar"
    }

}
