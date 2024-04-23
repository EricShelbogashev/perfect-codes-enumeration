/**
 * Канонический преобразователь матриц.
 */
object MatrixConverter {
    /**
     * Преобразует матрицу по теореме о каноническом преобразовании.
     *
     * @param matrix Матрица для преобразования. Не должна быть квадратной и вырожденной.
     * @return Проверочная, если исходная матрица - порождающая.<p>Порождающая, если исходная матрица - проверочная.
     */
    fun canonical(matrix: Matrix): Matrix {
        val (rrefMatrix, pivotColumns) = matrix.toRowEchelonForm(2)
        val rearrangedMatrix = Matrix(matrix.rows, matrix.cols)
        val columnOrder = pivotColumns + (0 until matrix.cols).filterNot { pivotColumns.contains(it) }
        columnOrder.forEachIndexed { index, colIndex ->
            if (index < matrix.rows) {
                // Перестановка столбцов для ведущих столбцов
                for (row in 0 until matrix.rows) {
                    rearrangedMatrix[row, index] = rrefMatrix[row, colIndex]
                }
            } else {
                // Заполнение оставшихся столбцов
                for (row in 0 until matrix.rows) {
                    rearrangedMatrix[row, index] = rrefMatrix[row, colIndex]
                }
            }
        }

        // Транспонирование подматрицы A и объединение с единичной матрицей
        val aMatrix = rearrangedMatrix.subMatrix(0, matrix.rows, matrix.rows, matrix.cols)
        val aTransposed = aMatrix.transpose()

        val eyeMatrix = eye(aTransposed.rows)
        val generativeMatrix = hstack(aTransposed, eyeMatrix)

        // Восстановление исходной матрицы G
        val restoredG = Matrix(generativeMatrix.rows, generativeMatrix.cols)
        pivotColumns.forEachIndexed { index, pivot ->
            for (row in 0 until generativeMatrix.rows) {
                restoredG[row, pivot] = generativeMatrix[row, index]
            }
        }
        (0 until generativeMatrix.cols).filterNot { pivotColumns.contains(it) }.forEachIndexed { index, col ->
            for (row in 0 until generativeMatrix.rows) {
                restoredG[row, col] = generativeMatrix[row, index + pivotColumns.size]
            }
        }
        val restoredGModule = restoredG % 2
        return restoredGModule
    }
}

/**
 * Функция оператора, выполняющая поэлементное вычисления остатка при делении на скаляр.
 *
 * @receiver Матрица, над которой производится операция.
 * @param constant Константа, на которую производится деление.
 * @return Результат поэлементного деления матрицы на константу.
 */
operator fun Matrix.rem(constant: Int): Matrix {
    val result = Matrix(rows, cols)
    for (i in 0 until rows) {
        for (j in 0 until cols) {
            result[i, j] = this[i, j] % constant
        }
    }
    return result
}

/**
 * Функция для получения подматрицы из исходной матрицы.
 *
 * @receiver Исходная матрица.
 * @param startRow Начальная строка подматрицы (включительно).
 * @param endRow Конечная строка подматрицы (исключительно).
 * @param startCol Начальный столбец подматрицы (включительно).
 * @param endCol Конечный столбец подматрицы (исключительно).
 * @return Подматрица исходной матрицы.
 */
fun Matrix.subMatrix(startRow: Int, endRow: Int, startCol: Int, endCol: Int): Matrix {
    val subRows = endRow - startRow
    val subCols = endCol - startCol
    val result = Matrix(subRows, subCols)
    for (i in 0 until subRows) {
        for (j in 0 until subCols) {
            result[i, j] = this[startRow + i, startCol + j]
        }
    }
    return result
}

/**
 * Функция для транспонирования матрицы.
 *
 * @receiver Исходная матрица.
 * @return Транспонированная матрица.
 */
fun Matrix.transpose(): Matrix {
    val result = Matrix(cols, rows)
    for (i in 0 until rows) {
        for (j in 0 until cols) {
            result[j, i] = this[i, j]
        }
    }
    return result
}