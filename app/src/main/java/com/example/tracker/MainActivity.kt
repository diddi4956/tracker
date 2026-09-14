package com.example.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.tracker.data.database.DatabaseProvider
import androidx.lifecycle.ViewModelProvider
import com.example.tracker.ui.TrackerViewModelFactory
import com.example.tracker.ui.daily.DailyScreen
import com.example.tracker.ui.daily.DailyViewModel
import com.example.tracker.ui.theme.TrackerTheme
import com.example.tracker.ui.tracking.TrackingScreen
import com.example.tracker.ui.tracking.TrackingViewModel


class MainActivity: ComponentActivity() {
    /*  1-1. 코틀린 문법) [:(상속할 클래스))], [변수:(변수타입)], [fun (함수명)(파라미터):(리턴타입)]
        1-2. 근데 여기서는 왜 ComponentActivity가 아닌 ComponentActivity()(생성자)를 쓸까?
        일반 상속: 기능을 물려받는다 / 생성자 호출
        = 자바의 super()와 비슷 = 부모부분을 먼저 초기화(부모의 필드값을 먼저 채움)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = DatabaseProvider.getDatabase(this)

        val factory = TrackerViewModelFactory(db)

        val dailyViewModel = ViewModelProvider(this, factory)[DailyViewModel::class.java]
        val trackingViewModel = ViewModelProvider(this, factory)[TrackingViewModel::class.java]

        setContent {
            TrackerTheme {
                var selectedScreen by remember { mutableStateOf(TrackerScreen.DAILY) }

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedScreen == TrackerScreen.DAILY,
                                onClick = { selectedScreen = TrackerScreen.DAILY },
                                icon = { Text("오늘") },
                                label = { Text("Daily") }
                            )
                            NavigationBarItem(
                                selected = selectedScreen == TrackerScreen.TRACKING,
                                onClick = { selectedScreen = TrackerScreen.TRACKING },
                                icon = { Text("기간") },
                                label = { Text("Tracking") }
                            )
                        }
                    }
                ) { innerPadding ->
                    when (selectedScreen) {
                        TrackerScreen.DAILY -> DailyScreen(dailyViewModel, Modifier.padding(innerPadding))
                        TrackerScreen.TRACKING -> TrackingScreen(trackingViewModel, Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

private enum class TrackerScreen {
    DAILY,
    TRACKING
}

/*
<클래스 구성>
1. 필드(프로퍼티, 값저장)
2. 메서드(함수, 행동)
3. 생성자(객체 만들 때 초기화)
4. 상속/접근제어 등 추가요소

=> 객체 = "상태(필드값) + 행동(메소드 사용 능력) + 타입정보"를 가진 실체
 */
