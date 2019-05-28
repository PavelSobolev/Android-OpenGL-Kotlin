package pavelsobolev.kotogl.Spiral2D

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Spiral
{
    internal val COORDS_IN_VERTEX = 7
    
    private val mVrtxBffr: FloatBuffer
    private val mFloatSize = 4
    private val mCntPoint = 360 * 6

    val mSpiralVirtices = FloatArray(mCntPoint * COORDS_IN_VERTEX)

    private var mIncrementalRadius = 0.0f
    private var mSpiralSpan = 0.0003f

    private var mUp = true

    private val mClrOffset = 3

    /** Size of the position data in elements.  */
    private val mPstnDataSize = 3

    /** Size of the color data in elements.  */
    private val mClrDataSize = 4

    private val mVrtxShaderCode = "uniform mat4 uMVPMatrix;" +
            "attribute vec4 a_Position;" +
            "attribute vec4 a_Color;" +
            "varying vec4 v_Color;" +
            "void main() {" +
            "  v_Color = a_Color;" +
            "  gl_Position = uMVPMatrix * a_Position;" +
            "}"
    private val mFrgmntShaderCode = "precision mediump float;" +
            "varying vec4 v_Color;" +
            "void main() {" +
            "  gl_FragColor = v_Color;" +
            "}"

    private val mSdrProgram: Int

    private var mPstnHandle: Int = 0
    private var mClrHandle: Int = 0
    private var mMVPMtrxHandle: Int = 0

    private val mVrtxCount = mSpiralVirtices.size / COORDS_IN_VERTEX
    private val mVrtxStride = COORDS_IN_VERTEX * 4 // 4 bytes per vertex

    init // constructor
    {
        GLES20.glLineWidth(3.0f)
        mIncrementalRadius = 0.0f
        mSpiralSpan = 0.00042f

        var angl = 0.0f   //helper vars
        var color = 0.0f
        var i = 0


        // circle for constructing of spiral
        while (i < mCntPoint * COORDS_IN_VERTEX)
        {
            // position
            mSpiralVirtices[i] = mIncrementalRadius * Math.sin(angl.toDouble()).toFloat()
            mSpiralVirtices[i + 1] = mIncrementalRadius * Math.cos(angl.toDouble()).toFloat()
            mSpiralVirtices[i + 2] = 0.0f
            // color
            mSpiralVirtices[i + 3] = color
            mSpiralVirtices[i + 4] = Math.cos(color.toDouble()).toFloat()
            mSpiralVirtices[i + 5] = Math.sin(color.toDouble()).toFloat()
            mSpiralVirtices[i + 6] = 1.0f
            angl += (Math.PI / 180.0).toFloat()
            mIncrementalRadius += mSpiralSpan

            if (mUp)
                color += 0.1f
            else
                color -= 0.1f

            if (color == 1f)
                mUp = false
            if (color == 0f)
                mUp = true
            i += COORDS_IN_VERTEX

        }

        // construction of buffer
        val bb = ByteBuffer.allocateDirect(mSpiralVirtices.size * mFloatSize)
        bb.order(ByteOrder.nativeOrder())
        mVrtxBffr = bb.asFloatBuffer()
        mVrtxBffr.put(mSpiralVirtices)
        mVrtxBffr.position(0)

        val vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                mVrtxShaderCode)
        val fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                mFrgmntShaderCode)

        mSdrProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(mSdrProgram, vertexShader)
        GLES20.glAttachShader(mSdrProgram, fragmentShader)
        GLES20.glBindAttribLocation(mSdrProgram, 0, "a_Position")
        GLES20.glBindAttribLocation(mSdrProgram, 1, "a_Color")
        GLES20.glLinkProgram(mSdrProgram)
    }

    fun getSpiralVirtices(): FloatArray {
        return mSpiralVirtices
    }

    fun draw(mvpMatrix: FloatArray)
    {
        GLES20.glUseProgram(mSdrProgram)

        mPstnHandle = GLES20.glGetAttribLocation(mSdrProgram, "a_Position")

        mVrtxBffr.position(0)
        GLES20.glVertexAttribPointer(mPstnHandle, mPstnDataSize,
                GLES20.GL_FLOAT, false,
                mVrtxStride, mVrtxBffr)
        GLES20.glEnableVertexAttribArray(mPstnHandle)


        mClrHandle = GLES20.glGetAttribLocation(mSdrProgram, "a_Color")
        mVrtxBffr.position(mClrOffset)
        GLES20.glVertexAttribPointer(mClrHandle, mClrDataSize, GLES20.GL_FLOAT, false,
                mVrtxStride, mVrtxBffr)
        GLES20.glEnableVertexAttribArray(mClrHandle)

        mMVPMtrxHandle = GLES20.glGetUniformLocation(mSdrProgram, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mMVPMtrxHandle, 1, false, mvpMatrix, 0)

        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, mVrtxCount)

        GLES20.glDisableVertexAttribArray(mPstnHandle)

    }
}
