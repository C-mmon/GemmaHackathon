package com.example.gemmahackathon.domain.Logic

import com.example.gemmahackathon.data.diary.DiaryAnalysis
import org.json.JSONObject
import android.util.Log
import com.example.gemmahackathon.data.user.UserEntity
/**
 * Result of LLM analysis parsing.
 * Contains both the structured analysis and the list of tags.
 */
data class AnalysisResult(
    val analysis: DiaryAnalysis,
    val tags: List<String>
)

object GemmaParser {

    /**
     * Parses the LLM's JSON reply into a DiaryAnalysis object and a list of tags.
     *
     * @param reply Raw response string from Gemma
     * @param entryId The ID of the DiaryEntry this analysis is for something which I thought was useful but is not now
     * @return [AnalysisResult] containing parsed analysis and tags, or null if parsing failed
     */
    fun parse(reply: String?, entryId: Long): AnalysisResult? {
        if (reply.isNullOrBlank()) return null

        return try {
            val cleanJson = reply
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val json = JSONObject(cleanJson)

            val analysis = DiaryAnalysis(
                entryId = entryId,
                mood = json.optString("mood"),
                moodConfidence = json.optDouble("moodConfidence", -1.0).toFloat().takeIf { it >= 0 },
                summary = json.optString("summary"),
                reflectionQuestions = json.optJSONArray("reflectionQuestions")
                    ?.let { arr -> (0 until arr.length()).joinToString(", ") { i -> arr.getString(i) } },
                writingStyle = json.optString("writingStyle"),
                emotionDistribution = json.optJSONObject("emotionDistribution")?.toString(),
                stressLevel = json.optInt("stressLevel", -1).takeIf { it >= 0 },
                tone = json.optString("tone")
            )

            val tags = json.optJSONArray("tags")
                ?.let { arr -> (0 until arr.length()).map { i -> arr.getString(i) } }
                ?: emptyList()

            AnalysisResult(analysis, tags)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun parseTagsArray(raw: String?): List<String>? {
        if (raw.isNullOrBlank()) return null

        return try {
            val cleanJson = raw
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val json = JSONObject(cleanJson)
            val jsonArray = json.optJSONArray("tags")
                ?: throw IllegalArgumentException("Missing 'tags' field")

            List(jsonArray.length()) { i -> jsonArray.getString(i) }
        } catch (e: Exception) {
            Log.e("GemmaParser", "Failed to parse tags: ${e.message}")
            null
        }
    }

    fun parseUserSignatureJson(raw: String?): UserEntity? {
        if (raw.isNullOrBlank()) return null

        return try {
            val cleanJson = raw
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val json = JSONObject(cleanJson)

            UserEntity(
                id = 0, // Set appropriately if updating existing user
                name = "", // Fill later
                about = "", // Fill later
                visualMoodColour = json.optString("visualMoodColour", null),
                moodSensitivityLevel = json.optInt("moodSensitivityLevel", -1).takeIf { it >= 0 },
                thinkingStyle = json.optString("thinkingStyle", null),
                learningStyle = json.optString("learningStyle", null),
                writingStyle = json.optString("writingStyle", null),
                emotionalStrength = json.optString("emotionalStrength", null),
                emotionalWeakness = json.optString("emotionalWeakness", null),
                emotionalSignature = json.optJSONArray("emotionalSignature")?.let { array ->
                    (0 until array.length()).joinToString(",") { i -> array.getString(i) }
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}