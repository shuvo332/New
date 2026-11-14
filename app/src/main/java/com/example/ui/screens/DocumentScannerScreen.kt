package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.ui.components.GlassCard
import com.example.ui.theme.CyanVibrant
import com.example.ui.theme.EmeraldVibrant
import com.example.ui.theme.GlassWhiteStrong
import com.example.ui.theme.MintGlow
import com.example.ui.theme.MissingRed
import com.example.ui.theme.MissingRedBg
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TealDeep
import com.example.util.ImageLoaderUtil
import java.io.File

@Composable
fun DocumentScannerScreen(
    targetSlot: String,
    onImageCaptured: (Uri) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isInvoice = targetSlot == "invoice"

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionDeniedOnce by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            permissionDeniedOnce = true
        }
    }

    // Refresh permission state when returning from App Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasCameraPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Request camera permission automatically on first entry if not granted
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Fallback launchers (System Camera & Photo Picker)
    var pendingSystemCaptureUri by remember { mutableStateOf<Uri?>(null) }
    val systemCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingSystemCaptureUri?.let(onImageCaptured)
        }
    }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let(onImageCaptured)
    }

    var cameraError by remember { mutableStateOf<String?>(null) }
    var useFrontCamera by remember { mutableStateOf(false) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_AUTO) }
    var isCapturing by remember { mutableStateOf(false) }

    val title = stringResource(
        if (isInvoice) R.string.camera_scanner_title_invoice else R.string.camera_scanner_title_ss
    )

    if (!hasCameraPermission || cameraError != null) {
        CameraPermissionOrFallbackView(
            title = title,
            hasPermission = hasCameraPermission,
            permissionDenied = permissionDeniedOnce,
            cameraError = cameraError,
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            onOpenSettings = {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null)
                )
                context.startActivity(intent)
            },
            onLaunchSystemCamera = {
                val uri = ImageLoaderUtil.createCaptureUri(
                    context,
                    if (isInvoice) "invoice_doc" else "system_ss"
                )
                pendingSystemCaptureUri = uri
                systemCameraLauncher.launch(uri)
            },
            onLaunchGallery = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onClose = onClose
        )
        return
    }

    val previewView = remember { PreviewView(context) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
    }

    LaunchedEffect(flashMode) {
        imageCapture.flashMode = flashMode
    }

    LaunchedEffect(useFrontCamera, lifecycleOwner) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener(
            {
                try {
                    val cameraProvider = providerFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    val selector = if (useFrontCamera &&
                        cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                    ) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    } else {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    }
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        imageCapture
                    )
                    cameraError = null
                } catch (e: Exception) {
                    cameraError = e.localizedMessage ?: "Camera preview unavailable on this device."
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Document Guide Frame Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val frameWidth = size.width * 0.84f
            val frameHeight = size.height * 0.56f
            val left = (size.width - frameWidth) / 2f
            val top = (size.height - frameHeight) / 2f

            // Subtle dark scrim outside framing box
            drawRect(
                color = Color.Black.copy(alpha = 0.42f),
                topLeft = Offset.Zero,
                size = Size(size.width, top)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.42f),
                topLeft = Offset(0f, top + frameHeight),
                size = Size(size.width, size.height - (top + frameHeight))
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.42f),
                topLeft = Offset(0f, top),
                size = Size(left, frameHeight)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.42f),
                topLeft = Offset(left + frameWidth, top),
                size = Size(size.width - (left + frameWidth), frameHeight)
            )

            // Crisp cyan-emerald document border
            drawRoundRect(
                color = MintGlow.copy(alpha = 0.85f),
                topLeft = Offset(left, top),
                size = Size(frameWidth, frameHeight),
                cornerRadius = CornerRadius(24f, 24f),
                style = Stroke(width = 4f)
            )

            // Accent corner ticks
            val tick = 44f
            val stroke = Stroke(width = 10f, cap = StrokeCap.Round)
            drawLine(CyanVibrant, Offset(left, top + tick), Offset(left, top), strokeWidth = stroke.width)
            drawLine(CyanVibrant, Offset(left, top), Offset(left + tick, top), strokeWidth = stroke.width)
            drawLine(CyanVibrant, Offset(left + frameWidth - tick, top), Offset(left + frameWidth, top), strokeWidth = stroke.width)
            drawLine(CyanVibrant, Offset(left + frameWidth, top), Offset(left + frameWidth, top + tick), strokeWidth = stroke.width)
            drawLine(MintGlow, Offset(left, top + frameHeight - tick), Offset(left, top + frameHeight), strokeWidth = stroke.width)
            drawLine(MintGlow, Offset(left, top + frameHeight), Offset(left + tick, top + frameHeight), strokeWidth = stroke.width)
            drawLine(MintGlow, Offset(left + frameWidth - tick, top + frameHeight), Offset(left + frameWidth, top + frameHeight), strokeWidth = stroke.width)
            drawLine(MintGlow, Offset(left + frameWidth, top + frameHeight), Offset(left + frameWidth, top + frameHeight - tick), strokeWidth = stroke.width)
        }

        // Top Bar Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.35f))
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("camera_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_close_scanner),
                        tint = PureWhite
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, MintGlow.copy(alpha = 0.5f))
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = PureWhite,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.35f))
                ) {
                    IconButton(
                        onClick = {
                            flashMode = when (flashMode) {
                                ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
                                ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_OFF
                                else -> ImageCapture.FLASH_MODE_AUTO
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("camera_flash_button")
                    ) {
                        Icon(
                            imageVector = when (flashMode) {
                                ImageCapture.FLASH_MODE_ON -> Icons.Filled.FlashOn
                                ImageCapture.FLASH_MODE_OFF -> Icons.Filled.FlashOff
                                else -> Icons.Filled.FlashAuto
                            },
                            contentDescription = stringResource(R.string.cd_toggle_flash),
                            tint = if (flashMode == ImageCapture.FLASH_MODE_ON) MintGlow else PureWhite
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.35f))
                ) {
                    IconButton(
                        onClick = { useFrontCamera = !useFrontCamera },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("camera_switch_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Cameraswitch,
                            contentDescription = stringResource(R.string.cd_switch_camera),
                            tint = PureWhite
                        )
                    }
                }
            }
        }

        // Bottom Shutter & Guidance Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.55f)
            ) {
                Text(
                    text = stringResource(R.string.camera_align_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = PureWhite,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Gallery Quick Pick
                Surface(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("camera_gallery_fallback_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = stringResource(R.string.btn_gallery),
                            tint = PureWhite
                        )
                    }
                }

                // Primary Shutter Button
                Surface(
                    onClick = {
                        if (!isCapturing) {
                            isCapturing = true
                            val capturesDir = File(context.cacheDir, "captures").apply { mkdirs() }
                            val photoFile = File(
                                capturesDir,
                                "${if (isInvoice) "invoice" else "ss"}_${System.currentTimeMillis()}.jpg"
                            )
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                            imageCapture.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        isCapturing = false
                                        val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                                        onImageCaptured(savedUri)
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        isCapturing = false
                                        cameraError = exception.localizedMessage ?: "Capture failed."
                                    }
                                }
                            )
                        }
                    },
                    shape = CircleShape,
                    color = EmeraldVibrant,
                    border = BorderStroke(4.dp, PureWhite),
                    modifier = Modifier
                        .size(76.dp)
                        .testTag("camera_shutter_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isCapturing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = PureWhite,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = stringResource(R.string.cd_capture_photo),
                                tint = PureWhite,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // Native System Camera App fallback
                Surface(
                    onClick = {
                        val uri = ImageLoaderUtil.createCaptureUri(
                            context,
                            if (isInvoice) "invoice_doc" else "system_ss"
                        )
                        pendingSystemCaptureUri = uri
                        systemCameraLauncher.launch(uri)
                    },
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("camera_system_app_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = stringResource(R.string.btn_system_camera),
                            tint = MintGlow
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraPermissionOrFallbackView(
    title: String,
    hasPermission: Boolean,
    permissionDenied: Boolean,
    cameraError: String?,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onLaunchSystemCamera: () -> Unit,
    onLaunchGallery: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .navigationBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = CircleShape,
                color = PureWhite.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.35f))
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_close_scanner),
                        tint = PureWhite
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = PureWhite
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        GlassCard(
            containerColor = GlassWhiteStrong,
            modifier = Modifier.widthIn(max = 480.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(EmeraldVibrant.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = null,
                        tint = MintGlow,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = if (cameraError != null) "Camera Unavailable" else stringResource(R.string.camera_permission_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = PureWhite,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = when {
                        cameraError != null -> cameraError
                        permissionDenied -> stringResource(R.string.camera_permission_denied_note)
                        else -> stringResource(R.string.camera_permission_rationale)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = PureWhite.copy(alpha = 0.90f),
                    textAlign = TextAlign.Center
                )

                if (cameraError != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MissingRedBg
                    ) {
                        Text(
                            text = "You can still capture using the System Camera app or pick from Gallery.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MissingRed,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                if (!hasPermission) {
                    Button(
                        onClick = onRequestPermission,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("grant_camera_permission_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintGlow,
                            contentColor = TealDeep
                        )
                    ) {
                        Text(stringResource(R.string.btn_grant_camera))
                    }

                    if (permissionDenied) {
                        OutlinedButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("open_camera_settings_button"),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.btn_open_settings), color = PureWhite)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onLaunchSystemCamera,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.45f))
                    ) {
                        Text(stringResource(R.string.btn_system_camera), color = PureWhite)
                    }
                    OutlinedButton(
                        onClick = onLaunchGallery,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.45f))
                    ) {
                        Text(stringResource(R.string.btn_gallery), color = PureWhite)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
