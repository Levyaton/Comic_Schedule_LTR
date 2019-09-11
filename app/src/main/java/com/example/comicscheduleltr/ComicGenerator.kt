package com.example.comicscheduleltr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL


import java.util.*
import kotlin.collections.ArrayList


/**
 * TO DO
 */
class ComicGenerator(val context: Context, val date: String, val url: String) {

    val comcis = getComics()



    /**
     * TO DO
     */
    private fun getComics(): ArrayList<Comic> {

        var html = htmlParse()

        val comics: ArrayList<Comic> = parseHtml(html)
        Log.d("got here", "before for-cycle")

        while (true) {
            try {
                runBlocking {
                    addDescriptions(comics)
                }
                break
            } catch (e: Exception) {
               Log.d("Retrying", e.toString())
            }
        }


        Log.d("for-cycle", "ended")
        return comics

    }


    /**
     * TO DO
     */
    private fun htmlParse(): List<String>{
        var html: List<String> = listOf()

        while (true) {
            try {
                runBlocking {
                    html = getHtml(url).await().lines()
                }
                break
            } catch (e: Exception) {
                Log.d("Retrying", url)
            }
        }

        return html
    }

    /**
     * TO DO
     */
    private suspend fun addDescriptions(comics: ArrayList<Comic>) = withContext(Dispatchers.IO) {
        // withContext waits for all children coroutines
        for (comic in comics) {
            async {
                while (true) {
                    try {
                        Log.d("loading description of", comic.title)

                        //comic.description = "Testing"

                        comic.description = getDescriptions(comic.descriptionLink)

                        Log.d("Finished description of", comic.title)
                        break
                    } catch (e: Exception) {
                        Log.d("Retrying", "Descriptions")
                    }

                }
            }

        }
    }

    /**
     * TO DO
     */
    private suspend fun getHtml(url: String): Deferred<String> = GlobalScope.async {

        try {
            val text: String = async {
                val urlConnection = URL(url).openConnection() as HttpURLConnection
                val html = urlConnection.inputStream.bufferedReader().readText()
                urlConnection.disconnect()
                html
            }.await()

            return@async text
        } catch (e: Exception) {
            var result: String? = null
            runBlocking {
                result = getHtml(url).await()
            }
            return@async result as String

        }
    }

    /**
     * TO DO
     */
    private fun parseHtml(html: List<String>): ArrayList<Comic> {
        var comics: ArrayList<Comic> = arrayListOf()
        var pubFound = false
        var currentPub = ""
        for (line in html) {

            if (line.contains("entry-title")) {

                pubFound = true

            } else if (pubFound) {

                currentPub = line
                Log.d("Publisher", "Found")
                pubFound = false

            } else if (titleIsValid(line)) {
                comics = getComicsArray(line,comics,currentPub)
            }
        }

        Log.d("Comics prepared", "Next step")
        return comics.distinct() as ArrayList<Comic>
    }

    /**
     * TO DO
     */
    private fun getComicsArray(line: String, comics: ArrayList<Comic>, currentPub: String): ArrayList<Comic>{

        val comics = comics
        var temp_title =
            line.substring(line.indexOf("alt=") + 5, line.indexOf("style=") - 2)
        var cover: String =
            line.substring(line.indexOf("src=") + 5, line.indexOf("alt=") - 2)
        val coverLink: ArrayList<String> = arrayListOf()
        coverLink.add(cover)
        if (temp_title.contains("Cover") && comics.size > 0 && temp_title.contains(comics.last().title)) {

            comics.elementAt(comics.lastIndex).addCoverLink(cover)

        } else if(!temp_title.contains("Cover")){

            val descriptionLink = "https://freshcomics.us/" + line.substring(
                line.indexOf("<a href=") + 9,
                line.indexOf("><img") - 1
            )
            val title = temp_title.replace('/', '&')

            comics.add(Comic(formatString(title), date, currentPub, descriptionLink, coverLink))

        }

        return comics
    }

    /**
     * TO DO
     */
    private fun titleIsValid(line: String): Boolean{

        val firstPoint = line.contains("<a href=")
        val secondPoint= line.contains("<img src=")
        val thirdPoint= line.contains("alt=")
        val fourthPoint = !line.contains("(True Believers)")
        val fifthPoint = !line.contains(" Printing)")
        val sixthPoint = !line.contains("(Complete Collection)")
        val seventhPoint =  !line.contains(" Vol. ")

        return (firstPoint && secondPoint && thirdPoint && fourthPoint && fifthPoint && sixthPoint && seventhPoint)

    }

    /**
     * TO DO
     */
    private suspend fun getDescriptions(link: String): String {
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
                    it.substring(
                        it.indexOf(sliceStart) + sliceStart.length,
                        it.indexOf(sliceEnd) - 1
                    )
                description = formatString(description)
                found = false
            }
        }

        return description
    }

    /**
     * TO DO
     */
    private fun formatString(text: String): String {
        var newText = text.replace("<br><br>", "\n")
        newText = newText.replace("&#39;", "'")
        newText = newText.replace("&amp;", "&")
        newText = newText.replace("<br />", "\n")
        newText = newText.replace("&quot;", "''")
        return newText
    }
}




