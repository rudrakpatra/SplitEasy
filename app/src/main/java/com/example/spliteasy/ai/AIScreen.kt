package com.example.spliteasy.ai

import android.graphics.Bitmap
import android.net.Uri
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.TextView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.spliteasy.auth.AuthManager
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AIScreen(
    viewModel: AIViewModel = AIViewModel(),
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hasAttachedImage by viewModel.hasAttachedImage.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val authState by AuthManager.state.collectAsState()
    val displayName= authState.user?.displayName
    val title = if(displayName !== null)"Hello $displayName" else "See you Again"
    var userInput by remember { mutableStateOf("") }

    var showImageDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    var isImageLoading by remember { mutableStateOf(false) }

    val markwon = remember {
        Markwon.builder(context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .usePlugin(TaskListPlugin.create(context))
            .usePlugin(HtmlPlugin.create())
            .build()
    }

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
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(selectedUri)
                    val bitmap = inputStream?.use { android.graphics.BitmapFactory.decodeStream(it) }
                    bitmap?.let { bmp ->
                        viewModel.setImage(bmp)
                    }
                } catch (e: Exception) {
                    Log.e("AIScreen", "Error loading image", e)
                } finally {
                    isImageLoading = false
                }
            }
        } ?: run {
            isImageLoading = false
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        isImageLoading = true
        bitmap?.let { bmp ->
            viewModel.setImage(bmp)
        }
        isImageLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick ={
                        viewModel.endSession()
                        onBackClick()
                    } ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(8.dp)
            ) {
                ChatInputBar(
                    userInput = userInput,
                    onUserInputChange = { userInput = it },
                    onSend = {
                        if (userInput.isNotBlank() || viewModel.hasAttachedImage()) {
                            viewModel.sendMessage(userInput)
                            userInput = ""
                        }
                    },
                    onAttachClick = { showImageDialog = true },
                    canSend = (userInput.isNotBlank() || viewModel.hasAttachedImage()) && uiState !is ChatUiState.Loading,
                    isLoading = uiState is ChatUiState.Loading,
                    showImage = hasAttachedImage,
                    showImageLoader = isImageLoading,
                    attachedImageBitmap = viewModel.getAttachedImageBitmap(),
                    onImageRemoveClick = { viewModel.clearImage() }
                )
            }
        }
    ) { innerPadding ->
        // Create a box that fills the screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Messages list that fills available space
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                items(messages) { message ->
                    ChatMessageItem(message, markwon, "text")
                }
                if (uiState is ChatUiState.Loading) {
                    item {
                        ChatMessageItem(Message("...", false, null), markwon, "loading")
                    }
                }

                // Show an error message if there's an error
                if (uiState is ChatUiState.Error) {
                    item {
                        val errorMessage = (uiState as ChatUiState.Error).errorMessage
                        ChatMessageItem(Message(errorMessage, false, null), markwon, "error")
                    }
                }
            }
        }
    }

    // Image source selection dialog
    if (showImageDialog) {
        AlertDialog(
            onDismissRequest = { showImageDialog = false },
            title = { Text("Attach a Photo") },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            cameraLauncher.launch(null)
                            showImageDialog = false
                        }
                    ) { Text("Camera") }

                    TextButton(
                        onClick = {
                            imagePickerLauncher.launch("image/*")
                            showImageDialog = false
                        }
                    ) { Text("Gallery") }
                }
            }
            ,
            confirmButton = {
                TextButton(onClick = {showImageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview
@Composable
fun ChatInputBar(
    userInput: String = "",
    onUserInputChange: (String) -> Unit = {},
    onSend: () -> Unit = {},
    onAttachClick: () -> Unit = {},
    canSend: Boolean = false,
    isLoading: Boolean = false,
    attachedImageBitmap: Bitmap? = null,
    showImage: Boolean = false,
    showImageLoader: Boolean = false,
    onImageRemoveClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        if (showImageLoader) {
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
        } else if (attachedImageBitmap !== null && showImage) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.TopEnd,
                ) {
                    Image(
                        bitmap = attachedImageBitmap.asImageBitmap(),
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
                    Box(modifier = Modifier.padding(4.dp)) {
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
                        tint = if (canSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ChatMessageItem(message: Message= Message("**Hello World**", false), markwon: Markwon = Markwon.create(LocalContext.current),type:String="text") {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val spannedText = remember(markwon, message.content) {
        markwon.toMarkdown(message.content)
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth(),
        horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start
    ) {
        // Optional sender label
        Text(
            text = if (message.isFromUser) {
                "You"
            } else {
                "Split Easy AI"
            },
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
            modifier = Modifier
                .padding(top = 2.dp, start = if (message.isFromUser) 32.dp else 0.dp, end = if (!message.isFromUser) 32.dp else 0.dp)
                .align(if (message.isFromUser) Alignment.End else Alignment.Start)
            ,
            colors = CardDefaults.cardColors(
                when (type) {
                    "error" -> MaterialTheme.colorScheme.errorContainer
                    else -> when (message.isFromUser) {
                        true -> MaterialTheme.colorScheme.primaryContainer
                        false -> MaterialTheme.colorScheme.surfaceContainer
                    }
                }
            ),
            content = {
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
                    AndroidView(
                        factory = { context ->
                            TextView(context).apply {
                                movementMethod = LinkMovementMethod.getInstance()
                            }
                        },
                        update = { textView ->
                            markwon.setParsedMarkdown(textView, spannedText)
                        },
                        modifier = Modifier.wrapContentWidth()
                    )
                }
            })
    }
}