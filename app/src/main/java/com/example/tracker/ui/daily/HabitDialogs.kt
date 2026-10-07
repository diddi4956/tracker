package com.example.tracker.ui.daily

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tracker.data.entity.HabitCategoryDefinition
import com.example.tracker.data.entity.HabitDefinition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HabitProjectDialog(
    initialProject: HabitCategoryDefinition,
    onDismiss: () -> Unit,
    onSave: (HabitCategoryDefinition) -> Unit
) {
    var name by remember(initialProject) { mutableStateOf(initialProject.name) }
    var startDate by remember(initialProject) {
        mutableStateOf(initialProject.startDate.orEmpty())
    }
    var endDate by remember(initialProject) {
        mutableStateOf(initialProject.endDate.orEmpty())
    }
    var showDateRangePicker by remember { mutableStateOf(false) }
    val hasValidPeriod = startDate.isBlank() || endDate.isBlank() || startDate <= endDate

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialProject.id == 0L) "습관 프로젝트 추가" else "습관 프로젝트 수정")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("프로젝트 이름") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = if (startDate.isBlank() && endDate.isBlank()) {
                        "기간 제한 없음"
                    } else {
                        "$startDate  ~  $endDate"
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = startDate.isNotBlank() || endDate.isNotBlank(),
                        onClick = { showDateRangePicker = true },
                        modifier = Modifier.weight(1f),
                        label = { Text("기간 선택") }
                    )
                    FilterChip(
                        selected = startDate.isBlank() && endDate.isBlank(),
                        onClick = {
                            startDate = ""
                            endDate = ""
                        },
                        label = { Text("기간 없음") }
                    )
                }
                if (!hasValidPeriod) {
                    Text(
                        text = "종료일은 시작일보다 빠를 수 없습니다.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && hasValidPeriod,
                onClick = {
                    onSave(
                        initialProject.copy(
                            name = name.trim(),
                            startDate = startDate.ifBlank { null },
                            endDate = endDate.ifBlank { null }
                        )
                    )
                }
            ) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )

    if (showDateRangePicker) {
        val rangeState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = dateStringToUtcMillis(startDate),
            initialSelectedEndDateMillis = dateStringToUtcMillis(endDate)
        )

        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(
                    enabled = rangeState.selectedStartDateMillis != null &&
                        rangeState.selectedEndDateMillis != null,
                    onClick = {
                        startDate = utcMillisToDateString(rangeState.selectedStartDateMillis!!)
                        endDate = utcMillisToDateString(rangeState.selectedEndDateMillis!!)
                        showDateRangePicker = false
                    }
                ) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangePicker = false }) { Text("취소") }
            }
        ) {
            DateRangePicker(
                state = rangeState,
                modifier = Modifier.height(520.dp),
                showModeToggle = false
            )
        }
    }
}

@Composable
internal fun HabitDefinitionDialog(
    initialHabit: HabitDefinition,
    onDismiss: () -> Unit,
    onSave: (HabitDefinition) -> Unit
) {
    var name by remember(initialHabit) { mutableStateOf(initialHabit.name) }
    var importanceText by remember(initialHabit) {
        mutableStateOf(initialHabit.importance.toString())
    }
    val importance = importanceText.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialHabit.id == 0L) "습관 추가" else "습관 수정") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("습관 이름") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = importanceText,
                    onValueChange = { value ->
                        if (value.all(Char::isDigit)) importanceText = value
                    },
                    label = { Text("중요도") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && importance != null,
                onClick = {
                    onSave(
                        initialHabit.copy(
                            name = name.trim(),
                            importance = importance ?: 0
                        )
                    )
                }
            ) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

private fun dateStringToUtcMillis(date: String): Long? {
    if (date.isBlank()) return null
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
        isLenient = false
    }
    return runCatching { format.parse(date)?.time }.getOrNull()
}

private fun utcMillisToDateString(millis: Long): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return format.format(Date(millis))
}
