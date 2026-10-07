package com.example.tracker.ui.tracking

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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tracker.data.entity.ConditionDefinition
import com.example.tracker.data.entity.ConditionTag
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import kotlin.math.atan2
import kotlin.math.hypot

@Composable
internal fun TrackingSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    val description = when (title) {
        "지출" -> "카테고리와 날짜를 기준으로 소비 흐름을 비교합니다."
        "해빗" -> "프로젝트별 실천 정도와 습관의 지속성을 확인합니다."
        "컨디션" -> "증상과 환경 태그가 함께 나타난 패턴을 살펴봅니다."
        else -> "선택한 기록의 변화를 확인합니다."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE1E1DE)),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0EFED), MaterialTheme.shapes.small)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                HorizontalDivider(color = Color(0xFFD7D6D2))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF777772),
                    maxLines = 2
                )
            }
            content()
        }
    }
}

@Composable
internal fun TrackingSubheading(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F4F2), MaterialTheme.shapes.small)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF666662),
        maxLines = 1
    )
}

@Composable
internal fun ChoiceRow(content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), content = content)
}

@Composable
internal fun ValueRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}


@Composable
internal fun TimelineTable(
    dates: List<String>,
    rowNames: List<String>,
    isDateActive: (String) -> Boolean = { true },
    cellText: (rowIndex: Int, date: String) -> String
) {
    if (rowNames.isEmpty()) {
        EmptyResult("먼저 항목을 선택하세요")
        return
    }
    if (dates.isEmpty()) return

    val cellWidth = 92.dp
    Row(Modifier.horizontalScroll(rememberScrollState())) {
        Column {
            Row {
                TimelineCell("항목 / 날짜", 120.dp, true)
                dates.forEach { date ->
                    val active = isDateActive(date)
                    TimelineCell(
                        text = date.drop(5),
                        width = cellWidth,
                        heading = true,
                        containerColor = if (active) null else Color(0xFFE8DDD8),
                        contentAlpha = if (active) 1f else 0.55f
                    )
                }
            }
            rowNames.forEachIndexed { rowIndex, rowName ->
                Row {
                    TimelineCell(rowName, 120.dp, true)
                    dates.forEach { date ->
                        val active = isDateActive(date)
                        TimelineCell(
                            text = if (active) cellText(rowIndex, date) else "",
                            width = cellWidth,
                            heading = false,
                            containerColor = if (active) null else Color(0xFFE8DDD8),
                            contentAlpha = if (active) 1f else 0.55f
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun TimelineCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    heading: Boolean,
    containerColor: Color? = null,
    contentAlpha: Float = 1f
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(52.dp)
            .padding(2.dp)
            .background(
                containerColor ?: if (heading) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(6.dp)
            )
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun EmptyResult(message: String) {
    Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

internal fun datesBetween(start: String, end: String): List<String> {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { isLenient = false }
    val startDate = runCatching { format.parse(start) }.getOrNull() ?: return emptyList()
    val endDate = runCatching { format.parse(end) }.getOrNull() ?: return emptyList()
    if (startDate.after(endDate)) return emptyList()

    val calendar = Calendar.getInstance().apply { time = startDate }
    return buildList {
        while (!calendar.time.after(endDate) && size < 366) {
            add(format.format(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }
}
