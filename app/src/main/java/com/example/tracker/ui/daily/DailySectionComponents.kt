package com.example.tracker.ui.daily

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tracker.data.dto.ConditionRecordWithTags
import com.example.tracker.data.dto.HabitGetDailyListDto
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun DateSelector(date: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onPrevious) { Text("이전") }
        Text(date, style = MaterialTheme.typography.titleLarge)
        TextButton(onClick = onNext) { Text("다음") }
    }
}

@Composable
internal fun NotionSection(
    title: String,
    description: String,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE1E1DE)),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0EFED), MaterialTheme.shapes.small)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    action?.invoke()
                }
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
internal fun SectionBand(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF4F3F1), MaterialTheme.shapes.small)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF777772),
            maxLines = 1
        )
    }
}

@Composable
internal fun SoftBlock(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .background(Color(0xFFF1F6F2), MaterialTheme.shapes.medium)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        content = content
    )
}

@Composable
internal fun EmptyMessage(message: String) {
    Text(
        text = message,
        modifier = Modifier.padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
internal fun ExpenseCategoryCard(
    category: ExpenseByCategory,
    modifier: Modifier = Modifier,
    onAddExpense: () -> Unit,
    onEditExpense: (ExpenseDailyRecord) -> Unit,
    onDeleteExpense: (ExpenseDailyRecord) -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F6FA))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.categoryName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                    Text("${NumberFormat.getNumberInstance().format(category.totalPrice)}원")
                }
                TextButton(onClick = onAddExpense) { Text("추가") }
            }
            if (category.recordList.isEmpty()) {
                Text(
                    text = "기록 없음",
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                category.recordList.forEach { record ->
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(record.itemName, maxLines = 1)
                            if (record.memo.isNotBlank()) {
                                Text(record.memo, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            }
                        }
                        Text("${NumberFormat.getNumberInstance().format(record.totalPrice)}원")
                        TextButton(onClick = { onEditExpense(record) }) { Text("수정") }
                        TextButton(onClick = { onDeleteExpense(record) }) { Text("삭제") }
                    }
                }
            }
        }
    }
}

@Composable
internal fun HabitProjectCard(
    project: HabitCategory,
    modifier: Modifier = Modifier,
    selectedDate: String,
    onEditProject: () -> Unit,
    onDeleteProject: () -> Unit,
    onAddHabit: () -> Unit,
    onHabitChecked: (HabitGetDailyListDto) -> Unit,
    onEditHabit: (HabitGetDailyListDto) -> Unit,
    onDeleteHabit: (HabitGetDailyListDto) -> Unit
) {
    val actualHabits = project.habitList.filter { habit ->
        habit.id != null && habit.name != null
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F1F8))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(project.categoryName, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                    Text(
                        text = projectPeriodLabel(project.period, selectedDate),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row {
                    TextButton(onClick = onEditProject) { Text("수정", maxLines = 1) }
                    TextButton(onClick = onDeleteProject) { Text("삭제", maxLines = 1) }
                    TextButton(onClick = onAddHabit) { Text("추가", maxLines = 1) }
                }
            }

            if (actualHabits.isEmpty()) {
                EmptyMessage("등록된 습관이 없어요")
            } else {
                actualHabits.forEach { habit ->
                    val habitName = habit.name ?: return@forEach
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = habit.checked,
                            onCheckedChange = { onHabitChecked(habit) }
                        )
                        Text(text = habitName, modifier = Modifier.weight(1f), maxLines = 1)
                        TextButton(onClick = { onEditHabit(habit) }) { Text("수정") }
                        TextButton(onClick = { onDeleteHabit(habit) }) { Text("삭제") }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ConditionDefinitionRow(
    definition: DefinitionWithFrequency,
    checkedCondition: ConditionRecordWithTags?,
    date: String,
    viewModel: DailyViewModel,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { viewModel.conditionRecord(date, definition.id) },
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F6F2))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checkedCondition != null,
                onCheckedChange = { viewModel.conditionRecord(date, definition.id) }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(definition.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text("누적 ${definition.frequency}회", style = MaterialTheme.typography.bodySmall)
            }
            if (checkedCondition != null) {
                Text("오늘 체크", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

internal fun moveDate(date: String, amount: Int): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val parsedDate = format.parse(date) ?: return date
    return Calendar.getInstance().run {
        time = parsedDate
        add(Calendar.DAY_OF_MONTH, amount)
        format.format(time)
    }
}

private fun projectPeriodLabel(endDate: String, selectedDate: String): String {
    if (endDate == "-" || endDate.isBlank()) return "무기한"

    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
        isLenient = false
    }
    val end = runCatching { format.parse(endDate) }.getOrNull() ?: return endDate
    val selected = runCatching { format.parse(selectedDate) }.getOrNull() ?: return endDate
    val days = TimeUnit.MILLISECONDS.toDays(end.time - selected.time)

    return when {
        days > 0 -> "D-$days"
        days == 0L -> "D-Day"
        else -> "종료"
    }
}
