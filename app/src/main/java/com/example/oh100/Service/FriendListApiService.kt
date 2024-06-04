package com.example.oh100.Service

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

data class FriendListResponse(
    @SerializedName("tier") val tier: Int,
    @SerializedName("solvedCount") val solvedCount: Int,
    @SerializedName("profileImageUrl") val profileImageUrl: String?
)

data class FriendListApiResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("items") val items: List<FriendListResponse>
)

interface FriendListApiService {
    @GET("/api/v3/search/user")
    fun getUserData(@Query("query") userId: String): Call<FriendListApiResponse>
}
