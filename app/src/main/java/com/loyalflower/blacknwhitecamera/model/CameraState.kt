package com.loyalflower.blacknwhitecamera.model

// 카메라 상태를 위한 데이터 클래스
data class CameraState(
    val isReady: Boolean = false,
    val error: String? = null
)