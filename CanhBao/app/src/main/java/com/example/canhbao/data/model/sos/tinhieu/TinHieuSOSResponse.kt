package com.example.canhbao.data.model.sos.tinhieu


data class TinHieuSOSResponse(
    val success: Boolean,
    val sosData: TinHieuSOSData,
    val truSoGanNhat: TruSoResponse?,
    val message: String
)