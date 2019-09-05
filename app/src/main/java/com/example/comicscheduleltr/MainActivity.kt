package com.example.comicscheduleltr

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.File
import java.io.FileWriter
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }



    override fun onStart() {
        super.onStart()
        //proccessComics(getWeekDate(true))
        //proccessComics(getWeekDate(true))
        //testOldDate()
        dbUpdater()
        Log.d("Comics Updated", "Done")
        datePicker()
    }


    private fun datePicker() {
        val linLay: LinearLayout = LinearLayout(this)

        linLay.orientation = LinearLayout.VERTICAL
        linLay.gravity = Gravity.CENTER

        val tButton: Button = Button(this)
        val tButtonText: String = "This weeks comics"
        tButton.text = tButtonText
        tButton.setOnClickListener {
            val intent = Intent(this, PublishersActivity::class.java)
            intent.putExtra("date", getWeekDate(true))
            this.startActivity(intent)
        }

        val nButton: Button = Button(this)
        val nButtonText: String = "Next weeks comics"
        nButton.text = nButtonText
        nButton.setOnClickListener {
            val intent = Intent(this, PublishersActivity::class.java)
            intent.putExtra("date", getWeekDate(false))
            this.startActivity(intent)
        }

        linLay.addView(tButton)
        linLay.addView(nButton)
        setContentView(linLay)
    }

    private fun dbUpdater(){
        val dh: DatabaseHandler = DatabaseHandler(this)
        val thisWeek = getWeekDate(true)
        val nextWeek = getWeekDate(false)
        var foundThisWeek = false
        var foundNextWeek = false
        for(comic in dh.viewComics()){
            if(comic.date != thisWeek && comic.date != nextWeek){
                dh.deleteComic(comic)
                File(this.filesDir,comic.title + ".json").delete()
                Log.d("Deleted", comic.title)
            }
            else if(comic.date == thisWeek){
                foundThisWeek = true
            }
            else if(comic.date == nextWeek){
                foundNextWeek = true
            }
        }
        if(!foundThisWeek){
            proccessComics(getWeekDate(true))
        }
        if(!foundNextWeek){
            proccessComics(getWeekDate(false))
        }
    }

    private fun getWeekDate(thisWeek: Boolean): String{
        val cal: Calendar = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        if(!thisWeek){
            cal.add(Calendar.DAY_OF_YEAR, + 7)
        }
        val sdf = android.icu.text.SimpleDateFormat("yyyy-MM-dd")
        val date: Date = cal.time
        return sdf.format(date)
    }

    private fun linkMaker(date: String): String{
        val intYear = Calendar.getInstance().get(Calendar.YEAR)
        val intMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        val intDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        var year = intYear.toString()
        var day = intDay.toString()
        if(intDay<10){
            day = "0" + day
        }
        var month = intMonth.toString()
        if(intMonth<10){
            month = "0" + month
        }
        var link: String  = "https://freshcomics.us/issues/"
        link += date
        return link
    }


    private fun proccessComics(date: String){
        val cg = ComicGenerator(this,date,linkMaker(date))
        val comics: ArrayList<Comic> = cg.comcis
        Log.d("ComicNum", comics.size.toString())
        generateDatabase(comics)
        generateJsonComics(comics)
    }


    //for testing purposes
    private fun testOldDate(){
        val cg = ComicGenerator(this,"2019-08-23",linkMaker("2019-08-23"))
        val comics: ArrayList<Comic> = cg.comcis
        Log.d("ComicNum", comics.size.toString())
        generateJsonComics(comics)
        generateDatabase(comics)
    }

    private fun generateJsonComics(comics: ArrayList<Comic>){
        
        for(comic in comics){
            Log.d("comic title", comic.title)
            val gson = Gson()
            val Jcomic: JsonObject = JsonObject()
            var jsonString:String = gson.toJson(comic.toJComic())
            val file: File = File(this.filesDir.path +"/" + comic.title + ".json")
            //file.mkdir()
            //file.mkdirs()
            file.writeText(jsonString)
            
        }
    }
    
    private fun generateDatabase(comics: ArrayList<Comic>){
        Log.d("The number should be", comics.size.toString())
        //val comics: ArrayList<ComicDatabase> = arrayListOf()
        val dh: DatabaseHandler = DatabaseHandler(this)

        var idCounter: Int = dh.viewComics().size + 1
        
        for(comic in comics){
            dh.addComic(Comic_Database(idCounter,comic.title,comic.publisher,comic.date))
            idCounter++
           // val db: SugarRecord = ComicDatabase(comic.date,comic.publisher,comic.title)
            //val db = ComicDatabase()
            //db.id = null

            //SugarRecord.save(db)
            //val db: SQLiteDatabase = SugarDb(this).
            //SugarRecord.save(cd)
            //comics.add(cd)
        }
        //SugarRecord.saveInTx(comics)
        //Log.d("comic number in database", comics.size.toString())

        val cms = dh.viewComics()
        Log.d("comic number in database",cms.size.toString())

    }


}
