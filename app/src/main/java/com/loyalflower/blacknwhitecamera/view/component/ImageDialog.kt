package com.loyalflower.blacknwhitecamera.view.component

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage

/**
 * 이미지를 전체 화면으로 표시하는 다이얼로그 Composable 함수입니다.
 *
 * @param imageUrl 표시할 이미지의 Uri
 * @param onDismiss 다이얼로그를 닫을 때 호출될 콜백 함수
 */
@Composable
fun ImageDialog(
    imageUrl: Uri,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss, // 사용자가 다이얼로그 바깥을 클릭하거나 뒤로 가기 버튼을 누를 때 onDismiss 콜백 호출
        //기본 플랫폼 크기 사용 X (전체 화면 사용)
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize() // 화면 전체를 채우도록 설정
                .background(Color.Black) // 배경색을 검은색으로 설정
        ) {
            // Coil 라이브러리의 AsyncImage를 사용하여 이미지 표시
            AsyncImage(
                model = imageUrl, // 표시할 이미지 Uri
                contentDescription = "Captured Image",
                modifier = Modifier.fillMaxSize() // 이미지를 Box 내에서 전체 크기로 표시
            )
            Icon(
                imageVector = Icons.Filled.Close, // 닫기 아이콘 사용
                contentDescription = "Close",
                tint = Color.White, // 아이콘 색상을 흰색으로 설정
                modifier = Modifier
                    .align(Alignment.TopEnd) // Box 내에서 우측 상단에 배치
                    .padding(16.dp) // 16dp 패딩 추가
                    .size(24.dp) // 아이콘 크기를 24dp로 설정
                    .clickable { onDismiss() } // 클릭 시 onDismiss 콜백 호출
            )
        }
    }
}