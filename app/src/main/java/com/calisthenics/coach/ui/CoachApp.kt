package com.calisthenics.coach.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.calisthenics.coach.logic.AppViewModel

sealed class Route {
    data object Home : Route()
    data object Train : Route()
    data object Library : Route()
    data object Skills : Route()
    data object Progress : Route()
    data object Settings : Route()
    data object Nutrition : Route()
    data object Stages : Route()
    data object Workout : Route()
    data class ExerciseDetail(val id: String) : Route()
    data class SkillDetail(val id: String) : Route()
}

private data class Tab(val route: Route, val label: String, val icon: String)

private val tabs = listOf(
    Tab(Route.Home, "الرئيسية", "🏠"),
    Tab(Route.Train, "التمرين", "💪"),
    Tab(Route.Library, "التمارين", "📚"),
    Tab(Route.Skills, "المهارات", "⭐"),
    Tab(Route.Progress, "التقدم", "📈")
)

@Composable
fun CoachApp(vm: AppViewModel) {
    if (!vm.loaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    if (!vm.state.onboarded) {
        OnboardingScreen(vm)
        return
    }

    val stack = remember { mutableStateListOf<Route>(Route.Home) }
    var tabRoute by remember { mutableStateOf<Route>(Route.Home) }

    fun push(r: Route) {
        stack.add(r)
    }

    fun back() {
        if (stack.size > 1) stack.removeAt(stack.lastIndex)
    }

    fun selectTab(r: Route) {
        tabRoute = r
        stack.clear()
        stack.add(r)
    }

    val current = stack.last()
    val inWorkout = current is Route.Workout
    BackHandler(enabled = stack.size > 1 && !inWorkout) { back() }

    // While a workout is running, back is handled by the workout screen itself.
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!inWorkout) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    tabs.forEach { t ->
                        val selected = tabRoute == t.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { selectTab(t.route) },
                            icon = { Text(t.icon, fontSize = 20.sp) },
                            label = { Text(t.label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = current,
            transitionSpec = {
                (slideInHorizontally { -it / 6 } + fadeIn()) togetherWith (slideOutHorizontally { it / 6 } + fadeOut())
            },
            modifier = Modifier.padding(padding),
            label = "nav"
        ) { route ->
            when (route) {
                Route.Home -> HomeScreen(
                    vm,
                    onTrain = { selectTab(Route.Train) },
                    onOpen = { push(it) }
                )
                Route.Train -> TrainScreen(
                    vm,
                    onStart = {
                        vm.startSession()
                        if (vm.session != null) push(Route.Workout)
                    },
                    onOpen = { push(it) }
                )
                Route.Library -> LibraryScreen(onOpen = { push(Route.ExerciseDetail(it)) })
                Route.Skills -> SkillsScreen(vm, onOpen = { push(Route.SkillDetail(it)) })
                Route.Progress -> ProgressScreen(vm, onOpen = { push(it) })
                Route.Settings -> SettingsScreen(vm, onBack = { back() })
                Route.Nutrition -> NutritionScreen(onBack = { back() })
                Route.Stages -> StagesScreen(vm, onBack = { back() })
                Route.Workout -> {
                    val s = vm.session
                    if (s == null) {
                        LaunchedEffect(Unit) { selectTab(Route.Home) }
                    } else {
                        WorkoutScreen(
                            vm = vm,
                            session = s,
                            onExit = { selectTab(Route.Home) }
                        )
                    }
                }
                is Route.ExerciseDetail -> ExerciseDetailScreen(route.id, vm.state, onBack = { back() }, onOpen = { push(Route.ExerciseDetail(it)) })
                is Route.SkillDetail -> SkillDetailScreen(route.id, vm, onBack = { back() }, onOpen = { push(Route.ExerciseDetail(it)) })
            }
        }
    }
}
