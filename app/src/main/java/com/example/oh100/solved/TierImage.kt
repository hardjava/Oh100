package com.example.oh100.solved

import android.content.Context
import android.widget.ImageView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.load
import coil.size.Scale

object TierImage {
    fun load(context: Context, imageView: ImageView, problem_level : Int?) {
        val imageUrl = "https://static.solved.ac/tier_small/$problem_level.svg"

        val imageLoader = ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()

        imageView.load(imageUrl, imageLoader) {
            scale(Scale.FIT)
        }
    }
}