package com.example.bricklist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun queryOnClick(v: View) {
        val databaseAccess = DatabaseAccess(this).getInstance(applicationContext)
        databaseAccess?.open()

        result.text = databaseAccess?.getColors()

        databaseAccess?.close()
    }

}
