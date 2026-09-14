package com.example.tracker.ui.daily

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tracker.data.entity.ConditionCheckRecord
import com.example.tracker.data.entity.HabitRecord
import com.example.tracker.data.entity.ItemDefinition
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DailyScreen(viewModel: DailyViewModel, modifier: Modifier = Modifier) {
    val state = viewModel.dailyUiState

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DateSelector(
                date = state.date,
                onPrevious = { viewModel.changeDate(moveDate(state.date, -1)) },
                onNext = { viewModel.changeDate(moveDate(state.date, 1)) }
            )
        }

        item { SectionTitle("지출") }
        items(state.dailyExpenses) { category ->
            ExpenseCategoryCard(
                category = category,
                onAddExpense = viewModel::openAddExpenseRecord
            )
        }

        item { SectionTitle("습관") }
        if (state.dailyHabits.isEmpty()) {
            item { EmptyMessage("등록된 습관이 없어요") }
        } else {
            items(state.dailyHabits) { category ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(category.categoryName, style = MaterialTheme.typography.titleMedium)
                        category.habitList.forEach { habit ->
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = habit.checked,
                                    onCheckedChange = {
                                        viewModel.checkingHabit(
                                            HabitRecord(
                                                id = habit.recordId ?: 0L,
                                                date = state.date,
                                                habitDefinitionId = habit.id,
                                                checked = !habit.checked
                                            )
                                        )
                                    }
                                )
                                Text(habit.name)
                            }
                        }
                    }
                }
            }
        }

        item { SectionTitle("컨디션") }
        if (state.dailyConditions.isEmpty()) {
            item { EmptyMessage("등록된 컨디션이 없어요") }
        } else {
            items(state.dailyConditions) { tag ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(tag.tagName, style = MaterialTheme.typography.titleMedium)
                        tag.conditionList.forEach { condition ->
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = condition.checked,
                                    onCheckedChange = {
                                        viewModel.checkCondition(
                                            ConditionCheckRecord(
                                                date = state.date,
                                                conditionDefinitionId = condition.id
                                            )
                                        )
                                    }
                                )
                                Text(condition.name)
                            }
                        }
                    }
                }
            }
        }

    }

    state.expenseRecordForm?.let { form ->
        ExpenseRecordDialog(
            initialForm = form,
            candidates = state.itemCandidates,
            onSearch = viewModel::searchItems,
            onDismiss = viewModel::closeExpenseRecordForm,
            onSave = { completedForm ->
                if (completedForm.recordId == 0L) {
                    viewModel.addExpenseRecord(completedForm)
                } else {
                    viewModel.updateExpenseRecord(completedForm)
                }
                viewModel.closeExpenseRecordForm()
            }
        )
    }
}


@Composable
private fun ExpenseRecordDialog(
    initialForm: ExpenseRecordForm,
    candidates: List<ItemDefinition>,
    onSearch: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (ExpenseRecordForm) -> Unit
){
    var draft by remember(initialForm){
        mutableStateOf(initialForm)
    }

    var priceText by remember(initialForm){
        mutableStateOf(initialForm.unitPrice.toString())
    }

    var quantityText by remember(initialForm){
        mutableStateOf(initialForm.quantity.toString())
    }

    val price = priceText.toLongOrNull()
    val quantity = quantityText.toIntOrNull()

    val canSave =
        draft.itemId !=0L && draft.subCategoryId != 0L && price != null && quantity !=null && quantity >0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if(initialForm.recordId == 0L){
                    "지출 추가"
                } else{
                    "지출 수정"
                }
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draft.itemName,
                    onValueChange = { keyword ->
                        draft = draft.copy(
                            itemName = keyword,
                            itemId = 0L,
                            subCategoryId = 0L,
                            subCategoryName = ""
                        )
                        onSearch(keyword)
                    },
                    label = { Text("아이템 검색") },
                    singleLine = true
                )
                if (candidates.isNotEmpty() && draft.itemId == 0L) {
                    LazyColumn(modifier = Modifier.heightIn(max = 160.dp)) {
                        items(candidates) { item ->
                            TextButton(
                                onClick = {
                                    draft = draft.copy(
                                        itemName = item.name,
                                        itemId = item.id,
                                        subCategoryId = item.subCategoryId,
                                        unitPrice = item.defaultPrice
                                    )
                                    priceText = item.defaultPrice.toString()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(item.name)
                                    Text(
                                        text = "${item.defaultPrice}원",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
                if (draft.itemId != 0L) {
                    Text(
                        text = "선택한 아이템 Id: ${draft.itemId}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "서브카테고리 Id: ${draft.subCategoryId}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { input -> priceText = input },
                    label = { Text("단가") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { input -> quantityText = input },
                    label = { Text("수량") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = draft.memo,
                    onValueChange = { input -> draft = draft.copy(memo = input) },
                    label = { Text("메모") }
                )

            }
        },

        confirmButton = {
            Button(
                enabled = canSave,
                onClick = { onSave(draft.copy(unitPrice = price!!, quantity = quantity!!)) }
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss){
                Text("취소")
            }
        }
    )
}

@Composable
private fun DateSelector(date: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onPrevious) { Text("이전") }
        Text(date, style = MaterialTheme.typography.titleLarge)
        TextButton(onClick = onNext) { Text("다음") }
    }
}

@Composable
private fun ExpenseCategoryCard(
    category: ExpenseByCategory,
    onAddExpense: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(category.categoryName, style = MaterialTheme.typography.titleMedium)
                    Text("${NumberFormat.getNumberInstance().format(category.totalPrice)}원")
                }
                TextButton(onClick = onAddExpense) {
                    Text("추가")
                }
            }
            if (category.recordList.isEmpty()) {
                Text("기록 없음", Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                category.recordList.forEach { record ->
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(record.itemName)
                            if (record.memo.isNotBlank()) {
                                Text(record.memo, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Text("${NumberFormat.getNumberInstance().format(record.totalPrice)}원")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) = Text(title, style = MaterialTheme.typography.headlineSmall)

@Composable
private fun EmptyMessage(message: String) =
    Text(message, Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)

private fun moveDate(date: String, amount: Int): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val parsedDate = format.parse(date) ?: return date
    return Calendar.getInstance().run {
        time = parsedDate
        add(Calendar.DAY_OF_MONTH, amount)
        format.format(time)
    }
}
