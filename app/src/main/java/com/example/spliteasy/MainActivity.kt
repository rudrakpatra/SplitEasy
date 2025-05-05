package com.example.spliteasy
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.BackHandler
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.Button
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedButton
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.lifecycle.lifecycleScope
//import androidx.navigation.NavType
//import androidx.navigation.compose.*
//import androidx.navigation.navArgument
//import com.example.spliteasy.auth.AuthManager
//import com.example.spliteasy.ui.theme.SplitEasyTheme
//import androidx.compose.animation.slideInHorizontally
//import androidx.compose.animation.slideOutHorizontally
//import androidx.compose.animation.fadeIn
//import kotlinx.coroutines.launch
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.isSystemInDarkTheme
//import androidx.compose.ui.graphics.Color
//import androidx.core.view.WindowCompat
//import androidx.core.view.WindowInsetsControllerCompat
//import com.example.spliteasy.account.AccountScreen
//import com.example.spliteasy.account.AccountsScreen
//import com.example.spliteasy.account.AddAccountScreen
//import com.example.spliteasy.aichat.AIChatScreen
//import com.example.spliteasy.aichat.AIChatViewModel
//import com.example.spliteasy.group.GroupScreen
//import com.example.spliteasy.group.GroupsScreen
//import com.example.spliteasy.group.AddGroupScreen
//import com.example.spliteasy.transaction.TransactionsScreen
//import com.google.accompanist.systemuicontroller.rememberSystemUiController
//import com.google.firebase.firestore.FirebaseFirestore
//
//class MainActivity : ComponentActivity() {
//    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
//    private var userId: String = ""
//    private var group: Group = Group()
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        // Enable edge-to-edge rendering
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//
//        setContent {
//            // Set up system UI controller for full screen
//            FullScreenSetup()
//            SplitEasyTheme {
//                val navController = rememberNavController()
//                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
//                    NavHost(
//                        navController = navController,
//                        startDestination = "login",
//                        enterTransition = {
//                            slideInHorizontally(
//                                initialOffsetX = { it },
//                                animationSpec = tween(300)
//                            ) + fadeIn(animationSpec = tween(300))
//                        },
//                        exitTransition = {
//                            slideOutHorizontally(
//                                targetOffsetX = { -it },
//                                animationSpec = tween(300)
//                            )
//                        },
//                        popEnterTransition = {
//                            slideInHorizontally(
//                                initialOffsetX = { -it },
//                                animationSpec = tween(300)
//                            ) + fadeIn(animationSpec = tween(300))
//                        },
//                        popExitTransition = {
//                            slideOutHorizontally(
//                                targetOffsetX = { it },
//                                animationSpec = tween(300)
//                            )
//                        }
//                    ) {
//                        composable("login") {
//                            LoginScreen(onSignInSuccess = { firebaseUser ->
//                                userId=firebaseUser.uid
//                                navController.navigate("groups") {
//                                    popUpTo("login") { inclusive = true }
//                                }
//                            })
//                        }
//
//                        composable("groups") {
//                            var showDialog by remember { mutableStateOf(false) }
//
//                            BackHandler { showDialog = true }
//
//                            if (showDialog) {
//                                AlertDialog(
//                                    onDismissRequest = { showDialog = false },
//                                    title = { Text("Log out?") },
//                                    text = { Text("Are you sure you want to log out and go back to login?") },
//                                    confirmButton = {
//                                        Button(onClick = {
//                                            showDialog = false
//                                            lifecycleScope.launch {
//                                                AuthManager.signOut(this@MainActivity)
//                                                navController.navigate("login") {
//                                                    popUpTo("group") { inclusive = true }
//                                                }
//                                            }
//                                        }) { Text("Log out") }
//                                    },
//                                    dismissButton = {
//                                        OutlinedButton(onClick = { showDialog = false }) {
//                                            Text("Cancel")
//                                        }
//                                    }
//                                )
//                            }
//
//                            GroupsScreen(
//                                userId = userId,
//                                onBackClick = { showDialog = true },
//                                onGroupEnterClick = {
//                                    group=it
//                                    navController.navigate("groups/${it.id}")},
//                                onGroupLeaveClick = {},
//                                onAddGroupClick = { navController.navigate("groups/add")}
//                            )
//                        }
//
//                        composable(
//                            "groups/{groupId}",
//                            arguments = listOf(
//                                navArgument("groupId") { type = NavType.StringType }
//                            )
//                        ) { entry ->
//                            val groupId = entry.arguments?.getString("groupId") ?: ""
//                            GroupScreen(
//                                groupId=groupId,
//                                groupDefault = group,
//                                onBackClick = { navController.popBackStack()},
//                                onAccountsClick = { navController.navigate("groups/$groupId/accounts") },
//                                onTransactionsClick = { navController.navigate("groups/$groupId/transactions") },
//                                onSummaryClick = { navController.navigate("groups") },
//                                onAIQueryClick={navController.navigate("ai/you are an assistant to help me organise the expenses of my group : You help solve any query I have regarding this")}
//                            )
//                        }
//
//                        composable(
//                            "groups/add"
//                        ){
//                            AddGroupScreen(
//                                onAddButtonClicked = {group->
//
//                                    lifecycleScope.launch{
//                                        val user=Users.get(db,userId)!!
//                                        Groups.joinNewGroup(db,group,user.id,user.name)
//                                        navController.popBackStack()
//                                    }
//                                 },
//                                onBackButtonClicked = {navController.popBackStack()}
//                            )
//                        }
//
//                        composable(
//                            "groups/{groupId}/accounts",
//                            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
//                        ) { entry ->
//                            val groupId = entry.arguments?.getString("groupId") ?: ""
//                            AccountsScreen(
//                                groupId,
//                                onAccountClick = { accountId ->
//                                    navController.navigate("groups/$groupId/accounts/$accountId")
//                                },
//                                onAddAccountClick = {
//                                    navController.navigate("groups/$groupId/accounts/add")
//                                },
//                                onAccountRemove = { accountId ->
//
//                                },
//                                onBackClick = { navController.popBackStack()}
//                            )
//                        }
//
//                        composable(
//                            "groups/{groupId}/accounts/{accountId}",
//                            arguments = listOf(
//                                navArgument("groupId") { type = NavType.StringType },
//                                navArgument("accountId") { type = NavType.StringType }
//                            )
//                        ){
//                            val groupId= it.arguments?.getString("groupId") ?: ""
//                            val accountId = it.arguments?.getString("accountId") ?: ""
//                            AccountScreen(
//                                groupId,
//                                accountId,
//                                onExpenditureClick = {},
//                                onBackClick = { navController.popBackStack()}
//                            )
//                        }
//
//                        composable(
//                            "groups/{groupId}/accounts/add",
//                            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
//                        ) { entry ->
//                            val groupId = entry.arguments?.getString("groupId") ?: ""
//                            AddAccountScreen(
//                                groupId,
//                                onUserSelected = {user->
//                                    lifecycleScope.launch {
//                                        Accounts.add(db,groupId,Account(user = db.document("/users/${user.id}")))
//                                    }
//                                    navController.popBackStack()
//                                },
//                                onBackButtonClicked = { navController.popBackStack() }
//                            )
//                        }
//
//                        composable(
//                            "groups/{groupId}/transactions",
//                            arguments = listOf(
//                                navArgument("groupId") { type = NavType.StringType },
//                            )
//                        ){
//                            val groupId= it.arguments?.getString("groupId") ?: ""
//                            TransactionsScreen(
//                                groupId,
//                                onBackClick = { navController.popBackStack()},
//                                onTransactionClick = {
//                                        transactionId ->
//                                    navController.navigate("group/$groupId/transaction/$transactionId")
//                                },
//                                onAddTransaction = { navController.navigate("group/$groupId/transaction/add")}
//                            )
//                        }
////
////                        composable(
////                            "group/{groupId}/transaction/{transactionId}",
////                            arguments = listOf(
////                                navArgument("transactionId") { type = NavType.StringType },
////                            )
////                        ){
////                            val transactionId = it.arguments?.getString("transactionId") ?: ""
////                            TransactionScreen(
////                                transaction = getTransactionById(transactionId),
////                                onBackClick = { navController.popBackStack()},
////                                onEditClick = {},
////                                onDeleteClick = {}
////                            )
////                        }
////
////                        composable(
////                            "group/{groupId}/expenses/add",
////                            arguments = listOf(
////                                navArgument("groupId") { type = NavType.StringType },
////                            )
////                        ){
////                            val groupId = it.arguments?.getString("groupId") ?: ""
////                            AddTransactionScreen(
////                                members = getMembersByGroupId(groupId),
////                                onBackButtonClicked = { navController.popBackStack()},
////                                onTransactionComplete = {navController.popBackStack()},
////                            )
////                        }
//
//                        composable(
//                            "ai/{prompt}",
//                            arguments= listOf(
//                                navArgument("prompt") { type = NavType.StringType },
//                            )){
//                            val prompt = it.arguments?.getString("prompt") ?: ""
//                            AIChatScreen(AIChatViewModel(prompt),onBackClick={ navController.popBackStack()})
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//@Composable
//fun FullScreenSetup() {
//    val systemUiController = rememberSystemUiController()
//    val useDarkIcons = !isSystemInDarkTheme()
//
//    DisposableEffect(systemUiController, useDarkIcons) {
//        // Make status bar visible but transparent
//        systemUiController.isStatusBarVisible = true
//
//        // Set navigation bar color to transparent
//        systemUiController.setNavigationBarColor(
//            color = Color.Transparent,
//            darkIcons = useDarkIcons
//        )
//
//        // Set status bar color to transparent
//        systemUiController.setStatusBarColor(
//            color = Color.Transparent,
//            darkIcons = useDarkIcons
//        )
//
//        // Make the system bars draw over your content
//        systemUiController.systemBarsBehavior =
//            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//
//        onDispose {}
//    }
//}