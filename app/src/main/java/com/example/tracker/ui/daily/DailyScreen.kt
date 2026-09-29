package com.example.tracker.ui.daily

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tracker.data.dto.HabitGetDailyListDto
import com.example.tracker.data.entity.ConditionCheckRecord
import com.example.tracker.data.entity.ConditionDefinition
import com.example.tracker.data.entity.ConditionTag
import com.example.tracker.data.entity.ExpenseSubCategoryDefinition
import com.example.tracker.data.entity.HabitCategoryDefinition
import com.example.tracker.data.entity.HabitDefinition
import com.example.tracker.data.entity.HabitRecord
import com.example.tracker.data.entity.ItemDefinition
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DailyScreen(viewModel: DailyViewModel, modifier: Modifier = Modifier) {
    val state = viewModel.dailyUiState
    var selectedExpenseCategoryId by remember { mutableStateOf<Long?>(null) }
    var pendingSubCategoryName by remember { mutableStateOf("") }
    var expensePendingDelete by remember { mutableStateOf<ExpenseDailyRecord?>(null) }
    var habitPendingDelete by remember { mutableStateOf<HabitGetDailyListDto?>(null) }
    var projectPendingDelete by remember { mutableStateOf<Pair<Long, String>?>(null) }
    var conditionKeyword by remember { mutableStateOf("") }
    var showConditionDefinitionAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.searchConditions("")
    }

    LazyColumn(
        modifier = modifier
            .background(Color(0xFFFAFAF8))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 18.dp)) {
                Text(
                    text = "오늘의 기록",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "하루의 지출, 습관과 컨디션을 한곳에 기록해요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF777772)
                )
            }
            DateSelector(
                date = state.date,
                onPrevious = { viewModel.changeDate(moveDate(state.date, -1)) },
                onNext = { viewModel.changeDate(moveDate(state.date, 1)) }
            )
        }

        item {
            NotionSection(
                title = "지출",
                description = "오늘 사용한 금액과 지출 항목을 기록해요."
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.dailyExpenses, key = { it.categoryId }) { category ->
                        ExpenseCategoryCard(
                            category = category,
                            modifier = Modifier.width(270.dp),
                            onAddExpense = {
                                selectedExpenseCategoryId = category.categoryId
                                viewModel.searchSubCategory(category.categoryId, "")
                                viewModel.openAddExpenseRecord()
                            },
                            onEditExpense = { record ->
                                record.recordId?.let { recordId ->
                                    selectedExpenseCategoryId = record.categoryId
                                    viewModel.searchSubCategory(record.categoryId, "")
                                    viewModel.openUpdateExpenseRecord(recordId)
                                }
                            },
                            onDeleteExpense = { record -> expensePendingDelete = record }
                        )
                    }
                }
            }
        }

        item {
            NotionSection(
                title = "습관",
                description = "오늘의 습관을 확인하고 실천 여부를 체크해요.",
                action = {
                    Button(
                        onClick = viewModel::openAddProject,
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("+") }
                }
            ) {
                if (state.dailyHabits.isEmpty()) {
                    EmptyMessage("등록된 습관이 없어요")
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(state.dailyHabits, key = { it.categoryName }) { project ->
                            HabitProjectCard(
                                project = project,
                                modifier = Modifier.width(300.dp),
                                selectedDate = state.date,
                                onEditProject = {
                                    val categoryId = project.habitList.firstOrNull()?.categoryId
                                        ?: return@HabitProjectCard
                                    viewModel.openUpdateProject(categoryId)
                                },
                                onDeleteProject = {
                                    val categoryId = project.habitList.firstOrNull()?.categoryId
                                        ?: return@HabitProjectCard
                                    projectPendingDelete = categoryId to project.categoryName
                                },
                                onAddHabit = {
                                    val categoryId = project.habitList.firstOrNull()?.categoryId
                                        ?: return@HabitProjectCard
                                    viewModel.openAddHabit(categoryId)
                                },
                                onHabitChecked = { habit ->
                                    habit.id?.let { habitDefinitionId ->
                                        viewModel.checkingHabit(
                                            HabitRecord(
                                                id = habit.recordId ?: 0L,
                                                date = state.date,
                                                habitDefinitionId = habitDefinitionId,
                                                checked = !habit.checked
                                            )
                                        )
                                    }
                                },
                                onEditHabit = { habit -> habit.id?.let(viewModel::openUpdateHabit) },
                                onDeleteHabit = { habit -> habitPendingDelete = habit }
                            )
                        }
                    }
                }
            }
        }

        val checkedByDefinitionId = state.dailyConditions.associateBy { condition ->
            condition.conditionRecord.conditionCheckedRecord.conditionDefinitionId
        }
        val frequencyByDefinitionId = state.conditionDefinitionListByFrequency.associateBy {
            definition -> definition.id
        }
        val definitionsWithFrequency = state.conditionDefinitions
            .map { definition ->
                DefinitionWithFrequency(
                    id = definition.id,
                    name = definition.name,
                    frequency = frequencyByDefinitionId[definition.id]?.frequency ?: 0L
                )
            }
        val displayedDefinitions = if (conditionKeyword.isBlank()) {
            definitionsWithFrequency.sortedWith(
                compareByDescending<DefinitionWithFrequency> { definition -> definition.frequency }
                    .thenBy { definition -> definition.name }
            )
        } else {
            definitionsWithFrequency.sortedWith(
                compareBy<DefinitionWithFrequency> { definition ->
                    when {
                        definition.name.equals(conditionKeyword, ignoreCase = true) -> 0
                        definition.name.startsWith(conditionKeyword, ignoreCase = true) -> 1
                        else -> 2
                    }
                }.thenBy { definition ->
                    definition.name.indexOf(conditionKeyword, ignoreCase = true)
                }.thenByDescending { definition -> definition.frequency }
            )
        }

        item {
            NotionSection(
                title = "컨디션",
                description = "오늘 느낀 증상과 함께 떠오른 환경 태그를 남겨요.",
                action = {
                    Button(
                        onClick = { showConditionDefinitionAddDialog = true },
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("+") }
                }
            ) {
                OutlinedTextField(
                    value = conditionKeyword,
                    onValueChange = { keyword ->
                        conditionKeyword = keyword
                        viewModel.searchConditions(keyword)
                    },
                    label = { Text("컨디션 이름 검색") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                SectionBand(
                    if (conditionKeyword.isBlank()) "전체 컨디션 · 빈도순" else "검색 결과"
                )
                if (displayedDefinitions.isEmpty()) {
                    EmptyMessage(
                        if (conditionKeyword.isBlank()) {
                            "등록된 컨디션 데피니션이 없어요"
                        } else {
                            "검색된 데피니션이 없어요"
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(displayedDefinitions, key = { definition -> definition.id }) { definition ->
                            ConditionDefinitionRow(
                                definition = definition,
                                checkedCondition = checkedByDefinitionId[definition.id],
                                date = state.date,
                                viewModel = viewModel
                            )
                        }
                    }
                }

                SectionBand("오늘 체크된 컨디션")
                if (state.dailyConditions.isEmpty()) {
                    EmptyMessage("오늘 체크된 컨디션이 없어요")
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.dailyConditions, key = {
                            it.conditionRecord.conditionCheckedRecord.id
                        }) { condition ->
                            val record = condition.conditionRecord
                            SoftBlock(modifier = Modifier.width(220.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        record.definitionName,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    TextButton(
                                        onClick = {
                                            viewModel.openUpdateRelation(
                                                record.conditionCheckedRecord,
                                                condition.tags
                                            )
                                        }
                                    ) { Text("태그") }
                                }
                                Text(
                                    text = if (condition.tags.isEmpty()) {
                                        "태그 없음"
                                    } else {
                                        condition.tags.joinToString(" · ") { it.name }
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF5F6F67),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }

    state.expenseRecordForm?.let { form ->
        ExpenseRecordDialog(
            initialForm = form,
            categoryId = selectedExpenseCategoryId ?: 0L,
            itemCandidates = state.itemCandidates,
            subCategoryCandidates = state.expenseSubCategoryCandidates,
            onSubCategorySearch = viewModel::searchSubCategory,
            onAddSubCategory = { categoryId, name ->
                pendingSubCategoryName = name
                viewModel.openAddSubCategory(categoryId)
            },
            onEditSubCategory = viewModel::openUpdateSubCategory,
            onItemSearch = viewModel::searchItems,
            onAddItem = viewModel::openAddItem,
            onEditItem = viewModel::openUpdateItem,
            onDismiss = {
                viewModel.closeExpenseRecordForm()
                selectedExpenseCategoryId = null
            },
            onSave = { completedForm ->
                if (completedForm.recordId == 0L) {
                    viewModel.addExpenseRecord(completedForm)
                } else {
                    viewModel.updateExpenseRecord(completedForm)
                }
                viewModel.closeExpenseRecordForm()
                selectedExpenseCategoryId = null
            }
        )
    }

    state.subCategoryForm?.let { subCategoryForm ->
        ExpenseSubCategoryDialog(
            initialSubCategory = if (subCategoryForm.id == 0L) {
                subCategoryForm.copy(name = pendingSubCategoryName)
            } else {
                subCategoryForm
            },
            onDismiss = viewModel::closeSubCategoryForm,
            onSave = { subCategory ->
                if (subCategory.id == 0L) {
                    viewModel.addSubCategory(subCategory)
                } else {
                    viewModel.updateSubCategory(subCategory)
                    viewModel.closeSubCategoryForm()
                }
            }
        )
    }

    state.itemForm?.let { itemForm ->
        ItemDefinitionDialog(
            initialItem = itemForm,
            onDismiss = viewModel::closeItemForm,
            onSave = { item ->
                if (item.id == 0L) {
                    viewModel.addItem(item)
                } else {
                    viewModel.updateItem(item)
                }
            }
        )
    }

    state.updateHabitCategory?.let { projectForm ->
        HabitProjectDialog(
            initialProject = projectForm,
            onDismiss = viewModel::closeHabitCategoryForm,
            onSave = { project ->
                if (project.id == 0L) {
                    viewModel.addProject(project)
                } else {
                    viewModel.updateProject(project)
                }
            }
        )
    }

    state.updateHabit?.let { habitForm ->
        HabitDefinitionDialog(
            initialHabit = habitForm,
            onDismiss = viewModel::closeHabitForm,
            onSave = { habit ->
                if (habit.id == 0L) {
                    viewModel.addHabit(habit)
                } else {
                    viewModel.updateHabit(habit)
                }
            }
        )
    }

    state.checkingForm?.let { form ->
        val isUpdate = state.dailyConditions.any { condition ->
            condition.conditionRecord.conditionCheckedRecord.id == form.recordId
        }

        LaunchedEffect(form.recordId) {
            viewModel.searchTags("")
        }

        ConditionTagDialog(
            selectedTags = form.tags,
            candidates = state.tags,
            isUpdate = isUpdate,
            onSearch = viewModel::searchTags,
            onAddTag = viewModel::addTag,
            onToggle = viewModel::toggleConditionTag,
            onDismiss = viewModel::closeCheckingForm,
            onSave = {
                if (isUpdate) {
                    viewModel.updateRelation()
                } else {
                    viewModel.addRelation()
                }
            }
        )
    }

    if (showConditionDefinitionAddDialog) {
        ConditionDefinitionDialog(
            onDismiss = { showConditionDefinitionAddDialog = false },
            onSave = { definition ->
                conditionKeyword = ""
                viewModel.addDefinition(definition)
                showConditionDefinitionAddDialog = false
            }
        )
    }

    habitPendingDelete?.let { habit ->
        AlertDialog(
            onDismissRequest = { habitPendingDelete = null },
            title = { Text("습관 삭제") },
            text = { Text("${habit.name ?: "이 습관"}을 삭제할까요?") },
            confirmButton = {
                Button(
                    onClick = {
                        habit.id?.let(viewModel::deleteHabit)
                        habitPendingDelete = null
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { habitPendingDelete = null }) {
                    Text("취소")
                }
            }
        )
    }

    expensePendingDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { expensePendingDelete = null },
            title = { Text("지출 기록 삭제") },
            text = { Text("${record.itemName} 기록을 삭제할까요?") },
            confirmButton = {
                Button(
                    onClick = {
                        record.recordId?.let(viewModel::deleteExpenseRecord)
                        expensePendingDelete = null
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { expensePendingDelete = null }) {
                    Text("취소")
                }
            }
        )
    }

    projectPendingDelete?.let { (projectId, projectName) ->
        AlertDialog(
            onDismissRequest = { projectPendingDelete = null },
            title = { Text("습관 프로젝트 삭제") },
            text = { Text("$projectName 프로젝트와 모든 습관 기록을 삭제할까요?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProject(projectId)
                        projectPendingDelete = null
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { projectPendingDelete = null }) {
                    Text("취소")
                }
            }
        )
    }
}
