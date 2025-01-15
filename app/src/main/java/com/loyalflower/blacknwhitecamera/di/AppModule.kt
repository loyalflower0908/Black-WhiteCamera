package com.loyalflower.blacknwhitecamera.di

import android.content.Context
import com.loyalflower.blacknwhitecamera.controller.CameraController
import com.loyalflower.blacknwhitecamera.model.manager.CameraManager
import com.loyalflower.blacknwhitecamera.model.manager.ImageStorageManager
import com.loyalflower.blacknwhitecamera.model.usecase.ProcessImageUseCase
import com.loyalflower.blacknwhitecamera.model.usecase.SavePhotoUseCase
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

/**
 * 앱의 종속성을 정의하는 Koin 모듈입니다.
 * 싱글톤 객체와 팩토리 객체를 정의하여 의존성 주입을 관리합니다.
 */
val appModule = module {
    /**
     * 사진 처리와 저장 관련 UseCase들을 싱글톤으로 정의합니다.
     * Context를 사용하여 초기화됩니다.
     */
    single { (context: Context) -> ProcessImageUseCase(context) } // 이미지 처리 유스케이스
    single { (context: Context) -> SavePhotoUseCase(context) }   // 사진 저장 유스케이스

    /**
     * 카메라와 관련된 매니저 및 저장소 관리 객체를 싱글톤으로 정의합니다.
     */
    single { CameraManager(get()) }    // 카메라 제어 매니저
    single { ImageStorageManager(get()) } // 이미지 저장소 관리 매니저

    /**
     * CameraController를 팩토리로 정의합니다.
     * CameraController는 Context와 다양한 유스케이스 및 매니저를 의존성으로 사용합니다.
     *
     * @param context 컨트롤러 초기화에 사용되는 Context
     */
    factory { (context: Context) ->
        CameraController(
            context = context,
            processImageUseCase = get { parametersOf(context) },
            savePhotoUseCase = get { parametersOf(context) },
            cameraManager = get(),
            storageManager = get()
        )
    }
}
