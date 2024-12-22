package com.loyalflower.blacknwhitecamera.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint

//이미지 처리 유틸
object ImageUtil {
    /**
     * Bitmap 이미지를 흑백 이미지로 변환합니다.
     *
     * @param bmpOriginal 원본 Bitmap 이미지
     * @return 흑백으로 변환된 Bitmap 이미지
     */
    fun toGrayscale(bmpOriginal: Bitmap): Bitmap {
        val width: Int = bmpOriginal.width
        val height: Int = bmpOriginal.height
        val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmpGrayscale) // 캔버스 생성
        val paint = Paint()
        val cm = ColorMatrix() // 컬러 매트릭스 생성
        cm.setSaturation(0f) // 채도를 0으로 설정하여 흑백으로 변환
        paint.colorFilter = ColorMatrixColorFilter(cm) // 페인트에 컬러 매트릭스 필터 적용
        c.drawBitmap(bmpOriginal, 0f, 0f, paint) // 캔버스에 원본 이미지 그리기 (흑백 필터 적용됨)
        return bmpGrayscale
    }
    /**
     * Bitmap 이미지를 주어진 각도만큼 회전합니다.
     *
     * @param bitmap 원본 Bitmap 이미지
     * @param degrees 회전할 각도 (시계 방향)
     * @return 회전된 Bitmap 이미지
     */
    fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}