package com.loyalflower.blacknwhitecamera.view.screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.loyalflower.blacknwhitecamera.view.component.CameraPreview
import com.loyalflower.blacknwhitecamera.view.component.ImageDialog
import java.util.concurrent.Executors

/**
 * 카메라 화면을 구성하는 Composable 함수입니다.
 * 카메라 권한을 확인하고, 권한이 있는 경우 CameraPreview 를 화면에 보여줍니다.
 * 사진 촬영 후 ImageDialog를 통해 촬영된 이미지를 전체화면으로 표시합니다.
 */
@Composable
fun CameraScreen() {
    // 촬영된 이미지의 Uri를 저장하는 상태 변수, imageUri가 null이면 사진이 없는 것으로 간주
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // 이미지 다이얼로그 표시 여부 상태 변수
    var showImageDialog by remember { mutableStateOf(false) }

    // 현재 Context를 가져옵니다.
    val context = LocalContext.current

    // 카메라 권한 보유 여부를 저장하는 상태 변수, 앱 시작 시 카메라 권한을 확인하고 hasCameraPermission에 저장
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // 권한 요청 런처, ActivityResultContracts.RequestPermission()를 사용하여 권한을 요청하고 onResult 콜백에서 결과를 처리
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    // 앱이 시작될 때 카메라 권한을 요청합니다.
    LaunchedEffect(key1 = true) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    // 카메라 관련 작업을 백그라운드 스레드에서 실행하기 위한 ExecutorService 생성
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // onDispose는 컴포지션이 종료될 때 실행됩니다.(카메라 ExecutorService 종료)
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    // 카메라 권한이 있는 경우
    if (hasCameraPermission) {
        // 카메라 미리보기 화면(촬영 버튼 포함)
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            executor = cameraExecutor,
            onImageCaptured = { uri ->
                imageUri = uri
                showImageDialog = true
            },
            onError = { errorMessage ->
                Log.e("CameraPreview", errorMessage)
            }
        )
    }
    // imageUri가 null이 아닐 때만 ImageDialog를 표시
    if (imageUri != null && showImageDialog) {
        ImageDialog(
            imageUrl = imageUri!!,
            onDismiss = {
                showImageDialog = false
                imageUri = null // 이미지 Uri 초기화
            }
        )
    }
}