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

- **언어**: Kotlin 
- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) 
- **카메라**: [CameraX](https://developer.android.com/training/camerax) 
- **이미지 처리**: [Coil](https://coil-kt.github.io/coil/) 
- **아키텍처**: MVC (Model-View-Controller) 
- **비동기 처리**: [Kotlin Coroutines](https://developer.android.com/kotlin/coroutines)
- **의존성 주입**: [Koin](https://insert-koin.io/docs/reference/koin-compose/compose)

---

## 🏗️ 아키텍처

본 애플리케이션은 MVC (Model-View-Controller) 패턴을 기반으로 설계되었습니다.


- **Model**: 카메라 (CameraX), 이미지 저장, 흑백 필터 적용 관련 비즈니스 로직을 담당합니다.
- **View**: Jetpack Compose로 구성된 UI 레이어입니다. 사용자 인터페이스를 표시하고 사용자 입력을 처리합니다.
- **Controller**: View와 Model 사이의 중재자 역할을 합니다. View의 요청을 받아 Model을 조작하고, Model의 변경 사항을 View에 반영합니다.
이러한 구조를 통해 코드의 관심사를 분리하고, 재사용성과 테스트 용이성을 높였습니다. 또한, 각 컴포넌트의 역할을 명확히 정의하여 코드의 가독성을 향상시켰습니다.

~~-> 현재 컨트롤러에 너무 많은 부담이 되어 있어서 비즈니스 로직을 Model로 분리하는 작업 필요~~

-> V 1.1 Koin을 통한 의존성 주입으로 해결

---

## 💭 느낀점  

MVC 패턴으로 개발을 해보면서 처음엔 더 구조가 간단해서 쉬울줄 알았다.

우선 내가 아는 짤막한 지식대로 MVC 구조를 구현해서 만들어보고,

다른 앱도 MVP 패턴으로 구현해보고 다시 돌아오니까 내가 구현한 MVC패턴이 잘못됐다는 것을 알았다.

Controller에 비즈니스 로직도 많이 포함되어있고 View와 관련한 로직을 처리하는 부분까지 포함되어있었다.

지금 돌아보면 MVVM 패턴을 사용할때는 구조가 명확하다 보니까 지식이 적었을 때 코딩했어도 역할 분리가 잘 되었다는 것을 느낀다.

MVP 패턴을 사용할 때는 좀 더 내가 역할 분리를 신경써야 한다는 느낌을 받았다. 덕분에 책임 분산에 대한 느낌이 조금 늘었다.

MVC 패턴을 사용할 때는 역할 분리가 힘들었다.

실제로 MVC패턴이 Controller가 과도한 책임을 지기 쉽고 역할을 분리하기도 어렵다는 것을 알았다.

수정을 통해 최대한 MVC패턴을 지키며 역할 분리를 해보았지만 이게 완벽한 분리는 아니라는 것이 나의 생각이다.

아쉬운 점: 카메라 프리뷰의 화질과 색감이 결과물과 다르다. 카메라 프리뷰에서 흑백으로 보여지게 하는 이미지 처리에서 왜곡이 많이 일어나서 그런것 같다(+풀 스크린 카메라 프리뷰).. 

반대로 결과물은 잘 나온다.

--- 

## 🖼️ 스크린샷  

| <img src="https://github.com/loyalflower0908/Black-WhiteCamera/blob/master/B-W-C%20Screenshot/B-W-C%20Screenshot1.png" width="200"> | <img src="https://github.com/loyalflower0908/Black-WhiteCamera/blob/master/B-W-C%20Screenshot/B-W-C%20Screenshot2.png" width="200"> | <img src="https://github.com/loyalflower0908/Black-WhiteCamera/blob/master/B-W-C%20Screenshot/B-W-C%20Result.jpg" width="200"> |  
| :----------------------------------------------: | :----------------------------------------------: | :----------------------------------------------: |  
|                 기본 카메라 화면                 |                  결과물 확인 화면                 |                    결과물 예시                    |  

---

_____________________________________________________
### 🕐 개발 기간 🕐
2024.12 (3일)

~~V 1.1수정 2025.  01. 15 : Koin을 통한 책임 분리~~


V 1.2수정 2025.  01. 15 : Controller에서 UI 렌더링 로직 분리
