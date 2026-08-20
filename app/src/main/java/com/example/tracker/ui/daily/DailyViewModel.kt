package com.example.tracker.ui.daily

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker.data.dao.ConditionDefinitionDao
import com.example.tracker.data.dao.ConditionRecordDao
import com.example.tracker.data.dao.ExpenseRecordDao
import com.example.tracker.data.dao.ExpenseSubCategoryDao
import com.example.tracker.data.dao.HabitCategoryDefinitionDao
import com.example.tracker.data.dao.HabitDefinitionDao
import com.example.tracker.data.dao.HabitRecordDao
import com.example.tracker.data.dao.ItemDefinitionDao
import com.example.tracker.data.entity.ConditionCheckRecord
import com.example.tracker.data.entity.ConditionDefinition
import com.example.tracker.data.entity.ConditionDefinitionTag
import com.example.tracker.data.entity.ConditionTag
import com.example.tracker.data.entity.ExpenseRecord
import com.example.tracker.data.entity.HabitCategoryDefinition
import com.example.tracker.data.entity.HabitDefinition
import com.example.tracker.data.entity.HabitRecord
import com.example.tracker.data.entity.ItemDefinition
import kotlinx.coroutines.launch

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
    private val conditionDefinitionDao: ConditionDefinitionDao,
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
): ViewModel(){
    // state

    /*
    "var age = 20
        private set"
    은
    "fun getAge() // public
    private fun setAge()" 과 같다. 코틀린 문법이고, 아래거를 생략해주는거임. 프로퍼티선언 + getter + setter를 한꺼번에 쓰는 문법.
     */

    // 제네릭: 함수 설계시, 매개변수의 타입을 지정하지 않음. 자바로치면, 오버라이드 함수이름은 같아도 매개변수가 다르면 다른함수가 되므로 다양한 타입의 매개변수를 받으면서 같은 펑션을 쓰고싶은경우 열심히 생성읋 해야한다는 단점을 극복함.
    var  dailyUiState by mutableStateOf(DailyUiState()) // 기본값이 없는것들이 있어서 에러라나
        private set // val이면 이게 안되네 변수인건가 setter의 접근권한을 바꾸는거 val 은 읽기전용으로 getter만 있다?라던가? 뭘까?

    // function

    // 1. DB에서 선택 날짜의 지출 기록을 가져옴
    fun loadDailyData(){
        viewModelScope.launch { // DAO함수가 suspend fun이면 그냥 호출 못하고 코루틴안에서 해야하므로 하는것.
            // Android Jetpack의 ViewModel 라이브러리가 제공하는 것
            // 1. 지출 조회
            val expenseRecords = expenseRecordDao.getByDate(dailyUiState.date) // List<ExpenseRecord>

            // 2. 해빗 조회
            // habits
            val habitRecords = habitRecordDao.getDailyList(dailyUiState.date) // List<HabitGetDailyListDto> 1. DAO결과 받기
            val habitCategories = habitRecords.groupBy{habit -> habit.categoryName}.map{(categoryName, habitList) -> HabitCategory(categoryName = categoryName, habitList = habitList)} // 2. 카테고리별로 변환하기
            dailyUiState = dailyUiState.copy(habits = habitCategories) // 3. State에 저장하기. 기본 State를 복사하면서 habits만 바꾼 새 객체를 만드는 함수(copy). 왜냐면 val이라서 바꿀수가 없음

            // 3. 컨디션 조회
            val conditionRecord = conditionRecordDao.getDailyList((dailyUiState.date), ) // tagIds리스트 들어가야함. List<ConditionGetDailyListDto>

            // 4. dto -> UiState 변환
            // 5. dailyUiState 갱신
        }
    }

    // 지출내역(record) 추가 팝업
    fun openAddExpenseRecord(){
        dailyUiState = dailyUiState.copy(expenseRecordForm = ExpenseRecordForm(0L, "", 0L, "", 0L, 0L, 0))
    }

    // 2. 지출내역 추가.
    fun addExpenseRecord(record: ExpenseRecord){
       viewModelScope.launch{
           expenseRecordDao.addExpenseRecord(record)
           loadDailyData()
       }
    }

    //
    fun openUpdateExpenseRecord(record: ExpenseRecord){
        viewModelScope.launch{
            val formData = expenseRecordDao.getRecordData(record.id)
            dailyUiState = dailyUiState.copy(expenseRecordForm = formData)
        }

    }
     // 3. 지출내역 수정
    fun updateExpenseRecord(record: ExpenseRecord) {
        viewModelScope.launch{
            val candidates = expenseRecordDao.findSameExpenseRecord(record.date, record.itemId, record.subCategoryId, record.id)

            if(candidates == null) { // 중복이 없으면 DB 수정
                expenseRecordDao.update(record)
                loadDailyData()
            }
        }
    }

//    // 리코드에 대한 팝업(2,3번 전 작업)
//    fun loadExpenseRecord(recordId: Long){
//        viewModelScope.launch{
//            val updateRecord =  expenseRecordDao.getRecordData(recordId)
//            dailyUiState = dailyUiState.copy(updateRecord = updateRecord)
//        }
//    }

    // 4. 리코드 삭제
    fun deleteExpenseRecord(record: ExpenseRecord){
        viewModelScope.launch{
            expenseRecordDao.delete(record)
            loadDailyData()
        }
    } //이래야하나?

    // 5. 아이템검색
    fun searchItems(itemName: String){
        viewModelScope.launch{
            val items = itemDefinitionDao.getByName(itemName)
            dailyUiState = dailyUiState.copy(itemCandidates = items)
            loadDailyData()
        }
    }

    // 아이템 추가 팝업 (초반에 세팅되는 데이터가 달라 추가와 수정 분리함)
    fun openAddItem(){
        dailyUiState = dailyUiState.copy(itemForm = ItemDefinition(subCategoryId = 0L, name = "", store = null, kcalPerUnit = null, defaultPrice = 0L, memo = ""))
    }

    // 6. 아이템 추가
    fun addItem(item: ItemDefinition){
        viewModelScope.launch{
            // excludeId를 null로 설정
            val candidates = itemDefinitionDao.duplicationTest(item.subCategoryId, item.name, item.store, item.kcalPerUnit, item.defaultPrice, null)
            // dailyUiState = dailyUiState.copy(itemCandidates = candidates, showDuplicateDialog = candidates.isNotEmpty()) //candidates가 있으면 true

            if(candidates.isEmpty()){ // 팝업을 열지 못하면(중복이 없으면) insert, 페이지 리로드
                itemDefinitionDao.insert(item)
                loadDailyData()
            }

        }
    }

    // 아이템 수정 팝업
    fun openUpdateItem(item: ItemDefinition){
        dailyUiState = dailyUiState.copy(itemForm = item)
    }

    // 7. 아이템 수정
    fun updateItem(item: ItemDefinition){
        viewModelScope.launch{
            val candidates = itemDefinitionDao.duplicationTest(item.subCategoryId, item.name, item.store, item.kcalPerUnit, item.defaultPrice, item.id)
            // 팝업에 입력된 내용을 띄워줌
            // dailyUiState = dailyUiState.copy(itemCandidates = candidates, showDuplicateDialog = candidates.isNotEmpty()) // 후보가 있으면 true(중복 데이터 띄움) -> candidate를 띄워주는 기능을 없앰(디비 입력에의 허용/거부만 남김)

            if(candidates.isEmpty()){ // 중복이 없으면 update
                itemDefinitionDao.update(item)
                loadDailyData() // 수정이 됐을때만 화면 리로드
            }
        }
    } // 이게 나으려나? 아님 itemId를 받아야하나

    // 8. 삭제
    fun deleteItem(item: ItemDefinition){
        viewModelScope.launch{
            itemDefinitionDao.delete(item)
            loadDailyData()
        }
    }


    // 해빗 입력하기
    // 1. 체크
    fun checkingHabit(record: HabitRecord){
        viewModelScope.launch{
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
    fun updateHabit(habitDefinition: HabitDefinition){
        viewModelScope.launch{
            habitDefinitionDao.update(habitDefinition)
            loadDailyData()
        }
    }

    fun addHabit(habitDefinition: HabitDefinition){
        viewModelScope.launch{
            habitDefinitionDao.insert(habitDefinition)
            loadDailyData()
        }
    }
    fun openUpdateHabit(habitId: Long){ // 해빗 수정을 눌렀을 때 팝업창 채워줌
        viewModelScope.launch{
            val definition = habitDefinitionDao.findDefinition(habitId)
            dailyUiState = dailyUiState.copy(updateHabit = definition)
        }
    }

    fun openAddHabit(){
        dailyUiState = dailyUiState.copy(updateHabit = HabitDefinition(id = null, categoryId = null, name = "", )
    }

    // 4. 프로젝트 추가/수정하기
    fun addProject(habitProject: HabitCategoryDefinition){
        viewModelScope.launch{
            habitCategoryDefinitionDao.insert(habitProject) // 트랜잭션 아직 덜함
            loadDailyData()
        }
    }

    // 5. 프로젝트 수정하기
    fun updateProject(project: HabitCategoryDefinition){
        viewModelScope.launch{
            habitCategoryDefinitionDao.update(project)
            loadDailyData()
        }
    }

    fun loadHabitProject(projectId: Long){
        viewModelScope.launch{
            val habitProject = habitCategoryDefinitionDao.findHabitProject(projectId)
            dailyUiState = dailyUiState.copy(updateHabitCategory = habitProject)
        }
    }

    // 6. 프로젝트 삭제
    fun deleteProject(project: HabitCategoryDefinition){
        viewModelScope.launch{
            habitCategoryDefinitionDao.delete(project)
            loadDailyData()
        }
    }

    // 7. 해빗(definition) 삭제
    fun deleteHabit(habit: HabitDefinition){
        viewModelScope.launch{
            habitDefinitionDao.delete(habit)
            loadDailyData()
        }
    }


    // condition
    // 체크
    fun checkCondition(record: ConditionCheckRecord){
        viewModelScope.launch{
            conditionRecordDao.checkingRecordAndDefinitionFrequency(record)
            loadDailyData()
        }
    }
    // 1. 검색창
    fun searchCondition(string: String){
        viewModelScope.launch{
            conditionDefinitionDao.getByName(string)
            loadDailyData()
        }
    }

    // 2.
    fun addConditionDefinition(condition: ConditionDefinition, tagIds:List<Long>){
        viewModelScope.launch{
            conditionDefinitionDao.insertDefinitionWithTag(condition, tagIds)
            loadDailyData()
        }
    }

    // 3.
    fun updateCondition(condition: ConditionDefinition){
        viewModelScope.launch{
            conditionDefinitionDao.updateDefinition(condition)
            loadDailyData()
        }
    }

    // 4. 태그연결하기(데피니션에서 태그 선택해서 추가하기) conditionDefinitionId를 사용하여 relation만듦
    fun addTagToDefinition(conditionDefinition: ConditionDefinition, conditionTagIds: List<Long>){
        viewModelScope.launch{
            val relations = conditionTagIds.map { conditionTagId ->
                ConditionDefinitionTag(
                    conditionDefinitionId = conditionDefinition.id,
                    tagId = conditionTagId
                )
            }
            conditionDefinitionDao.insertDefinitionTag(relations)
            loadDailyData()
        }
    }

    // 5. 태그연결하기(태그에서 컨디션 추가하기)
    fun addDefinitionToTag(conditionTag: ConditionTag, conditionDefinitionIds:List<Long>){
        viewModelScope.launch{
            val relations = conditionDefinitionIds.map{definitionId -> ConditionDefinitionTag(
                conditionDefinitionId = definitionId,
                tagId = conditionTag.id)
            }
            conditionDefinitionDao.insertDefinitionTag(relations)
            loadDailyData()
        }
    }

    // 6. 태그 검색하기
    fun searchTag(string: String){
        viewModelScope.launch{
            conditionDefinitionDao.getByTagName(string)
            loadDailyData()
        }
    }

    // 7. 태그 추가하기
    fun addTag(tag: ConditionTag){
        viewModelScope.launch{
            conditionDefinitionDao.insertTag(tag) // 근데 태그는 데피니션이 필수가 아닌가? 굳이인가보당 하긴
            loadDailyData()
        }
    }

    // 8. 태그 수정하기
    fun updateTag(tag: ConditionTag){
        viewModelScope.launch{
            conditionDefinitionDao.updateTag(tag)
            loadDailyData()
        }
    }

    // 9. 삭제. 태그랑 컨디션 데피니션 삭제
    fun deleteTag(tag: ConditionTag){
        viewModelScope.launch{
            conditionDefinitionDao.deleteRelationByTag(tag)
            loadDailyData()
        }
    }
    fun deleteConditionDefinition(condition:ConditionDefinition){
        viewModelScope.launch{
            conditionDefinitionDao.deleteRelationByDefinition(condition)
            loadDailyData()
        }
    }
    fun deleteRelation(relation: ConditionDefinitionTag){
        viewModelScope.launch {
            conditionDefinitionDao.deleteRelation(relation)
            loadDailyData()
        }
    }// 트랜잭션으로 중복예방이 아닌 unique 키 추가 -> unique보다는 relation의 id를 사용할일이 없으니 복합주키로 만들어 중복예방


}
// launch가 왜 suspend 함수를 실행할 수 있는지