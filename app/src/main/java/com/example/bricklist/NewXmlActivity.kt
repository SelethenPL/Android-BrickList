package com.example.bricklist

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
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

    var text : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun downloadData() {
        val bg = BgTask()
        bg.execute()
    }
    fun refresh(v: View) {
        downloadData()
    }

    private inner class BgTask: AsyncTask<String, Int, String>() {
        private var link : String = "http://fcds.cs.put.poznan.pl/MyWeb/BL/"

        override fun onPreExecute() {
            super.onPreExecute()
            link += editText.text.toString() + ".xml"
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val string = loadData()
            textView.text = string
        }

        fun loadData(): String? {
            var result = ""

            val filename = "waluty.xml"
            val path = filesDir
            val inDir = File(path, "XML")

            if (inDir.exists()) {
                val file = File(inDir, filename)
                if (file.exists()) {
                    val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)

                    xmlDoc.documentElement.normalize()

                    val items: NodeList = xmlDoc.getElementsByTagName("ITEM")

                    for (i in 0 until items.length) {
                        val itemNode: Node = items.item(i)

                        if (itemNode.nodeType == Node.ELEMENT_NODE) {

                            val elem = itemNode as Element
                            val children = elem.childNodes

                            for (j in 0 until children.length) {
                                val node = children.item(j)
                                if (node is Element) {
                                    result += node.nodeName + " " + node.textContent + "\n"
                                }
                            }
                        }
                    }
                }
            }
            if (result.isNotEmpty())
                return result
            else
                return "File could not be loaded"
        }

        override fun doInBackground(vararg params: String?): String {
            try {
                val url = URL( link )
                val conn = url.openConnection()
                conn.connect()
                val lengthOfFile = conn.contentLength
                val isStream = url.openStream()
                val testDirectory = File("$filesDir/XML")
                if (!testDirectory.exists())
                    testDirectory.mkdir()
                val fos = FileOutputStream("$testDirectory/waluty.xml")
                val data = ByteArray(1024)
                var count = 0
                var total: Long = 0
                var progress = 0
                count = isStream.read(data)
                while (count != -1) {
                    total += count.toLong()
                    val progress_temp = total.toInt() * 100 / lengthOfFile
                    if (progress_temp % 10 == 0 && progress != progress_temp)
                        progress = progress_temp
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

    /* Database products functions
    fun newProduct(view: View) {

        val dbHandler = DbHandler(this, null, null, 1)
        val qty = Integer.parseInt(productQuantity.text.toString())
        val product = Product(productName.text.toString(), qty)

        dbHandler.addProduct(product)
        productName.setText("")
        productQuantity.setText("")
    }

    fun lookupProduct(view: View) {
        val dbHandler = DbHandler(this, null, null, 1)
        val product = dbHandler.findProduct(
            productName.text.toString()
        )
        if (product != null) {
            productID.text = product.id.toString()
            productQuantity.setText(product.qty.toString())
        } else {
            productID.text = "Not found"
        }
    }

    fun removeProduct(view: View) {
        val dbHandler = DbHandler(this, null, null, 1)
        val result = dbHandler.deleteProduct(
            productName.text.toString()
        )
        if (result) {
            productID.text = "Product deleted"
            productName.setText("")
            productQuantity.setText("")
        } else {
            productID.text = "Not found"
        }
    } */
}
