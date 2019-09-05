package com.example.comicscheduleltr

import android.content.Intent
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity

class PublishersActivity: AppCompatActivity()  {

    override fun onStart() {
        super.onStart()
        getPublishers(intent.getStringExtra("date") as String)
    }

    private fun setLayout(publishers: ArrayList<String>, date:String){

        val scroll: ScrollView = ScrollView(this)
        val linLay: LinearLayout = LinearLayout(this)
        linLay.orientation = LinearLayout.VERTICAL
        linLay.gravity = Gravity.CENTER
        linLay.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        Log.d("Number of publishers", publishers.size.toString())
        for(publisher in publishers){
            val button: Button = Button(this)
            button.text = publisher
            button.setOnClickListener{
                val intent = Intent(this, TitlesActivity::class.java)
                intent.putExtra("publisher",publisher)
                intent.putExtra("date", date)
                this.startActivity(intent)
            }
            linLay.addView(button)
        }
        scroll.addView(linLay)
        setContentView(scroll)
    }

    private fun getPublishers(date: String){
        val publishers: ArrayList<String> = arrayListOf()
        val dh: DatabaseHandler = DatabaseHandler(this)
        for(comic in dh.viewComics()){
            Log.d("ViewTitleActivity is", comic.title)
            Log.d("Publisher is", comic.publisher)
            if(comic.date == date){
                if(!publishers.contains(comic.publisher))
                publishers.add(comic.publisher)
            }
        }
        //Log.d("comics in pub", publishers.size.toString())
        publishers.sort()
        setLayout(publishers, date)
    }

}