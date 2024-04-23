import java.io.Serializable

/**
 * Класс, представляющий кодовое слово.
 *
 * @property bits Биты кодового слова.
 * @property stringBits Биты кодового слова (строковое представление).
 * @constructor Создает новый экземпляр кодового слова.
 */
data class Codeword(
    var stringBits: String,
    var bits: Int,
) : Serializable {
    constructor(stringData: String) : this(stringData, stringData.toInt(2))
    constructor(intData: Int) : this(intData.toBinary(7), intData)

    /**
     * Вычисляет расстояние Хэмминга между двумя кодовыми словами.
     *
     * @param other Другое кодовое слово.
     * @return Расстояние Хэмминга между текущим и другим кодовым словом.
     */
    infix fun distance(other: Codeword) = this.stringBits.zip(other.stringBits).count { (c1, c2) -> c1 != c2 }

    /**
     * Вычисляет ближайшие кодовые слова с заданным расстоянием.
     *
     * @param distance Расстояние.
     * @return Множество ближайших кодовых слов.
     */
    fun nearest(distance: Int = 1): Set<Codeword> {
        if (distance != 3) {
            return Companion.nearest(this, distance).minus(this)
        }
        return zeroNearest3DistanceIndexes
            .map { stringBits.inverseAt(it) }
            .map(::Codeword)
            .toSet()
    }

    /**
     * Применяет сдвиг к текущему кодовому слову.
     *
     * @param shift Сдвиг.
     * @return Новое кодовое слово после применения сдвига.
     */
    infix fun xor(shift: Int): Codeword {
        return Codeword(bits xor shift)
    }

    /**
     * Применяет сдвиг к текущему кодовому слову.
     *
     * @param shift Сдвиг.
     * @return Новое кодовое слово после применения сдвига.
     */
    operator fun plus(shift: Codeword): Codeword {
        return Codeword(bits xor shift.bits)
    }

    /**
     * Создает новое кодовое слово с переставленными битами согласно заданной перестановке.
     *
     * @param permutation Перестановка, список старых индексов в требуемой упорядоченности.
     * @return Новое кодовое слово с переставленными битами.
     */
    fun toPermuted(permutation: List<Int>): Codeword {
        return Codeword(stringBits.toPermuted(permutation))
    }

    override fun toString(): String {
        return stringBits
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Codeword

        return bits == other.bits
    }

    override fun hashCode(): Int = bits

    private companion object {
        val zeroNearest3Distance: Set<Codeword> = nearest(Codeword(0), 3)
        val zeroNearest3DistanceIndexes = zeroNearest3Distance.map { it.stringBits.indicesOfOnes() }

        fun nearest(codeword: Codeword, distance: Int): Set<Codeword> {
            fun generateWordsWithHammingDistance(word: String, distance: Int, alphabet: String): List<String> {
                if (distance == 0) return listOf(word)
                if (word.isEmpty()) return emptyList()

                val words = mutableListOf<String>()
                for (i in word.indices) {
                    for (char in alphabet) {
                        if (char != word[i]) {
                            val newWord = word.substring(0, i) + char + word.substring(i + 1)
                            words.addAll(generateWordsWithHammingDistance(newWord, distance - 1, alphabet))
                        }
                    }
                }
                return words
            }

            return generateWordsWithHammingDistance(codeword.stringBits, distance, "01")
                .map(::Codeword)
                .toSet()
                .filter { codeword.distance(it) == distance }
                .toSet()
        }

        fun String.indicesOfOnes(): List<Int> {
            val indices = mutableListOf<Int>()
            this.forEachIndexed { index, char ->
                if (char == '1') {
                    indices.add(index)
                }
            }
            return indices
        }

        fun String.inverseAt(positions: List<Int>): String {
            val stringBuilder = StringBuilder(this)
            positions.forEach { pos ->
                val currentChar = stringBuilder[pos]
                stringBuilder[pos] = if (currentChar == '0') '1' else '0'
            }
            return stringBuilder.toString()
        }
    }
}

private fun Int.toBinary(len: Int): String {
    return String.format("%" + len + "s", this.toString(2)).replace(" ".toRegex(), "0")
}

private fun String.toPermuted(indices: List<Int>): String {
    val list = this.reversed().toMutableList()
    val permuted = list.toPermuted(indices)
    return permuted.joinToString("")
}

private fun <T> List<T>.toPermuted(indices: List<Int>): MutableList<T> {
    val temp = mutableListOf<T>()
    for (index in indices) {
        temp.add(this[index])
    }
    return temp
}