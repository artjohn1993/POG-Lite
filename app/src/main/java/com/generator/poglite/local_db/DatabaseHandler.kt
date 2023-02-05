package com.generator.poglite.local_db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.icu.util.Calendar
import android.os.Build
import androidx.annotation.RequiresApi
import com.generator.poglite.enum.Table
import com.generator.poglite.enum.WebOpenerDB
import com.generator.poglite.model.RangeData
import com.generator.poglite.model.URLData
import com.generator.poglite.model.Wordpress

class DatabaseHandler(val context : Context) : SQLiteOpenHelper(context, WebOpenerDB.DATABASE_NAME.getValue(), null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE " + WebOpenerDB.TABLE_URL.getValue() + " (" +
                Table.Table_Url.URL.getValue() + " VARCHAR(200)," +
                Table.Table_Url.DAYS.getValue() + " VARCHAR(200)," +
                Table.Table_Url.PAGES.getValue() + " VARCHAR(200)," +
                Table.Table_Url.PAUSEFROM.getValue() + " VARCHAR(200)," +
                Table.Table_Url.PAUSETO.getValue() + " VARCHAR(200))"
        )

        db?.execSQL("CREATE TABLE " + WebOpenerDB.TABLE_FACTOR.getValue() + " (" +
                Table.Table_Factor.FACTOR.getValue() + " VARCHAR(200))"
        )

        db?.execSQL("CREATE TABLE " + WebOpenerDB.TABLE_RANGE.getValue() + " (" +
                Table.Table_Range.RANGE_OF_POST.getValue() + " VARCHAR(200)," +
                Table.Table_Range.RANGE_TO_LOAD.getValue() + " VARCHAR(200))"
        )

        db?.execSQL("CREATE TABLE " + WebOpenerDB.TABLE_WORDPRESS.getValue() + " (" +
                Table.Table_Wordpress.TITLE.getValue() + " VARCHAR(200)," +
                Table.Table_Wordpress.DATE.getValue() + " VARCHAR(200), " +
                Table.Table_Wordpress.GROUP.getValue() + " VARCHAR(200), " +
                Table.Table_Wordpress.LINK.getValue() + " VARCHAR(200))"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    fun insertURL(data : URLData.Details) : Boolean {
        val db = this.writableDatabase
        var url = ContentValues()

        url.put(Table.Table_Url.URL.getValue(), data.url)
        url.put(Table.Table_Url.DAYS.getValue(), data.days)
        url.put(Table.Table_Url.PAGES.getValue(), data.pages)
        url.put(Table.Table_Url.PAUSEFROM.getValue(), data.pauseFrom)
        url.put(Table.Table_Url.PAUSETO.getValue(), data.pauseTo)
        var result = db.insert(WebOpenerDB.TABLE_URL.getValue(), null , url)
        db.close()
        return result != (-1).toLong()
    }

    fun insertRange(toLoad : String, ofPost: String) : Boolean {
        deleteDatabase(WebOpenerDB.TABLE_RANGE.getValue())
        val db = this.writableDatabase
        var range = ContentValues()

        range.put(Table.Table_Range.RANGE_OF_POST.getValue(), ofPost)
        range.put(Table.Table_Range.RANGE_TO_LOAD.getValue(), toLoad)
        var result = db.insert(WebOpenerDB.TABLE_RANGE.getValue(), null , range)
        db.close()
        return result != (-1).toLong()
    }

    fun insertWordpress(data : Wordpress.Result, group : String) : Boolean {
        val db = this.writableDatabase
        var wordpress = ContentValues()

        wordpress.put(Table.Table_Wordpress.TITLE.getValue(), data.title.rendered)
        wordpress.put(Table.Table_Wordpress.LINK.getValue(), data.link)
        wordpress.put(Table.Table_Wordpress.DATE.getValue(), data.date)
        wordpress.put(Table.Table_Wordpress.GROUP.getValue(), group)
        var result = db.insert(WebOpenerDB.TABLE_WORDPRESS.getValue(), null , wordpress)
        db.close()
        return result != (-1).toLong()
    }

    fun insertFactor(data : String) : Boolean {
        val db = this.writableDatabase
        var interval = ContentValues()

        interval.put(Table.Table_Factor.FACTOR.getValue(), data)
        var result = db.insert(WebOpenerDB.TABLE_FACTOR.getValue(), null , interval)
        db.close()
        return result != (-1).toLong()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getURL() : MutableList<URLData.Details>{
        val list : MutableList<URLData.Details> = ArrayList()
        val db = this.readableDatabase
        val result = db.rawQuery("SELECT * from " + WebOpenerDB.TABLE_URL.getValue(), null)
        if (result.moveToFirst()) {
            do {
                var urlData = URLData.Details(
                    result.getString(result.getColumnIndex(Table.Table_Url.URL.getValue())),
                    "",
                    result.getString(result.getColumnIndex(Table.Table_Url.DAYS.getValue())),
                    result.getString(result.getColumnIndex(Table.Table_Url.PAGES.getValue())),
                    result.getString(result.getColumnIndex(Table.Table_Url.PAUSEFROM.getValue())),
                    result.getString(result.getColumnIndex(Table.Table_Url.PAUSETO.getValue())),
                )
                val timeToMatch = Calendar.getInstance()
                var currentHour = timeToMatch[Calendar.HOUR_OF_DAY]
                var currentMinute = timeToMatch[Calendar.MINUTE]
                var pauseFrom = urlData.pauseFrom.split(":").toTypedArray()
                var pauseTo = urlData.pauseTo.split(":").toTypedArray()

                if(!checkRange(pauseFrom, pauseTo, currentHour, currentMinute)) {
                    list.add(urlData)
                }
            }while (result.moveToNext() )
        }
        db.close()
        return list
    }

    fun checkRange(pauseFrom: Array<String>, pauseTo : Array<String> , currentHour: Int, currentMinute: Int) : Boolean {
        if (currentHour in pauseFrom[0].toInt()..pauseTo[0].toInt()) {
            if(currentHour == pauseFrom[0].toInt()) {
                if(pauseFrom[1].toInt() > currentMinute) {
                    return false
                }
            }
            else if(currentHour == pauseTo[0].toInt()) {
                if(pauseTo[1].toInt() < currentMinute) {
                    return false
                }
            }

            return true
        }

        return false
    }

    fun getWordpress() : MutableList<Wordpress.Result>{
        val list : MutableList<Wordpress.Result> = ArrayList()
        val db = this.readableDatabase
        val result = db.rawQuery("SELECT * from " + WebOpenerDB.TABLE_WORDPRESS.getValue(), null)
        if (result.moveToFirst()) {
            do {
                var data = Wordpress.Result(
                        result.getString(result.getColumnIndex(Table.Table_Wordpress.GROUP.getValue())),
                        result.getString(result.getColumnIndex(Table.Table_Wordpress.LINK.getValue())),
                        result.getString(result.getColumnIndex(Table.Table_Wordpress.DATE.getValue())),
                Wordpress.Title(result.getString(result.getColumnIndex(Table.Table_Wordpress.TITLE.getValue()))))
                list.add(data)
            }while (result.moveToNext() )
        }
        db.close()
        return list
    }

    fun getRange() : RangeData.Result? {
        var data : RangeData.Result? = null
        val db = this.readableDatabase
        val result = db.rawQuery("SELECT * from " + WebOpenerDB.TABLE_RANGE.getValue(), null)
        if (result.moveToFirst()) {
            do {
                data = RangeData.Result(
                        result.getString(result.getColumnIndex(Table.Table_Range.RANGE_TO_LOAD.getValue())),
                        result.getString(result.getColumnIndex(Table.Table_Range.RANGE_OF_POST.getValue()))
                )
            }while (result.moveToNext() )
        }
        db.close()
        return data
    }

    fun deleteDatabase(data : String) {
        val db = this.writableDatabase
        db.delete(data,null,null)
        db.close()
    }

}