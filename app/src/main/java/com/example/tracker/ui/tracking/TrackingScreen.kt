package com.example.tracker.ui.tracking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tracker.data.entity.ConditionTag
import java.text.NumberFormat

@Composable
fun TrackingScreen(viewModel: TrackingViewModel, modifier: Modifier = Modifier) {
    val state = viewModel.trackingUiState
    var selectedTags by remember { mutableStateOf(emptyList<ConditionTag>()) }

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "${state.startDate}  ~  ${state.endDate}",
                Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.titleLarge
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    viewModel::extendPeriodToPast,
                    Modifier.weight(1f)
                ) { Text("과거 1일 추가") }
                OutlinedButton(
                    viewModel::extendPeriodToFuture,
                    Modifier.weight(1f)
                ) { Text("미래 1일 추가") }
            }
        }

        item { TrackingTitle("지출") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(viewModel::calcDailyExpense) { Text("일별 합계") }
                OutlinedButton(viewModel::wholeCircleGraphing) { Text("전체 비율") }
            }
        }
        items(state.calcDailyExpense) { expense ->
            TrackingRow(expense.date, "${formatNumber(expense.dailyTotalPrice)}원")
        }
    }
}

@Composable
private fun TrackingTitle(title: String) =
    Text(title, Modifier.padding(top = 8.dp), style = MaterialTheme.typography.headlineSmall)

@Composable
private fun TrackingRow(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label)
            Text(value)
        }
    }
}

private fun formatNumber(value: Long): String = NumberFormat.getNumberInstance().format(value)
