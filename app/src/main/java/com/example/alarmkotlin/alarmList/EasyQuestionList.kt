package com.example.alarmkotlin.alarmList

import kotlin.random.Random

class EasyQuestionList {
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
            // Загадки в стихах
            5 -> setRiddle(
                "Дом без окон и дверей,\n" +
                        "А внутри — сто богатырей.",
                "огурец"
            )
            6 -> setRiddle(
                "Висит на стене, молчит,\n" +
                        "Но утро настанет — вдруг закричит.",
                "будильник"
            )
            7 -> setRiddle(
                "Без рук, без ног,\n" +
                        "А рисовать умеет.",
                "мороз"
            )
            8 -> setRiddle(
                "В воде родится,\n" +
                        "А воды боится.",
                "соль"
            )
            9 -> setRiddle(
                "Не птица, а летает,\n" +
                        "Не рыба, а плещется.",
                "тень"
            )
            10 -> setRiddle(
                "Два кольца, два конца,\n" +
                        "Посередине — гвоздик.",
                "ножницы"
            )
            11 -> setRiddle(
                "Что вверх дном растет,\n" +
                        "А вниз — никак не пойдет?",
                "сосулька"
            )
            12 -> setRiddle(
                "Белая вата\n" +
                        "Плывет куда-то.",
                "облако"
            )
        }
    }

    /** Генерирует математический пример и автоматически вычисляет ответ */
    private fun generateMathTask(operator: String) {
        val a = Random.nextInt(1, 10)
        val b = Random.nextInt(1, 10)

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