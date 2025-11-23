package com.example.compassandlevel

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.compassandlevel.ui.theme.CompassAndLevelTheme

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager : SensorManager
    private var accelerometer : Sensor? = null
    private var magnetometer : Sensor? = null

    // accelerometer readings
    private var _x by mutableFloatStateOf(0f)
    private var _y by mutableFloatStateOf(0f)
    private var _z by mutableFloatStateOf(0f)

    // magnetometer readigns
    private var _mx by mutableFloatStateOf(0f)
    private var _my by mutableFloatStateOf(0f)
    private var _mz by mutableFloatStateOf(0f)
    private var _accuracy by mutableStateOf("Unknown")

    private var _orientationAngle by mutableFloatStateOf(0f)
    private var _compassDirection by mutableStateOf("")




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initalize
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        enableEdgeToEdge()
        setContent {
            CompassAndLevelTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
                    CompassScreen(direction = _compassDirection)
                }
            }
        }
    }

    // other functions for sensors
    override fun onResume() {
        super.onResume()
        // register both listeners
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        // unregisters all listeners
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event : SensorEvent?) {
        event?.let {
            when(it.sensor.type) { // do different things depending on sensor type
                Sensor.TYPE_ACCELEROMETER -> {
                    _x = it.values[0]
                    _y = it.values[1]
                    _z = it.values[2]
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    _mx = it.values[0]
                    _my = it.values[1]
                    _mz = it.values[2]
                }
            }
        }
        _orientationAngle = calculateOrientation(floatArrayOf(_x, _y, _z), floatArrayOf(_mx, _my, _mz))
        _compassDirection = _orientationAngle.toString()
//        _compassDirection = when(_orientationAngle) {
//            0f -> "N"
//            90f -> "E"
//            180f -> "S"
//            270f -> "W"
//            else -> {"middle"}
//        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _accuracy = when (accuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "High"
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Medium"
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Low"
            SensorManager.SENSOR_STATUS_UNRELIABLE -> "Unreliable"
            else -> "Unknown"
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CompassAndLevelTheme {
        Greeting("Android")
    }
}

//Create a compass using the magnetometer and accelerometer,
// and a digital level using the gyroscope.
//•	Use the magnetometer and accelerometer to calculate the compass heading.
//•	Display the compass needle using a Canvas or Image rotation in Compose.
//•	Use the gyroscope to detect tilt and
// display a digital level with roll and pitch values.
//
//Make the UI interesting and fun.

@Composable
fun CompassScreen(direction : String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = direction)
    }
}

private fun calculateOrientation(accelerometerReading : FloatArray, magnetometerReading : FloatArray) : Float {
    val rotationMatrix = FloatArray(9)
    SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)

    val orientationAngles = FloatArray(3)
    SensorManager.getOrientation(rotationMatrix, orientationAngles)

    // azimuth angles orientationAngles[0] correspond to compass direction
    var azimuthAngle = orientationAngles[0]
    if (azimuthAngle < 0) {
        azimuthAngle += 360; // Ensure positive values
    }
    return azimuthAngle
}