package com.example.spliteasy.api

data class User(val name: String,val email: String, val id: String)

val ExampleUsers= listOf(
    User(id = "user1", name = "Alice", email = "alice@example.com"),
    User(id = "user2", name = "Bob", email = "bob@example.com"),
    User(id = "user3", name = "Charlie", email = "charlie@example.com"),
    User(id = "user4", name = "David", email = "david@example.com"),
    User(id = "user5", name = "Eve", email = "eve@example.com"),
    User(id = "user6", name = "Frank", email = "frank@example.com"),
)
