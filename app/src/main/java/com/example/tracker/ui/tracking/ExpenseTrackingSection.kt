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
internal fun ExpenseTrackingSection(state: TrackingUiState, viewModel: TrackingViewModel) {
    var expandedCategoryId by remember { mutableStateOf<Long?>(null) }

    TrackingSection("지출") {
        TrackingSubheading("카테고리별 기록 트래킹")
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
            EmptyResult("카테고리를 선택하면 날짜별 서브카테고리 기록을 보여줍니다")
        } else if (state.expenseSubCategories.isEmpty()) {
            EmptyResult("이 카테고리에 등록된 서브카테고리가 없음")
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

        TrackingSubheading("카테고리별 지출 비율")
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

        TrackingSubheading("날짜별 전체 소비 금액")
        if (state.calcDailyExpense.isEmpty()) {
            EmptyResult("선택한 기간의 지출 기록이 없음")
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
