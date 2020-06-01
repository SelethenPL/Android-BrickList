package com.example.bricklist.activities

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bricklist.R
import com.example.bricklist.database.DatabaseAccess
import com.example.bricklist.models.PartsDetailed
import kotlinx.android.synthetic.main.activity_project_parts_show.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.net.URL
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


class ProjectPartsShowActivity : AppCompatActivity() {

    var id: Int = 0
    var name: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_parts_show)

        id = intent.extras!!.getInt("id")
        name = intent.extras!!.getString("name").toString()

        showProject(id)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.project_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.archive -> {
                // To archive
                val databaseAccess = DatabaseAccess(this).getInstance(applicationContext)
                databaseAccess?.open()
                val result = databaseAccess?.activate_deactivate(id)
                databaseAccess?.close()
                when (result) {
                    true -> {
                        Toast.makeText(applicationContext, "Activated project", Toast.LENGTH_SHORT).show()
                    }
                    false -> {
                        Toast.makeText(applicationContext, "Deactivated project", Toast.LENGTH_SHORT).show()
                    }
                    null -> {
                        Toast.makeText(applicationContext, "Error while activation function", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            R.id.saveToXML -> {
                // Save to XML
                writeXML(id)
            }
        }
        return super.onOptionsItemSelected(item)
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

    private fun addItemToList(part: PartsDetailed) {
        val row = TableRow(this)
        val row2 = TableRow(this)
        val brickInfoView = TextView(this)
        val quantityView = TextView(this)
        val plusButton = Button(this)
        val minusButton = Button(this)
        val separator = TableRow(this)

        Thread {
            try {
                // fun checkImageExist(id: Int): String
                val databaseAccess = DatabaseAccess(this).getInstance(applicationContext)
                databaseAccess?.open()
                val link = databaseAccess?.checkImageExist(part.id)
                databaseAccess?.close()

                val imageView = ImageView(applicationContext)
                val url = URL(link)
                val image = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                imageView.setImageBitmap(image)

                row.addView(imageView)
            } catch (e: Exception) {
                Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_LONG).show()
            }

            runOnUiThread {
                brickInfoView.text = ("${part.name} ${part.colorName} [${part.code}]")
                quantityView.text = ("${part.qtyInStock} of ${part.qtyInSet}")

                plusButton.text = "+"
                plusButton.setOnClickListener {
                    val databaseAccess = DatabaseAccess(this).getInstance(applicationContext)
                    databaseAccess?.open()
                    val value = databaseAccess?.updateQty(part.id, "PLUS")
                    databaseAccess?.close()
                    when (value) {
                        true -> {
                            if (part.qtyInStock < part.qtyInSet) {
                                Toast.makeText(applicationContext, "Added one of ${part.code}.", Toast.LENGTH_SHORT).show()
                                part.qtyInStock += 1
                                quantityView.text = ("${part.qtyInStock} of ${part.qtyInSet}")
                                if (part.qtyInStock == part.qtyInSet){
                                    row.setBackgroundColor(Color.LTGRAY)
                                    row2.setBackgroundColor(Color.LTGRAY)
                                }
                            }
                        }
                        null -> {
                            Toast.makeText(applicationContext, "Error while adding", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                minusButton.text = "-"
                minusButton.setOnClickListener {
                    val databaseAccess = DatabaseAccess(this).getInstance(applicationContext)
                    databaseAccess?.open()
                    val value = databaseAccess?.updateQty(part.id, "MINUS")
                    databaseAccess?.close()
                    when (value) {
                        false -> {
                            if (0 < part.qtyInStock) {
                                Toast.makeText(applicationContext, "Minus one of ${part.code}.", Toast.LENGTH_SHORT).show()
                                part.qtyInStock -= 1
                                quantityView.text = ("${part.qtyInStock} of ${part.qtyInSet}")
                                row.setBackgroundColor(Color.TRANSPARENT)
                                row2.setBackgroundColor(Color.TRANSPARENT)
                            }
                        }
                        null -> {
                            Toast.makeText(applicationContext, "Error while adding", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                separator.addView(TextView(this))

                row.addView(brickInfoView, 350, 500)
                row.addView(quantityView)

                row2.addView(plusButton)
                row2.addView(minusButton)


                tableView.addView(row)
                tableView.addView(row2)
                tableView.addView(separator)
            }
        }.start()
    }

    private fun writeXML(id: Int) {
        val databaseAccess = DatabaseAccess(this).getInstance(applicationContext)
        databaseAccess?.open()
        val items = databaseAccess?.getInventoriesPartsForXML(id)!!
        databaseAccess.close()

        val docBuilder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc: Document = docBuilder.newDocument()

        val rootElement: Element = doc.createElement("INVENTORY")

        for (i in 0 until items.size) {
            val qty = items[i].qtyInSet - items[i].qtyInStock
            if (qty == 0)
                continue

            val item: Element = doc.createElement("ITEM")

            val itemType: Element = doc.createElement("ITEMTYPE")
            itemType.appendChild(doc.createTextNode(items[i].itemType))
            item.appendChild(itemType)

            val itemID: Element = doc.createElement("ITEMID")
            itemID.appendChild(doc.createTextNode(items[i].itemID))
            item.appendChild(itemID)

            val itemColor: Element = doc.createElement("COLOR")
            itemColor.appendChild(doc.createTextNode(items[i].colorID.toString()))
            item.appendChild(itemColor)

            val qtyFilled: Element = doc.createElement("QTYFILLED")
            qtyFilled.appendChild(doc.createTextNode(qty.toString()))
            item.appendChild(qtyFilled)

            rootElement.appendChild(item)
        }
        doc.appendChild(rootElement)

        val transformer: Transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty("{gttp://xml.apache.org.xslt}indent-amount", "2")

        val path = this.filesDir
        val outDir = File(path, "output")
        outDir.mkdir()

        Toast.makeText(applicationContext, "Data saved in: $path/output", Toast.LENGTH_SHORT).show()

        val file = File(outDir, "project.xml")
        transformer.transform(DOMSource(doc), StreamResult(file))


    }
}
