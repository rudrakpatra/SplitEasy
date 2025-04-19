package com.example.spliteasy.api
import java.util.Date
import com.google.firebase.Timestamp

enum class TransactionType { Expense, Transfer }

data class Transaction(
    val id: String,
    val label: String,
    val type: TransactionType,
    val tags: List<String> = emptyList(),
    val from: List<Pair<Member, Double>>,
    val to: List<Pair<Member, Double>>,
    var timestamp: Timestamp
)

val ExampleTransactions = listOf(

    Transaction(
        id = "1",
        label = "Lunch",
        type = TransactionType.Expense,
        from = listOf(Pair(ExampleMembers[0], 30.0)),
        to = listOf(Pair(ExampleMembers[0], 10.0),Pair(ExampleMembers[1], 10.0),Pair(ExampleMembers[2], 10.0)),
        timestamp = Timestamp(Date(System.currentTimeMillis())) // Today
    ),
    Transaction(
        id = "2",
        label = "Groceries",
        type = TransactionType.Expense,
        from = listOf(Pair(ExampleMembers[1], 50.0)),
        to = listOf(Pair(ExampleMembers[0], 25.0),Pair(ExampleMembers[1], 15.0),Pair(ExampleMembers[2], 10.0)),
        timestamp = Timestamp(Date(System.currentTimeMillis() - 86400000))// Yesterday
    ),
    Transaction(
        id = "3",
        label = "Rent",
        type = TransactionType.Transfer,
        from = listOf(Pair(ExampleMembers[0], 500.0),Pair(ExampleMembers[1], 500.0)),
        to = listOf(Pair(ExampleMembers[2], 1000.0)),
        timestamp = Timestamp(Date(System.currentTimeMillis() - 86400000*5))// 5 days ago
    ),
        Transaction(
        id = "4",
        label = "Cinema",
        type = TransactionType.Expense,
        from = listOf(Pair(ExampleMembers[2], 40.0)),
        to = listOf(Pair(ExampleMembers[0], 10.0),Pair(ExampleMembers[1], 15.0),Pair(ExampleMembers[2], 15.0)),
        timestamp = Timestamp(Date(System.currentTimeMillis() - 86400000*3))// 3 days ago
    ),
        Transaction(
        id = "5",
        label = "Party",
        type = TransactionType.Expense,
        from = listOf(Pair(ExampleMembers[0], 150.0)),
        to = listOf(Pair(ExampleMembers[0], 50.0),Pair(ExampleMembers[1], 50.0),Pair(ExampleMembers[2], 50.0)),
        timestamp = Timestamp(Date(System.currentTimeMillis() - 86400000*7))// 7 days ago
    ),
    Transaction(
        id = "6",
        label = "Gift",
        type = TransactionType.Expense,
        from = listOf(Pair(ExampleMembers[1], 20.0)),
        to = listOf(Pair(ExampleMembers[0], 10.0),Pair(ExampleMembers[1], 10.0)),
        timestamp = Timestamp(Date(System.currentTimeMillis() - 86400000*2))// 2 days ago
    ),
    Transaction(
        id = "7",
        label = "Snacks",
        type = TransactionType.Expense,
        from = listOf(Pair(ExampleMembers[1], 50.0)),
        to = listOf(Pair(ExampleMembers[0], 20.0),Pair(ExampleMembers[1], 30.0)),
        timestamp = Timestamp(Date(System.currentTimeMillis() - 86400000*2))// 2 days ago
    )

)

fun getTransactionsByGroupId(groupId:String): List<Transaction> {
    return ExampleTransactions // Replace this with actual logic to filter by group ID if needed
}
fun getTransactionById(transactionId:String): Transaction {
    return ExampleTransactions.first { it.id == transactionId }
}