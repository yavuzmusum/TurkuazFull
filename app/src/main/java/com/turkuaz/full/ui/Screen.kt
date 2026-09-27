package com.turkuaz.full.ui

/**
 * Tam Surum'de Beta'daki gibi alt sekme yok - tek ana ekran (Sohbet),
 * digerlerine hamburger menuden (drawer) geciliyor.
 */
sealed class AppScreen(val title: String) {
    data object Chat : AppScreen("TURKUAZ AI")
    data object History : AppScreen("Geçmiş Sohbetler")
    data object Memory : AppScreen("Hafızam")
    data object Feedback : AppScreen("Geri Bildirim")
    data object About : AppScreen("Hakkımızda")
}
