package com.example.spliteasy.aichat

import android.graphics.Bitmap
import android.net.Uri
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun AIChatScreen(
    aiChatViewModel: AIChatViewModel = viewModel(),
    onBackClick: () -> Unit={},
) {
    val hasAttachedImage by aiChatViewModel.hasAttachedImage.collectAsState()
    val messages by aiChatViewModel.messages.collectAsState()
    val uiState by aiChatViewModel.uiState.collectAsState()
    var userInput by remember { mutableStateOf("") }
    var showImageDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    var isImageLoading by remember { mutableStateOf(false) }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        isImageLoading = true
        uri?.let { selectedUri ->
            val inputStream = context.contentResolver.openInputStream(selectedUri)
            val bitmap = inputStream?.use { android.graphics.BitmapFactory.decodeStream(it) }
            bitmap?.let { bmp ->
                aiChatViewModel.setImage(bmp)
            }
        }
        isImageLoading = false
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        isImageLoading = true
        bitmap?.let { bmp ->
            aiChatViewModel.setImage(bmp)
        }
        isImageLoading = false
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Assist") },
                navigationIcon = {
                    IconButton(onClick = {
                        // Handle back navigation here
                        onBackClick()
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Back")
                    }
                },
                // Only handle status bars, not IME
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.statusBars.only(WindowInsetsSides.Top)
                )
            )
        },
        // Prevent Scaffold from adjusting for IME
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            Box(modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .navigationBarsPadding()
                .padding(PaddingValues(8.dp))
            ){
                ChatInputBar(
                    userInput = userInput,
                    onUserInputChange = { userInput = it },
                    onSend = {
                        aiChatViewModel.sendMessage(userInput)
                        userInput = ""
                    },
                    onAttachClick = { showImageDialog = true },
                    canSend = (userInput.isNotBlank() || aiChatViewModel.hasAttachedImage()) && uiState !is ChatUiState.Loading,
                    isLoading = uiState is ChatUiState.Loading,
                    showImage = hasAttachedImage,
                    showImageLoader = isImageLoading,
                    bitmap = aiChatViewModel.getAttachedImage(),
                    onImageRemoveClick = { aiChatViewModel.clearImage() }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding),
            state = listState,
        ) {
            items(messages) { message ->
                ChatMessageItem(message = message)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()){
        // Loading indicator
        if (uiState is ChatUiState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        }

        // Error display
        if (uiState is ChatUiState.Error) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            ) {
                Text(
                    text = (uiState as ChatUiState.Error).errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    // Image source selection dialog
    if (showImageDialog) {
        AlertDialog(
            onDismissRequest = { showImageDialog = false },
            title = { Text("Choose Image Source") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TextButton(
                        onClick = {
                            imagePickerLauncher.launch("image/*")
                            showImageDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Gallery") }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            cameraLauncher.launch(null)
                            showImageDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Camera") }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
@Preview
fun ChatInputBar(
    userInput: String="",
    onUserInputChange: (String) -> Unit={},
    onSend: () -> Unit={},
    onAttachClick: () -> Unit={},
    canSend: Boolean=false,
    isLoading: Boolean=false,
    bitmap: Bitmap?=null,
    showImage: Boolean=false,
    showImageLoader:Boolean=false,
    onImageRemoveClick: () -> Unit={}
) {
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            if(showImageLoader) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(
                                    Color.LightGray.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }
            else if (bitmap !== null && showImage) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.TopEnd,
                    ){
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Image Preview",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(8.dp)
                                ),
                            contentScale = ContentScale.Crop
                        )
                        Box(modifier=Modifier.padding(4.dp)){
                            IconButton(
                                onClick = onImageRemoveClick,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                        CircleShape
                                    )
                                    .size(24.dp)
                                    .padding(4.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Remove Image")
                            }
                        }
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onAttachClick) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Attach Image",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                BasicTextField(
                    value = userInput,
                    onValueChange = onUserInputChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    decorationBox = { innerTextField ->
                        Box {
                            if (userInput.isEmpty()) {
                                Text(
                                    "Type a message...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            innerTextField()
                        }
                    },
                    maxLines = 5
                )

                IconButton(
                    onClick = onSend,
                    enabled = canSend
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }


@Composable
fun ChatMessageItem(message: Message) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Get local context to initialize Markwon
    val context = LocalContext.current

    // Initialize Markwon
    val markwon = remember {
        Markwon.builder(context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(context))
            .usePlugin(TablePlugin.create(context))
            .build()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start
    ) {
        // Optional sender label
        Text(
            text = if (message.isFromUser) "You" else "AI Assistant",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Message content
        Card(
            shape = RoundedCornerShape(
                topStart = if (message.isFromUser) 16.dp else 0.dp,
                topEnd = if (message.isFromUser) 0.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            modifier = Modifier.padding(top = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Display attached image if available
                message.imageAttachment?.let { bitmap ->
                    val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

                    // Calculate dimensions with constraints (max 50% of width/height)
                    val maxWidth = screenWidth * 0.5f
                    val maxHeight = screenHeight * 0.5f

                    // Calculate dimensions while preserving aspect ratio
                    val imageWidth = minOf(maxWidth, maxHeight * aspectRatio)
                    val imageHeight = imageWidth / aspectRatio

                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Attached Image",
                        modifier = Modifier
                            .size(width = imageWidth, height = imageHeight)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Use the proper MarkdownText component
                MarkdownText(
                    markdown = message.content,
                    markwon = markwon
                )
            }
        }
    }
}

@Composable
fun MarkdownText(markdown: String, markwon: Markwon) {
    val spanned = remember(markdown) {
        markwon.toMarkdown(markdown)
    }

    AndroidView(
        factory = { context ->
            TextView(context).apply {
                movementMethod = LinkMovementMethod.getInstance()
            }
        },
        update = { textView ->
            markwon.setParsedMarkdown(textView, spanned)
        },
        modifier = Modifier.fillMaxWidth()
    )
}