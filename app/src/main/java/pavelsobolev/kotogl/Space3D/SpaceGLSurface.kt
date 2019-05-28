package pavelsobolev.kotogl.Space3D

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import pavelsobolev.kotogl.R
import java.util.*

/**
 * represents surface which is capable to be used by OpenGL framework
 */
class SpaceGLSurface(cntxt: Context, private var mHandleMode: Boolean) : GLSurfaceView(cntxt), Observer
{
    /**
     * renderer for this surface
     */
    private val mSpaceGlRenderer: SpaceGLRenderer

    /**
     * number which affects the velocity of rotation when touch point
     * is moving on the screen
     */
    private val SCALE_TOUCH_RATIO = 180.0f / 800

    /**
     * x-coordinate of previous location of the touch point
     */
    private var mXPrev: Float = 0.toFloat()
    private var mRotationDirection: Boolean = false

    /**
     * y-coordinate of previous location of the touch point
     */
    private var mYPrev: Float = 0.toFloat()


    init
    {
        setEGLContextClientVersion(2)

        //mTiltData = TiltData(0f, 0f)
        //TiltData.setData(0f, 0f)
        mRotationDirection = false //Y is rotational vector

        val images = intArrayOf(
                R.drawable.marble_12, R.drawable.marble_13,
                R.drawable.marble_2, R.drawable.marble_10,
                R.drawable.marble_4, R.drawable.marble_1,
                R.drawable.marble_3, R.drawable.marble_5,
                R.drawable.marble_6, R.drawable.marble_7,
                R.drawable.marble_8, R.drawable.marble_9,
                R.drawable.marble_11, R.drawable.marble_14)

        //mSpaceGlRenderer = SpaceGLRenderer(cntxt, mTiltData, images)
        mSpaceGlRenderer = SpaceGLRenderer(cntxt, /*TiltData,*/ images)

        mSpaceGlRenderer.HandleMode = mHandleMode
        setRenderer(mSpaceGlRenderer)

        if (mSpaceGlRenderer.HandleMode)
            renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        else
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }

    var isHandleMode: Boolean
        get() = mHandleMode
        set(_handleMode)
        {
            mHandleMode = _handleMode
            mSpaceGlRenderer.HandleMode = mHandleMode

            if (mSpaceGlRenderer.HandleMode)
                renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            else
                renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

            requestRender()
        }

    var isRotationDirection: Boolean
        get() = mRotationDirection
        set(_mRotationDirection)
        {
            mRotationDirection = _mRotationDirection
            mSpaceGlRenderer.HadleRotation = mRotationDirection
            requestRender()
        }

    // handles event when user touch the screen and move finger
    // on the surface of the screen of the device
    override fun onTouchEvent(e: MotionEvent): Boolean
    {
        //if(!mSpaceGlRenderer.isHandleMode()) return true;

        if (e==null) return false;

        val posx = e.x
        val posy = e.y

        when (e.action)
        {
            MotionEvent.ACTION_MOVE ->
            {
                var deltax = posx - mXPrev
                var deltay = posy - mYPrev

                if (posy > height / 2) deltax = -deltax

                if (posx < width / 2)  deltay = -deltay

                mSpaceGlRenderer.Angle = mSpaceGlRenderer.Angle + (deltax + deltay) * SCALE_TOUCH_RATIO
                requestRender()
            }
        }

        mXPrev = posx
        mYPrev = posy
        return true
    }

    fun updateDistance()
    {
        mSpaceGlRenderer.swapTextures()
        requestRender()
    }

    fun passSwapTextures()
    {
        mSpaceGlRenderer.swapTextures()
        requestRender()
    }

    // when information in TiltData global object is changed this object will get notification and
    // will force the scene renderer to redraw the picture
    override fun update(observableObject: Observable?, observableData: Any?)
    {
        mSpaceGlRenderer.setRotationDirection()
        requestRender()
    }

    fun sendVoiceCommandToRenderer(command:String)
    {
        mSpaceGlRenderer.setRotationDirection(command)
    }
}