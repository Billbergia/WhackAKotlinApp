package com.example.myfirstkotlinapp.viewmodel

import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {

    //private val _

    override fun onCleared() {
        /*
         This method will be called when this ViewModel is no longer used and will be destroyed.
         It’s useful when the ViewModel observes some data, and you need to clear this subscription to prevent a leak of this ViewModel.
         */
        super.onCleared()
    }
}