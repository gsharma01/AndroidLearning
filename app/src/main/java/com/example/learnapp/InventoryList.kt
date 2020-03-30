package com.example.learnapp

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView


class ListDisplay : Activity() {
    // Array of strings...
    var mobileArray = arrayOf(
        "Android", "IPhone", "WindowsMobile", "Blackberry",
        "WebOS", "Ubuntu", "Windows7", "Max OS X"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val adapter: ArrayAdapter<*> = ArrayAdapter(
            this,
            R.layout.inventory_row, mobileArray
        )
        val listView =
            findViewById<View>(R.id.mobile_list) as ListView
        listView.adapter = adapter
    }
}