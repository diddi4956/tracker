package com.example.tracker.ui.daily

import java.math.BigDecimal
import java.math.RoundingMode

internal fun evaluateMoneyExpression(expression: String): Long? {
    if (expression.isBlank()) return null

    return runCatching {
        val value = MoneyExpressionParser(expression).parse()
        if (value.signum() < 0) return null

        value
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
    }.getOrNull()
}

private class MoneyExpressionParser(private val source: String) {
    private var position = 0

    fun parse(): BigDecimal {
        val value = parseExpression()
        skipWhitespace()
        require(position == source.length) { "올바르지 않은 문자가 있습니다." }
        return value
    }

    private fun parseExpression(): BigDecimal {
        var value = parseTerm()

        while (true) {
            skipWhitespace()
            value = when {
                consume('+') -> value.add(parseTerm())
                consume('-') -> value.subtract(parseTerm())
                else -> return value
            }
        }
    }

    private fun parseTerm(): BigDecimal {
        var value = parseFactor()

        while (true) {
            skipWhitespace()
            value = when {
                consume('*') -> value.multiply(parseFactor())
                consume('/') -> {
                    val divisor = parseFactor()
                    require(divisor.compareTo(BigDecimal.ZERO) != 0) { "0으로 나눌 수 없습니다." }
                    value.divide(divisor, 12, RoundingMode.HALF_UP)
                }
                else -> return value
            }
        }
    }

    private fun parseFactor(): BigDecimal {
        skipWhitespace()

        return when {
            consume('+') -> parseFactor()
            consume('-') -> parseFactor().negate()
            consume('(') -> {
                val value = parseExpression()
                skipWhitespace()
                require(consume(')')) { "닫는 괄호가 필요합니다." }
                value
            }
            else -> parseNumber()
        }
    }

    private fun parseNumber(): BigDecimal {
        skipWhitespace()
        val start = position
        var hasDecimalPoint = false

        while (position < source.length) {
            val character = source[position]
            when {
                character.isDigit() -> position++
                character == '.' && !hasDecimalPoint -> {
                    hasDecimalPoint = true
                    position++
                }
                else -> break
            }
        }

        require(position > start) { "숫자가 필요합니다." }
        return source.substring(start, position).toBigDecimal()
    }

    private fun consume(expected: Char): Boolean {
        if (position >= source.length || source[position] != expected) return false
        position++
        return true
    }

    private fun skipWhitespace() {
        while (position < source.length && source[position].isWhitespace()) {
            position++
        }
    }
}
