package com.example.postureV2

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

data class ExerciseData(val angles: List<Float>, val label: String)

class ReadFile(private val context: Context, private val targetExercise: String) {

    fun loadExerciseData(): List<ExerciseData> {
        val result = mutableListOf<ExerciseData>()
        val inputStream = context.assets.open("exercise_angles.csv")
        val reader = BufferedReader(InputStreamReader(inputStream))

        var line = reader.readLine() // Read header and skip
        while (reader.readLine()?.also { line = it } != null) {
            val tokens = line.split(",")

            // Assume last column is the label
            val label = tokens.last().trim()
            if (label.equals(targetExercise, ignoreCase = true)) {
                val angles = tokens.dropLast(1).mapNotNull { it.trim().toFloatOrNull() }
                result.add(ExerciseData(angles, label))
            }
        }

        reader.close()
        return result
    }
}