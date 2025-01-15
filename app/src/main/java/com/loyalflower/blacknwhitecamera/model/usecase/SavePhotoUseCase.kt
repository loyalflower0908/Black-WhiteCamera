package com.loyalflower.blacknwhitecamera.model.usecase

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.loyalflower.blacknwhitecamera.util.ImageUtil

/**
 * 이미지를 저장하는 UseCase 클래스입니다.
 * 주어진 Bitmap을 흑백으로 변환한 후, 지정된 URI에 저장합니다.
 *
 * @param context 이미지 저장 작업을 수행하기 위한 Context
 */
class SavePhotoUseCase(private val context: Context) {

    /**
     * 주어진 Bitmap을 흑백으로 변환한 후, 지정된 URI에 JPEG 형식으로 저장합니다.
     *
     * @param bitmap 저장할 Bitmap 이미지
     * @param uri 이미지를 저장할 URI
     */
    operator fun invoke(bitmap: Bitmap, uri: Uri) {
        try {
            // 1. Bitmap을 흑백으로 변환
            val grayscaleBitmap = ImageUtil.toGrayscale(bitmap)

            // 2. 지정된 URI에 OutputStream을 열어 이미지 저장
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                // 3. 흑백 이미지를 JPEG 형식으로 압축하여 저장 (품질 100)
                grayscaleBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
        } catch (e: Exception) {
            // 4. 오류 발생 시 로그에 오류 메시지 출력
            Log.e("SavePhotoUseCase", "Error saving bitmap: ${e.message}")
        }
    }
}
