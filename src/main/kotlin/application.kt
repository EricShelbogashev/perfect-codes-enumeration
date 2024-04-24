import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.SingleGraph
import java.util.*

/* Если взять не ноль, получим базисные матрицы ранга 5, где одна из строк будет являться смещением. */
fun main() {
    /* Задаем начальный вектор. */
    val center = Codeword(0)
    val initCode = Code(setOf(center))

    /* Находим сетчатую окрестность с расстоянием хотя бы 3 между словами для начального слова. */
    val codes8words = initCode.nearestMesh(distance = 3, partitions = 8) // [depth] - количество итераций поиска.
    val codes16words = mutableListOf<Code>()
    for (code8 in codes8words) {
        /* Находим сетчатую окрестность с расстоянием хотя бы 3 между словами для 8ми-словного кода. */
        val codes15words = code8.nearestMesh(distance = 3, partitions = 8)
        for (code15 in codes15words) {
            /* Находим сетчатую окрестность с расстоянием хотя бы 3 между словами для 15ти-словного кода. */
            val fullCodes = code15.nearestMesh(distance = 3, partitions = 1)
            codes16words.addAll(fullCodes)
        }
    }

    /* Применяем Гаусса к матрице из кодовых слов, вытаскиваем подматрицу по рангу. */
    val bases = codes16words.map(Code::echelonBasis)
    /* Преобразуем базис - порождающую матрицу к проверочной. */
    val parityChecks = bases.map(MatrixConverter::canonical)
    /* Проверяем, что проверочные матрицы всех кодов являются матрицами кода Хэмминга. */
    val allHammingH = parityChecks.all(Matrix::isHammingH)

    codes16words.map(Code::toString).forEach(::println)
    println(allHammingH)
    println(codes16words.weightConfigurations(distinct = true))
}

// OLD ====================================================================================================

private fun drawGraph(codes16words: MutableList<Code>) {
    for (code in codes16words) {
        val graph: Graph = SingleGraph(code.words.first().stringBits)
        System.setProperty("org.graphstream.ui", "swing")

        val styleSheet = """
        node {
            fill-color: gray;
        }
        node.marked1 {
            fill-color: red;
        }
    """.trimIndent()

        graph.setAttribute("ui.stylesheet", styleSheet)
        fun addNode(codeword: Codeword) {
            val node = graph.addNode(codeword.stringBits)
            node.setAttribute("ui.label", codeword.stringBits)
            if (codes16words.first().contains(codeword)) {
                node.setAttribute("ui.class", "marked1")
            }
        }

        fun addEdge(codeword1: Codeword, codeword2: Codeword) {
            if (codeword1.distance(codeword2) == 1) {
                try {
                    graph.addEdge(UUID.randomUUID().toString(), codeword1.stringBits, codeword2.stringBits)
                } catch (e: Exception) {
                    println("$codeword1, $codeword2")
                }
            }
        }
        for (i in 0 until 127) {
            val codeword1 = Codeword(i)
            addNode(codeword1)
        }
        for (i in 0 until 127) {
            val codeword1 = Codeword(i)
            for (j in i until 127) {
                val codeword2 = Codeword(j)
                addEdge(codeword1, codeword2)
            }
        }
        graph.display()
    }
}

fun Collection<Code>.weightConfigurations(distinct: Boolean = false): Collection<Collection<Int>> {
    if (!distinct) {
        return this.map { it.weightsList().sorted() }
    }
    return this.map { it.weightsSet() }.toSet()
}

fun mainOld1() {
    val center = Codeword(0)
    val initCode = Code(setOf(center))

    val halfOfCodes = initCode.nearestMesh(3, 8) // Весовая конфигурация [list=30, set=1]

    /**
     * Весовая конфигурация [list=1, set=1]. Все весовые конфигурации этой функции для кодов из halfOfCodes совпадают.
     */
    val preFullCodes = halfOfCodes.first().nearestMesh(3, 8)
    val fullCodes = preFullCodes.first().nearestMesh(3, 1) // Весовая конфигурация [list=1, set=1]

    fullCodes.map(Code::asMatrix).map { it.toRowEchelonForm(2) }.forEach(::println)
    println()
    println(fullCodes.first().echelonBasis())
}

fun mainOld2() { // можно менять центр
    for (intCenter in 0..127) {
        val center = Codeword(intCenter)
        val initCode = Code(setOf(center))

//        val halfOfCodes = findCodes(initCode, 3, 8)
//        val allFullCodes = mutableListOf<Code>()
//        for (halfCode in halfOfCodes) {
//            val preFullCodes = findCodes(halfCode, 3, 8)
//            for (preCode in preFullCodes) {
//                val fullCodes = findCodes(preCode, 3, 1)
//                allFullCodes.addAll(fullCodes)
//                println(fullCodes.last())
//                println()
//            }
//        }

//        println(allFullCodes.weightConfigurations(true))
//        val bases = allFullCodes.map(Code::echelonBasis)
//        val parityChecks = bases.map(MatrixConverter::canonical)
//        println("center=$center, isHamming=${parityChecks.all { it.isHammingH() }}")
    }

    val codewords = listOf(
        "0000001",
        "1001101",
        "1010011",
        "0011111",
        "0101100",
        "1111110",
        "0110010",
        "1001010",
        "1010100",
        "1100111",
        "1111001",
        "0101011",
        "0110101",
        "0000110",
        "0011000",
        "1100000"
    ).map { Codeword(it) }
    val offset = Codeword((1).shl(4))
    val code = (Code(codewords) + Codeword("0000001") + offset)
    println(code.echelonBasis())
    val h = MatrixConverter.canonical(code.echelonBasis())
    println(h.isHammingH())
}