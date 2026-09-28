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
fun TrackingScreen(
    viewModel: TrackingViewModel,
    modifier: Modifier = Modifier
) {
    val state = viewModel.trackingUiState

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "트래킹",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        item {
            PeriodSelector(
                startDate = state.startDate,
                endDate = state.endDate,
                onStartPast = viewModel::extendStartDateToPast,
                onStartFuture = viewModel::extendStartDateToFuture,
                onEndPast = viewModel::extendEndDateToPast,
                onEndFuture = viewModel::extendEndDateToFuture
            )
        }

        item { ExpenseTrackingSection(state, viewModel) }
        item { HabitTrackingSection(state, viewModel) }
        item { ConditionTrackingSection(state, viewModel) }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun PeriodSelector(
    startDate: String,
    endDate: String,
    onStartPast: () -> Unit,
    onStartFuture: () -> Unit,
    onEndPast: () -> Unit,
    onEndFuture: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("조회 기간", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("시작일", modifier = Modifier.width(44.dp))
                OutlinedButton(onClick = onStartPast) { Text("−") }
                Text(startDate, fontWeight = FontWeight.SemiBold)
                OutlinedButton(onClick = onStartFuture) { Text("+") }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("종료일", modifier = Modifier.width(44.dp))
                OutlinedButton(onClick = onEndPast) { Text("−") }
                Text(endDate, fontWeight = FontWeight.SemiBold)
                OutlinedButton(onClick = onEndFuture) { Text("+") }
            }
        }
    }
}

@Composable
private fun ExpenseTrackingSection(state: TrackingUiState, viewModel: TrackingViewModel) {
    var expandedCategoryId by remember { mutableStateOf<Long?>(null) }

    TrackingSection("지출") {
        Text("카테고리별 기록 트래킹", style = MaterialTheme.typography.titleSmall)
        ChoiceRow {
            items(state.categoryList) { category ->
                FilterChip(
                    selected = state.selectedExpenseTrackingCategory?.id == category.id,
                    onClick = { viewModel.selectCategory(category.id) },
                    label = { Text(category.name) }
                )
            }
        }
        if (state.selectedExpenseTrackingCategory == null) {
            EmptyResult("카테고리를 선택하면 날짜별 서브카테고리 기록을 보여줘")
        } else if (state.expenseSubCategories.isEmpty()) {
            EmptyResult("이 카테고리에 등록된 서브카테고리가 없어")
        } else {
            TimelineTable(
                dates = datesBetween(state.startDate, state.endDate),
                rowNames = state.expenseSubCategories.map { it.name },
                cellText = { rowIndex, date ->
                    val subCategoryId = state.expenseSubCategories[rowIndex].id
                    if (state.expenseTracking.any {
                            it.subCategoryId == subCategoryId && it.date == date
                        }) "●" else ""
                }
            )
        }

        Text("카테고리별 지출 비율", style = MaterialTheme.typography.titleSmall)
        PieChart(
            rows = state.wholeCircleGraphing.map { graph ->
                val category = state.categoryList.firstOrNull { it.id == graph.categoryId }
                BarData(category?.name ?: "카테고리 ${graph.categoryId}", graph.totalPrice)
            },
            onClick = { index ->
                val graph = state.wholeCircleGraphing[index]
                state.categoryList.firstOrNull { it.id == graph.categoryId }?.let {
                    viewModel.selectCategoryForCircleGraph(it)
                    expandedCategoryId = it.id
                }
            }
        )

        state.selectedCategory?.let { category ->
            val expanded = expandedCategoryId == category.id
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expandedCategoryId = if (expanded) null else category.id
                    }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${category.name}의 세부 지출", style = MaterialTheme.typography.titleSmall)
                Text(if (expanded) "▲" else "▼")
            }
            if (expanded) {
                PieChart(
                    rows = state.circleGraphingByCategory.map {
                        BarData(it.subCategoryName, it.totalPrice)
                    }
                )
            }
        }

        Text("날짜별 전체 소비 금액", style = MaterialTheme.typography.titleSmall)
        if (state.calcDailyExpense.isEmpty()) {
            EmptyResult("선택한 기간의 지출 기록이 없어")
        } else {
            DailyExpenseLineChart(
                rows = datesBetween(state.startDate, state.endDate).map { date ->
                    LineData(
                        date = date,
                        value = state.calcDailyExpense
                            .firstOrNull { it.date == date }
                            ?.dailyTotalPrice ?: 0L
                    )
                }
            )
        }
    }
}

@Composable
private fun HabitTrackingSection(state: TrackingUiState, viewModel: TrackingViewModel) {
    TrackingSection("해빗") {
        Text("프로젝트 선택", style = MaterialTheme.typography.titleSmall)
        if (state.habitProjects.isEmpty()) {
            EmptyResult("선택한 기간과 겹치는 프로젝트가 없어")
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
            Text("${project.name} 월간 실천 기록", style = MaterialTheme.typography.titleSmall)
            HabitMonthlyCalendar(
                startDate = state.startDate,
                endDate = state.endDate,
                totalHabitCount = state.habitDefinitions.size,
                countsByDate = state.projectTracking.associate {
                    it.date to it.countOfRecord
                }
            )

            Text("해빗별 실천 기록", style = MaterialTheme.typography.titleSmall)
            TimelineTable(
                dates = datesBetween(state.startDate, state.endDate),
                rowNames = state.habitDefinitions.map { it.name },
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

private data class CalendarMonth(val year: Int, val month: Int)

@Composable
private fun HabitMonthlyCalendar(
    startDate: String,
    endDate: String,
    totalHabitCount: Int,
    countsByDate: Map<String, Int>
) {
    val months = calendarMonthsBetween(startDate, endDate)
    if (months.isEmpty()) {
        EmptyResult("조회 기간을 확인해줘")
        return
    }

    val dateFormat = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { isLenient = false }
    }
    val total = max(1, totalHabitCount)

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
                                    val count = countsByDate[date] ?: 0
                                    val strength = count.toFloat() / total.toFloat()
                                    val background = if (count == 0 || !inTrackingPeriod) {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.16f + strength * 0.84f
                                        )
                                    }

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .padding(2.dp)
                                            .alpha(if (inTrackingPeriod) 1f else 0.28f)
                                            .background(background, RoundedCornerShape(7.dp)),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(day.toString(), style = MaterialTheme.typography.labelMedium)
                                        if (inTrackingPeriod && count > 0) {
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

private fun calendarMonthsBetween(start: String, end: String): List<CalendarMonth> {
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

@Composable
private fun ConditionTrackingSection(state: TrackingUiState, viewModel: TrackingViewModel) {
    var resultMode by remember { mutableIntStateOf(0) }
    var optionType by remember { mutableIntStateOf(0) }
    var keyword by remember { mutableStateOf("") }
    val isDefinition = optionType == 0
    val isTimeline = resultMode == 0
    val options = if (isDefinition) {
        state.conditionDefinitions.map { it.id to it.name }
    } else {
        state.conditionTags.map { it.id to it.name }
    }.filter { (_, name) -> name.contains(keyword.trim(), ignoreCase = true) }
    val selectedIds = when {
        isTimeline && isDefinition -> state.selectedConDefinitions.map { it.id }.toSet()
        isTimeline -> state.selectedConTags.map { it.id }.toSet()
        isDefinition -> setOfNotNull(state.selectedConDefinition?.id)
        else -> setOfNotNull(state.selectedConTag?.id)
    }

    TrackingSection("컨디션") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("날짜별 트래킹", "빈도 비교").forEachIndexed { index, name ->
                FilterChip(
                    selected = resultMode == index,
                    onClick = { resultMode = index },
                    label = { Text(name) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            ConditionSelectorPanel(
                modifier = Modifier.width(145.dp),
                optionType = optionType,
                onOptionTypeChange = {
                    optionType = it
                    keyword = ""
                },
                keyword = keyword,
                onKeywordChange = { keyword = it },
                options = options,
                selectedIds = selectedIds,
                onSelect = { id ->
                    if (isDefinition) {
                        state.conditionDefinitions.firstOrNull { it.id == id }?.let {
                            if (isTimeline) viewModel.selectDefinitions(it)
                            else viewModel.selectDefinition(it)
                        }
                    } else {
                        state.conditionTags.firstOrNull { it.id == id }?.let {
                            if (isTimeline) viewModel.selectTags(it)
                            else viewModel.selectTag(it)
                        }
                    }
                }
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when {
                    isTimeline && isDefinition -> TimelineTable(
                        dates = datesBetween(state.startDate, state.endDate),
                        rowNames = state.selectedConDefinitions.map { it.name },
                        cellText = { rowIndex, date ->
                            val id = state.selectedConDefinitions[rowIndex].id
                            state.trackingByConDefinitions
                                .firstOrNull { it.definitionId == id && it.date == date }
                                ?.tags?.joinToString(", ") { it.name }.orEmpty()
                        }
                    )
                    isTimeline -> TimelineTable(
                        dates = datesBetween(state.startDate, state.endDate),
                        rowNames = state.selectedConTags.map { it.name },
                        cellText = { rowIndex, date ->
                            val id = state.selectedConTags[rowIndex].id
                            state.trackingByConTags
                                .firstOrNull { it.tagId == id && it.date == date }
                                ?.definitions?.joinToString(", ") { it.name }.orEmpty()
                        }
                    )
                    isDefinition -> {
                        Text("선택한 데피니션에 연결된 태그")
                        FrequencyBars(state.graphingTags.map { BarData(it.name, it.count) })
                    }
                    else -> {
                        Text("선택한 태그에 연결된 데피니션")
                        FrequencyBars(state.graphingDefinitions.map { BarData(it.name, it.count) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ConditionSelectorPanel(
    modifier: Modifier = Modifier,
    optionType: Int,
    onOptionTypeChange: (Int) -> Unit,
    keyword: String,
    onKeywordChange: (String) -> Unit,
    options: List<Pair<Long, String>>,
    selectedIds: Set<Long>,
    onSelect: (Long) -> Unit
) {
    Card(modifier) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("데피니션", "태그").forEachIndexed { index, name ->
                    FilterChip(
                        selected = optionType == index,
                        onClick = { onOptionTypeChange(index) },
                        label = { Text(name, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            OutlinedTextField(
                value = keyword,
                onValueChange = onKeywordChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("검색") },
                singleLine = true
            )
            Column(
                modifier = Modifier
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (options.isEmpty()) {
                    EmptyResult("결과 없음")
                } else {
                    options.forEach { (id, name) ->
                        FilterChip(
                            selected = id in selectedIds,
                            onClick = { onSelect(id) },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackingSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
private fun ChoiceRow(content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), content = content)
}

@Composable
private fun ValueRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

private data class BarData(val name: String, val value: Long)
private data class LineData(val date: String, val value: Long)

@Composable
private fun PieChart(rows: List<BarData>, onClick: ((Int) -> Unit)? = null) {
    val visibleRows = rows.withIndex().filter { it.value.value > 0 }
    if (visibleRows.isEmpty()) {
        EmptyResult("원그래프로 표시할 기록이 없어")
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
private fun DailyExpenseLineChart(rows: List<LineData>) {
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

private fun roundToTen(value: Long): Long = ((value + 5L) / 10L) * 10L

@Composable
private fun FrequencyBars(rows: List<BarData>, onClick: ((Int) -> Unit)? = null) {
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

@Composable
private fun TimelineTable(
    dates: List<String>,
    rowNames: List<String>,
    cellText: (rowIndex: Int, date: String) -> String
) {
    if (rowNames.isEmpty()) {
        EmptyResult("먼저 항목을 선택해줘")
        return
    }
    if (dates.isEmpty()) return

    val cellWidth = 92.dp
    Row(Modifier.horizontalScroll(rememberScrollState())) {
        Column {
            Row {
                TimelineCell("항목 / 날짜", 120.dp, true)
                dates.forEach { TimelineCell(it.drop(5), cellWidth, true) }
            }
            rowNames.forEachIndexed { rowIndex, rowName ->
                Row {
                    TimelineCell(rowName, 120.dp, true)
                    dates.forEach { date -> TimelineCell(cellText(rowIndex, date), cellWidth, false) }
                }
            }
        }
    }
}

@Composable
private fun TimelineCell(text: String, width: androidx.compose.ui.unit.Dp, heading: Boolean) {
    Box(
        modifier = Modifier
            .width(width)
            .height(52.dp)
            .padding(2.dp)
            .background(
                if (heading) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(6.dp)
            )
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EmptyResult(message: String) {
    Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

private fun datesBetween(start: String, end: String): List<String> {
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
