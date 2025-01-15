package com.loyalflower.blacknwhitecamera.model.manager

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * 카메라 기능을 관리하는 클래스입니다.
 * 카메라 초기화, 설정 생성, 라이프사이클 바인딩 등 주요 카메라 관련 작업을 제공합니다.
 *
 * @param context 카메라 사용에 필요한 Context
 */
class CameraManager(private val context: Context) {
    // ProcessCameraProvider 인스턴스, 카메라 관리를 담당합니다.
    private var cameraProvider: ProcessCameraProvider? = null

    /**
     * 카메라를 초기화하고 ProcessCameraProvider를 반환합니다.
     *
     * @return 초기화된 ProcessCameraProvider
     * @throws Exception 초기화 중 오류가 발생한 경우
     */
    suspend fun initializeCamera(): ProcessCameraProvider {
        return suspendCoroutine { continuation ->
            // CameraProvider 요청
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    // CameraProvider 가져오기
                    val provider = cameraProviderFuture.get()
                    cameraProvider = provider
                    continuation.resume(provider)
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    /**
     * 지정된 카메라 렌즈를 선택하는 CameraSelector를 생성합니다.
     *
     * @param isFrontCamera true면 전면 카메라, false면 후면 카메라를 선택합니다.
     * @return 설정된 CameraSelector
     */
    fun getCameraSelector(isFrontCamera: Boolean = false): CameraSelector {
        return CameraSelector.Builder()
            .requireLensFacing(
                if (isFrontCamera) CameraSelector.LENS_FACING_FRONT
                else CameraSelector.LENS_FACING_BACK
            )
            .build()
    }

    /**
     * 사진 촬영 기능을 제공하는 ImageCapture 객체를 생성합니다.
     *
     * @return 설정된 ImageCapture 객체
     */
    fun createImageCapture(): ImageCapture {
        return ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    /**
     * 이미지 분석 기능을 제공하는 ImageAnalysis 객체를 생성합니다.
     *
     * @return 설정된 ImageAnalysis 객체
     */
    fun createImageAnalysis(): ImageAnalysis {
        return ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }

    /**
     * 라이프사이클에 카메라를 바인딩합니다.
     * 기존의 바인딩된 모든 카메라는 해제됩니다.
     *
     * @param lifecycleOwner 라이프사이클 소유자
     * @param cameraSelector 사용할 카메라 렌즈 선택
     * @param preview 미리보기 설정
     * @param imageCapture 사진 촬영 설정
     * @param imageAnalysis 이미지 분석 설정
     */
    fun bindToLifecycle(
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector,
        preview: Preview,
        imageCapture: ImageCapture,
        imageAnalysis: ImageAnalysis
    ) {
        // 기존 바인딩 해제
        cameraProvider?.unbindAll()
        // 카메라를 lifecycle에 바인딩
        cameraProvider?.bindToLifecycle(
            lifecycleOwner, // LifecycleOwner
            cameraSelector, // 카메라 선택
            preview,        // 미리보기 설정
            imageCapture,   // 사진 촬영 설정
            imageAnalysis   // 이미지 분석 설정
        )
    }
}
