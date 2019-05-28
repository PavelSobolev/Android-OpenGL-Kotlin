package pavelsobolev.kotogl.Helpers

import java.util.*


// the class calculates angle of tilt of a device (on the base of data from its accelerator)
object TiltData : Observable()
{
    private var xTilt : Float = 0.0f
    private var yTilt : Float = 0.0f
    private var tiltScale : Float = 1.5f

    init
    {
        setData(0.0f,0.0f)
        tiltScale = 1.5f
    }

    // sets new tilt data for current object
    fun setData(x:Float, y:Float)
    {
        if (Math.abs(x)>10 || Math.abs(y)>10) return;

        xTilt = x
        yTilt = y
        setChanged()
        notifyObservers()
    }

    fun getDirection(): TiltDirections
    {
        return getDirection(xTilt, yTilt)
    }

    fun getDirection(tiltX:Float, tiltY:Float) : TiltDirections
    {
        if (Math.abs(Math.abs(tiltX)-Math.abs(tiltY)) <= 3.0)
        {
            return TiltDirections.UNKNOWN
        }

        if (Math.abs(tiltX) > Math.abs(tiltY))
        {
            if (tiltX < 0) return TiltDirections.RIGHT
            if (tiltX > 0) return TiltDirections.LEFT
        }
        else
        {
            if (tiltY < 0) return TiltDirections.UP
            if (tiltY > 0) return TiltDirections.DOWN
        }

        val threshold = 1.0f
        if (tiltX > -threshold && tiltX < threshold && tiltY > -threshold && tiltY < threshold)
        {
            return TiltDirections.UNKNOWN
        }

        return TiltDirections.UNKNOWN
    }
}