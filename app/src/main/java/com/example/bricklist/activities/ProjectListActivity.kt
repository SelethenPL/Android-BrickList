package com.example.bricklist.activities

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.example.bricklist.R
import com.example.bricklist.database.DatabaseAccess
import kotlinx.android.synthetic.main.activity_project_list.*

class ProjectListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_list)

        showProjects()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.settings -> {
                val intent = Intent(applicationContext, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    fun addNewProject(v: View) {
        // Here do transfer to ProjectViewActivity
        val intent = Intent(applicationContext, NewXmlActivity::class.java)
        startActivity(intent)
    }

    private fun showProjects() {
        val archived = PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean("archive", false)

        val databaseAccess = DatabaseAccess(this).getInstance(applicationContext)
        databaseAccess?.open()
        val items = databaseAccess?.getProjectList(!archived)!!
        databaseAccess.close()

        for (i in items) {
            addItemToList(i.first, i.second)
        }
    }

    private fun addItemToList(id: Int, name: String) {
        val row = TableRow(this)

        val details = TextView(this)

        details.text = ("$id - $name")
        details.textSize = 30.0f

        row.addView(details)
        row.setBackgroundColor(Color.LTGRAY)

        tableView.addView(row)

        details.setOnClickListener {
            val param = details.text.toString().split(" - ")
            Toast.makeText(applicationContext, "Id: ${param[0]}, Name: ${param[1]}", Toast.LENGTH_LONG).show()

            // Here do transfer to ProjectViewActivity
            val intent = Intent(applicationContext, ProjectPartsShowActivity::class.java)
            intent.putExtra("id", param[0].toInt())
            intent.putExtra("name", param[1])
            startActivity(intent)
        }

        val separator = TableRow(this)
        separator.addView(TextView(this))
        tableView.addView(separator)
    }
}
