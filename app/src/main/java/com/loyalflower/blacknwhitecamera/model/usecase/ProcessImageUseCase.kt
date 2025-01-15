package com.loyalflower.blacknwhitecamera.model.usecase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import com.loyalflower.blacknwhitecamera.util.ImageUtil
import java.io.ByteArrayOutputStream

/**
 * 이미지 처리 작업을 수행하는 UseCase 클래스입니다.
 * 카메라에서 캡처한 이미지 (ImageProxy)를 받아서 처리 후 Bitmap으로 반환합니다.
 *
 * @param context 이미지 처리에 필요한 Context
 */
class ProcessImageUseCase(private val context: Context) {

    /**
     * 이미지 처리 메서드.
     * ImageProxy에서 데이터를 추출하고, Bitmap으로 변환 후 필요한 처리(흑백 변환, 회전 등)를 수행합니다.
     *
     * @param image ImageProxy 객체 (카메라에서 캡처된 이미지)
     * @return 처리된 Bitmap 이미지
     */
    operator fun invoke(image: ImageProxy): Bitmap {
        // 1. ImageProxy에서 ByteBuffer 가져오기
        val buffer = image.planes[0].buffer

        // 2. ByteBuffer에서 ByteArray로 데이터 복사
        val data = ByteArray(buffer.remaining())
        buffer.get(data)

        // 3. ByteArrayOutputStream 생성 (try-with-resources 사용)
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
            val rotatedBitmap = ImageUtil.rotateBitmap(grayScaleBitmap, image.imageInfo.rotationDegrees)

            // 10. Bitmap 리소스 재활용 (메모리 관리)
            bitmap.recycle()
            grayScaleBitmap.recycle()

            return rotatedBitmap
        }
    }
}
