package com.example.bricklist

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHandler(context: Context,
                         name: String?,
                         factory: SQLiteDatabase.CursorFactory?,
                         version: Int) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {
    companion object {
        private val DATABASE_NAME = "productDB.db"
        private val DATABASE_VERSION = 1
        val TABLE_PRODUCTS = "products"
        val COLUMN_ID = "_id"
        val COLUMN_PRODUCTNAME = "productname"
        val COLUMN_QUANTITY = "quantity"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_PRODUCTS_TABLE = ("CREATE TABLE " +
                TABLE_PRODUCTS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY," +
                COLUMN_PRODUCTNAME + " TEXT," +
                COLUMN_QUANTITY + " INTEGER )")
        db.execSQL(CREATE_PRODUCTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS)
        onCreate(db)
    }


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
            db.delete(TABLE_PRODUCTS, COLUMN_ID + "= ?", arrayOf(id.toString()))
            cursor.close()
            result = true
        }
        db.close()
        return result
    }

    fun findProduct(productName: String) : Product? {
        val query = "SELECT * FROM $TABLE_PRODUCTS WHERE $COLUMN_PRODUCTNAME LIKE \"$productName\""
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        var product: Product? = null

        if (cursor.moveToFirst()) {
            val id = Integer.parseInt(cursor.getString(0))
            val name = cursor.getString(1)
            val qty = Integer.parseInt(cursor.getString(2))
            product = Product(id, name, qty)
            cursor.close()
        }
        db.close()
        return product
    }

}