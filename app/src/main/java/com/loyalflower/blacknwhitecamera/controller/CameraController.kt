package com.loyalflower.blacknwhitecamera.controller

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorSpace
import android.graphics.ImageDecoder
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.loyalflower.blacknwhitecamera.model.CameraState
import com.loyalflower.blacknwhitecamera.util.ImageUtil
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService

/**
 * CameraX 라이브러리를 사용하여 카메라 기능을 제어하는 클래스입니다.
 * 카메라 설정, 미리보기, 사진 촬영, 이미지 처리 등의 기능을 제공합니다.
 *
 * @param context 애플리케이션 컨텍스트
 */
class CameraController(private val context: Context) {
    /**
     * 카메라의 현재 상태를 저장하는 데이터 클래스
     * @property isReady 카메라가 준비되었는지 여부
     * @property error 카메라에서 발생한 오류 메시지 (null이면 오류 없음)
     */
    private var cameraState = CameraState()

    /**
     * 카메라를 설정하고 Preview, ImageCapture, ImageAnalysis를 lifecycle에 바인딩합니다.
     *
     * @param lifecycleOwner lifecycle 바인딩에 사용될 LifecycleOwner
     * @param textureView 카메라 미리보기를 표시할 TextureView
     * @param executor 이미지 분석에 사용될 ExecutorService
     * @param onCameraReady 카메라가 준비되었을 때 호출될 콜백 함수. ImageCapture와 ImageAnalysis 객체를 인자로 받습니다.
     * @param onError 카메라 설정 중 오류가 발생했을 때 호출될 콜백 함수. 오류 메시지를 인자로 받습니다.
     */
    fun setupCamera(
        lifecycleOwner: LifecycleOwner,
        textureView: TextureView,
        executor: ExecutorService,
        onCameraReady: (ImageCapture, ImageAnalysis) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // 1. CameraProvider 요청
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            // 2. CameraProvider 사용 가능 여부 확인
            cameraProviderFuture.addListener({
                // 3. CameraProvider 가져오기
                val cameraProvider = cameraProviderFuture.get()

                // 4. 카메라 선택 (후면 카메라) , requireLensFacing 매개변수를 바꾸면 전면 카메라 LENS_FACING_FRONT
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                // 5. ImageCapture 설정 (지연 시간 최소화 모드)
                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                // 6. ImageAnalysis 설정 (가장 최근(마지막)의 프레임만 분석)
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .apply {
                        // 이미지 분석기 설정
                        setAnalyzer(executor) { image ->
                            try {
                                processImage(image, textureView) // 이미지 처리 함수 호출
                            } catch (e: Exception) {
                                onError("Image analysis failed: ${e.message}")
                            } finally {
                                image.close() // 이미지 프록시 닫기
                            }
                        }
                    }

                // 7. Preview 설정
                val preview = Preview.Builder().build()

                try {
                    // 8. 기존 바인딩 해제
                    cameraProvider.unbindAll()

                    // 9. 카메라를 lifecycle에 바인딩
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, // LifecycleOwner
                        cameraSelector, // 카메라 선택
                        preview, // 미리보기
                        imageCapture, // 이미지 캡처
                        imageAnalysis // 이미지 분석
                    )

                    // 10. 카메라 상태를 '준비됨' 으로 설정
                    cameraState = cameraState.copy(isReady = true)

                    // 11. 카메라 준비 완료 콜백 호출
                    onCameraReady(imageCapture, imageAnalysis)

                } catch (e: Exception) {
                    // 12. 바인딩 실패 시 오류 처리 / 상태 변경, 에러 로그
                    cameraState = cameraState.copy(error = e.message)
                    onError("Camera binding failed: ${e.message}")
                }
            }, ContextCompat.getMainExecutor(context))
        } catch (e: Exception) {
            // 13. 카메라 설정 실패 시 오류 처리 / 상태 변경, 에러 로그
            cameraState = cameraState.copy(error = e.message)
            onError("Camera setup failed: ${e.message}")
        }
    }

    /**
     * ImageProxy를 흑백 이미지로 변환하고 회전하여 TextureView에 표시합니다.
     *
     * 회전하는 이유! 카메라 센서에서 캡처한 이미지의 방향과 사용자가 화면을 보는 방향을 일치시켜,
     * 미리보기 이미지가 올바른 방향으로 표시되도록 하기 위함
     *
     * @param image 분석할 ImageProxy
     * @param textureView 흑백 이미지를 표시할 TextureView
     */
    private fun processImage(image: ImageProxy, textureView: TextureView) {
        // 1. ImageProxy에서 ByteBuffer 가져오기(ImageProxy - CameraX에서 이미지 데이터에 접근하기 위한 인터페이스)
        val buffer = image.planes[0].buffer

        // 2. ByteBuffer에서 ByteArray로 데이터 복사
        val data = ByteArray(buffer.remaining())
        buffer.get(data)

        // 3. ByteArrayOutputStream 생성 (try-with-resources 사용) -> 블록이 끝나면 out.close가 자동 호출(AutoCloseable)
        ByteArrayOutputStream().use { out ->
            // 4. YuvImage 생성 (NV21 포맷)
            val yuvImage = YuvImage(
                data,
                ImageFormat.NV21,
                image.width,
                image.height,
                null
            )

            // 5. YuvImage를 JPEG 형식으로 압축 (품질 50)
            yuvImage.compressToJpeg(
                Rect(0, 0, image.width, image.height),
                50,
                out
            )

            // 6. JPEG 데이터를 ByteArray로 변환
            val imageBytes = out.toByteArray()

            // 7. ByteArray를 Bitmap으로 디코딩
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

            // 8. Bitmap을 흑백으로 변환
            val grayScaleBitmap = ImageUtil.toGrayscale(bitmap)

            // 9. 흑백 Bitmap을 회전
            val rotatedBitmap =
                ImageUtil.rotateBitmap(grayScaleBitmap, image.imageInfo.rotationDegrees)

            // 10. TextureView의 SurfaceTexture에 Bitmap 그리기
            textureView.surfaceTexture?.let { texture ->
                // 10-1. SurfaceTexture의 버퍼 크기 설정
                texture.setDefaultBufferSize(rotatedBitmap.width, rotatedBitmap.height)
                // 10-2. Surface 생성
                val surface = Surface(texture)
                try {
                    // 10-3. Surface에서 Canvas 잠금
                    val canvas = surface.lockCanvas(null)
                    try {
                        // 10-4. Canvas에 Bitmap 그리기
                        canvas.drawBitmap(rotatedBitmap, 0f, 0f, null)
                    } finally {
                        // 10-5. Canvas 잠금 해제 및 갱신
                        surface.unlockCanvasAndPost(canvas)
                    }
                } finally {
                    // 10-6. Surface 해제
                    surface.release()
                }
            }

            // 11. Bitmap 리소스 재활용
            bitmap.recycle()
            grayScaleBitmap.recycle()
            rotatedBitmap.recycle()
        }
    }

    /**
     * 사진을 촬영하고 저장합니다.
     *
     * @param imageCapture ImageCapture 객체
     * @param executor 사진 촬영 작업에 사용될 ExecutorService
     * @param onImageCaptured 사진이 저장된 후 호출될 콜백 함수. 저장된 이미지의 Uri를 인자로 받습니다.
     * @param onError 사진 촬영 중 오류가 발생했을 때 호출될 콜백 함수. 오류 메시지를 인자로 받습니다.
     */
    fun takePhoto(
        imageCapture: ImageCapture?,
        executor: ExecutorService,
        onImageCaptured: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        // 1. 타임 스탬프 기반 파일 이름 생성
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())

        // 2. ContentValues 생성 (파일 정보)
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "Pictures/Black&WhiteCamera"
                ) // 원하는 폴더 지정
            }
        }

        // 3. OutputFileOptions 생성 (저장 위치 및 메타데이터 설정)
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        // 4. 사진 촬영 요청
        imageCapture?.takePicture(
            outputOptions, // 출력 옵션
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    // 5. 오류 처리
                    cameraState = cameraState.copy(error = exc.message)
                    onError("Photo capture failed: ${exc.message}")
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // 6. 저장된 이미지 Uri 가져오기
                    val savedUri = output.savedUri ?: Uri.EMPTY

                    // 7. Uri로부터 Bitmap 로드 (ImageDecoder 사용, ARGB_8888 설정)
                    val originalBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(context.contentResolver, savedUri)
                        ImageDecoder.decodeBitmap(source) { decoder, info, src ->
                            decoder.setTargetColorSpace(ColorSpace.get(ColorSpace.Named.SRGB))
                            decoder.allocator =
                                ImageDecoder.ALLOCATOR_SOFTWARE //강제로 Software Decoding
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, savedUri)
                    }

                    // 8. Bitmap을 흑백으로 변환
                    val grayscaleBitmap = ImageUtil.toGrayscale(originalBitmap)

                    // 9. 흑백 Bitmap을 Uri에 저장
                    saveBitmapToUri(context, grayscaleBitmap, savedUri)

                    // 10. 이미지 저장 완료 콜백 호출
                    onImageCaptured(savedUri)

                    // 11. Handler를 사용하여 UI 스레드에서 Toast 메시지 표시
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "사진이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    /**
     * Bitmap을 Uri에 저장합니다.
     *
     * @param context 애플리케이션 컨텍스트
     * @param bitmap 저장할 Bitmap
     * @param uri 저장할 Uri
     */
    private fun saveBitmapToUri(context: Context, bitmap: Bitmap, uri: Uri) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
        } catch (e: Exception) {
            Log.e("CameraX", "Error saving bitmap: ${e.message}")
        }
    }
}