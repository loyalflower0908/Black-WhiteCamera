# 📸 B&W Camera - 흑백 카메라 앱

## 🌟 소개  

**B&W Camera**는 실시간 카메라 미리보기에 흑백 필터를 적용하고, 흑백 사진을 촬영할 수 있는 Android 애플리케이션입니다.  
CameraX 라이브러리를 사용하여 최신 Android 기기와의 호환성을 극대화하고, Jetpack Compose로 간결하고 현대적인 UI를 구현했습니다.  

---

## ✨ 주요 기능  

✔️ **실시간 흑백 필터**:  
카메라 미리보기에 실시간으로 흑백 필터를 적용하여 촬영 전 결과물을 확인할 수 있습니다.  

✔️ **고품질 사진 촬영**:  
CameraX의 `ImageCapture`를 활용해 고해상도 흑백 사진을 촬영합니다.  

✔️ **Jetpack Compose UI**:  
선언형 UI 프레임워크 Jetpack Compose를 사용해 현대적이고 직관적인 인터페이스를 구성했습니다.  

---

## ⚙️ 기술 스택  

- **언어**: Kotlin 🛠️  
- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) 🎨  
- **카메라**: [CameraX](https://developer.android.com/training/camerax) 📸  
- **이미지 처리**: [Coil](https://coil-kt.github.io/coil/) 🖼️  
- **아키텍처**: MVC (Model-View-Controller) 🏗️  
- **비동기 처리**: Kotlin Coroutines ⏳  

---

## 🏗️ 아키텍처

본 애플리케이션은 MVC (Model-View-Controller) 패턴을 기반으로 설계되었습니다.


- **Model**: 카메라 (CameraX), 이미지 저장, 흑백 필터 적용 관련 비즈니스 로직을 담당합니다.
- **View**: Jetpack Compose로 구성된 UI 레이어입니다. 사용자 인터페이스를 표시하고 사용자 입력을 처리합니다.
- **Controller**: View와 Model 사이의 중재자 역할을 합니다. View의 요청을 받아 Model을 조작하고, Model의 변경 사항을 View에 반영합니다.
이러한 구조를 통해 코드의 관심사를 분리하고, 재사용성과 테스트 용이성을 높였습니다. 또한, 각 컴포넌트의 역할을 명확히 정의하여 코드의 가독성을 향상시켰습니다.

-> 현재 컨트롤러에 너무 많은 부담이 되어 있어서 비즈니스 로직을 Model로 분리하는 작업 필요

---

## 💭 느낀점  

📝 *이 부분은 곧 작성될 예정입니다!*  

--- 

## 🖼️ 스크린샷  

| <img src="path/to/screenshot1.jpg" width="200"> | <img src="path/to/screenshot2.jpg" width="200"> | <img src="path/to/screenshot3.jpg" width="200"> |  
| :----------------------------------------------: | :----------------------------------------------: | :----------------------------------------------: |  
|                 기본 카메라 화면                 |                  결과물 확인 화면                 |                    결과물 예시                    |  

---
