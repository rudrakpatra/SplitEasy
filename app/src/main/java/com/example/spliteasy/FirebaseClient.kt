package com.example.spliteasy
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.math.abs

@Target(AnnotationTarget.FIELD)
annotation class ImmutableAfterCreation

// Main Data Models
data class User(
    @ImmutableAfterCreation
    val name: String = "",
    @ImmutableAfterCreation
    val createdAt: Timestamp = Timestamp.now(),
    val defaultGroup: DocumentReference? = null
){
    constructor() : this("", Timestamp.now(), null)
}

// Group Summary for displaying in user's groups list
data class GroupSummary(
    val ref: DocumentReference? = null,
    val name: String = "",
    val description: String = "",
    val joinedAt: Timestamp = Timestamp.now(),
    val id: String = generateGroupId()
){
    constructor() : this(null, "", "", Timestamp.now(), "")

    companion object {
        fun from(ref: DocumentReference, group: Group): GroupSummary {
            return GroupSummary(ref, group.name, group.description, Timestamp.now(), group.id)
        }

        // Generate a group ID in format xxx-xxx-xxx
        fun generateGroupId(): String {
            val chars = "abcdefghijklmnopqrstuvwxyz"
            val random = Random()
            val id = StringBuilder()

            for (i in 0 until 3) {
                for (j in 0 until 3) {
                    id.append(chars[random.nextInt(chars.length)])
                }
                if (i < 2) id.append("-")
            }

            return id.toString()
        }
    }
}

data class Group(
    @ImmutableAfterCreation
    val name: String = "",
    @ImmutableAfterCreation
    val description: String = "",
    @ImmutableAfterCreation
    val createdAt: Timestamp = Timestamp.now(),
    @ImmutableAfterCreation
    val id: String = GroupSummary.generateGroupId(),
    val payments: Payments = Payments(),
    val activities: Activities = Activities()
){
    constructor() : this("", "", Timestamp.now(), GroupSummary.generateGroupId(), Payments(), Activities())
}

// Account Summary for displaying in lists and references
data class AccountSummary(
    val ref: DocumentReference? = null,
    val nickname: String = "",
    val balance: Double = 0.0,
    val income: Double = 0.0,
    val expenditure: Double = 0.0
){
    constructor() : this(null, "", 0.0, 0.0, 0.0)

    companion object {
        fun from(ref: DocumentReference, account: Account): AccountSummary {
            return AccountSummary(ref, account.nickname, account.balance, account.income, account.expenditure)
        }
    }
}

data class Account(
    @ImmutableAfterCreation
    val createdAt: Timestamp = Timestamp.now(),
    @ImmutableAfterCreation
    val owner: DocumentReference? = null,
    @ImmutableAfterCreation
    val group: DocumentReference? = null,
    @ImmutableAfterCreation
    val nickname: String = "",
    val balance: Double = 0.0,
    val expenditure: Double = 0.0,
    val income: Double = 0.0
){
    constructor() : this(Timestamp.now(), null, null, "", 0.0, 0.0, 0.0)
}

data class Payments(
    val proposals: List<ProposalSummary> = emptyList(),
    val finalized: List<DocumentReference> = emptyList()
){
    constructor() : this(emptyList(), emptyList())
}

data class ProposalSummary(
    val ref: DocumentReference? = null,
    val title: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val creatorNickname: String = "",
    val amount: Double = 0.0,
    val currency: String = "",
    val type: PaymentType = PaymentType.EXPENSE,
    val paymentSummary: String = "",
    val approvals: Pair<Int, Int> = Pair(0, 0)
){
    constructor() : this(null, "", Timestamp.now(), "", 0.0, "", PaymentType.EXPENSE, "", Pair(0, 0))

    companion object {
        fun from(ref: DocumentReference, proposal: Proposal): ProposalSummary {
            val payment = proposal.draft
            val totalAmount = calculatePaymentTotal(payment)
            val paymentSummary = createPaymentSummary(payment)

            return ProposalSummary(
                ref = ref,
                title = payment.title,
                createdAt = proposal.createdAt,
                creatorNickname = proposal.creatorSummary.nickname,
                amount = totalAmount,
                currency = if (payment.sources.isNotEmpty()) payment.sources[0].currency else "",
                type = payment.type,
                paymentSummary = paymentSummary,
                approvals = Pair(
                    proposal.approvals.count { it.value },
                    proposal.approvals.size
                )
            )
        }

        fun createPaymentSummary(payment: Payment): String {
            return when (payment.type) {
                PaymentType.EXPENSE -> createSourceDestinationSummary(payment)
                PaymentType.INCOME -> createSourceDestinationSummary(payment)
                PaymentType.TRANSFER -> createSourceDestinationSummary(payment)
            }
        }

        private fun createSourceDestinationSummary(payment: Payment): String {
            // Just list out sources and destinations, let UI handle presentation
            val sources = payment.sources.map { it.accountSummary.nickname }
            val destinations = payment.destinations.map { it.accountSummary.nickname }

            return "Sources: ${sources.joinToString(", ")}; Destinations: ${destinations.joinToString(", ")}"
        }
    }
}

data class Proposal(
    val draft: Payment = Payment(),
    val approvals: Map<String, Boolean> = emptyMap(),
    @ImmutableAfterCreation
    val createdAt: Timestamp = Timestamp.now(),
    @ImmutableAfterCreation
    val creatorSummary: AccountSummary = AccountSummary()
){
    constructor() : this(Payment(), emptyMap(), Timestamp.now(), AccountSummary())
}

enum class PaymentType {
    EXPENSE, // bills, buying things for the group, etc.
    INCOME,  // cancellations, refunds, etc.
    TRANSFER // transfers between accounts, reimbursements etc.
}

data class PaymentSummary(
    val ref: DocumentReference? = null,
    val title: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val creatorNickname: String = "",
    val amount: Double = 0.0,
    val currency: String = "",
    val type: PaymentType = PaymentType.EXPENSE,
    val paymentSummary: String = ""
){
    constructor() : this(null, "", Timestamp.now(), "", 0.0, "", PaymentType.EXPENSE, "")

    companion object {
        fun from(ref: DocumentReference, payment: Payment): PaymentSummary {
            val totalAmount = calculatePaymentTotal(payment)
            val paymentSummary = ProposalSummary.createPaymentSummary(payment)

            return PaymentSummary(
                ref = ref,
                title = payment.title,
                createdAt = payment.createdAt,
                creatorNickname = payment.creatorSummary.nickname,
                amount = totalAmount,
                currency = if (payment.sources.isNotEmpty()) payment.sources[0].currency else "",
                type = payment.type,
                paymentSummary = paymentSummary
            )
        }
    }
}

data class Payment(
    @ImmutableAfterCreation
    val title: String = "",
    val sources: List<Source> = emptyList(),
    val destinations: List<Destination> = emptyList(),
    val type: PaymentType = PaymentType.EXPENSE,
    @ImmutableAfterCreation
    val createdAt: Timestamp = Timestamp.now(),
    @ImmutableAfterCreation
    val creatorSummary: AccountSummary = AccountSummary(),
    val details: String = "",
    val roundingDifference: Double = 0.0,
    val isValid: Boolean = false
){
    constructor() : this("", emptyList(), emptyList(), PaymentType.EXPENSE, Timestamp.now(), AccountSummary(), "", 0.0, false)
}

data class Source(
    val accountSummary: AccountSummary = AccountSummary(),
    val amount: Double = 0.0,
    val currency: String = "",
    val rate: Double = 1.0
){
    constructor() : this(AccountSummary(), 0.0, "", 1.0)
}

data class Destination(
    val accountSummary: AccountSummary = AccountSummary(),
    val bill: List<BillEntry> = emptyList()
){
    constructor() : this(AccountSummary(), emptyList())
}

data class BillEntry(
    val item: String = "",
    val amount: Double = 0.0,
    val multiplier: Double = 1.0
){
    constructor() : this("", 0.0, 1.0)
}

data class Activities(
    val recent: List<ActivitySummary> = emptyList(),
    val all: List<DocumentReference> = emptyList()
){
    constructor() : this(emptyList(), emptyList())
}

data class ActivitySummary(
    val ref: DocumentReference? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val title: String = "",
    val detail: String = "",
    val type: ActivityType = ActivityType.GENERAL
){
    constructor() : this(null, Timestamp.now(), "", "", ActivityType.GENERAL)

    companion object {
        fun from(ref: DocumentReference, activity: Activity): ActivitySummary {
            return ActivitySummary(
                ref = ref,
                createdAt = activity.createdAt,
                title = activity.title,
                detail = activity.detail,
                type = activity.type
            )
        }
    }
}

enum class ActivityType {
    GENERAL,
    USER_JOINED,
    USER_LEFT,
    ACCOUNT_CREATED,
    PROPOSAL_CREATED,
    PROPOSAL_APPROVED,
    PROPOSAL_REJECTED,
    PAYMENT_FINALIZED
}

data class Activity(
    val createdAt: Timestamp = Timestamp.now(),
    val title: String = "",
    val detail: String = "",
    val type: ActivityType = ActivityType.GENERAL
){
    constructor() : this(Timestamp.now(), "", "", ActivityType.GENERAL)
}

// Helper functions
private fun calculateDestinationTotal(destination: Destination): Double {
    var total = 0.0
    for (entry in destination.bill) {
        total += entry.amount * entry.multiplier
    }
    return total
}

private fun calculatePaymentTotal(payment: Payment): Double {
    var total = 0.0
    for (source in payment.sources) {
        total += source.amount
    }
    return total
}

// Get Functions
suspend fun getUser(db: FirebaseFirestore, uid: String): User {
    return try {
        val userRef = db.collection("users").document(uid)
        userRef.get().await().toObject(User::class.java)
            ?: throw Exception("User not found")
    } catch (e: Exception) {
        throw Exception("Error getting user: ${e.message}")
    }
}

suspend fun getUserGroupSummaries(userRef: DocumentReference): List<GroupSummary> {
    return try {
        val groupSummaries = userRef.collection("groups").get().await()
        groupSummaries.toObjects(GroupSummary::class.java)
    } catch (e: Exception) {
        throw Exception("Error getting user groups: ${e.message}")
    }
}

suspend inline fun <reified T> getDocRef(docRef: DocumentReference): T? {
    return try {
        docRef.get().await().toObject(T::class.java)
    } catch (e: Exception) {
        throw Exception("Error getting ${T::class.simpleName}: ${e.message}")
    }
}

// Create and Update Functions with Transactions
suspend fun createUser(db: FirebaseFirestore, uid: String, name: String) {
    try {
        db.runTransaction { transaction ->
            val userRef = db.collection("users").document(uid)
            transaction.set(userRef, User(name))
        }.await()
    } catch (e: Exception) {
        throw Exception("Error creating user: ${e.message}")
    }
}

suspend fun createGroup(db: FirebaseFirestore, name: String, description: String): DocumentReference {
    try {
        val groupRef = db.collection("groups").document()
        db.runTransaction { transaction ->
            val groupId = GroupSummary.generateGroupId()
            transaction.set(groupRef, Group(name, description, Timestamp.now(), groupId))
        }.await()
        return groupRef
    } catch (e: Exception) {
        throw Exception("Error creating group: ${e.message}")
    }
}

// Helper function to add activity to recent activities without nested transactions
private fun addActivityToRecentActivities(
    transaction: com.google.firebase.firestore.Transaction,
    groupRef: DocumentReference,
    group: Group, // Pass the already retrieved group data
    activitySummary: ActivitySummary,
    activityRef: DocumentReference
) {
    // Add to recent activities at the beginning
    val recentActivities = group.activities.recent.toMutableList()
    recentActivities.add(0, activitySummary)

    // Keep only the most recent 10 activities
    while (recentActivities.size > 10) {
        recentActivities.removeAt(recentActivities.size - 1)
    }

    // Add to all activities list
    val allActivities = group.activities.all.toMutableList()
    allActivities.add(activityRef)

    // Update the group
    transaction.update(groupRef,
        mapOf(
            "activities.recent" to recentActivities,
            "activities.all" to allActivities
        )
    )

    // Add activity summary to collection
    transaction.set(
        groupRef.collection("activitySummaries").document(activityRef.id),
        activitySummary
    )
}

suspend fun joinGroup(db: FirebaseFirestore, groupRef: DocumentReference, userRef: DocumentReference) {
    try {
        db.runTransaction { transaction ->
            // Perform all reads first
            val userSnapshot = transaction.get(userRef)
            if (!userSnapshot.exists()) {
                throw Exception("User does not exist")
            }
            val user = userSnapshot.toObject(User::class.java) ?: throw Exception("Could not convert to User")

            val groupSnapshot = transaction.get(groupRef)
            if (!groupSnapshot.exists()) {
                throw Exception("Group does not exist")
            }
            val group = groupSnapshot.toObject(Group::class.java) ?: throw Exception("Could not convert to Group")

            // Check if user is already in the group
            val existingGroupSummaryRef = userRef.collection("groups").document(groupRef.id)
            val existingGroupSummarySnapshot = transaction.get(existingGroupSummaryRef)
            if (existingGroupSummarySnapshot.exists()) {
                throw Exception("User is already in this group")
            }

            // Now perform writes

            // Add group summary to user's groups sub-collection
            val groupSummary = GroupSummary.from(groupRef, group)
            transaction.set(existingGroupSummaryRef, groupSummary)

            // Create user account in group
            val accountRef = groupRef.collection("accounts").document(userRef.id)
            val userAccount = Account(
                owner = userRef,
                group = groupRef,
                nickname = user.name
            )

            // Create activity entry
            val activityData = Activity(
                title = "User Joined",
                detail = "${user.name} joined the group",
                type = ActivityType.USER_JOINED
            )
            val activityRef = groupRef.collection("activities").document()
            transaction.set(activityRef, activityData)

            // Add account to group
            transaction.set(accountRef, userAccount)

            // Create account summary in group's accounts sub-collection for quick access
            val accountSummary = AccountSummary.from(accountRef, userAccount)
            transaction.set(groupRef.collection("accountSummaries").document(userRef.id), accountSummary)

            // Add to recent activities without nested transactions
            val activitySummary = ActivitySummary.from(activityRef, activityData)
            addActivityToRecentActivities(transaction, groupRef, group, activitySummary, activityRef)
        }.await()
    } catch (e: Exception) {
        throw Exception("Error joining group: ${e.message}")
    }
}

suspend fun leaveGroup(db: FirebaseFirestore, userRef: DocumentReference, groupRef: DocumentReference) {
    try {
        db.runTransaction { transaction ->
            // Perform all reads first
            val userSnapshot = transaction.get(userRef)
            if (!userSnapshot.exists()) {
                throw Exception("User does not exist")
            }
            val user = userSnapshot.toObject(User::class.java) ?: throw Exception("Could not convert to User")

            val groupSnapshot = transaction.get(groupRef)
            if (!groupSnapshot.exists()) {
                throw Exception("Group does not exist")
            }
            val group = groupSnapshot.toObject(Group::class.java) ?: throw Exception("Could not convert to Group")

            // Check user account balance
            val accountRef = groupRef.collection("accounts").document(userRef.id)
            val accountSnapshot = transaction.get(accountRef)
            if (!accountSnapshot.exists()) {
                throw Exception("User account not found")
            }
            val account = accountSnapshot.toObject(Account::class.java)
                ?: throw Exception("Could not convert to Account")

            if (account.balance != 0.0) {
                throw Exception("User account balance is not zero: ${account.balance}")
            }

            // Now perform writes

            // Create activity entry
            val activityData = Activity(
                title = "User Left",
                detail = "${user.name} left the group",
                type = ActivityType.USER_LEFT
            )
            val activityRef = groupRef.collection("activities").document()
            transaction.set(activityRef, activityData)

            // Remove group from user's groups sub-collection
            transaction.delete(userRef.collection("groups").document(groupRef.id))

            // Remove user account from group
            transaction.delete(accountRef)

            // Remove account summary
            transaction.delete(groupRef.collection("accountSummaries").document(userRef.id))

            // Add to recent activities
            val activitySummary = ActivitySummary.from(activityRef, activityData)
            addActivityToRecentActivities(transaction, groupRef, group, activitySummary, activityRef)
        }.await()
    } catch (e: Exception) {
        throw Exception("Error leaving group: ${e.message}")
    }
}

suspend fun createAccount(db: FirebaseFirestore, groupRef: DocumentReference, ownerRef: DocumentReference, nickname: String): DocumentReference {
    try {
        getDocRef<User>(ownerRef) ?: throw Exception("Could not get user")
        val group = getDocRef<Group>(groupRef) ?: throw Exception("Could not get group")
        val accountRef = groupRef.collection("accounts").document(ownerRef.id)
        db.runTransaction { transaction ->
            // Check if account already exists
            val accountSnapshot = transaction.get(accountRef)
            if (accountSnapshot.exists()) {
                throw Exception("Account already exists")
            }

            // Create activity
            val activityData = Activity(
                title = "Account Created",
                detail = "$nickname created an account",
                type = ActivityType.ACCOUNT_CREATED
            )

            val activityRef = groupRef.collection("activities").document()
            transaction.set(activityRef, activityData)

            // Add to recent activities
            val activitySummary = ActivitySummary.from(activityRef, activityData)
            addActivityToRecentActivities(transaction, groupRef, group, activitySummary, activityRef)

            // Create account
            val account = Account(
                createdAt = Timestamp.now(),
                group = groupRef,
                nickname = nickname,
                owner = ownerRef
            )
            transaction.set(accountRef, account)

            // Create account summary
            val accountSummary = AccountSummary.from(accountRef, account)
            transaction.set(groupRef.collection("accountSummaries").document(ownerRef.id), accountSummary)

        }.await()
        return accountRef
    } catch (e: Exception) {
        throw Exception("Error creating account: ${e.message}")
    }
}

// Helper function to validate payment proposal
fun validatePaymentProposal(payment: Payment): Triple<Boolean, Double, String> {
    val errorMessages = mutableListOf<String>()

    // Check if payment has a title
    if (payment.title.isBlank()) {
        errorMessages.add("Payment must have a title")
    }

    // Check if sources and destinations are not empty
    if (payment.sources.isEmpty()) {
        errorMessages.add("Payment must have at least one source")
    }

    if (payment.destinations.isEmpty()) {
        errorMessages.add("Payment must have at least one destination")
    }

    // Check if all sources have valid amounts
    for ((index, source) in payment.sources.withIndex()) {
        if (source.amount <= 0) {
            errorMessages.add("Source #${index + 1} (${source.accountSummary.nickname}) has an invalid amount: ${source.amount}")
        }
        if (source.accountSummary.ref == null) {
            errorMessages.add("Source #${index + 1} (${source.accountSummary.nickname}) has no account reference")
        }
    }

    // Check if all destinations have valid bill entries
    for ((index, destination) in payment.destinations.withIndex()) {
        if (destination.accountSummary.ref == null) {
            errorMessages.add("Destination #${index + 1} (${destination.accountSummary.nickname}) has no account reference")
        }

        if (destination.bill.isEmpty()) {
            errorMessages.add("Destination #${index + 1} (${destination.accountSummary.nickname}) has no bill entries")
        }

        for ((entryIndex, entry) in destination.bill.withIndex()) {
            if (entry.amount <= 0) {
                errorMessages.add("Bill entry #${entryIndex + 1} for ${destination.accountSummary.nickname} has an invalid amount: ${entry.amount}")
            }
        }
    }

    // Check if total sources amount equals total destinations amount
    val sourcesTotal = payment.sources.sumOf { it.amount }
    val destinationsTotal = payment.destinations.sumOf { destination ->
        destination.bill.sumOf { it.amount * it.multiplier }
    }

    // Calculate rounding difference
    val roundingDifference = sourcesTotal - destinationsTotal

    // Create detail message
    val details = if (errorMessages.isEmpty()) {
        "Payment validated successfully. Total from sources: $sourcesTotal, Total to destinations: $destinationsTotal, Difference: $roundingDifference"
    } else {
        "Validation failed: ${errorMessages.joinToString("; ")}"
    }

    // Allow small rounding differences
    val isValid = errorMessages.isEmpty() && abs(roundingDifference) < 0.01

    return Triple(isValid, roundingDifference, details)
}

suspend fun createProposal(db: FirebaseFirestore, groupRef: DocumentReference, draft: Payment): DocumentReference {
    try {
        // Validate the payment first
        val (isValid, roundingDifference, validationDetails) = validatePaymentProposal(draft)
        if (!isValid) {
            throw Exception("Payment validation error: $validationDetails")
        }

        // Create a valid copy of the draft with validation results
        val validatedDraft = draft.copy(
            isValid = isValid,
            roundingDifference = roundingDifference,
            details = validationDetails
        )
        // Create proposal
        val proposalRef = groupRef.collection("proposals").document()
        // Perform all reads before writes in the transaction
        db.runTransaction { transaction ->
            // First perform all reads
            val groupSnapshot = transaction.get(groupRef)
            if (!groupSnapshot.exists()) {
                throw Exception("Group does not exist")
            }
            val groupData = groupSnapshot.toObject(Group::class.java)
                ?: throw Exception("Could not convert to Group")

            // Create approvals map with all involved parties
            val approvals = mutableMapOf<String, Boolean>()
            validatedDraft.sources.forEach { source ->
                approvals[source.accountSummary.nickname] = false
            }
            validatedDraft.destinations.forEach { destination ->
                if (!approvals.containsKey(destination.accountSummary.nickname)) {
                    approvals[destination.accountSummary.nickname] = false
                }
            }
            // Now perform all writes
            // Create activity
            val activityData = Activity(
                title = "New Payment Proposal",
                detail = "${validatedDraft.creatorSummary.nickname} proposed a payment: ${validatedDraft.title}",
                type = ActivityType.PROPOSAL_CREATED
            )
            val activityRef = groupRef.collection("activities").document()

            // Create Proposal object
            val proposal = Proposal(
                draft = validatedDraft,
                approvals = approvals,
                createdAt = Timestamp.now(),
                creatorSummary = validatedDraft.creatorSummary
            )

            // Set data
            transaction.set(activityRef, activityData)
            transaction.set(proposalRef, proposal)

            // Create proposal summary
            val proposalSummary = ProposalSummary.from(proposalRef, proposal)
            transaction.set(groupRef.collection("proposalSummaries").document(proposalRef.id), proposalSummary)

            // Update group's proposals list
            val updatedProposals = groupData.payments.proposals + proposalSummary
            transaction.update(groupRef, "payments.proposals", updatedProposals)

            // Add to recent activities (refactored to avoid nested transactions)
            val activitySummary = ActivitySummary.from(activityRef, activityData)
            addActivityToRecentActivities(transaction, groupRef, groupData, activitySummary, activityRef)
        }.await()
        return proposalRef
    } catch (e: Exception) {
        throw Exception("Error adding proposal: ${e.message}")
    }
}

suspend fun editProposalApproval(db: FirebaseFirestore, proposalRef: DocumentReference, accountSummary: AccountSummary, approve: Boolean) {
    try {
        val groupRef = proposalRef.parent.parent ?: throw Exception("Could not get group reference")
        val group = getDocRef<Group>(groupRef) ?: throw Exception("Could not get group")

        db.runTransaction { transaction ->
            // Check if proposal exists
            val proposalSnapshot = transaction.get(proposalRef)
            if (!proposalSnapshot.exists()) {
                throw Exception("Proposal does not exist")
            }

            val proposal = proposalSnapshot.toObject(Proposal::class.java)
                ?: throw Exception("Could not convert to Proposal")

            // Update approval
            val approvals = proposal.approvals.toMutableMap()

            // Check if account is part of this proposal
            if (!approvals.containsKey(accountSummary.nickname)) {
                throw Exception("Account ${accountSummary.nickname} is not part of this proposal")
            }

            // Update approval status
            approvals[accountSummary.nickname] = approve

            // Update proposal with new approvals
            transaction.update(proposalRef, "approvals", approvals)

            // Calculate new approval counts
            val approvedCount = approvals.count { it.value }
            val totalCount = approvals.size

            // Get proposal summary from sub-collection
            val proposalSummaryRef = groupRef.collection("proposalSummaries").document(proposalRef.id)
            val proposalSummarySnapshot = transaction.get(proposalSummaryRef)

            // Create activity
            val status = if (approve) "approved" else "rejected"
            val activityType = if (approve) ActivityType.PROPOSAL_APPROVED else ActivityType.PROPOSAL_REJECTED
            val activityData = Activity(
                title = "Proposal $status",
                detail = "${accountSummary.nickname} $status payment proposal: ${proposal.draft.title}",
                type = activityType
            )
            val activityRef = groupRef.collection("activities").document()
            transaction.set(activityRef, activityData)

            // Add to recent activities
            val activitySummary = ActivitySummary.from(activityRef, activityData)
            addActivityToRecentActivities(transaction, groupRef, group, activitySummary, activityRef)

            if (proposalSummarySnapshot.exists()) {
                // Update the approvals in the summary
                transaction.update(proposalSummaryRef, "approvals", Pair(approvedCount, totalCount))

                // Update the group's proposal list
                val groupSnapshot = transaction.get(groupRef)
                val groupData = groupSnapshot.toObject(Group::class.java)
                    ?: throw Exception("Could not convert to Group")

                val updatedProposals = groupData.payments.proposals.map {
                    if (it.ref?.id == proposalRef.id) {
                        it.copy(approvals = Pair(approvedCount, totalCount))
                    } else {
                        it
                    }
                }

                transaction.update(groupRef, "payments.proposals", updatedProposals)
            }
        }.await()
    } catch (e: Exception) {
        throw Exception("Error editing proposal approval: ${e.message}")
    }
}

// Process different payment types
private fun processExpenseTransaction(
    transaction: com.google.firebase.firestore.Transaction,
    payment: Payment,
    accountData: Map<String, Pair<DocumentReference, Account>>,
    accountSummaryRefs: Map<String, DocumentReference>
) {
    // Process payers (sources)
    for (source in payment.sources) {
        val accountRef = source.accountSummary.ref ?: continue
        val accountPair = accountData[accountRef.id] ?: continue
        val account = accountPair.second

        // Update balance and expenditure
        val newBalance = account.balance - source.amount
        val newExpenditure = account.expenditure + source.amount

        // Update account
        transaction.update(accountRef,
            mapOf(
                "balance" to newBalance,
                "expenditure" to newExpenditure
            )
        )

        // Update account summary
        val accountSummaryRef = accountSummaryRefs[accountRef.id]
        if (accountSummaryRef != null) {
            transaction.update(accountSummaryRef,
                mapOf(
                    "balance" to newBalance,
                    "expenditure" to newExpenditure
                )
            )
        }
    }

    // Process receivers (destinations)
    for (destination in payment.destinations) {
        val accountRef = destination.accountSummary.ref ?: continue
        val accountPair = accountData[accountRef.id] ?: continue
        val account = accountPair.second

        // Calculate total amount for this destination
        val destinationTotal = calculateDestinationTotal(destination)

        // Update balance
        val newBalance = account.balance + destinationTotal

        // Update account
        transaction.update(accountRef, "balance", newBalance)

        // Update account summary
        val accountSummaryRef = accountSummaryRefs[accountRef.id]
        if (accountSummaryRef != null) {
            transaction.update(accountSummaryRef, "balance", newBalance)
        }
    }
}

private fun processIncomeTransaction(
    transaction: com.google.firebase.firestore.Transaction,
    payment: Payment,
    accountData: Map<String, Pair<DocumentReference, Account>>,
    accountSummaryRefs: Map<String, DocumentReference>
) {
    // Process sources (incoming money)
    for (source in payment.sources) {
        val accountRef = source.accountSummary.ref ?: continue
        val accountPair = accountData[accountRef.id] ?: continue
        val account = accountPair.second

        // Update balance and income
        val newBalance = account.balance + source.amount
        val newIncome = account.income + source.amount

        // Update account
        transaction.update(accountRef,
            mapOf(
                "balance" to newBalance,
                "income" to newIncome
            )
        )

        // Update account summary
        val accountSummaryRef = accountSummaryRefs[accountRef.id]
        if (accountSummaryRef != null) {
            transaction.update(accountSummaryRef,
                mapOf(
                    "balance" to newBalance,
                    "income" to newIncome
                )
            )
        }
    }

    // Process destinations (receiving accounts with detailed breakdown)
    for (destination in payment.destinations) {
        // No balance changes needed for destinations in INCOME transactions
        // They are just for recording what the income was for
    }
}

private fun processTransferTransaction(
    transaction: com.google.firebase.firestore.Transaction,
    payment: Payment,
    accountData: Map<String, Pair<DocumentReference, Account>>,
    accountSummaryRefs: Map<String, DocumentReference>
) {
    // Process sources (sending money)
    for (source in payment.sources) {
        val accountRef = source.accountSummary.ref ?: continue
        val accountPair = accountData[accountRef.id] ?: continue
        val account = accountPair.second

        // Update balance
        val newBalance = account.balance - source.amount

        // Update account
        transaction.update(accountRef, "balance", newBalance)

        // Update account summary
        val accountSummaryRef = accountSummaryRefs[accountRef.id]
        if (accountSummaryRef != null) {
            transaction.update(accountSummaryRef, "balance", newBalance)
        }
    }

    // Process destinations (receiving money)
    for (destination in payment.destinations) {
        val accountRef = destination.accountSummary.ref ?: continue
        val accountPair = accountData[accountRef.id] ?: continue
        val account = accountPair.second

        // Calculate total amount for this destination
        val destinationTotal = calculateDestinationTotal(destination)

        // Update balance
        val newBalance = account.balance + destinationTotal

        // Update account
        transaction.update(accountRef, "balance", newBalance)

        // Update account summary
        val accountSummaryRef = accountSummaryRefs[accountRef.id]
        if (accountSummaryRef != null) {
            transaction.update(accountSummaryRef, "balance", newBalance)
        }
    }
}

suspend fun finalizeProposal(db: FirebaseFirestore, proposalRef: DocumentReference) {
    try {
        val groupRef = proposalRef.parent.parent ?: throw Exception("Could not get group reference")

        db.runTransaction { transaction ->
            // Check if proposal exists
            val proposalSnapshot = transaction.get(proposalRef)
            if (!proposalSnapshot.exists()) {
                throw Exception("Proposal does not exist")
            }

            val proposal = proposalSnapshot.toObject(Proposal::class.java)
                ?: throw Exception("Could not convert to Proposal")

            val payment = proposal.draft

            // Verify that payment is valid
            if (!payment.isValid) {
                throw Exception("Cannot finalize invalid payment")
            }

            // Verify that all parties have approved
            val allApproved = proposal.approvals.all { it.value }
            if (!allApproved) {
                throw Exception("Cannot finalize: not all parties have approved")
            }

            // Get group data
            val groupSnapshot = transaction.get(groupRef)
            val group = groupSnapshot.toObject(Group::class.java)
                ?: throw Exception("Could not convert to Group")

            // Collect all account references from payment
            val accountRefs = mutableSetOf<DocumentReference>()
            payment.sources.forEach { source ->
                source.accountSummary.ref?.let { accountRefs.add(it) }
            }
            payment.destinations.forEach { destination ->
                destination.accountSummary.ref?.let { accountRefs.add(it) }
            }

            // Get account data for all involved accounts
            val accountData = mutableMapOf<String, Pair<DocumentReference, Account>>()
            val accountSummaryRefs = mutableMapOf<String, DocumentReference>()

            for (accountRef in accountRefs) {
                val accountSnapshot = transaction.get(accountRef)
                if (!accountSnapshot.exists()) {
                    throw Exception("Account ${accountRef.id} does not exist")
                }

                val account = accountSnapshot.toObject(Account::class.java)
                    ?: throw Exception("Could not convert to Account")

                accountData[accountRef.id] = Pair(accountRef, account)

                // Get account summary reference
                val accountSummaryRef = groupRef.collection("accountSummaries").document(accountRef.id)
                accountSummaryRefs[accountRef.id] = accountSummaryRef
            }

            // Create a reference for the finalized payment
            val paymentRef = groupRef.collection("payments").document()

            // Create payment document
            transaction.set(paymentRef, payment)

            // Create payment summary
            val paymentSummary = PaymentSummary.from(paymentRef, payment)
            transaction.set(groupRef.collection("paymentSummaries").document(paymentRef.id), paymentSummary)

            // Process payment based on type
            when (payment.type) {
                PaymentType.EXPENSE -> processExpenseTransaction(transaction, payment, accountData, accountSummaryRefs)
                PaymentType.INCOME -> processIncomeTransaction(transaction, payment, accountData, accountSummaryRefs)
                PaymentType.TRANSFER -> processTransferTransaction(transaction, payment, accountData, accountSummaryRefs)
            }

            // Create activity
            val activityData = Activity(
                title = "Payment Finalized",
                detail = "${proposal.creatorSummary.nickname}'s payment has been finalized: ${payment.title}",
                type = ActivityType.PAYMENT_FINALIZED
            )
            val activityRef = groupRef.collection("activities").document()
            transaction.set(activityRef, activityData)

            // Add activity to recent activities
            val activitySummary = ActivitySummary.from(activityRef, activityData)
            addActivityToRecentActivities(transaction, groupRef, group, activitySummary, activityRef)

            // Update group's finalized payments list
            val updatedFinalized = group.payments.finalized + paymentRef
            transaction.update(groupRef, "payments.finalized", updatedFinalized)

            // Remove proposal from group's proposals list
            val updatedProposals = group.payments.proposals.filter { it.ref?.id != proposalRef.id }
            transaction.update(groupRef, "payments.proposals", updatedProposals)

            // Remove proposal summary
            transaction.delete(groupRef.collection("proposalSummaries").document(proposalRef.id))

            // Mark proposal as finalized (or optionally delete it)
            transaction.update(proposalRef, "finalized", true)
        }.await()
    } catch (e: Exception) {
        throw Exception("Error finalizing proposal: ${e.message}")
    }
}

suspend fun getGroupData(groupRef: DocumentReference): Group {
    return try {
        val groupSnapshot = groupRef.get().await()
        groupSnapshot.toObject(Group::class.java) ?: throw Exception("Could not convert to Group")
    } catch (e: Exception) {
        throw Exception("Error getting group data: ${e.message}")
    }
}

suspend fun getGroupAccounts(groupRef: DocumentReference): List<AccountSummary> {
    return try {
        val accountSummaries = groupRef.collection("accountSummaries").get().await()
        accountSummaries.toObjects(AccountSummary::class.java)
    } catch (e: Exception) {
        throw Exception("Error getting group accounts: ${e.message}")
    }
}

suspend fun getGroupProposals(groupRef: DocumentReference): List<ProposalSummary> {
    return try {
        val proposalSummaries = groupRef.collection("proposalSummaries").get().await()
        proposalSummaries.toObjects(ProposalSummary::class.java)
    } catch (e: Exception) {
        throw Exception("Error getting group proposals: ${e.message}")
    }
}

suspend fun getGroupPayments(groupRef: DocumentReference): List<PaymentSummary> {
    return try {
        val paymentSummaries = groupRef.collection("paymentSummaries").get().await()
        paymentSummaries.toObjects(PaymentSummary::class.java)
    } catch (e: Exception) {
        throw Exception("Error getting group payments: ${e.message}")
    }
}

// Balance calculation functions
fun calculateUserOwes(account: Account, allAccounts: List<Account>): Map<String, Double> {
    val result = mutableMapOf<String, Double>()

    if (account.balance < 0) {
        // This user owes money
        val totalDebt = -account.balance
        val positiveAccounts = allAccounts.filter { it.balance > 0 }
        val totalPositive = positiveAccounts.sumOf { it.balance }

        if (totalPositive > 0) {
            for (creditor in positiveAccounts) {
                val proportion = creditor.balance / totalPositive
                val amountOwed = totalDebt * proportion
                if (amountOwed > 0.01) { // Only track meaningful amounts
                    result[creditor.nickname] = amountOwed
                }
            }
        }
    }

    return result
}

fun calculateUserIsOwed(account: Account, allAccounts: List<Account>): Map<String, Double> {
    val result = mutableMapOf<String, Double>()

    if (account.balance > 0) {
        // This user is owed money
        val totalCredit = account.balance
        val negativeAccounts = allAccounts.filter { it.balance < 0 }
        val totalNegative = negativeAccounts.sumOf { -it.balance }

        if (totalNegative > 0) {
            for (debtor in negativeAccounts) {
                val proportion = -debtor.balance / totalNegative
                val amountOwed = totalCredit * proportion
                if (amountOwed > 0.01) { // Only track meaningful amounts
                    result[debtor.nickname] = amountOwed
                }
            }
        }
    }

    return result
}

// Simplified settlement calculation
suspend fun calculateSettlements(groupRef: DocumentReference): List<Settlement> {
    try {
        val accountSnapshots = groupRef.collection("accounts").get().await()
        val accounts = accountSnapshots.toObjects(Account::class.java)

        val settlements = mutableListOf<Settlement>()
        val debtors = accounts.filter { it.balance < 0 }.sortedBy { it.balance }
        val creditors = accounts.filter { it.balance > 0 }.sortedByDescending { it.balance }

        for (debtor in debtors) {
            var remaining = -debtor.balance

            for (creditor in creditors) {
                if (remaining <= 0 || creditor.balance <= 0) continue

                val amount = minOf(remaining, creditor.balance)
                if (amount > 0.01) {  // Only create settlements for meaningful amounts
                    settlements.add(
                        Settlement(
                            from = debtor.nickname,
                            to = creditor.nickname,
                            amount = amount
                        )
                    )
                }

                remaining -= amount
            }
        }

        return settlements
    } catch (e: Exception) {
        throw Exception("Error calculating settlements: ${e.message}")
    }
}

data class Settlement(
    val from: String,
    val to: String,
    val amount: Double
)