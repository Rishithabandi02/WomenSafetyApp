package com.example.safeher.services
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(
    context: Context,
    private val onShakeDetected: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)


    // Threshold — how hard the shake must be to trigger
    private val SHAKE_THRESHOLD = 18f
    private val SHAKE_COOLDOWN = 3000L // 3 seconds between triggers

    private var lastShakeTime = 0L
    private var shakeCount = 0
    private var firstShakeTime = 0L

    private val REQUIRED_SHAKES = 3
    private val SHAKE_WINDOW = 2000L


    fun start() {
        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Calculate total force minus gravity
        val gForce = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH

        if (gForce > SHAKE_THRESHOLD) {
            val now = System.currentTimeMillis()

            // Cooldown — prevent multiple triggers from one shake
            if (firstShakeTime == 0L ||
                now - firstShakeTime > SHAKE_WINDOW
            ) {
                firstShakeTime = now
                shakeCount = 1
            } else {
                shakeCount++
            }
            if (shakeCount >= REQUIRED_SHAKES &&
                now - lastShakeTime > SHAKE_COOLDOWN
            ) {

                lastShakeTime = now

                shakeCount = 0
                firstShakeTime = 0L

                onShakeDetected()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}