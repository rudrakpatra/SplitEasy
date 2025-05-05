package com.example.spliteasy

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseClientTest : ComponentActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var testUserId: String
    private lateinit var testUserName: String
    private var testGroupRef: DocumentReference? = null
    private var testUserRef: DocumentReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        db = Firebase.firestore

        // Set up test user (in a real app, this would be authenticated)
        testUserId = "test_user_${System.currentTimeMillis()}"
        testUserName = "Test User"
        testUserRef = db.collection("users").document(testUserId)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FirebaseTestScreen(this)
                }
            }
        }
    }

    // Test helper functions
    private fun logResult(operation: String, result: String) {
        Log.d("FirebaseTest", "[$operation] $result")
    }

    fun runTest(testName: String, onSuccess: (String) -> Unit, onError: (String) -> Unit, testFn: suspend () -> String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = testFn()
                logResult(testName, "SUCCESS: $result")
                withContext(Dispatchers.Main) {
                    onSuccess(result)
                }
            } catch (e: Exception) {
                val errorMsg = "FAILED: ${e.message}"
                logResult(testName, errorMsg)
                withContext(Dispatchers.Main) {
                    onError(errorMsg)
                }
            }
        }
    }

    // Test operations
    fun testCreateUser(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        runTest("CreateUser", onSuccess, onError) {
            createUser(db, testUserId, testUserName)
            "Created user $testUserName with ID $testUserId"
        }
    }

    fun testGetUser(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        runTest("GetUser", onSuccess, onError) {
            val user = getUser(db, testUserId)
            "Retrieved user: ${user.name}"
        }
    }

    fun testCreateGroup(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        runTest("CreateGroup", onSuccess, onError) {
            val groupName = "Test Group"
            val groupDesc = "A group for testing Firebase operations"
            testGroupRef = createGroup(db, groupName, groupDesc)
            "Created group '$groupName' with ID ${testGroupRef?.id}"
        }
    }

    fun testJoinGroup(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        runTest("JoinGroup", onSuccess, onError) {
            testGroupRef?.let { groupRef ->
                testUserRef?.let { userRef ->
                    joinGroup(db, groupRef, userRef)
                    "User $testUserName joined group ${groupRef.id}"
                } ?: throw Exception("User reference not initialized")
            } ?: throw Exception("Group reference not initialized")
        }
    }

    fun testGetUserGroups(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        runTest("GetUserGroups", onSuccess, onError) {
            testUserRef?.let { userRef ->
                val groups = getUserGroupSummaries(userRef)
                "User is in ${groups.size} groups: ${groups.joinToString { it.name }}"
            } ?: throw Exception("User reference not initialized")
        }
    }

    fun testGetGroupData(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        runTest("GetGroupData", onSuccess, onError) {
            testGroupRef?.let { groupRef ->
                val group = getGroupData(groupRef)
                "Group data: ${group.name}, ${group.description}, ID: ${group.id}"
            } ?: throw Exception("Group reference not initialized")
        }
    }

    fun testGetGroupAccounts(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        runTest("GetGroupAccounts", onSuccess, onError) {
            testGroupRef?.let { groupRef ->
                val accounts = getGroupAccounts(groupRef)
                "Group has ${accounts.size} accounts: ${accounts.joinToString { it.nickname }}"
            } ?: throw Exception("Group reference not initialized")
        }
    }

    fun testCreateProposal(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        runTest("CreateProposal", onSuccess, onError) {
            testGroupRef?.let { groupRef ->
                // Get the user's account summary
                val accounts = getGroupAccounts(groupRef)
                val userAccount = accounts.find { it.nickname == testUserName }
                    ?: throw Exception("User account not found in group")

                // Create a simple expense payment
                val payment = Payment(
                    title = "Test Payment",
                    type = PaymentType.EXPENSE,
                    creatorSummary = userAccount,
                    sources = listOf(
                        Source(
                            accountSummary = userAccount,
                            amount = 50.0,
                            currency = "USD"
                        )
                    ),
                    destinations = listOf(
                        Destination(
                            accountSummary = userAccount,
                            bill = listOf(
                                BillEntry(
                                    item = "Test Item",
                                    amount = 50.0,
                                    multiplier = 1.0
                                )
                            )
                        )
                    )
                )

                val proposalRef = createProposal(db, groupRef, payment)
                "Created payment proposal with ID ${proposalRef.id}"
            } ?: throw Exception("Group reference not initialized")
        }
    }

    fun testGetGroupProposals(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        runTest("GetGroupProposals", onSuccess, onError) {
            testGroupRef?.let { groupRef ->
                val proposals = getGroupProposals(groupRef)
                "Group has ${proposals.size} proposals: ${proposals.joinToString { it.title }}"
            } ?: throw Exception("Group reference not initialized")
        }
    }

    fun testApproveProposal(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        runTest("ApproveProposal", onSuccess, onError) {
            testGroupRef?.let { groupRef ->
                // Get proposals
                val proposals = getGroupProposals(groupRef)
                if (proposals.isEmpty()) {
                    throw Exception("No proposals found to approve")
                }

                // Get user account
                val accounts = getGroupAccounts(groupRef)
                val userAccount = accounts.find { it.nickname == testUserName }
                    ?: throw Exception("User account not found in group")

                // Approve the first proposal
                val proposalRef = proposals[0].ref
                    ?: throw Exception("Proposal reference is null")

                editProposalApproval(db, proposalRef, userAccount, true)
                "Approved proposal: ${proposals[0].title}"
            } ?: throw Exception("Group reference not initialized")
        }
    }

    fun testFinalizeProposal(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        runTest("FinalizeProposal", onSuccess, onError) {
            testGroupRef?.let { groupRef ->
                // Get proposals
                val proposals = getGroupProposals(groupRef)
                if (proposals.isEmpty()) {
                    throw Exception("No proposals found to finalize")
                }

                // Finalize the first proposal
                val proposalRef = proposals[0].ref
                    ?: throw Exception("Proposal reference is null")

                finalizeProposal(db, proposalRef)
                "Finalized proposal: ${proposals[0].title}"
            } ?: throw Exception("Group reference not initialized")
        }
    }

    fun testGetGroupPayments(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        runTest("GetGroupPayments", onSuccess, onError) {
            testGroupRef?.let { groupRef ->
                val payments = getGroupPayments(groupRef)
                "Group has ${payments.size} finalized payments: ${payments.joinToString { it.title }}"
            } ?: throw Exception("Group reference not initialized")
        }
    }

    fun testLeaveGroup(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        runTest("LeaveGroup", onSuccess, onError) {
            testGroupRef?.let { groupRef ->
                testUserRef?.let { userRef ->
                    leaveGroup(db, userRef, groupRef)
                    "User $testUserName left group ${groupRef.id}"
                } ?: throw Exception("User reference not initialized")
            } ?: throw Exception("Group reference not initialized")
        }
    }

    fun runAllTests(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        var currentStep = 1
        var totalSteps = 10
        var results = ""

        fun runNextTest() {
            when (currentStep) {
                1 -> testCreateUser({ result ->
                    results += "1. Create User: SUCCESS\n"
                    currentStep++
                    runNextTest()
                }, { error ->
                    results += "1. Create User: $error\n"
                    onError(results)
                })

                2 -> testGetUser({ result ->
                    results += "2. Get User: SUCCESS\n"
                    currentStep++
                    runNextTest()
                }, { error ->
                    results += "2. Get User: $error\n"
                    onError(results)
                })

                3 -> testCreateGroup({ result ->
                    results += "3. Create Group: SUCCESS\n"
                    currentStep++
                    runNextTest()
                }, { error ->
                    results += "3. Create Group: $error\n"
                    onError(results)
                })

                4 -> testJoinGroup({ result ->
                    results += "4. Join Group: SUCCESS\n"
                    currentStep++
                    runNextTest()
                }, { error ->
                    results += "4. Join Group: $error\n"
                    onError(results)
                })

                5 -> testGetUserGroups({ result ->
                    results += "5. Get User Groups: SUCCESS\n"
                    currentStep++
                    runNextTest()
                }, { error ->
                    results += "5. Get User Groups: $error\n"
                    onError(results)
                })

                6 -> testGetGroupAccounts({ result ->
                    results += "6. Get Group Accounts: SUCCESS\n"
                    currentStep++
                    runNextTest()
                }, { error ->
                    results += "6. Get Group Accounts: $error\n"
                    onError(results)
                })

                7 -> testCreateProposal({ result ->
                    results += "7. Create Proposal: SUCCESS\n"
                    currentStep++
                    runNextTest()
                }, { error ->
                    results += "7. Create Proposal: $error\n"
                    onError(results)
                })

                8 -> testApproveProposal({ result ->
                    results += "8. Approve Proposal: SUCCESS\n"
                    currentStep++
                    runNextTest()
                }, { error ->
                    results += "8. Approve Proposal: $error\n"
                    onError(results)
                })

                9 -> testFinalizeProposal({ result ->
                    results += "9. Finalize Proposal: SUCCESS\n"
                    currentStep++
                    runNextTest()
                }, { error ->
                    results += "9. Finalize Proposal: $error\n"
                    onError(results)
                })

                10 -> testGetGroupPayments({ result ->
                    results += "10. Get Group Payments: SUCCESS\n"
                    onSuccess(results)
                }, { error ->
                    results += "10. Get Group Payments: $error\n"
                    onError(results)
                })
            }
        }

        runNextTest()
    }

    fun cleanup(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        runTest("Cleanup", onSuccess, onError) {
            try {
                // Get all user groups
                testUserRef?.let { userRef ->
                    val groups = getUserGroupSummaries(userRef)

                    // Leave all groups
                    for (group in groups) {
                        group.ref?.let { groupRef ->
                            try {
                                leaveGroup(db, userRef, groupRef)
                            } catch (e: Exception) {
                                Log.w("FirebaseTest", "Error leaving group ${group.name}: ${e.message}")
                            }
                        }
                    }

                    // Delete user document
                    userRef.delete().await()

                    "Cleanup completed: User and related data deleted"
                } ?: throw Exception("User reference not initialized")
            } catch (e: Exception) {
                throw Exception("Cleanup failed: ${e.message}")
            }
        }
    }
}

@Composable
fun FirebaseTestScreen(tester: FirebaseClientTest) {
    var testResults by remember { mutableStateOf(emptyList<TestResult>()) }
    var isRunningTests by remember { mutableStateOf(false) }

    val onSuccess = { operation: String, message: String ->
        testResults = testResults + TestResult(operation, message, true)
        isRunningTests = false
    }

    val onError = { operation: String, message: String ->
        testResults = testResults + TestResult(operation, message, false)
        isRunningTests = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "SplitEasy Firebase Client Tester",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    isRunningTests = true
                    testResults = emptyList()
                    tester.runAllTests(
                        { result -> onSuccess("All Tests", result) },
                        { error -> onError("All Tests", error) }
                    )
                },
                enabled = !isRunningTests
            ) {
                Text("Run All Tests")
            }

            Button(
                onClick = {
                    isRunningTests = true
                    tester.cleanup(
                        { result -> onSuccess("Cleanup", result) },
                        { error -> onError("Cleanup", error) }
                    )
                },
                enabled = !isRunningTests
            ) {
                Text("Cleanup Test Data")
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "Individual Test Operations",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                TestButton(
                    text = "Create User",
                    enabled = !isRunningTests,
                    onClick = {
                        isRunningTests = true
                        tester.testCreateUser(
                            { result -> onSuccess("Create User", result) },
                            { error -> onError("Create User", error) }
                        )
                    }
                )
            }

            item {
                TestButton(
                    text = "Get User",
                    enabled = !isRunningTests,
                    onClick = {
                        isRunningTests = true
                        tester.testGetUser(
                            { result -> onSuccess("Get User", result) },
                            { error -> onError("Get User", error) }
                        )
                    }
                )
            }

            item {
                TestButton(
                    text = "Create Group",
                    enabled = !isRunningTests,
                    onClick = {
                        isRunningTests = true
                        tester.testCreateGroup(
                            { result -> onSuccess("Create Group", result) },
                            { error -> onError("Create Group", error) }
                        )
                    }
                )
            }

            item {
                TestButton(
                    text = "Join Group",
                    enabled = !isRunningTests,
                    onClick = {
                        isRunningTests = true
                        tester.testJoinGroup(
                            { result -> onSuccess("Join Group", result) },
                            { error -> onError("Join Group", error) }
                        )
                    }
                )
            }

            item {
                TestButton(
                    text = "Get User Groups",
                    enabled = !isRunningTests,
                    onClick = {
                        isRunningTests = true
                        tester.testGetUserGroups(
                            { result -> onSuccess("Get User Groups", result) },
                            { error -> onError("Get User Groups", error) }
                        )
                    }
                )
            }

            item {
                TestButton(
                    text = "Get Group Data",
                    enabled = !isRunningTests,
                    onClick = {
                        isRunningTests = true
                        tester.testGetGroupData(
                            { result -> onSuccess("Get Group Data", result) },
                            { error -> onError("Get Group Data", error) }
                        )
                    }
                )
            }

            item {
                TestButton(
                    text = "Get Group Accounts",
                    enabled = !isRunningTests,
                    onClick = {
                        isRunningTests = true
                        tester.testGetGroupAccounts(
                            { result -> onSuccess("Get Group Accounts", result) },
                            { error -> onError("Get Group Accounts", error) }
                        )
                    }
                )
            }

            item {
                TestButton(
                    text = "Create Proposal",
                    enabled = !isRunningTests,
                    onClick = {
                        isRunningTests = true
                        tester.testCreateProposal(
                            { result -> onSuccess("Create Proposal", result) },
                            { error -> onError("Create Proposal", error) }
                        )
                    }
                )
            }

            item {
                TestButton(
                    text = "Get Group Proposals",
                    enabled = !isRunningTests,
                    onClick = {
                        isRunningTests = true
                        tester.testGetGroupProposals(
                            { result -> onSuccess("Get Group Proposals", result) },
                            { error -> onError("Get Group Proposals", error) }
                        )
                    }
                )
            }

            item {
                TestButton(
                    text = "Approve Proposal",
                    enabled = !isRunningTests,
                    onClick = {
                        isRunningTests = true
                        tester.testApproveProposal(
                            { result -> onSuccess("Approve Proposal", result) },
                            { error -> onError("Approve Proposal", error) }
                        )
                    }
                )
            }

            item {
                TestButton(
                    text = "Finalize Proposal",
                    enabled = !isRunningTests,
                    onClick = {
                        isRunningTests = true
                        tester.testFinalizeProposal(
                            { result -> onSuccess("Finalize Proposal", result) },
                            { error -> onError("Finalize Proposal", error) }
                        )
                    }
                )
            }

            item {
                TestButton(
                    text = "Get Group Payments",
                    enabled = !isRunningTests,
                    onClick = {
                        isRunningTests = true
                        tester.testGetGroupPayments(
                            { result -> onSuccess("Get Group Payments", result) },
                            { error -> onError("Get Group Payments", error) }
                        )
                    }
                )
            }

            item {
                TestButton(
                    text = "Leave Group",
                    enabled = !isRunningTests,
                    onClick = {
                        isRunningTests = true
                        tester.testLeaveGroup(
                            { result -> onSuccess("Leave Group", result) },
                            { error -> onError("Leave Group", error) }
                        )
                    }
                )
            }
        }

        if (isRunningTests) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        if (testResults.isNotEmpty()) {
            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "Test Results",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(testResults) { result ->
                    TestResultItem(result = result)
                }
            }
        }
    }
}

@Composable
fun TestButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}

data class TestResult(
    val operation: String,
    val message: String,
    val success: Boolean
)

@Composable
fun TestResultItem(result: TestResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.success)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = result.operation,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = result.message)
        }
    }
}