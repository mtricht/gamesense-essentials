package eu.tricht.gamesense.model

import eu.tricht.gamesense.preferences

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.NativeHookException
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import com.sun.jna.platform.win32.User32
import kotlin.math.pow

class Calculator {
    private val digits = ('0'..'9').toList()
    private val operators = listOf('+', '-', '*', '/', '^')
    private val textMaxLength = 15

    // Used in non-algebraic (basic) mode
    private var carryOnValue = 0.0
    private var carryOnOperator: Char? = null

    private var input = ""
    var inputDisplay = "|"
    private var answer = ""
    var answerDisplay = ""

    var numpadOn = isNumLockOn()
    private var lShiftOn = false
    private var rShiftOn = false

    init {
        try {
            // Initialize the GlobalScreen to start capturing global input events
            GlobalScreen.registerNativeHook()

            GlobalScreen.addNativeKeyListener(object : NativeKeyListener {
                override fun nativeKeyPressed(nativeEvent: NativeKeyEvent) {
                    if (nativeEvent.keyCode == 42) { // LShift
                        lShiftOn = true
                    } else if (nativeEvent.keyCode == 3638) { // RShift
                        rShiftOn = true
                    } else if (isNumpadKey(nativeEvent.keyCode)) {
                        if (if (!preferences.get("calculatorNumpadFlip", "false").toBoolean()) !numpadOn else numpadOn) {
                            return
                        }

                        val shiftOn = lShiftOn || rShiftOn

                        when (nativeEvent.keyCode) {
                            14 -> { // Backspace
                                if (!preferences.get("calculatorIsAlgebraic", "false").toBoolean()) {
                                    if (input.isNotEmpty() && input.last() in operators.toString()) {
                                        return
                                    }
                                }
                                if (input.isNotEmpty()) {
                                    input = input.dropLast(1)
                                    updateDisplay()
                                }
                                return
                            }
                            28 -> { // Enter
                                evaluate()
                                return
                            }
                        }

                        var text = NativeKeyEvent.getKeyText(nativeEvent.keyCode).toCharArray()[0]
                        if (preferences.get("calculatorNumpadFlip", "false").toBoolean()) {
                            when (nativeEvent.keyCode) {
                                3666 -> text = '0'
                                3663 -> text = '1'
                                57424 -> text = '2'
                                3665 -> text = '3'
                                57419 -> text = '4'
                                57420 -> text = '5'
                                57421 -> text = '6'
                                3655 -> text = '7'
                                57416 -> text = '8'
                                3657 -> text = '9'
                            }
                        }
                        when (nativeEvent.keyCode) {
                            53 -> { text = '/' }
                            3639 -> { text = '*' }
                            3658 -> { text = '-' }
                            3662 -> { text = '+' }
                            83 -> { text = '.' }
                            3667 -> { text = '.' }
                            7 -> if (shiftOn) { text = '^' }
                            10 -> if (shiftOn) { text = '(' }
                            11 -> if (shiftOn) { text = ')' }
                        }

                        if (input.isNotEmpty() && input.last() in operators && text in operators) {
                            input = input.dropLast(1)
                        }
                        input += text

                        if (!preferences.get("calculatorIsAlgebraic", "false").toBoolean()) {
                            if (text == ')' || text == '(') {
                                input = input.dropLast(1)
                                return
                            }

                            if (when (nativeEvent.keyCode) {
                                53 -> true // /
                                3639 -> true // *
                                3658 -> true // -
                                3662 -> true // +
                                7 -> shiftOn // ^
                                else -> false
                            }) {
                                if (input.length <= 1) {
                                    input = ""
                                    return
                                }
                                evaluate(shouldCarryOnValue = true)
                                return
                            }
                        }

                        updateDisplay()
                    }
                }

                override fun nativeKeyReleased(nativeEvent: NativeKeyEvent) {
                    when (nativeEvent.keyCode) {
                        42 -> { // LShift
                            lShiftOn = false
                        }
                        3638 -> { // RShift
                            rShiftOn = false
                        }
                        69 -> { // Numpad
                            numpadOn = !numpadOn
                            reset()
                        }
                    }
                }

            })
        } catch (e: NativeHookException) {
            println("There was an error registering the native hook: ${e.message}")
        }
    }

    private fun isNumLockOn(): Boolean {
        val keyboardState = ByteArray(256) // Array to store the state of all keys

        // TODO: Add mac support...
        val result = User32.INSTANCE.GetKeyboardState(keyboardState)

        if (!result) {
            println("Failed to get keyboard state")
            return false
        }

        // Check the state of the Num Lock key (key code 0x90)
        val numLockState = keyboardState[0x90].toInt()

        return numLockState and 0x01 != 0
    }

    private fun isNumpadKey(keyCode: Int): Boolean {
        val numpadNumbers = if (!preferences.get("calculatorNumpadFlip", "false").toBoolean()) {
            2..11 // Numpad number keys
        } else {
            listOf(
                3666, // 0
                3663, // 1
                57424, // 2
                3665, // 3
                57419, // 4
                57420, // 5
                57421, // 6
                3655, // 7
                57416, // 8
                3657, // 9
            ) // Alternative keys
        }

        return when (keyCode) {
            in numpadNumbers -> true // 0-9
            53 -> true // /
            3639 -> true // *
            3658 -> true // -
            3662 -> true // +
            83 -> true // .
            3667 -> true // .
            14 -> true // Backspace
            28 -> true // Enter
            else -> false
        }
    }

    fun reset(shouldUpdateDisplay: Boolean = true) {
        carryOnValue = 0.0
        carryOnOperator = null
        input = ""
        answer = ""
        if (shouldUpdateDisplay) { updateDisplay() }
    }

    private fun updateDisplay(hasError: Boolean = false) {
        if (hasError) {
            inputDisplay = truncateDisplayText(input)
            answerDisplay = "Syntax error"
            return
        }

        if (!preferences.get("calculatorIsAlgebraic", "false").toBoolean()) {
            // Find the last occurrence of any operator
            val lastOperatorIndex = input.indexOfLast { it in operators }

            if (lastOperatorIndex == -1) {
                inputDisplay = "${truncateDisplayText(input)}|"
                answerDisplay = truncateDisplayText(answer)
            } else {
                // Get the terms before and after the operator
//                val beforeOperator = input.substring(0, lastOperatorIndex + 1).trim()
                val afterOperator = input.substring(lastOperatorIndex + 1).trim()

                inputDisplay = truncateDisplayText("${parseValue(carryOnValue)}${carryOnOperator}")
                answerDisplay = truncateDisplayText("${afterOperator}|")
            }
        } else {
            inputDisplay = "${truncateDisplayText(input)}|"
            answerDisplay = truncateDisplayText(answer)
        }
    }

    private fun truncateDisplayText(text: String): String {
        return if (text.length <= textMaxLength) {
            text
        } else {
            text.takeLast(textMaxLength)
        }
    }

    private fun precedence(op: String): Int {
        return when (op) {
            "+", "-" -> 1
            "*", "/" -> 2
            "^" -> 3
            else -> 0
        }
    }

    private fun infixToPostfix(expression: List<String>): List<String> {
        val result = mutableListOf<String>()
        val stack = mutableListOf<String>()

        for (token in expression) {
            when (token) {
                // If the token is a number, add it to the result
                !in (operators + listOf('(', ')')).toString() -> result.add(token)
                "(" -> stack.add(token)
                // If the token is a closing parenthesis, pop from the stack until an opening parenthesis is encountered
                ")" -> {
                    while (stack.isNotEmpty() && stack.last() != "(") {
                        result.add(stack.removeAt(stack.size - 1))
                    }
                    stack.removeAt(stack.size - 1) // Remove '('
                }
                in operators.toString() -> {
                    // Any higher precedence operators is added first before adding lower precedence token operator
                    while (stack.isNotEmpty() && precedence(stack.last()) >= precedence(token)) {
                        result.add(stack.removeAt(stack.size - 1))
                    }
                    stack.add(token)
                }
            }
        }

        // Pop all the operators remaining in the stack
        while (stack.isNotEmpty()) {
            result.add(stack.removeAt(stack.size - 1))
        }

        return result
    }

    private fun evalPostfix(postfix: List<String>): Double {
        val stack = mutableListOf<Double>()

        for (token in postfix) {
            when (token) {
                // If the token is a number, push it onto the stack
                !in (operators + listOf('(', ')')).toString() -> stack.add(token.toDouble())
                // If the token is an operator, pop two operands and apply the operation
                in listOf("+", "-", "*", "/", "^") -> {
                    val b = stack.removeAt(stack.size - 1)  // Second operand
                    val a = stack.removeAt(stack.size - 1)  // First operand
                    val result = when (token) {
                        "+" -> a + b
                        "-" -> a - b
                        "*" -> a * b
                        "/" -> a / b
                        "^" -> a.pow(b)
                        else -> throw IllegalArgumentException("Unknown operator: $token")
                    }
                    stack.add(result)  // Push the result back onto the stack
                }
            }
        }

        // The result is the last remaining item on the stack
        return stack.lastOrNull() ?: throw IllegalArgumentException("Invalid expression")
    }

    private fun evaluateInfix(expression: List<String>): Double {
        val postfix = infixToPostfix(expression)
        return evalPostfix(postfix)
    }

    private fun stringExpressionToList(expression: String): Pair<Boolean, List<String>> {
        val tokens = mutableListOf<String>()
        var hasTerms = false
        var openingBracketCount = 0
        var constructingTerm = ""
        var constructingHasDecimal = false

        fun addTerm(term: String) {
            if (term.isEmpty()) {
                return
            }

            hasTerms = true
            tokens.add(term)
            constructingHasDecimal = false
        }

        for (char in expression) {
            if (char == '(') {
                if (constructingTerm.isNotEmpty()) {
                    // There are digits in front of the bracket separated by any operator
                    addTerm(constructingTerm)
                    constructingTerm = ""
                    addTerm("*")
                } else if (constructingTerm.isEmpty() && tokens.isNotEmpty() && tokens.last() == ")") {
                    // There is a ( directly after a )
                    constructingTerm = ""
                    addTerm("*")
                }
                addTerm("(")
                openingBracketCount++
            } else if (char == ')') {
                openingBracketCount--

                addTerm(constructingTerm)
                constructingTerm = ""

                if (tokens.last() == "(" || tokens.last() in operators.toString()) {
                    // A ) cannot come after an operator or a (
                    return Pair(false, emptyList())
                }

                addTerm(")")

                if (openingBracketCount < 0) {
                    return Pair(false, emptyList())
                }
            } else if (char in operators && !hasTerms && constructingTerm.isEmpty()) {
                // Cannot have an operator as first char
                if (char == '-') {
                    // Unless it is a negative number as the first term
                    constructingTerm += char
                } else {
                    println("Cannot have an operator as first char")
                    return Pair(false, emptyList())
                }
            } else if (char in operators && (hasTerms || constructingTerm.isNotEmpty())) {
                // There were digits preceding the operator
                // Now register this term to items
                addTerm(constructingTerm)
                constructingTerm = ""

                if (tokens.last() == "(") {
                    // An operator cannot come immediately after a (
                    return Pair(false, emptyList())
                }

                addTerm(char.toString())
            } else if (char in digits) {
                constructingTerm += char
            } else if (char == '.') {
                if (constructingHasDecimal) {
                    println("There cannot be more than one decimal in a single term")
                    return Pair(false, emptyList())
                }
                constructingTerm += '.'
                constructingHasDecimal = true
            }
        }

        if (openingBracketCount != 0) {
            // Unclosed brackets or excess closing brackets
            return Pair(false, emptyList())
        }

        if (constructingTerm.isNotEmpty()) {
            // There was no operator after this term to register it
            addTerm(constructingTerm)
            constructingTerm = ""
        }

        if (tokens.isEmpty()) {
            println("Expression cannot be empty")
            return Pair(false, emptyList())
        }

        if (tokens.last() in operators.toString()) {
            println("The last char should not be an operator")
            return Pair(false, emptyList())
        }

        return Pair(true, tokens)
    }

    fun evaluate(shouldCarryOnValue: Boolean = false) {
        if (input.isEmpty()) {
            return
        } else if (shouldCarryOnValue) {
            val (success, tokens) = stringExpressionToList(if (input.last() in operators) {input.dropLast(1)} else {input})
            if (success) {
                val operator = input.last()
                carryOnValue = evaluateInfix(tokens)
                carryOnOperator = operator
                input = "(${input.dropLast(1)})${operator}"
                updateDisplay()
            } else {
                updateDisplay(hasError = true)
            }
        } else {
            val (success, tokens) = stringExpressionToList(if (input.last() in operators) {input.dropLast(1)} else {input})
            if (success) {
                inputDisplay = ""
                answerDisplay = parseValue(evaluateInfix(tokens))
                reset(shouldUpdateDisplay = false)
            } else {
                updateDisplay(hasError = true)
            }
        }
    }

    private fun parseValue(value: Double): String {
        val string = value.toString()

        return if (string == "NaN" || string == "Infinity") {
            // Likely due to a div by 0
            "Div by 0"
        } else {
            String.format("%.15f", value).trimEnd('0').trimEnd('.')
        }
    }
}