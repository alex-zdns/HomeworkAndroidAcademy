package ru.alexzdns.fundamentals.homework.ui.moviesList

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.alexzdns.fundamentals.homework.BuildConfig
import ru.alexzdns.fundamentals.homework.data.repository.MoviesRepository
import ru.alexzdns.fundamentals.homework.domain.models.Movie
import ru.alexzdns.fundamentals.homework.network.MovieApi
import ru.alexzdns.fundamentals.homework.network.dto.GenreDto
import ru.alexzdns.fundamentals.homework.network.dto.MovieDto

class MoviesListViewModel(
    private val repository: MoviesRepository,
    private val movieApi: MovieApi
) : ViewModel() {
    private val _mutableState = MutableLiveData<State>(State.Default())
    val state: LiveData<State> get() = _mutableState

    private val _mutableMoviesList = MutableLiveData<List<Movie>>(emptyList())
    val moviesList: LiveData<List<Movie>> get() = _mutableMoviesList

    private var favoriteMovie: Set<Long> = emptySet()

    fun getMovies() {
        viewModelScope.launch {
            try {
                _mutableState.value = State.Loading()

                if (moviesList.value?.isEmpty() == true) {
                    getMoviesFromDb()
                }

                getMoviesFromServer()
                _mutableState.value = State.Success()
            } catch (e: Exception) {
                Log.e("loadMoviesFromDB", e.message ?: "")
                e.printStackTrace()
                _mutableState.value = State.Error()
            }
        }
    }

    private suspend fun getMoviesFromDb() {
        favoriteMovie = repository.getAllFavoriteMovie()
        val moviesFromDb = repository.getPopularMovies()
        _mutableMoviesList.value = moviesFromDb
    }

    private suspend fun getMoviesFromServer() {
        val movies = loadMoviesFromServer()
        _mutableMoviesList.value = movies
        repository.savePopularMovies(movies)
        _mutableState.value = State.Success()
    }

    private suspend fun loadMoviesFromServer(): List<Movie> {
        val genresMap = movieApi.getGenres().genres
            .associateBy { it.id }

        val moviesDto = movieApi.getPopularMovie().movies
            .filter { it.backdropPath != null && it.posterPath != null }

        return mapMovie(moviesDto, genresMap)
    }

    private fun mapMovie(moviesDto: List<MovieDto>, genres: Map<Int, GenreDto>): List<Movie> =
        moviesDto.map { dto ->
            Movie(
                id = dto.id,
                title = dto.title,
                overview = dto.overview,
                poster = BuildConfig.IMAGE_BASE_URL + BuildConfig.POSTER_SIZES_PATCH + dto.posterPath,
                backdrop = BuildConfig.IMAGE_BASE_URL + BuildConfig.BACKDROP_SIZES_PATCH + dto.backdropPath,
                ratings = dto.voteAverage / 2.0f,
                numberOfRatings = dto.voteCount,
                minimumAge = if (dto.adult) 16 else 13,
                genres = dto.genresIds
                    .map { genres[it] ?: throw IllegalArgumentException("Genre not found") }
                    .joinToString(separator = ", ") { it.name },
                isFavorite = favoriteMovie.contains(dto.id)
            )
        }

    fun onLikeHandle(movie: Movie) {
        movie.isFavorite = !movie.isFavorite
        viewModelScope.launch {
            if (movie.isFavorite) {
                repository.addFavoriteMovieById(movie.id)
            } else {
                repository.removeFromFavoriteMovie(movie.id)
            }

            favoriteMovie = repository.getAllFavoriteMovie()
        }
    }

    sealed class State {
        class Default : State()
        class Loading : State()
        class Error : State()
        class Success : State()
    }
}