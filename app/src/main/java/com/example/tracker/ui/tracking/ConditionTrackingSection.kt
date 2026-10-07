package com.example.tracker.ui.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
internal fun ConditionTrackingSection(state: TrackingUiState, viewModel: TrackingViewModel) {
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

        ConditionSelectorPanel(
            modifier = Modifier.fillMaxWidth(),
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
            modifier = Modifier.fillMaxWidth(),
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
                    TrackingSubheading("선택한 데피니션에 연결된 태그")
                    FrequencyBars(state.graphingTags.map { BarData(it.name, it.count) })
                }
                else -> {
                    TrackingSubheading("선택한 태그에 연결된 데피니션")
                    FrequencyBars(state.graphingDefinitions.map { BarData(it.name, it.count) })
                }
            }
        }
    }
}

@Composable
internal fun ConditionSelectorPanel(
    modifier: Modifier = Modifier,
    optionType: Int,
    onOptionTypeChange: (Int) -> Unit,
    keyword: String,
    onKeywordChange: (String) -> Unit,
    options: List<Pair<Long, String>>,
    selectedIds: Set<Long>,
    onSelect: (Long) -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F6F2))
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.width(145.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("데피니션", "태그").forEachIndexed { index, name ->
                        val selected = optionType == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        Color.Transparent
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onOptionTypeChange(index) }
                                .padding(horizontal = 2.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = keyword,
                    onValueChange = onKeywordChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("검색") },
                    singleLine = true
                )
            }

            if (options.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    EmptyResult("결과 없음")
                }
            } else {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(options, key = { it.first }) { (id, name) ->
                        FilterChip(
                            selected = id in selectedIds,
                            onClick = { onSelect(id) },
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
