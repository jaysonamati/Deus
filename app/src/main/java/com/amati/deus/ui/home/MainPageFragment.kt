package com.amati.deus.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.amati.deus.R
import com.amati.deus.databinding.FragmentMainPageBinding
import timber.log.Timber
import java.util.concurrent.TimeUnit

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainPageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainPageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentMainPageBinding
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_main_page,container,false)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        binding.wakeButtonCardView.setOnClickListener {
            binding.wakeButtonCardView.visibility = View.GONE
            binding.timeElapsedCardView.visibility = View.VISIBLE
            wakeUp()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.getElapsedTime().observe(viewLifecycleOwner, Observer {

            Timber.e((TimeUnit.MILLISECONDS.toMinutes(3660000) % 60).toString())

            var elapsedTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(it) % 60
            var elapsedTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(it) % 60
            var elapsedTimeHours = TimeUnit.MILLISECONDS.toHours(it)

//            Timber.e(elapsedTimeSeconds.toString())

            if (elapsedTimeSeconds > 0 && elapsedTimeMinutes <= 0 && elapsedTimeHours <= 0){
                binding.secondsElapsedTextView.visibility = View.VISIBLE
                binding.secondsLabelTextView.visibility = View.VISIBLE
            }else if (elapsedTimeMinutes > 0 && elapsedTimeHours <= 0){
                binding.minutesElapsedTextView.visibility = View.VISIBLE
                binding.minutesLabelTextView.visibility = View.VISIBLE
            }else if (elapsedTimeHours > 0){
                binding.hoursElapsedTextView.visibility = View.VISIBLE
                binding.hoursLabelTextView.visibility = View.VISIBLE
            }
            binding.secondsElapsedTextView.text = elapsedTimeSeconds.toString()
            binding.minutesElapsedTextView.text = elapsedTimeMinutes.toString()
            binding.hoursElapsedTextView.text = elapsedTimeHours.toString()

        })
    }

    private fun wakeUp() {
        mainViewModel.startTimerAndRecord()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainPageFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainPageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}