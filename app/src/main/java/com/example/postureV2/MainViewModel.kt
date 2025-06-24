/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.postureV2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import android.content.Context
import java.util.*

/**
 *  This ViewModel is used to store pose landmarker helper settings
 */
class MainViewModel : ViewModel() {

    private var results: PoseLandmarkerResult? = null
    private var _model = PoseLandmarkerHelper.MODEL_POSE_LANDMARKER_LITE
    private var _delegate: Int = PoseLandmarkerHelper.DELEGATE_CPU
    private var _minPoseDetectionConfidence: Float =
        PoseLandmarkerHelper.DEFAULT_POSE_DETECTION_CONFIDENCE
    private var _minPoseTrackingConfidence: Float = PoseLandmarkerHelper
        .DEFAULT_POSE_TRACKING_CONFIDENCE
    private var _minPosePresenceConfidence: Float = PoseLandmarkerHelper
        .DEFAULT_POSE_PRESENCE_CONFIDENCE

    //Added by FitHit Developers
    private val _poseResults = MutableLiveData<String>("Waiting for data...")
    val poseResults: LiveData<String> = _poseResults
    private val _angleRead = MutableLiveData<String>("...Loadingg")
    val angleRead: LiveData<String> = _angleRead

    private val _postureFeedback = MutableLiveData<String>()
    val postureFeedback: LiveData<String> = _postureFeedback
    private val _exerciseReport = MutableLiveData<String>()
    val exerciseReport: LiveData<String> = _exerciseReport

    private var referenceFrames: List<Map<String, Float>> = emptyList()
    private val performanceHistory = mutableListOf<FrameMatchResult>()
    private var lastMatchedIndex = -1
    private val frameThreshold = 35f
    private val minSequenceLength = 5

    val currentDelegate: Int get() = _delegate
    val currentModel: Int get() = _model
    val currentMinPoseDetectionConfidence: Float
        get() =
            _minPoseDetectionConfidence
    val currentMinPoseTrackingConfidence: Float
        get() =
            _minPoseTrackingConfidence
    val currentMinPosePresenceConfidence: Float
        get() =
            _minPosePresenceConfidence

    fun setDelegate(delegate: Int) {
        _delegate = delegate
    }

    fun setMinPoseDetectionConfidence(confidence: Float) {
        _minPoseDetectionConfidence = confidence
    }

    fun setMinPoseTrackingConfidence(confidence: Float) {
        _minPoseTrackingConfidence = confidence
    }

    fun setMinPosePresenceConfidence(confidence: Float) {
        _minPosePresenceConfidence = confidence
    }

    fun setModel(model: Int) {
        _model = model
    }

    //added by FitHit Developers
    fun poseResult(poseResult: PoseLandmarkerResult) {
        val landmark = poseResult.landmarks()?.firstOrNull()?.getOrNull(11)
        _poseResults.value = if (landmark != null)
            "X: ${landmark.x()}, Y: ${landmark.y()}"
        else
            "No landmarks detected"
    }

    fun angleReading(result: PoseLandmarkerResult) {
        val angles = calculateAngles(result)
        val readAngle = angles["Shoulder_Angle"].toString()
        _angleRead.value = if (angles["Shoulder_Angle"] != null)
            readAngle
        else
            "No Angles detected"
    }

    fun processFrameOld(result:PoseLandmarkerResult, context: Context, targetExercise: String, currentFrame: Map<String, Float>) {
        //getting readings from the dataset
        val datasetReadings = loadExerciseData(context, targetExercise)

        //then get the readings from the google mediapipe
        val appReadings = calculateAngles(result)

        //Comparison
        //went to write the findBestMatchInContext, brb

        //Take the difference between both left and right sides, save it in a list
        //transverse the list
            //in the list, anything higher 0.5 is Good, and so on
            //add the labels into a new list, this one will be for printing
        //return the list
    }

    //from further onwards is the code for posture correction
    fun setAngleReadings(result: PoseLandmarkerResult){
        processFrame(calculateAngles(result))
    }

    fun setExercise(context: Context, exerciseName: String) {
        referenceFrames = loadExerciseData(context, exerciseName)
    }

    private fun processFrame(jointAngles: Map<String, Float>){
        if (referenceFrames.isEmpty()) return

        val result = processExerciseFrame(jointAngles,referenceFrames, lastMatchedIndex)

        performanceHistory.add(result)
        lastMatchedIndex=result.frameIndex

        val progress = (lastMatchedIndex*100) / referenceFrames.size
        var feedback = "Fram ${result.frameIndex}/${referenceFrames.size} (${"%.1f".format(result.deviation)})\nProgress: $progress%"

        result.jointDeviation.forEach{(joint, deviation) ->
            if (deviation > 30f) {
                feedback += "\n$joint: ${"%.1f".format(deviation)} off"
            }
        }
        _postureFeedback.postValue(feedback)
    }

    fun completeExercise() {
        val report = generateExerciseReport(performanceHistory, frameThreshold, minSequenceLength)
        val reportText = buildReportString(report)
        _exerciseReport.postValue(reportText)
        resetExerciseTracking()
    }

    private fun resetExerciseTracking(){
        performanceHistory.clear()
        lastMatchedIndex = -1
    }

//    private fun buildReportString(report: ExerciseReport): String {
//        val sb = StringBuilder()
//        sb.append("Overall Score ${"%.1f".format(report.overallScore)}/100\n")
//        sb.append("Average Deviation: ${"%.1f".format(report.deviation)}\n")
//
//        report.problemSegments.forEach{ segment ->
//            sb.append("\nFrames ${segment.startIndex}-${segment.endIndex} (Avg Dev: ${"%.1f".format(segment.deviation)}):\n")
//            segment.problemJoint.forEach{(joint, deviation) ->
//                sb.append(" - $joint: ${"%.1f".format(deviation)}\n")
//            }
//        }
//        return sb.toString()
//    }

    private fun buildReportString(report: ExerciseReport): String {
        val sb = StringBuilder()
        sb.append("Overall Score: ${"%.1f".format(report.overallScore)}/100\n")
        sb.append("Average Deviation: ${"%.1f".format(report.deviation)}\n\n")

        sb.append("Areas for Improvement:\n")
        report.problemSegments.forEachIndexed{ index, segment ->
            sb.append("\n${index +1}. Frames ${segment.startIndex}-${segment.endIndex}:\n")
            sb.append("   Average Deviation: ${"%.1f".format(segment.deviation)}\n")

            segment.problemJoint.forEach{(joint, deviation) ->
                sb.append("   $joint: ${"%.1f".format(deviation)} deviation\n")
            }
        }

        return sb.toString()
    }
}