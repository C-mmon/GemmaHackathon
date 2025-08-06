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
    //We are already supporting returning Analysis result as part of the tags
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
                reflectionQuestions = when {
                    json.has("reflectionQuestions") -> {
                        val reflectionValue = json.get("reflectionQuestions")
                        when (reflectionValue) {
                            is org.json.JSONArray -> (0 until reflectionValue.length()).joinToString(", ") { i -> reflectionValue.getString(i) }
                            else -> reflectionValue.toString()
                        }
                    }
                    else -> null
                },
                writingStyle = json.optString("writingStyle"),
                emotionDistribution = json.optJSONObject("emotionDistribution")?.toString(),
                stressLevel = when {
                    json.has("stressLevel") -> {
                        val stressValue = json.get("stressLevel")
                        when (stressValue) {
                            is Int -> stressValue
                            is String -> when (stressValue.lowercase()) {
                                "low" -> 2
                                "medium", "moderate" -> 5
                                "high" -> 8
                                else -> stressValue.toIntOrNull() ?: -1
                            }
                            else -> -1
                        }
                    }
                    else -> -1
                }?.takeIf { it >= 0 },
                tone = json.optString("tone"),
                selfhelp = json.optString("self-help").takeIf { it.isNotBlank() } 
                    ?: json.optString("selfhelp").takeIf { it.isNotBlank() }
            )

            val tags = json.optJSONArray("tags")
                ?.let { arr -> (0 until arr.length()).map { i -> arr.getString(i) } }
                ?: emptyList()

            AnalysisResult(analysis, tags)
        } catch (e: Exception) {
            Log.e("GemmaParser", "Failed to parse JSON response: ${e.message}")
            Log.e("GemmaParser", "Raw response: $reply")
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
            Log.d("GemmaParser", "Original raw response:")
            Log.d("GemmaParser", raw)
            
            // Extract JSON from the response more carefully
            val jsonStart = raw.indexOf("{")
            val jsonEnd = raw.lastIndexOf("}") + 1
            
            if (jsonStart == -1 || jsonEnd == 0 || jsonStart >= jsonEnd) {
                Log.e("GemmaParser", "Could not find valid JSON boundaries")
                return null
            }
            
            var cleanJson = raw.substring(jsonStart, jsonEnd)
            
            Log.d("GemmaParser", "Extracted JSON substring:")
            Log.d("GemmaParser", cleanJson)
            
            // Remove any potential BOM or invisible characters
            cleanJson = cleanJson.replace("\uFEFF", "") // BOM
            cleanJson = cleanJson.replace("\u200B", "") // Zero-width space
            cleanJson = cleanJson.replace("\u00A0", " ") // Non-breaking space
            
            Log.d("GemmaParser", "Final cleaned JSON:")
            Log.d("GemmaParser", cleanJson)

            val json = JSONObject(cleanJson)

            // Helper function to get string or null (handles empty strings and "null" strings)
            fun getStringOrNull(key: String): String? {
                return if (json.has(key)) {
                    val value = json.getString(key)
                    if (value.isBlank() || value == "null") null else value
                } else null
            }

            // Helper function to get int or null
            fun getIntOrNull(key: String): Int? {
                return if (json.has(key)) {
                    val value = json.get(key)
                    when (value) {
                        is Int -> value
                        is String -> value.toIntOrNull()
                        else -> null
                    }
                } else null
            }

            val userEntity = UserEntity(
                id = 0, // Set appropriately if updating existing user
                name = "", // Fill later
                about = "", // Fill later
                visualMoodColour = getStringOrNull("visualMoodColour"),
                moodSensitivityLevel = getIntOrNull("moodSensitivityLevel"),
                thinkingStyle = getStringOrNull("thinkingStyle"),
                learningStyle = getStringOrNull("learningStyle"),
                writingStyle = getStringOrNull("writingStyle"),
                emotionalStrength = getStringOrNull("emotionalStrength"),
                emotionalWeakness = getStringOrNull("emotionalWeakness"),
                emotionalSignature = when {
                    json.has("emotionalSignature") -> {
                        val sigValue = json.get("emotionalSignature")
                        when (sigValue) {
                            is org.json.JSONArray -> (0 until sigValue.length()).joinToString(", ") { i -> sigValue.getString(i) }
                            is String -> if (sigValue.isBlank() || sigValue == "null") null else sigValue
                            else -> sigValue.toString()
                        }
                    }
                    else -> null
                }
            )
            
            Log.d("GemmaParser", "Parsed UserEntity: $userEntity")
            userEntity
            
        } catch (e: Exception) {
            Log.e("GemmaParser", "Failed to parse user signature JSON: ${e.message}")
            Log.e("GemmaParser", "Raw response: $raw")
            e.printStackTrace()
            null
        }
    }
}