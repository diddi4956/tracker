package com.example.tracker.ui.daily

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.tracker.data.entity.ConditionDefinition
import com.example.tracker.data.entity.ConditionTag
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun ConditionDefinitionDialog(
    initialDefinition: ConditionDefinition? = null,
    onDismiss: () -> Unit,
    onSave: (ConditionDefinition) -> Unit
) {
    var name by remember(initialDefinition?.id, initialDefinition?.name) {
        mutableStateOf(initialDefinition?.name.orEmpty())
    }
    val isUpdate = initialDefinition != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isUpdate) "컨디션 데피니션 수정" else "컨디션 데피니션 추가") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
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
                        initialDefinition?.copy(name = name.trim())
                            ?: ConditionDefinition(id = 0L, name = name.trim())
                    )
                }
            ) { Text(if (isUpdate) "저장" else "추가") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ConditionTagDialog(
    selectedTags: List<ConditionTag>,
    candidates: List<ConditionTag>,
    isUpdate: Boolean,
    onSearch: (String) -> Unit,
    onAddTag: (ConditionTag) -> Unit,
    onUpdateTag: (ConditionTag) -> Unit,
    onDeleteTag: (ConditionTag) -> Unit,
    onToggle: (ConditionTag) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var keyword by remember { mutableStateOf("") }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }
    var pendingCreatedTagName by remember { mutableStateOf<String?>(null) }
    var tagForActions by remember { mutableStateOf<ConditionTag?>(null) }
    var tagBeingEdited by remember { mutableStateOf<ConditionTag?>(null) }
    var tagPendingDelete by remember { mutableStateOf<ConditionTag?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(candidates, pendingCreatedTagName) {
        val createdName = pendingCreatedTagName ?: return@LaunchedEffect
        val createdTag = candidates.firstOrNull { it.name == createdName }
            ?: return@LaunchedEffect

        if (selectedTags.none { it.id == createdTag.id }) onToggle(createdTag)
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
                    text = "태그는 선택하지 않아도 저장할 수 있음.",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = keyword,
                        onValueChange = {
                            keyword = it
                            onSearch(it)
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
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("+") }
                }

                if (candidates.isEmpty()) {
                    EmptyMessage("검색된 태그가 없음")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                        items(candidates, key = { it.id }) { tag ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { onToggle(tag) },
                                        onLongClick = { tagForActions = tag }
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedTags.any { it.id == tag.id },
                                    onCheckedChange = null
                                )
                                Text(tag.name)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onSave) { Text("저장") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )

    if (showAddTagDialog) {
        AlertDialog(
            onDismissRequest = { showAddTagDialog = false },
            title = { Text("태그 추가") },
            text = {
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
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
                ) { Text("추가") }
            },
            dismissButton = {
                TextButton(onClick = { showAddTagDialog = false }) { Text("취소") }
            }
        )
    }

    tagForActions?.let { tag ->
        AlertDialog(
            onDismissRequest = { tagForActions = null },
            title = { Text(tag.name) },
            text = { Text("태그를 수정하거나 삭제할 수 있음.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        tagForActions = null
                        tagBeingEdited = tag
                    }
                ) { Text("수정") }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            tagForActions = null
                            tagPendingDelete = tag
                        }
                    ) { Text("삭제") }
                    TextButton(onClick = { tagForActions = null }) { Text("닫기") }
                }
            }
        )
    }

    tagBeingEdited?.let { tag ->
        ConditionTagEditDialog(
            initialTag = tag,
            onDismiss = { tagBeingEdited = null },
            onSave = { updatedTag ->
                onUpdateTag(updatedTag)
                tagBeingEdited = null
            }
        )
    }

    tagPendingDelete?.let { tag ->
        AlertDialog(
            onDismissRequest = { tagPendingDelete = null },
            title = { Text("태그 삭제") },
            text = {
                Text(
                    "'${tag.name}' 태그를 삭제함.\n" +
                        "연결된 컨디션 기록에서는 이 태그만 제거됨."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTag(tag)
                        tagPendingDelete = null
                    }
                ) { Text("삭제") }
            },
            dismissButton = {
                TextButton(onClick = { tagPendingDelete = null }) { Text("취소") }
            }
        )
    }
}

@Composable
internal fun ConditionTagEditDialog(
    initialTag: ConditionTag,
    onDismiss: () -> Unit,
    onSave: (ConditionTag) -> Unit
) {
    var name by remember(initialTag.id, initialTag.name) {
        mutableStateOf(initialTag.name)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("태그 수정") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("태그 이름") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && name.trim() != initialTag.name,
                onClick = { onSave(initialTag.copy(name = name.trim())) }
            ) { Text("저장") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}
