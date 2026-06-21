package com.example.canhbao.data.model.sos.tinhieu

// 1. DTO thông tin cơ bản
data class UserInfoResponseDTO(
    val name: String?,
    val vip: Boolean?, // Jackson map thuộc tính 'boolean vip' thành key "vip"
    val email: String?
)

// 2. DTO mở rộng (đầy đủ các thuộc tính từ cả lớp cha và lớp con ở Backend)
data class UserMiniDTO(
    val id: String?,
    val name: String?,
    val email: String?,
    val vip: Boolean?,
    val totalPoints: Int?
)