package com.example

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testDataProtectionBackupJsonFormat() {
        val root = JSONObject().apply {
            put("version", 2)
            put("wallpaperId", "matrix_code")
            put("wallpaperDim", 0.9)
            put("tasks", JSONArray().apply {
                put(JSONObject().apply {
                    put("title", "Test Task")
                    put("isCompleted", false)
                })
            })
            put("shortcuts", JSONArray().apply {
                put(JSONObject().apply {
                    put("name", "PFX Player")
                    put("urlOrPackage", "https://pfxplayer.online/v/31d20a56-5c2c-4e02-a6a2-198dc3582124")
                })
            })
        }

        val jsonStr = root.toString(2)
        val parsed = JSONObject(jsonStr)

        assertEquals(2, parsed.getInt("version"))
        assertEquals("matrix_code", parsed.getString("wallpaperId"))
        assertEquals(1, parsed.getJSONArray("shortcuts").length())
        assertEquals(
            "https://pfxplayer.online/v/31d20a56-5c2c-4e02-a6a2-198dc3582124",
            parsed.getJSONArray("shortcuts").getJSONObject(0).getString("urlOrPackage")
        )
    }
}
