package com.example.InventoryManagementApp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DBHandler(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    // below is the method for creating a database by a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        // below is a sqlite query, where column names
        // along with their data types is given
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY, " +
                ITEM_NAME + " TEXT UNIQUE," +
                ITEM_QUANTITY + " INTEGER, " +
                ITEM_PRICE + " FLOAT, " +
                TOTAL_VALUE + " FLOAT, " +
                IMAGE_URI + " TEXT " + ")") // Added space before FLOAT


        // we are calling sqlite
        // method for executing our query
        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        // this method is to check if table already exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }
    fun updateItemEntry(id: Int, itemName: String, itemQuantity: Int, itemPrice: Float, totalValue: Float, callback: UpdateCallback) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(ITEM_NAME, itemName)
            put(ITEM_QUANTITY, itemQuantity)
            put(ITEM_PRICE, itemPrice)
            put(TOTAL_VALUE, totalValue)
        }

        CoroutineScope(Dispatchers.IO).launch {
            var success=false
            try {
                // Code to update the item
                success = db.update(TABLE_NAME, values, "$ID_COL=?", arrayOf(id.toString())) > 0
                db.close()
                withContext(Dispatchers.Main) {
                    callback.onUpdate(success)
                }
            } catch (e: SQLiteConstraintException) {
                // Handle the exception
                withContext(Dispatchers.Main){
                    callback.onUpdate(success)
                }
            }
        }
    }
    fun removeItemEntry(id: Int): Boolean {
        val db = this.writableDatabase
        var success = false
        try {
            success = db.delete(TABLE_NAME, "$ID_COL=?", arrayOf(id.toString())) > 0
            db.close()
        } catch (e: SQLiteException){
            Log.e("DBHandler",e.printStackTrace().toString())
        }
        return success
    }

    fun updateItemEntry(id: Int, itemName: String, itemQuantity: Int, itemPrice: Float, totalValue: Float,imageUri: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(ITEM_NAME, itemName)
            put(ITEM_QUANTITY, itemQuantity)
            put(ITEM_PRICE, itemPrice)
            put(TOTAL_VALUE, totalValue)
            put(IMAGE_URI,imageUri)
        }
        var success=false
        try {
            success = db.update(TABLE_NAME, values, "$ID_COL=?", arrayOf(id.toString())) > 0
            db.close()
            return success
        }catch (e: SQLiteException){
            Log.e("DBHandler",e.printStackTrace().toString())
        }
        return success
    }
    fun updateItemEntry(id: Int,imageUri: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(IMAGE_URI,imageUri)
        }
        val success = db.update(TABLE_NAME, values, "$ID_COL=?", arrayOf(id.toString()))
        db.close()
        return success > 0
    }
    fun updateItemEntry(id: Int,quantity: Int,value: Float, callback: UpdateCallback) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(ITEM_QUANTITY,quantity)
            put(TOTAL_VALUE,value)
        }

        CoroutineScope(Dispatchers.IO).launch {
            val success = db.update(TABLE_NAME, values, "$ID_COL=?", arrayOf(id.toString())) > 0
            db.close()

            withContext(Dispatchers.Main) {
                callback.onUpdate(success)
            }
        }
    }
    //     This method is for adding data in the database
    fun addItemEntry(itemName: String, itemQuantity: Int, itemPrice: Float, totalValue: Float, callback: UpdateCallback) {

        // Create a new map of values, where column names are the keys
        val values = ContentValues().apply {
            put(ITEM_NAME, itemName)
            put(ITEM_QUANTITY, itemQuantity)
            put(ITEM_PRICE, itemPrice)
            put(TOTAL_VALUE, totalValue)
            put(IMAGE_URI, "")
        }

        // Get writable database because we want to write data
        val db = this.writableDatabase

        try {
            // Insert the new row, returning the primary key value of the new row
            db.insertOrThrow(TABLE_NAME, null, values)
            // operation succeeded, callback with true
            callback.onUpdate(true)
        } catch (e: android.database.sqlite.SQLiteConstraintException) {
            // This block will be executed if the 'itemName' already exists in the database
            // Handle the exception here. For example, you could show a Toast message to user.
            Log.d("Insert Error:", e.printStackTrace().toString())
            // operation failed, callback with false
            callback.onUpdate(false)
        } finally {
            db.close()
        }
    }


    @SuppressLint("Range")
    fun getAll(): List<Item> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        val itemList = mutableListOf<Item>()

        if (cursor.moveToFirst()) {
            return getListFromCursor(cursor)
        }else{
            Log.d("DBHandlerGetAll","Cursor Not loaded")
            return emptyList()
        }

    }

    fun searchItemName(itemName: String): List<Item> {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $ITEM_NAME LIKE ?"
        val cursor = db.rawQuery(query, arrayOf("%$itemName%"))
        return if (cursor.moveToFirst()) {
                return getListFromCursor(cursor)
                } else{
                    Log.d("DBHandlerSearchItemName","Cursor Not loaded")
                    return emptyList()
                }
            }

    fun searchItemNameSpecific(itemName: String): Cursor? {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $ITEM_NAME = ?"
        val cursor = db.rawQuery(query, arrayOf(itemName))
        return if (cursor.moveToFirst()) {
            cursor // Return cursor if it contains data
        } else {
            null // Return null if cursor is empty
        }
    }

    @SuppressLint("Range")
    fun getListFromCursor(cursor: Cursor): List<Item> {
        var itemList= mutableListOf<Item>()
        do {
            var uri_ = Uri.parse(cursor.getString(cursor.getColumnIndex(DBHandler.IMAGE_URI)))
            if (uri_.toString().isEmpty()) {
                uri_ = null
            }

            val item = Item(
                cursor.getString(cursor.getColumnIndex(DBHandler.ID_COL)),
                cursor.getString(cursor.getColumnIndex(DBHandler.ITEM_NAME)),
                cursor.getString(cursor.getColumnIndex(DBHandler.ITEM_QUANTITY)),
                cursor.getString(cursor.getColumnIndex(DBHandler.ITEM_PRICE)),
                cursor.getString(cursor.getColumnIndex(DBHandler.TOTAL_VALUE)),
                uri_
            )
            itemList.add(item)
        } while (cursor.moveToNext())
        cursor.close()
        return itemList
    }

    companion object{
        // here we have defined variables for our database

        // below is variable for database name
        private val DATABASE_NAME = "InventoryManagementAppDB"

        // below is the variable for database version
        private val DATABASE_VERSION = 8

        // below is the variable for table name
        val TABLE_NAME = "itemsTable"

        // below is the variable for id column
        val ID_COL = "id"

        // below is the variable for item name column
        val ITEM_NAME = "itemName"

        // below is the variable for item quantity column
        val ITEM_QUANTITY = "itemQuantity"

        // below is the variable for item quantity column
        val ITEM_PRICE = "itemPrice"

        // below is the variable for item quantity column
        val TOTAL_VALUE= "totalValue"

        val IMAGE_URI= "imageUri"
    }
}