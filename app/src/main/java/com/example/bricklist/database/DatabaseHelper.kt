package com.example.bricklist.database

import android.content.Context
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper

class DatabaseHelper (context: Context)
    : SQLiteAssetHelper(context,
    DATABASE_NAME, null,
    DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "BrickList.db"
        private const val DATABASE_VERSION = 1
    }

}