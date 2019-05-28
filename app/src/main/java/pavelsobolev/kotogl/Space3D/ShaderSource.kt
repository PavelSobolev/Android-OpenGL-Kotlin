package pavelsobolev.kotogl.Space3D

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * helper class of static methods for generating string
 * sources of OpenGL pipeline's programs
 */
object ShaderSource
{

    fun getTextFromResourceFile(appContext: Context, appResourceId: Int): String?
    {
        val resourceReader = BufferedReader(
                InputStreamReader(appContext.resources.openRawResource(appResourceId)))

        var llineFromFile : String = ""
        var shaderText = StringBuilder()

        try
        {
            llineFromFile = resourceReader.readLine()
            while (llineFromFile!= null)
            {
                shaderText.append(llineFromFile)
                shaderText.append('\n')
                try {
                    llineFromFile = resourceReader.readLine()
                }
                catch (ee:Exception)
                {
                    break
                }
            }
        }
        catch (e: IOException)
        {
            return null
        }

        return shaderText.toString()
    }

    fun readTextureFromResource(appContext: Context, puctId: Int): Int
    {
        val handleOfTexture = IntArray(1)

        GLES20.glGenTextures(1, handleOfTexture, 0)

        if (handleOfTexture[0] != 0)
        {
            val bitmapOptions = BitmapFactory.Options()
            bitmapOptions.inScaled = false   // No pre-scaling

            // retrieving binary data of resource
            val bitmap = BitmapFactory.decodeResource(appContext.resources,
                    puctId, bitmapOptions)

            // texture binding
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, handleOfTexture[0])

            // setting of color and coordinates approximation of texture
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)

            // bitmap loading
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

            // remove binary data from system memory
            bitmap.recycle()
        }

        if (handleOfTexture[0] == 0) {
            throw RuntimeException("Impossible to load texture.")
        }

        return handleOfTexture[0]
    }
}