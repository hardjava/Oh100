package com.example.oh100.solved

import android.widget.ImageView
import coil.load
import coil.size.Scale

object TierImage {
    fun load(imageView: ImageView, problem_number : Int?) {
        val imageUrl = "https://static.solved.ac/tier_small/$problem_number.svg"

        imageView.load(imageUrl) {
            scale(Scale.FIT)
        }
    }
}