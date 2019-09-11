package com.example.comicscheduleltr

import android.content.Context
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
import android.graphics.Color.RED
import android.graphics.Color.WHITE
import android.graphics.LinearGradient
import android.widget.LinearLayout
import android.widget.TextView
import com.koushikdutta.async.future.Future
import java.net.HttpURLConnection
import com.koushikdutta.ion.Ion
import java.io.ByteArrayOutputStream
import java.lang.reflect.Array


/**
 * TO DO
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    var useWebView: Boolean = false

    override fun onStart() {
        super.onStart()
        //proccessComics(getWeekDate(true))
        //proccessComics(getWeekDate(true))
        //testOldDate()
        val time = measureTimeMillis {
            dbUpdater()
        }
        Log.d("Comics Updated", "Done")
        Log.d("Num of minutes operation took to execute", (time/60000).toString())
        datePicker()
    }

    /**
     *  Let's the user choose which weeks comics they would like to view
     */

    private fun datePicker() {
        setContentView(R.layout.date_picker_layout)
        //val lay = findViewById<LinearLayoutCompat>(R.id.dp)
        setThisWeekButton()
        setNextWeekButton()
        setReloadButton()
        setWebViewButton()
        //setContentView(lay)

    }

    /**
     * Sets up a waiting screen content view
     */
    private fun setWaitingScreen(){
        val waitingText = TextView(this)
        val text: String = "Your comic books are loading, this will take about 10 - 15 minutes, please wait"
        waitingText.text = text
        waitingText.setTextColor(WHITE)
        val linLay = LinearLayout(this)
        linLay.setBackgroundColor(RED)
        linLay.addView(waitingText)
        setContentView(linLay)
    }

    /**
     * Sets up a web view button
     */
    private fun setWebViewButton(){
        val webButton: Button = findViewById(R.id.WebViewButton)
        webButton.setOnClickListener {
            var text = ""
            if(webButton.text ==  "Activate offline mode"){
                text = "Activate online mode"
                useWebView = false
            }else{
                text = "Activate offline mode"
                useWebView = true
            }
            webButton.text = text
        //Log.d("Button", "WebView Activated")
        }
    }

    /**
     * Sets up this Week Button
     */
    private fun setThisWeekButton(){
        val tButton: Button = findViewById(R.id.TW)
        val tButtonText: String = "This weeks comics"
        tButton.text = tButtonText
        tButton.setOnClickListener {
            if(!useWebView){
                val intent = Intent(this, PublishersActivity::class.java)
                intent.putExtra("date", getWeekDate(true))
                this.startActivity(intent)
            }else{
                val intent = Intent(this, WebViewActivity::class.java)
                intent.putExtra("url", linkMaker(getWeekDate(true)))
                this.startActivity(intent)
            }

        }
    }

    /**
     * Sets up next week button
     */
    private fun setNextWeekButton(){
        val nButton: Button = findViewById(R.id.NW)
        val nButtonText: String = "Next weeks comics"
        nButton.text = nButtonText
        nButton.setOnClickListener {
            if(!useWebView){
                val intent = Intent(this, PublishersActivity::class.java)
                intent.putExtra("date", getWeekDate(false))
                this.startActivity(intent)
            }else{
                val intent = Intent(this, WebViewActivity::class.java)
                intent.putExtra("url", linkMaker(getWeekDate(false)))
                this.startActivity(intent)
            }
        }
    }

    /**
     * Sets up reload button
     */
    private fun setReloadButton(){
        val reloadButton: Button = findViewById(R.id.RL)
        reloadButton.setOnClickListener {
            Log.d("Started blocking and set waiting layout as content view","Started")
            runBlocking {
                val dh: DatabaseHandler = DatabaseHandler(this@MainActivity)
                val thisWeek = getWeekDate(true)
                val nextWeek = getWeekDate(false)
                for (comic in dh.viewComics()) {
                    dh.deleteComic(comic)
                    File(this@MainActivity.filesDir, comic.title + ".json").delete()
                    Log.d("Deleted", comic.title)
                }
                proccessComics(getWeekDate(true))
                proccessComics(getWeekDate(false))
                Log.d("Reloading comics", "Finished")
            }
            Log.d("Started blocking and set waiting layout as content view","Finished, setting date picker layout as content view")
            setContentView(R.layout.date_picker_layout)
            Log.d("Setting date picker layout as content view", "Finished")

        }
    }

    /**
     * Updates the database
     */
    private fun dbUpdater(){
        val weekCheck = weekCheck()
        updateWeek(weekCheck[0],true,R.string.this_week)
        updateWeek(weekCheck[1],false,R.string.next_week)
    }

    /**
     * Updates a given week in database
     */
    private fun updateWeek(weekExists: Boolean, thisWeek: Boolean, week: Int){
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        val state = sharedPref.getString(getString(week),resources.getString(week))
        if(weekExists){
            if(state == "False"){
                proccessComics(getWeekDate(thisWeek))
                updateSP("True", week)
            }
        }
        else{
            updateSP("False", week)
            proccessComics(getWeekDate(thisWeek))
            updateSP("True", week)
        }
    }


    /**
     * Updates Shared Preferences
     */
    private fun updateSP(text: String, week: Int){
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString(getString(week), text)
            commit()
        }
    }

    /**
     * Checks if a week contains the correct dates/deletes old ones
     */
    private fun weekCheck(): ArrayList<Boolean>{
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
        return arrayListOf(foundThisWeek,foundNextWeek)
    }

    /**
     * Generates the date of a given week (this week or next week)
     */
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

    /**
     * Generates a link based on the given date String
     */
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

    /**
     * Gets and Stores comics for a given date
     */
    private fun proccessComics(date: String){
        val cg = ComicGenerator(this,date,linkMaker(date))
        val comics: ArrayList<Comic> = cg.comcis
        Log.d("ComicNum", comics.size.toString())
        generateDatabase(comics)
        generateJsonComics(comics)
    }

    /**
     * A function for testing the code that gets and stores comics from an old date
     */
    //for testing purposes
    private fun testOldDate(){
        val cg = ComicGenerator(this,"2019-08-23",linkMaker("2019-08-23"))
        val comics: ArrayList<Comic> = cg.comcis
        Log.d("ComicNum", comics.size.toString())
        generateJsonComics(comics)
        generateDatabase(comics)
    }

    /**
     * Stores comics in .json files
     */
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

    /**
     * Creates/updates the database with new comics
     */
    private fun generateDatabase(comics: ArrayList<Comic>){
        Log.d("The number should be", comics.size.toString())
        //val comics: ArrayList<ComicDatabase> = arrayListOf()
        val dh: DatabaseHandler = DatabaseHandler(this)

        var idCounter: Int = dh.viewComics().size + 1
        
        for(comic in comics){
            dh.addComic(Comic_Database(idCounter,comic.title,comic.publisher,comic.date))
            idCounter++
        }
        val cms = dh.viewComics()
        Log.d("comic number in database",cms.size.toString())

    }

    /**
     * Gets the covers from their links, using Ion
     */
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





    /**
     * Loads an image from a url as a String (Deferred String), without Ion
     */
    private suspend fun loadImage(url: String): Deferred<String> = GlobalScope.async {
            val image: String = async {
                  val url = URL(url)
                val img = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                val result = getStringFromBitmap(img)
                result

            }.await()

            return@async image
    }

    /**
     * Converts a Bitmap to a String
     */
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

    /**
     * Resets the useWebView variable to false
     */
    override fun onResume() {
        super.onResume()
        useWebView = false
    }
}
