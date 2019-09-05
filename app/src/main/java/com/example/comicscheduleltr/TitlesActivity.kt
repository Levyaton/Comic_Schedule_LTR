package com.example.comicscheduleltr

import android.content.Intent
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity

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
            if(comic.publisher == publisher){
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
            val button: Button = Button(this)
            button.text = title
            button.setOnClickListener{
                val intent = Intent(this, ViewTitleActivity::class.java)
                intent.putExtra("title",title)
                this.startActivity(intent)
            }
            linLay.addView(button)
        }
        scroll.addView(linLay)
        setContentView(scroll)
    }
}