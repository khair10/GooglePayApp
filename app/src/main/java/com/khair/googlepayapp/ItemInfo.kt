package com.khair.googlepayapp

import androidx.annotation.DrawableRes

data class ItemInfo(val name: String,
                    val priceRubles: Int,
                    @DrawableRes
                    val imageResourceId: Int)