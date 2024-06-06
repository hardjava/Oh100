package com.example.oh100.solved

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class Problem {
    private var problem : JsonObject? = null;

    suspend fun init(problem_number : Int) {
        withContext(Dispatchers.IO) {
            val client = OkHttpClient()

            val request = Request.Builder()
                .url("https://solved.ac/api/v3/problem/show?problemId=" + problem_number.toString())
                .get()
                .addHeader("x-solvedac-language", "")
                .addHeader("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null)
                    problem = Gson().fromJson(responseBody, JsonObject::class.java)
            }
        }
    }

    fun getJSON() : JsonObject? {
        return problem
    }

    fun getTitle() : String? {
        return problem?.get("titleKo")?.getAsString()
    }

    fun getLevel() : Int? {
        return problem?.get("level")?.getAsInt()
    }
}