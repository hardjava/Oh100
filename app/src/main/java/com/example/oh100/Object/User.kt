package com.example.oh100.Object

class User {
    private var userId: String = ""
    private var solvedCount: Int = 0
    private var tier: Int = 0
    private var profileImageUrl: String? = null

    constructor(userId: String) {
        this.userId = userId
    }

    constructor(userId: String, solvedCount: Int) {
        this.userId = userId
        this.solvedCount = solvedCount
    }

    constructor(userId: String, solvedCount: Int, tier: Int, profileImageUrl: String?) {
        this.userId = userId
        this.solvedCount = solvedCount
        this.tier = tier
        this.profileImageUrl = profileImageUrl
    }

    fun getUserId(): String {
        return userId
    }

    fun getSolvedCount(): Int {
        return solvedCount
    }

    fun getProfileImageUrl(): String? {
        return profileImageUrl
    }
}