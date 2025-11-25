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
import androidx.lifecycle.ViewModel
import com.example.compassandlevel.ui.theme.CompassAndLevelTheme

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager : SensorManager
    private var accelerometer : Sensor? = null
    private var magnetometer : Sensor? = null

//    private var gyroscope : Sensor? : null

    // accelerometer readings
//    private var _x by mutableFloatStateOf(0f)
//    private var _y by mutableFloatStateOf(0f)
//    private var _z by mutableFloatStateOf(0f)
//
//    // magnetometer readigns
//    private var _mx by mutableFloatStateOf(0f)
//    private var _my by mutableFloatStateOf(0f)
//    private var _mz by mutableFloatStateOf(0f)
    private var accelerometerValues: FloatArray? = null
    private var magnetometerValues: FloatArray? = null

    private var accelerometerReading by mutableStateOf(false)
    private var magnetometerReading by mutableStateOf(false)
    private var _accuracy by mutableStateOf("Unknown")

    private var _gx by mutableFloatStateOf(0f)
    private var _gy by mutableFloatStateOf(0f)
    private var _gz by mutableFloatStateOf(0f)

    private var _orientationAngle by mutableFloatStateOf(0f)
    private var _compassDirection by mutableStateOf("")

    // uing my view model
    val viewModel: MyViewModel = MyViewModel()




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
                    CompassScreen(viewModel)
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
//                    _x = it.values.clone()[0]
//                    _y = it.values.clone()[1]
//                    _z = it.values.clone()[2]
                    accelerometerValues = it.values.clone()
                    accelerometerReading = true
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
//                    _mx = it.values.clone()[0]
//                    _my = it.values.clone()[1]
//                    _mz = it.values.clone()[2]
                    magnetometerValues = it.values.clone()
                    magnetometerReading = true
                }
            }
        }
        // do this only if you have all readings
        if (accelerometerReading && magnetometerReading) {
//            _orientationAngle =
//                calculateOrientation(floatArrayOf(_x, _y, _z), floatArrayOf(_mx, _my, _mz))
//            _compassDirection = _orientationAngle.toString()
//            viewModel.updateOrientation(floatArrayOf(_x, _y, _z), floatArrayOf(_mx, _my, _mz))
            viewModel.updateOrientation(accelerometerValues, magnetometerValues)


        }
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
fun CompassScreen(viewModel: MyViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Direction: ${viewModel.compassDirection}")
        Text(text = "Angle: ${viewModel.orientationAngle.toInt()}°")
        Text(text = "Raw Angle: ${"%.2f".format(viewModel.orientationAngle)}°")
    }
}

private fun calculateOrientation(accelerometerReading : FloatArray?, magnetometerReading : FloatArray?) : Float {
    val rotationMatrix = FloatArray(9)
//    SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
    val success = SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)

    if (!success) {
        android.util.Log.e("CompassDebug", "getRotationMatrix failed!")
        return 0f
    }

    val orientationAngles = FloatArray(3)
    SensorManager.getOrientation(rotationMatrix, orientationAngles)

    // azimuth angles orientationAngles[0] correspond to compass direction
    var azimuthRadians = orientationAngles[0]
    var azimuthAngle = Math.toDegrees(azimuthRadians.toDouble()).toFloat()
    if (azimuthAngle < 0) {
        azimuthAngle += 360 // Ensure positive values
    }
    return azimuthAngle
}

// need a viewmodel because the configuration change makes everyhting reset as N
// cant use rmemeber saveable from main (or any non composable) so just use view model

class MyViewModel : ViewModel() {
    var orientationAngle by mutableFloatStateOf(0f)
    var compassDirection by mutableStateOf("")

    // function to update orientation
    fun updateOrientation(accelerometerReading : FloatArray?, magnetometerReading : FloatArray?) {
        android.util.Log.d("CompassDebug", "Accel: ${accelerometerReading.contentToString()}")
        android.util.Log.d("CompassDebug", "Mag: ${magnetometerReading.contentToString()}")

        val azimuthAngle = calculateOrientation(accelerometerReading, magnetometerReading)
        android.util.Log.d("CompassDebug", "Azimuth angle after normalization: $azimuthAngle")
        orientationAngle = azimuthAngle
        compassDirection = when {
            azimuthAngle >= 337.5f || azimuthAngle < 22.5f -> "N"
            azimuthAngle >= 22.5f && azimuthAngle < 67.5f -> "NE"
            azimuthAngle >= 67.5f && azimuthAngle < 112.5f -> "E"
            azimuthAngle >= 112.5f && azimuthAngle < 157.5f -> "SE"
            azimuthAngle >= 157.5f && azimuthAngle < 202.5f -> "S"
            azimuthAngle >= 202.5f && azimuthAngle < 247.5f -> "SW"
            azimuthAngle >= 247.5f && azimuthAngle < 292.5f -> "W"
            azimuthAngle >= 292.5f && azimuthAngle < 337.5f -> "NW"
            else -> "Unknown"
        }
    }
}


// level stuff??

