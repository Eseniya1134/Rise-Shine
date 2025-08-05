package com.example.alarmkotlin.alarmList

import android.util.Log
import kotlin.random.Random

class QwestionList {

    private lateinit var qwestion: String
    private lateinit var answer: String

    fun getQwestion() : String{
        qwestion = generateQwest()
        return qwestion
    }

    fun getAnswer(): String{
        return answer
    }

    private fun generateQwest(): String {
        val randomNumber = Random.nextInt(1, 6)
        when (randomNumber) {
            1 -> qwestion = "34 + 75";
            2 -> qwestion = "24 + 79"
            3 -> qwestion = "28 + 19"
            4 -> qwestion = "48 + 14"
            5 -> qwestion = "43 + 94"
            else -> qwestion = "Много ног, но он плывёт,\n" +
                    "В море синем он живёт,\n" +
                    "Беспозвоночное создание,\n" +
                    "В присосках его созерцание.\n"
        }
    }


}
