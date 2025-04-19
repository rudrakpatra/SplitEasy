package com.example.spliteasy.api

data class Account(val balance: Double = 0.0,val expenditure: Double = 0.0 , val creditList: List<Transaction> = emptyList(), val debitList: List<Transaction> = emptyList())

val ExampleCredits = mutableListOf(
    ExampleTransactions[0],
    ExampleTransactions[2],
    ExampleTransactions[4],
)
val ExampleDebits = listOf(
    ExampleTransactions[1],
    ExampleTransactions[3],
    ExampleTransactions[5],
    ExampleTransactions[6],
)
val ExampleAccounts= mutableListOf(
    Account(0.0,0.0, ExampleCredits, ExampleDebits),
    Account(0.0,0.0, emptyList(), emptyList()),
    Account(0.0,0.0, emptyList(), emptyList()),
    Account(0.0,0.0, emptyList(), emptyList()),
    Account(0.0,0.0, emptyList(), emptyList()),
)