package com.example.comicscheduleltr

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Base64
import android.widget.Button
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.File
import java.lang.Exception
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlinx.coroutines.*
import java.io.IOException
import kotlin.system.measureTimeMillis
import android.graphics.BitmapFactory
import com.koushikdutta.async.future.Future
import java.net.HttpURLConnection
import com.koushikdutta.ion.Ion
import java.io.ByteArrayOutputStream



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
        setContentView(R.layout.waiting_layout)
        val time = measureTimeMillis {
            dbUpdater()
        }
        Log.d("Comics Updated", "Done")
        Log.d("Num of minutes operation took to execute", (time/60000).toString())
        datePicker()
    }


    private fun datePicker() {
        setContentView(R.layout.date_picker_layout)
        //val lay = findViewById<LinearLayoutCompat>(R.id.dp)
        val tButton: Button = findViewById(R.id.TW)
        val tButtonText: String = "This weeks comics"
        tButton.text = tButtonText
        tButton.setOnClickListener {
            val intent = Intent(this, PublishersActivity::class.java)
            intent.putExtra("date", getWeekDate(true))
            this.startActivity(intent)
        }

        val nButton: Button = findViewById(R.id.NW)
        val nButtonText: String = "Next weeks comics"
        nButton.text = nButtonText
        nButton.setOnClickListener {
            val intent = Intent(this, PublishersActivity::class.java)
            intent.putExtra("date", getWeekDate(false))
            this.startActivity(intent)
        }
        val reloadButton: Button = findViewById(R.id.RL)
        reloadButton.setOnClickListener {
            //setContentView(R.layout.waiting_layout)
            val dh: DatabaseHandler = DatabaseHandler(this)
            val thisWeek = getWeekDate(true)
            val nextWeek = getWeekDate(false)
            for(comic in dh.viewComics()){
                    dh.deleteComic(comic)
                    File(this.filesDir,comic.title + ".json").delete()
                    Log.d("Deleted", comic.title)
            }
            proccessComics(getWeekDate(true))
            proccessComics(getWeekDate(false))
            Log.d("Reloading comics", "Finished")
            //setContentView(R.layout.date_picker_layout)
        }
        //setContentView(lay)

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
            var jsonString:String = gson.toJson(comic.toJComic(getCovers(comic.coverLinks)))
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

    private fun getCovers(coverLinks: ArrayList<String>): ArrayList<String> {
        val covers: ArrayList<String> = arrayListOf()
        var mainCover: Bitmap? = null
        for(cover in coverLinks){
            var ba: String? = null
            while (true){
                try{
                    runBlocking {
                        Log.d("Loading img", "Started")
                        //ba = loadImage(cover).await()

                        ba = async {
                            getStringFromBitmap(Ion.with(this@MainActivity).load(cover).asBitmap().get())
                        }.await()

                        Log.d("Loading img", "Ended")
                    }
                    break
                }
                catch (e: Exception){
                    Log.d("exception was", e.toString())
                    Log.d("Retrying", cover)
                    Thread.sleep(50)
                }
            }
            covers.add(ba!!)
        }
        return covers
    }






    private suspend fun loadImage(url: String): Deferred<String> = GlobalScope.async {
            val image: String = async {
                  val url = URL(url)
                val img = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                val result = getStringFromBitmap(img)
                result

            }.await()

            return@async image
    }


    private fun encodeByteArr(covers: List<String>): String{
        var coded = ""
        for(cover in covers){
            coded += "$cover[*]"
        }
        coded = coded.substring(0,coded.length - 3)
        return coded
    }

    @Throws(IOException::class)
    private fun getByteArray(url: String): ByteArray {
        val url = URL(url)
        val connection = url.openConnection() as HttpURLConnection
        connection.setDoInput(true)
        connection.connect()
        val input = connection.getInputStream()
        val ba =input.readBytes()
        input.close()
        return ba
    }

    private fun getStringFromBitmap(bitmapPicture: Bitmap): String {
        val COMPRESSION_QUALITY = 100
        val encodedImage: String
        val byteArrayBitmapStream = ByteArrayOutputStream()
        bitmapPicture.compress(
            Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
            byteArrayBitmapStream
        )
        val b = byteArrayBitmapStream.toByteArray()
        encodedImage = Base64.encodeToString(b,Base64.DEFAULT)
        return encodedImage
    }


}
