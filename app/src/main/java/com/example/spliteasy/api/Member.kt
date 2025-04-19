package com.example.spliteasy.api;

data class Member(val id: String, val user: User, val account: Account)

val ExampleMembers = mutableListOf(
    Member(id = "member1", user = ExampleUsers[0], account = Account()),
    Member(id = "member2", user = ExampleUsers[1], account = Account()),
    Member(id = "member3", user = ExampleUsers[2], account = Account()),
    Member(id = "member4", user = ExampleUsers[3], account = Account()),
)
fun addMember(member: Member, groupId: String) {
    ExampleMembers.add(member)
}
fun getMembersByGroupId(groupId: String): List<Member> {
    return ExampleMembers // Replace this with actual logic to filter by group ID if needed
}

fun getMemberByGroupIdAndMemberId(groupId:String,memberId: String): Member {
    return ExampleMembers.find { it.id == memberId }!!
}
