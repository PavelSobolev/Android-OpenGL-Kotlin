package pavelsobolev.kotogl.Spiral2D


import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import pavelsobolev.kotogl.Helpers.TiltData
import pavelsobolev.kotogl.Helpers.TiltDirections
import java.util.*

// view for rendering the 2D scene - for embedding into the activity
class MyGLSurfaceView(context: Context) : GLSurfaceView(context), Observer
{
    private val myGLRenderer: MyGLRenderer
    private val TOUCH_SCALE_RATIO = 180.0f / 400
    private var mPrevX: Float = 0.toFloat()
    private var mPrevY: Float = 0.toFloat()

    init
    {
        setEGLContextClientVersion(2)
        myGLRenderer = MyGLRenderer()
        setRenderer(myGLRenderer)
    }

    override fun onTouchEvent(motiEvent: MotionEvent): Boolean
    {
        val posx = motiEvent.x
        val posy = motiEvent.y

        when (motiEvent.action)
        {
            MotionEvent.ACTION_MOVE ->
            {
                var deltaX = posx - mPrevX
                var deltaY = posy - mPrevY

                if (posy > height / 2)
                {
                    deltaX = -deltaX
                }

                if (x < width / 2)
                {
                    deltaY = -deltaY
                }

                myGLRenderer.Angle = (myGLRenderer.Angle + (deltaX + deltaY) * TOUCH_SCALE_RATIO)
                requestRender()
            }
        }
        mPrevX = posx
        mPrevY = posy
        return true
    }


    // when information in TiltData global object is changed this object will get notification and
    // will force the scene renderer to redraw the picture
    override fun update(observableObject: Observable?, observableData: Any?)
    {
        when (TiltData.getDirection())
        {
            TiltDirections.UP -> myGLRenderer.CurrentSquareDirection = true
            TiltDirections.DOWN -> myGLRenderer.CurrentSquareDirection = false
            TiltDirections.LEFT -> myGLRenderer.CurrentTriangleDirection = true
            TiltDirections.RIGHT -> myGLRenderer.CurrentTriangleDirection = false
        }
        requestRender()
    }

    fun sendVoiceCommandData(voiceCommand: String)
    {
        var squareWords = arrayListOf<String>("square", "four", "for")
        if (squareWords.contains(voiceCommand))
        {
            myGLRenderer.CurrentSquareDirection = !myGLRenderer.CurrentSquareDirection
            return
        }


        var triangleWords = arrayListOf<String>("triangle", "three", "tree", "free")
        if (triangleWords.contains(voiceCommand))
        {
            myGLRenderer.CurrentTriangleDirection = !myGLRenderer.CurrentTriangleDirection
            return
        }

        requestRender()
    }
}
