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
            .background(Color(0xFFFAFAF8))
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
            Text(
                text = "기록을 기간별로 비교하고 변화의 흐름을 살펴봐요.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF777772)
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE1E1DE))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "조회 기간",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0EFED), MaterialTheme.shapes.small)
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
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
