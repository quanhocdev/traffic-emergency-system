package com.example.canhbao.viewmodel

import android.content.Context
import android.graphics.*
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.canhbao.data.model.info.camera.CameraMapDto
import com.example.canhbao.data.model.info.truso.TruSoMapDto
import com.example.canhbao.data.model.suco.baocao.SuCoMapResponseDTO
import com.example.canhbao.data.network.AppConfig
import com.example.canhbao.data.network.BaoCaoSuCoApi
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit
import com.example.canhbao.data.network.PublicSocketManager
import com.example.canhbao.data.network.SocketClientProvider
import com.google.gson.Gson // Sử dụng Gson để parse JSON nhận từ PublicSocketManager
import com.mapbox.geojson.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MapViewModel : ViewModel() {

    private val client = OkHttpClient()
    private val suCoApi: BaoCaoSuCoApi = BaoCaoSuCoRetrofit.api
    private val gson = Gson()

    // Quản lý instance PublicSocketManager dựa trên file bạn đã viết
    private var publicSocketManager: PublicSocketManager? = null

    // --- 1. QUẢN LÝ TÌM ĐƯỜNG & ĐIỀU HƯỚNG ---
    private val _startPoint = MutableStateFlow<Point?>(null)
    private val _endPoint = MutableStateFlow<Point?>(null)
    private val _startName = MutableStateFlow("")
    private val _endName = MutableStateFlow("")

    private val _routeDistance = MutableStateFlow("")
    val routeDistance: StateFlow<String> = _routeDistance

    private val _routeDuration = MutableStateFlow("")
    val routeDuration: StateFlow<String> = _routeDuration

    private val _travelMode = MutableStateFlow("Xe máy")
    val travelMode: StateFlow<String> = _travelMode

    private val _allRoutes = MutableStateFlow<List<List<Point>>>(emptyList())
    val allRoutes = _allRoutes.asStateFlow()

    private val _routeInfos = MutableStateFlow<List<Pair<String, String>>>(emptyList())

    private val _selectedRouteIndex = MutableStateFlow(0)
    val selectedRouteIndex = _selectedRouteIndex.asStateFlow()

    val routePoints: StateFlow<List<Point>> =
        combine(_allRoutes, _selectedRouteIndex) { routes, index ->
            if (routes.isNotEmpty() && index < routes.size) routes[index] else emptyList()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- 2. DỮ LIỆU MARKER ĐÃ ĐÈ MÀU (CHỈ CÒN SỰ CỐ) ---
    private val _suCoWithIcons = MutableStateFlow<List<Pair<SuCoMapResponseDTO, Bitmap>>>(emptyList())
    val suCoWithIcons = _suCoWithIcons.asStateFlow()


    // --- 3. DANH SÁCH GỐC PHỤC VỤ CLICK & ĐO KHOẢNG CÁCH ---
    private val _suCoList = MutableStateFlow<List<SuCoMapResponseDTO>>(emptyList())
    val suCoList = _suCoList.asStateFlow()


    // --- 4. TRẠNG THÁI BỘ LỌC (CHỈ GIỮ LẠI SỰ CỐ) ---
    private val _showSuCo = MutableStateFlow(true)
    val showSuCo = _showSuCo.asStateFlow()


    // --- 5. CÁC HÀM TẢI DỮ LIỆU BAN ĐẦU TỪ API ---

    fun loadSuCoForMap(context: Context) {
        viewModelScope.launch {
            try {
                _suCoWithIcons.value = emptyList()
                _suCoList.value = emptyList()

                val list = suCoApi.getSuCoForMap()
                _suCoList.value = list
                loadAllIcons(context, list)

                Log.d("MAP_DEBUG", "✅ Đã đồng bộ mới danh sách sự cố thành công. Số lượng: ${list.size}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleFilter(type: String) {
        if (type == "SU_CO") {
            _showSuCo.value = !_showSuCo.value
        }
    }


    // --- 6. XỬ LÝ ĐỒNG BỘ REALTIME TỪ SOCKET MANAGER BẮN QUA ---

    fun updateSuCoFromSocket(context: Context, suCo: SuCoMapResponseDTO) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("REALTIME_BUG", "🔄 ViewModel bắt đầu xử lý sự cố ID = ${suCo.id}")

            val currentList = _suCoList.value.toMutableList()
            currentList.removeAll { it.id == suCo.id }
            currentList.add(suCo)
            _suCoList.value = currentList

            if (suCo.trangThaiXuLy == "HOAN_THANH") {
                val currentWithIcons = _suCoWithIcons.value.toMutableList()
                currentWithIcons.removeAll { it.first.id == suCo.id }
                withContext(Dispatchers.Main) {
                    _suCoWithIcons.value = currentWithIcons.toList()
                }
                Log.d("REALTIME_BUG", "❌ Đã xóa sự cố hoàn thành khỏi bản đồ (ID = ${suCo.id})")
                return@launch
            }

            val path = suCo.iconUrl ?: ""
            val fullUrl = if (path.startsWith("http")) path else "${AppConfig.HTTP_BASE_URL}$path"

            val bmp = loadMarkerIcon(context, fullUrl, suCo.mucDoSuCo)
            if (bmp != null) {
                val currentWithIcons = _suCoWithIcons.value.toMutableList()
                currentWithIcons.removeAll { it.first.id == suCo.id }
                currentWithIcons.add(suCo to bmp)

                withContext(Dispatchers.Main) {
                    _suCoWithIcons.value = currentWithIcons.toList()
                }
                Log.d("REALTIME_BUG", "✅ Vẽ đè Icon thành công! Đã đẩy vào StateFlow.")
            }
        }
    }

    fun removeSuCoFromSocket(id: Long) {
        viewModelScope.launch(Dispatchers.Main) {
            val updatedList = _suCoList.value.filter { it.id != id }
            _suCoList.value = updatedList

            val updatedWithIcons = _suCoWithIcons.value.filter { it.first.id != id }
            _suCoWithIcons.value = updatedWithIcons

            Log.d("REALTIME_BUG", "Đã xóa sự cố $id khỏi State.")
        }
    }

    // KHỞI CHẠY KẾT NỐI VÀ ĐĂNG KÝ THEO PUBLIC_SOCKET_MANAGER
    fun startRealtimeSocket(
        context: Context,
        truSoViewModel: TruSoViewModel,
        cameraViewModel: CameraViewModel
    ) {
        val currentClient = SocketClientProvider.stompClient
        if (publicSocketManager != null && currentClient.isConnected) return

        publicSocketManager = null
        SocketClientProvider.initNewClient()
        val activeClient = SocketClientProvider.stompClient

        // Khởi tạo PublicSocketManager theo đúng cấu trúc file của bạn
        publicSocketManager = PublicSocketManager(activeClient).apply {
            subscribe(object : PublicSocketManager.Callback {

                override fun onSuCoUpdate(json: String) {
                    try {
                        val suCo = gson.fromJson(json, SuCoMapResponseDTO::class.java)
                        updateSuCoFromSocket(context, suCo)
                    } catch (e: Exception) {
                        Log.e("MAP_SOCKET", "Lỗi parse SuCo: ${e.message}")
                    }
                }

                override fun onSuCoDelete(id: Long) {
                    removeSuCoFromSocket(id)
                }

                override fun onTruSoUpdate(json: String) {
                    try {
                        val truSo = gson.fromJson(json, TruSoMapDto::class.java)
                        truSoViewModel.updateTruSoFromSocket(context, truSo)
                    } catch (e: Exception) {
                        Log.e("MAP_SOCKET", "Lỗi parse TruSo: ${e.message}")
                    }
                }

                override fun onTruSoDelete(id: Long) {
                    truSoViewModel.removeTruSoFromSocket(id)
                }

                override fun onCameraUpdate(json: String) {
                    try {
                        val camera = gson.fromJson(json, CameraMapDto::class.java)
                        cameraViewModel.updateCameraFromSocket(context, camera)
                    } catch (e: Exception) {
                        Log.e("MAP_SOCKET", "Lỗi parse Camera: ${e.message}")
                    }
                }

                override fun onCameraDelete(id: Long) {
                    cameraViewModel.removeCameraFromSocket(id)
                }

                override fun onPublicFundUpdate(json: String) {
                    Log.d("MAP_SOCKET", "Quỹ công khai cập nhật: $json")
                    // Triển khai thêm cập nhật Quỹ tại đây nếu cần thiết
                }
            })
        }
        activeClient.connect()
    }


    // --- 7. LOGIC XỬ LÝ VẼ ĐÈ MÀU MARKER SỰ CỐ ---

    private suspend fun processAndAddSuCo(context: Context, suCo: SuCoMapResponseDTO) {
        val path = suCo.iconUrl ?: ""
        val fullUrl = if (path.startsWith("http")) path else "${AppConfig.HTTP_BASE_URL}$path"

        if (suCo.trangThaiXuLy == "HOAN_THANH") {
            val currentList = _suCoWithIcons.value.toMutableList()
            currentList.removeAll { it.first.id == suCo.id }
            _suCoWithIcons.value = currentList.toList()
            return
        }

        loadMarkerIcon(context, fullUrl, suCo.mucDoSuCo)?.let { bmp ->
            val currentList = _suCoWithIcons.value.toMutableList()
            currentList.removeAll { it.first.id == suCo.id }
            currentList.add(suCo to bmp)
            _suCoWithIcons.value = currentList.toList()
        }
    }

    private suspend fun loadAllIcons(context: Context, list: List<SuCoMapResponseDTO>) {
        withContext(Dispatchers.IO) {
            list.forEach { suCo -> processAndAddSuCo(context, suCo) }
            Log.d("MapViewModel", "Đã xử lý xong toàn bộ icon sự cố")
        }
    }

    private suspend fun loadMarkerIcon(context: Context, url: String, mucDo: String?): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context).data(url).allowHardware(false).build()
                val result = (loader.execute(request) as? SuccessResult)?.drawable
                result?.toBitmap()?.let { originalBitmap ->
                    val size = 160
                    val centerX = size / 2f
                    val circleRadius = 50f
                    val strokeThickness = 12f
                    val tailHeight = 25f
                    val padding = 15f
                    val output = Bitmap.createBitmap(size, (size + tailHeight).toInt(), Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(output)
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

                    val strokeColor = when (mucDo?.uppercase()?.trim()) {
                        "HIGH" -> Color.RED
                        "MEDIUM" -> Color.rgb(241, 196, 15)
                        "LOW" -> Color.rgb(46, 204, 113)
                        else -> Color.rgb(148, 163, 184)
                    }

                    val path = Path().apply {
                        moveTo(centerX - 22f, size - 28f)
                        lineTo(centerX + 22f, size - 28f)
                        lineTo(centerX, size + tailHeight - 2f)
                        close()
                    }
                    paint.color = strokeColor
                    canvas.drawPath(path, paint)

                    canvas.drawCircle(centerX, centerX, circleRadius + strokeThickness, paint)
                    paint.color = Color.WHITE
                    canvas.drawCircle(centerX, centerX, circleRadius, paint)

                    val innerSize = ((circleRadius - padding) * 2).toInt()
                    val left = (centerX - (innerSize / 2)).toInt()
                    val top = (centerX - (innerSize / 2)).toInt()
                    canvas.drawBitmap(originalBitmap, null, Rect(left, top, left + innerSize, top + innerSize), paint)

                    output
                }
            } catch (e: Exception) {
                Log.e("REALTIME_BUG", "Lỗi tại hàm dựng Canvas Bitmap: ${e.message}")
                null
            }
        }
    }

    // --- 8. LOGIC ĐIỀU HƯỚNG TÌM ĐƯỜNG OSRM COOP ---

    fun setTravelMode(mode: String) { _travelMode.value = mode }
    fun setStartPoint(point: Point, name: String) { _startPoint.value = point; _startName.value = name }
    fun setEndPoint(point: Point, name: String) { _endPoint.value = point; _endName.value = name }

    fun selectRoute(index: Int) {
        _selectedRouteIndex.value = index
        _routeInfos.value.getOrNull(index)?.let {
            _routeDistance.value = it.first
            _routeDuration.value = it.second
        }
    }

    fun clearRoute() {
        _allRoutes.value = emptyList()
        _startPoint.value = null
        _endPoint.value = null
        _routeDistance.value = ""
        _routeDuration.value = ""
        _selectedRouteIndex.value = 0
    }

    fun getFreeDirections() {
        val start = _startPoint.value
        val end = _endPoint.value
        if (start == null || end == null) return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val profile = when (_travelMode.value) {
                        "Xe máy" -> "bike"
                        "Ô tô" -> "car"
                        else -> "foot"
                    }
                    val url = "https://router.project-osrm.org/route/v1/$profile/${start.longitude()},${start.latitude()};${end.longitude()},${end.latitude()}?overview=full&geometries=geojson&alternatives=true"
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()
                    val jsonData = JSONObject(response.body?.string() ?: "")
                    val routes = jsonData.getJSONArray("routes")
                    val tempAllRoutes = mutableListOf<List<Point>>()
                    val tempRouteInfos = mutableListOf<Pair<String, String>>()

                    for (i in 0 until routes.length()) {
                        val route = routes.getJSONObject(i)
                        val geometry = route.getJSONObject("geometry")
                        val coordinates = geometry.getJSONArray("coordinates")
                        val points = mutableListOf<Point>()
                        for (j in 0 until coordinates.length()) {
                            val coord = coordinates.getJSONArray(j)
                            points.add(Point.fromLngLat(coord.getDouble(0), coord.getDouble(1)))
                        }
                        tempAllRoutes.add(points)
                        val dist = route.getDouble("distance") / 1000.0
                        val dur = route.getDouble("duration") / 60.0
                        tempRouteInfos.add(String.format("%.1f km", dist) to String.format("%.0f phút", dur))
                    }

                    _allRoutes.value = tempAllRoutes
                    _routeInfos.value = tempRouteInfos
                    if (tempRouteInfos.isNotEmpty()) {
                        _selectedRouteIndex.value = 0
                        _routeDistance.value = tempRouteInfos[0].first
                        _routeDuration.value = tempRouteInfos[0].second
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    fun navigateToDestination(destPoint: Point, destName: String, userLocation: Point?) {
        if (userLocation == null) return
        setStartPoint(userLocation, "Vị trí của bạn")
        setEndPoint(destPoint, destName)
        getFreeDirections()
    }

    fun stopLocationUpdates() {
        Log.w("GPS_DEBUG", "Tiến hành gỡ bỏ lắng nghe GPS để bảo vệ Main Thread...")
    }

    override fun onCleared() {
        super.onCleared()
        Log.e("MAP_DEBUG", "MapViewModel OnCleared! Khai tử toàn bộ luồng định vị và Socket.")
        try {
            stopLocationUpdates()
            publicSocketManager = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}