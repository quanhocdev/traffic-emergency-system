    package com.example.canhbao.data.network

    import com.example.canhbao.data.model.suco.baocao.response.BaoCaoResponse
    import com.example.canhbao.data.model.suco.baocao.request.BaoCaoSuCoRequest
    import com.example.canhbao.data.model.CameraMapDto
    import com.example.canhbao.data.model.DoiTienDto
    import com.example.canhbao.data.model.GoiDto
    import com.example.canhbao.data.model.LichSuDto
    import com.example.canhbao.data.model.suco.loai.LoaiSuCo
    import com.example.canhbao.data.model.MuaGoiDto
    import com.example.canhbao.data.model.MuaGoiRequest
    import com.example.canhbao.data.model.SuCoMapDto
    import com.example.canhbao.data.model.SuCoUserDto
    import com.example.canhbao.data.model.ThongKeQuyDto
    import com.example.canhbao.data.model.TinHieuSOSRequest
    import com.example.canhbao.data.model.TruSoMapDto
    import com.example.canhbao.data.model.qua.QuaResponseDTO
    import com.example.canhbao.data.model.qua.doiqua.DoiQuaRequestDTO
    import com.example.canhbao.data.model.qua.doiqua.TuiQuaResponseDTO
    import retrofit2.Response
    import retrofit2.http.Body
    import retrofit2.http.GET
    import retrofit2.http.Header
    import retrofit2.http.PATCH
    import retrofit2.http.POST
    import retrofit2.http.Path
    import retrofit2.http.Query

    interface BaoCaoSuCoApi {
        @GET("/api/loai-su-co")
        suspend fun getAllLoaiSuCo(): List<LoaiSuCo>

        @POST("/api/su-co")
        suspend fun submitReport(
            @Header("Authorization") token: String,
            @Body request: BaoCaoSuCoRequest
        ): Response<BaoCaoResponse>
        @PATCH("/api/su-co/{id}")
        suspend fun cancelSuCo(
            @Header("Authorization") token: String,
            @Path("id") id: Long
        ): Response<Map<String, Any>>

        @GET("/api/su-co/map")
        suspend fun getSuCoForMap(): List<SuCoMapDto>

        @GET("/api/tru-so/all")
        suspend fun getAllTruSo(): List<TruSoMapDto>

        @GET("/api/camera/all")
        suspend fun getAllCamera(): List<CameraMapDto>

        // URL: BASE_URL + /api/tin-hieu-sos + /submit
        @POST("/api/tin-hieu-sos/submit")
        suspend fun submitSOS(
            @Header("Authorization") token: String,
            @Body request: TinHieuSOSRequest
        ): Response<Map<String, Any>>


        @GET("/api/auth/me")
        suspend fun getUserInfo(
            @Header("Authorization") token: String
        ): SuCoUserDto


        @GET("/api/lich-su/all")
        suspend fun getAllHistory(
            @Header("Authorization") token: String,
            @Query("type") type: String? = null
        ): List<LichSuDto>


        @POST("/api/tin-hieu-sos/cancel/{id}")
        suspend fun cancelSOS(
            @Header("Authorization") token: String,
            @Path("id") id: Long
        ): Response<Map<String, Any>>
        // Lấy danh sách gói về
        @GET("/api/mua-goi/danh-sach")
        suspend fun getDanhSachGoi(): List<GoiDto>

        // Gửi yêu cầu mua gói
        @POST("/api/mua-goi/dang-ky")
        suspend fun dangKyMuaGoi(
            @Header("Authorization") token: String,
            @Body request: MuaGoiRequest
        ): Response<Map<String, Any>>

        @GET("/api/mua-goi/my-packages")
        suspend fun getGoiCuaToi(
            @Header("Authorization") token: String
        ): List<MuaGoiDto>

        @POST("/api/mua-goi/cancel/{id}")
        suspend fun cancelMuaGoi(
            @Header("Authorization") token: String,
            @Path("id") id: Long
        ): Response<Map<String, Any>>

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

        @POST("/api/hoa-don/xac-nhan")
        suspend fun confirmPayment(
            @Header("Authorization") token: String,
            @Query("id") id: Long,
            @Query("quaId") quaId: Long?
        ): Response<Map<String, Any>>

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
    }
