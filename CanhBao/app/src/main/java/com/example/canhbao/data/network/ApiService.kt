package com.example.canhbao.data.network

import com.example.canhbao.data.model.info.camera.CameraMapDto
import com.example.canhbao.data.model.hoadon.HoaDonUserResponseDTO
import com.example.canhbao.data.model.hoadon.payment.ThanhToanRequestDTO
import com.example.canhbao.data.model.hoadon.payment.ThanhToanResponseDTO
import com.example.canhbao.data.model.qua.QuaResponseDTO
import com.example.canhbao.data.model.qua.doiqua.DoiQuaRequestDTO
import com.example.canhbao.data.model.qua.doiqua.TuiQuaResponseDTO
import com.example.canhbao.data.model.sos.goi.GoiResponseDto
import com.example.canhbao.data.model.sos.goi.MuaGoiUserResponseDto
import com.example.canhbao.data.model.sos.goi.MuaGoiRequestDTO
import com.example.canhbao.data.model.sos.tinhieu.SOSMapResponseDTO
import com.example.canhbao.data.model.sos.tinhieu.TheoDoiSOSDetailResponseDTO
import com.example.canhbao.data.model.sos.tinhieu.TheoDoiSOSItemResponseDTO
import com.example.canhbao.data.model.sos.tinhieu.TinHieuSOSRequestDTO
import com.example.canhbao.data.model.sos.tinhieu.TinHieuSOSResponse
import com.example.canhbao.data.model.suco.baocao.*
import com.example.canhbao.data.model.suco.loai.LoaiSuCo
import com.example.canhbao.data.model.tien.DoiTienDto
import com.example.canhbao.data.model.tien.ThongKeQuyDto
import com.example.canhbao.data.model.info.truso.TruSoMapDto
import retrofit2.Response
import retrofit2.http.*

interface BaoCaoSuCoApi {

    // BÁO CÁO SỰ CỐ & BẢN ĐỒ REALTIME

    @GET("/api/loai-su-co")
    suspend fun getAllLoaiSuCo(): List<LoaiSuCo>

    @POST("/api/su-co")
    suspend fun submitReport(
        @Header("Authorization") token: String,
        @Body request: SuCoRequestDTO
    ): Response<AiResponse>

    @GET("/api/su-co/map")
    suspend fun getSuCoForMap(): List<SuCoMapResponseDTO>

    @GET("/api/su-co/{id}")
    suspend fun getSuCoDetail(
        @Path("id") id: Long
    ): UserSuCoDetailResponseDTO

    @PATCH("/api/su-co/{id}")
    suspend fun cancelSuCo(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Map<String, Any>>

    @GET("/api/su-co/theo-doi")
    suspend fun getTheoDoiSuCo(
        @Header("Authorization") token: String
    ): List<TheoDoiSuCoItemResponseDTO>

    @GET("/api/su-co/theo-doi/{id}")
    suspend fun getTheoDoiSuCoDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): TheoDoiSuCoDetailResponseDTO

    @GET("/api/tru-so/all")
    suspend fun getAllTruSo(): List<TruSoMapDto>

    @GET("/api/camera/all")
    suspend fun getAllCamera(): List<CameraMapDto>


    // TÍN HIỆU SOS KHẨN CẤP


    @POST("/api/tin-hieu-sos/submit")
    suspend fun submitSOS(
        @Header("Authorization") token: String,
        @Body request: TinHieuSOSRequestDTO
    ): Response<TinHieuSOSResponse>

    @POST("/api/tin-hieu-sos/cancel/{id}")
    suspend fun cancelSOS(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Map<String, Any>>

    @GET("/api/sos/map")
    suspend fun getSOSMap(): List<SOSMapResponseDTO>
    @GET("/api/sos/theo-doi")
    suspend fun getTheoDoiSOS(
        @Header("Authorization") token: String
    ): List<TheoDoiSOSItemResponseDTO>
    @GET("/api/sos/theo-doi/{id}")
    suspend fun getTheoDoiSOSDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): TheoDoiSOSDetailResponseDTO


    // MUA GÓI CỨU TRỢ (RESCUE PACKAGES)

    @GET("/api/mua-goi/danh-sach")
    suspend fun getDanhSachGoi(): List<GoiResponseDto>

    @POST("/api/mua-goi/dang-ky")
    suspend fun dangKyMuaGoi(
        @Header("Authorization") token: String,
        @Body request: MuaGoiRequestDTO
    ): Response<Map<String, Any>>

    @GET("/api/mua-goi/my-packages")
    suspend fun getGoiCuaToi(
        @Header("Authorization") token: String
    ): List<MuaGoiUserResponseDto>

    @POST("/api/mua-goi/cancel/{id}")
    suspend fun cancelMuaGoi(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Map<String, Any>>


    // ĐỔI QUÀ THƯỞNG

    @GET("/api/qua/all")
    suspend fun getAllQua(): List<QuaResponseDTO>

    @POST("/api/qua/exchange")
    suspend fun exchangeQua(
        @Header("Authorization") token: String,
        @Body request: DoiQuaRequestDTO
    ): Response<Map<String, Any>>

    @GET("/api/qua/my-gifts")
    suspend fun getMyGifts(
        @Header("Authorization") token: String
    ): List<TuiQuaResponseDTO>


    // HÓA ĐƠN & THANH TOÁN


    @GET("/api/hoa-don/user/danh-sach")
    suspend fun getHoaDonUser(
        @Header("Authorization") token: String
    ): List<HoaDonUserResponseDTO>

    @GET("/api/hoa-don/user/{id}")
    suspend fun getHoaDonDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): HoaDonUserResponseDTO

    @GET("/api/hoa-don/user/{hoaDonId}/thanh-toan")
    suspend fun getChiTietThanhToan(
        @Header("Authorization") token: String,
        @Path("hoaDonId") hoaDonId: Long
    ): ThanhToanResponseDTO

    @POST("/api/hoa-don/xac-nhan")
    suspend fun confirmPayment(
        @Header("Authorization") token: String,
        @Body request: ThanhToanRequestDTO
    ): Response<ThanhToanResponseDTO>

    @POST("/api/doi-tien/thuc-hien")
    suspend fun thucHienDoiTien(
        @Body request: DoiTienDto
    ): Response<Boolean>

    @GET("/api/doi-tien/lich-su/{userId}")
    suspend fun getHistory(
        @Path("userId") userId: String,
        @Query("loai") loai: String? = null
    ): List<DoiTienDto>

    @GET("/api/doi-tien/thong-ke-quy")
    suspend fun getThongKeQuy(): ThongKeQuyDto


    // THÔNG TIN USER & LỊCH SỬ CHUNG

    @GET("/api/auth/me")
    suspend fun getUserInfo(
        @Header("Authorization") token: String
    ): SuCoUserDto

}