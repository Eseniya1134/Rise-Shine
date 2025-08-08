package com.example.alarmkotlin.alarmList.QuestionPackage

import kotlin.random.Random

class DifficultQuestionList {
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

            // Загадки
            // Загадки в стихах
            5 -> setRiddle(
                "Всех на свете обшивает,\n" +
                        "Что сошьет — не надевает.",
                "игла"
            )
            6 -> setRiddle(
                "В темнице — красна девица,\n" +
                        "А коса — на улице.",
                "морковь"
            )
            7 -> setRiddle(
                "Не огонь, а жжется,\n" +
                        "Не лук, а лукавит.",
                "крапива"
            )
            8 -> setRiddle(
                "Что за птица?\n" +
                        "Не поет, не летает,\n" +
                        "А клюет и людей не пускает.",
                "кузнечик"
            )
            9 -> setRiddle(
                "Без рук, без топоренка,\n" +
                        "Построена избенка.",
                "гнездо"
            )
            10 -> setRiddle(
                "Что за зверь:\n" +
                        "Зимой ест, а летом спит,\n" +
                        "Тело теплое, а крови нет?",
                "печь"
            )
            11 -> setRiddle(
                "Висит сито,\n" +
                        "Не руками свито.",
                "паутина"
            )
            4 -> setRiddle(
                "Что нельзя съесть на завтрак,\n" +
                        "Но можно на ужин?",
                "сон"
            )
        }
    }

    /** Генерирует математический пример и автоматически вычисляет ответ */
    private fun generateMathTask(operator: String) {
        val a = Random.nextInt(1, 10)
        val b = Random.nextInt(31, 50)
        val c = Random.nextInt(31, 50)

        question = "$a $operator $b = ?"

        answer = when (operator) {
            "+" -> (a + b).toString()
            "-" -> (a - b).toString()
            "*" -> (a * c).toString()
            else -> "0"
        }
    }

    /** Устанавливает загадку и ответ */
    private fun setRiddle(riddle: String, correctAnswer: String) {
        question = riddle
        answer = correctAnswer
    }
}