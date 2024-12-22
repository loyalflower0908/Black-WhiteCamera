package com.loyalflower.blacknwhitecamera.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CameraShutterButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 상호작용 소스를 생성합니다.
    val interactionSource = remember { MutableInteractionSource() }
    // 상호작용 소스에서 눌림 상태를 감지합니다.
    val isPressed by interactionSource.collectIsPressedAsState()

    // 눌렸을 때와 안 눌렸을 때의 안쪽 원 색상을 정의합니다.
    val innerCircleColor = if (isPressed) Color(0xFFCCCCCC) else Color(0xFFEEEEEE)

    Box(
        modifier = modifier
            .size(72.dp) // 바깥쪽 원 크기
            .clip(CircleShape) // 원형 모양으로 클리핑
            .background(Color.White) // 바깥쪽 원 색상
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true, color = Color.Gray), // Ripple 효과(UI 요소를 터치했을 때 물결처럼 퍼져나가는 시각적 피드백)
                onClick = onClick
            ),
        contentAlignment = Alignment.Center // 안쪽 원을 중앙에 배치
    ) {
        Box(
            modifier = Modifier
                .size(60.dp) // 안쪽 원 크기
                .clip(CircleShape) // 원형 모양으로 클리핑
                .background(innerCircleColor) // 안쪽 원 색상
        )
    }
}