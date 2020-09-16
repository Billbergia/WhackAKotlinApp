package com.example.myfirstkotlinapp.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import com.example.myfirstkotlinapp.GameView
import com.example.myfirstkotlinapp.R
import com.example.myfirstkotlinapp.viewmodel.GameViewModel

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class GameFragment : Fragment() {

    lateinit var viewModel: GameViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(GameViewModel::class.java)

        val gameView = view.findViewById<GameView>(R.id.game_view)
        view.findViewById<Button>(R.id.start_game).setOnClickListener { gameView.startGame() }
    }
}