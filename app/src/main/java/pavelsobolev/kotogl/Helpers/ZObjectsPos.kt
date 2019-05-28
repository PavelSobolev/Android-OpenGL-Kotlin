package pavelsobolev.kotogl.Helpers

// distances from point of view to objects of 3D scene
object ZObjectsPos
{
    /**
     * 0=>central polyhedron z-position, 1=>left, 2=>right, 3=>top, 4=>bottom
     */
    private var zPositions: IntArray

    init
    {
        zPositions = IntArray(5)
        zPositions[0] = -6
        zPositions[1] = -7
        zPositions[2] = -6
        zPositions[3] = -7
        zPositions[4] = -7
    }

    fun setPosition(pos: Int, newVal: Int)
    {
        zPositions[pos] = newVal
    }

    fun getPosition(pos: Int): Int
    {
        return zPositions[pos]
    }
}
