package com.example.comicscheduleltr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import com.koushikdutta.ion.Ion
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import com.google.gson.Gson
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.util.Base64
import android.util.Log
import com.google.gson.stream.JsonReader
import java.io.BufferedReader
import java.io.FileReader
import java.io.Reader
import java.lang.Exception


class ViewTitleActivity: AppCompatActivity() {
    override fun onStart() {
        super.onStart()
        getComic(intent.getStringExtra("title") as String)
    }

    private fun getComic(title: String){
        val gson = Gson()
        val modTitle = "/$title.json"
        val bufferedReader: BufferedReader = File(filesDir.path + modTitle).bufferedReader()
        val inputString = bufferedReader.use { it.readText() }
        var comic = gson.fromJson(inputString, JComic::class.java)

        setLayout(comic)
    }

   private fun setLayout(comic: JComic){
       val sv: ScrollView = ScrollView(this)
        val relativeLayout: RelativeLayout = RelativeLayout(this)
        var mainImg: ImageView = ImageView(this)
        val covers: ArrayList<Bitmap> = arrayListOf()
       var bitmap: Bitmap = BitmapFactory.decodeByteArray(Base64.decode(comic.covers!![0], Base64.DEFAULT), 0, Base64.decode(comic.covers!![0], Base64.DEFAULT).size)
       //covers.add(bitmap!!)
       mainImg.setImageBitmap(bitmap)
        val linLay: LinearLayout = LinearLayout(this)
        linLay.orientation = LinearLayout.VERTICAL
        linLay.addView(mainImg)
        mainImg.layoutParams.height += 1000;
        mainImg.layoutParams.width += 1000;
        val horizontalScrolling: HorizontalScrollView = HorizontalScrollView(this)
        val imageGallery: LinearLayout = LinearLayout(this)
        imageGallery.orientation = TableRow.HORIZONTAL
        imageGallery.gravity = Gravity.CENTER

        for (variant in comic.covers!!) {
            var image: ImageView = ImageView(this)
            var bp: Bitmap = BitmapFactory.decodeByteArray(Base64.decode(variant, Base64.DEFAULT), 0, Base64.decode(variant, Base64.DEFAULT).size)
           
            image.setImageBitmap(bp)
            covers.add(bp!!)
            image.setPadding(20, 20, 20, 20)
            imageGallery.addView(image)
            image.layoutParams.height = 250
            image.layoutParams.width = 250
            image.setOnClickListener {
                mainImg.setImageBitmap(bp)
            }
        }
        horizontalScrolling.addView(imageGallery)
        linLay.addView(horizontalScrolling)
        val description: TextView = TextView(this)
        description.text = comic.description
        linLay.addView(description)
        relativeLayout.addView(linLay)
        sv.addView(relativeLayout)
        setContentView(sv)
    }


}

    
