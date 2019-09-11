package com.example.comicscheduleltr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.JsonObject

/**
 * A gson compatible version of a Comic object
 */
class JComic{
    
    var title: String? = null
    var publisher: String? = null
    var date: String? = null
    var coverLinks: List<String>? = null
    var description: String? = null
    var covers: List<String>? = null
    
    

    constructor() : super() {}

    constructor(title: String, publisher: String, date: String, coverLinks: List<String>, description: String, covers: List<String>?) : super() {
        this.title = title
        this.publisher = publisher
        this.date = date
        this.coverLinks = coverLinks
        this.description = description
        this.covers = covers
    }


}

/**
 * An object that stores information about a comic
 */
class Comic {

    lateinit var title: String
    lateinit var date: String
    lateinit var publisher: String
    lateinit var descriptionLink: String
    lateinit var coverLinks: ArrayList<String>

    lateinit var description: String
    lateinit var covers: ArrayList<ByteArray>


    constructor(comic: JsonObject){
        title = comic.get("title").asString
        date = comic.get("date").asString
        publisher = comic.get("publisher").asString
        description = comic.get("description").asString
        covers = decodeByteArray(comic.get("covers").asString)
    }
    
    constructor(getTitle: String, getDate: String, getPublisher: String, getDescriptionLink: String, getCoversLinks: ArrayList<String>) {
        title = getTitle
        date = getDate
        publisher = getPublisher
        descriptionLink = getDescriptionLink
        coverLinks = getCoversLinks
    }

    /**
     * Adds another cover link to coverLinks
     */
    fun addCoverLink(cover: String){
        coverLinks.add(cover)
    }

    /**
     * Returns covers as an ArrayList of Bitmaps
     */
    fun getBitmapCovers(): ArrayList<Bitmap>{
        val bitmapCovers: ArrayList<Bitmap> = arrayListOf()
        for(cover in this!!.covers!!){
            bitmapCovers.add(BitmapFactory.decodeByteArray(cover, 0, cover.size))
        }
        return bitmapCovers
    }

    /**
     * Decodes a coded ByteArray String (Outdated), provided the key is replacing [*] with a new line
     */
    private fun decodeByteArray(code: String):  ArrayList<ByteArray>{
        val covers: ArrayList<ByteArray> = arrayListOf()

        val arrs: List<String> = code.replace("[*]", "\n").lines()

        for(line in arrs){
            covers.add(line.toByteArray())
        }
        return covers
    }

    /**
     * Converts the Comic object to a JComic
     */
    fun toJComic(covers: List<String>?): JComic{
        return JComic(title, publisher, date,coverLinks, description,covers)
    }
}