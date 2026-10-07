package com.example.tracker.ui.daily

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tracker.data.entity.ExpenseSubCategoryDefinition
import com.example.tracker.data.entity.ItemDefinition
import com.example.tracker.data.model.IdWithName

@Composable
internal fun ExpenseRecordDialog(
    initialForm: ExpenseRecordForm,
    categoryId: Long,
    categories: List<IdWithName>,
    itemCandidates: List<ItemDefinition>,
    subCategoryCandidates: List<ExpenseSubCategoryDefinition>,
    onCategoryChange: (Long) -> Unit,
    onSubCategorySearch: (Long, String) -> Unit,
    onAddSubCategory: (Long, String) -> Unit,
    onEditSubCategory: (ExpenseSubCategoryDefinition) -> Unit,
    onItemSearch: (String) -> Unit,
    onAddItem: (String) -> Unit,
    onEditItem: (ItemDefinition) -> Unit,
    onDismiss: () -> Unit,
    onSave: (ExpenseRecordForm) -> Unit
) {
    var draft by remember(initialForm.recordId) { mutableStateOf(initialForm) }
    var priceText by remember(initialForm.recordId) {
        mutableStateOf(initialForm.unitPrice.toString())
    }
    var quantityText by remember(initialForm.recordId) {
        mutableStateOf(initialForm.quantity.toString())
    }
    var subCategoryKeyword by remember(initialForm.recordId) {
        mutableStateOf(initialForm.subCategoryName)
    }

    LaunchedEffect(initialForm.subCategoryId, initialForm.subCategoryName) {
        if (initialForm.subCategoryId != 0L && initialForm.subCategoryId != draft.subCategoryId) {
            draft = draft.copy(
                subCategoryId = initialForm.subCategoryId,
                subCategoryName = initialForm.subCategoryName
            )
            subCategoryKeyword = initialForm.subCategoryName
        }
    }

    LaunchedEffect(initialForm.itemId, initialForm.itemName, initialForm.unitPrice) {
        if (initialForm.itemId != 0L && initialForm.itemId != draft.itemId) {
            draft = draft.copy(
                itemId = initialForm.itemId,
                itemName = initialForm.itemName,
                unitPrice = initialForm.unitPrice
            )
            priceText = initialForm.unitPrice.toString()
        }
    }

    val price = evaluateMoneyExpression(priceText)
    val quantity = quantityText.toIntOrNull()
    val canSave = draft.itemId != 0L && draft.subCategoryId != 0L &&
        price != null && quantity != null && quantity > 0
    val selectedItem = itemCandidates.firstOrNull { it.id == draft.itemId }

    LaunchedEffect(draft.itemId) {
        if (draft.itemId != 0L && selectedItem == null) {
            onItemSearch(draft.itemName)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialForm.recordId == 0L) "지출 추가" else "지출 수정") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("지출 카테고리", style = MaterialTheme.typography.titleSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories, key = { it.id }) { category ->
                        FilterChip(
                            selected = category.id == categoryId,
                            onClick = {
                                if (category.id != categoryId) {
                                    draft = draft.copy(
                                        subCategoryName = "",
                                        subCategoryId = 0L
                                    )
                                    subCategoryKeyword = ""
                                    onCategoryChange(category.id)
                                }
                            },
                            label = { Text(category.name) }
                        )
                    }
                }

                Text("서브카테고리 검색 및 선택", style = MaterialTheme.typography.titleSmall)
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
                                subCategoryId = 0L
                            )
                            onSubCategorySearch(categoryId, keyword)
                        },
                        label = { Text("서브카테고리 검색") },
                        enabled = categoryId != 0L,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onAddSubCategory(categoryId, subCategoryKeyword) }) {
                        Text("추가")
                    }
                }

                if (subCategoryCandidates.isNotEmpty() && draft.subCategoryId == 0L) {
                    LazyColumn(modifier = Modifier.heightIn(max = 120.dp)) {
                        val displayedCandidates = if (subCategoryKeyword.isBlank()) {
                            subCategoryCandidates.take(1)
                        } else {
                            subCategoryCandidates
                        }
                        items(displayedCandidates) { subCategory ->
                            TextButton(
                                onClick = {
                                    subCategoryKeyword = subCategory.name
                                    draft = draft.copy(
                                        subCategoryName = subCategory.name,
                                        subCategoryId = subCategory.id
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(subCategory.name) }
                        }
                    }
                } else if (subCategoryKeyword.isNotBlank() && draft.subCategoryId == 0L) {
                    Text("검색 결과가 없어요", style = MaterialTheme.typography.bodySmall)
                }

                if (draft.subCategoryId != 0L) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "선택: ${draft.subCategoryName}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(
                            onClick = {
                                onEditSubCategory(
                                    ExpenseSubCategoryDefinition(
                                        id = draft.subCategoryId,
                                        categoryId = categoryId,
                                        name = draft.subCategoryName
                                    )
                                )
                            }
                        ) { Text("수정") }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = draft.itemName,
                        onValueChange = { keyword ->
                            draft = draft.copy(itemName = keyword, itemId = 0L, unitPrice = 0L)
                            priceText = "0"
                            onItemSearch(keyword)
                        },
                        label = { Text("아이템 검색") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onAddItem(draft.itemName) }) { Text("추가") }
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
                                    Text("${item.defaultPrice}원", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                if (draft.itemId != 0L) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "선택한 아이템: ${draft.itemName}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(
                            enabled = selectedItem != null,
                            onClick = { selectedItem?.let(onEditItem) }
                        ) { Text("수정") }
                    }
                }

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("단가") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("수량") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = draft.memo,
                    onValueChange = { draft = draft.copy(memo = it) },
                    label = { Text("메모") }
                )
            }
        },
        confirmButton = {
            Button(
                enabled = canSave,
                onClick = { onSave(draft.copy(unitPrice = price!!, quantity = quantity!!)) }
            ) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

@Composable
internal fun ExpenseSubCategoryDialog(
    initialSubCategory: ExpenseSubCategoryDefinition,
    onDismiss: () -> Unit,
    onSave: (ExpenseSubCategoryDefinition) -> Unit
) {
    var draft by remember(initialSubCategory) { mutableStateOf(initialSubCategory) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialSubCategory.id == 0L) "서브카테고리 추가" else "서브카테고리 수정")
        },
        text = {
            OutlinedTextField(
                value = draft.name,
                onValueChange = { draft = draft.copy(name = it) },
                label = { Text("서브카테고리 이름") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(enabled = draft.name.isNotBlank(), onClick = { onSave(draft) }) {
                Text(if (initialSubCategory.id == 0L) "추가" else "저장")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}

@Composable
internal fun ItemDefinitionDialog(
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
        title = { Text(if (initialItem.id == 0L) "아이템 추가" else "아이템 수정") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { draft = draft.copy(name = it) },
                    label = { Text("아이템 이름") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = draft.store.orEmpty(),
                    onValueChange = { draft = draft.copy(store = it.ifBlank { null }) },
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
                    onValueChange = { draft = draft.copy(memo = it.ifBlank { null }) },
                    label = { Text("메모") }
                )
            }
        },
        confirmButton = {
            Button(
                enabled = canSave,
                onClick = {
                    onSave(draft.copy(kcalPerUnit = kcal, defaultPrice = defaultPrice!!))
                }
            ) { Text(if (initialItem.id == 0L) "추가" else "저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}
