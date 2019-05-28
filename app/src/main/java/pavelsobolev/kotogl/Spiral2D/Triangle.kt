package pavelsobolev.kotogl.Spiral2D

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

// represent the triangle which has different color for aech vertex with using of interpolation
// color of each vertex is changing cyclically during the process of rendering
class Triangle
{

    private val COORDS_PER_VERTEX = 7
    private val mFloatSizeInBytes = 4

    private var mVertexBuffer: FloatBuffer? = null

    private var mTriangle1VertData = floatArrayOf(
            // X, Y, Z,
            // R, G, B, A
            -0.5f, -0.25f, 0.0f, // 0  1  2
            1.0f, 0.0f, 0.0f, 1.0f, // 3  4  5  6

            0.5f, -0.25f, 0.0f, // 7 8 9
            0.0f, 1.0f, 0.0f, 1.0f, // 10  11 12 13

            0.0f, 0.559016994f, 0.0f, // 14 15 16
            0.0f, 0.0f, 1.0f, 1.0f  // 17  18  19  20
    )

    private val mColors = floatArrayOf(
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f)

    private val mColorIncrement = floatArrayOf(
            0.05f, 0.001f, 0.025f, 0.001f,
            0.02f, 0.02f, 0.025f, 0.001f,
            0.01f, 0.005f, 0.025f, 0.001f,
            0.04f, 0.005f, 0.025f, 0.001f)

    private val mColorUp = booleanArrayOf(
            false, true, true,
            true, true, false,
            true, true, true,
            true, false, true)

    private val mColorOffset = 3

    //
    private val mPositionDataSize = 3

    /** Size of the color data in elements.  */
    private val mColorDataSize = 4
    //internal var color = floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f)

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

    private val mShaderProgram: Int

    private var mPositionHandle: Int = 0
    private var mColorHandle: Int = 0
    private var mMVPMatrixHandle: Int = 0

    private val vertexCount = mTriangle1VertData.size / COORDS_PER_VERTEX
    private val vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    init  // constructor
    {
        val vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                mVrtxShaderCode)
        val fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                mFrgmntShaderCode)

        mShaderProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(mShaderProgram, vertexShader)
        GLES20.glAttachShader(mShaderProgram, fragmentShader)
        GLES20.glBindAttribLocation(mShaderProgram, 0, "a_Position")
        GLES20.glBindAttribLocation(mShaderProgram, 1, "a_Color")
        GLES20.glLinkProgram(mShaderProgram)
    }

    fun draw(mvpMatrix: FloatArray, x: Float, y: Float, z: Float)
    {

        mTriangle1VertData = floatArrayOf(
                x, y + 0.08f, z,
                mColors[0], mColors[1], mColors[2], mColors[3],
                x - 0.08f, y - 0.08f, z,
                mColors[4], mColors[5], mColors[6], mColors[7],
                x + 0.08f, y - 0.08f, z,
                mColors[8], mColors[9], mColors[10], mColors[11])

        val bb = ByteBuffer.allocateDirect(mTriangle1VertData.size * mFloatSizeInBytes)
        bb.order(ByteOrder.nativeOrder())
        mVertexBuffer = bb.asFloatBuffer()
        mVertexBuffer!!.put(mTriangle1VertData)
        mVertexBuffer!!.position(0)

        GLES20.glUseProgram(mShaderProgram)

        loopColor() //change color cyclically


        mPositionHandle = GLES20.glGetAttribLocation(mShaderProgram, "a_Position")

        // triangle coordinate data
        mVertexBuffer!!.position(0)
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize,
                GLES20.GL_FLOAT, false,
                vertexStride, mVertexBuffer)

        GLES20.glEnableVertexAttribArray(mPositionHandle)


        // Set mColors for drawing the triangle

        mColorHandle = GLES20.glGetAttribLocation(mShaderProgram, "a_Color")
        mVertexBuffer!!.position(mColorOffset)
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                vertexStride, mVertexBuffer)
        GLES20.glEnableVertexAttribArray(mColorHandle)

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mShaderProgram, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)


        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle)

    }

    fun loopColor() //change color of each vertex cyclically
    {
        for (k in 0..11)
        {
            if (mColorUp[k])
            {
                if (mColors[k] <= 1.1)
                    mColors[k] += mColorIncrement[k]
                else
                    mColorUp[k] = false
            }
            else
            {
                if (mColors[k] >= 0.01)
                    mColors[k] -= mColorIncrement[k]
                else
                    mColorUp[k] = true
            }
        }
    }
}