package com.example.comicscheduleltr

import android.content.Context
import android.os.FileObserver.CREATE
import com.orm.SugarRecord
import com.orm.dsl.Table
import com.orm.dsl.Unique
import java.io.File

@Table
class ComicDatabase: SugarRecord {

    var date: String? = null
    var publisher: String? = null
    var title: String? = null


    constructor(){}

    constructor(date: String, publisher: String, title: String){
        this.date = date
        this.publisher = publisher
        this.title = title
    }
}