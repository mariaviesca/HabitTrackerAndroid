package com.habittrackerapp.model

import java.time.LocalDate
import java.util.UUID

data class Coupon(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val icon: String = "🍕"
) {
    companion object {
        val starters = listOf(
            Coupon(title = "Pizza night", icon = "🍕"),
            Coupon(title = "Movie night", icon = "🎬"),
            Coupon(title = "Fancy coffee", icon = "☕"),
            Coupon(title = "Small treat", icon = "🛍️"),
        )
    }
}

data class EarnedCoupon(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val icon: String = "",
    val earnedAt: LocalDate = LocalDate.now(),
    val redeemedAt: LocalDate? = null
) {
    val isRedeemed: Boolean get() = redeemedAt != null
}
