package com.habittrackerapp.viewmodel

import android.app.Application
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import com.google.gson.reflect.TypeToken
import com.habittrackerapp.model.*
import com.habittrackerapp.util.LocalDateAdapter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale

class HabitStore(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences =
        application.getSharedPreferences("habit_tracker", 0)
    private val gson = com.google.gson.GsonBuilder()
        .registerTypeAdapter(java.time.LocalDate::class.java, LocalDateAdapter())
        .create()

    var habits by mutableStateOf(listOf<Habit>())
        private set
    var categories by mutableStateOf(Category.defaults)
        private set
    var achievements by mutableStateOf(Achievement.all)
        private set
    var missedWeeks by mutableStateOf(listOf<MissedWeek>())
        private set
    var habitScore by mutableIntStateOf(100)
        private set
    var newlyUnlocked by mutableStateOf<Achievement?>(null)

    var couponPool by mutableStateOf(Coupon.starters)
        private set
    var earnedCoupons by mutableStateOf(listOf<EarnedCoupon>())
        private set
    var pendingGiftBoxes by mutableIntStateOf(0)
        private set

    var monthlyTrophy by mutableStateOf<MonthlyTrophy?>(null)
        private set
    var wonTrophies by mutableStateOf(listOf<WonTrophy>())
        private set
    var newlyWonTrophy by mutableStateOf<WonTrophy?>(null)

    init {
        load()
        consumeFreezesForMissedDays()
        checkForMissedWeeks()
        checkForPerfectWeek()
        checkForPerfectMonth()
        NotificationHelper.rescheduleAll(application, habits)
    }

    // MARK: - Habits

    fun addHabit(habit: Habit) {
        habits = habits + habit
        NotificationHelper.schedule(getApplication(), habit)
        saveAndEvaluate()
    }

    fun updateHabit(habit: Habit) {
        habits = habits.map { if (it.id == habit.id) habit else it }
        NotificationHelper.schedule(getApplication(), habit)
        saveAndEvaluate()
    }

    fun deleteHabit(habit: Habit) {
        NotificationHelper.cancel(getApplication(), habit)
        habits = habits.filter { it.id != habit.id }
        saveAndEvaluate()
    }

    fun toggleCompletion(habit: Habit, date: LocalDate = LocalDate.now()) {
        val idx = habits.indexOfFirst { it.id == habit.id }
        if (idx < 0) return
        val h = habits[idx]
        val entryIdx = h.entries.indexOfFirst { it.date == date }
        val newEntries: List<HabitEntry>
        if (entryIdx >= 0) {
            val wasCompleted = h.entries[entryIdx].completed
            newEntries = h.entries.toMutableList().also {
                it[entryIdx] = it[entryIdx].copy(completed = !wasCompleted)
            }
            habitScore += if (wasCompleted) -1 else 1
        } else {
            newEntries = h.entries + HabitEntry(date = date, completed = true)
            habitScore += 1
        }
        habitScore = maxOf(-999, habitScore)
        val updated = h.copy(entries = newEntries)
        habits = habits.toMutableList().also { it[idx] = updated }
        awardStreakFreezes(idx)
        saveAndEvaluate()
    }

    // MARK: - Streak freezes

    private fun awardStreakFreezes(idx: Int) {
        val h = habits[idx]
        if (!h.frequency.isDaily) return
        val earnable = Habit.freezesEarnable(h.currentStreak)
        if (earnable > h.freezesEarnedThisStreak) {
            val newFreezes = h.streakFreezesAvailable + (earnable - h.freezesEarnedThisStreak)
            habits = habits.toMutableList().also {
                it[idx] = it[idx].copy(
                    streakFreezesAvailable = newFreezes,
                    freezesEarnedThisStreak = earnable
                )
            }
        }
    }

    private fun consumeFreezesForMissedDays() {
        val today = LocalDate.now()
        val mutableHabits = habits.toMutableList()
        for (idx in mutableHabits.indices) {
            val h = mutableHabits[idx]
            if (!h.frequency.isDaily) continue
            val covered = h.entries.filter { it.completed }.map { it.date }.toSet() +
                    h.frozenDates.toSet()
            val lastCovered = covered.filter { it.isBefore(today) }.maxOrNull() ?: continue

            val gapDays = mutableListOf<LocalDate>()
            var day = lastCovered.plusDays(1)
            while (day.isBefore(today)) {
                gapDays.add(day)
                day = day.plusDays(1)
            }
            if (gapDays.isEmpty()) continue

            if (gapDays.size <= h.streakFreezesAvailable) {
                mutableHabits[idx] = h.copy(
                    streakFreezesAvailable = h.streakFreezesAvailable - gapDays.size,
                    frozenDates = h.frozenDates + gapDays
                )
            } else {
                mutableHabits[idx] = h.copy(freezesEarnedThisStreak = 0)
            }
        }
        habits = mutableHabits
        save()
    }

    // MARK: - Categories

    fun category(habit: Habit): Category? =
        categories.firstOrNull { it.id == habit.categoryID }

    fun addCategory(category: Category) {
        categories = categories + category
        save()
    }

    fun habitsForCategory(category: Category): List<Habit> =
        habits.filter { it.categoryID == category.id }

    // MARK: - Stats

    fun completionRate(habit: Habit, days: Int = 30): Double {
        val today = LocalDate.now()
        var completed = 0
        var activeDays = 0
        for (offset in 0 until days) {
            val date = today.minusDays(offset.toLong())
            if (!habit.isActive(date)) continue
            activeDays++
            if (habit.isCompleted(date)) completed++
        }
        return if (activeDays > 0) completed.toDouble() / activeDays else 0.0
    }

    val unlockedCount: Int get() = achievements.count { it.isUnlocked }

    val scoreColor: Color
        get() = when {
            habitScore >= 150 -> Color(0xFF34C759)
            habitScore >= 80 -> Color(0xFF007AFF)
            habitScore >= 0 -> Color(0xFFFF9500)
            else -> Color(0xFFFF3B30)
        }

    val scoreGrade: String
        get() = when {
            habitScore >= 150 -> "S"
            habitScore >= 120 -> "A"
            habitScore >= 90 -> "B"
            habitScore >= 60 -> "C"
            habitScore >= 0 -> "D"
            else -> "F"
        }

    // MARK: - Missed week detection

    private fun checkForMissedWeeks() {
        val today = LocalDate.now()
        val lastWeekAnchor = today.minusWeeks(1)
        val wf = WeekFields.of(Locale.getDefault())
        val thisWeekStart = today.with(wf.dayOfWeek(), 1)

        val savedWeek = prefs.getString("last_checked_week", null)
        if (savedWeek != null && savedWeek == thisWeekStart.toString()) return
        prefs.edit().putString("last_checked_week", thisWeekStart.toString()).apply()

        val mutableMissed = missedWeeks.toMutableList()
        var score = habitScore
        for (habit in habits) {
            if (habit.frequency.isDaily) continue
            if (!habit.metGoal(lastWeekAnchor)) {
                val weekStart = lastWeekAnchor.with(wf.dayOfWeek(), 1)
                val alreadyRecorded = mutableMissed.any {
                    it.habitID == habit.id && it.weekStarting == weekStart
                }
                if (!alreadyRecorded) {
                    val priorMisses = consecutiveMisses(habit, weekStart, mutableMissed)
                    val penalty = listOf(15, 25, 40)[minOf(priorMisses, 2)]
                    mutableMissed.add(
                        MissedWeek(
                            habitID = habit.id,
                            habitName = habit.name,
                            habitIcon = habit.icon,
                            weekStarting = weekStart,
                            points = penalty
                        )
                    )
                    score -= penalty
                }
            }
        }
        habitScore = maxOf(-999, score)
        missedWeeks = mutableMissed
        save()
    }

    // MARK: - Rewards

    private fun checkForPerfectWeek() {
        val today = LocalDate.now()
        val wf = WeekFields.of(Locale.getDefault())
        val lastWeek = today.minusWeeks(1)
        val lastWeekStart = lastWeek.with(wf.dayOfWeek(), 1)
        val thisWeekStart = today.with(wf.dayOfWeek(), 1)

        val savedWeek = prefs.getString("last_reward_week", null)
        if (savedWeek != null && savedWeek == thisWeekStart.toString()) return
        prefs.edit().putString("last_reward_week", thisWeekStart.toString()).apply()

        val days = (0 until 7).map { lastWeekStart.plusDays(it.toLong()) }
        var hadAnyRequirement = false

        for (habit in habits) {
            if (habit.frequency.isDaily) {
                val activeDays = days.filter { habit.isActive(it) }
                if (activeDays.isEmpty()) continue
                hadAnyRequirement = true
                if (!activeDays.all { habit.isCompleted(it) || habit.isFrozen(it) }) return
            } else {
                if (!habit.isActive(lastWeekStart.plusDays(6))) continue
                hadAnyRequirement = true
                if (!habit.metGoal(lastWeek)) return
            }
        }
        if (!hadAnyRequirement) return
        pendingGiftBoxes++
        save()
    }

    fun openGiftBox(): EarnedCoupon? {
        if (pendingGiftBoxes <= 0 || couponPool.isEmpty()) return null
        val drawn = couponPool.random()
        pendingGiftBoxes--
        val earned = EarnedCoupon(title = drawn.title, icon = drawn.icon)
        earnedCoupons = listOf(earned) + earnedCoupons
        save()
        return earned
    }

    fun redeemCoupon(coupon: EarnedCoupon) {
        earnedCoupons = earnedCoupons.map {
            if (it.id == coupon.id) it.copy(redeemedAt = LocalDate.now()) else it
        }
        save()
    }

    fun addCoupon(coupon: Coupon) {
        couponPool = couponPool + coupon
        save()
    }

    fun deleteCoupon(coupon: Coupon) {
        couponPool = couponPool.filter { it.id != coupon.id }
        save()
    }

    // MARK: - Monthly trophy

    fun isPerfectDay(day: LocalDate): Boolean {
        val active = habits.filter { it.frequency.isDaily && it.isActive(day) }
        if (active.isEmpty()) return false
        return active.all { it.isCompleted(day) || it.isFrozen(day) }
    }

    fun perfectDaysCount(month: LocalDate): Int {
        val start = month.withDayOfMonth(1)
        val end = start.plusMonths(1)
        val today = LocalDate.now()
        var count = 0
        var day = start
        while (day.isBefore(end) && !day.isAfter(today)) {
            if (isPerfectDay(day)) count++
            day = day.plusDays(1)
        }
        return count
    }

    private fun checkForPerfectMonth() {
        val trophy = monthlyTrophy ?: return
        val today = LocalDate.now()
        val lastMonth = today.minusMonths(1)
        val thisMonthStart = today.withDayOfMonth(1)

        val savedMonth = prefs.getString("last_trophy_month", null)
        if (savedMonth != null && savedMonth == thisMonthStart.toString()) return
        prefs.edit().putString("last_trophy_month", thisMonthStart.toString()).apply()

        if (perfectDaysCount(lastMonth) < trophy.targetDays) return

        val won = WonTrophy(
            title = trophy.title,
            icon = trophy.icon,
            month = lastMonth.withDayOfMonth(1)
        )
        wonTrophies = listOf(won) + wonTrophies
        newlyWonTrophy = won
        save()
    }

    fun setTrophy(trophy: MonthlyTrophy?) {
        monthlyTrophy = trophy
        save()
    }

    private fun consecutiveMisses(
        habit: Habit, weekStart: LocalDate, missedList: List<MissedWeek>
    ): Int {
        var count = 0
        var anchor = weekStart
        while (true) {
            val prev = anchor.minusWeeks(1)
            val found = missedList.any {
                it.habitID == habit.id && it.weekStarting == prev
            }
            if (!found) break
            count++
            anchor = prev
        }
        return count
    }

    // MARK: - Achievements

    private fun evaluateAchievements() {
        val before = achievements.filter { it.isUnlocked }.map { it.id }.toSet()
        achievements = Achievement.evaluate(habits, achievements)
        val after = achievements.filter { it.isUnlocked }.map { it.id }.toSet()
        val firstNew = (after - before).firstOrNull()
        if (firstNew != null) {
            newlyUnlocked = achievements.firstOrNull { it.id == firstNew }
        }
    }

    // MARK: - Persistence

    private fun saveAndEvaluate() {
        save()
        evaluateAchievements()
    }

    private fun save() {
        prefs.edit().apply {
            putString("habits", gson.toJson(habits))
            putString("categories", gson.toJson(categories))
            putString("achievements", gson.toJson(achievements))
            putString("missed_weeks", gson.toJson(missedWeeks))
            putInt("habit_score", habitScore)
            putString("coupon_pool", gson.toJson(couponPool))
            putString("earned_coupons", gson.toJson(earnedCoupons))
            putInt("gift_boxes", pendingGiftBoxes)
            if (monthlyTrophy != null) {
                putString("monthly_trophy", gson.toJson(monthlyTrophy))
            } else {
                remove("monthly_trophy")
            }
            putString("won_trophies", gson.toJson(wonTrophies))
            apply()
        }
    }

    private fun load() {
        prefs.getString("habits", null)?.let {
            habits = gson.fromJson(it, object : TypeToken<List<Habit>>() {}.type) ?: emptyList()
        }
        prefs.getString("categories", null)?.let {
            categories = gson.fromJson(it, object : TypeToken<List<Category>>() {}.type)
                ?: Category.defaults
        }
        prefs.getString("achievements", null)?.let {
            achievements = gson.fromJson(it, object : TypeToken<List<Achievement>>() {}.type)
                ?: Achievement.all
        } ?: run { achievements = Achievement.all }
        prefs.getString("missed_weeks", null)?.let {
            missedWeeks = gson.fromJson(it, object : TypeToken<List<MissedWeek>>() {}.type)
                ?: emptyList()
        }
        prefs.getString("coupon_pool", null)?.let {
            couponPool = gson.fromJson(it, object : TypeToken<List<Coupon>>() {}.type)
                ?: Coupon.starters
        }
        prefs.getString("earned_coupons", null)?.let {
            earnedCoupons = gson.fromJson(it, object : TypeToken<List<EarnedCoupon>>() {}.type)
                ?: emptyList()
        }
        pendingGiftBoxes = prefs.getInt("gift_boxes", 0)
        prefs.getString("monthly_trophy", null)?.let {
            monthlyTrophy = gson.fromJson(it, MonthlyTrophy::class.java)
        }
        prefs.getString("won_trophies", null)?.let {
            wonTrophies = gson.fromJson(it, object : TypeToken<List<WonTrophy>>() {}.type)
                ?: emptyList()
        }
        habitScore = if (prefs.contains("habit_score")) prefs.getInt("habit_score", 100) else 100
    }
}
