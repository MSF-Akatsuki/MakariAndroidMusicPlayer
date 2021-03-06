package com.msfakatsuki.musicplayer.ui.play

import android.content.Context
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.msfakatsuki.musicplayer.MusicPlayUIActivity
import com.msfakatsuki.musicplayer.R
import com.msfakatsuki.musicplayer.databinding.FragmentPlayerViewPagerBinding
/**
 * A simple [Fragment] subclass.
 * Use the [PlayerViewPagerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlayerViewPagerFragment : Fragment() {

    lateinit var binding : FragmentPlayerViewPagerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        binding = FragmentPlayerViewPagerBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_pager,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_all-> {
                val mediaController = MediaControllerCompat.getMediaController(requireActivity())
                mediaController.transportControls.sendCustomAction("CA_REMOVE_ALL_FROM_QUEUE",null)
                true
            }
            else->super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.playerPager.adapter=Adapter(childFragmentManager)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activity = activity as? MusicPlayUIActivity
        activity?.let {
            it.OnBackPressedCallback = {
                if (binding.playerPager.currentItem==0)
                    false
                else {
                    binding.playerPager.setCurrentItem(0)
                    true
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        val activity = activity as? MusicPlayUIActivity
        activity?.let {
            it.OnBackPressedCallback = null
        }
    }

    inner class Adapter(fm: FragmentManager): FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount() = 2

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> {
                    MusicPlayUIFragment.newInstance()
                }
                else -> {
                    SongItemFragment.newInstance()
                }
            }
        }

    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PlayerViewPagerFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlayerViewPagerFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

}