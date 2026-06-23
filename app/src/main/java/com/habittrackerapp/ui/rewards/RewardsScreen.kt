@file:OptIn(ExperimentalLayoutApi::class)
package com.habittrackerapp.ui.rewards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.habittrackerapp.model.*
import com.habittrackerapp.ui.components.ConfettiEffect
import com.habittrackerapp.viewmodel.HabitStore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RewardsScreen(store: HabitStore) {
    var showAddCoupon by remember { mutableStateOf(false) }
    var showSetTrophy by remember { mutableStateOf(false) }
    var showBoxOpening by remember { mutableStateOf(false) }
    var trophyConfetti by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Rewards") }) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Trophy won banner
                store.newlyWonTrophy?.let { won ->
                    TrophyWonBanner(won, onDismiss = { store.newlyWonTrophy = null })
                    LaunchedEffect(won) { trophyConfetti = true }
                }

                // Gift box banner
                if (store.pendingGiftBoxes > 0) {
                    GiftBoxBanner(store.pendingGiftBoxes) { showBoxOpening = true }
                } else {
                    HowItWorksCard()
                }

                // Monthly trophy
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Monthly trophy", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                        TextButton(onClick = { showSetTrophy = true }) {
                            Text(if (store.monthlyTrophy == null) "Set up" else "Edit")
                        }
                    }
                    store.monthlyTrophy?.let { trophy ->
                        TrophyProgressCard(trophy, store.perfectDaysCount(LocalDate.now()))
                    } ?: Text(
                        "Promise yourself something big — like a weekend trip — for a (nearly) perfect month. 🏆",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    store.wonTrophies.forEach { won ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFFCC00).copy(alpha = 0.1f)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(won.icon, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(won.title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                                    Text(
                                        won.month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text("🏆", fontSize = 20.sp)
                            }
                        }
                    }
                }

                // Earned coupons
                if (store.earnedCoupons.isNotEmpty()) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text("Your coupons", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(10.dp))
                        store.earnedCoupons.forEach { coupon ->
                            EarnedCouponCard(coupon) { store.redeemCoupon(coupon) }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                // Coupon pool
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Reward ideas", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                        IconButton(onClick = { showAddCoupon = true }) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Add Reward")
                        }
                    }
                    Text(
                        "Mystery boxes draw a random reward from this list.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    store.couponPool.forEach { coupon ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(coupon.icon, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(coupon.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
                                IconButton(onClick = { store.deleteCoupon(coupon) }) {
                                    Icon(Icons.Default.RemoveCircle, contentDescription = "Remove", tint = Color.Red.copy(alpha = 0.7f))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    if (store.couponPool.isEmpty()) {
                        Text(
                            "Add at least one reward idea so your next gift box has something inside! 🎁",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF9500)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        ConfettiEffect(trigger = trophyConfetti, onFinished = { trophyConfetti = false })
    }

    if (showAddCoupon) {
        AddCouponDialog(store = store, onDismiss = { showAddCoupon = false })
    }

    if (showSetTrophy) {
        SetTrophyDialog(store = store, onDismiss = { showSetTrophy = false })
    }

    if (showBoxOpening) {
        GiftBoxOpeningDialog(store = store, onDismiss = { showBoxOpening = false })
    }
}

@Composable
private fun GiftBoxBanner(count: Int, onOpen: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFAF52DE).copy(alpha = 0.15f)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🎁", fontSize = 44.sp)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (count == 1) "You earned a mystery box!" else "You earned $count mystery boxes!",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text("Perfect week — tap to open 🎉", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("›", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HowItWorksCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🎁", fontSize = 36.sp, modifier = Modifier.then(Modifier))
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text("Earn a mystery box", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                Text(
                    "Complete every habit for a full week and a gift box with a random reward appears here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TrophyWonBanner(trophy: WonTrophy, onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFCC00).copy(alpha = 0.15f)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🏆", fontSize = 40.sp)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("You won your trophy!", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color(0xFFFFCC00))
                Text("${trophy.icon} ${trophy.title} — go enjoy it, you earned it!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TrophyProgressCard(trophy: MonthlyTrophy, perfectDays: Int) {
    val progress = (perfectDays.toFloat() / trophy.targetDays).coerceIn(0f, 1f)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFCC00).copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) { Text(trophy.icon, fontSize = 24.sp) }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(trophy.title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                    Text("$perfectDays of ${trophy.targetDays} perfect days this month", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFFFFCC00),
                trackColor = Color(0xFFFFCC00).copy(alpha = 0.2f),
            )
        }
    }
}

@Composable
private fun EarnedCouponCard(coupon: EarnedCoupon, onRedeem: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                Color(0xFFAF52DE).copy(alpha = if (coupon.isRedeemed) 0.1f else 0.4f),
                RoundedCornerShape(16.dp)
            )
            .then(if (coupon.isRedeemed) Modifier else Modifier)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFAF52DE).copy(alpha = if (coupon.isRedeemed) 0.06f else 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) { Text(coupon.icon, fontSize = 24.sp) }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    coupon.title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (coupon.isRedeemed) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (coupon.isRedeemed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                coupon.redeemedAt?.let {
                    Text("Redeemed ${it.format(DateTimeFormatter.ofPattern("MMM d"))}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                } ?: Text("Won ${coupon.earnedAt.format(DateTimeFormatter.ofPattern("MMM d"))}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!coupon.isRedeemed) {
                Button(
                    onClick = onRedeem,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAF52DE)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Redeem", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
private fun GiftBoxOpeningDialog(store: HabitStore, onDismiss: () -> Unit) {
    var revealed by remember { mutableStateOf<EarnedCoupon?>(null) }
    var showConfetti by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (revealed != null) {
                        Text(revealed!!.icon, fontSize = 80.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("You won", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(revealed!!.title, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Treat yourself — you earned it! 💜", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text("🎁", fontSize = 110.sp, modifier = Modifier.clickable {
                            val coupon = store.openGiftBox()
                            if (coupon != null) {
                                revealed = coupon
                                showConfetti = true
                            } else {
                                onDismiss()
                            }
                        })
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Tap to open!", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                    TextButton(onClick = onDismiss) {
                        Text(if (revealed == null) "Later" else "Done", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                    }
                }
            }
            ConfettiEffect(trigger = showConfetti, onFinished = { showConfetti = false })
        }
    }
}

@Composable
private fun AddCouponDialog(store: HabitStore, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("🍕") }
    val icons = listOf("🍕", "🍣", "🍰", "🍦", "☕", "🍿", "🎬", "🎮", "🎵", "📖", "🛍️", "💆", "💅", "🧖", "🌮", "🍔", "🥂", "🎳", "⛰️", "🏖️")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Reward") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reward") },
                    placeholder = { Text("e.g. Dinner at my favorite place") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Icon", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    icons.forEach { emoji ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (icon == emoji) Color(0xFFAF52DE).copy(alpha = 0.2f)
                                    else Color.Transparent
                                )
                                .clickable { icon = emoji }
                        ) {
                            Text(emoji, fontSize = 22.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    store.addCoupon(Coupon(title = title.trim(), icon = icon))
                    onDismiss()
                },
                enabled = title.trim().isNotEmpty()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SetTrophyDialog(store: HabitStore, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf(store.monthlyTrophy?.title ?: "") }
    var icon by remember { mutableStateOf(store.monthlyTrophy?.icon ?: "🏝️") }
    var targetDays by remember { mutableIntStateOf(store.monthlyTrophy?.targetDays ?: 25) }
    val icons = listOf("🏝️", "✈️", "👟", "👗", "💻", "🎧", "📱", "🎟️", "💎", "🛏️", "🍽️", "🧳", "🚲", "⌚", "📷", "🎸", "🛋️", "🌹", "🎢", "⛷️")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Monthly Trophy") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("The big reward") },
                    placeholder = { Text("e.g. Weekend trip") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Icon", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    icons.forEach { emoji ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (icon == emoji) Color(0xFFFFCC00).copy(alpha = 0.25f)
                                    else Color.Transparent
                                )
                                .clickable { icon = emoji }
                        ) {
                            Text(emoji, fontSize = 22.sp)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Perfect days", modifier = Modifier.weight(1f))
                    IconButton(onClick = { if (targetDays > 15) targetDays-- }) {
                        Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("$targetDays days")
                    IconButton(onClick = { if (targetDays < 31) targetDays++ }) {
                        Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (store.monthlyTrophy != null) {
                    TextButton(onClick = {
                        store.setTrophy(null)
                        onDismiss()
                    }) {
                        Text("Remove trophy goal", color = Color.Red)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    store.setTrophy(MonthlyTrophy(title = title.trim(), icon = icon, targetDays = targetDays))
                    onDismiss()
                },
                enabled = title.trim().isNotEmpty()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
