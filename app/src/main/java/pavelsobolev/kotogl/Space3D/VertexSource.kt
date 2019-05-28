package pavelsobolev.kotogl.Space3D

object VertexSource
{
    // constants for colours and coordinates
    private val one : Float = 1.0f
    private val zero : Float = 0.0f
    private val z8 : Float = 0.7f
    private val z7 : Float = 0.8f

    // X, Y, Z of hexahedron vertices
    private val hexahedronCoordinates = floatArrayOf(
            // Front
            -one, one, one,
            -one, -one, one,
            one, one, one,
            -one, -one, one,
            one, -one, one,
            one, one, one,

            // Right
            one, one, one,
            one, -one, one,
            one, one, -one,
            one, -one, one,
            one, -one, -one,
            one, one, -one,

            // Back
            one, one, -one,
            one, -one, -one,
            -one, one, -one,
            one, -one, -one,
            -one, -one, -one,
            -one, one, -one,

            // Left
            -one, one, -one,
            -one, -one, -one,
            -one, one, one,
            -one, -one, -one,
            -one, -one, one,
            -one, one, one,

            // Top
            -one, one, -one,
            -one, one, one,
            one, one, -one,
            -one, one, one,
            one, one, one,
            one, one, -one,

            // Bottom
            one, -one, -one,
            one, -one, one,
            -one, -one, -one,
            one, -one, one,
            -one, -one, one,
            -one, -one, -one)

    // R, G, B, A of hexahedron vertices
    private val hexahedronColors = floatArrayOf(
            // Front color
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one,

            // Right color
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one,

            // Back color
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one,

            // Left color
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one,

            // Top color
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one,

            // Bottom color
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one,
            z8, z8, z8, one)

    // X, Y, Z
    private val hexahedronNormalVectors = floatArrayOf(
            // Front face
            zero, zero, one,
            zero, zero, one,
            zero, zero, one,
            zero, zero, one,
            zero, zero, one,
            zero, zero, one,

            // Right face
            one, zero, zero,
            one, zero, zero,
            one, zero, zero,
            one, zero, zero,
            one, zero, zero,
            one, zero, zero,

            // Back face
            zero, zero, -one,
            zero, zero, -one,
            zero, zero, -one,
            zero, zero, -one,
            zero, zero, -one,
            zero, zero, -one,

            // Left face
            -one, zero, zero,
            -one, zero, zero,
            -one, zero, zero,
            -one, zero, zero,
            -one, zero, zero,
            -one, zero, zero,

            // Top face
            zero, one, zero,
            zero, one, zero,
            zero, one, zero,
            zero, one, zero,
            zero, one, zero,
            zero, one, zero,

            // Bottom face
            zero, -one, zero,
            zero, -one, zero,
            zero, -one, zero,
            zero, -one, zero,
            zero, -one, zero,
            zero, -one, zero)

    private val hexahedronTextureCoordinateData = floatArrayOf(
            // Front face
            zero, zero, zero,
            one, one, zero,
            zero, one, one,
            one, one, zero,

            // Right face
            zero, zero, zero,
            one, one, zero,
            zero, one, one,
            one, one, zero,

            // Back face
            zero, zero, zero,
            one, one, zero,
            zero, one, one,
            one, one, zero,

            // Left face
            zero, zero, zero,
            one, one, zero,
            zero, one, one,
            one, one, zero,

            // Top face
            zero, zero, zero,
            one, one, zero,
            zero, one, one,
            one, one, zero,

            // Bottom face
            zero, zero, zero,
            one, one, zero,
            zero, one, one,
            one, one, zero)

    // tetrahedron data

    private val tetrahedronCoordinates = floatArrayOf(
            -one, one, one,
            -one, -one, -one,
            one, -one, one,

            one, -one, one,
            -one, -one, -one,
            one, one, -one,

            one, one, -one,
            -one, -one, -one,
            -one, one, one,

            -one, one, one,
            one, -one, one,
            one, one, -one)

    private val tetrahedronColors = floatArrayOf(
            z7, z7, z7, one,
            z7, z7, z7, one,
            z7, z7, z7, one,

            z7, z7, z7, one,
            z7, z7, z7, one,
            z7, z7, z7, one,

            z7, z7, z7, one,
            z7, z7, z7, one,
            z7, z7, z7, one,

            z7, z7, z7, one,
            z7, z7, z7, one,
            z7, z7, z7, one)

    private val tetrahedronNormalVectors = floatArrayOf(
            -one, -one, one,
            -one, -one, one,
            -one, -one, one,

            one, -one, -one,
            one, -one, -one,
            one, -one, -one,

            -one, one, -one,
            -one, one, -one,
            -one, one, -one,

            one, one, one,
            one, one, one,
            one, one, one)


    // --- public getters

    val HexahedronCoordinates : FloatArray
        get() = this.hexahedronCoordinates

    val HexahedronColors: FloatArray
        get() = hexahedronColors

    val HexahedronNormalVectors: FloatArray
        get() = hexahedronNormalVectors

    val TetrahedronCoordinates: FloatArray
        get() = tetrahedronCoordinates

    val TetrahedronColors: FloatArray
        get() = tetrahedronColors

    val TetrahedronNormalVectors: FloatArray
        get() = tetrahedronNormalVectors

    val HexahedronTextureCoordinateData: FloatArray
        get() = hexahedronTextureCoordinateData
}