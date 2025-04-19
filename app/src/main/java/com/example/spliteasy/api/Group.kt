package com.example.spliteasy.api

data class Group(val name: String, val description: String,val id:String)
val ExampleGroups:List<Group> = listOf(
    Group("Family Trip", "Group for our summer vacation", "family-trip-1"),
    Group("Work Project", "Team working on Project Alpha", "work-project-2"),
    Group("Friends Hangout", "Weekend hangout group", "friends-hangout-3"),
    Group("Book Club", "Reading and discussing books", "book-club-4"),
    Group("Hiking Buddies", "Exploring nature trails together", "hiking-buddies-5"),
    Group("Gaming Squad", "Online gaming adventures", "gaming-squad-6"),
    Group("Study Group", "Preparing for exams together", "study-group-7"),
    Group("Soccer Team", "Weekly soccer matches", "soccer-team-8"),
    Group("Chess Club", "Strategic board game enthusiasts", "chess-club-9"),
    Group("Movie Buffs", "Discussing films and series", "movie-buffs-10"),
    Group("Travel Planners", "Planning trips together", "travel-planners-11")
)
fun addGroup(group:Group):Group{
    return Group("New Group", "Description", "new-group-12")
}
fun getGroupsByUserId(userId: String): List<Group> {
    return ExampleGroups
}
fun getGroupByGroupId(groupId:String):Group{
    return ExampleGroups.find { it.id == groupId }!!
}