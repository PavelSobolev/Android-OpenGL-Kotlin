package pavelsobolev.kotogl.Spiral2D

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import javax.microedition.khronos.opengles.GL10

// 2D renderer - shows the spiral, the square and the triangle
//    using OpenGL
class MyGLRenderer : GLSurfaceView.Renderer 
{
    internal var mW = 0
    internal var mH = 0

    var mIsHandleMode: Boolean = false

    //@Volatile
    var mAngle: Float = 0.toFloat()
    private var mRotatDir: Float = 0.toFloat()

    private var mTriangle: Triangle? = null
    private var mSquare: Square? = null
    private var mSpiral: Spiral? = null

    private var mCrrntPointSquare = 0
    private var mCrrntPointSquareUp = true

    private var mCrrntPointTri = 0
    private var mCrrntPointTriUp = true

    private val mMtrxMVP = FloatArray(16) // model-view-projection matrix
    private val mMtrxProjection = FloatArray(16)
    private val mMtrxView = FloatArray(16)
    private val mMtrxRotation = FloatArray(16)
    //private val mTranslationMatrix = FloatArray(16)
    
    override fun onSurfaceCreated(gl10: GL10, eglConfig: javax.microedition.khronos.egl.EGLConfig) 
    {
        mAngle = 0.0f
        mRotatDir = 1f
        GLES20.glClearColor(0.7f, 0.7f, 0.7f, 1.0f)
        mTriangle = Triangle()
        mSquare = Square()
        mSpiral = Spiral()
        mCrrntPointSquare = 0
        mCrrntPointSquareUp = true

        mCrrntPointTri = mSpiral!!.getSpiralVirtices().size - 7
        mCrrntPointSquareUp = false
    }

    var Angle : Float
        get() = mAngle
        set(newVal)
        {
            mAngle = newVal
        }

    override fun onDrawFrame(unused: GL10) 
    {
        val localMtrx = FloatArray(16)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        if (!mIsHandleMode)
        {
            val time = SystemClock.uptimeMillis() % 40000L
            mAngle = 0.009f * time.toInt()
        }

        val eyeX = 0.0f
        val eyeY = 0.0f
        val eyeZ = 1.5f

        val lookX = 0.0f
        val lookY = 0.0f
        val lookZ = 0.0f

        val upX = 0.0f
        val upY = 1.0f
        val upZ = 0.0f

        Matrix.setLookAtM(mMtrxView, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ)
        Matrix.multiplyMM(mMtrxMVP, 0, mMtrxProjection, 0, mMtrxView, 0)

        Matrix.setIdentityM(localMtrx, 0)
        Matrix.setRotateM(mMtrxRotation, 0, mRotatDir * mAngle, 0.0f, 0.0f, 1.0f)
        Matrix.multiplyMM(localMtrx, 0, mMtrxMVP, 0, mMtrxRotation, 0)

        mSpiral!!.draw(localMtrx)

        mSquare!!.draw(localMtrx,
                mSpiral!!.getSpiralVirtices()[mCrrntPointSquare],
                mSpiral!!.getSpiralVirtices()[mCrrntPointSquare + 1],
                mSpiral!!.getSpiralVirtices()[mCrrntPointSquare + 2])

        if (mCrrntPointSquareUp)
        {
            if (mCrrntPointSquare < mSpiral!!.getSpiralVirtices().size - 7)
                mCrrntPointSquare += 7
            else
            {
                mCrrntPointSquareUp = false
            }
        }
        else
        {
            if (mCrrntPointSquare > 0)
            {
                mCrrntPointSquare -= 7
            }
            else
            {
                mCrrntPointSquareUp = true
            }
        }

        mTriangle!!.draw(localMtrx,
                mSpiral!!.getSpiralVirtices()[mCrrntPointTri],
                mSpiral!!.getSpiralVirtices()[mCrrntPointTri + 1],
                mSpiral!!.getSpiralVirtices()[mCrrntPointTri + 2])

        if (mCrrntPointTriUp)
        {
            if (mCrrntPointTri < mSpiral!!.getSpiralVirtices().size - 7)
                mCrrntPointTri += 7
            else
                mCrrntPointTriUp = false
        }
        else
        {
            if (mCrrntPointTri > 0)
                mCrrntPointTri -= 7
            else
                mCrrntPointTriUp = true
        }
    }


    var CurrentSquareDirection : Boolean
        get() = mCrrntPointSquareUp
        set(newDirect)
        {
            mCrrntPointSquareUp = newDirect
        }

    var CurrentTriangleDirection : Boolean
        get() = mCrrntPointTriUp
        set(newDirect)
        {
            mCrrntPointTriUp = newDirect
        }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) 
    {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height
        //Matrix.perspectiveM(mMtrxProjection,);
        val left = -ratio
        val bottom = -1.0f
        val top = 1.0f
        val near = 1.0f
        val far = 10.0f
        Matrix.frustumM(mMtrxProjection, 0, left, ratio, bottom, top, near, far)

    }

    companion object
    {
        fun loadShader(type: Int, shaderCode: String): Int 
        {
            val shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            return shader
        }
    }
}
