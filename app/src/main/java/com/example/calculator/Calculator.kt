package com.example.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * Evaluates a chain of calculations such as "12+3×4−6÷2".
 * Multiplication and division are applied before addition and subtraction.
 */
object Calculator {

    const val ADD = '+'
    const val SUBTRACT = '−'
    const val MULTIPLY = '×'
    const val DIVIDE = '÷'
    val OPERATORS = setOf(ADD, SUBTRACT, MULTIPLY, DIVIDE)

    /** Returns the formatted result, or "Error" for invalid input / division by zero. */
    fun evaluate(expression: String): String {
        return try {
            format(compute(tokenize(expression)))
        } catch (e: ArithmeticException) {
            "Error"
        } catch (e: IllegalArgumentException) {
            "Error"
        }
    }

    private fun tokenize(expression: String): List<Any> {
        val tokens = mutableListOf<Any>()
        val number = StringBuilder()
        for ((i, c) in expression.withIndex()) {
            val isLeadingMinus = c == SUBTRACT && (i == 0 || expression[i - 1] in OPERATORS)
            if (c.isDigit() || c == '.' || isLeadingMinus) {
                number.append(if (c == SUBTRACT) '-' else c)
            } else if (c in OPERATORS) {
                require(number.isNotEmpty())
                tokens += BigDecimal(number.toString())
                number.clear()
                tokens += c
            }
        }
        require(number.isNotEmpty())
        tokens += BigDecimal(number.toString())
        return tokens
    }

    private fun compute(tokens: List<Any>): BigDecimal {
        // First pass: × and ÷
        val terms = mutableListOf<Any>(tokens[0])
        var i = 1
        while (i < tokens.size) {
            val op = tokens[i] as Char
            val right = tokens[i + 1] as BigDecimal
            if (op == MULTIPLY || op == DIVIDE) {
                val left = terms.removeAt(terms.lastIndex) as BigDecimal
                terms += if (op == MULTIPLY) {
                    left.multiply(right)
                } else {
                    if (right.signum() == 0) throw ArithmeticException("Division by zero")
                    left.divide(right, MathContext.DECIMAL64)
                }
            } else {
                terms += op
                terms += right
            }
            i += 2
        }
        // Second pass: + and −
        var result = terms[0] as BigDecimal
        i = 1
        while (i < terms.size) {
            val op = terms[i] as Char
            val value = terms[i + 1] as BigDecimal
            result = if (op == ADD) result.add(value) else result.subtract(value)
            i += 2
        }
        return result
    }

    private fun format(value: BigDecimal): String {
        val rounded = value.setScale(10, RoundingMode.HALF_UP).stripTrailingZeros()
        return rounded.toPlainString().replace('-', SUBTRACT)
    }
}
