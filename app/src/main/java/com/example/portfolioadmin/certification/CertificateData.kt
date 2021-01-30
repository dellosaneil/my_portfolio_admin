package com.example.portfolioadmin.certification

data class CertificateData(
    val certificateTitle: String = "",
    val companyName: String = "",
    val credentialId: String = "",
    val credentialUrl: String = "",
    val timeUploaded : Long = System.currentTimeMillis()
)

