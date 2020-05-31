package com.example.bricklist.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.bricklist.models.InventoriesPart
import com.example.bricklist.models.PartsDetailed

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

    fun getColors(): String {
        cursor = db?.rawQuery("SELECT Name FROM Colors", null)
        val buffer = StringBuffer()
        if (cursor?.moveToFirst()!!) {
            buffer.append("" + cursor?.getString(0) + "\n")
        }
        return buffer.toString()
    }

    private fun getItemTypesID(code: String): Int {
        cursor = db?.rawQuery("SELECT id FROM ItemTypes WHERE Code=\"$code\"", null)
        var result = 0
        if (cursor?.moveToFirst()!!) {
            result = cursor?.getString(0)?.toInt()!!        }
        return result
    }

    private fun getPartsID(code: String): Int {
        cursor = db?.rawQuery("SELECT id FROM Parts WHERE Code=\"$code\"", null)
        var result = 0
        if (cursor?.moveToFirst()!!) {
            result = cursor?.getString(0)?.toInt()!!        }
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

    /**
     * Returns array of specially declared Part Detail object.
     */
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


    fun clearInventories() {
        db?.execSQL("DELETE FROM Inventories")
        db?.execSQL("DELETE FROM InventoriesParts")
    }

}