package com.example.expensetracker.features.intro

import androidx.annotation.DrawableRes

/**
 * SlideModel — dữ liệu cho mỗi trang trong màn hình Intro.
 */
data class SlideModel(
    @DrawableRes val iconRes: Int,
    @DrawableRes val iconBgRes: Int,
    @DrawableRes val buttonBgRes: Int,
    val dotColor: Int,
    val title: String,
    val description: String,
    val isLastSlide: Boolean = false
)
