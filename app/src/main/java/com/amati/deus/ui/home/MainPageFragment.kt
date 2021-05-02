package com.amati.deus.ui.home

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.amati.deus.DeusApplication
import com.amati.deus.MainActivity
import com.amati.deus.R
import com.amati.deus.databinding.FragmentMainPageBinding
import com.amati.deus.models.DeviceSensor
import com.amati.deus.services.ServiceActions
import com.amati.deus.services.ServiceState
import com.amati.deus.services.WatchingService
import com.amati.deus.services.getServiceState
import com.amati.deus.utils.SharedPreferencesManager
import timber.log.Timber
import java.util.*
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

    private lateinit var sensorManager: SensorManager
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    private lateinit var binding: FragmentMainPageBinding
    private lateinit var mainViewModel: MainViewModel
    private val sensorViewModel: SensorViewModel by viewModels {
        SensorViewModelFactory((requireActivity().application as DeusApplication).repository)
    }

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_page, container, false)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        sharedPreferencesManager = SharedPreferencesManager(requireContext())

        binding.wakeButtonCardView.setOnClickListener {
            binding.wakeButtonCardView.visibility = View.GONE
            binding.timeElapsedCardView.visibility = View.VISIBLE
            wakeUp()
        }

        binding.timeElapsedCardView.setOnClickListener {
            Timber.d("Stop the foreground service on demand".toUpperCase(Locale.ROOT))
            actionOnService(ServiceActions.STOP)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.getElapsedTime().observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            Timber.e((TimeUnit.MILLISECONDS.toMinutes(3660000) % 60).toString())

            var elapsedTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(it) % 60
            var elapsedTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(it) % 60
            var elapsedTimeHours = TimeUnit.MILLISECONDS.toHours(it)

//            Timber.e(elapsedTimeSeconds.toString())

            if (elapsedTimeSeconds > 0 && elapsedTimeMinutes <= 0 && elapsedTimeHours <= 0) {
                binding.secondsElapsedTextView.visibility = View.VISIBLE
                binding.secondsLabelTextView.visibility = View.VISIBLE
            } else if (elapsedTimeMinutes > 0 && elapsedTimeHours <= 0) {
                binding.minutesElapsedTextView.visibility = View.VISIBLE
                binding.minutesLabelTextView.visibility = View.VISIBLE
            } else if (elapsedTimeHours > 0) {
                binding.hoursElapsedTextView.visibility = View.VISIBLE
                binding.hoursLabelTextView.visibility = View.VISIBLE
            }
            binding.secondsElapsedTextView.text = elapsedTimeSeconds.toString()
            binding.minutesElapsedTextView.text = elapsedTimeMinutes.toString()
            binding.hoursElapsedTextView.text = elapsedTimeHours.toString()

        })
    }

    private fun checkSensorsAvailable() {
        if (sharedPreferencesManager.hasCheckedSensors()) {
            showAvailableSensors()
        } else {
            sensorManager =
                requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
            updateDeviceSensorsInDb(deviceSensors)
        }

    }

    private fun updateDeviceSensorsInDb(deviceSensors: List<Sensor>) {
        for (deviceSensor: Sensor in deviceSensors) {
            val sensor = DeviceSensor(deviceSensor.id, deviceSensor.name)
            sensorViewModel.insert(sensor)
        }
    }

    private fun showAvailableSensors() {
        TODO("Not yet implemented")
    }


    private fun wakeUp() {
        mainViewModel.startTimerAndRecord()
//        Intent(requireActivity(), ExampleService::class.java).also { intent ->
//            activity?.startService(intent)
//        }
        Timber.d("Start the foreground service on demand".toUpperCase(Locale.ROOT))
        actionOnService(ServiceActions.START)
    }

    private fun actionOnService(action: ServiceActions) {
        if (getServiceState(requireContext()) == ServiceState.STOPPED && action == ServiceActions.STOP) return
        Intent(requireContext(), WatchingService::class.java).also {
            it.action = action.name
            activity?.startForegroundService(it)
            return
        }
    }


    private fun forGroundServiceExample() {
        val CHANNEL_DEFAULT_IMPORTANCE = "CHANNEL_DEFAULT_IMPORTANCE"

        val pendingIntent: PendingIntent =
            Intent(requireContext(), MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(requireContext(), 0, notificationIntent, 0)
            }

        val notification: Notification =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Notification.Builder(requireContext(), "CHANNEL_DEFAULT_IMPORTANCE")
                    .setContentTitle("Example Service Notification")
                    .setContentText("This shows that a foreground service has started")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
                    .setTicker("This is the ticker text")
                    .build()
            } else {
                Notification.Builder(requireContext())
                    .build()
            }

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