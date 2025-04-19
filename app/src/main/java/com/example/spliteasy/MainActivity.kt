package com.example.spliteasy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.spliteasy.auth.AuthManager
import com.example.spliteasy.ui.theme.SplitEasyTheme
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import kotlinx.coroutines.launch
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.spliteasy.aichat.AIChatScreen
import com.example.spliteasy.aichat.AIChatViewModel
import com.example.spliteasy.api.ExampleUsers
import com.example.spliteasy.api.User
import com.example.spliteasy.api.getGroupsByUserId
import com.example.spliteasy.api.getMembersByGroupId
import com.example.spliteasy.api.getTransactionsByGroupId
import com.example.spliteasy.api.Group
import com.example.spliteasy.api.getGroupByGroupId
import com.example.spliteasy.api.getMemberByGroupIdAndMemberId
import com.example.spliteasy.api.addGroup
import com.example.spliteasy.api.addMember
import com.example.spliteasy.api.getTransactionById
import com.example.spliteasy.group.GroupScreen
import com.example.spliteasy.group.GroupsScreen
import com.example.spliteasy.member.MemberScreen
import com.example.spliteasy.member.MembersScreen
import com.example.spliteasy.member.AddMemberScreen
import com.example.spliteasy.transaction.TransactionScreen
import com.example.spliteasy.transaction.TransactionsScreen
import com.example.spliteasy.group.AddGroupScreen
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    private val user: User = ExampleUsers[0]
    private val groups: List<Group> = getGroupsByUserId(user.id)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge rendering
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // Set up system UI controller for full screen
            FullScreenSetup()
            SplitEasyTheme {
                val navController = rememberNavController()
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(300)
                            )
                        },
                        popEnterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(300)
                            )
                        }
                    ) {
                        composable("login") {
                            LoginScreen(onSignInSuccess = {
                                navController.navigate("group") {
                                    popUpTo("login") { inclusive = true }
                                }
                            })
                        }

                        composable("group") {
                            var showDialog by remember { mutableStateOf(false) }

                            BackHandler { showDialog = true }

                            if (showDialog) {
                                AlertDialog(
                                    onDismissRequest = { showDialog = false },
                                    title = { Text("Log out?") },
                                    text = { Text("Are you sure you want to log out and go back to login?") },
                                    confirmButton = {
                                        Button(onClick = {
                                            showDialog = false
                                            lifecycleScope.launch {
                                                AuthManager.signOut(this@MainActivity)
                                                navController.navigate("login") {
                                                    popUpTo("group") { inclusive = true }
                                                }
                                            }
                                        }) { Text("Log out") }
                                    },
                                    dismissButton = {
                                        OutlinedButton(onClick = { showDialog = false }) {
                                            Text("Cancel")
                                        }
                                    }
                                )
                            }

                            GroupsScreen(
                                groupList = groups,
                                onBackClick = {showDialog = true},
                                onGroupEnterClick={navController.navigate("group/${it.id}")},
                                onGroupLeaveClick = {},
                                onAddGroupClick = {navController.navigate("group/add")}
                            )
                        }

                        composable(
                            "group/{groupId}",
                            arguments = listOf(
                                navArgument("groupId") { type = NavType.StringType }
                            )
                        ) { entry ->
                            val groupId = entry.arguments?.getString("groupId") ?: ""
                            GroupScreen(
                                group=getGroupByGroupId(groupId),
                                onBackClick = { navController.popBackStack()},
                                onOverviewClick = { navController.navigate("group/$groupId/members") },
                                onTransactionsClick = { navController.navigate("group/$groupId/transactions") },
                                onSummaryClick = { navController.navigate("group") },
                                onAIQueryClick={navController.navigate("ai/you are an assistant to help me organise the expenses of my group ${getGroupByGroupId(groupId)}: You help solve any query I have regarding this")}
                            )
                        }

                        composable(
                            "group/add"
                        ){
                            AddGroupScreen(
                                onAddButtonClicked = {group->addGroup(group)},
                                onBackButtonClicked = {navController.popBackStack()}
                            )
                        }

                        composable(
                            "group/{groupId}/members",
                            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
                        ) { entry ->
                            val groupId = entry.arguments?.getString("groupId") ?: ""
                            MembersScreen(
                                group = getGroupByGroupId(groupId),
                                members = getMembersByGroupId(groupId),
                                onMemberClick = { memberId ->
                                    navController.navigate("group/$groupId/member/$memberId")
                                },
                                onAddMemberClick = {
                                    navController.navigate("group/$groupId/member/add")
                                },
                                onMemberRemove = { memberId ->

                                },
                                onBackClick = { navController.popBackStack()}
                            )
                        }

                        composable(
                            "group/{groupId}/member/{memberId}",
                            arguments = listOf(
                                navArgument("groupId") { type = NavType.StringType },
                                navArgument("memberId") { type = NavType.StringType }
                            )
                        ){
                            val groupId= it.arguments?.getString("groupId") ?: ""
                            val memberId = it.arguments?.getString("memberId") ?: ""
                            MemberScreen(
                                group=getGroupByGroupId(groupId),
                                member=getMemberByGroupIdAndMemberId(groupId,memberId),
                                onExpenditureClick = {},
                                onBackClick = { navController.popBackStack()} )
                        }

                        composable(
                            "group/{groupId}/member/add",
                            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
                        ) { entry ->
                            val groupId = entry.arguments?.getString("groupId") ?: ""
                            AddMemberScreen(
                                group = getGroupByGroupId(groupId),
                                onAddButtonClicked = {
                                    addMember(it,groupId)
                                    navController.popBackStack()
                                },
                                onBackButtonClicked = { navController.popBackStack() }
                            )
                        }



                        composable(
                            "group/{groupId}/transactions",
                            arguments = listOf(
                                navArgument("groupId") { type = NavType.StringType },
                            )
                        ){
                            val groupId= it.arguments?.getString("groupId") ?: ""
                            TransactionsScreen(
                                group = getGroupByGroupId(groupId),
                                transactions = getTransactionsByGroupId(groupId),
                                onBackClick = { navController.popBackStack()},
                                onRefreshClick = {},
                                onTransactionClick = {
                                        transactionId ->
                                    navController.navigate("transaction/$transactionId")
                                },
                                onAddTransaction = {}
                            )
                        }

                        composable(
                            "transaction/{transactionId}",
                            arguments = listOf(
                                navArgument("transactionId") { type = NavType.StringType },
                            )
                        ){
                            val transactionId = it.arguments?.getString("transactionId") ?: ""
                            TransactionScreen(
                                transaction = getTransactionById(transactionId),
                                onBackClick = { navController.popBackStack()},
                                onEditClick = {},
                                onDeleteClick = {}
                            )
                        }

                        composable(
                            "ai/{prompt}",
                            arguments= listOf(
                                navArgument("prompt") { type = NavType.StringType },
                            )){
                            val prompt = it.arguments?.getString("prompt") ?: ""
                            AIChatScreen(AIChatViewModel(prompt),onBackClick={ navController.popBackStack()})
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun FullScreenSetup() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()

    DisposableEffect(systemUiController, useDarkIcons) {
        // Make status bar visible but transparent
        systemUiController.isStatusBarVisible = true

        // Set navigation bar color to transparent
        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )

        // Set status bar color to transparent
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )

        // Make the system bars draw over your content
        systemUiController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        onDispose {}
    }
}