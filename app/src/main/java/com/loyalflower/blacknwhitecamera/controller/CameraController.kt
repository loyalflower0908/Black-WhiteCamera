package com.loyalflower.blacknwhitecamera.controller

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import com.loyalflower.blacknwhitecamera.model.CameraState
import com.loyalflower.blacknwhitecamera.model.manager.CameraManager
import com.loyalflower.blacknwhitecamera.model.manager.ImageStorageManager
import com.loyalflower.blacknwhitecamera.model.usecase.ProcessImageUseCase
import com.loyalflower.blacknwhitecamera.model.usecase.SavePhotoUseCase
import com.loyalflower.blacknwhitecamera.view.util.TextureViewUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
/**
 * 카메라를 설정하고, 이미지 캡처 및 저장을 담당하는 컨트롤러 클래스입니다.
 * 카메라 초기화, 이미지 분석, 사진 촬영 및 저장 기능을 제공합니다.
 *
 * @param context 애플리케이션의 Context
 * @param processImageUseCase 이미지 처리 관련 UseCase
 * @param savePhotoUseCase 이미지 저장 관련 UseCase
 * @param cameraManager 카메라 초기화 및 설정을 담당하는 Manager
 * @param storageManager 이미지 저장 및 로드 작업을 담당하는 Manager
 */
class CameraController(
    private val context: Context,
    private val processImageUseCase: ProcessImageUseCase,
    private val savePhotoUseCase: SavePhotoUseCase,
    private val cameraManager: CameraManager,
    private val storageManager: ImageStorageManager
) {

    // 카메라 상태를 추적하는 데이터 클래스
    private var cameraState = CameraState()

    /**
     * 카메라를 초기화하고 설정을 완료하는 메서드입니다.
     * 카메라 미리보기, 이미지 캡처 및 분석을 위한 바인딩을 처리합니다.
     *
     * @param lifecycleOwner 카메라 생명 주기를 관리할 LifecycleOwner
     * @param textureView 카메라 미리보기를 표시할 TextureView
     * @param executor 카메라 이미지 분석을 위한 Executor
     * @param onCameraReady 카메라 준비 완료 후 호출될 콜백 함수
     * @param onError 오류 발생 시 호출될 콜백 함수
     */
    fun setupCamera(
        lifecycleOwner: LifecycleOwner,
        textureView: TextureView,
        executor: ExecutorService,
        onCameraReady: (ImageCapture, ImageAnalysis) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // 카메라 초기화
                    cameraManager.initializeCamera()

                    // 카메라 선택, 이미지 캡처 및 분석 설정
                    val cameraSelector = cameraManager.getCameraSelector()
                    val imageCapture = cameraManager.createImageCapture()
                    val imageAnalysis = cameraManager.createImageAnalysis().apply {
                        setAnalyzer(executor) { image ->
                            // 이미지 분석 후 처리
                            processImage(image, textureView, onError)
                        }
                    }
                    val preview = Preview.Builder().build()

                    // 카메라 생명주기에 바인딩
                    cameraManager.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture,
                        imageAnalysis
                    )

                    // 카메라 준비 완료 상태로 업데이트
                    cameraState = cameraState.copy(isReady = true)

                    // 카메라 준비 완료 콜백 호출
                    onCameraReady(imageCapture, imageAnalysis)
                } catch (e: Exception) {
                    // 초기화 실패 시 오류 처리
                    cameraState = cameraState.copy(error = e.message)
                    onError("Camera setup failed: ${e.message}")
                }
            }
        } catch (e: Exception) {
            // 전체 카메라 설정 실패 시 오류 처리
            cameraState = cameraState.copy(error = e.message)
            onError("Camera setup failed: ${e.message}")
        }
    }

    /**
     * 이미지 분석 후 결과를 화면에 표시하는 메서드입니다.
     *
     * @param image 분석할 ImageProxy 객체
     * @param textureView 결과를 표시할 TextureView
     * @param onError 오류 발생 시 호출될 콜백 함수
     */
    private fun processImage(
        image: ImageProxy,
        textureView: TextureView,
        onError: (String) -> Unit
    ) {
        try {
            // 이미지 처리 및 비트맵 생성
            val processedBitmap = processImageUseCase(image)
            // 결과 이미지를 TextureView에 표시
            TextureViewUtil.drawBitmapToTextureView(processedBitmap, textureView)
        } catch (e: Exception) {
            // 이미지 분석 실패 시 오류 처리
            onError("Image analysis failed: ${e.message}")
        } finally {
            // ImageProxy 리소스 해제
            image.close()
        }
    }

    /**
     * 사진 촬영을 수행하고, 촬영된 사진을 저장하는 메서드입니다.
     *
     * @param imageCapture 이미지 캡처 객체
     * @param executor 이미지 캡처를 위한 Executor
     * @param onImageCaptured 사진이 저장된 후 호출될 콜백 함수
     * @param onError 오류 발생 시 호출될 콜백 함수
     */
    fun takePhoto(
        imageCapture: ImageCapture?,
        executor: ExecutorService,
        onImageCaptured: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        // 사진 저장을 위한 출력 옵션 생성
        val outputOptions = storageManager.createOutputOptions()

        imageCapture?.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    // 사진 촬영 실패 시 오류 처리
                    cameraState = cameraState.copy(error = exc.message)
                    onError("Photo capture failed: ${exc.message}")
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // 촬영된 사진 저장 URI
                    val savedUri = output.savedUri ?: Uri.EMPTY
                    // 저장된 이미지 로드 후 처리
                    val originalBitmap = storageManager.loadBitmapFromUri(savedUri)
                    savePhotoUseCase(originalBitmap, savedUri)
                    onImageCaptured(savedUri)
                    // 사진 저장 완료 메시지 표시
                    showSavedMessage()
                }
            }
        )
    }

    /**
     * 사진이 저장된 후 사용자에게 메시지를 표시하는 메서드입니다.
     */
    private fun showSavedMessage() {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "사진이 저장되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
