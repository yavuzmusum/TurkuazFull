package com.turkuaz.full.data

import com.turkuaz.full.BuildConfig

/**
 * Sunucu adresi artik build tipine gore DINAMIK (bkz. app/build.gradle.kts
 * buildTypes -> buildConfigField):
 *   - debug   -> http://10.0.2.2:8000 (emulator'den host'un localhost'u)
 *   - release -> https://turkuaz-core2-production.up.railway.app
 * Fiziksel cihazda debug APK ile test ederken 10.0.2.2 CALISMAZ (o sadece
 * emulator icin gecerli bir takma ad) - bilgisayarinizin LAN IP'siyle
 * degistirmeniz gerekir (build.gradle.kts'teki debug blogundan).
 */
object Config {
    val BASE_URL: String = BuildConfig.BASE_URL
    const val APP_VERSION: String = "1.0.0"
    const val PACKAGE_NAME: String = "com.turkuaz.full"
}
