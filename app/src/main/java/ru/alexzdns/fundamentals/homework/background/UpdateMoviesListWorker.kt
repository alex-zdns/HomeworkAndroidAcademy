package ru.alexzdns.fundamentals.homework.background

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.*
import retrofit2.create
import ru.alexzdns.fundamentals.homework.data.repository.MoviesRepository
import ru.alexzdns.fundamentals.homework.domain.models.Movie
import ru.alexzdns.fundamentals.homework.network.MoviesLoader
import ru.alexzdns.fundamentals.homework.network.NetworkModule
import ru.alexzdns.fundamentals.homework.ui.moviesList.item.MovieLists

class UpdateMoviesListWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    private val repository: MoviesRepository = MoviesRepository(context)
    private val moviesLoader = MoviesLoader(NetworkModule.retrofit.create())
    private val movieNotifications = MovieNotifications(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.i(this::class.simpleName, "Start update")

            val popularMovieFromDb = repository.getPopularMovies()
            val popularMovies = moviesLoader.loadMoviesFromServer(emptySet(), MovieLists.POPULAR.path)
            popularMovies.forEach { cachingImage(it.poster) }
            popularMovies.forEach { cachingImage(it.backdrop) }
            findNewMovieAndShowNotification(popularMovieFromDb, popularMovies)
            repository.savePopularMovies(popularMovies)

            val topRatedMoviesFromDb = repository.getTopRatedMovies()
            val topRatedMovies = moviesLoader.loadMoviesFromServer(emptySet(), MovieLists.TOP.path)
            topRatedMovies.forEach { cachingImage(it.poster) }
            topRatedMovies.forEach { cachingImage(it.backdrop) }
            findNewMovieAndShowNotification(topRatedMoviesFromDb, topRatedMovies)
            repository.saveTopRatedMovies(topRatedMovies)

            val nowPlayingMovieFromDb = repository.getNowPlayingMovies()
            val nowPlayingMovies = moviesLoader.loadMoviesFromServer(emptySet(), MovieLists.NOW_PLAYING.path)
            nowPlayingMovies.forEach { cachingImage(it.poster) }
            nowPlayingMovies.forEach { cachingImage(it.backdrop) }
            findNewMovieAndShowNotification(nowPlayingMovieFromDb, nowPlayingMovies)
            repository.savePopularMovies(nowPlayingMovies)

            Log.i(this::class.simpleName, "Successful update")
            return@withContext Result.success()
        } catch (e: Exception) {
            Log.e(this::class.simpleName, e.message?:"")
            return@withContext Result.failure()
        }
    }

    private fun cachingImage(url: String) = Glide.with(context).load(url).diskCacheStrategy(
        DiskCacheStrategy.ALL).submit()

    private fun findNewMovieAndShowNotification(oldList: List<Movie>, newList: List<Movie>) {
        val newPopularMovie = newList.toSet().subtract(oldList).maxByOrNull { it.ratings }
        if (newPopularMovie != null) {
            movieNotifications.showNotification(newPopularMovie)
        }
    }

    companion object {
        const val TAG = "Movies update"
    }
}