package br.com.nglauber.starwarsrx

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.core.content.ContextCompat
import br.com.nglauber.starwarsrx.model.api.StarWarsApi
import rx.Scheduler
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class MainActivity : AppCompatActivity() {

    var listView: ListView? = null
    var movies = mutableListOf<String>()
    var movieAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listView = ListView(this)
        setContentView(listView)
        movieAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, movies)
        listView?.adapter = movieAdapter

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(),"SEM PERMISSAO",Toast.LENGTH_SHORT).show();
            // Permission is not granted
        }

        val api = StarWarsApi()
        api.loadMoviesFull()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ movie ->
                movies.add("${movie.title} -- ${movie.episodeId} \n ${movie.characters.toString()}")
            }, { e ->
                e.printStackTrace()
            }, {
                movieAdapter?.notifyDataSetChanged()
            })
    }
}
