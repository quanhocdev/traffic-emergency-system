package com.example.canhbao.viewmodel

import android.content.Context
import android.graphics.*
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.canhbao.data.network.SocketClientProvider
import coil.request.SuccessResult
import com.example.canhbao.data.model.CameraMapDto
import com.example.canhbao.data.model.TruSoMapDto
import com.example.canhbao.data.model.suco.baocao.SuCoMapResponseDTO
import com.example.canhbao.data.network.AppConfig
import com.example.canhbao.data.network.BaoCaoSuCoApi
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit
import com.example.canhbao.ui.screens.createCameraIcon
import com.example.canhbao.ui.screens.createTruSoMarkerBitmap
import com.mapbox.geojson.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import ua.naiksoftware.stomp.StompClient

class MapViewModel : ViewModel() {

    private val client = OkHttpClient()
    private val suCoApi: BaoCaoSuCoApi = BaoCaoSuCoRetrofit.api

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


    // --- 2. DỮ LIỆU MARKER ĐÃ ĐÈ MÀU (DÙNG CHO UI MAP VẼ) ---
    private val _suCoWithIcons = MutableStateFlow<List<Pair<SuCoMapResponseDTO, Bitmap>>>(emptyList())
    val suCoWithIcons = _suCoWithIcons.asStateFlow()

    private val _truSoWithIcons = MutableStateFlow<List<Pair<TruSoMapDto, Bitmap>>>(emptyList())
    val truSoWithIcons = _truSoWithIcons.asStateFlow()

    private val _cameraWithIcons = MutableStateFlow<List<Pair<CameraMapDto, Bitmap>>>(emptyList())
    val cameraWithIcons = _cameraWithIcons.asStateFlow()


    // --- 3. DANH SÁCH GỐC PHỤC VỤ CLICK & ĐO KHOẢNG CÁCH ---
    private val _suCoList = MutableStateFlow<List<SuCoMapResponseDTO>>(emptyList())
    val suCoList = _suCoList.asStateFlow()


    // --- 4. TRẠNG THÁI BỘ LỌC (FILTER ON/OFF) ---
    private val _showSuCo = MutableStateFlow(true)
    val showSuCo = _showSuCo.asStateFlow()

    private val _showTruSo = MutableStateFlow(true)
    val showTruSo = _showTruSo.asStateFlow()

    private val _showCamera = MutableStateFlow(true)
    val showCamera = _showCamera.asStateFlow()


    // --- 5. CÁC HÀM TẢI DỮ LIỆU BAN ĐẦU TỪ API ---

    fun loadSuCoForMap(context: Context) {
        viewModelScope.launch {
            try {
                // 💡 BƯỚC CHÍ MẠNG: Ép buộc dọn sạch danh sách hiển thị cũ trên UI State
                // Nếu biến lưu icon của bạn tên là _suCoWithIcons, hãy xóa trắng nó ở đây:
                _suCoWithIcons.value = emptyList()
                _suCoList.value = emptyList()

                // Gọi API lấy danh sách mới nhất từ Database về
                val list = suCoApi.getSuCoForMap()

                // Cập nhật danh sách mới
                _suCoList.value = list

                // Nạp lại icon dựa trên danh sách mới sạch sẽ hoàn toàn
                loadAllIcons(context, list)

                android.util.Log.d("MAP_DEBUG", "✅ Đã đồng bộ mới danh sách sự cố thành công. Số lượng: ${list.size}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadTruSoData(context: Context) {
        viewModelScope.launch {
            try {
                val list = suCoApi.getAllTruSo()
                val icon = createTruSoMarkerBitmap(context)
                _truSoWithIcons.value = list.map { it to icon }
                Log.d("MapViewModel", "🏠 Load API: Đã lấy ${list.size} trụ sở")
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun loadCameraData(context: Context) {
        viewModelScope.launch {
            try {
                val list = suCoApi.getAllCamera()
                val icon = createCameraIcon(context)
                _cameraWithIcons.value = list.map { it to icon }
                Log.d("MapViewModel", "📸 Đã load ${list.size} camera.")
            } catch (e: Exception) { Log.e("MapViewModel", "❌ Lỗi load camera: ${e.message}") }
        }
    }

    fun toggleFilter(type: String) {
        when(type) {
            "SU_CO" -> _showSuCo.value = !_showSuCo.value
            "TRU_SO" -> _showTruSo.value = !_showTruSo.value
            "CAMERA" -> _showCamera.value = !_showCamera.value
        }
    }


    // --- 6. XỬ LÝ ĐỒNG BỘ REALTIME TỪ SOCKET MANAGER BẮN QUA (TỐI ƯU LUỒNG) ---

    fun updateSuCoFromSocket(context: Context, suCo: SuCoMapResponseDTO) {
        viewModelScope.launch(Dispatchers.IO) {
            android.util.Log.d("REALTIME_BUG", "🔄 ViewModel bắt đầu xử lý sự cố ID = ${suCo.id}")

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
                android.util.Log.d("REALTIME_BUG", "❌ Đã xóa sự cố hoàn thành khỏi bản đồ (ID = ${suCo.id})")
                return@launch
            }

            val path = suCo.iconUrl ?: ""
            val fullUrl = if (path.startsWith("http")) path else "${AppConfig.HTTP_BASE_URL}$path"
            android.util.Log.d("REALTIME_BUG", "📸 Đường dẫn tải Icon: $fullUrl")

            val bmp = loadMarkerIcon(context, fullUrl, suCo.mucDoNghiemTrong)
            if (bmp != null) {
                val currentWithIcons = _suCoWithIcons.value.toMutableList()
                currentWithIcons.removeAll { it.first.id == suCo.id }
                currentWithIcons.add(suCo to bmp)

                withContext(Dispatchers.Main) {
                    _suCoWithIcons.value = currentWithIcons.toList()
                }
                // 📝 LOG CHÉC 2: Thành công hay không phải thấy dòng này!
                android.util.Log.d("REALTIME_BUG", "✅ Vẽ đè Icon thành công! Đã đẩy vào StateFlow hiển thị. Tổng số Marker hiện tại: ${currentWithIcons.size}")
            } else {
                android.util.Log.e("REALTIME_BUG", "💥 LỖI: Tải hoặc xử lý tạo Bitmap cho Icon thất bại!")
            }
        }
    }
    // Thêm biến global trong class MapViewModel để giữ kết nối không bị chết yểu
    // Trong MapViewModel.kt
    private var realtimeSocketManager: RealtimeSocketManager? = null

    fun startRealtimeSocket(context: Context, stompClient: StompClient) {
        // Nếu manager cũ có rồi và stomp đang chạy tốt thì bỏ qua
        if (realtimeSocketManager != null && stompClient.isConnected) {
            return
        }

        // Nếu socket lỗi/chết, hủy diệt tận gốc để tái sinh luồng mới
        android.util.Log.w("REALTIME_BUG", "⚠️ Phát hiện rò rỉ luồng cũ hoặc ngắt kết nối. Đang tái khởi tạo đường truyền...")
        realtimeSocketManager = null

        // Ép tạo mới một client sạch không dính rác RxJava cũ
        SocketClientProvider.initNewClient()
        val activeClient = SocketClientProvider.stompClient
        activeClient.connect()

        realtimeSocketManager = RealtimeSocketManager(context, activeClient).apply {
            subscribe(object : RealtimeSocketManager.Callback {
                override fun onSuCoUpdate(suCo: SuCoMapResponseDTO) {
                    if (suCo.trangThaiXuLy == "HUY_BO" || suCo.trangThaiXuLy == "HOAN_THANH") {
                        removeSuCoFromSocket(suCo.id)
                    } else {
                        updateSuCoFromSocket(context, suCo)
                    }
                }

                override fun onSuCoRemove(id: Long) {
                    removeSuCoFromSocket(id)
                }
                override fun onTruSoRemove(id: Long) {}
                override fun onCameraUpdate(camera: CameraMapDto) {}
                override fun onCameraRemove(id: Long) {}
            })
        }
    }

    fun removeSuCoFromSocket(id: Long) {
        viewModelScope.launch(Dispatchers.Main) { // Chạy thẳng trên Main thread cho an toàn với UI State
            // 1. Cập nhật danh sách tracking nền
            val updatedList = _suCoList.value.filter { it.id != id }
            _suCoList.value = updatedList

            // 2. Cập nhật danh sách Icon đồ họa Marker
            val updatedWithIcons = _suCoWithIcons.value.filter { it.first.id != id }
            _suCoWithIcons.value = updatedWithIcons

            android.util.Log.d("REALTIME_BUG", "🔥 Đã xóa sự cố $id khỏi State. Số lượng còn lại: ${updatedWithIcons.size}")
        }
    }
    fun updateCameraFromSocket(camera: CameraMapDto, icon: Bitmap) {
        val current = _cameraWithIcons.value.toMutableList()
        current.removeAll { it.first.id == camera.id }
        current.add(camera to icon)
        _cameraWithIcons.value = current
    }

    fun removeCameraFromSocket(id: Long) {
        val current = _cameraWithIcons.value.toMutableList()
        current.removeAll { it.first.id == id }
        _cameraWithIcons.value = current
    }


    // --- 7. LOGIC XỬ LÝ VẼ ĐÈ MÀU MARKER SỰ CỐ (TỐI ƯU MƯỢT MÀ) ---

    private suspend fun processAndAddSuCo(context: Context, suCo: SuCoMapResponseDTO) {
        val path = suCo.iconUrl ?: ""
        val fullUrl = if (path.startsWith("http")) path else "${AppConfig.HTTP_BASE_URL}$path"

        if (suCo.trangThaiXuLy == "HOAN_THANH") {
            // Loại bỏ withContext(Dispatchers.Main) dư thừa vì StateFlow vốn dĩ đã Thread-safe
            val currentList = _suCoWithIcons.value.toMutableList()
            currentList.removeAll { it.first.id == suCo.id }
            _suCoWithIcons.value = currentList.toList()
            return
        }

        loadMarkerIcon(context, fullUrl, suCo.mucDoNghiemTrong)?.let { bmp ->
            val currentList = _suCoWithIcons.value.toMutableList()
            currentList.removeAll { it.first.id == suCo.id }
            currentList.add(suCo to bmp)
            _suCoWithIcons.value = currentList.toList()
        }
    }

    private suspend fun loadAllIcons(context: Context, list: List<SuCoMapResponseDTO>) {
        withContext(Dispatchers.IO) {
            list.forEach { suCo ->
                processAndAddSuCo(context, suCo)
            }
            Log.d("MapViewModel", "✅ Đã xử lý xong toàn bộ icon sự cố")
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

                    val strokeColor = when (mucDo?.uppercase()) {
                        "HIGH" -> Color.RED
                        "MEDIUM" -> Color.rgb(255, 165, 0) // Màu Cam chuẩn xác
                        "LOW" -> Color.GREEN
                        else -> Color.WHITE
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
            } catch (e: Exception) { null }
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
        android.util.Log.w("GPS_DEBUG", "🧹 Tiến hành gỡ bỏ lắng nghe GPS để bảo vệ Main Thread...")
        // Nếu trong dự án bạn có viết hàm gỡ LocationCallback của FusedLocationProviderClient,
        // hãy thực hiện gọi lệnh removeLocationUpdates ở đây.
        // Ví dụ: fusedLocationClient?.removeLocationUpdates(locationCallback)
    }
    override fun onCleared() {
        super.onCleared()
        android.util.Log.e("MAP_DEBUG", "💥 MapViewModel OnCleared! Khai tử toàn bộ luồng định vị và Socket.")
        try {
            stopLocationUpdates()
            // Đóng socket hoàn toàn để tránh rò rỉ RxJava ngầm
            realtimeSocketManager = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}