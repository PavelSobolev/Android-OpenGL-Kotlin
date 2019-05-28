package pavelsobolev.kotogl.Activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.annotation.RequiresApi
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main_screen.*

import pavelsobolev.kotogl.Helpers.TiltData
import pavelsobolev.kotogl.R
import pavelsobolev.kotogl.Space3D.SpaceGLSurface
import pavelsobolev.kotogl.Spiral2D.MyGLSurfaceView

// activity contains tabs for 2D and 3D scenes
@RequiresApi(api = Build.VERSION_CODES.O)
class MainScreenActivity : AppCompatActivity(), SensorEventListener
{

    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null

    private var spaceGLSurface: SpaceGLSurface? = null  //
    private var flatGLSurface: MyGLSurfaceView? = null

    internal var mainL: ConstraintLayout? = null
    private var active: View? = null

    private var isTiltable = false
    private var useTilt: Boolean = false

    private var mMyMenu: Menu? = null

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main_screen) //move

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        // main layout is a container for OpenGL surfaces and belongs to main activity
        mainL = findViewById(R.id.mainLayout)

        // ----------- creation of GL rendering surfaces

        spaceGLSurface = SpaceGLSurface(this, true) //observer of mTiltData
        TiltData.addObserver(spaceGLSurface)

        flatGLSurface = MyGLSurfaceView(this)
        TiltData.addObserver(flatGLSurface)

        // loading view into main activity

        mainL!!.addView(spaceGLSurface, //spaceGLSurface, // flatGLSurface,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT))
        active = spaceGLSurface

        // -------- setting up the accelerometer
        setAccelerometer()

        // setup voice command handler
        fab.visibility = View.GONE
        setVoiceCommandMode()
    }

    private fun setVoiceCommandMode()
    {
        fab.setOnClickListener { view ->
            intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            startActivityForResult(intent, 1010); //result handled in "onActivityResult"
        }
    }

    private fun setAccelerometer()
    {
        useTilt = false
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        TiltData.setData(0.0f, 0.0f)
    }

    internal var mTiltCounter = 0 // counter of
    internal var mTiltCounterThreshold = 10 //gap between consecutive accelerometers events which are handled

    override fun onSensorChanged(sensorEvent: SensorEvent)
    {
        if (!isTiltable) return

        if (sensorEvent.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = sensorEvent.values[0]
        val y = sensorEvent.values[1]

        //in order not ot react to any change of accelerometer (every mTiltCounterThreshold-th event accepted)
        if (mTiltCounter == mTiltCounterThreshold) TiltData.setData(x, y) // <<--- change tilt data
        if (mTiltCounter++ > mTiltCounterThreshold) mTiltCounter = 0
    }

    override fun onAccuracyChanged(sensor: Sensor, i: Int)
    {
        //this method must be implemented even if not used
    }

    override fun onResume()
    {
        super.onResume()
        if (isTiltable)
            mSensorManager!!.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)

    }
    
    override fun onPause()
    {
        super.onPause()
        if (isTiltable)
            mSensorManager!!.unregisterListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        menuInflater.inflate(R.menu.menu_main_screen, menu)
        mMyMenu = menu
        return true
    }

    // main menu events

    @SuppressLint("RestrictedApi")
    fun menuManualMode(item : MenuItem)
    {
        if (spaceGLSurface!!.isHandleMode)
        {
            spaceGLSurface!!.isHandleMode = false
            item.icon = ContextCompat.getDrawable(this, R.drawable.touch)
            fab.visibility = View.VISIBLE
            mMyMenu!!.findItem(R.id.action_Tilt).setVisible(true)
        }
        else
        {
            spaceGLSurface!!.isHandleMode = true
            item.icon = ContextCompat.getDrawable(this, R.drawable.if_ic_history_48px_352426)
            fab.visibility = View.GONE
            mMyMenu!!.findItem(R.id.action_Tilt).setVisible(false)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun menuToggle2D()
    {
        mainL!!.removeView(active)
        active = flatGLSurface
        mainL!!.addView(flatGLSurface, // cubeGLSurface,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT))

        setItemsVisibility(false)
        fab.visibility = View.GONE
        mMyMenu!!.findItem(R.id.action_Tilt).setVisible(true)
    }

    @SuppressLint("RestrictedApi")
    private fun menuToggle3D()
    {
        if (active === spaceGLSurface) return

        mainL!!.removeView(active)
        active = spaceGLSurface
        mainL!!.addView(spaceGLSurface,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT))

        setItemsVisibility(true)

        if(isTiltable)
            fab.visibility = View.GONE
        else
            fab.visibility = View.VISIBLE

    }

    @SuppressLint("RestrictedApi")
    private fun menuToggleAccelerometer(item: MenuItem)
    {
        isTiltable = !isTiltable

        if (isTiltable)
        {
            fab.visibility = View.GONE // disable voice commands

            mSensorManager!!.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
            item.icon = ContextCompat.getDrawable(this, R.drawable.nonrotate)
        }
        else
        {
            fab.visibility = View.VISIBLE // enable voice command

            mSensorManager!!.unregisterListener(this)
            item.icon = ContextCompat.getDrawable(this, R.drawable.rotate)
        }
    }


    private fun menuInvokeDistanceActivity()
    {
        val intent = Intent(this, DistanceActivity::class.java)
        startActivityForResult(intent, Activity.RESULT_OK)
    }

    private fun menuToggleRotation(item: MenuItem)
    {
        if (spaceGLSurface!!.isRotationDirection)
        {
            spaceGLSurface!!.isRotationDirection = false
            item.icon = ContextCompat.getDrawable(this, R.drawable.leftright)
        }
        else
        {
            spaceGLSurface!!.isRotationDirection = true
            item.icon = ContextCompat.getDrawable(this, R.drawable.updown)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            R.id.action_manual -> menuManualMode(item)
            R.id.action_2D -> menuToggle2D()
            R.id.action_3D -> menuToggle3D()
            R.id.action_Tilt -> menuToggleAccelerometer(item)
            R.id.action_settingsDistance -> menuInvokeDistanceActivity()
            R.id.action_swap_textures -> spaceGLSurface!!.passSwapTextures()
            R.id.action_Rotate -> menuToggleRotation(item)
        }
        return super.onOptionsItemSelected(item)
    }


    // show or hide some buttons in the main menu
    @SuppressLint("RestrictedApi")
    private fun setItemsVisibility(visible: Boolean)
    {
        mMyMenu!!.findItem(R.id.action_manual).isVisible = visible
        mMyMenu!!.findItem(R.id.action_swap_textures).isVisible = visible
        mMyMenu!!.findItem(R.id.action_settingsDistance).isVisible = visible
        mMyMenu!!.findItem(R.id.action_Rotate).isVisible = visible
        if (/*active==spaceGLSurface && */spaceGLSurface!!.isHandleMode || isTiltable)
            fab.visibility = View.GONE
        else
            fab.visibility = View.VISIBLE

    }

    // after returning from activity "DistanceActivity"
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        if (active == flatGLSurface) // commands for flat scene
        {
            if (requestCode == 1010 && resultCode == RESULT_OK) // speech recognition results arrived
            {
                decodeFlatVoiceCommands(data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS))
                //var results = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            }
            return
        }

        // else - commands for 3D scene
        if (requestCode == 1010 && resultCode == RESULT_OK) // speech recognition results arrived
        {
            decodeSpaceVoiceCommands(data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS))
            return
        }

        // new distance data results arrived
        if (resultCode == Activity.RESULT_OK)  spaceGLSurface!!.updateDistance()
    }

    fun decodeFlatVoiceCommands(results : ArrayList<String>)
    {
        if (results.count()==0) return

        var spokenText: String = results.get(0)
        flatGLSurface!!.sendVoiceCommandData(spokenText.toLowerCase())
    }

    fun decodeSpaceVoiceCommands(results : ArrayList<String>)
    {
        if (results.count()==0) return

        var spokenText: String = results.get(0)
        spaceGLSurface!!.sendVoiceCommandToRenderer(spokenText.toLowerCase())
    }
}