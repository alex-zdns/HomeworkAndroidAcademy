package ru.alexzdns.fundamentals.homework.ui.moviesList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.alexzdns.fundamentals.homework.App

class MoviesListViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = when (modelClass) {
        MoviesListViewModel::class.java -> MoviesListViewModel(context = App.context())
        else -> throw IllegalArgumentException("$modelClass is not registered ViewModel")
    } as T
}