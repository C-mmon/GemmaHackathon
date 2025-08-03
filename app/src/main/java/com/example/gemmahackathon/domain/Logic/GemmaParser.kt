package com.example.gemmahackathon.domain.Logic

import com.example.gemmahackathon.data.DiaryAnalysis
import org.json.JSONObject

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
     * @param entryId The ID of the DiaryEntry this analysis is for
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
}