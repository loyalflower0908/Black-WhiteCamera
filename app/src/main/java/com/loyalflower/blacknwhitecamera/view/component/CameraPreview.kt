package com.loyalflower.blacknwhitecamera.view.component

import android.annotation.SuppressLint
import android.net.Uri
import androidx.camera.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import java.util.concurrent.ExecutorService
import android.graphics.SurfaceTexture
import android.view.TextureView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.loyalflower.blacknwhitecamera.controller.CameraController
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

/**
 * 카메라 미리보기와 사진 촬영 기능을 제공하는 Composable 함수입니다.
 *
 * @param modifier Composable에 적용할 Modifier
 * @param executor 카메라 작업에 사용될 ExecutorService
 * @param onImageCaptured 사진이 촬영된 후 호출될 콜백 함수. 저장된 이미지의 Uri를 인자로 받습니다.
 * @param onError 카메라 설정 또는 사진 촬영 중 오류가 발생했을 때 호출될 콜백 함수. 오류 메시지를 인자로 받습니다.
 */
@SuppressLint("RestrictedApi")
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    executor: ExecutorService,
    onImageCaptured: (Uri) -> Unit,
    onError: (String) -> Unit
) {
    // 현재 Context를 가져옵니다.
    val context = LocalContext.current

    // 현재 LifecycleOwner를 가져옵니다.
    val lifecycleOwner = LocalLifecycleOwner.current

    // 카메라 준비 상태를 저장하는 상태 변수
    var isCameraReady by remember { mutableStateOf(false) }

    // ImageCapture 객체를 저장하는 상태 변수
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    // ImageAnalysis 객체를 저장하는 상태 변수
    var imageAnalysis by remember { mutableStateOf<ImageAnalysis?>(null) }

    // TextureView를 저장하는 상태 변수
    var textureView by remember { mutableStateOf<TextureView?>(null) }

    // CameraController 객체 생성 - Koin으로부터 가져옴
    val controller = koinInject<CameraController> { parametersOf(context) }

    // DisposableEffect를 사용하여 리소스 해제
    DisposableEffect(Unit) {
        onDispose {
            // TextureView의 SurfaceTexture 리스너 해제
            textureView?.surfaceTexture?.release()
            // ImageAnalysis의 분석기 해제
            imageAnalysis?.clearAnalyzer()
        }
    }

    // Box 레이아웃을 사용하여 CameraPreview와 CameraShutterButton을 겹쳐서 배치
    Box(modifier = modifier) {
        // AndroidView를 사용하여 TextureView를 Compose에 통합
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                TextureView(ctx).apply {
                    // TextureView를 변수에 저장
                    textureView = this
                    // TextureView의 SurfaceTextureListener 설정
                    setupTextureViewListener(this)
                    // CameraController를 사용하여 카메라 설정
                    controller.setupCamera(
                        lifecycleOwner = lifecycleOwner,
                        textureView = this,
                        executor = executor,
                        onCameraReady = { capture, analysis ->
                            // 카메라가 준비되면 ImageCapture와 ImageAnalysis 객체를 저장하고, 카메라 준비 상태를 true로 설정
                            imageCapture = capture
                            imageAnalysis = analysis
                            isCameraReady = true
                        },
                        onError = onError
                    )
                }
            }
        )

        // 카메라가 준비된 경우에만 CameraShutterButton 표시
        if (isCameraReady) {
            CameraShutterButton(
                onClick = {
                    // 사진 촬영 함수 호출
                    controller.takePhoto(
                        imageCapture = imageCapture,
                        executor = executor,
                        onImageCaptured = onImageCaptured,
                        onError = onError
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter) // 화면 하단 중앙에 배치
                    .padding(bottom = 64.dp) // 하단에 64dp 패딩 추가
            )
        }
    }
}

/**
 * TextureView에 SurfaceTextureListener를 설정하는 함수입니다.
 *
 * @param textureView SurfaceTextureListener를 설정할 TextureView
 */
private fun setupTextureViewListener(textureView: TextureView) {
    textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            // SurfaceTexture가 사용 가능할 때 호출됩니다.
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            // TextureView의 크기가 변경되었을 때 호출됩니다.
            // 미리보기 화면 비율을 조정하는 등의 작업을 수행할 수 있습니다.
            val previewRatio = 16f / 9f // 예시 비율 (원하는 비율로 수정)
            val screenRatio = width.toFloat() / height.toFloat()
            // 화면 비율에 맞춰 TextureView의 크기를 조정합니다.
            if (screenRatio > previewRatio) {
                textureView.layoutParams.height = (width / previewRatio).toInt()
            } else {
                textureView.layoutParams.width = (height * previewRatio).toInt()
            }
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            // SurfaceTexture가 소멸되었을 때 호출됩니다.
            return true
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            // SurfaceTexture가 업데이트되었을 때 호출됩니다.
        }
    }
}