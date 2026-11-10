package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.Calculator.OPERATORS

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CalculatorScreen()
                }
            }
        }
    }
}

private val BUTTON_ROWS = listOf(
    listOf("C", "⌫", "÷"),
    listOf("7", "8", "9", "×"),
    listOf("4", "5", "6", "−"),
    listOf("1", "2", "3", "+"),
    listOf("0", ".", "="),
)

@Composable
fun CalculatorScreen() {
    var expression by rememberSaveable { mutableStateOf("") }
    var result by rememberSaveable { mutableStateOf("") }

    fun onButton(label: String) {
        // After "=", typing a digit starts fresh; typing an operator continues from the result.
        if (result.isNotEmpty()) {
            expression = if (label.first() in OPERATORS && result != "Error") result else ""
            result = ""
        }
        val last = expression.lastOrNull()
        when (label) {
            "C" -> expression = ""
            "⌫" -> expression = expression.dropLast(1)
            "=" -> if (expression.isNotEmpty()) result = Calculator.evaluate(expression)
            "." -> {
                val currentNumber = expression.takeLastWhile { it !in OPERATORS }
                if ('.' !in currentNumber) {
                    expression += if (currentNumber.isEmpty()) "0." else "."
                }
            }
            else -> {
                val op = label.first()
                if (op in OPERATORS) {
                    expression = when {
                        expression.isEmpty() -> if (op == Calculator.SUBTRACT) "−" else ""
                        last in OPERATORS -> expression.dropLast(1) + op // replace operator
                        else -> expression + op
                    }
                } else {
                    expression += label
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = expression.ifEmpty { "0" },
            fontSize = 32.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = if (result.isEmpty()) "" else "= $result",
            fontSize = 44.sp,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.padding(8.dp))
        BUTTON_ROWS.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                row.forEach { label ->
                    // "C" and "0" take two columns so every row fills 4 slots.
                    val span = if (label == "C" || label == "0") 2f else 1f
                    val isAction = label.first() in OPERATORS || label in listOf("=", "C", "⌫")
                    Button(
                        onClick = { onButton(label) },
                        colors = if (isAction) ButtonDefaults.buttonColors()
                        else ButtonDefaults.filledTonalButtonColors(),
                        modifier = Modifier
                            .weight(span)
                            .aspectRatio(span * 1.2f),
                    ) {
                        Text(text = label, fontSize = 24.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorPreview() {
    MaterialTheme { CalculatorScreen() }
}

