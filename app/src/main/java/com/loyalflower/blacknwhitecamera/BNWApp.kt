package com.loyalflower.blacknwhitecamera

import android.app.Application
import com.loyalflower.blacknwhitecamera.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Koin을 사용한 의존성 주입 설정을 위한 Application 클래스입니다.
 * 이 클래스는 앱의 진입점으로 Koin 모듈을 로드하는 역할을 합니다.
 */
class BNWApp : Application() {

    /**
     * 애플리케이션이 생성될 때 호출됩니다.
     * Koin 의존성 주입을 설정하고 앱의 모듈들을 로드합니다.
     */
    override fun onCreate() {
        super.onCreate()

        // Koin을 시작하고 Android Context와 모듈을 설정합니다
        startKoin {
            // Android Context를 Koin에 제공하여 Android 관련 의존성들을 사용할 수 있게 합니다
            androidContext(this@BNWApp)
            // 의존성을 관리할 Koin 모듈들을 로드합니다
            modules(appModule)
        }
    }
}
