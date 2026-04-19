package com.example.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // (질문12에서 이어짐)
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember // (질문8에서 이어짐)이건가!! 컴포즈꺼네!!
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tracker.data.database.AppDatabase
import com.example.tracker.data.database.DatabaseProvider
import com.example.tracker.data.entity.DailyEntry
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items


class MainActivity: ComponentActivity(){
    /*  1-1. 코틀린 문법) [:(상속할 클래스))], [변수:(변수타입)], [fun (함수명)(파라미터):(리턴타입)]
        1-2. 근데 여기서는 왜 ComponentActivity가 아닌 ComponentActivity()(생성자)를 쓸까?
        일반 상속: 기능을 물려받는다 / 생성자 호출
        = 자바의 super()와 비슷 = 부모부분을 먼저 초기화(부모의 필드값을 먼저 채움)
     */
    override fun onCreate(savedInstanceState: Bundle?){
        /*  2. Bundle은? 안드로이드 프레임워크가 제공하는 클래스
            +) 여기서 ?는 null가능성 의미
            코틀린 문법) 자바에선 (Bundle savedInstanceState)
         */
        super.onCreate(savedInstanceState)
        /*  3. 오버라이드는 부모함수를 재정의
            super.onCreate()는 부모의 onCreate()를 실행하라
         */
        // 갑자기 궁금해지네. super.onCreate하면 부모의 객체를 아 객체가 아니구나. 그냥 일단 부모의 내용을 실행하는거구나 메소드니까.
        val db = DatabaseProvider.getDatabase(this)
        /*  this) 지금 이 MainActivity를 context(함수의 파라미터)로 넘김
         */

        setContent{ // Composes the given composable into the given activity 이거 확장함수 멤버함수가아님. 생각해보면 당연함 소속이 다른걸
            // Compose UI 트리를 시작 화면을 그릴 구조를 선언.
            /*  4. setContent는 Compose라이브러리가 제공하는 함수.
                그럼 "함수인데 안에 내용을 직접 쓰는구조" 즉, 람다를 파라미터로 넘기는 형태는 어떤식으로 작동이 되느냐 => 함수의 재정의가 아님
                람다를 넘기면 기존의 함수 로직은 어떻게 될까? -> 넘긴 그 코드는 함수 내부에서 실행됨
                일단 코틀린 문법) function(a, {...})대신 function(a){...}
             */
            MaterialTheme{ // customization of your Material Design app
                /*  5. 코틀린에서는 함수 이름도 대문자로 시작할 수 있다.
                    특히 Compose는 UI 요소를 컴포넌트처럼 보이게 하려고 대문자 함수명을 많이 사용함
                 */
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding -> // the basic material design visual layout structure.
                    DailyPage(
                        db = db,
                        modifier = Modifier.padding(innerPadding)
                    )
                    ExpenseRecord(db = db, modifier = Modifier.padding(innerPadding))
                }
                /*  6. innerPadding은 scaffold로부터 넘겨받은 정보. {a->b}이거에서 ->전의 것은 넘겨받은 정보.
                    그렇다면 scaffold(~~){innerPadding -> ~~}에서 scaffold의 이너패딩을 다시 인풋으로 해서 다음 로직을 실행하라라는 람다를 scaffold한테 준거?
                    내가 고민해봤는데, 그럼 함수에 파라미터로 람다를 넘겨주는건 함수가 끝난다음에 이걸 해라라는 느낌으로 주는건가.
                    굳이 함수에 이어서 하는이유는 함수 다음에 하기보다 함수파라미터로 넘기는게 동기화부분에서 유리한가?
                 */
            }
        }
    }
}
@Composable// 화면을 그리는 함수라는 표시를 해주는 어노테이션. compose가 화면으로 취급하는 함수라는 표시.
/*  7. 코틀린은 클래스 밖에 함수를 만들어도됨
    코틀린은 top-level function이 가능. 파일 안에, 클래스 밖에 함수 정의가 가능하다는 뜻.
    그래서 파일 안에 있기 때문에 다른 파일에서는 import를 해야함.
 */
/*
    객체.함수() : 객체의 멤버함수일대
    그냥 함수() : 같은 파일이거나 import한 top-level function일때
    클래스.함수() : companion object/ static비슷한경우 -> 이게 뭐지?
 */
fun DailyPage(db: AppDatabase, modifier: Modifier = Modifier) {
    val dao = db.dailyEntryDao()

    var inputText by remember { mutableStateOf("") }
    /*  8. var, val : var은 재할당 가능 / val은 재할당 불가
     */
    /*  9. by : 위임문법. 이걸 해두면 state.value로 지정하지 않고도 state만으로 value를 출력할 수 있게됨. 단, 위임은 메모리 할당이 아니므로 멤버가 아닌 값을 위임할 수 없음. 또한 멤버값을 읽고 수정할 getter, setter가 필수.
        하 좀 틀렸대...by : 코틀린의 위임 문법.

프로퍼티의 값을 직접 저장하지 않고,
다른 객체에게 읽기/쓰기 동작을 맡긴다.

Compose MutableState와 함께 쓰면
.value 없이 일반 변수처럼 사용할 수 있다.

단, 위임 대상 객체는 값을 읽고 수정할 수 있는
getValue / setValue 규칙을 제공해야 한다.

이게 맞대...
     * */
    /*  10. remember : compose에서의 상태 기억
     */
    var entryList by remember { mutableStateOf(listOf<DailyEntry>()) }
    // 그럼 이거는 컴포저블이 다시 갱신되어도 내용은 기억하고 entryList에서 하나를 지정하지 않아도 된단건가? 해석을 못하겠음ㅋㅋ 적용이 안됨
    // entryList는 컴포저블이 아닌데 컴포저블이 다시 그려져도 기억한다? 아 컴포저블 안의 내용이니까 컴포저블 전체가 갱신돼도 기억한다기보다는, 컴포저블 안의 필드 하나하나에 적용시키는건가보네.
    /*
        11. listOf<(타입)>() : 비어있는 (타입) 리스트 생성
     */
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { // 함수가 실행돼도 항상 다시 실행되진않음. 단순 코드블록이 아닌 Compose가 관리하는 effect객체같은 개념? 키1이 기준이됨 key1이 바뀌면 다시 실행됨
        // 각자의 페이지가 이 함수를 사용하면 각자의 키를 갖게돼서 서로에게 영향은 없음. 이 키값은 동일메소드에서만 유효
        /*  12. Unit : 코틀린에서 제공하는 타입. '의미있는 반환값 없음'
         */
        /*  13. launch는 코루틴을 실행시키는 방법 중 하나. (코루틴은 오래걸릴 수 있는 작업을 비동기적으로 실행되는 작업 흐름. 이 함수의 기능은 디비를 연동)
            LaunchedEffect(date)는 date가 바뀔 때 다시 실행됨. Unit은 변할 일이 없으니 한번만 실행되게됨.
         */
        // 일반 함수는 실행하면 메모리에 호출 스택이 생김. 각 프레임에는 지역변수 리턴주소 매개변수 임시값이 들어감. 함수가 끝나면 pop => 중간저장이 필요없이 한번에 쭉 진행돼서 스택 구조가 효율적임
        // 코루틴은 하다가 멈추고 나중에 이어서 하고 다른 작업으로 전환가능하기도 해야하는데 스택은 멈춘 상태 보관에 약해서, 상태머신을 쓴다고? 코드를 상태머신으로바꿔? 상태머신은 첨듣네.
        // 필요한 변수들은 힙 메모리 객체에 저장(지역변수 일부, 어디까지 실행했는지, 재개위치 등). 돌릴때도 순간적으로 스택을 사용하지만, 진행상태를 스택에만 의존하지않음.
        // 헉 그렇게되면 이상하다? 스레드는 여러개의 메소드가 한번에되는건데 그것도 상태저장이 필요한거아닌가?
        // 스레드는 자기 전용 호출 스택을 가진다네. 코루틴은 적은 수의 스레드 위에서 돌아가므로 각 코루틴마다 전용스택을 줄 수 없어서 상태를 힙객체에 저장
        // os가 스레드를 만들면 각 스레드마다 레지스터상태, 프로그램카운터(pc), 자기호출스택을 줌. 그래서 멀티 스레드는 비싸군 각 스레드마다 메모리(스택공간) os관리비용 컨텍스트스위칭비용이 드니까.
        // 코루틴은 멈출 때 필요한 정보만 힙 객체에 저장.
        // 스레드가 비싼 이유는 1. 전용스택메모리(함수호출용공간), 2. cpu상태정보(레지스터값(현재계산값같은거), pc), 3, os관리구조체(스케줄링 정보 등)
        // 대기중인 코루틴은 스레드 점유 안하고 필요할때만 스레드에서실행함
        entryList =
            dao.getAll() // composable의 DB전체목록을 읽어 entryList를 갱신. 근데 이게 launch~(Unit)안에 있으므로 처음에 한번만 비동기적으로 실행됨
    }//

    Column(modifier = modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("데일리 페이지", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(12.dp)) // 근데 이 함수들 중간에 쉼표 안써주나? => 람다 안에 함수가 여러개 호출된것뿐이라고?

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("메모입력") })

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            scope.launch {
                // -> 바로 써도 되지만 매번 길게 쓰기 귀찮으니까 변수에 담는거라고함. (단지 코드상의 문제가 아니라 불러오는게 여러번이면 작동중에도 성능에 문제가 있을수있겠네.)
                if (inputText.isNotBlank()) {
                    dao.insert(
                        DailyEntry(
                            date = "2026-04-03",
                            memo = inputText
                        )
                    )
                    inputText = ""
                    entryList = dao.getAll()
                }
            }
        }
        ) {
            Text("추가")
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(entryList) { entry -> // 자바로는 for(DailyEntry entry : entryList){...}
                // imtes에 entryList를 넘기고 items가 리스트를 순회하며 원소 각각을 entry로 받아 람다 안에서 사용
                Text("• ${entry.date} / ${entry.memo}")
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
fun ExpenseRecord(db: AppDatabase, modifier: Modifier = Modifier){

}

/*
<내가 몰랐던것들>
1. 코틀린 언어 문법
- val, var
- by
- {}
- ->
- $

2. 안드로이드 기본 구조
- ComponentActivity
- onCreate
- Bundle
- super.onCreate(...)

3. Compose UI 라이브러리
- @Composable
- setContent
- Text
- Column
- Button
- LazyColumn

4. 코루틴/비동기
- launch
- rememberCoroutineScope
- LaunchedEffect
 */

/*
<클래스 구성>
1. 필드(프로퍼티, 값저장)
2. 메서드(함수, 행동)
3. 생성자(객체 만들 때 초기화)
4. 상속/접근제어 등 추가요소

=> 객체 = "상태(필드값) + 행동(메소드 사용 능력) + 타입정보"를 가진 실체
 */