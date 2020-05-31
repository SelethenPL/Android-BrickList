package com.example.bricklist

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import com.example.bricklist.database.DatabaseAccess
import com.example.bricklist.models.PartsDetailed
import kotlinx.android.synthetic.main.activity_project_parts_show.*

class ProjectPartsShowActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_parts_show)

        val id = intent.extras!!.getInt("id")
        val name = intent.extras!!.getString("name")

        showProject(id)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    private fun showProject(id: Int) {
        val databaseAccess = DatabaseAccess(this).getInstance(applicationContext)
        databaseAccess?.open()
        val items = databaseAccess?.getPartsList(id)!!
        databaseAccess.close()

        for (i in items) {
            addItemToList(i)
        }
    }

    // change part to "Inventory"
    private fun addItemToList(part: PartsDetailed) {
        val row = TableRow(this)

        // adding image to item


        val info = TextView(this)

        info.text = ("${part.name} ${part.colorName} [${part.code}]")
        row.addView(info, 350, 500)

        val qty = TextView(this)

        qty.text = ("${part.qtyInStock} of ${part.qtyInSet}")
        row.addView(qty)
        // change color
        row.setBackgroundColor(Color.CYAN)

        val plus = Button(this)
        plus.text = "+"
        row.addView(plus)

        plus.setOnClickListener {
            Toast.makeText(applicationContext, "Clicked plus one", Toast.LENGTH_SHORT).show()
        }

        val minus = Button(this)
        minus.text = "-"
        row.addView(minus)

        minus.setOnClickListener {
            Toast.makeText(applicationContext, "Clicked minus one item", Toast.LENGTH_SHORT).show()
        }

        tableView.addView(row)
        val separator = TableRow(this)
        separator.addView(TextView(this))
        tableView.addView(separator)
    }

}
