package com.example.canhbao.data.model.info.camera

data class CameraMapDto(
    val id: Long,
    val tenCamera: String,
    val kinhDo: Double,
    val viDo: Double,
    val anhCamera: String? = null,
    val videoUrl: String? = null,
    val moTa: String? = null
)