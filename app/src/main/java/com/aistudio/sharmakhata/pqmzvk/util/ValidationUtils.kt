package com.aistudio.sharmakhata.pqmzvk.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * VisualTransformation that allows only one decimal point in numeric input.
 * Prevents "12.34.56" by stripping extra dots.
 */
class DecimalVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val filtered = buildString {
            var dotCount = 0
            for (ch in text) {
                if (ch == '.') {
                    dotCount++
                    if (dotCount <= 1) append(ch)
                } else {
                    append(ch)
                }
            }
        }
        return TransformedText(AnnotatedString(filtered), OffsetMapping.Identity)
    }
}

/**
 * VisualTransformation that formats phone input as XXX XXX XXXX (Indian 10-digit).
 * Strips non-digit characters automatically.
 */
class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.filter { it.isDigit() }.take(10)
        val formatted = buildString {
            for (i in digits.indices) {
                if (i == 3 || i == 6) append(' ')
                append(digits[i])
            }
        }
        return TransformedText(
            AnnotatedString(formatted),
            PhoneOffsetMapping(digits.length)
        )
    }
}

private class PhoneOffsetMapping(private val digitCount: Int) : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
        if (offset <= 0) return 0
        val digitOffset = minOf(offset, digitCount)
        var spaces = 0
        for (i in 0 until digitOffset) {
            if (i == 3 || i == 6) spaces++
        }
        return digitOffset + spaces
    }

    override fun transformedToOriginal(offset: Int): Int {
        var originalOffset = 0
        var transformedOffset = 0
        while (originalOffset < digitCount && transformedOffset < offset) {
            if (originalOffset == 3 || originalOffset == 6) {
                transformedOffset++ // skip space
            }
            originalOffset++
            transformedOffset++
        }
        return minOf(originalOffset, digitCount)
    }
}

object FormValidators {

    fun isValidPhone(phone: String): Boolean {
        val digits = phone.filter { it.isDigit() }
        return digits.length == 10 && digits.first() in '6'..'9'
    }

    fun isValidAmount(amount: String): Boolean {
        val value = amount.toDoubleOrNull() ?: return false
        return value > 0
    }

    fun isValidPrice(price: String): Boolean {
        val value = price.toDoubleOrNull() ?: return false
        return value >= 0
    }

    fun isValidName(name: String): Boolean {
        return name.isNotBlank() && name.trim().length >= 2
    }

    fun parseDoubleSafe(text: String): Double {
        return text.toDoubleOrNull() ?: 0.0
    }

    fun parseIntSafe(text: String, default: Int = 1): Int {
        return text.toIntOrNull() ?: default
    }
}
