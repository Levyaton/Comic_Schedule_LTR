package com.example.comicscheduleltr

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHandler(context: Context): SQLiteOpenHelper(context,DATABASE_title,null,DATABASE_VERSION) {
    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_title = "ComicDatabase"
        private val TABLE_CONTACTS = "ComicTable"
        private val KEY_ID = "id"
        private val KEY_TITLE = "title"
        private val KEY_PUBLISHER = "publisher"
        private val KEY_DATE = "date"
    }
    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_CONTACTS_TABLE = ("CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TITLE + " TEXT," + KEY_PUBLISHER + " TEXT,"
                + KEY_DATE + " TEXT" + ")")
        db?.execSQL(CREATE_CONTACTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS)
        onCreate(db)
    }


    /**
     * TO DO
     */
    fun addComic(comic: Comic_Database):Long{
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_ID, comic.comicId)
        contentValues.put(KEY_TITLE, comic.title)
        contentValues.put(KEY_PUBLISHER, comic.publisher)
        contentValues.put(KEY_DATE,comic.date)
        //adds Row
        //db.insert(TABLE_CONTACTS, null, contentValues)
        val success = db.insert(TABLE_CONTACTS, null, contentValues)
        db.close()
        return success
    }

    /**
     * TO DO
     */
    fun viewComics():List<Comic_Database>{
        val comics:ArrayList<Comic_Database> = ArrayList<Comic_Database>()
        val selectQuery = "SELECT  * FROM $TABLE_CONTACTS"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try{
            cursor = db.rawQuery(selectQuery, null)
        }catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        var comicId: Int
        var title: String
        var publisher: String
        var date: String
        if (cursor.moveToFirst()) {
            do {
                comicId = cursor.getInt(cursor.getColumnIndex("id"))
                title = cursor.getString(cursor.getColumnIndex("title"))
                publisher = cursor.getString(cursor.getColumnIndex("publisher"))
                date = cursor.getString(cursor.getColumnIndex("date"))
                val comic = Comic_Database(comicId = comicId,title = title, publisher = publisher, date = date)
                comics.add(comic)
            } while (cursor.moveToNext())
        }
        return comics
    }

    /**
     * TO DO
     */
    fun updateComic(comic: Comic_Database):Int{
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_ID, comic.comicId)
        contentValues.put(KEY_TITLE,comic.title)
        contentValues.put(KEY_PUBLISHER, comic.publisher)
        contentValues.put(KEY_DATE,comic.date )

      //updates Row
        val success = db.update(TABLE_CONTACTS, contentValues,"id="+comic.comicId,null)
        
        db.close() 
        return success
    }

    /**
     * TO DO
     */
    fun deleteComic(comic: Comic_Database):Int{
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_TITLE, comic.title)
       //deletes Row
        val success = db.delete(TABLE_CONTACTS,"id="+comic.comicId,null)
        db.close()
        return success
    }
}
