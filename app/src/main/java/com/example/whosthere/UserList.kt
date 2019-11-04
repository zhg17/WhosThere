package com.example.whosthere

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class UserList (private val context: Activity, internal var users:List<Friend>):
    ArrayAdapter<Friend>(context,R.layout.layout_user_list,users){
    override fun getView(position:Int, converView: View?, parent: ViewGroup):View{
        val inflater = context.layoutInflater
        val listViewItem=inflater.inflate(R.layout.layout_user_list,null,true)

        val textViewName=listViewItem.findViewById<View>(R.id.textViewName) as TextView

        val user = users[position]
        textViewName.text=user.username

        return listViewItem

    }

}