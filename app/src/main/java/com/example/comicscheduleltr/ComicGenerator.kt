package com.example.comicscheduleltr

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL


import java.util.*
import kotlin.collections.ArrayList

class ComicGenerator(val context: Context, val date: String, val url: String) {

    val comcis = getComics()

    private fun getComics(): ArrayList<Comic>{

        var html: List<String> = listOf()

        runBlocking {
            html = getHtml(url).await().lines()
        }

        val comics: ArrayList<Comic> = parseHtml(html)
        Log.d("got here", "before for-cycle")
        runBlocking {
            GlobalScope.async{
                for(comic in comics){

                        Log.d("got here","before runBlocking")

                        //delay(100)
                        //comic.description = "temp"
                        comic.description = getDescriptions(comic.descriptionLink)

                        Log.d("ViewTitleActivity is", comic.title)

                }
            }.await()
        }

        Log.d("for-cycle", "ended")
        return comics

    }

    private suspend fun getHtml(url: String): Deferred<String> = GlobalScope.async {

        try{
            val text: String = async {
                val urlConnection = URL(url).openConnection() as HttpURLConnection
                val html = urlConnection.inputStream.bufferedReader().readText()
                urlConnection.disconnect()
                html
            }.await()

            return@async text
        }
        catch(e: Exception){
            var result: String? = null
            runBlocking {
                result = getHtml(url).await()
            }
            return@async result as String

        }
    }

    private fun parseHtml(html: List<String>): ArrayList<Comic>{
        val comics: ArrayList<Comic> = arrayListOf()
        var pubFound = false
        var currentPub = ""
        for (line in html) {

            if (line.contains("entry-title")) {
                pubFound = true
            } else if (pubFound) {
                currentPub = line
                Log.d("Publisher", "Found")
                pubFound = false
            } else if (line.contains("<a href=") && line.contains("<img src=") && line.contains(
                    "alt="
                ) && !line.contains("(True Believers)") && !line.contains(" Printing)") && !line.contains(
                    "(Complete Collection)"
                ) && !line.contains(" Vol. ")
            ) {
                var temp_title =
                    line.substring(line.indexOf("alt=") + 5, line.indexOf("style=") - 2)
                var cover: String =
                    line.substring(line.indexOf("src=") + 5, line.indexOf("alt=") - 2)
                val coverLink: ArrayList<String> = arrayListOf()
                coverLink.add(cover)
                if (temp_title.contains("Cover") && comics.size > 0) {
                    comics.elementAt(comics.lastIndex).addCoverLink(cover)
                } else {
                    val descriptionLink = "https://freshcomics.us/" + line.substring(
                        line.indexOf("<a href=") + 9,
                        line.indexOf("><img") - 1
                    )
                    val title = temp_title.replace('/', '&')

                    val comic: Comic = Comic(formatString(title), date, currentPub, descriptionLink,coverLink)
                    comics.add(comic)
                    Log.d("Created Comic with publisher", currentPub)
                }
            }
        }
        Log.d("Comics prepared", "Next step")
        return comics
    }

    private suspend fun getDescriptions(link: String): String{
        var html: List<String> = listOf()
        runBlocking {
            html = getHtml(link).await().lines()
        }

        var description: String = ""
        val sliceEnd: String = "</p>"
        val sliceStart: String = "<p>"
        var found: Boolean = false

        html.forEach {
            if (it.contains("entry-content") && it.contains("<div class=")) {
                found = true
            } else if (found && it.contains(sliceStart)) {
                description =
                    it.substring(it.indexOf(sliceStart) + sliceStart.length, it.indexOf(sliceEnd) - 1)
                description = formatString(description)
                found = false
            }
        }

        return description
    }

    private fun formatString(text: String): String{
        var newText = text.replace("<br><br>", "\n")
        newText = newText.replace("&#39;", "'")
        newText = newText.replace("&amp;", "&")
        newText = newText.replace("<br />", "\n")
        newText = newText.replace("&quot;", "''")
        return newText
    }

}