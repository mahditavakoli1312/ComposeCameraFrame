package com.example.composecameraframe

import android.content.ContentValues
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import java.util.Locale
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        setContent {
            CameraPermissionHandler()
        }
    }
}

@Composable
fun JawImageCapture() {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraProvider = cameraProviderFuture.get()
    val previewView = remember { PreviewView(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    // screen width and height details
    val displayMetrics = context.resources.displayMetrics
    val screenWidthPx = displayMetrics.widthPixels
    val screenHeightPx = displayMetrics.heightPixels
    val density = LocalDensity.current


    val preview = remember { Preview.Builder().build() }

    LaunchedEffect(cameraProvider) {
        imageCapture = ImageCapture.Builder().build()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        cameraProvider.bindToLifecycle(
            context as ComponentActivity,
            cameraSelector,
            preview,
            imageCapture
        )
        preview.setSurfaceProvider(previewView.surfaceProvider)

    }

    var point1 by remember {
        mutableStateOf(
            Offset(
                screenWidthPx.toFloat() * 1 / 10,
                screenHeightPx.toFloat() * 1 / 5
            )
        )
    }
    var point2 by remember {
        mutableStateOf(
            Offset(
                screenWidthPx.toFloat() * 5 / 10,
                screenHeightPx.toFloat() * 4 / 5
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay with draggable points
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        val position = change.position
                        if (position.getDistance(point1) < 50f) {
                            point1 += dragAmount
                        } else if (position.getDistance(point2) < 50f) {
                            point2 += dragAmount
                        }
                    }
                }
        ) {
            drawCircle(Color.Red, radius = 20f, center = point1)
            drawCircle(Color.Blue, radius = 20f, center = point2)
            drawLine(Color.Green, start = point1, end = point2, strokeWidth = 5f)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter)
        ) {

            // Guide Text at the Bottom
            Text(
                text = "Red circle is 6th tooth.\nBlue circle is 1st tooth.",
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,

                )


            // Take Image Button
            Button(
                onClick = {
                    val imageCapture = imageCapture ?: return@Button
                    val contentResolver = context.contentResolver

                    // Create time-stamped name and MediaStore entry.
                    val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                        .format(System.currentTimeMillis())
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/JawImages")
                    }

                    val outputOptions = ImageCapture.OutputFileOptions
                        .Builder(
                            contentResolver,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            contentValues
                        )
                        .build()

                    imageCapture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                val savedUri = outputFileResults.savedUri
                                Toast.makeText(
                                    context,
                                    "Image saved: $savedUri",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Toast.makeText(context, "Failed to save image.", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Take Image")
            }
        }
    }
}

private fun Offset.getDistance(other: Offset): Float {
    return sqrt((this.x - other.x) * (this.x - other.x) + (this.y - other.y) * (this.y - other.y))
}

@Composable
fun CameraPermissionHandler() {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(false) }

    // Request permission
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            permissionGranted = isGranted
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            permissionGranted = true
        } else {
            launcher.launch(android.Manifest.permission.CAMERA)
        }
    }

    if (permissionGranted) {
        JawImageCapture()
    } else {
        // Display a message if permission is not granted
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Camera permission is required to use this app.",
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
