package com.loyalflower.blacknwhitecamera.view.util

import android.graphics.Bitmap
import android.view.Surface
import android.view.TextureView

object TextureViewUtil {
    /**
     * 비트맵 이미지를 TextureView에 그리는 메서드입니다.
     *
     * @param bitmap 그릴 비트맵 이미지
     * @param textureView 이미지를 그릴 TextureView
     */
    fun drawBitmapToTextureView(bitmap: Bitmap, textureView: TextureView) {
        textureView.surfaceTexture?.let { texture ->
            texture.setDefaultBufferSize(bitmap.width, bitmap.height)
            val surface = Surface(texture)
            try {
                val canvas = surface.lockCanvas(null)
                try {
                    // 비트맵을 캔버스에 그리기
                    canvas.drawBitmap(bitmap, 0f, 0f, null)
                } finally {
                    surface.unlockCanvasAndPost(canvas)
                }
            } finally {
                // Surface 리소스 해제
                surface.release()
            }
        }
    }
}