package com.example.oh100.Object

class MyInfo {
    private var myId: String = ""
    private var myProfileImageUrl: String? = null
    private var mySolvedCount: Int = 0
    private var myTier: Int = 0
    private var myRank: Int = 0
    private var myMaxStreak: Int = 0

    constructor(
        myId: String,
        myProfileImageUrl: String?,
        mySolvedCount: Int,
        myTier: Int,
        myRank: Int,
        myMaxStreak: Int
    ) {
        this.myId = myId
        this.myProfileImageUrl = myProfileImageUrl
        this.mySolvedCount = mySolvedCount
        this.myTier = myTier
        this.myRank = myRank
        this.myMaxStreak = myMaxStreak
    }

    fun getMyId(): String {
        return myId
    }

    fun getMyProfileImageUrl(): String? {
        return myProfileImageUrl
    }

    fun getMySolvedCount(): Int {
        return mySolvedCount
    }

    fun getMyTier(): Int {
        return myTier
    }

    fun getMyRank(): Int {
        return myRank
    }

    fun getMyMaxStreak(): Int {
        return myMaxStreak
    }
}