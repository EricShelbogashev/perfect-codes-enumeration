fun Matrix.rank(): Int {
    val ref = this.toRowEchelonForm(2).first
    var rank = 0
    for (i in 0 until ref.rows) {
        for (j in 0 until ref.cols) {
            if (ref[i, j] != 0) {
                rank++
                break
            }
        }
    }
    return rank
}

fun Code.asMatrix(): Matrix {
    val parser = MatrixParser()
    return parser.createMatrixFromString(
        words.map { word -> word.stringBits }.map { word -> word.map { symbol -> symbol - '0' } }.toString()
    )
}

private fun Code.echelonForm(): Matrix {
    return asMatrix().toRowEchelonForm(2).first
}

fun Code.echelonBasis(): Matrix {
    val form = echelonForm()
    val matrix = Matrix(form.rank(), form.cols)
    for (row in 0 until form.rank()) {
        for (col in 0 until form.cols) {
            matrix[row, col] = form[row, col]
        }
    }
    return matrix
}

fun eye(size: Int): Matrix {
    val result = Matrix(size, size)
    for (i in 0 until size) {
        result[i, i] = 1
    }
    return result
}

fun hstack(a: Matrix, b: Matrix): Matrix {
    if (a.rows != b.rows) throw IllegalArgumentException("Matrices don't have the same number of rows.")
    val result = Matrix(a.rows, a.cols + b.cols)
    for (i in 0 until a.rows) {
        for (j in 0 until a.cols) {
            result[i, j] = a[i, j]
        }
        for (j in 0 until b.cols) {
            result[i, j + a.cols] = b[i, j]
        }
    }
    return result
}

fun Matrix.isHammingH(): Boolean {
    val transposed = transpose()
    val nums = mutableSetOf<Int>()
    for (row in 0 until transposed.rows) {
        val num = mutableListOf<Int>()
        for (col in 0 until transposed.cols) {
            num.add(transposed[row, col])
        }
        nums.add(
            num.joinToString(separator = "").toInt(2)
        )
    }
    return nums.containsAll(listOf(1, 2, 3, 4, 5, 6, 7))
}