package com.aymenace.blogdroid

const val URL = "http://192.168.1.10:8000/"
const val HOME = URL+"api/"
const val ALIVE = HOME+"alive"
const val LOGIN = HOME+"login"
const val REGISTER = HOME+"register"
const val SAVE_USER_INFO = HOME+"save_user_info"
const val LOGOUT = HOME+"logout"

const val STORAGE_PROFILES = URL+"storage/profiles/"
const val STORAGE_POSTS = URL+"storage/posts/"

const val POSTS = HOME+"posts/"
const val CREATE_POST = POSTS+"create"
const val UPDATE_POST = POSTS+"update"
const val DELETE_POST = POSTS+"delete"
const val LIKE_POST = POSTS+"like"
const val COMMENTS = POSTS+"comments/"

const val COMMENT = HOME+"comments/"
const val CREATE_COMMENT = COMMENT+"create/"
const val DELETE_COMMENT = COMMENT+"delete/"

const val GALLEY_PICK_AVATAR = 1
const val GALLEY_ADD_POST = 2
const val ACTIVITY_ADD_POST = 3
const val GALLEY_CHANGE_PHOTO = 4
const val UPDATE_USER_INFO = 5
const val AUTH = "Authorization"