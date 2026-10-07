package com.example.tracker.ui.tracking

import androidx.compose.foundation.background
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
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
internal fun HabitTrackingSection(state: TrackingUiState, viewModel: TrackingViewModel) {
    TrackingSection("해빗") {
        TrackingSubheading("프로젝트 선택")
        if (state.habitProjects.isEmpty()) {
            EmptyResult("선택한 기간과 겹치는 프로젝트가 없음")
        } else {
            ChoiceRow {
                items(state.habitProjects) { project ->
                    FilterChip(
                        selected = state.selectedHabitProject?.id == project.id,
                        onClick = { viewModel.projectTracking(project) },
                        label = { Text(project.name) }
                    )
                }
            }
        }

        state.selectedHabitProject?.let { project ->
            TrackingSubheading("${project.name} 월간 실천 기록")
            Text(
                text = "프로젝트 기간: ${project.startDate ?: "제한 없음"} ~ ${project.endDate ?: "제한 없음"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HabitMonthlyCalendar(
                startDate = state.startDate,
                endDate = state.endDate,
                projectStartDate = project.startDate,
                projectEndDate = project.endDate,
                totalHabitCount = state.habitDefinitions.size,
                countsByDate = state.projectTracking.associate {
                    it.date to it.countOfRecord
                }
            )

            TrackingSubheading("해빗별 실천 기록")
            TimelineTable(
                dates = datesBetween(state.startDate, state.endDate),
                rowNames = state.habitDefinitions.map { it.name },
                isDateActive = { date ->
                    (project.startDate == null || date >= project.startDate) &&
                        (project.endDate == null || date <= project.endDate)
                },
                cellText = { rowIndex, date ->
                    val definitionId = state.habitDefinitions[rowIndex].id
                    if (state.definitionTracking.any {
                            it.habitRecord.habitDefinitionId == definitionId &&
                                it.habitRecord.date == date &&
                                it.habitRecord.checked
                        }) "●" else ""
                }
            )
        }
    }
}

internal data class CalendarMonth(val year: Int, val month: Int)

@Composable
internal fun HabitMonthlyCalendar(
    startDate: String,
    endDate: String,
    projectStartDate: String?,
    projectEndDate: String?,
    totalHabitCount: Int,
    countsByDate: Map<String, Int>
) {
    val months = calendarMonthsBetween(startDate, endDate)
    if (months.isEmpty()) {
        EmptyResult("조회 기간을 확인하세요")
        return
    }

    val dateFormat = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { isLenient = false }
    }
    val total = max(1, totalHabitCount)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(12.dp)
                .background(Color(0xFFE8DDD8), RoundedCornerShape(3.dp))
        )
        Text("프로젝트 기간 밖", style = MaterialTheme.typography.labelSmall)
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(months) { month ->
            Card(modifier = Modifier.width(320.dp)) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${month.year}년 ${month.month + 1}월",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(Modifier.fillMaxWidth()) {
                        listOf("월", "화", "수", "목", "금", "토", "일").forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    val calendar = Calendar.getInstance().apply {
                        set(month.year, month.month, 1)
                    }
                    val firstDayOffset = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
                    val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val cells = List(firstDayOffset) { 0 } + (1..lastDay).toList()

                    cells.chunked(7).forEach { week ->
                        Row(Modifier.fillMaxWidth()) {
                            (week + List(7 - week.size) { 0 }).forEach { day ->
                                if (day == 0) {
                                    Spacer(Modifier.weight(1f).height(48.dp))
                                } else {
                                    val date = String.format(
                                        Locale.getDefault(),
                                        "%04d-%02d-%02d",
                                        month.year,
                                        month.month + 1,
                                        day
                                    )
                                    val inTrackingPeriod = date >= startDate && date <= endDate
                                    val inProjectPeriod =
                                        (projectStartDate == null || date >= projectStartDate) &&
                                            (projectEndDate == null || date <= projectEndDate)
                                    val isActiveDate = inTrackingPeriod && inProjectPeriod
                                    val count = countsByDate[date] ?: 0
                                    val strength = count.toFloat() / total.toFloat()
                                    val background = when {
                                        !inTrackingPeriod -> MaterialTheme.colorScheme.surfaceVariant
                                        !inProjectPeriod -> Color(0xFFE8DDD8)
                                        count == 0 -> MaterialTheme.colorScheme.surfaceVariant
                                        else -> MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.16f + strength * 0.84f
                                        )
                                    }

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .padding(2.dp)
                                            .alpha(
                                                when {
                                                    !inTrackingPeriod -> 0.28f
                                                    !inProjectPeriod -> 1f
                                                    else -> 1f
                                                }
                                            )
                                            .background(background, RoundedCornerShape(7.dp)),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(day.toString(), style = MaterialTheme.typography.labelMedium)
                                        if (isActiveDate && count > 0) {
                                            Text(
                                                "$count/$totalHabitCount",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun calendarMonthsBetween(start: String, end: String): List<CalendarMonth> {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { isLenient = false }
    val startValue = runCatching { format.parse(start) }.getOrNull() ?: return emptyList()
    val endValue = runCatching { format.parse(end) }.getOrNull() ?: return emptyList()
    if (startValue.after(endValue)) return emptyList()

    val current = Calendar.getInstance().apply {
        time = startValue
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val last = Calendar.getInstance().apply {
        time = endValue
        set(Calendar.DAY_OF_MONTH, 1)
    }

    return buildList {
        while (!current.after(last) && size < 120) {
            add(CalendarMonth(current.get(Calendar.YEAR), current.get(Calendar.MONTH)))
            current.add(Calendar.MONTH, 1)
        }
    }
}
