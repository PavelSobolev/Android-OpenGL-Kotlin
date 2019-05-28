package pavelsobolev.kotogl.Spiral2D

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Square 
{
    internal val CRDS_IN_VERTEX = 3
    internal var squareCoords = floatArrayOf(-0.15f, 0.15f, 0.15f, // top left
            -0.15f, -0.15f, 0.15f, // bottom left
            0.15f, -0.15f, 0.15f, // bottom right
            0.15f, 0.15f, 0.15f) // top right

    private var mVrtxBuffer: FloatBuffer? = null
    private val mDrawListBuffer: ShortBuffer

    private val mOrderOfDraw = shortArrayOf(0, 1, 2, 0, 2, 3)
    private val mGlobalColor = floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f)
    private val mColorIncrement = floatArrayOf(0.05f, 0.001f, 0.025f, 0.001f)
    private val mColorUp = booleanArrayOf(false, true, true, true)

    private val mVrtxShaderCode = "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            "   gl_Position = uMVPMatrix * vPosition;" +
            "}"
    private val mFrgmntShaderCode = "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "   gl_FragColor = vColor;" +
            "}"

    private val mShaderProgram: Int

    private var mPstnHandle: Int = 0
    private var mClrHadle: Int = 0
    private var mMVPMtrxHandle: Int = 0

    private val mVrtxCount = squareCoords.size / CRDS_IN_VERTEX
    private val mVrtxStride = CRDS_IN_VERTEX * 4

    init 
    {
        val dlb = ByteBuffer.allocateDirect(mOrderOfDraw.size * 2)
        dlb.order(ByteOrder.nativeOrder())
        mDrawListBuffer = dlb.asShortBuffer()
        mDrawListBuffer.put(mOrderOfDraw)
        mDrawListBuffer.position(0)

        val vrtxShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                mVrtxShaderCode)
        val frgmntShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                mFrgmntShaderCode)

        mShaderProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(mShaderProgram, vrtxShader)
        GLES20.glAttachShader(mShaderProgram, frgmntShader)
        GLES20.glLinkProgram(mShaderProgram)
    }

    fun draw(mvpMtrx: FloatArray, x: Float, y: Float, z: Float) 
    {
        squareCoords = floatArrayOf(
                x - 0.05f, y + 0.05f, z,
                x - 0.05f, y - 0.05f, z,
                x + 0.05f, y - 0.05f, z,
                x + 0.05f, y + 0.05f, z)

        val bb = ByteBuffer.allocateDirect(squareCoords.size * 4)
        bb.order(ByteOrder.nativeOrder())
        mVrtxBuffer = bb.asFloatBuffer()
        mVrtxBuffer!!.put(squareCoords)
        mVrtxBuffer!!.position(0)

        GLES20.glUseProgram(mShaderProgram)

        mPstnHandle = GLES20.glGetAttribLocation(mShaderProgram, "vPosition")
        GLES20.glEnableVertexAttribArray(mPstnHandle)

        GLES20.glEnableVertexAttribArray(mPstnHandle)
        GLES20.glVertexAttribPointer(mPstnHandle, CRDS_IN_VERTEX,
                GLES20.GL_FLOAT, false, mVrtxStride, mVrtxBuffer)

        mClrHadle = GLES20.glGetUniformLocation(mShaderProgram, "vColor")
        loopColor()
        GLES20.glUniform4fv(mClrHadle, 1, mGlobalColor, 0)

        mMVPMtrxHandle = GLES20.glGetUniformLocation(mShaderProgram, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mMVPMtrxHandle, 1, false, mvpMtrx, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, mVrtxCount)
        GLES20.glDisableVertexAttribArray(mPstnHandle)
    }

    fun loopColor()
    {
        for (i in mGlobalColor.indices)
        {
            if (mColorUp[i]) {
                if (mGlobalColor[i] <= 1.1)
                    mGlobalColor[i] += mColorIncrement[i]
                else
                    mColorUp[i] = false
            }
            else
            {
                if (mGlobalColor[i] >= 0.01)
                    mGlobalColor[i] = mGlobalColor[i] - mColorIncrement[i]
                else
                    mColorUp[i] = true
            }
        }
    }
}
