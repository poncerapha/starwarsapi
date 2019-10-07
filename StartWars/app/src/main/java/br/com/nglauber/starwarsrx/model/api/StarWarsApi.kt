package br.com.nglauber.starwarsrx.model.api


import android.net.Uri
import br.com.nglauber.starwarsrx.model.Character
import br.com.nglauber.starwarsrx.model.Movie
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observable

class StarWarsApi {


    val service: StarWarsApiDef
    val peopleCache = mutableMapOf<String, Person>()

    init {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(logging)

        val gson = GsonBuilder().setLenient().create()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://swapi.co/api/")
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient.build())
            .build()
        service = retrofit.create<StarWarsApiDef>(StarWarsApiDef::class.java)
    }

    fun loadMovies(): Observable<Movie> {
        return service.listMovies()
            .flatMap { filmResult -> Observable.from(filmResult.results) }
            .flatMap { film ->
                Observable.just(
                    Movie(
                        film.title,
                        film.episodeId,
                        ArrayList<Character>()
                    )
                )
            }
    }

    fun loadMoviesFull(): Observable<Movie> {
        return service.listMovies()
            .flatMap { filmeResult -> Observable.from(filmeResult.results) }
            .flatMap { film ->
                Observable.zip(
                    Observable.just(Movie(film.title, film.episodeId, ArrayList<Character>())),
                    Observable.from(film.personUrls)
                        .flatMap { personUrl ->
                            Observable.concat(
                                getCache(personUrl),
                                 service.loadPerson((Uri.parse(personUrl).lastPathSegment))
                            ).first()
                        }
                        .flatMap { person ->
                            Observable.just(
                                Character(
                                    person.name,
                                    person.gender
                                )
                            )
                        }.toList(), { movie, characters ->
                        movie.characters.addAll(characters)
                        movie
                    }
                )
            }
    }

    private fun getCache(personUrl: String): Observable<Person> {
        return Observable.from(peopleCache.keys)
            .filter { key ->
                key == personUrl
            }.flatMap { key -> Observable.just(peopleCache[personUrl]) }
    }


}