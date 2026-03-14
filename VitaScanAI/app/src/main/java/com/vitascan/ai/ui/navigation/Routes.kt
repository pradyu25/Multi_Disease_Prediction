package com.vitascan.ai.ui.navigation

object Routes {
    const val LOGIN           = "login"
    const val SIGNUP          = "signup"
    const val DASHBOARD       = "dashboard"
    const val UPLOAD          = "upload"
    const val REPORT_VIEW     = "report_view/{reportId}"
    const val ANALYTICS       = "analytics"
    const val RECOMMENDATIONS = "recommendations/{reportId}"

    fun reportView(reportId: String)         = "report_view/$reportId"
    fun recommendations(reportId: String)    = "recommendations/$reportId"
}
