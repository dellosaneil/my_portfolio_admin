package com.example.portfolioadmin.project

data class ProjectData(
    val projectTitle: String,
    val projectDescription: String,
    val projectLanguage: String,
    val gitHubRepository: String,
    val firstImageReference: String,
    val secondImageReference: String,
    val thirdImageReference: String,
    val timeUploaded : Long = System.currentTimeMillis()
)