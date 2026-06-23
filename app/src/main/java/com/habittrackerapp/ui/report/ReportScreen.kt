package com.habittrackerapp.ui.report

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.habittrackerapp.model.Habit
import com.habittrackerapp.viewmodel.HabitStore
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(store: HabitStore, onDone: () -> Unit) {
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Report") },
                navigationIcon = {
                    TextButton(onClick = onDone) { Text("Done") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Month picker
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Month", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { selectedMonth = selectedMonth.minusMonths(1) }) {
                            Text("◀", fontSize = 20.sp)
                        }
                        Text(
                            selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        val canForward = selectedMonth.isBefore(YearMonth.now())
                        TextButton(onClick = { if (canForward) selectedMonth = selectedMonth.plusMonths(1) }, enabled = canForward) {
                            Text("▶", fontSize = 20.sp)
                        }
                    }
                }
            }

            // Summary
            val firstDay = selectedMonth.atDay(1)
            val daysInMonth = selectedMonth.lengthOfMonth()
            val monthDays = (0 until daysInMonth).map { firstDay.plusDays(it.toLong()) }
            val totalCheckIns = store.habits.sumOf { h ->
                h.activeDays(monthDays).count { h.isCompleted(it) }
            }
            val possible = store.habits.sumOf { it.activeDays(monthDays).size }
            val avgRate = if (possible > 0) (totalCheckIns * 100 / possible) else 0

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "${store.habits.size} habits • $daysInMonth days",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "$totalCheckIns",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF007AFF)
                        )
                        Text(
                            "check-ins • $avgRate% avg",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Generate & share
            Button(
                onClick = { generateAndSharePdf(context, store.habits, selectedMonth) },
                modifier = Modifier.fillMaxWidth(),
                enabled = store.habits.isNotEmpty()
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate & Share PDF Report")
            }
        }
    }
}

private fun generateAndSharePdf(context: Context, habits: List<Habit>, month: YearMonth) {
    val pageWidth = 612
    val pageHeight = 792
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas

    val margin = 36f
    var y = margin

    val titlePaint = Paint().apply { textSize = 20f; isFakeBoldText = true; color = android.graphics.Color.BLACK }
    val subtitlePaint = Paint().apply { textSize = 10f; color = android.graphics.Color.GRAY }
    val headerPaint = Paint().apply { textSize = 8f; isFakeBoldText = true; color = android.graphics.Color.GRAY }
    val bodyPaint = Paint().apply { textSize = 9f; color = android.graphics.Color.BLACK }
    val smallPaint = Paint().apply { textSize = 7f; color = android.graphics.Color.DKGRAY }

    val monthTitle = month.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    canvas.drawText(monthTitle, margin, y + 16, titlePaint)
    y += 28

    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    canvas.drawText("Monthly Habit Report • Generated $today", margin, y + 8, subtitlePaint)
    y += 20

    val linePaint = Paint().apply { color = android.graphics.Color.LTGRAY }
    canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
    y += 12

    val daysInMonth = month.lengthOfMonth()
    val firstDay = month.atDay(1)
    val days = (0 until daysInMonth).map { firstDay.plusDays(it.toLong()) }
    val nameColW = 130f
    val totalColW = 36f
    val rateColW = 44f
    val dayColW = (pageWidth - margin * 2 - nameColW - totalColW - rateColW) / daysInMonth

    canvas.drawText("Habit", margin, y + 6, headerPaint)
    for ((i, day) in days.withIndex()) {
        val x = margin + nameColW + i * dayColW + dayColW / 2 - 4
        canvas.drawText("${day.dayOfMonth}", x, y + 6, Paint().apply { textSize = 6f; color = android.graphics.Color.GRAY })
    }
    y += 14

    val rowH = 18f
    val dotR = 5f

    for ((hIdx, habit) in habits.withIndex()) {
        if (hIdx % 2 == 0) {
            val bgPaint = Paint().apply { color = android.graphics.Color.parseColor("#F5F5F5") }
            canvas.drawRect(margin, y - 2, pageWidth - margin, y + rowH - 2, bgPaint)
        }

        val displayName = "${habit.icon} ${habit.name}"
        canvas.drawText(displayName, margin + 2, y + 10, bodyPaint)

        if (!habit.frequency.isDaily) {
            canvas.drawText(habit.frequency.displayText, margin + 2, y + 16, Paint().apply { textSize = 7f; color = android.graphics.Color.BLUE })
        }

        var total = 0
        var activeDayCount = 0
        for ((i, day) in days.withIndex()) {
            if (!habit.isActive(day)) continue
            activeDayCount++
            val cx = margin + nameColW + i * dayColW + dayColW / 2
            val cy = y + rowH / 2
            val done = habit.isCompleted(day)
            if (done) total++

            val dotPaint = Paint().apply {
                color = if (done) android.graphics.Color.parseColor(habit.colorHex)
                else android.graphics.Color.parseColor("#E0E0E0")
                isAntiAlias = true
            }
            canvas.drawCircle(cx, cy, dotR, dotPaint)
        }

        val totalX = margin + nameColW + daysInMonth * dayColW
        val rate = if (activeDayCount > 0) (total * 100 / activeDayCount) else 0
        canvas.drawText("$total", totalX + 6, y + 10, Paint().apply {
            textSize = 9f; isFakeBoldText = true
            color = if (total > 0) android.graphics.Color.parseColor("#34C759") else android.graphics.Color.GRAY
        })
        canvas.drawText("$rate%", totalX + totalColW + 4, y + 10, Paint().apply {
            textSize = 9f
            color = when {
                rate >= 80 -> android.graphics.Color.parseColor("#34C759")
                rate >= 50 -> android.graphics.Color.parseColor("#FF9500")
                else -> android.graphics.Color.parseColor("#FF3B30")
            }
        })

        y += rowH
    }

    y += 10
    canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
    y += 10
    canvas.drawText("Summary", margin, y + 10, Paint().apply { textSize = 13f; isFakeBoldText = true; color = android.graphics.Color.BLACK })
    y += 18

    val totalEntries = habits.flatMap { it.entries }.count { it.completed }
    canvas.drawText("Total Habits Tracked: ${habits.size}", margin, y + 8, subtitlePaint)
    y += 16
    canvas.drawText("Total Check-ins This Month: $totalEntries", margin, y + 8, subtitlePaint)
    y += 16
    canvas.drawText("Generated by HabitTracker", margin, y + 8, Paint().apply { textSize = 8f; color = android.graphics.Color.LTGRAY })

    document.finishPage(page)

    val file = File(context.cacheDir, "HabitReport_${month}.pdf")
    document.writeTo(FileOutputStream(file))
    document.close()

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Report"))
}
