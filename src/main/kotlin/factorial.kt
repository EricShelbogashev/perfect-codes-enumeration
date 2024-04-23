import java.math.BigInteger

val factorialCache = mutableMapOf<BigInteger, BigInteger>()

/**
 * Функция для вычисления факториала числа.
 *
 * @param n Число, для которого вычисляется факториал.
 * @return Значение факториала числа n.
 */
fun factorial(n: BigInteger): BigInteger = factorialCache.getOrPut(n) {
    if (n == BigInteger.ZERO) BigInteger.ONE else n * factorial(n - BigInteger.ONE)
}

/**
 * Функция для вычисления биномиального коэффициента.
 *
 * @param n Первый аргумент биномиального коэффициента.
 * @param k Второй аргумент биномиального коэффициента.
 * @return Значение биномиального коэффициента C(n, k).
 */
fun binomialCoefficient(n: Int, k: Int): BigInteger {
    if (n == 0 && k == 0) return BigInteger.ONE
    val N = n.toBigInteger()
    val K = k.toBigInteger()
    return factorial(N) / (factorial(K) * factorial(N.minus(K)))
}

/**
 * Функция для вычисления суммы биномиальных коэффициентов.
 *
 * @param n Первый аргумент биномиального коэффициента.
 * @param d Второй аргумент биномиального коэффициента.
 * @return Сумма биномиальных коэффициентов.
 */
fun sumBinomialCoefficients(n: Int, d: Int): BigInteger {
    val upperLimit = (d - 1) / 2
    return (0..upperLimit).fold(BigInteger.ZERO) { sum, i ->
        sum + binomialCoefficient(n, i)
    }
}

/**
 * Расширение для целых чисел для вычисления факториала.
 *
 * @return Значение факториала для данного целого числа.
 * @throws IllegalArgumentException Если число отрицательное.
 * @throws IllegalArgumentException Если число больше 20 (для предотвращения переполнения).
 */
fun Int.factorial(): Long {
    require(0 <= this) { "Факториал не определен для отрицательных чисел." }
    require(this < 21) { "Факториал не определен для чисел больше 20." }
    return when (this) {
        0, 1 -> 1L
        else -> (2..this).fold(1L) { acc, i -> acc * i }
    }
}
