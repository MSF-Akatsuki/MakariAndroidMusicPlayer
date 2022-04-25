package com.msfakatsuki.musicplayer.ui.play

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.msfakatsuki.musicplayer.R
import com.msfakatsuki.musicplayer.databinding.MusicPlayUiFragmentBinding

class MusicPlayUIFragment : Fragment() {

    companion object {
        fun newInstance() = MusicPlayUIFragment()
    }

    private lateinit var viewModel: MusicPlayUIViewModel
    private lateinit var _binding: MusicPlayUiFragmentBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.music_play_ui_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MusicPlayUIViewModel::class.java)
        // TODO: Use the ViewModel
    }

}