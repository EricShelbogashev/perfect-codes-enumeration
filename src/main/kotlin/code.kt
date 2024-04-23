import mu.KotlinLogging

/**
 * Класс, представляющий код, состоящий из множества кодовых слов [Codeword].
 * @property words Множество кодовых слов.
 * @constructor Создает объект [Code] из коллекции [words].
 * @param words Коллекция кодовых слов.
 */
data class Code(var words: Set<Codeword>) {
    constructor(words: Collection<Codeword>) : this(words.toSet())

    /**
     * Кодовое расстояние.
     */
    val d: Int by lazy {
        weightsSet().min()
    }

    /**
     * Количество кодовых слов.
     */
    val size = words.size

    /**
     * Сдвигает код на заданное смещение.
     * @param offset Смещение.
     * @return [Code], сдвинутый на [Codeword].
     */
    operator fun plus(offset: Codeword): Code {
        return Code(words.map { it + offset })
    }

    /**
     * Сдвигает код на заданное смещение.
     * @param code Смещение.
     * @return [Code], сдвинутый на [Codeword].
     */
    operator fun Codeword.plus(code: Code): Code {
        return Code(code.words.map { it + this })
    }

    /**
     * Возвращает список всех возможных весов между парами кодовых слов в коде.
     * @return Список весов.
     */
    fun weightsList(): MutableList<Int> {
        val weights = mutableListOf<Int>()
        for (word1 in words) {
            for (word2 in words) {
                weights.add(word1 distance word2)
            }
        }
        weights.remove(0)
        return weights
    }

    /**
     * Возвращает множество всех возможных весов между парами кодовых слов в коде.
     * @return Множество весов.
     */
    fun weightsSet(): MutableSet<Int> {
        val weights = mutableSetOf<Int>()
        for (word1 in words) {
            for (word2 in words) {
                weights.add(word1 distance word2)
            }
        }
        weights.remove(0)
        return weights
    }

    /**
     * Находит все ближайшие кодовые слова, удаленных на заданное расстояние.
     * @param distance Расстояние.
     * @return Множество ближайших кодовых слов.
     */
    fun nearest(distance: Int): Set<Codeword> {
        val codewords = words.map { it.nearest(distance) }.flatten()
        return codewords.filter { outer ->
            words.all { inner -> inner.distance(outer) >= distance }
        }.toSet()
    }

    fun nearestMesh(distance: Int, partitions: Int): Set<Code> {
        require(partitions > 0) { "количество итераций должно быть положительным" }
        return findMeshImpl(this, distance, partitions).map(::Code).toSet()
    }

    fun contains(codeword: Codeword): Boolean {
        return words.contains(codeword)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Code

        if (words != other.words) return false
        if (size != other.size) return false

        return true
    }

    override fun hashCode(): Int {
        var result = words.hashCode()
        result = 31 * result + size
        return result
    }

    companion object {
        private val logger = KotlinLogging.logger { }
        private fun findMeshImpl(
            initCode: Code,
            distance: Int,
            partitions: Int,
        ): Set<Set<Codeword>> {
            var currentWords = mutableSetOf<Set<Codeword>>()
            /* Для заданного кода находим всех соседей строго на расстоянии [distance]. */
            val nearest = initCode.nearest(distance)

            if (partitions == 1) {
                return nearest.map { setOf(it) + initCode.words }.toSet()
            }
            logger.trace {
                "first().size=${
                    nearest.map { setOf(it) + initCode.words }.toSet().first().size
                }, size()=${
                    nearest.map { setOf(it) + initCode.words }.toSet().size
                }, first()=${nearest.map { setOf(it) + initCode.words }.toSet().first()}"
            }

            /* Разбиваем соседей на пары, удаленные на [distance] или более друг от друга. [1] */
            for (word1 in nearest) {
                for (word2 in nearest) {
                    if (word1 distance word2 >= distance) {
                        currentWords.add(setOf(word1, word2) + initCode.words)
                    }
                }
            }

            logger.trace { "first().size=${currentWords.first().size}, size()=${currentWords.size}, first()=${currentWords.first()}" }
            if (partitions == 2) {
                return currentWords
            }

            /* Для каждого уровня глубины сетчатой окрестности... */
            for (i in 1..(partitions - 3)) {
                val newWords = mutableSetOf<Set<Codeword>>()
                for (code in currentWords) {
                    for (word in nearest) {
                        val minDistance = code.minOfOrNull { it.distance(word) }
                        if (minDistance == null) continue
                        if (minDistance >= distance) {
                            /* Если слово из [1] удалено от кода на расстояние как минимум 3, добавляем в код. */
                            newWords.add(code + word)
                        }
                    }
                }

                logger.trace { "first().size=${newWords.first().size}, size()=${newWords.size}, first()=${newWords.first()}" }
                currentWords = newWords
            }

            /* В какой-то момент соседи себя исчерпают и потребуется еще один запуск алгоритма с нахождением новых соседей. */
            return currentWords
        }

        val hamming by lazy {
            val code = mutableSetOf<Codeword>()
            val g = listOf(
                Codeword("1110000"),
                Codeword("1001100"),
                Codeword("0101010"),
                Codeword("1101001")
            )
            for (word1 in g) {
                for (word2 in g) {
                    code.add(word1 + word2)
                }
            }
            for (word1 in g) {
                for (word2 in g) {
                    for (word3 in g) {
                        code.add(word1 + word2 + word3)
                    }
                }
            }
            code.add(g[0] + g[1] + g[2] + g[3])
            code.addAll(g)
            return@lazy Code(code)
        }
    }
}