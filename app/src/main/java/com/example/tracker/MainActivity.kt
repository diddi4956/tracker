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
import com.example.tracker.ui.daily.DailyPage


class MainActivity: ComponentActivity() {
    /*  1-1. 코틀린 문법) [:(상속할 클래스))], [변수:(변수타입)], [fun (함수명)(파라미터):(리턴타입)]
        1-2. 근데 여기서는 왜 ComponentActivity가 아닌 ComponentActivity()(생성자)를 쓸까?
        일반 상속: 기능을 물려받는다 / 생성자 호출
        = 자바의 super()와 비슷 = 부모부분을 먼저 초기화(부모의 필드값을 먼저 채움)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
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

        setContent {
            DailyPage()
        }
    }
}

/*
<클래스 구성>
1. 필드(프로퍼티, 값저장)
2. 메서드(함수, 행동)
3. 생성자(객체 만들 때 초기화)
4. 상속/접근제어 등 추가요소

=> 객체 = "상태(필드값) + 행동(메소드 사용 능력) + 타입정보"를 가진 실체
 */