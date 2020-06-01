package com.example.bricklist.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.BitmapFactory
import android.widget.Toast
import com.example.bricklist.models.InventoriesPart
import com.example.bricklist.models.PartsDetailed
import com.example.bricklist.models.PartsXMLDownload
import java.net.URL

class DatabaseAccess(context: Context) {

    private var openHelper: SQLiteOpenHelper? = null
    private var db: SQLiteDatabase? = null
    private var instance: DatabaseAccess? = null
    private var cursor: Cursor? = null
    private val myContext = context

    init {
        openHelper = DatabaseHelper(context)
    }

    fun getInstance(context: Context): DatabaseAccess? {
        if (instance == null) {
            instance = DatabaseAccess(context)
        }
        return instance
    }
    fun open() {
        db = openHelper!!.writableDatabase
    }
    fun close() {
        if (db != null)
            db!!.close()
    }

    private fun getItemTypesID(code: String): Int {
        cursor = db?.rawQuery("SELECT id FROM ItemTypes WHERE Code=\"$code\"", null)
        var result = 0
        if (cursor?.moveToFirst()!!) {
            result = cursor?.getString(0)?.toInt()!!
        }
        return result
    }
    private fun getPartsID(code: String): Int {
        cursor = db?.rawQuery("SELECT id FROM Parts WHERE Code=\"$code\"", null)
        var result = 0
        if (cursor?.moveToFirst()!!) {
            result = cursor?.getString(0)?.toInt()!!
        }
        return result
    }

    fun getProjectList(onlyActive: Boolean = true): ArrayList<Pair<Int, String>> {
        val result = ArrayList<Pair<Int, String>>()

        cursor = if (onlyActive)
            db?.rawQuery("SELECT id, Name FROM Inventories WHERE Active=1", null)
        else
            db?.rawQuery("SELECT id, Name FROM Inventories", null)

        while (cursor?.moveToNext()!!) {
            val id = cursor?.getInt(0)!!
            val name = cursor?.getString(1)!!
            result.add( Pair(id, name) )
        }
        return result
    }

    fun activate_deactivate(id: Int): Boolean? {
        cursor = db?.rawQuery(
            "SELECT Active FROM Inventories WHERE id = $id",
            null)

        if (cursor?.moveToFirst()!!) {
            val result = cursor?.getString(0)?.toInt()!!
            if (result == 0) {
                db?.execSQL(
                    "UPDATE Inventories SET Active = 1 WHERE id = ?", arrayOf(id))
                return true
            }
            else if (result == 1) {
                db?.execSQL(
                    "UPDATE Inventories SET Active = 0 WHERE id = ?", arrayOf(id))
                return false
            }
        }
        return null
    }

    fun updateQty(partID: Int, value: String): Boolean? {
        if (value == "MINUS") {
            db?.execSQL(
                "UPDATE InventoriesParts " +
                        "SET QuantityInStore = QuantityInStore - 1 " +
                        "WHERE id=$partID AND 0 < QuantityInStore")
            return false
        } else if (value == "PLUS") {
            db?.execSQL(
                "UPDATE InventoriesParts " +
                        "SET QuantityInStore = QuantityInStore + 1 " +
                        "WHERE id=$partID AND QuantityInSet > QuantityInStore")
            return true
        }
        return null
    }
    
    fun getPartsList(projectID: Int): ArrayList<PartsDetailed> {
        val result = ArrayList<PartsDetailed>()

        cursor = db?.rawQuery( // ID, ColorName, Name, Code, QtyInSet, QtyInStore
            "SELECT ip.id, color.Name, p.Name, p.Code, ip.QuantityInSet, ip.QuantityInStore " +
                    "FROM InventoriesParts ip " +
                    "LEFT JOIN Colors color on (ip.ColorID = color.Code) " +
                    "LEFT JOIN Parts p on (ip.ItemID=p.id)" +
                    "WHERE InventoryID = $projectID",
            null )

        while (cursor?.moveToNext()!!) {
            val partDetails = PartsDetailed()
            partDetails.id = cursor?.getInt(0)!!
            partDetails.colorName = cursor?.getString(1)!!
            partDetails.name = cursor?.getString(2)!!
            partDetails.code = cursor?.getString(3)!!
            partDetails.qtyInSet = cursor?.getInt(4)!!
            partDetails.qtyInStock = cursor?.getInt(5)!!
            result.add(partDetails)
        }
        return result
    }

    private fun checkProjectExist(id: Int): Boolean {
        cursor = db?.rawQuery("SELECT * FROM Inventories WHERE id=\"$id\"", null)
        var result = false
        if (cursor?.moveToFirst()!!) {
            result = true
        }
        return result
    }

    /**
     * Adding new project to Inventories table.
     * Adding every part to InventoriesParts table.
     * @return number of added elements
     */
    fun addInventory(id: Int, name: String, parts: ArrayList<InventoriesPart>): Int {

        // Project already exists
        if (checkProjectExist(id))
            return -1

        val values = ContentValues()
        values.put("id", id)
        values.put("Name", name)
        values.put("Active", 1)
        values.put("LastAccessed", 0)
        db?.insert("Inventories", null, values)
        var result = 0

        for (part in parts) {
            val partValues = ContentValues()
            val typeID = getItemTypesID(part.itemType)
            val itemID = getPartsID(part.itemID)

            if (part.alternate == "N") {
                continue
            }
            if (typeID == 0 || itemID == 0) {
                continue
            }

            partValues.put("InventoryID", id)
            partValues.put("TypeID", typeID) // get from ItemTypes
            partValues.put("ItemID", itemID) // get from Parts
            partValues.put("ColorID", part.color)
            partValues.put("QuantityInSet", part.qty)
            partValues.put("QuantityInStore", 0)
            if (part.extra == "N") partValues.put("Extra", 0)
            else partValues.put("Extra", 1)
            db?.insert("InventoriesParts", null, partValues)
            result++
        }

        return result
    }

    fun getInventoriesPartsForXML(inventoryID: Int): ArrayList<PartsXMLDownload> {
        cursor = db?.rawQuery(
            "select it.code as ItemType, ip.ColorID, p.Code, ip.QuantityInSet, ip.QuantityInStore\n" +
                "FROM InventoriesParts ip\n" +
                "JOIN ItemTypes it on (ip.TypeID=it.id)\n" +
                "JOIN Parts p on (ip.ItemID=p.id)\n" +
                "WHERE InventoryID=$inventoryID", null)

        val result = ArrayList<PartsXMLDownload>()

        while (cursor?.moveToNext()!!) {
            val part = PartsXMLDownload()
            part.itemType = cursor!!.getString(0)
            part.colorID = cursor!!.getInt(1)
            part.itemID = cursor!!.getString(2)
            part.qtyInSet = cursor!!.getInt(3)
            part.qtyInStock = cursor!!.getInt(4)
            result.add(part)
        }
        return result
    }

    fun clearInventories() {
        db?.execSQL("DELETE FROM Inventories")
        db?.execSQL("DELETE FROM InventoriesParts")
    }

    fun checkImageExist(id: Int): String {
        cursor = db?.rawQuery(
            "SELECT c.Code, c.Image, p.Code, ip.colorID " +
                    "FROM Codes c " +
                    "JOIN InventoriesParts ip " +
                        "ON (ip.ItemID = c.ItemID) and (ip.ColorID = c.ColorID) " +
                    "JOIN Parts p on (p.id = c.ItemID) " +
                    "WHERE ip.id = $id", null)

        if (cursor?.moveToFirst()!!) {
            // code found, try to get more
            val designID = cursor?.getInt(0)
            var image = cursor?.getString(1)
            val code = cursor?.getString(2)
            val colorID = cursor?.getInt(3)

            // image in database return link
            if (image != null)
                return image

            image = getImageURL(designID, colorID, code)
            return image
        }

        cursor = db?.rawQuery(
            "SELECT p.Code, ip.colorID, ip.itemID " +
                    "FROM InventoriesParts ip " +
                    "JOIN Parts p on (p.id = ip.ItemID) " +
                    "WHERE ip.id = $id", null)

        if (cursor?.moveToFirst()!!) {
            val code = cursor?.getString(0)
            val colorID = cursor?.getInt(1)
            val itemID = cursor?.getInt(2)
            val image = getImageURL(null, colorID, code)

            val values = ContentValues()
            values.put("ItemID", itemID)
            values.put("ColorID", colorID)
            values.put("Image", image)
            db?.insert("Codes", null, values)
            return image
        }

        return "https://www.teknozeka.com/wp-content/uploads/2020/03/wp-header-logo-21.png"
    }

    private fun getImageURL(designID: Int?, colorCode: Int?, code: String?): String {
        try {
            // 1. https://www.lego.com/service/bricks/5/2/{DesignID}
            val link = "https://www.lego.com/service/bricks/5/2/$designID"
            val url = URL(link)
            val image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            return link

        } catch (e: Exception) { }

        try {
            // 2. http://img.bricklink.com/P/$colorCode/3001old.gif
            val link = "http://img.bricklink.com/P/$colorCode/$code.gif"
            val url = URL(link)
            val image = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            return link
        } catch (e: Exception) { }

        try {
            // 3. https://www.bricklink.com/PL/3430c02.jpg
            val link = "https://www.bricklink.com/PL/$code.jpg"
            val url = URL(link)
            val image = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            return link
        } catch (e: Exception) { }

        return ""
    }

}