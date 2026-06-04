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
import com.example.canhbao.data.model.CameraMapDto
import com.example.canhbao.data.model.SuCoMapDto
import com.example.canhbao.data.model.TruSoMapDto
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
import org.json.JSONArray
import org.json.JSONObject

import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient

class MapViewModel : ViewModel() {
    // Thêm vào phía trên cùng của class MapViewModel
    private val client = OkHttpClient()
    private val suCoApi: BaoCaoSuCoApi = BaoCaoSuCoRetrofit.api

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

    private val _suCoWithIcons = MutableStateFlow<List<Pair<SuCoMapDto, Bitmap>>>(emptyList())
    val suCoWithIcons = _suCoWithIcons.asStateFlow()


    // Thêm vào cùng chỗ với _suCoWithIcons
    private val _truSoWithIcons = MutableStateFlow<List<Pair<TruSoMapDto, Bitmap>>>(emptyList())
    val truSoWithIcons = _truSoWithIcons.asStateFlow()

    private val _placeSuggestions = MutableStateFlow<List<String>>(emptyList())
    val placeSuggestions: StateFlow<List<String>> = _placeSuggestions
    private val placePointMap = mutableMapOf<String, Point>()
    private val _truSoList = MutableStateFlow<List<TruSoMapDto>>(emptyList())
    val truSoList = _truSoList.asStateFlow()
    private val _cameraWithIcons = MutableStateFlow<List<Pair<CameraMapDto, Bitmap>>>(emptyList())
    val cameraWithIcons = _cameraWithIcons.asStateFlow()

    // Thêm các list này để hỗ trợ tìm kiếm khi click
    private val _suCoList = MutableStateFlow<List<SuCoMapDto>>(emptyList())
    val suCoList = _suCoList.asStateFlow()

    
    // Cập nhật lại loadSuCoForMap để lưu cả list DTO
    fun loadSuCoForMap(context: Context) {
        viewModelScope.launch {
            try {
                val list = suCoApi.getSuCoForMap()
                _suCoList.value = list // Lưu list DTO gốc ở đây
                loadAllIcons(context, list)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun loadTruSoData(context: Context) {
        viewModelScope.launch {
            try {
                val list = suCoApi.getAllTruSo()
                // Sửa tên hàm ở đây cho khớp với hàm bạn đã sửa ở trên
                val icon = createTruSoMarkerBitmap(context)
                _truSoWithIcons.value = list.map { it to icon }
                println("🏠 Load API: Đã lấy ${list.size} trụ sở với icon mới")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadCameraData(context: Context) {
        viewModelScope.launch {
            try {
                // Đảm bảo interface BaoCaoSuCoApi đã cập nhật trả về List<CameraMapDto> với đủ trường
                val list = suCoApi.getAllCamera()
                val icon = createCameraIcon(context)

                // Lưu trọn vẹn list vào Flow để MapScreen sử dụng
                _cameraWithIcons.value = list.map { camera ->
                    camera to icon
                }
                Log.d("MapViewModel", "📸 Đã load ${list.size} camera. Ví dụ video: ${list.firstOrNull()?.videoUrl}")
            } catch (e: Exception) {
                Log.e("MapViewModel", "❌ Lỗi load camera: ${e.message}")
            }
        }
    }
    // Trong MapViewModel.kt
    private val _showSuCo = MutableStateFlow(true)
    val showSuCo = _showSuCo.asStateFlow()

    private val _showTruSo = MutableStateFlow(true)
    val showTruSo = _showTruSo.asStateFlow()

    private val _showCamera = MutableStateFlow(true)
    val showCamera = _showCamera.asStateFlow()

    fun toggleFilter(type: String) {
        when(type) {
            "SU_CO" -> _showSuCo.value = !_showSuCo.value
            "TRU_SO" -> _showTruSo.value = !_showTruSo.value
            "CAMERA" -> _showCamera.value = !_showCamera.value
        }
    }


    override fun onCleared() {
        super.onCleared()
        mStompClient?.disconnect() // Ngắt kết nối khi thoát app để tiết kiệm pin
    }

    private suspend fun processAndAddSuCo(context: Context, suCo: SuCoMapDto) {
        val path = suCo.iconUrl ?: ""
        val fullUrl = if (path.startsWith("http")) path
        else "${AppConfig.HTTP_BASE_URL}$path"

        if (suCo.trangThaiXuLy == "HOAN_THANH") {
            withContext(Dispatchers.Main) {
                val currentList = _suCoWithIcons.value.toMutableList()
                currentList.removeAll { it.first.id == suCo.id }
                _suCoWithIcons.value = currentList.toList()
            }
            return
        }
        loadMarkerIcon(context, fullUrl, suCo.mucDoNghiemTrong)?.let { bmp ->
            withContext(Dispatchers.Main) {
                val currentList = _suCoWithIcons.value.toMutableList()
                // Xóa cũ dựa trên ID, thêm mới (giúp cập nhật trạng thái ngay lập tức)
                currentList.removeAll { it.first.id == suCo.id }
                currentList.add(suCo to bmp)
                _suCoWithIcons.value = currentList.toList()
            }
        }
    }

    // --- SỬA LẠI HÀM LOAD BAN ĐẦU ---
    private suspend fun loadAllIcons(context: Context, list: List<SuCoMapDto>) {
        withContext(Dispatchers.IO) {
            // Thay vì viết lại logic, ta gọi hàm dùng chung cho từng item
            list.forEach { suCo ->
                processAndAddSuCo(context, suCo)
            }
            println("✅ Đã load xong ${list.size} sự cố")
        }
    }

    fun onSearchQueryChanged(query: String) {
        if (query.length < 2) {
            _placeSuggestions.value = emptyList()
            return
        }
        viewModelScope.launch {
            val results = searchPlaces(query)
            _placeSuggestions.value = results.map { it.first }
            results.forEach { (name, point) ->
                placePointMap[name] = point
            }
        }
    }

    fun getPointForName(name: String): Point? = placePointMap[name]

    fun setTravelMode(mode: String) {
        _travelMode.value = mode
    }

    fun setEndPoint(point: Point, name: String) {
        _endPoint.value = point
        _endName.value = name
    }

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

    suspend fun searchPlaces(query: String): List<Pair<String, Point>> =
        withContext(Dispatchers.IO) {
            try {
                val url =
                    "https://nominatim.openstreetmap.org/search?q=$query&format=json&limit=5&countrycodes=vn"
                val request =
                    Request.Builder().url(url).header("User-Agent", "CanhBaoApp/1.0").build()
                val response = client.newCall(request).execute()
                val jsonArray = JSONArray(response.body?.string() ?: "[]")
                val results = mutableListOf<Pair<String, Point>>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    results.add(
                        obj.getString("display_name") to Point.fromLngLat(
                            obj.getDouble("lon"),
                            obj.getDouble("lat")
                        )
                    )
                }
                results
            } catch (e: Exception) {
                emptyList()
            }
        }

    suspend fun loadMarkerIcon(context: Context, url: String, mucDo: String?): Bitmap? {
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
                    val output = Bitmap.createBitmap(
                        size,
                        (size + tailHeight).toInt(),
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(output)
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                    val strokeColor = when (mucDo?.uppercase()) {
                        "HIGH" -> Color.RED
                        "MEDIUM" -> Color.rgb(255, 165, 0)
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
                    canvas.drawBitmap(
                        originalBitmap,
                        null,
                        Rect(left, top, left + innerSize, top + innerSize),
                        paint
                    )
                    output
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    // Thêm vào trong class MapViewModel
// Hàm này vẽ Marker có viền màu Blue cho Trụ sở

    fun getFreeDirections() {
        val start = _startPoint.value
        val end = _endPoint.value
        if (start == null || end == null) return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    // profile: car, bike, foot
                    val profile = when (_travelMode.value) {
                        "Xe máy" -> "bike"
                        "Ô tô" -> "car"
                        else -> "foot"
                    }

                    // URL OSRM: long,lat
                    val url = "https://router.project-osrm.org/route/v1/$profile/" +
                            "${start.longitude()},${start.latitude()};" +
                            "${end.longitude()},${end.latitude()}?overview=full&geometries=geojson&alternatives=true"

                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()
                    val jsonData = JSONObject(response.body?.string() ?: "")
                    val routes = jsonData.getJSONArray("routes")

                    val tempAllRoutes = mutableListOf<List<Point>>()
                    val tempRouteInfos = mutableListOf<Pair<String, String>>()

                    for (i in 0 until routes.length()) {
                        val route = routes.getJSONObject(i)

                        // Lấy tọa độ
                        val geometry = route.getJSONObject("geometry")
                        val coordinates = geometry.getJSONArray("coordinates")
                        val points = mutableListOf<Point>()
                        for (j in 0 until coordinates.length()) {
                            val coord = coordinates.getJSONArray(j)
                            points.add(Point.fromLngLat(coord.getDouble(0), coord.getDouble(1)))
                        }
                        tempAllRoutes.add(points)

                        // Lấy khoảng cách và thời gian
                        val dist = route.getDouble("distance") / 1000.0 // km
                        val dur = route.getDouble("duration") / 60.0 // phút
                        tempRouteInfos.add(
                            String.format("%.1f km", dist) to String.format("%.0f phút", dur)
                        )
                    }

                    _allRoutes.value = tempAllRoutes
                    _routeInfos.value = tempRouteInfos

                    // Mặc định chọn tuyến đường đầu tiên
                    if (tempRouteInfos.isNotEmpty()) {
                        _selectedRouteIndex.value = 0
                        _routeDistance.value = tempRouteInfos[0].first
                        _routeDuration.value = tempRouteInfos[0].second
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Đảm bảo bạn có hàm này cho DiChuyenScreen gọi
    fun setStartPoint(point: Point, name: String) {
        _startPoint.value = point
        _startName.value = name
    }







    fun navigateToDestination(destPoint: Point, destName: String, userLocation: Point?) {
        if (userLocation == null) return

        // 1. Đặt điểm bắt đầu là vị trí hiện tại
        setStartPoint(userLocation, "Vị trí của bạn")

        // 2. Đặt điểm kết thúc là đối tượng vừa chọn (Trụ sở/Sự cố)
        setEndPoint(destPoint, destName)

        // 3. Gọi hàm tìm đường OSRM đã viết
        getFreeDirections()

    }
    private fun getDirectionName(bearing: Float): String {
        return when {
            bearing in -22.5..22.5 -> "Phía Bắc"
            bearing in 22.5..67.5 -> "Hướng Đông Bắc"
            bearing in 67.5..112.5 -> "Phía Đông"
            bearing in 112.5..157.5 -> "Hướng Đông Nam"
            bearing in 157.5..180.0 || bearing in -180.0..-157.5 -> "Phía Nam"
            bearing in -157.5..-112.5 -> "Hướng Tây Nam"
            bearing in -112.5..-67.5 -> "Phía Tây"
            bearing in -67.5..-22.5 -> "Hướng Tây Bắc"
            else -> "Phía trước"
        }
    }

}
