package pavelsobolev.kotogl.Space3D

import android.app.Activity
import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import pavelsobolev.kotogl.R
import pavelsobolev.kotogl.Helpers.TiltData
import pavelsobolev.kotogl.Helpers.TiltDirections
import pavelsobolev.kotogl.Helpers.ZObjectsPos
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


//class for representing of OpenGL renderer (used for drawing on activity surface)

// private val mActivityContext: Context,// reference to current activity context
// internal var mTiltData: TiltData,  // device tilt data for changing of the direction of rotation of polyhedrons
// private val mTextures: IntArray) // data for texture binding
class SpaceGLRenderer(private val mActivityContext: Context,
                      private val mTextures: IntArray) : GLSurfaceView.Renderer
{
    
    var mHandleMode: Boolean = false

    //angles of rotation
    private var mAngleBodies: Float = 0.toFloat()
    private var mAngleCenter: Float = 0.toFloat()
    private var mGlobalAngle: Float = 0.toFloat()

    //vector around which scene is rotating (this value is affected by accelerometer)
    private var mRotationVector: FloatArray? = null

    // attribute of direction of rotation by touch gestures
    //if true oX is rotational axis, otherwise Y is rotational axis
    private var mHadleRotation: Boolean = false

     //mModelMtrx stores the model matrix
    private var mModelMtrx = FloatArray(16)

    //private val mGlobalMatrix = FloatArray(16)

    //mViewMtrx stores the view matrix
    private val mViewMtrx = FloatArray(16)

    // mProjMtrx stores the projection matrix
    private val mProjMtrx = FloatArray(16)

    //mMVPMtrx stores the final matrix
    private val mMVPMtrx = FloatArray(16)


    // mLightMdlMtrx stores a copy of the model matrix for the light position
    private val mLightMdlMtrx = FloatArray(16)

    // model data in a program's float buffer (for hexahedron's coordinates)
    private var mHexahedronPos: FloatBuffer? = null


     // model data in a program's float buffer (for hexahedron's colors)
    private var mHexahedronClrs: FloatBuffer? = null

    //model data in a program's float buffer (for hexahedron's normal vectors)
    private var mHexahedronNrmls: FloatBuffer? = null

    // model data in a program's float buffer (for tetrahedron's coordinates)
    private var mTetraPosns: FloatBuffer? = null

    // model data in a program's float buffer (for tetrahedron's colors)
    private var mTetraClrs: FloatBuffer? = null

    // model data in a program's float buffer (for tetrahedron's normal vectors)
    private var mTetraNrmls: FloatBuffer? = null

    // transformation matrix
    private var mMVPMtrxHndl: Int = 0

    // modelview matrix
    private var mMVMtrxHndl: Int = 0

    // light position
    private var mLightPsHndl: Int = 0

    // position information (hexahedron)
    private var mHexahedronPstnHndl: Int = 0

    // color information (hexahedron)
    private var mHexahedronClrHndl: Int = 0

    // normal vector information (hexahedron)
    private var mHexahedronNrmlHndl: Int = 0

    // position information (tetrahedron)
    private var mTetraPstnHndl: Int = 0

    // color information (tetrahedron)
    private var mTetraClrHndl: Int = 0

    // normal vector information (tetrahedron)
    private var mTetraNrmlHndl: Int = 0

    // size of float number in bytes
    private val mFloatSize : Int = 4

    // number of parameters for the position of one vertex (x,y,z)
    private val mPosDataSize : Int = 3

    // number of parameters for the color data for each vertes (red, green,blue, alpha)
    private val mClrDataSize : Int = 4

    // number of coordinates of normal vector
    private val mNrmlDataSize : Int = 3


    // ********* ------------- light positions ---------   **********************

    // Used to keep light source in the center of the origin in model coordinates
    private val mLightPosInModelCoords = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)

    // Used to keep the current position of the light
    private val mLightPosInWorldCoords = FloatArray(4)

    // Used to keep the transformed position of the light
    private val mLightPosInLookAtCoords = FloatArray(4)


    // -------- OpenGL ES programs handles
    // hexahedron's program
    private var mHexahedronVertexPrgrmHndl: Int = 0

    // tetrahedron's program
    private var mTetraVertexPrgrmHndl: Int = 0

    // light's program
    private var mLightPointPrgrmHndl: Int = 0


    // -------------- texture handling -------------------------

    // Store our model data in a float buffer.  */
    private var mHexahedronTextureCoords: FloatBuffer? = null

    // This will be used to pass in the texture.  */
    private var mTextureUniformHndl: Int = 0

    // This will be used to pass in model texture coordinate information.  */
    private var mTextureCoordHndl: Int = 0

    // Size of the texture coordinate data in elements.  */
    private val mTextureCoordinateDtSz = 2

    // Array of textures data  */
    private val mTextureDataHndls: IntArray


    // position characteristics of camera
    //private val xMove = 0.0f
    //private val yMove = 0.0f
    private var lkX = 0.0f
    private var lkY = 0.0f
    //private val lkZ = -5.0f

    init
    {
        mTextureDataHndls = IntArray(14)

        mAngleBodies = 0.0f // rotational angles
        mAngleCenter = 0.0f
        mGlobalAngle = 0.0f

        mRotationVector = floatArrayOf(0.0f, 0.0f, 1.0f)

        mHadleRotation = false

        initHexahedron()

        initTetrahedron()

    }

    fun initHexahedron()
    {
        // hexahedron settings -------------------

        // X, Y, Z
        val hexahedronPositionData = VertexSource.HexahedronCoordinates

        // R, G, B, A
        val hexahedronColorData = VertexSource.HexahedronColors

        // X, Y, Z
        val hexahedronNormalData = VertexSource.HexahedronNormalVectors

        // Initialize the buffers or hexahedron
        mHexahedronPos = ByteBuffer.allocateDirect(hexahedronPositionData.size * mFloatSize)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mHexahedronPos!!.put(hexahedronPositionData).position(0)

        mHexahedronClrs = ByteBuffer.allocateDirect(hexahedronColorData.size * mFloatSize)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mHexahedronClrs!!.put(hexahedronColorData).position(0)

        mHexahedronNrmls = ByteBuffer.allocateDirect(hexahedronNormalData.size * mFloatSize)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mHexahedronNrmls!!.put(hexahedronNormalData).position(0)

        val hexahedronTextureCoordinateData = VertexSource.HexahedronTextureCoordinateData
        mHexahedronTextureCoords = ByteBuffer.allocateDirect(hexahedronTextureCoordinateData.size * mFloatSize).order(ByteOrder.nativeOrder()).asFloatBuffer()
        mHexahedronTextureCoords!!.put(hexahedronTextureCoordinateData).position(0)
    }

    fun initTetrahedron()
    {
        // -------------- tetrahedron settings

        // X, Y, Z
        val tetraPositionData = VertexSource.TetrahedronCoordinates

        // R, G, B, A
        val tetraColorData = VertexSource.TetrahedronColors

        // X, Y, Z
        val tetraNormalData = VertexSource.TetrahedronNormalVectors

        // Initialize the buffers or hexahedron
        mTetraPosns = ByteBuffer.allocateDirect(tetraPositionData.size * mFloatSize)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mTetraPosns!!.put(tetraPositionData).position(0)

        mTetraClrs = ByteBuffer.allocateDirect(tetraColorData.size * mFloatSize)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mTetraClrs!!.put(tetraColorData).position(0)

        mTetraNrmls = ByteBuffer.allocateDirect(tetraNormalData.size * mFloatSize)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mTetraNrmls!!.put(tetraNormalData).position(0)
    }


    // init scene
    override fun onSurfaceCreated(glOldVers: GL10, glCnfg: EGLConfig)
    {
        // OGL global settings
        GLES20.glClearColor(0.85f, 0.85f, 0.85f, 0.0f)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // camera settings
        val eX = 0.0f
        val eY = 0.0f
        val eZ = -0.5f
        var lkX = 0.0f
        val lkY = 0.0f
        val lkZ = -5.0f
        val upOX = 0.0f
        val upOY = 1.0f
        val upOZ = 0.0f

        Matrix.setLookAtM(mViewMtrx, 0, eX, eY, eZ, lkX, lkY, lkZ, upOX, upOY, upOZ)

        val vertexShdr : String? = ShaderSource.getTextFromResourceFile(mActivityContext,
                R.raw.body_vertex_shader)
        val fragmentShdr : String? = ShaderSource.getTextFromResourceFile(mActivityContext,
                R.raw.body_fragment_shader)

        if (vertexShdr==null || fragmentShdr==null)
        {
            System.exit(0);
        }

        val vertexShdrHndl = compileShader(GLES20.GL_VERTEX_SHADER, vertexShdr)
        val fragmentShdrHndl = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShdr)

        mHexahedronVertexPrgrmHndl = createAndLinkProgram(vertexShdrHndl, fragmentShdrHndl,
                arrayOf("a_Position", "a_Color", "a_Normal", "a_TexCoordinate"))

        mTetraVertexPrgrmHndl = createAndLinkProgram(vertexShdrHndl, fragmentShdrHndl,
                arrayOf("a_Position", "a_Color", "a_Normal"))

        // Definition of shader programs for  the source of light
        val pointVrtxShdr = ShaderSource.getTextFromResourceFile(mActivityContext,
                R.raw.lignt_point_vertex_shader_code)
        val pointFragmentShader = ShaderSource.getTextFromResourceFile(mActivityContext,
                R.raw.lignt_point_fragment_shader_code)

        if (pointVrtxShdr==null || pointFragmentShader==null)
        {
            System.exit(0);
        }

        val pointVrtxShdrHndl = compileShader(GLES20.GL_VERTEX_SHADER, pointVrtxShdr)
        val pointFrgmntShdrHndl = compileShader(GLES20.GL_FRAGMENT_SHADER, pointFragmentShader)
        mLightPointPrgrmHndl = createAndLinkProgram(pointVrtxShdrHndl, pointFrgmntShdrHndl,
                arrayOf("a_Position"))

        for (i in 0..13) // init of textures array
            mTextureDataHndls[i] = ShaderSource.readTextureFromResource(mActivityContext, mTextures[i])
    }

    // change the area of view
    override fun onSurfaceChanged(glOldVers: GL10, width: Int, height: Int)
    {
        // Setting of the OpenGL viewport equal to the size of the screen
        GLES20.glViewport(0, 0, width, height)

        // Create a new perspective projection matrix
        // ratio is calculated to take in account real proportions of the screen
        val ratioSC = width.toFloat() / height
        val leftSC = -ratioSC
        val bottomSC = -1.0f
        val topSC = 1.0f
        val nearSC = 1.0f
        val farSC = 14.0f

        // cutting volume
        Matrix.frustumM(mProjMtrx, 0, leftSC, ratioSC, bottomSC, topSC, nearSC, farSC)
    }

    override fun onDrawFrame(glOldVers: GL10)
    {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // calculate rotations for automatic mode (animation here!)
        if (!mHandleMode) {
            val time = SystemClock.uptimeMillis() % 10000L
            mAngleBodies = 360.0f / 10000.0f * time.toInt()
            //val time2 = SystemClock.uptimeMillis() % 4000L
            mAngleCenter = 0.09f * time.toInt()
        }

        setPolyhedronsHandles()
        setLightHandles()
        
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

        //---------- global rotation ------------------------------
        Matrix.setIdentityM(mViewMtrx, 0)
        Matrix.translateM(mViewMtrx, 0, 0.0f, 0.0f, ZObjectsPos.getPosition(0).toFloat())
        if (!mHadleRotation)
        // rotation around X-axis
            Matrix.rotateM(mViewMtrx, 0, mGlobalAngle * 1.0f, 0.0f, 1.0f, 0.0f)
        else
        // rotation around Y-axis
            Matrix.rotateM(mViewMtrx, 0, mGlobalAngle * 1.0f, 1.0f, 0.0f, 0.0f)
        Matrix.translateM(mViewMtrx, 0, 0.0f, 0.0f, -ZObjectsPos.getPosition(0).toFloat())
        //==========================================================

        // Draw polyhedrons
        putRightBody()
        putLeftBody()
        putTopBody()
        putBottomBody()
        putCentralBody()
        drawLamp()

    }

    private fun setPolyhedronsHandles()
    {
        // Set program handles for polyhedrons
        GLES20.glUseProgram(mHexahedronVertexPrgrmHndl)

        mMVPMtrxHndl = GLES20.glGetUniformLocation(mHexahedronVertexPrgrmHndl, "u_MVPMatrix")
        mMVMtrxHndl = GLES20.glGetUniformLocation(mHexahedronVertexPrgrmHndl, "u_MVMatrix")
        mLightPsHndl = GLES20.glGetUniformLocation(mHexahedronVertexPrgrmHndl, "u_LightPos")
        mTextureUniformHndl = GLES20.glGetUniformLocation(mHexahedronPstnHndl, "u_Texture")
        mHexahedronPstnHndl = GLES20.glGetAttribLocation(mHexahedronVertexPrgrmHndl, "a_Position")
        mHexahedronClrHndl = GLES20.glGetAttribLocation(mHexahedronVertexPrgrmHndl, "a_Color")
        mHexahedronNrmlHndl = GLES20.glGetAttribLocation(mHexahedronVertexPrgrmHndl, "a_Normal")
        mTextureCoordHndl = GLES20.glGetAttribLocation(mHexahedronVertexPrgrmHndl, "a_TexCoordinate")

    }

    private fun setLightHandles()
    {
        // put vertex-lighting program
        GLES20.glUseProgram(mTetraVertexPrgrmHndl)

        // Set program handles (tetrahedron)
        mMVPMtrxHndl = GLES20.glGetUniformLocation(mTetraVertexPrgrmHndl, "u_MVPMatrix")
        mMVMtrxHndl = GLES20.glGetUniformLocation(mTetraVertexPrgrmHndl, "u_MVMatrix")
        mLightPsHndl = GLES20.glGetUniformLocation(mTetraVertexPrgrmHndl, "u_LightPos")
        mTetraPstnHndl = GLES20.glGetAttribLocation(mTetraVertexPrgrmHndl, "a_Position")
        mTetraClrHndl = GLES20.glGetAttribLocation(mTetraVertexPrgrmHndl, "a_Color")
        mTetraNrmlHndl = GLES20.glGetAttribLocation(mTetraVertexPrgrmHndl, "a_Normal")

    }

    private fun putRightBody() {
        // right
        // binding of the texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHndls[0])
        GLES20.glUniform1i(mTextureUniformHndl, 0)

        Matrix.setIdentityM(mModelMtrx, 0)
        Matrix.translateM(mModelMtrx, 0, 3.0f, 0.0f, ZObjectsPos.getPosition(2).toFloat())
        Matrix.rotateM(mModelMtrx, 0, mAngleBodies * 1.0f, mRotationVector!![0], mRotationVector!![1], mRotationVector!![2] /*0.0f, 1.0f, 1.0f*/)
        drawHexahedron()
    }

    private fun putLeftBody()
    {
        // left
        // binding of the texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHndls[1] /*mTextureDataHandle2*/)
        GLES20.glUniform1i(mTextureUniformHndl, 0)

        Matrix.setIdentityM(mModelMtrx, 0)
        Matrix.translateM(mModelMtrx, 0, -3.0f, 0.0f, ZObjectsPos.getPosition(1).toFloat())
        Matrix.rotateM(mModelMtrx, 0, mAngleBodies * 2f, mRotationVector!![0], mRotationVector!![1], mRotationVector!![2] /*0.0f, 1.0f, 1.0f*/)
        drawHexahedron()
    }

    private fun putTopBody()
    {
        // top
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHndls[2] /*mTextureDataHandle3*/)
        GLES20.glUniform1i(mTextureUniformHndl, 0)

        Matrix.setIdentityM(mModelMtrx, 0)
        Matrix.translateM(mModelMtrx, 0, 0.0f, 3.0f, ZObjectsPos.getPosition(3).toFloat())
        Matrix.rotateM(mModelMtrx, 0, mAngleBodies * 2f, mRotationVector!![0], mRotationVector!![1], mRotationVector!![2] /*0.0f, 0.0f, 1.0f*/)
        drawTetrahedron()
    }

    private fun putBottomBody()
    {
        // bottom
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHndls[3])
        GLES20.glUniform1i(mTextureUniformHndl, 0)

        Matrix.setIdentityM(mModelMtrx, 0)
        Matrix.translateM(mModelMtrx, 0, 0.0f, -3.0f, ZObjectsPos.getPosition(4).toFloat())
        Matrix.rotateM(mModelMtrx, 0, mAngleBodies * 3f, mRotationVector!![0], mRotationVector!![1], mRotationVector!![2] /*1.0f, 1.5f, 1.0f*/)
        drawTetrahedron()
    }

    private fun putCentralBody()
    {
        // central
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHndls[4])
        GLES20.glUniform1i(mTextureUniformHndl, 0)

        val tmp: FloatArray
        Matrix.setIdentityM(mModelMtrx, 0)
        Matrix.translateM(mModelMtrx, 0, 0.0f, 0.0f, ZObjectsPos.getPosition(0).toFloat())
        Matrix.rotateM(mModelMtrx, 0, 90 + mAngleCenter, 1.0f, 0.0f, 0.0f)
        drawTetrahedron()

        tmp = mModelMtrx
        Matrix.scaleM(mModelMtrx, 0, 0.7f, 0.7f, 0.7f)
        Matrix.rotateM(mModelMtrx, 0, mAngleCenter, 1.0f, 0.0f, 0.0f)
        mModelMtrx = tmp
        drawHexahedron()
    }


    // Draws a hexahedron
    private fun drawHexahedron()
    {
        // send position data to shader
        mHexahedronPos!!.position(0)
        GLES20.glVertexAttribPointer(mHexahedronPstnHndl, mPosDataSize, GLES20.GL_FLOAT, false,
                0, mHexahedronPos)

        GLES20.glEnableVertexAttribArray(mHexahedronPstnHndl)

        // send color data to shader
        mHexahedronClrs!!.position(0)
        GLES20.glVertexAttribPointer(mHexahedronClrHndl, mClrDataSize, GLES20.GL_FLOAT, false,
                0, mHexahedronClrs)

        GLES20.glEnableVertexAttribArray(mHexahedronClrHndl)

        // send normal vectors data to sender
        mHexahedronNrmls!!.position(0)
        GLES20.glVertexAttribPointer(mHexahedronNrmlHndl, mNrmlDataSize, GLES20.GL_FLOAT, false,
                0, mHexahedronNrmls)

        GLES20.glEnableVertexAttribArray(mHexahedronNrmlHndl)

        // textures -----------------------------
        mHexahedronTextureCoords!!.position(0)
        GLES20.glVertexAttribPointer(mTextureCoordHndl,
                mTextureCoordinateDtSz, GLES20.GL_FLOAT, false,
                0, mHexahedronTextureCoords)

        GLES20.glEnableVertexAttribArray(mTextureCoordHndl)
        Matrix.multiplyMM(mMVPMtrx, 0, mViewMtrx, 0, mModelMtrx, 0)

        GLES20.glUniformMatrix4fv(mMVMtrxHndl, 1, false, mMVPMtrx, 0)
        Matrix.multiplyMM(mMVPMtrx, 0, mProjMtrx, 0, mMVPMtrx, 0)

        // set data in final mtrx
        GLES20.glUniformMatrix4fv(mMVPMtrxHndl, 1, false, mMVPMtrx, 0)


        GLES20.glUniform3f(mLightPsHndl, mLightPosInLookAtCoords[0],
                mLightPosInLookAtCoords[1], mLightPosInLookAtCoords[2])

        // Draw regular hexahedron
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)
    }

    // Draws the tetrahedron
    private fun drawTetrahedron()
    {
        // set the position data
        mTetraPosns!!.position(0)
        GLES20.glVertexAttribPointer(mTetraPstnHndl, mPosDataSize, GLES20.GL_FLOAT, false,
                0, mTetraPosns)

        GLES20.glEnableVertexAttribArray(mTetraPstnHndl)

        // send the color data
        mTetraClrs!!.position(0)
        GLES20.glVertexAttribPointer(mTetraClrHndl, mClrDataSize, GLES20.GL_FLOAT, false,
                0, mTetraClrs)

        GLES20.glEnableVertexAttribArray(mTetraClrHndl)

        // set the normal vectors data
        mTetraNrmls!!.position(0)
        GLES20.glVertexAttribPointer(mTetraNrmlHndl, mNrmlDataSize, GLES20.GL_FLOAT, false,
                0, mTetraNrmls)

        GLES20.glEnableVertexAttribArray(mTetraNrmlHndl)
        Matrix.multiplyMM(mMVPMtrx, 0, mViewMtrx, 0, mModelMtrx, 0)

        // modelview 
        GLES20.glUniformMatrix4fv(mMVMtrxHndl, 1, false, mMVPMtrx, 0)
        Matrix.multiplyMM(mMVPMtrx, 0, mProjMtrx, 0, mMVPMtrx, 0)

        // final matrix
        GLES20.glUniformMatrix4fv(mMVPMtrxHndl, 1, false, mMVPMtrx, 0)

        // position of the source of the light  in viewer coordinate system
        GLES20.glUniform3f(mLightPsHndl, mLightPosInLookAtCoords[0],
                mLightPosInLookAtCoords[1], mLightPosInLookAtCoords[2])

        // Draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 12)
    }


    // Draws point of light
    private fun drawLamp()
    {
        // Draw a point to show the "lamp"

        Matrix.setIdentityM(mLightMdlMtrx, 0)
        Matrix.translateM(mLightMdlMtrx, 0, 0.0f, 0.0f, -5.0f)
        Matrix.rotateM(mLightMdlMtrx, 0, mAngleBodies * 3f, 0.0f, 1.0f, 0.0f)
        Matrix.translateM(mLightMdlMtrx, 0, 0.0f, 0.0f, 2.0f)
        Matrix.multiplyMV(mLightPosInWorldCoords, 0, mLightMdlMtrx, 0, mLightPosInModelCoords, 0)
        Matrix.multiplyMV(mLightPosInLookAtCoords, 0, mViewMtrx, 0, mLightPosInWorldCoords, 0)

        GLES20.glUseProgram(mLightPointPrgrmHndl)

        val pointMVPMtrxHndl = GLES20.glGetUniformLocation(mLightPointPrgrmHndl, "u_MVPMatrix")
        val pointPstnHndl = GLES20.glGetAttribLocation(mLightPointPrgrmHndl, "a_Position")
        
        GLES20.glVertexAttrib3f(pointPstnHndl, mLightPosInModelCoords[0], mLightPosInModelCoords[1], mLightPosInModelCoords[2])
        GLES20.glDisableVertexAttribArray(pointPstnHndl)
        
        Matrix.multiplyMM(mMVPMtrx, 0, mViewMtrx, 0, mLightMdlMtrx, 0)
        Matrix.multiplyMM(mMVPMtrx, 0, mProjMtrx, 0, mMVPMtrx, 0)
        GLES20.glUniformMatrix4fv(pointMVPMtrxHndl, 1, false, mMVPMtrx, 0)

        // set the light
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1)
    }


     // method compiles of the shader
    private fun compileShader(typeOfShader: Int, sourceOfShader: String?): Int
    {
        var shaderHndl = GLES20.glCreateShader(typeOfShader)

        if (shaderHndl != 0)
        {
            // set source of shader
            GLES20.glShaderSource(shaderHndl, sourceOfShader)

            // compilation of the shader
            GLES20.glCompileShader(shaderHndl)

            // get the result of compilation
            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(shaderHndl, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

            // If the compilation was not success, remove the shaderHndl
            if (compileStatus[0] == 0)
            {
                GLES20.glDeleteShader(shaderHndl)
                shaderHndl = 0
            }
        }

        if (shaderHndl == 0)
        {
            throw RuntimeException("Impossible to compile shader program.")
        }

        return shaderHndl
    }

    // compile and link the program
    private fun createAndLinkProgram(vertexShdrHndl: Int, fragmentShdrHndl: Int,
                                     attribs: Array<String>?): Int
    {
        var programHndl = GLES20.glCreateProgram()

        if (programHndl != 0)
        {
            // binding of the vertex shader to the shader program
            GLES20.glAttachShader(programHndl, vertexShdrHndl)

            // binding the fragment shader to the shader program
            GLES20.glAttachShader(programHndl, fragmentShdrHndl)

            // binding of attribs
            if (attribs != null)
            {
                val size = attribs.size
                for (i in 0 until size)
                {
                    GLES20.glBindAttribLocation(programHndl, i, attribs[i])
                }
            }

            // linking of shaders
            GLES20.glLinkProgram(programHndl)

            // getting the linkage result
            val linkResult = IntArray(1)
            GLES20.glGetProgramiv(programHndl, GLES20.GL_LINK_STATUS, linkResult, 0)

            // if fail
            if (linkResult[0] == 0)
            {
                GLES20.glDeleteProgram(programHndl)
                programHndl = 0
            }
        }

        if (programHndl == 0)
        {
            return 0
            throw RuntimeException("Impossible to create shader program.")
        }

        return programHndl
    }

    var Angle : Float
        get() = mGlobalAngle
        set(newvalue)
        {
            mGlobalAngle = newvalue
        }


    fun setRotationDirection()
    {
        when (TiltData.getDirection())
        {
            TiltDirections.UP ->
            {
                mRotationVector = floatArrayOf(-1.0f, 0.0f, 0.0f)
                mAngleBodies = Math.abs(mAngleBodies)
            }
            TiltDirections.DOWN ->
            {
                mRotationVector = floatArrayOf(1.0f, 0.0f, 0.0f)
                mAngleBodies = -Math.abs(mAngleBodies)
            }
            TiltDirections.LEFT ->
            {
                mRotationVector = floatArrayOf(0.0f, -1.0f, 0.0f)
                mAngleBodies = Math.abs(mAngleBodies)
            }
            TiltDirections.RIGHT ->
            {
                mRotationVector = floatArrayOf(0.0f, 1.0f, 0.0f)
                mAngleBodies = -Math.abs(mAngleBodies)
            }
            TiltDirections.UNKNOWN ->
            {
                return
                //mRotationVector = new float[] {0.0f, 0.0f, 1.0f};
            }
        }
    }

    fun setRotationDirection(voiceCommand : String)
    {
        //Log.d("spok",voiceCommand)

        var leftWords = arrayListOf<String>("left", "lift")
        if (leftWords.contains(voiceCommand))
        {
            mRotationVector = floatArrayOf(0.0f, -1.0f, 0.0f)
            mAngleBodies = Math.abs(mAngleBodies)
            return
        }

        if (voiceCommand.contains("right"))
        {
            mRotationVector = floatArrayOf(0.0f, 1.0f, 0.0f)
            mAngleBodies = -Math.abs(mAngleBodies)
            return
        }

        var upWords = arrayListOf<String>("op", "up", "top", "tab")
        if (upWords.contains(voiceCommand))
        {
            mRotationVector = floatArrayOf(-1.0f, 0.0f, 0.0f)
            mAngleBodies = Math.abs(mAngleBodies)
            return
        }

        var downWords = arrayListOf<String>("dull", "dell", "dong", "dog", "Darwin", "dumb", "gold","done", "dome", "dom", "down","don't","toll", "doll")
        if (downWords.contains(voiceCommand))
        {
            mRotationVector = floatArrayOf(1.0f, 0.0f, 0.0f)
            mAngleBodies = -Math.abs(mAngleBodies)
            return
        }
    }

    fun swapTextures()
    {
        val tmp = mTextureDataHndls[mTextureDataHndls.size - 1]
        for (i in mTextureDataHndls.size - 1 downTo 1)
        {
            mTextureDataHndls[i] = mTextureDataHndls[i - 1]
        }
        mTextureDataHndls[0] = tmp
    }

    var HadleRotation : Boolean
        get() = mHadleRotation
        set(newvalue)
        {
            mHadleRotation = newvalue
        }


    var HandleMode : Boolean
        get() = mHandleMode
        set(newvalue)
        {
            mHandleMode = newvalue
        }

}
