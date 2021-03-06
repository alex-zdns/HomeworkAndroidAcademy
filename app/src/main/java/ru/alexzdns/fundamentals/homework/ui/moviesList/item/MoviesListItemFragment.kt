package ru.alexzdns.fundamentals.homework.ui.moviesList.item

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ru.alexzdns.fundamentals.homework.R
import ru.alexzdns.fundamentals.homework.domain.models.Movie

class MoviesListItemFragment : androidx.fragment.app.Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private val viewModel: MoviesListViewModel by viewModels { MoviesListViewModelFactory() }

    private var recycler: RecyclerView? = null
    private var loader: SwipeRefreshLayout? = null
    private var listenerMovieList: MovieListClickListener? = null

    private val adapter = MoviesAdapter(object : MoviesAdapter.OnRecyclerMovieItemClicked {
        override fun onBannerClick(movie: Movie) {
            listenerMovieList?.openMovieDetailsFragment(movie)
        }

        override fun onLikeClick(movie: Movie) {
            viewModel.onLikeHandle(movie)
        }
    })

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listenerMovieList = context as? MovieListClickListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_movie_list_item, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.mlf_movie_list)
        recycler?.adapter = adapter

        loader = view.findViewById(R.id.fml_swipe_container)
        loader?.setOnRefreshListener(this)

        arguments?.getString(MOVIE_LIST_PATH)?.let {
            viewModel.movieListPath = it
        }

        viewModel.state.observe(this.viewLifecycleOwner, this::setState)
        viewModel.moviesList.observe(this.viewLifecycleOwner, this::updateMoviesList)

        if (viewModel.state.value is MoviesListViewModel.State.Default) viewModel.getMovies()
    }

    private fun setState(state: MoviesListViewModel.State) =
        when (state) {
            is MoviesListViewModel.State.Default,
            is MoviesListViewModel.State.Success -> {
                setLoading(false)
            }
            is MoviesListViewModel.State.Loading -> {
                setLoading(true)
            }
            is MoviesListViewModel.State.Error -> {
                setLoading(false)
                Toast.makeText(context, getString(R.string.loading_movies_error_message), Toast.LENGTH_LONG).show()
            }
        }

    private fun setLoading(loading: Boolean) {
        loader?.isRefreshing = loading
    }

    private fun updateMoviesList(movies: List<Movie>) {
        adapter.submitList(movies)
    }

    override fun onRefresh() {
        viewModel.getMovies()
    }

    override fun onDetach() {
        super.onDetach()
        listenerMovieList = null
    }

    companion object {
        private const val MOVIE_LIST_PATH = "movieListPath"

        fun newInstance(moviesPath: String): MoviesListItemFragment =
            MoviesListItemFragment().apply {
                val args = Bundle()
                args.putString(MOVIE_LIST_PATH, moviesPath)
                arguments = args
            }
    }

    interface MovieListClickListener {
        fun openMovieDetailsFragment(movie: Movie)
        fun openMovieDetailsFragment(movieId: Long)
    }
}