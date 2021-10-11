package me.sttimort.aot3plsa

import smile.math.matrix.Matrix

fun Matrix.mapIndexed(mapper: (rowIndex: Int, columnIndex: Int, value: Double) -> Double): Matrix {
    for (m in 0 until nrows()) for (n in 0 until ncols()) this[m, n] = mapper(m, n, this[m, n])
    return this
}

fun matrix(rows: Int, columns: Int, initializer: Matrix.(row: Int, column: Int) -> Double): Matrix =
    Matrix(rows, columns).run { mapIndexed { row, column, _ -> initializer(row, column) } }

fun Matrix.multiply(m2: Matrix): Matrix = this.let { m1 ->
    matrix(nrows(), m2.ncols()) { row, column ->
        val m2ColumnValues = m2.col(column)
        m1.row(row).mapIndexed { i, m1RowValue -> m1RowValue * m2ColumnValues[i] }.sum()
    }
}