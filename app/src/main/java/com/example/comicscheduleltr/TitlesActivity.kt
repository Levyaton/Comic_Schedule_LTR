package com.example.comicscheduleltr

import android.app.backup.BackupHelper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import java.io.*
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T




class TitlesActivity: AppCompatActivity() {

    override fun onStart() {
        super.onStart()
        getTitles(intent.getStringExtra("publisher") as String)
    }

    private fun getTitles(publisher: String){
        val titles: ArrayList<String> = arrayListOf()
        val dh: DatabaseHandler = DatabaseHandler(this)
        for(comic in dh.viewComics()){
            Log.d("ViewTitleActivity is", comic.title)
            Log.d("Publisher is", comic.publisher)
            if(comic.publisher == publisher && comic.date == intent.getStringExtra("date") as String){
                titles.add(comic.title)
            }
        }
        //Log.d("comics in pub", publishers.size.toString())
        titles.sort()
        setLayout(titles)
    }

    private fun setLayout(titles: ArrayList<String>){

        val scroll: ScrollView = ScrollView(this)
        val linLay: LinearLayout = LinearLayout(this)
        linLay.orientation = LinearLayout.VERTICAL
        linLay.gravity = Gravity.CENTER
        linLay.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        Log.d("Number of titles", titles.size.toString())
        for(title in titles){
            val comic: JComic = getComic(title)
            //Log.d("byteArr is", comic.covers?.get(0))
            var image: Bitmap = BitmapFactory.decodeByteArray(Base64.decode(comic.covers!![0], Base64.DEFAULT), 0, Base64.decode(comic.covers!![0], Base64.DEFAULT).size)
            image = Bitmap.createScaledBitmap(image!!,(image.width *0.8).toInt(), (image.height *0.8).toInt(), true)
            val button: Button = Button(this)
            button.text = title
            button.setOnClickListener{
                val intent = Intent(this, ViewTitleActivity::class.java)
                intent.putExtra("title",title)
                this.startActivity(intent)
            }
            linLay.addView(button)
            val imageView: ImageView = ImageView(this)

            imageView.setImageBitmap(image)
            imageView.setOnClickListener{
                val intent = Intent(this, ViewTitleActivity::class.java)
                intent.putExtra("title",title)
                this.startActivity(intent)
            }
            linLay.addView(imageView)
        }
        scroll.addView(linLay)
        setContentView(scroll)
    }

    private fun getComic(title: String): JComic{
        val gson = Gson()
        val modTitle = "/$title.json"
        val bufferedReader: BufferedReader = File(filesDir.path + modTitle).bufferedReader()
        val inputString = bufferedReader.use { it.readText() }
        var comic = gson.fromJson(inputString, JComic::class.java)
        return comic 
    }

}