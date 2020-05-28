package com.example.bricklist

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

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
        cursor = db?.rawQuery("SELECT Name FROM Colors", arrayOf())
        val buffer = StringBuffer()
        while (cursor?.moveToNext()!!) {
            buffer.append("" + cursor?.getString(0))
        }
        return buffer.toString()
    }

    /*
    fun addProduct(product: Product) {
        val values = ContentValues()
        values.put(COLUMN_PRODUCTNAME, product.productName)
        values.put(COLUMN_QUANTITY, product.qty)
        val db = this.writableDatabase
        db.insert(TABLE_PRODUCTS, null, values)
        db.close()
    }

    fun deleteProduct(productName: String) : Boolean {
        var result = false
        val query = "SELECT * FROM $TABLE_PRODUCTS WHERE $COLUMN_PRODUCTNAME LIKE \"$productName\""

        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            val id = Integer.parseInt(cursor.getString(0))
            db.delete(TABLE_PRODUCTS, "$COLUMN_ID = ?", arrayOf(id.toString()))
            cursor.close()
            result = true
        }
        db.close()
        return result
    }
    */

/*
    fun findProduct(colorCode: String) : String {
        val query = "SELECT * FROM $TABLE_COLORS WHERE $COLUMN_CODE = $colorCode"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        var result: String = ""

        if (cursor.moveToFirst()) {
            result += cursor.getString(0) + " "
            result += cursor.getString(1) + " "
            result += cursor.getString(2) + "\n"
            cursor.close()
        }
        db.close()
        if (result=="")
            return "Not found"
        return result
    }*/

}