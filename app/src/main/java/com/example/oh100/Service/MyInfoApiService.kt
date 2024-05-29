package com.example.oh100.Service

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


data class MyInfoResponse(
    @SerializedName("profileImageUrl") val profileImageUrl: String?,
    @SerializedName("solvedCount") val solvedCount: Int,
    @SerializedName("tier") val tier: Int,
    @SerializedName("rank") val rank: Int,
    @SerializedName("maxStreak") val makStreak: Int
)

data class MyInfoApiResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("items") val items: List<MyInfoResponse>
)

interface MyInfoApiService {
    @GET("/api/v3/search/user")
    fun getMyInfo(@Query("query") userId: String): Call<MyInfoApiResponse>
}