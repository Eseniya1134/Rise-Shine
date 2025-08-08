package com.example.alarmkotlin.alarmList.QuestionPackage

import kotlin.random.Random

class MiddleQuestionList : QuestionList{
    private lateinit var question: String
    private lateinit var answer: String

    override fun getQuestion(): String {
        generateQuestion()
        return question
    }

    override fun getAnswer(): String {
        return answer
    }

    private fun easyGenerateQuestion(){
        when (Random.nextInt(1, 11)) { // 10 вариантов (1-10)
            // Математические примеры (сложение, вычитание, умножение, деление)
            1 -> generateMathTask("+")
            2 -> generateMathTask("-")
            3 -> generateMathTask("*")
        }
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
                "Стоит дуб,\n" +
                        "На дубу 12 веток,\n" +
                        "На каждой ветке — по 4 гнезда.\n" +
                        "Что это?",
                "год"
            )
            6 -> setRiddle(
                "Не конь, а бежит,\n" +
                        "Не лес, а шумит.",
                "река"
            )
            7 -> setRiddle(
                "Без крыльев летят,\n" +
                        "Без ног бегут,\n" +
                        "Без паруса плывут.",
                "облака"
            )
            8 -> setRiddle(
                "Что за зверь:\n" +
                        "Днем ест, ночью спит,\n" +
                        "А глаза — во все стороны глядят?",
                "подсолнух"
            )
            9 -> setRiddle(
                "Сижу верхом,\n" +
                        "Не ведаю на ком.\n" +
                        "Знакомца встречу —\n" +
                        "Соскочу, привечу.",
                "шапка"
            )
            10 -> setRiddle(
                "Не живое, а идет,\n" +
                        "Неподвижное — ведет.",
                "дорога"
            )
            11 -> setRiddle(
                "Кто на свете всех сильнее?\n" +
                        "Кто на свете всех быстрее?",
                "мысль"
            )
            4 -> setRiddle(
                "Что нельзя удержать\n" +
                        "И десяти минут?",
                "дыхание"
            )
        }
    }

    /** Генерирует математический пример и автоматически вычисляет ответ */
    private fun generateMathTask(operator: String) {
        val a = Random.nextInt(1, 10)
        val b = Random.nextInt(11, 30)
        val c = Random.nextInt(11, 30)

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