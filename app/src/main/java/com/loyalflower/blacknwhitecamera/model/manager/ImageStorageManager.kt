package com.loyalflower.blacknwhitecamera.model.manager

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ColorSpace
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 이미지 저장 관련 작업을 관리하는 클래스입니다.
 * 이미지 파일을 저장하고, URI로부터 이미지를 로드하는 기능을 제공합니다.
 *
 * @param context 이미지 저장과 관련된 작업을 수행하기 위한 Context
 */
class ImageStorageManager(private val context: Context) {

    /**
     * 지정된 폴더에 사진을 저장하기 위한 OutputFileOptions를 생성합니다.
     *
     * @param folderName 저장할 이미지가 위치할 폴더명. 기본값은 "Black&WhiteCamera".
     * @return 생성된 OutputFileOptions 객체
     */
    fun createOutputOptions(folderName: String = "Black&WhiteCamera"): ImageCapture.OutputFileOptions {
        // 파일 이름을 현재 시간으로 설정 (밀리초 단위로)
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())

        // 저장할 이미지의 메타데이터를 설정
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.jpg")  // 이미지 파일 이름
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")    // MIME 타입
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                // 안드로이드 10 이상에서는 상대 경로로 저장
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$folderName")
            }
        }

        // OutputFileOptions 생성
        return ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()
    }

    /**
     * 주어진 URI로부터 Bitmap을 로드하여 반환합니다.
     * 안드로이드 버전에 따라 다른 방식으로 이미지를 디코딩합니다.
     *
     * @param uri 이미지의 URI
     * @return 로드된 Bitmap 이미지
     */
    fun loadBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 안드로이드 P 이상에서는 ImageDecoder를 사용하여 이미지 디코딩
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.setTargetColorSpace(ColorSpace.get(ColorSpace.Named.SRGB))
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            // 안드로이드 P 이하에서는 MediaStore의 getBitmap 메서드를 사용
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }
}
