package com.example.alarmkotlin.alarmList

import kotlin.random.Random

class QwestionList {

    private lateinit var question: String
    private lateinit var answer: String

    fun getQuestion(): String {
        generateQuestion()
        return question
    }

    fun getAnswer(): String {
        return answer
    }

    private fun generateQuestion() {
        when (Random.nextInt(1, 11)) { // 10 вариантов (1-10)
            // Математические примеры (сложение, вычитание, умножение, деление)
            1 -> generateMathTask("+")
            2 -> generateMathTask("-")
            3 -> generateMathTask("*")
            4 -> generateMathTask("/")

            // Загадки
            5 -> setRiddle(
                "Много ног, но он плывёт,\n" +
                        "В море синем он живёт,\n" +
                        "Беспозвоночное создание,\n" +
                        "В присосках его созерцание.",
                "осьминог"
            )
            6 -> setRiddle(
                "Без окон, без дверей,\n" +
                        "Полна горница людей.",
                "огурец"
            )
            7 -> setRiddle(
                "Висит груша — нельзя скушать.",
                "лампочка"
            )
            8 -> setRiddle(
                "Зимой и летом одним цветом.",
                "ёлка"
            )
            9 -> setRiddle(
                "Чем больше из неё берёшь,\n" +
                        "Тем больше становится.",
                "яма"
            )
            10 -> setRiddle(
                "Сидит дед, во сто шуб одет.\n" +
                        "Кто его раздевает, тот слёзы проливает.",
                "лук"
            )
        }
    }

    /** Генерирует математический пример и автоматически вычисляет ответ */
    private fun generateMathTask(operator: String) {
        val a = Random.nextInt(1, 50)
        val b = Random.nextInt(1, 50)

        question = "$a $operator $b = ?"

        answer = when (operator) {
            "+" -> (a + b).toString()
            "-" -> (a - b).toString()
            "*" -> (a * b).toString()
            "/" -> (a / b).toString()
            else -> "0"
        }
    }

    /** Устанавливает загадку и ответ */
    private fun setRiddle(riddle: String, correctAnswer: String) {
        question = riddle
        answer = correctAnswer
    }
}