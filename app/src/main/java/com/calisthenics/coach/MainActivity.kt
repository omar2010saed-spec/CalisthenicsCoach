package com.calisthenics.coach

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.calisthenics.coach.logic.AppViewModel
import com.calisthenics.coach.ui.CoachApp
import com.calisthenics.coach.ui.CoachTheme

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoachTheme(theme = vm.state.theme) {
                CoachApp(vm)
            }
        }
    }
}
