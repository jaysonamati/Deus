package com.amati.deus.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.widget.Toast
import com.amati.deus.Constants
import com.amati.deus.MainActivity
import com.amati.deus.R
import com.google.android.gms.location.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

class WatchingService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false

    private val db = Firebase.firestore

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // LocationRequest - Requirements for the location updates, i.e., how often you should receive
    // updates, the priority, etc.
    private lateinit var locationRequest: LocationRequest

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private lateinit var locationCallback: LocationCallback

    //Sensor Manager create an instance of sensor service
    private lateinit var sensorManager: SensorManager

    private var currentLocation: Location? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand executed with start Id: $startId")
        if (intent != null) {
            val action = intent.action
            Timber.d("using an intent with action $action")
            when (action) {
                ServiceActions.START.name -> startService()
                ServiceActions.STOP.name -> stopService()
                else -> Timber.d("This should never happen. No action in the received intent")
            }
        } else {
            Timber.d("with a null intent. It has been probably restarted by the system.")
        }

        // service is restarted if the system kills the service
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        Timber.d("The Service has been created".toUpperCase(Locale.ROOT))
        createNotification()
        val notification = showNotification("Spy service started")
        startForeground(1, notification)

        // Get Sensor Information for device
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
        val sensorPowerReq = HashMap<String, Float>()
        val sensorMinDelay = HashMap<String, Int>()

        for (deviceSensor: Sensor in deviceSensors) {
            Timber.e(deviceSensor.name.toUpperCase(Locale.ROOT))
            sensorPowerReq[deviceSensor.name] = deviceSensor.power
            sensorMinDelay[deviceSensor.name] = deviceSensor.minDelay
        }

        uploadSensorStats(sensorPowerReq, sensorMinDelay)





        locationRequest = LocationRequest.create().apply {

            // Sets the desired interval for active location updates. This interval is inexact. You
            // may not receive updates at all if no location sources are available, or you may
            // receive them less frequently than requested. You may also receive updates more
            // frequently than requested if other applications are requesting location at a more
            // frequent interval.
            //
            // IMPORTANT NOTE: Apps running on Android 8.0 and higher devices (regardless of
            // targetSdkVersion) may receive updates less frequently than this interval when the app
            // is no longer in the foreground.
            interval = TimeUnit.SECONDS.toMillis(5)

            // Sets the fastest rate for active location updates. This interval is exact, and your
            // application will never receive updates more frequently than this value.
            fastestInterval = TimeUnit.SECONDS.toMillis(3)

            // Sets the maximum time when batched location updates are delivered. Updates may be
            // delivered sooner than this interval.
            maxWaitTime = TimeUnit.MINUTES.toMillis(1)

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                currentLocation = locationResult.lastLocation

                // Updates notification content if this service is running as a foreground
                // service.
                if (isServiceStarted) {
                    var locationName = currentLocation.toString()
                    Timber.e(locationName)
                    showNotification(locationName)
                    pushUpdateToDb(currentLocation)
                }
            }
        }
    }

    private fun uploadSensorStats(
        sensorPowerReq: HashMap<String, Float>,
        sensorMinDelay: HashMap<String, Int>
    ) {
        db.collection(Constants.SENSOR_STATS_COLLECTION_NAME)
            .document(Constants.SENSOR_REQ_DOCUMENT_ID)
            .set(sensorPowerReq)

        db.collection(Constants.SENSOR_STATS_COLLECTION_NAME)
            .document(Constants.SENSOR_MIN_DELAY_DOCUMENT_ID)
            .set(sensorMinDelay)
    }

    private fun pushUpdateToDb(currentLocation: Location?) {

        val locationUpdate = hashMapOf(
            "latitude" to currentLocation?.latitude,
            "longitude" to currentLocation?.longitude,
            "altitude" to currentLocation?.altitude,
            "speed" to currentLocation?.speed,
            "bearing" to currentLocation?.bearing,
            "time" to currentLocation?.time
        )
//        val jsonLocationData = JSONObject(locationUpdate as Map<*, *>)

        db.collection(Constants.LOCATION_COLLECTION_NAME)
            .add(locationUpdate)
            .addOnSuccessListener { documentReference ->
                Timber.d("Document snapshot added with ID : ${documentReference.id})")
            }
            .addOnFailureListener { e ->
                Timber.e(e, "Error adding document")
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("The service has been destroyed".toUpperCase(Locale.ROOT))
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, WatchingService::class.java).also {
            it.setPackage(packageName)
        }

        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(
            this, 1,
            restartServiceIntent, PendingIntent.FLAG_ONE_SHOT
        )
        applicationContext.getSystemService(Context.ALARM_SERVICE)
        val alarmService: AlarmManager =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        )
    }

    private fun startService() {
        if (isServiceStarted) return
        Timber.d("Starting the foreground service task")
        Toast.makeText(this, "Service starting it's task", Toast.LENGTH_SHORT).show()
        isServiceStarted = true
        setServiceState(this, ServiceState.STARTED)
        subscribeToLocationUpdates()
        // We need this lock so that our service does not get affected by doze mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WatchingService::lock").apply {
                    acquire(10 * 60 * 1000L /*10 minutes*/)
                }
            }

        // We're starting a loop in a coroutine
        GlobalScope.launch(Dispatchers.IO) {
            while (isServiceStarted) {
                launch(Dispatchers.IO) {
//                    pingDeviceLocation()
                }
//                delay(1000)
            }
            Timber.d("End of loop for the service")
        }
    }

    private fun stopService() {
        Timber.d("Stopping the foreground service")
        Toast.makeText(this, "Service Stopping", Toast.LENGTH_SHORT).show()
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            Timber.e("Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
        setServiceState(this, ServiceState.STOPPED)
    }

    private fun pingDeviceLocation() {
        subscribeToLocationUpdates()
    }

    private fun createNotification() {


        // depending on the android API level we're dealing with we will have
        // to use a specific method to create the notification
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Endless service notification channel",
            NotificationManager.IMPORTANCE_HIGH
        ).let {
            it.description = "Endless service channel"
            it.enableLights(true)
            it.lightColor = Color.CYAN
            it.enableVibration(true)
            it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            it
        }
        notificationManager.createNotificationChannel(channel)


    }

    private fun showNotification(notificationMsg: String): Notification {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val builder: Notification.Builder = Notification.Builder(
            this, NOTIFICATION_CHANNEL_ID
        )

        return builder
            .setContentTitle("Spy Service")
            .setContentText(notificationMsg)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setTicker("The spy service continues")
            .build()
    }

    private fun updateNotification(notificationMsg: String) {

    }

    fun subscribeToLocationUpdates() {
        Timber.d("Subscribing to location Updates")
        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
            Timber.d("Subscribed to location Updates")
        } catch (exception: SecurityException) {
            Timber.e(exception)
        }

    }

    fun unsubscribeToLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "ENDLESS SERVICE CHANNEL"
    }


//    private fun showNotification(){
//
//    }
}