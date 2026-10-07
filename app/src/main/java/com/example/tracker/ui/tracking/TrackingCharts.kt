package com.example.tracker.ui.tracking

import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracker.data.entity.ConditionDefinition
import com.example.tracker.data.entity.ConditionTag
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

internal data class BarData(val name: String, val value: Long)
internal data class LineData(val date: String, val value: Long)

@Composable
internal fun PieChart(rows: List<BarData>, onClick: ((Int) -> Unit)? = null) {
    val visibleRows = rows.withIndex().filter { it.value.value > 0 }
    if (visibleRows.isEmpty()) {
        EmptyResult("원그래프로 표시할 기록이 없음")
        return
    }

    val colors = listOf(
        Color(0xFF5C6BC0),
        Color(0xFF26A69A),
        Color(0xFFFFA726),
        Color(0xFFEC407A),
        Color(0xFF7E57C2),
        Color(0xFF66BB6A),
        Color(0xFF42A5F5),
        Color(0xFFFF7043)
    )
    val total = visibleRows.sumOf { it.value.value }.toFloat()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.62f)
                .aspectRatio(1f)
                .pointerInput(visibleRows, onClick) {
                    detectTapGestures { offset ->
                        if (onClick == null) return@detectTapGestures
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val distance = hypot(offset.x - centerX, offset.y - centerY)
                        if (distance > minOf(size.width, size.height) / 2f) {
                            return@detectTapGestures
                        }

                        val rawAngle = Math.toDegrees(
                            atan2(offset.y - centerY, offset.x - centerX).toDouble()
                        ).toFloat()
                        val angleFromTop = (rawAngle + 90f + 360f) % 360f
                        var accumulatedAngle = 0f

                        visibleRows.forEach { indexedRow ->
                            val sweepAngle = indexedRow.value.value / total * 360f
                            if (angleFromTop >= accumulatedAngle &&
                                angleFromTop < accumulatedAngle + sweepAngle
                            ) {
                                onClick(indexedRow.index)
                                return@detectTapGestures
                            }
                            accumulatedAngle += sweepAngle
                        }
                    }
                }
        ) {
            var startAngle = -90f
            visibleRows.forEachIndexed { index, indexedRow ->
                val sweepAngle = indexedRow.value.value / total * 360f
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )

                if (sweepAngle >= 18f) {
                    val middleAngle = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())
                    val labelRadius = size.minDimension * 0.30f
                    val labelX = center.x + cos(middleAngle).toFloat() * labelRadius
                    val labelY = center.y + sin(middleAngle).toFloat() * labelRadius
                    val label = indexedRow.value.name.take(7)
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        labelX,
                        labelY,
                        Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 12.sp.toPx()
                            textAlign = Paint.Align.CENTER
                            isFakeBoldText = true
                        }
                    )
                }
                startAngle += sweepAngle
            }
        }

        visibleRows.forEachIndexed { visibleIndex, indexedRow ->
            val row = indexedRow.value
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (onClick != null) Modifier.clickable { onClick(indexedRow.index) }
                        else Modifier
                    )
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(12.dp)
                            .background(
                                colors[visibleIndex % colors.size],
                                RoundedCornerShape(3.dp)
                            )
                    )
                    Text(row.name)
                }
                Text(
                    text = "${row.value} · ${((row.value / total) * 100).toInt()}%",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
internal fun DailyExpenseLineChart(rows: List<LineData>) {
    if (rows.isEmpty()) return

    val lineColor = MaterialTheme.colorScheme.primary
    val guideColor = MaterialTheme.colorScheme.outlineVariant
    val rawMaximum = rows.maxOf { it.value }
    val maximum = max(10L, ((rawMaximum + 9L) / 10L) * 10L)

    val chartWidth = max(320, rows.size * 72).dp

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier
                .width(56.dp)
                .height(180.dp)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            listOf(
                maximum,
                roundToTen(maximum * 2 / 3),
                roundToTen(maximum / 3),
                0L
            ).forEach { amount ->
                Text(
                    text = "${NumberFormat.getNumberInstance().format(amount)}원",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .width(chartWidth)
                    .height(180.dp)
            ) {
                val horizontalPadding = 12.dp.toPx()
                val verticalPadding = 12.dp.toPx()
                val graphWidth = size.width - horizontalPadding * 2
                val graphHeight = size.height - verticalPadding * 2

                repeat(4) { index ->
                    val y = verticalPadding + graphHeight * index / 3f
                    drawLine(
                        color = guideColor,
                        start = androidx.compose.ui.geometry.Offset(horizontalPadding, y),
                        end = androidx.compose.ui.geometry.Offset(size.width - horizontalPadding, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val points = rows.mapIndexed { index, row ->
                    val x = if (rows.size == 1) {
                        size.width / 2f
                    } else {
                        horizontalPadding + graphWidth * index / (rows.size - 1f)
                    }
                    val y = verticalPadding + graphHeight * (1f - row.value.toFloat() / maximum)
                    androidx.compose.ui.geometry.Offset(x, y)
                }

                points.zipWithNext().forEach { (start, end) ->
                    drawLine(
                        color = lineColor,
                        start = start,
                        end = end,
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                points.forEach { point ->
                    drawCircle(color = lineColor, radius = 4.dp.toPx(), center = point)
                }
            }
            Row(Modifier.width(chartWidth)) {
                rows.forEach { row ->
                    Text(
                        text = row.date.drop(5),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

internal fun roundToTen(value: Long): Long = ((value + 5L) / 10L) * 10L

@Composable
internal fun FrequencyBars(rows: List<BarData>, onClick: ((Int) -> Unit)? = null) {
    if (rows.isEmpty()) {
        EmptyResult("비교할 기록이 없어")
        return
    }

    val maximum = max(1L, rows.maxOf { it.value })
    val chartWidth = max(280, rows.size * 72).dp
    val guideColor = MaterialTheme.colorScheme.outlineVariant
    val barColor = MaterialTheme.colorScheme.primary
    val ticks = listOf(
        maximum,
        (maximum * 2 + 2) / 3,
        (maximum + 2) / 3,
        0L
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Y축: 기록 개수
        Column(
            modifier = Modifier
                .width(32.dp)
                .height(180.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            ticks.forEach { tick ->
                Text(
                    text = tick.toString(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // 그래프와 X축은 항목이 많을 때 함께 가로로 움직인다.
        Column(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(chartWidth)
                    .height(180.dp)
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    repeat(4) { index ->
                        val y = size.height * index / 3f
                        drawLine(
                            color = guideColor,
                            start = androidx.compose.ui.geometry.Offset(0f, y),
                            end = androidx.compose.ui.geometry.Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    drawLine(
                        color = guideColor,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(0f, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    rows.forEachIndexed { index, row ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (onClick != null) Modifier.clickable { onClick(index) }
                                    else Modifier
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = row.value.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Box(
                                Modifier
                                    .fillMaxWidth(0.62f)
                                    .height(
                                        max(
                                            2,
                                            (145f * row.value.toFloat() / maximum.toFloat()).toInt()
                                        ).dp
                                    )
                                    .background(
                                        barColor,
                                        RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)
                                    )
                            )
                        }
                    }
                }
            }

            // X축: 태그 또는 데피니션 이름
            Row(
                modifier = Modifier
                    .width(chartWidth)
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rows.forEach { row ->
                    Text(
                        text = row.name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
