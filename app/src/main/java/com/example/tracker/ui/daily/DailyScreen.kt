package com.example.tracker.ui.daily

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
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
                onDeleteExpense = { record ->
                    expensePendingDelete = record
                }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("습관")
                Button(
                    onClick = viewModel::openAddProject,
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+")
                }
            }
        }
        if (state.dailyHabits.isEmpty()) {
            item { EmptyMessage("등록된 습관이 없어요") }
        } else {
            items(state.dailyHabits) { project ->
                HabitProjectCard(
                    project = project,
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
                    onEditHabit = { habit ->
                        habit.id?.let(viewModel::openUpdateHabit)
                    },
                    onDeleteHabit = { habit ->
                        habitPendingDelete = habit
                    }
                )
            }
        }

        item {
            SectionTitle("컨디션")
        }
        item {
            Text("데피니션 검색", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = conditionKeyword,
                    onValueChange = { keyword ->
                        conditionKeyword = keyword
                        viewModel.searchConditions(keyword)
                    },
                    label = { Text("컨디션 이름 검색") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { showConditionDefinitionAddDialog = true },
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+")
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
            Text(
                if (conditionKeyword.isBlank()) "전체 컨디션 · 빈도순" else "검색 결과",
                style = MaterialTheme.typography.titleMedium
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
        }

        item { Text("오늘 체크된 컨디션", style = MaterialTheme.typography.titleMedium) }
        if (state.dailyConditions.isEmpty()) {
            item { EmptyMessage("오늘 체크된 컨디션이 없어요") }
        } else {
            itemsIndexed(state.dailyConditions) { _, condition ->
                val record = condition.conditionRecord
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(record.definitionName, style = MaterialTheme.typography.titleMedium)
                            TextButton(
                                onClick = {
                                    viewModel.openUpdateRelation(
                                        record.conditionCheckedRecord,
                                        condition.tags
                                    )
                                }
                            ) { Text("태그 수정") }
                        }
                        if (condition.tags.isEmpty()) {
                            Text("태그 없음", style = MaterialTheme.typography.bodySmall)
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(condition.tags, key = { tag -> tag.id }) { tag ->
                                    Card(shape = MaterialTheme.shapes.small) {
                                        Text(
                                            text = tag.name,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelMedium
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
            onItemSearch = viewModel::searchItems,
            onAddItem = viewModel::openAddItem,
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
            initialSubCategory = subCategoryForm.copy(name = pendingSubCategoryName),
            onDismiss = viewModel::closeSubCategoryForm,
            onSave = { subCategory ->
                viewModel.addSubCategory(subCategory)
            }
        )
    }

    state.itemForm?.let { itemForm ->
        ItemDefinitionDialog(
            initialItem = itemForm,
            onDismiss = viewModel::closeItemForm,
            onSave = { item ->
                viewModel.addItem(item)
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

@Composable
private fun ConditionDefinitionRow(
    definition: DefinitionWithFrequency,
    checkedCondition: com.example.tracker.data.dto.ConditionRecordWithTags?,
    date: String,
    viewModel: DailyViewModel
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checkedCondition != null,
                onCheckedChange = { viewModel.conditionRecord(date, definition.id) }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(definition.name, style = MaterialTheme.typography.titleMedium)
                Text("누적 ${definition.frequency}회", style = MaterialTheme.typography.bodySmall)
            }
            if (checkedCondition != null) {
                Text("오늘 체크", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun ConditionDefinitionDialog(
    onDismiss: () -> Unit,
    onSave: (ConditionDefinition) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("컨디션 데피니션 추가") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { changedName -> name = changedName },
                label = { Text("컨디션 이름") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(
                        ConditionDefinition(
                            id = 0L,
                            name = name.trim()
                        )
                    )
                }
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
private fun ConditionTagDialog(
    selectedTags: List<ConditionTag>,
    candidates: List<ConditionTag>,
    isUpdate: Boolean,
    onSearch: (String) -> Unit,
    onAddTag: (ConditionTag) -> Unit,
    onToggle: (ConditionTag) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var keyword by remember { mutableStateOf("") }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }
    var pendingCreatedTagName by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(candidates, pendingCreatedTagName) {
        val createdName = pendingCreatedTagName ?: return@LaunchedEffect
        val createdTag = candidates.firstOrNull { tag -> tag.name == createdName }
            ?: return@LaunchedEffect

        if (selectedTags.none { tag -> tag.id == createdTag.id }) {
            onToggle(createdTag)
        }
        pendingCreatedTagName = null
        keyword = ""
        onSearch("")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isUpdate) "컨디션 태그 수정" else "컨디션 태그 선택") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "태그는 선택하지 않아도 저장할 수 있어요.",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = keyword,
                        onValueChange = { value ->
                            keyword = value
                            onSearch(value)
                        },
                        label = { Text("태그 검색") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            newTagName = keyword
                            showAddTagDialog = true
                        },
                        modifier = Modifier.padding(start = 8.dp).size(40.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("+")
                    }
                }

                if (candidates.isEmpty()) {
                    EmptyMessage("검색된 태그가 없어요")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                        items(candidates, key = { tag -> tag.id }) { tag ->
                            val selected = selectedTags.any { selectedTag ->
                                selectedTag.id == tag.id
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selected,
                                    onCheckedChange = { onToggle(tag) }
                                )
                                Text(tag.name)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )

    if (showAddTagDialog) {
        AlertDialog(
            onDismissRequest = { showAddTagDialog = false },
            title = { Text("태그 추가") },
            text = {
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { name -> newTagName = name },
                    label = { Text("태그 이름") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    enabled = newTagName.isNotBlank(),
                    onClick = {
                        val name = newTagName.trim()
                        onAddTag(ConditionTag(id = 0L, name = name))
                        keyword = name
                        pendingCreatedTagName = name
                        showAddTagDialog = false
                        coroutineScope.launch {
                            delay(200)
                            onSearch(name)
                        }
                    }
                ) {
                    Text("추가")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTagDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}


@Composable
private fun ExpenseRecordDialog(
    initialForm: ExpenseRecordForm,
    categoryId: Long,
    itemCandidates: List<ItemDefinition>,
    subCategoryCandidates: List<ExpenseSubCategoryDefinition>,
    onSubCategorySearch: (Long, String) -> Unit,
    onAddSubCategory: (Long, String) -> Unit,
    onItemSearch: (String) -> Unit,
    onAddItem: (String) -> Unit,
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

    var subCategoryKeyword by remember(initialForm) {
        mutableStateOf(initialForm.subCategoryName)
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
                Text(
                    text = "서브카테고리 검색 및 선택",
                    style = MaterialTheme.typography.titleSmall
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = subCategoryKeyword,
                        onValueChange = { keyword ->
                            subCategoryKeyword = keyword
                            draft = draft.copy(
                                subCategoryName = keyword,
                                subCategoryId = 0L,
                                itemName = "",
                                itemId = 0L,
                                unitPrice = 0L
                            )
                            priceText = "0"
                            onSubCategorySearch(categoryId, keyword)
                        },
                        label = { Text("서브카테고리 검색") },
                        enabled = categoryId != 0L,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { onAddSubCategory(categoryId, subCategoryKeyword) }
                    ) {
                        Text("추가")
                    }
                }

                if (subCategoryCandidates.isNotEmpty() && draft.subCategoryId == 0L) {
                    LazyColumn(modifier = Modifier.heightIn(max = 120.dp)) {
                        items(subCategoryCandidates) { subCategory ->
                            TextButton(
                                onClick = {
                                    subCategoryKeyword = subCategory.name
                                    draft = draft.copy(
                                        subCategoryName = subCategory.name,
                                        subCategoryId = subCategory.id,
                                        itemName = "",
                                        itemId = 0L,
                                        unitPrice = 0L
                                    )
                                    priceText = "0"
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(subCategory.name)
                            }
                        }
                    }
                } else if (subCategoryKeyword.isNotBlank() && draft.subCategoryId == 0L) {
                    Text(
                        text = "검색 결과가 없어요",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (draft.subCategoryId != 0L) {
                    Text(
                        text = "선택: ${draft.subCategoryName}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = draft.itemName,
                        onValueChange = { keyword ->
                            draft = draft.copy(
                                itemName = keyword,
                                itemId = 0L,
                                unitPrice = 0L
                            )
                            priceText = "0"
                            onItemSearch(keyword)
                        },
                        label = { Text("아이템 검색") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { onAddItem(draft.itemName) }
                    ) {
                        Text("추가")
                    }
                }
                if (itemCandidates.isNotEmpty() && draft.itemId == 0L) {
                    LazyColumn(modifier = Modifier.heightIn(max = 160.dp)) {
                        items(itemCandidates) { item ->
                            TextButton(
                                onClick = {
                                    draft = draft.copy(
                                        itemName = item.name,
                                        itemId = item.id,
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
private fun ExpenseSubCategoryDialog(
    initialSubCategory: ExpenseSubCategoryDefinition,
    onDismiss: () -> Unit,
    onSave: (ExpenseSubCategoryDefinition) -> Unit
) {
    var draft by remember(initialSubCategory) {
        mutableStateOf(initialSubCategory)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("서브카테고리 추가") },
        text = {
            OutlinedTextField(
                value = draft.name,
                onValueChange = { name -> draft = draft.copy(name = name) },
                label = { Text("서브카테고리 이름") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                enabled = draft.name.isNotBlank(),
                onClick = { onSave(draft) }
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable // 얜 왜있는거?
private fun ItemDefinitionDialog(
    initialItem: ItemDefinition,
    onDismiss: () -> Unit,
    onSave: (ItemDefinition) -> Unit
) {
    var draft by remember(initialItem) { mutableStateOf(initialItem) }
    var kcalText by remember(initialItem) {
        mutableStateOf(initialItem.kcalPerUnit?.toString().orEmpty())
    }
    var defaultPriceText by remember(initialItem) {
        mutableStateOf(initialItem.defaultPrice.toString())
    }

    val kcal = kcalText.toLongOrNull()
    val defaultPrice = defaultPriceText.toLongOrNull()
    val canSave = draft.name.isNotBlank() && defaultPrice != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("아이템 추가") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { name -> draft = draft.copy(name = name) },
                    label = { Text("아이템 이름") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = draft.store.orEmpty(),
                    onValueChange = { store -> draft = draft.copy(store = store.ifBlank { null }) },
                    label = { Text("구입처") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = kcalText,
                    onValueChange = { kcalText = it },
                    label = { Text("단위당 칼로리") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = defaultPriceText,
                    onValueChange = { defaultPriceText = it },
                    label = { Text("기본 단가") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = draft.memo.orEmpty(),
                    onValueChange = { memo -> draft = draft.copy(memo = memo.ifBlank { null }) },
                    label = { Text("메모") }
                )
            }
        },
        confirmButton = {
            Button(
                enabled = canSave,
                onClick = {
                    onSave(
                        draft.copy(
                            kcalPerUnit = kcal,
                            defaultPrice = defaultPrice!!
                        )
                    )
                }
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
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
    onAddExpense: () -> Unit,
    onEditExpense: (ExpenseDailyRecord) -> Unit,
    onDeleteExpense: (ExpenseDailyRecord) -> Unit
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
                        TextButton(onClick = { onEditExpense(record) }) {
                            Text("수정")
                        }
                        TextButton(onClick = { onDeleteExpense(record) }) {
                            Text("삭제")
                        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitProjectDialog(
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
                    Button(
                        onClick = { showDateRangePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("기간 선택")
                    }
                    TextButton(
                        onClick = {
                            startDate = ""
                            endDate = ""
                        }
                    ) {
                        Text("기간 없음")
                    }
                }
                if (!hasValidPeriod) {
                    Text(
                        text = "종료일은 시작일보다 빠를 수 없어요.",
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
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
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
                        startDate = utcMillisToDateString(
                            rangeState.selectedStartDateMillis!!
                        )
                        endDate = utcMillisToDateString(
                            rangeState.selectedEndDateMillis!!
                        )
                        showDateRangePicker = false
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangePicker = false }) {
                    Text("취소")
                }
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

@Composable
private fun HabitProjectCard(
    project: HabitCategory,
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

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.categoryName,
                    style = MaterialTheme.typography.titleMedium
                )
                Row {
                    TextButton(onClick = onEditProject) {
                        Text("수정")
                    }
                    TextButton(onClick = onDeleteProject) {
                        Text("삭제")
                    }
                    TextButton(onClick = onAddHabit) {
                        Text("추가")
                    }
                }
            }

            if (actualHabits.isEmpty()) {
                Text(
                    text = "등록된 습관이 없어요",
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
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
                        Text(
                            text = habitName,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { onEditHabit(habit) }) {
                            Text("수정")
                        }
                        TextButton(onClick = { onDeleteHabit(habit) }) {
                            Text("삭제")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitDefinitionDialog(
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
        title = {
            Text(if (initialHabit.id == 0L) "습관 추가" else "습관 수정")
        },
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
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
