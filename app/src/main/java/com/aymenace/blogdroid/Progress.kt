package com.aymenace.blogdroid

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog


class Progress(var context: Context, var text: String) {

    private var inflater: LayoutInflater ?= null
    private var builder: AlertDialog.Builder ?= null
    private var view: View?= null
    private var progressBar: ProgressBar?= null
    private var message: TextView?= null
    private var dialog: AlertDialog ?= null

    init {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater!!.inflate(R.layout.layout_loading_dialog, null)
        progressBar = view!!.findViewById(R.id.progressBar)
        message = view!!.findViewById(R.id.message)
        message!!.text = text

        builder = AlertDialog.Builder(context)
        builder!!.setCancelable(false)
        builder!!.setView(view)
        dialog = builder!!.create()
    }

    fun show(){
        dialog!!.show()
    }

    fun dismiss(){
        dialog!!.dismiss()
    }

}