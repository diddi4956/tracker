package com.example.tracker.ui.daily

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker.data.dao.ConditionRecordDao
import com.example.tracker.data.dao.ExpenseRecordDao
import com.example.tracker.data.dao.ExpenseSubCategoryDao
import com.example.tracker.data.dao.HabitCategoryDefinitionDao
import com.example.tracker.data.dao.HabitDefinitionDao
import com.example.tracker.data.dao.HabitRecordDao
import com.example.tracker.data.dao.ItemDefinitionDao
import com.example.tracker.data.entity.ExpenseRecord
import com.example.tracker.data.entity.ExpenseSubCategoryDefinition
import com.example.tracker.data.entity.HabitCategoryDefinition
import com.example.tracker.data.entity.HabitDefinition
import com.example.tracker.data.entity.HabitRecord
import com.example.tracker.data.entity.ItemDefinition
import com.example.tracker.data.model.expenseCategories
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/*
1. AppDatabase에 Entity/DAO 등록 확인
2. DAO 함수 반환 타입이 DTO인지 확인
3. DailyViewModel 만들기
4. DailyViewModel에서 DAO 호출해서 DTO를 상태 변수에 저장
5. DailyPage가 ViewModel 상태를 읽도록 수정
6. MainActivity에서 DB 만들고 ViewModel 만들어 DailyPage에 넘김

viewModel : 필요한 데이터를 상태로 들고있고, ui행동에 따라 dao를 호출해서 그 상태를 갱신.
즉 뷰모델은 상태를 전달하는 역할. 이 상태는 ui에의해 dao가 호출될때 갱신됨.
-> 이 화면에 바로 보여야하는 데이터인가? 버튼을 누르면 바뀌고 화면이 다시 그려져야하는가?
-> 현재 화면에 필요한 dao결과만 상태로 감쌈.
변경되는 순간 바로 화면에 반영되어야할것들 = 상태객체로 감싸야함 => 뷰모델에 등록(?)

1. 필요한 것 가져오기(dao) : private val conditionDao: ConditionRecordDao
2. 상태 저장(State) : var conditions by mutableStateOf(...) : 화면이 볼 데이터 저장 -> db호출하면 빈 배열 등 이던게 채워지는거임
3. 행동처리(Function) : fun loadConditions() {
    viewModelScope.launch {
        conditions = conditionDao.getDailyList(...)
    }
} dao 호출, db 읽기, state 갱신 역할을 함
*/

class  DailyViewModel(
    private val expenseRecordDao: ExpenseRecordDao,
    private val habitRecordDao: HabitRecordDao,
    private val conditionRecordDao: ConditionRecordDao, // 변수이면서 생성자 매개변수.
    private val itemDefinitionDao: ItemDefinitionDao,
    private val habitDefinitionDao: HabitDefinitionDao,
    private val habitCategoryDefinitionDao: HabitCategoryDefinitionDao,
    private val expenseSubCategoryDao: ExpenseSubCategoryDao
    /*
    1.
        class DailyViewModel extends ViewModel {
            private final ExpenseRecordDao expenseRecordDao; // final은 반드시 생성될 때 한번 초기화되어야 한다.
            public DailyViewModel(ExpenseRecordDao expenseRecordDao){
                this.expenseRecordDao = expenseRecordDao;
            }
        }
    -> 생성자에 변수를 넣으면 객체를 만드는 순간 값이 정해짐 바로 완성된 객체를 생성함. 반대로 클래스 내부에 선언하면 일단 빈 객체를 만든 후 값을 변경하게 됨
    2. 그러면 DI(또는 ViewModelFactory)가
    그 객체를 넣어준다.

    3. 나는 받은 객체만 사용하면 된다.
    => compose는 완성된 상태 객체를 만들고 copy()로 교체하는 방식이라서.
     */
): ViewModel() {
    var dailyUiState by mutableStateOf(DailyUiState()) // 기본값이 없는것들이 있어서 에러라나
        private set // setter의 접근권한을 바꾸는거 val 은 읽기전용으로 getter만 있다?라던가? 뭘까?

    // function

    init {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        changeDate(today)
    }

    fun changeDate(date: String) {
        dailyUiState = dailyUiState.copy(date = date)
        loadDailyData()
    }

    // 1. DB에서 선택 날짜의 지출 기록을 가져옴
    fun loadDailyData() {
        viewModelScope.launch { // DAO함수가 suspend fun이면 그냥 호출 못하고 코루틴안에서 해야하므로 하는것.
            // Android Jetpack의 ViewModel 라이브러리가 제공하는 것
            val date = dailyUiState.date

            val expenseRecords = expenseRecordDao.getByDate(date)
            val habitRecords = habitRecordDao.getDailyList(date)


            // val 결과목록 = 원본목록.map { 원본한개 ->
            //    결과객체(...)
            //}
            val expenseByCategory = expenseCategories.map { category ->
                val records = expenseRecords.filter { record -> record.categoryId == category.id }
                ExpenseByCategory(
                    categoryName = category.name,
                    categoryId = category.id,
                    recordList = records,
                    totalPrice = records.sumOf { record -> record.totalPrice })
            }
            val habits = habitRecords.groupBy { record -> record.categoryId }.map { (_, records) ->
                HabitCategory(
                    records.firstOrNull()?.categoryName ?: "-",
                    records
                )
            }

            dailyUiState = dailyUiState.copy(
                dailyExpenses = expenseByCategory,
                dailyHabits = habits,
            ) // 3. State에 저장하기. 기본 State를 복사하면서 habits만 바꾼 새 객체를 만드는 함수(copy). 왜냐면 val이라서 바꿀수가 없음

            // 4. dto -> UiState 변환
            // 5. dailyUiState 갱신
        }
    }

    // 지출내역(record) 추가 팝업
    fun openAddExpenseRecord() {
        dailyUiState =
            dailyUiState.copy(expenseRecordForm = ExpenseRecordForm(0L, "", 0L, "", 0L, 0L, 0, ""))
    }

    fun closeExpenseRecordForm() {
        dailyUiState = dailyUiState.copy(expenseRecordForm = null)
    }

    // 2. 지출내역 추가.
    fun addExpenseRecord(form: ExpenseRecordForm) {
        viewModelScope.launch {
            val record = ExpenseRecord(
                date = dailyUiState.date,
                subCategoryId = form.subCategoryId,
                itemId = form.itemId,
                unitPrice = form.unitPrice,
                quantity = form.quantity,
                memo = form.memo
            ) // 폼의 내용을 받아서 디비에 넣기위해 ExpenseRecord로 가공

            expenseRecordDao.addExpenseRecord(record)
            loadDailyData()
        }
    }

    // 수정팝업
    fun openUpdateExpenseRecord(recordId: Long) {
        viewModelScope.launch {
            val formData = expenseRecordDao.getRecordData(recordId)
            dailyUiState = dailyUiState.copy(expenseRecordForm = formData)
        }

    }

    fun closeUpdateExpenseRecord() {
        dailyUiState = dailyUiState.copy(expenseRecordForm = null)
    }

    // 3. 지출내역 수정
    fun updateExpenseRecord(form: ExpenseRecordForm) {
        viewModelScope.launch {
            // val record = expenseRecordDao.getRecord(recordId)
            val record = ExpenseRecord(
                id = form.recordId,
                date = dailyUiState.date,
                subCategoryId = form.subCategoryId,
                itemId = form.itemId,
                unitPrice = form.unitPrice,
                quantity = form.quantity,
                memo = form.memo
            )
            val candidates = expenseRecordDao.findSameExpenseRecord(
                record.date,
                record.itemId,
                record.subCategoryId,
                record.id
            )

            if (candidates == null) { // 중복이 없으면 DB 수정
                expenseRecordDao.update(record)
                loadDailyData()
            }
        }
    }


    // 4. 리코드 삭제
    fun deleteExpenseRecord(recordId: Long) {
        viewModelScope.launch {
            val record = expenseRecordDao.getRecord(recordId)
            expenseRecordDao.delete(record)
            loadDailyData()
        }
    }

    fun searchSubCategory(categoryId: Long, name: String) {
        viewModelScope.launch {
            val subCategories = expenseSubCategoryDao.getByCategoryId(categoryId, name)
            dailyUiState = dailyUiState.copy(expenseSubCategoryCandidates = subCategories)
        }
    }

    fun openAddSubCategory(categoryId: Long) {
        dailyUiState =
            dailyUiState.copy(subCategoryForm = ExpenseSubCategoryDefinition(0L, categoryId, ""))
    }

    fun closeSubCategoryForm() {
        dailyUiState = dailyUiState.copy(subCategoryForm = null)
    }

    fun addSubCategory(subCategory: ExpenseSubCategoryDefinition) {
        viewModelScope.launch {
            val candidate = expenseSubCategoryDao.duplicationTest(
                null,
                subCategory.categoryId,
                subCategory.name
            )

            if (candidate.isEmpty()) {
                val insertedId = expenseSubCategoryDao.insert(subCategory)
                dailyUiState = dailyUiState.copy(
                    expenseRecordForm = dailyUiState.expenseRecordForm?.copy(
                        subCategoryName = subCategory.name,
                        subCategoryId = insertedId
                    ),
                    expenseSubCategoryCandidates = emptyList(),
                    subCategoryForm = null
                )
            }
        }
    }

    fun openUpdateSubCategory(subCategory: ExpenseSubCategoryDefinition) {
        dailyUiState = dailyUiState.copy(subCategoryForm = subCategory)
    }

    fun updateSubCategory(subCategory: ExpenseSubCategoryDefinition) {
        viewModelScope.launch {
            val candidate = expenseSubCategoryDao.duplicationTest(
                subCategory.id,
                subCategory.categoryId,
                subCategory.name
            )

            if (candidate.isEmpty()) {
                expenseSubCategoryDao.update(subCategory)
            }
        }
    }

    // 5. 아이템검색
    fun searchItems(itemName: String) {
        viewModelScope.launch {
            val items = itemDefinitionDao.getByName(itemName)
            dailyUiState = dailyUiState.copy(itemCandidates = items)
        }
    }

    // 아이템 추가 팝업 (초반에 세팅되는 데이터가 달라 추가와 수정 분리함)
    fun openAddItem(name: String) {
        dailyUiState = dailyUiState.copy(
            itemForm = ItemDefinition(
                name = name,
                store = null,
                kcalPerUnit = null,
                defaultPrice = 0L,
                memo = ""
            )
        )
    }

    fun closeItemForm() {
        dailyUiState = dailyUiState.copy(itemForm = null)
    }

    // 6. 아이템 추가
    fun addItem(item: ItemDefinition) {
        viewModelScope.launch {
            // excludeId를 null로 설정
            val candidates = itemDefinitionDao.duplicationTest(
                item.name,
                item.store,
                item.kcalPerUnit,
                item.defaultPrice,
                null
            )
            // dailyUiState = dailyUiState.copy(itemCandidates = candidates, showDuplicateDialog = candidates.isNotEmpty()) //candidates가 있으면 true

            if (candidates.isEmpty()) { // 팝업을 열지 못하면(중복이 없으면) insert, 페이지 리로드
                val insertedId = itemDefinitionDao.insert(item)
                dailyUiState = dailyUiState.copy(
                    expenseRecordForm = dailyUiState.expenseRecordForm?.copy(
                        itemName = item.name,
                        itemId = insertedId,
                        unitPrice = item.defaultPrice
                    ),
                    itemCandidates = emptyList(),
                    itemForm = null
                )
            }

        }
    }

    // 아이템 수정 팝업
    fun openUpdateItem(item: ItemDefinition) {
        dailyUiState = dailyUiState.copy(itemForm = item)
    }

    // 7. 아이템 수정
    fun updateItem(item: ItemDefinition) {
        viewModelScope.launch {
            val candidates = itemDefinitionDao.duplicationTest(
                item.name,
                item.store,
                item.kcalPerUnit,
                item.defaultPrice,
                item.id
            )
            // 팝업에 입력된 내용을 띄워줌
            // dailyUiState = dailyUiState.copy(itemCandidates = candidates, showDuplicateDialog = candidates.isNotEmpty()) // 후보가 있으면 true(중복 데이터 띄움) -> candidate를 띄워주는 기능을 없앰(디비 입력에의 허용/거부만 남김)

            if (candidates.isEmpty()) { // 중복이 없으면 update
                itemDefinitionDao.update(item)
                dailyUiState = dailyUiState.copy(
                    itemForm = null,
                    itemCandidates = emptyList()
                )
            }
        }
    } // 이게 나으려나? 아님 itemId를 받아야하나

    // 8. 삭제
    fun deleteItem(item: ItemDefinition) {
        viewModelScope.launch {
            itemDefinitionDao.delete(item)
            dailyUiState = dailyUiState.copy(itemForm = null, itemCandidates = emptyList())
            loadDailyData()
        }
    }


    // 해빗 입력하기
    // 1. 체크
    fun checkingHabit(record: HabitRecord) {
        viewModelScope.launch {
            habitRecordDao.checkHabit(record)
            loadDailyData()
        }
    }

    // 2. 해빗수정하기
//    fun updateHabit(habitDefinition: HabitDefinition){
//        viewModelScope.launch{
//            habitDefinitionDao.update(habitDefinition)
//            loadDailyData()
//        }
//    }

    // 3. 해빗 추가/수정하기(저장버튼 눌렀을시) 버튼 -> state변경(팝업 등) -> UI변경(컴포즈역할) -> 저장버튼 -> db변경(이때 기존에 있는지도 판단)
    fun updateHabit(habitDefinition: HabitDefinition) {
        viewModelScope.launch {
            val candidate = habitDefinitionDao.findDuplicationDefinition(
                habitDefinition.categoryId,
                habitDefinition.name,
                habitDefinition.id
            )

            if (candidate == null) {
                habitDefinitionDao.update(habitDefinition)
                dailyUiState = dailyUiState.copy(updateHabit = null)
                loadDailyData()
            }
        }
    }

    fun addHabit(habitDefinition: HabitDefinition) {
        viewModelScope.launch {
            val candidate = habitDefinitionDao.findDuplicationDefinition(
                habitDefinition.categoryId,
                habitDefinition.name,
                null
            )

            if (candidate == null) { // 중복이 없는경우 -> insert
                habitDefinitionDao.insert(habitDefinition)
                dailyUiState = dailyUiState.copy(updateHabit = null)
                loadDailyData()
            }
        }
    }

    // 해빗데피니션 수정 팝업
    fun openUpdateHabit(habitDefinitionId: Long) {
        viewModelScope.launch {
            habitDefinitionDao.findDefinition(habitDefinitionId)?.let { habit ->
                dailyUiState = dailyUiState.copy(updateHabit = habit)
            }
        }
    }

    // 해빗데피니션 추가 팝업
    fun openAddHabit(categoryId: Long) {
        dailyUiState = dailyUiState.copy(
            updateHabit = HabitDefinition(
                id = 0L,
                categoryId = categoryId,
                name = "",
                importance = 0
            )
        )
    }

    fun closeHabitForm() {
        dailyUiState = dailyUiState.copy(updateHabit = null)
    }

    // 4. 프로젝트 추가/수정하기
    fun addProject(habitProject: HabitCategoryDefinition) {
        viewModelScope.launch {
            val candidates = habitCategoryDefinitionDao.testDuplication(
                habitProject.name,
                habitProject.endDate,
                habitProject.startDate,
                null
            )

            if (candidates.isEmpty()) {
                habitCategoryDefinitionDao.insert(habitProject)
                dailyUiState = dailyUiState.copy(updateHabitCategory = null)
                loadDailyData()
            }
        }
    }

    //
    fun openAddProject() {
        dailyUiState =
            dailyUiState.copy(updateHabitCategory = HabitCategoryDefinition(0L, "", null, null))
    }

    fun closeHabitCategoryForm() {
        dailyUiState = dailyUiState.copy(updateHabitCategory = null)
    }

    // 5. 프로젝트 수정하기
    fun updateProject(project: HabitCategoryDefinition) {
        viewModelScope.launch {
            val candidates = habitCategoryDefinitionDao.testDuplication(
                project.name,
                project.endDate,
                project.startDate,
                project.id
            )

            if (candidates.isEmpty()) {
                habitCategoryDefinitionDao.update(project)
                dailyUiState = dailyUiState.copy(updateHabitCategory = null)
                loadDailyData()
            }
        }
    }

    fun openUpdateProject(projectId: Long) {
        viewModelScope.launch {
            val habitProject = habitCategoryDefinitionDao.findHabitProject(projectId)
            dailyUiState = dailyUiState.copy(updateHabitCategory = habitProject)
        }
    }

    // 6. 프로젝트 삭제
    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            habitCategoryDefinitionDao.deleteHabitProject(projectId)
            dailyUiState = dailyUiState.copy(updateHabitCategory = null)
            loadDailyData()
        }
    }

    // 7. 해빗(definition) 삭제
    fun deleteHabit(habit: HabitDefinition) {
        viewModelScope.launch {
            habitDefinitionDao.delete(habit)
            loadDailyData()
        }
    }

    fun deleteHabit(habitDefinitionId: Long) {
        viewModelScope.launch {
            habitDefinitionDao.findDefinition(habitDefinitionId)?.let { habit ->
                habitDefinitionDao.delete(habit)
                loadDailyData()
            }
        }
    }


    // condition

}// 트랜잭션으로 중복예방이 아닌 unique 키 추가 -> unique보다는 relation의 id를 사용할일이 없으니 복합주키로 만들어 중복예방


// launch가 왜 suspend 함수를 실행할 수 있는지
