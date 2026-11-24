package com.akiba.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.akiba.app.data.local.AkibaDatabase
import com.akiba.app.data.repository.AkibaRepository
import com.akiba.app.ui.screens.CreateGroupScreen
import com.akiba.app.ui.screens.GroupDashboardScreen
import com.akiba.app.ui.screens.HomeGroupsScreen
import com.akiba.app.ui.theme.AkibaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AkibaDatabase.getDatabase(applicationContext)
        val repository = AkibaRepository(database.akibaDao())

        setContent {
            AkibaTheme {
                val navController = rememberNavController()
                val repo = remember { repository }

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeGroupsScreen(
                            repository = repo,
                            onOpenGroup = { groupId -> navController.navigate("group/$groupId") },
                            onCreateGroupClick = { navController.navigate("create_group") }
                        )
                    }
                    composable("create_group") {
                        CreateGroupScreen(
                            repository = repo,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("group/{groupId}") { backStackEntry ->
                        val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
                        GroupDashboardScreen(
                            groupId = groupId,
                            repository = repo,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
