package com.habittrackerapp.model

import java.time.LocalDate

data class DailyQuote(val text: String, val author: String) {
    companion object {
        val all = listOf(
            DailyQuote("We are what we repeatedly do. Excellence, then, is not an act, but a habit.", "Aristotle"),
            DailyQuote("The secret of getting ahead is getting started.", "Mark Twain"),
            DailyQuote("Small daily improvements over time lead to stunning results.", "Robin Sharma"),
            DailyQuote("Motivation is what gets you started. Habit is what keeps you going.", "Jim Ryun"),
            DailyQuote("You don't rise to the level of your goals, you fall to the level of your systems.", "James Clear"),
            DailyQuote("Success is the sum of small efforts, repeated day in and day out.", "Robert Collier"),
            DailyQuote("The difference between who you are and who you want to be is what you do.", "Unknown"),
            DailyQuote("Consistency is the true foundation of trust.", "Roy T. Bennett"),
            DailyQuote("It's not about perfect. It's about effort.", "Jillian Michaels"),
            DailyQuote("Every action you take is a vote for the type of person you wish to become.", "James Clear"),
            DailyQuote("Don't watch the clock; do what it does. Keep going.", "Sam Levenson"),
            DailyQuote("The only bad workout is the one that didn't happen.", "Unknown"),
            DailyQuote("Your habits will determine your future.", "Jack Canfield"),
            DailyQuote("A year from now you may wish you had started today.", "Karen Lamb"),
            DailyQuote("Dream big, start small, act now.", "Robin Sharma"),
        )

        val today: DailyQuote
            get() {
                val dayOfYear = LocalDate.now().dayOfYear
                return all[dayOfYear % all.size]
            }
    }
}
