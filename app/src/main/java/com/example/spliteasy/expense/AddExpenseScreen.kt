package com.example.spliteasy.expense
//
//import android.graphics.Bitmap
//import android.net.Uri
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.horizontalScroll
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.pager.HorizontalPager
//import androidx.compose.foundation.pager.rememberPagerState
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.BasicTextField
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.automirrored.filled.ArrowForward
//import androidx.compose.material.icons.automirrored.filled.Send
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.focus.onFocusChanged
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalFocusManager
//import androidx.compose.ui.text.AnnotatedString
//import androidx.compose.ui.text.SpanStyle
//import androidx.compose.ui.text.TextRange
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.buildAnnotatedString
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.TextFieldValue
//import androidx.compose.ui.text.withStyle
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.compose.ui.window.Dialog
//import androidx.compose.ui.window.DialogProperties
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.window.layout.DisplayFeature
//import com.example.spliteasy.api.ExampleMembers
//import com.example.spliteasy.api.Member
//import com.example.spliteasy.api.TransactionType
//import com.example.spliteasy.transaction.ExpenseViewModel
//import io.noties.markwon.Markwon
//import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
//import io.noties.markwon.ext.tables.TablePlugin
//import io.noties.markwon.ext.tasklist.TaskListPlugin
//import kotlinx.coroutines.launch
//import org.json.JSONObject
//
//enum class TransactionFlowStep {
//    EXPENSE_BREAKDOWN,
//    EXPENSE_DETAILS,
//    TRANSFER_DETAILS,
//    TRANSACTION_PROPOSAL
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//@Preview
//fun AddTransactionScreen(
//    members: List<Member> = ExampleMembers,
//    onBackButtonClicked: () -> Unit = {},
//    onTransactionComplete: () -> Unit = {},
//    transactionViewModel: ExpenseViewModel = viewModel(),
//    displayFeatures: List<DisplayFeature> = emptyList()
//) {
//    val transactionType by transactionViewModel.transactionType.collectAsState()
//    val isProcessing by transactionViewModel.isProcessing.collectAsState()
//    val aiResponse by transactionViewModel.aiResponse.collectAsState()
//    val summaryData by transactionViewModel.summaryData.collectAsState()
//    val billImage = transactionViewModel.billImage.collectAsState().value
//
//    val pagerState = rememberPagerState { 4 }
//    val coroutineScope = rememberCoroutineScope()
//    val currentStep = when (pagerState.currentPage) {
//        0 -> TransactionFlowStep.EXPENSE_BREAKDOWN
//        1 -> if (transactionType == TransactionType.Expense)
//            TransactionFlowStep.EXPENSE_DETAILS
//        else
//            TransactionFlowStep.TRANSFER_DETAILS
//        2 -> TransactionFlowStep.TRANSACTION_PROPOSAL
//        else -> TransactionFlowStep.EXPENSE_BREAKDOWN
//    }
//
//    var showEditPromptDialog by remember { mutableStateOf(false) }
//    var currentEditPrompt by remember { mutableStateOf("") }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        text = when (currentStep) {
//                            TransactionFlowStep.EXPENSE_BREAKDOWN -> if (transactionType == TransactionType.Expense)
//                                "Expense Breakdown" else "Transfer"
//                            TransactionFlowStep.EXPENSE_DETAILS -> "Expense Details"
//                            TransactionFlowStep.TRANSFER_DETAILS -> "Transfer Details"
//                            TransactionFlowStep.TRANSACTION_PROPOSAL -> "Transaction Proposal"
//                        }
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = onBackButtonClicked) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
//                    }
//                },
//                actions = {
//                    IconButton(onClick = { showEditPromptDialog = true }) {
//                        Icon(Icons.Default.Edit, contentDescription = "Edit")
//                    }
//                }
//            )
//        },
//        bottomBar = {
//            BottomAppBar {
//                IconButton(
//                    onClick = {
//                        when (currentStep) {
//                            TransactionFlowStep.EXPENSE_BREAKDOWN -> transactionViewModel.resetBillImage()
//                            TransactionFlowStep.EXPENSE_DETAILS,
//                            TransactionFlowStep.TRANSFER_DETAILS -> transactionViewModel.resetAIResponse()
//                            TransactionFlowStep.TRANSACTION_PROPOSAL -> transactionViewModel.resetSummary()
//                        }
//                    }
//                ) {
//                    Icon(Icons.Default.Close, contentDescription = "Clear")
//                }
//
//                Spacer(Modifier.weight(1f))
//
//                IconButton(
//                    onClick = {
//                        currentEditPrompt = when (currentStep) {
//                            TransactionFlowStep.EXPENSE_BREAKDOWN -> "Please itemize this bill"
//                            TransactionFlowStep.EXPENSE_DETAILS -> "Suggest a fair split strategy"
//                            TransactionFlowStep.TRANSFER_DETAILS -> "Finalize the transfer details"
//                            TransactionFlowStep.TRANSACTION_PROPOSAL -> "Summarize this transaction"
//                        }
//                        showEditPromptDialog = true
//                    }
//                ) {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Icon(Icons.Default.Edit, contentDescription = "Prompt")
//                        Text("Prompt", modifier = Modifier.padding(start = 4.dp))
//                    }
//                }
//
//                Spacer(Modifier.weight(1f))
//
//                Button(
//                    onClick = {
//                        if (currentStep == TransactionFlowStep.TRANSACTION_PROPOSAL) {
//                            onTransactionComplete()
//                        } else {
//                            coroutineScope.launch {
//                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
//                            }
//                        }
//                    },
//                    enabled = when (currentStep) {
//                        TransactionFlowStep.EXPENSE_BREAKDOWN -> billImage != null
//                        TransactionFlowStep.EXPENSE_DETAILS,
//                        TransactionFlowStep.TRANSFER_DETAILS -> aiResponse.isNotBlank()
//                        TransactionFlowStep.TRANSACTION_PROPOSAL -> true
//                    }
//                ) {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Text(
//                            if (currentStep == TransactionFlowStep.TRANSACTION_PROPOSAL) "Complete" else "Next"
//                        )
//                        Icon(
//                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
//                            contentDescription = "Next",
//                            modifier = Modifier.padding(start = 4.dp)
//                        )
//                    }
//                }
//            }
//        }
//    ) { innerPadding ->
//
//        Column(modifier = Modifier
//            .fillMaxSize()
//            .padding(innerPadding)) {
//
//            // Step navigation indicator
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(8.dp),
//                horizontalArrangement = Arrangement.SpaceEvenly
//            ) {
//                listOf(
//                    "Breakdown" to 0,
//                    "Details" to 1,
//                    "Proposal" to 2
//                ).forEach { (label, index) ->
//                    val isSelected = pagerState.currentPage == index
//                    TextButton(
//                        onClick = {
//                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
//                        },
//                        colors = ButtonDefaults.textButtonColors(
//                            contentColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
//                        )
//                    ) {
//                        Text(label)
//                    }
//                }
//            }
//
//            Box(modifier = Modifier.fillMaxSize()) {
//                HorizontalPager(
//                    state = pagerState,
//                    modifier = Modifier.fillMaxSize()
//                ) { page ->
//                    when (page) {
//                        0 -> ExpenseBreakdownPage(
//                            transactionType = transactionType,
//                            onTypeSelected = { transactionViewModel.setTransactionType(it) },
//                            billImage = billImage,
//                            onImageSelected = { transactionViewModel.setBillImage(it) },
//                            onPromptSent = { prompt -> transactionViewModel.analyzeImage(prompt) },
//                            aiResponse = aiResponse
//                        )
//                        1 -> if (transactionType == TransactionType.Expense) {
//                            ExpenseDetailsPage(
//                                members = members,
//                                aiResponse = aiResponse,
//                                onPromptSent = { prompt, selectedMembers ->
//                                    transactionViewModel.generateSplitStrategy(prompt, selectedMembers)
//                                }
//                            )
//                        } else {
//                            TransferDetailsPage(
//                                members = members,
//                                aiResponse = aiResponse,
//                                onPromptSent = { prompt, selectedMembers ->
//                                    transactionViewModel.generateTransferDetails(prompt, selectedMembers)
//                                }
//                            )
//                        }
//                        2 -> TransactionProposalPage(
//                            transactionType = transactionType,
//                            summaryData = summaryData
//                        )
//                    }
//                }
//
//                if (isProcessing) {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .background(Color.Black.copy(alpha = 0.5f)),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        CircularProgressIndicator()
//                    }
//                }
//            }
//        }
//    }
//
//    if (showEditPromptDialog) {
//        AIPromptDialog(
//            initialPrompt = currentEditPrompt,
//            onDismiss = { showEditPromptDialog = false },
//            onPromptSent = { prompt ->
//                when (currentStep) {
//                    TransactionFlowStep.EXPENSE_BREAKDOWN -> transactionViewModel.analyzeImage(prompt)
//                    TransactionFlowStep.EXPENSE_DETAILS -> transactionViewModel.generateSplitStrategy(prompt, emptyList())
//                    TransactionFlowStep.TRANSFER_DETAILS -> transactionViewModel.generateTransferDetails(prompt, emptyList())
//                    TransactionFlowStep.TRANSACTION_PROPOSAL -> transactionViewModel.generateSummary(prompt)
//                }
//                showEditPromptDialog = false
//            }
//        )
//    }
//}
//
//
//@Composable
//fun ExpenseBreakdownPage(
//    transactionType: TransactionType,
//    onTypeSelected: (TransactionType) -> Unit,
//    billImage: Bitmap?,
//    onImageSelected: (Bitmap) -> Unit,
//    onPromptSent: (String) -> Unit,
//    aiResponse: String
//) {
//    var showImageDialog by remember { mutableStateOf(false) }
//    var showPromptDialog by remember { mutableStateOf(false) }
//    val context = LocalContext.current
//    var isImageLoading by remember { mutableStateOf(false) }
//
//    val imagePickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        isImageLoading = true
//        uri?.let { selectedUri ->
//            val inputStream = context.contentResolver.openInputStream(selectedUri)
//            val bitmap = inputStream?.use { android.graphics.BitmapFactory.decodeStream(it) }
//            bitmap?.let { bmp ->
//                onImageSelected(bmp)
//                // Auto-send default prompt after image is selected
//                onPromptSent("Please itemize this bill and extract all items with prices.")
//            }
//        }
//        isImageLoading = false
//    }
//
//    val cameraLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.TakePicturePreview()
//    ) { bitmap: Bitmap? ->
//        isImageLoading = true
//        bitmap?.let { bmp ->
//            onImageSelected(bmp)
//            // Auto-send default prompt after image is taken
//            onPromptSent("Please itemize this bill and extract all items with prices.")
//        }
//        isImageLoading = false
//    }
//
//    val markwon = remember {
//        Markwon.builder(context)
//            .usePlugin(StrikethroughPlugin.create())
//            .usePlugin(TaskListPlugin.create(context))
//            .usePlugin(TablePlugin.create(context))
//            .build()
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = "Transaction Type",
//            style = MaterialTheme.typography.titleMedium,
//            modifier = Modifier.padding(bottom = 16.dp)
//        )
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceEvenly
//        ) {
//            TransactionTypeOption(
//                type = TransactionType.Expense,
//                isSelected = transactionType == TransactionType.Expense,
//                onSelected = { onTypeSelected(TransactionType.Expense) },
//                description = "Added to expenditures"
//            )
//
//            TransactionTypeOption(
//                type = TransactionType.Transfer,
//                isSelected = transactionType == TransactionType.Transfer,
//                onSelected = { onTypeSelected(TransactionType.Transfer) },
//                description = "Not added to expenditures"
//            )
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        if (transactionType == TransactionType.Expense) {
//            Text(
//                text = "Expense Breakdown",
//                style = MaterialTheme.typography.titleMedium,
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//        }
//
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(200.dp)
//                .border(
//                    width = 1.dp,
//                    color = MaterialTheme.colorScheme.outline,
//                    shape = RoundedCornerShape(12.dp)
//                )
//                .clip(RoundedCornerShape(12.dp))
//                .clickable { showImageDialog = true },
//            contentAlignment = Alignment.Center
//        ) {
//            if (isImageLoading) {
//                CircularProgressIndicator()
//            } else if (billImage != null) {
//                Image(
//                    bitmap = billImage.asImageBitmap(),
//                    contentDescription = "Bill Image",
//                    modifier = Modifier.fillMaxSize(),
//                    contentScale = ContentScale.Fit
//                )
//            } else {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Person,
//                        contentDescription = "Upload Bill",
//                        modifier = Modifier.size(48.dp),
//                        tint = MaterialTheme.colorScheme.primary
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Text(
//                        text = "Upload image of a bill",
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Button(
//                onClick = { showPromptDialog = true },
//                enabled = billImage != null,
//                modifier = Modifier.weight(1f)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Edit,
//                    contentDescription = "Prompt",
//                    modifier = Modifier.padding(end = 8.dp)
//                )
//                Text("Prompt")
//            }
//
//            Spacer(modifier = Modifier.width(8.dp))
//
//            Button(
//                onClick = { onPromptSent("Please itemize this bill and extract all items with prices.") },
//                enabled = billImage != null,
//                modifier = Modifier.weight(1f)
//            ) {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Filled.Send,
//                    contentDescription = "Send",
//                    modifier = Modifier.padding(end = 8.dp)
//                )
//                Text("Send")
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        if (aiResponse.isNotBlank()) {
//            Text(
//                text = "Itemized Bill",
//                style = MaterialTheme.typography.titleMedium,
//                modifier = Modifier.padding(vertical = 8.dp)
//            )
//
//            Card(
//                modifier = Modifier
//                    .weight(1f)
//                    .fillMaxWidth(),
//                shape = RoundedCornerShape(12.dp)
//            ) {
//                AndroidView(
//                    factory = { ctx ->
//                        android.widget.TextView(ctx).apply {
//                            setTextIsSelectable(true)
//                        }
//                    },
//                    update = { textView ->
//                        markwon.setMarkdown(textView, aiResponse)
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp)
//                )
//            }
//        }
//    }
//
//    if (showImageDialog) {
//        AlertDialog(
//            onDismissRequest = { showImageDialog = false },
//            title = { Text("Choose Image Source") },
//            text = {
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    TextButton(
//                        onClick = {
//                            imagePickerLauncher.launch("image/*")
//                            showImageDialog = false
//                        },
//                        modifier = Modifier.fillMaxWidth()
//                    ) { Text("Gallery") }
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    TextButton(
//                        onClick = {
//                            cameraLauncher.launch(null)
//                            showImageDialog = false
//                        },
//                        modifier = Modifier.fillMaxWidth()
//                    ) { Text("Camera") }
//                }
//            },
//            confirmButton = {
//                TextButton(onClick = { showImageDialog = false }) {
//                    Text("Cancel")
//                }
//            }
//        )
//    }
//
//    if (showPromptDialog) {
//        AIPromptDialog(
//            initialPrompt = "Please itemize this bill and extract all items with prices.",
//            onDismiss = { showPromptDialog = false },
//            onPromptSent = { prompt ->
//                onPromptSent(prompt)
//                showPromptDialog = false
//            }
//        )
//    }
//}
//
//@Composable
//fun TransactionTypeOption(
//    type: TransactionType,
//    isSelected: Boolean,
//    onSelected: () -> Unit,
//    description: String
//) {
//    Card(
//        modifier = Modifier
//            .width(150.dp)
//            .clickable { onSelected() },
//        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = if (isSelected)
//                MaterialTheme.colorScheme.primaryContainer
//            else
//                MaterialTheme.colorScheme.surfaceVariant
//        )
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Icon(
//                imageVector = when(type) {
//                    TransactionType.Expense -> Icons.Default.Add
//                    TransactionType.Transfer -> Icons.AutoMirrored.Filled.ArrowForward
//                },
//                contentDescription = type.name,
//                modifier = Modifier.size(32.dp),
//                tint = if (isSelected)
//                    MaterialTheme.colorScheme.onPrimaryContainer
//                else
//                    MaterialTheme.colorScheme.onSurfaceVariant
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//                text = type.name.lowercase().replaceFirstChar { it.uppercase() },
//                style = MaterialTheme.typography.bodyLarge,
//                color = if (isSelected)
//                    MaterialTheme.colorScheme.onPrimaryContainer
//                else
//                    MaterialTheme.colorScheme.onSurfaceVariant
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            Text(
//                text = description,
//                style = MaterialTheme.typography.bodySmall,
//                color = if (isSelected)
//                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
//                else
//                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
//            )
//        }
//    }
//}
//
//@Composable
//fun ExpenseDetailsPage(
//    members: List<Member>,
//    aiResponse: String,
//    onPromptSent: (String, List<Member>) -> Unit
//) {
//    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
//    val selectedMembers = remember { mutableStateListOf<Member>() }
//    var cursorPosition by remember { mutableStateOf(0) }
//    val context = LocalContext.current
//    var showSuggestions by remember { mutableStateOf(false) }
//    var suggestionItems by remember { mutableStateOf(emptyList<String>()) }
//    val focusManager = LocalFocusManager.current
//
//    // Extract item names from AI response
//    val itemNames = remember(aiResponse) {
//        extractItemNamesFromResponse(aiResponse)
//    }
//
//    // Initialize Markwon
//    val markwon = remember {
//        Markwon.builder(context)
//            .usePlugin(StrikethroughPlugin.create())
//            .usePlugin(TaskListPlugin.create(context))
//            .usePlugin(TablePlugin.create(context))
//            .build()
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        Text(
//            text = "Splitting Strategy",
//            style = MaterialTheme.typography.titleMedium,
//            modifier = Modifier.padding(bottom = 8.dp)
//        )
//
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .border(
//                    width = 1.dp,
//                    color = MaterialTheme.colorScheme.outline,
//                    shape = RoundedCornerShape(12.dp)
//                ),
//            shape = RoundedCornerShape(12.dp)
//        ) {
//            Column(modifier = Modifier.padding(8.dp)) {
//                // Input field with member names highlighted
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Box(modifier = Modifier.weight(1f)) {
//                        val annotatedString = buildAnnotatedStringWithHighlightedNames(
//                            text = textFieldValue.text,
//                            members = members,
//                            itemNames = itemNames
//                        )
//
//                        BasicTextField(
//                            value = textFieldValue,
//                            onValueChange = { newValue ->
//                                textFieldValue = newValue
//                                cursorPosition = newValue.selection.end
//
//                                // Check current word for suggestions
//                                val currentWord = getCurrentWordAtCursor(newValue.text, cursorPosition)
//                                if (currentWord.isNotBlank() && currentWord.length >= 2) {
//                                    val memberSuggestions = members
//                                        .map { it.user.name }
//                                        .filter { it.startsWith(currentWord, ignoreCase = true) }
//
//                                    val itemSuggestions = itemNames
//                                        .filter { it.startsWith(currentWord, ignoreCase = true) }
//
//                                    suggestionItems = (memberSuggestions + itemSuggestions).distinct()
//                                    showSuggestions = suggestionItems.isNotEmpty()
//                                } else {
//                                    showSuggestions = false
//                                }
//                            },
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(8.dp)
//                                .onFocusChanged { focusState ->
//                                    if (!focusState.isFocused) {
//                                        showSuggestions = false
//                                    }
//                                },
//                            textStyle = MaterialTheme.typography.bodyMedium.copy(
//                                color = MaterialTheme.colorScheme.onSurface
//                            ),
//                            decorationBox = { innerTextField ->
//                                Box {
//                                    if (textFieldValue.text.isEmpty()) {
//                                        Text(
//                                            text = "E.g., Split the bill equally between John and Sarah, but John pays for his extra drink...",
//                                            style = MaterialTheme.typography.bodyMedium,
//                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
//                                        )
//                                    }
//                                    innerTextField()
//                                }
//                            }
//                        )
//
//                        // Suggestions panel
//                        if (showSuggestions && suggestionItems.isNotEmpty()) {
//                            Card(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(top = 40.dp),
//                                shape = RoundedCornerShape(8.dp),
//                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//                            ) {
//                                Column(modifier = Modifier.padding(8.dp)) {
//                                    for (suggestion in suggestionItems.take(5)) {
//                                        Text(
//                                            text = suggestion,
//                                            modifier = Modifier
//                                                .fillMaxWidth()
//                                                .clickable {
//                                                    // Replace current word with suggestion
//                                                    val currentWord = getCurrentWordAtCursor(textFieldValue.text, cursorPosition)
//                                                    val wordStart = cursorPosition - currentWord.length
//                                                    val beforeWord = textFieldValue.text.substring(0, wordStart)
//                                                    val afterWord = textFieldValue.text.substring(cursorPosition)
//                                                    val newText = "$beforeWord$suggestion$afterWord"
//
//                                                    textFieldValue = TextFieldValue(
//                                                        text = newText,
//                                                        selection = TextRange(wordStart + suggestion.length)
//                                                    )
//                                                    cursorPosition = wordStart + suggestion.length
//
//                                                    // Add member to selected members if it's a member
//                                                    members.find { it.user.name == suggestion }?.let {
//                                                        if (!selectedMembers.contains(it)) {
//                                                            selectedMembers.add(it)
//                                                        }
//                                                    }
//
//                                                    showSuggestions = false
//                                                }
//                                                .padding(vertical = 8.dp, horizontal = 16.dp),
//                                            style = MaterialTheme.typography.bodyMedium
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    IconButton(
//                        onClick = {
//                            if (textFieldValue.text.isNotBlank()) {
//                                onPromptSent(textFieldValue.text, selectedMembers)
//                                focusManager.clearFocus()
//                            }
//                        }
//                    ) {
//                        Icon(
//                            imageVector = Icons.AutoMirrored.Filled.Send,
//                            contentDescription = "Send Prompt",
//                            tint = MaterialTheme.colorScheme.primary
//                        )
//                    }
//                }
//
//                // Display selected members chips
//                if (selectedMembers.isNotEmpty()) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(8.dp),
//                        horizontalArrangement = Arrangement.Start
//                    ) {
//                        Text(
//                            text = "Including: ",
//                            style = MaterialTheme.typography.bodySmall
//                        )
//
//                        Row(
//                            modifier = Modifier.horizontalScroll(rememberScrollState())
//                        ) {
//                            selectedMembers.forEach { member ->
//                                SuggestionChip(
//                                    onClick = { /* Nothing */ },
//                                    label = { Text(member.user.name) },
//                                    modifier = Modifier.padding(end = 4.dp)
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    Spacer(modifier = Modifier.height(16.dp))
//
//    if (aiResponse.isNotBlank()) {
//        Card(
//            modifier = Modifier
//                .fillMaxWidth(),
//            shape = RoundedCornerShape(12.dp)
//        ) {
//            AndroidView(
//                factory = { ctx ->
//                    android.widget.TextView(ctx).apply {
//                        setTextIsSelectable(true)
//                        setPadding(16, 16, 16, 16)
//                    }
//                },
//                update = { textView ->
//                    markwon.setMarkdown(textView, aiResponse)
//                },
//                modifier = Modifier.fillMaxSize()
//            )
//        }
//    }
//}
//
//@Composable
//fun TransferDetailsPage(
//    members: List<Member>,
//    aiResponse: String,
//    onPromptSent: (String, List<Member>) -> Unit
//) {
//    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
//    val selectedMembers = remember { mutableStateListOf<Member>() }
//    var cursorPosition by remember { mutableStateOf(0) }
//    var showSuggestions by remember { mutableStateOf(false) }
//    var suggestionItems by remember { mutableStateOf(emptyList<String>()) }
//    val focusManager = LocalFocusManager.current
//    val context = LocalContext.current
//
//    // Initialize Markwon
//    val markwon = remember {
//        Markwon.builder(context)
//            .usePlugin(StrikethroughPlugin.create())
//            .usePlugin(TaskListPlugin.create(context))
//            .usePlugin(TablePlugin.create(context))
//            .build()
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        Text(
//            text = "Transfer Details",
//            style = MaterialTheme.typography.titleMedium,
//            modifier = Modifier.padding(bottom = 8.dp)
//        )
//
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .border(
//                    width = 1.dp,
//                    color = MaterialTheme.colorScheme.outline,
//                    shape = RoundedCornerShape(12.dp)
//                ),
//            shape = RoundedCornerShape(12.dp)
//        ) {
//            Column(modifier = Modifier.padding(8.dp)) {
//                // Input field with member names highlighted
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Box(modifier = Modifier.weight(1f)) {
//                        val annotatedString = buildAnnotatedStringWithHighlightedNames(
//                            text = textFieldValue.text,
//                            members = members,
//                            itemNames = emptyList()
//                        )
//
//                        BasicTextField(
//                            value = textFieldValue,
//                            onValueChange = { newValue ->
//                                textFieldValue = newValue
//                                cursorPosition = newValue.selection.end
//
//                                // Check current word for suggestions
//                                val currentWord = getCurrentWordAtCursor(newValue.text, cursorPosition)
//                                if (currentWord.isNotBlank() && currentWord.length >= 2) {
//                                    val memberSuggestions = members
//                                        .map { it.user.name }
//                                        .filter { it.startsWith(currentWord, ignoreCase = true) }
//
//                                    suggestionItems = memberSuggestions.distinct()
//                                    showSuggestions = suggestionItems.isNotEmpty()
//                                } else {
//                                    showSuggestions = false
//                                }
//                            },
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(8.dp)
//                                .onFocusChanged { focusState ->
//                                    if (!focusState.isFocused) {
//                                        showSuggestions = false
//                                    }
//                                },
//                            textStyle = MaterialTheme.typography.bodyMedium.copy(
//                                color = MaterialTheme.colorScheme.onSurface
//                            ),
//                            decorationBox = { innerTextField ->
//                                Box {
//                                    if (textFieldValue.text.isEmpty()) {
//                                        Text(
//                                            text = "E.g., John transfers $20 to Sarah...",
//                                            style = MaterialTheme.typography.bodyMedium,
//                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
//                                        )
//                                    }
//                                    innerTextField()
//                                }
//                            }
//                        )
//
//                        // Suggestions panel
//                        if (showSuggestions && suggestionItems.isNotEmpty()) {
//                            Card(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(top = 40.dp),
//                                shape = RoundedCornerShape(8.dp),
//                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//                            ) {
//                                Column(modifier = Modifier.padding(8.dp)) {
//                                    for (suggestion in suggestionItems.take(5)) {
//                                        Text(
//                                            text = suggestion,
//                                            modifier = Modifier
//                                                .fillMaxWidth()
//                                                .clickable {
//                                                    // Replace current word with suggestion
//                                                    val currentWord = getCurrentWordAtCursor(textFieldValue.text, cursorPosition)
//                                                    val wordStart = cursorPosition - currentWord.length
//                                                    val beforeWord = textFieldValue.text.substring(0, wordStart)
//                                                    val afterWord = textFieldValue.text.substring(cursorPosition)
//                                                    val newText = "$beforeWord$suggestion$afterWord"
//
//                                                    textFieldValue = TextFieldValue(
//                                                        text = newText,
//                                                        selection = TextRange(wordStart + suggestion.length)
//                                                    )
//                                                    cursorPosition = wordStart + suggestion.length
//
//                                                    // Add member to selected members if it's a member
//                                                    members.find { it.user.name == suggestion }?.let {
//                                                        if (!selectedMembers.contains(it)) {
//                                                            selectedMembers.add(it)
//                                                        }
//                                                    }
//
//                                                    showSuggestions = false
//                                                }
//                                                .padding(vertical = 8.dp, horizontal = 16.dp),
//                                            style = MaterialTheme.typography.bodyMedium
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    IconButton(
//                        onClick = {
//                            if (textFieldValue.text.isNotBlank()) {
//                                onPromptSent(textFieldValue.text, selectedMembers)
//                                focusManager.clearFocus()
//                            }
//                        }
//                    ) {
//                        Icon(
//                            imageVector = Icons.AutoMirrored.Filled.Send,
//                            contentDescription = "Send Prompt",
//                            tint = MaterialTheme.colorScheme.primary
//                        )
//                    }
//                }
//
//                // Display selected members chips
//                if (selectedMembers.isNotEmpty()) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(8.dp),
//                        horizontalArrangement = Arrangement.Start
//                    ) {
//                        Text(
//                            text = "Including: ",
//                            style = MaterialTheme.typography.bodySmall
//                        )
//
//                        Row(
//                            modifier = Modifier.horizontalScroll(rememberScrollState())
//                        ) {
//                            selectedMembers.forEach { member ->
//                                SuggestionChip(
//                                    onClick = { /* Nothing */ },
//                                    label = { Text(member.user.name) },
//                                    modifier = Modifier.padding(end = 4.dp)
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        if (aiResponse.isNotBlank()) {
//            Card(
//                modifier = Modifier
//                    .weight(1f)
//                    .fillMaxWidth(),
//                shape = RoundedCornerShape(12.dp)
//            ) {
//                AndroidView(
//                    factory = { ctx ->
//                        android.widget.TextView(ctx).apply {
//                            setTextIsSelectable(true)
//                            setPadding(16, 16, 16, 16)
//                        }
//                    },
//                    update = { textView ->
//                        markwon.setMarkdown(textView, aiResponse)
//                    },
//                    modifier = Modifier.fillMaxSize()
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun TransactionProposalPage(
//    transactionType: TransactionType,
//    summaryData: String
//) {
//    val context = LocalContext.current
//    val jsonData = remember(summaryData) {
//        try {
//            if (summaryData.isNotBlank()) {
//                JSONObject(summaryData)
//            } else {
//                null
//            }
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//    val markwon = remember {
//        Markwon.builder(context)
//            .usePlugin(StrikethroughPlugin.create())
//            .usePlugin(TaskListPlugin.create(context))
//            .usePlugin(TablePlugin.create(context))
//            .build()
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        Text(
//            text = if (transactionType == TransactionType.Expense) "Expense Summary" else "Transfer Summary",
//            style = MaterialTheme.typography.titleMedium,
//            modifier = Modifier.padding(bottom = 16.dp)
//        )
//
//        if (jsonData != null) {
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f),
//                shape = RoundedCornerShape(12.dp)
//            ) {
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(16.dp)
//                ) {
//                    item {
//                        Text(
//                            text = if (transactionType == TransactionType.Expense) "Expense Details" else "Transfer Details",
//                            style = MaterialTheme.typography.titleSmall,
//                            fontWeight = FontWeight.Bold,
//                            modifier = Modifier.padding(bottom = 8.dp)
//                        )
//                    }
//
//                    // Display transaction details based on type
//                    if (transactionType == TransactionType.Expense) {
//                        // Expense breakdown
//                        try {
//                            val breakdown = jsonData.getJSONArray("breakdown")
//                            if (breakdown.length() > 0) {
//                                item {
//                                    Text(
//                                        text = "Items",
//                                        style = MaterialTheme.typography.bodyMedium,
//                                        fontWeight = FontWeight.Bold,
//                                        modifier = Modifier.padding(vertical = 8.dp)
//                                    )
//                                }
//
//                                items(breakdown.length()) { index ->
//                                    val item = breakdown.getJSONObject(index)
//                                    Row(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(vertical = 4.dp),
//                                        horizontalArrangement = Arrangement.SpaceBetween
//                                    ) {
//                                        Text(
//                                            text = item.getString("item"),
//                                            style = MaterialTheme.typography.bodyMedium
//                                        )
//
//                                        Row {
//                                            if (item.has("quantity") && !item.isNull("quantity")) {
//                                                Text(
//                                                    text = "x${item.getInt("quantity")}",
//                                                    style = MaterialTheme.typography.bodyMedium,
//                                                    modifier = Modifier.padding(end = 8.dp)
//                                                )
//                                            }
//
//                                            Text(
//                                                text = "$${item.getString("amount")}",
//                                                style = MaterialTheme.typography.bodyMedium,
//                                                fontWeight = FontWeight.Bold
//                                            )
//                                        }
//                                    }
//                                    Divider(modifier = Modifier.padding(vertical = 4.dp))
//                                }
//                            }
//
//                            // Paid by details
//                            val details = jsonData.getJSONObject("details")
//                            val response = details.getJSONObject("response")
//                            val paidBy = response.getJSONArray("paidBy")
//
//                            if (paidBy.length() > 0) {
//                                item {
//                                    Text(
//                                        text = "Paid By",
//                                        style = MaterialTheme.typography.bodyMedium,
//                                        fontWeight = FontWeight.Bold,
//                                        modifier = Modifier.padding(vertical = 8.dp)
//                                    )
//                                }
//
//                                items(paidBy.length()) { index ->
//                                    val payee = paidBy.getJSONObject(index)
//                                    Row(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(vertical = 4.dp),
//                                        horizontalArrangement = Arrangement.SpaceBetween
//                                    ) {
//                                        Text(
//                                            text = payee.getString("member"),
//                                            style = MaterialTheme.typography.bodyMedium
//                                        )
//
//                                        Text(
//                                            text = "$${payee.getString("amount")}",
//                                            style = MaterialTheme.typography.bodyMedium,
//                                            fontWeight = FontWeight.Bold
//                                        )
//                                    }
//                                }
//                            }
//
//                            // Paid for details
//                            val paidFor = response.getJSONArray("paidFor")
//
//                            if (paidFor.length() > 0) {
//                                item {
//                                    Text(
//                                        text = "Paid For",
//                                        style = MaterialTheme.typography.bodyMedium,
//                                        fontWeight = FontWeight.Bold,
//                                        modifier = Modifier.padding(vertical = 8.dp)
//                                    )
//                                }
//
//                                items(paidFor.length()) { index ->
//                                    val recipient = paidFor.getJSONObject(index)
//                                    Row(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(vertical = 4.dp),
//                                        horizontalArrangement = Arrangement.SpaceBetween
//                                    ) {
//                                        Text(
//                                            text = recipient.getString("member"),
//                                            style = MaterialTheme.typography.bodyMedium
//                                        )
//
//                                        Text(
//                                            text = "$${recipient.getString("amount")}",
//                                            style = MaterialTheme.typography.bodyMedium,
//                                            fontWeight = FontWeight.Bold
//                                        )
//                                    }
//
//                                    // Display bill items for this member if available
//                                    if (recipient.has("bill")) {
//                                        val bill = recipient.getJSONArray("bill")
//                                        if (bill.length() > 0) {
//                                            Column(
//                                                modifier = Modifier
//                                                    .fillMaxWidth()
//                                                    .padding(start = 16.dp, top = 4.dp, bottom = 8.dp)
//                                            ) {
//                                                for (i in 0 until bill.length()) {
//                                                    val billItem = bill.getJSONObject(i)
//                                                    Row(
//                                                        modifier = Modifier
//                                                            .fillMaxWidth()
//                                                            .padding(vertical = 2.dp),
//                                                        horizontalArrangement = Arrangement.SpaceBetween
//                                                    ) {
//                                                        Text(
//                                                            text = billItem.getString("item"),
//                                                            style = MaterialTheme.typography.bodySmall,
//                                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                                                        )
//
//                                                        Row {
//                                                            if (billItem.has("quantity")) {
//                                                                Text(
//                                                                    text = "x${billItem.getInt("quantity")}",
//                                                                    style = MaterialTheme.typography.bodySmall,
//                                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
//                                                                    modifier = Modifier.padding(end = 8.dp)
//                                                                )
//                                                            }
//
//                                                            Text(
//                                                                text = "$${billItem.getString("amount")}",
//                                                                style = MaterialTheme.typography.bodySmall,
//                                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                                                            )
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
//
//                                    Divider(modifier = Modifier.padding(vertical = 4.dp))
//                                }
//                            }
//
//                        } catch (e: Exception) {
//                            item {
//                                Text(
//                                    text = "Error parsing expense data: ${e.message}",
//                                    style = MaterialTheme.typography.bodyMedium,
//                                    color = MaterialTheme.colorScheme.error
//                                )
//                            }
//                        }
//                    } else {
//                        // Transfer details
//                        try {
//                            val details = jsonData.getJSONObject("details")
//                            val response = details.getJSONObject("response")
//
//                            // From details
//                            val from = response.getJSONArray("from")
//                            if (from.length() > 0) {
//                                item {
//                                    Text(
//                                        text = "From",
//                                        style = MaterialTheme.typography.bodyMedium,
//                                        fontWeight = FontWeight.Bold,
//                                        modifier = Modifier.padding(vertical = 8.dp)
//                                    )
//                                }
//
//                                items(from.length()) { index ->
//                                    val sender = from.getJSONObject(index)
//                                    Row(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(vertical = 4.dp),
//                                        horizontalArrangement = Arrangement.SpaceBetween
//                                    ) {
//                                        Text(
//                                            text = sender.getString("member"),
//                                            style = MaterialTheme.typography.bodyMedium
//                                        )
//
//                                        Text(
//                                            text = "$${sender.getString("amount")}",
//                                            style = MaterialTheme.typography.bodyMedium,
//                                            fontWeight = FontWeight.Bold
//                                        )
//                                    }
//                                    Divider(modifier = Modifier.padding(vertical = 4.dp))
//                                }
//                            }
//
//                            // To details
//                            val to = response.getJSONArray("to")
//                            if (to.length() > 0) {
//                                item {
//                                    Text(
//                                        text = "To",
//                                        style = MaterialTheme.typography.bodyMedium,
//                                        fontWeight = FontWeight.Bold,
//                                        modifier = Modifier.padding(vertical = 8.dp)
//                                    )
//                                }
//
//                                items(to.length()) { index ->
//                                    val receiver = to.getJSONObject(index)
//                                    Row(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(vertical = 4.dp),
//                                        horizontalArrangement = Arrangement.SpaceBetween
//                                    ) {
//                                        Text(
//                                            text = receiver.getString("member"),
//                                            style = MaterialTheme.typography.bodyMedium
//                                        )
//
//                                        Text(
//                                            text = "$${receiver.getString("amount")}",
//                                            style = MaterialTheme.typography.bodyMedium,
//                                            fontWeight = FontWeight.Bold
//                                        )
//                                    }
//                                    Divider(modifier = Modifier.padding(vertical = 4.dp))
//                                }
//                            }
//
//                        } catch (e: Exception) {
//                            item {
//                                Text(
//                                    text = "Error parsing transfer data: ${e.message}",
//                                    style = MaterialTheme.typography.bodyMedium,
//                                    color = MaterialTheme.colorScheme.error
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        } else {
//            // Display text-based summary if JSON is not available
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f),
//                shape = RoundedCornerShape(12.dp)
//            ) {
//                if (summaryData.isNotBlank()) {
//                    AndroidView(
//                        factory = { ctx ->
//                            android.widget.TextView(ctx).apply {
//                                setTextIsSelectable(true)
//                                setPadding(16, 16, 16, 16)
//                            }
//                        },
//                        update = { textView ->
//                            markwon.setMarkdown(textView, summaryData)
//                        },
//                        modifier = Modifier.fillMaxSize()
//                    )
//                } else {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(16.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = "No transaction summary available",
//                            style = MaterialTheme.typography.bodyLarge,
//                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun AIPromptDialog(
//    initialPrompt: String,
//    onDismiss: () -> Unit,
//    onPromptSent: (String) -> Unit
//) {
//    var promptText by remember { mutableStateOf(initialPrompt) }
//
//    Dialog(
//        onDismissRequest = onDismiss,
//        properties = DialogProperties(dismissOnClickOutside = true)
//    ) {
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            shape = RoundedCornerShape(16.dp)
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp)
//            ) {
//                Text(
//                    text = "AI Prompt",
//                    style = MaterialTheme.typography.titleLarge,
//                    modifier = Modifier.padding(bottom = 16.dp)
//                )
//
//                OutlinedTextField(
//                    value = promptText,
//                    onValueChange = { promptText = it },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(200.dp),
//                    label = { Text("Enter your prompt") },
//                    textStyle = MaterialTheme.typography.bodyMedium
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.End
//                ) {
//                    TextButton(onClick = onDismiss) {
//                        Text("Cancel")
//                    }
//
//                    Spacer(modifier = Modifier.width(8.dp))
//
//                    Button(
//                        onClick = { onPromptSent(promptText) },
//                        enabled = promptText.isNotBlank()
//                    ) {
//                        Text("Send")
//                    }
//                }
//            }
//        }
//    }
//}
//
//// Helper functions
//fun buildAnnotatedStringWithHighlightedNames(
//    text: String,
//    members: List<Member>,
//    itemNames: List<String>
//): AnnotatedString {
//    return buildAnnotatedString {
//        val words = text.split(" ", ",", ".", "!", "?", ":", ";", "(", ")")
//        var currentPos = 0
//
//        for (word in words) {
//            val memberMatch = members.find { it.user.name.equals(word, ignoreCase = true) }
//            val itemMatch = itemNames.find { it.equals(word, ignoreCase = true) }
//
//            val wordStartPos = text.indexOf(word, currentPos)
//            if (wordStartPos >= 0) {
//                val wordEndPos = wordStartPos + word.length
//
//                // Add text before the current word
//                append(text.substring(currentPos, wordStartPos))
//
//                // Add the current word with style if it matches a member or item
//                if (memberMatch != null) {
//                    withStyle(SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold)) {
//                        append(text.substring(wordStartPos, wordEndPos))
//                    }
//                } else if (itemMatch != null) {
//                    withStyle(SpanStyle(color = Color.Green, fontWeight = FontWeight.Bold)) {
//                        append(text.substring(wordStartPos, wordEndPos))
//                    }
//                } else {
//                    append(text.substring(wordStartPos, wordEndPos))
//                }
//
//                currentPos = wordEndPos
//            }
//        }
//
//        // Add any remaining text
//        if (currentPos < text.length) {
//            append(text.substring(currentPos))
//        }
//    }
//}
//
//fun getCurrentWordAtCursor(text: String, cursorPosition: Int): String {
//    if (text.isEmpty() || cursorPosition == 0) return ""
//
//    // Find word boundaries
//    val start = text.lastIndexOfAny(charArrayOf(' ', '\n', ',', '.', '!', '?', ':', ';', '(', ')', '\t'), cursorPosition - 1) + 1
//    val end = minOf(text.length, text.indexOfAny(charArrayOf(' ', '\n', ',', '.', '!', '?', ':', ';', '(', ')', '\t'), cursorPosition).let { if (it == -1) text.length else it })
//
//    return if (start <= end) text.substring(start, end) else ""
//}
//
//fun extractItemNamesFromResponse(response: String): List<String> {
//    val itemNames = mutableListOf<String>()
//
//    // Simple parsing strategy - look for lines containing "item" and "price" or "amount"
//    // This could be improved depending on how structured the AI response is
//    val lines = response.split("\n")
//
//    // Check for markdown table format
//    val tableHeaderLine = lines.indexOfFirst { it.contains("|") && (it.contains("item", ignoreCase = true) || it.contains("name", ignoreCase = true)) && (it.contains("price", ignoreCase = true) || it.contains("amount", ignoreCase = true)) }
//
//    if (tableHeaderLine >= 0 && tableHeaderLine < lines.size - 1) {
//        // We have a markdown table
//        val headerParts = lines[tableHeaderLine].split("|").map { it.trim() }
//        val itemColumnIndex = headerParts.indexOfFirst { it.contains("item", ignoreCase = true) || it.contains("name", ignoreCase = true) }
//
//        if (itemColumnIndex >= 0) {
//            // Now extract items from the table
//            for (i in (tableHeaderLine + 2) until lines.size) {
//                val line = lines[i]
//                if (!line.contains("|")) break // End of table
//
//                val parts = line.split("|").map { it.trim() }
//                if (parts.size > itemColumnIndex && parts[itemColumnIndex].isNotBlank()) {
//                    itemNames.add(parts[itemColumnIndex])
//                }
//            }
//        }
//    } else {
//        // Try to find items in other formats
//        for (line in lines) {
//            if ((line.contains("item", ignoreCase = true) || line.contains("name", ignoreCase = true)) &&
//                (line.contains("$") || line.contains("price", ignoreCase = true) || line.contains("amount", ignoreCase = true))) {
//
//                // Extract item name (very simple approach - could be improved)
//                val itemNameMatch = Regex("\"([^\"]+)\"|'([^']+)'|([\\w\\s]+)(?=\\s*[-:$])").find(line)
//                itemNameMatch?.groupValues?.firstOrNull { it.isNotEmpty() }?.let {
//                    itemNames.add(it.trim())
//                }
//            }
//        }
//    }
//
//    return itemNames.distinct()
//}
//
//@Composable
//fun ListDetailPaneScaffold(
//    listPane: @Composable () -> Unit,
//    detailPane: @Composable () -> Unit,
//    displayFeatures: List<DisplayFeature> = emptyList()
//) {
//    Row(
//        modifier = Modifier.fillMaxSize()
//    ) {
//        Box(
//            modifier = Modifier.width(80.dp)
//        ) {
//            listPane()
//        }
//
//        Box(
//            modifier = Modifier
//                .fillMaxHeight()
//                .weight(1f)
//        ) {
//            detailPane()
//        }
//    }
//}