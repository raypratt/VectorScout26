package com.example.vectorscout26.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object MatchScouting : Screen("match_scouting")
    object ActionDetail : Screen("action_detail/{phase}/{actionType}") {
        fun createRoute(phase: String, actionType: String) = "action_detail/$phase/$actionType"
    }
    object QRCode : Screen("qr_code/{matchScoutId}") {
        fun createRoute(matchScoutId: Long) = "qr_code/$matchScoutId"
    }
    object PitScouting : Screen("pit_scouting")
    object PitQRCode : Screen("pit_qr_code/{pitScoutId}") {
        fun createRoute(pitScoutId: Long) = "pit_qr_code/$pitScoutId"
    }
}
