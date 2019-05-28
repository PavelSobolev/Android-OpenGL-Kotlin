package pavelsobolev.kotogl.Activities

import android.app.Activity
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import pavelsobolev.kotogl.Helpers.ZObjectsPos
import pavelsobolev.kotogl.R

// activity for setting distances of the scene's objects
class DistanceActivity : AppCompatActivity()
{
    private var sbars: Array<SeekBar>? = null
    private var txts: Array<TextView>? = null
    private var txtids: IntArray? = null
    private val MIN_DIST = 3

    private var i = 0

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_distance)

        setArraysOfControls()

        i = 0
        while (i < 5)
        {
            setDataToInterface(sbars!![i], i, txts!![i])
            sbars!![i].setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {
                        var pos = i
                        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean)
                        {
                            getDataFromInterface(sbars!![pos], pos, txts!![pos])
                        }
                        override fun onStopTrackingTouch(seekBar: SeekBar) {}
                        override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    })
            i++
        }

        (findViewById(R.id.buttonOk) as Button).setOnClickListener { view ->
            setResult(Activity.RESULT_OK)
            finish()
        }

        (findViewById(R.id.buttonCancel) as Button).setOnClickListener { view ->
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun setArraysOfControls()
    {
        sbars = arrayOf(
                findViewById(R.id.seekBarCentral) as SeekBar,
                findViewById(R.id.seekBarLeft) as SeekBar,
                findViewById(R.id.seekBarRight) as SeekBar,
                findViewById(R.id.seekBarTop) as SeekBar,
                findViewById(R.id.seekBarBottom) as SeekBar)

        txts = arrayOf(
                findViewById(R.id.textViewCenter) as TextView,
                findViewById(R.id.textViewLeft) as TextView,
                findViewById(R.id.textViewRight) as TextView,
                findViewById(R.id.textViewTop) as TextView,
                findViewById(R.id.textViewBottom) as TextView)

        txtids = intArrayOf(
                R.string.center_pos_txt,
                R.string.left_pos_txt,
                R.string.right_pos_txt,
                R.string.top_pos_txt,
                R.string.bottom_pos_txt)
    }

    private fun getDataFromInterface(sb: SeekBar, i: Int, tv: TextView)
    {
        tv.text = String.format("%s is %d units", getString(txtids!![i]), sb.progress + MIN_DIST)
        ZObjectsPos.setPosition(i, -(sb.progress + MIN_DIST))
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun setDataToInterface(sb: SeekBar, i: Int, tv: TextView)
    {
        tv.text = String.format("%s is %d  units", getString(txtids!![i]), -ZObjectsPos.getPosition(i))
        sb.setProgress(-ZObjectsPos.getPosition(i) - MIN_DIST, true)
    }
}
