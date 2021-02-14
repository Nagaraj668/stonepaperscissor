package com.myspsgame.app

import kotlin.math.abs

class SPS(
    var option1: Int,
    var option2: Int
) {

    fun findWinner(): Int {
        val diff = abs(option1 - option2)

        if (diff == 1) {
            return if (option1 > option2)
                option1
            else
                option2
        } else if (diff == 2) {
            return if (option1 > option2)
                option2
            else
                option1
        } else {
            return 0
        }
    }
}
/*

fun main() {
    println(SPS(3, 3).findWinner())
}*/
