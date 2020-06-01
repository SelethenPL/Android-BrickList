package com.example.bricklist.activities

import android.annotation.SuppressLint
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.example.bricklist.R
import com.example.bricklist.database.DatabaseAccess
import com.example.bricklist.models.InventoriesPart
import kotlinx.android.synthetic.main.activity_new_xml.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

class NewXmlActivity : AppCompatActivity() {

    private var id: String = "0"
    private val fileName: String = "temp.xml"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_xml)

        checkButton.isEnabled = true
        addButton.isEnabled = false

        numberText.isEnabled = true
        projectNameText.isEnabled = true

    }

    fun load(v: View) {
        val bg = BgTask()

        // get config's link
        id = numberText.text.toString()
        val link = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString("url", "http://fcds.cs.put.poznan.pl/MyWeb/BL/")

        // link = .text.toString()
        bg.execute(id, link)
    }

    fun add(v: View) {

        if (projectNameText.text.isEmpty()) {
            Toast.makeText(applicationContext, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        when (val result = loadData()) {
            "-1" -> {
                textView.text = ("Project with given ID already exists.")
            }
            "Load Error" -> {
                textView.text = result
            }
            else -> {
                textView.text = ("Added project with id: $id. \n" +
                                 "Loaded $result new elements.")
            }
        }
        Toast.makeText(applicationContext, "Added project id: $id, name: ", Toast.LENGTH_SHORT).show()

        projectNameText.isEnabled = false
        addButton.isEnabled = false
    }

    private fun loadData(): String {
        var result = ""

        val path = filesDir
        val inDir = File(path, "XML")

        if (inDir.exists()) {
            val file = File(inDir, fileName)
            if (file.exists()) {
                val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)

                xmlDoc.documentElement.normalize()

                val items: NodeList = xmlDoc.getElementsByTagName("ITEM")

                val partsArray = ArrayList<InventoriesPart>()

                for (i in 0 until items.length) {
                    val itemNode: Node = items.item(i)

                    if (itemNode.nodeType == Node.ELEMENT_NODE) {

                        val part = InventoriesPart()

                        val elem = itemNode as Element
                        val children = elem.childNodes

                        for (j in 0 until children.length) {
                            val node = children.item(j)
                            if (node is Element) {
                                when (node.nodeName) {
                                    "ITEMTYPE" -> {
                                        part.itemType = node.textContent
                                    }
                                    "ITEMID" -> {
                                        part.itemID = node.textContent
                                    }
                                    "QTY" -> {
                                        part.qty = node.textContent.toInt()
                                    }
                                    "COLOR" -> {
                                        part.color = node.textContent.toInt()
                                    }
                                    "EXTRA" -> {
                                        part.extra = node.textContent
                                    }
                                    "ALTERNATE" -> {
                                        part.alternate = node.textContent
                                    }
                                    "MATCHID" -> {
                                        part.alternate = node.textContent
                                    }
                                    "COUNTERPART" -> {
                                        part.counterPart = node.textContent
                                    }
                                }
                            }
                        }
                        partsArray.add(part)
                    }
                }
                Toast.makeText(applicationContext, "Size: ${partsArray.size}", Toast.LENGTH_LONG).show()
                val databaseAccess = DatabaseAccess(
                    this
                ).getInstance(applicationContext)
                databaseAccess?.open()
                result = databaseAccess?.addInventory(id.toInt(), projectNameText.text.toString(), partsArray).toString()
                databaseAccess?.close()
            }
        }

        return if (result.isNotEmpty()) {
            result
        } else {
            "Load Error"
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class BgTask: AsyncTask<String, Int, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
            val testFile = File("$filesDir/XML/$fileName")
            var string = "Exist: ${testFile.exists()}. Length: ${testFile.length()}.\n"
            testFile.delete()
            string += "After deletion.\n"
            string += "Exist: ${testFile.exists()}. Length: ${testFile.length()}."
            textView.text = string
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            // unlock Add button
            if (result.equals("Success")) {
                addButton.isEnabled = true
                checkButton.isEnabled = false
                numberText.isEnabled = false
                Toast.makeText(applicationContext,"Success", Toast.LENGTH_SHORT).show()
            } else {
                addButton.isEnabled = false
                checkButton.isEnabled = true
                numberText.isEnabled = true
                Toast.makeText(applicationContext,"Error: $result", Toast.LENGTH_SHORT).show()
            }
        }

        override fun doInBackground(vararg params: String?): String {
            try {
                val url = URL( params[1] + params[0] + ".xml" )
                val conn = url.openConnection()
                conn.connect()
                val lengthOfFile = conn.contentLength
                val isStream = url.openStream()
                val testDirectory = File("$filesDir/XML")
                if (!testDirectory.exists())
                    testDirectory.mkdir()
                val fos = FileOutputStream("$testDirectory/$fileName")
                val data = ByteArray(1024)
                var total: Long = 0
                var progress = 0
                var count = isStream.read(data)
                while (count != -1) {
                    total += count.toLong()
                    val progressTemp = total.toInt() * 100 / lengthOfFile
                    if (progressTemp % 10 == 0 && progress != progressTemp)
                        progress = progressTemp
                    fos.write(data, 0, count)
                    count = isStream.read(data)
                }
                isStream.close()
                fos.close()
            } catch (e: MalformedURLException) {
                Log.i("File", "Malformed URL")
                return "Malformed URL"
            } catch (e: FileNotFoundException) {
                Log.i("File", "File not found")
                return "File not found"
            } catch (e: IOException) {
                Log.i("File", "IO Exception")
                return "IO Exception"
            }
            Log.i("File", "Success")
            return "Success"
        }
    }
}
