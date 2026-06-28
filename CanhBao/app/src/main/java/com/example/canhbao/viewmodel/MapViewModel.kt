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

import com.example.canhbao.data.model.suco.baocao.SuCoMapResponseDTO
import com.example.canhbao.data.network.AppConfig
import com.example.canhbao.data.network.BaoCaoSuCoApi
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit
import com.example.canhbao.data.network.PublicSocketManager
import com.example.canhbao.data.network.SocketClientProvider
import com.example.canhbao.viewmodel.suco.SuCoSocket
import com.google.firebase.auth.FirebaseAuth
import com.mapbox.geojson.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MapViewModel : ViewModel() {

    private val client = OkHttpClient()
    private val suCoApi: BaoCaoSuCoApi = BaoCaoSuCoRetrofit.api


    private val suCoSocket = SuCoSocket()
    private lateinit var appContext: Context

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

        appContext = context.applicationContext

        viewModelScope.launch {

            try {

                Log.d("MAP_SUCO", "START CALL API")


                val list = suCoApi.getSuCoForMap()


                val currentSocketData =
                    _suCoList.value


                _suCoList.value =
                    (list + currentSocketData)
                        .distinctBy { it.id }



                Log.d(
                    "MAP_SUCO",
                    "LOAD ICON"
                )


                loadAllIcons(
                    appContext,
                    _suCoList.value
                )


                Log.d(
                    "MAP_SUCO",
                    "FINISH"
                )


            } catch(e:Exception){

                Log.e(
                    "MAP_SUCO",
                    "API ERROR ${e.message}",
                    e
                )

            }
        }
    }
    init {
        startRealtimeSocket()
    }



    private fun startRealtimeSocket(){

        viewModelScope.launch {

            val user =
                FirebaseAuth.getInstance()
                    .currentUser
                    ?: return@launch


            val token =
                user.getIdToken(false)
                    .await()
                    .token
                    ?: return@launch



            SocketClientProvider.initNewClient(
                token
            )



            val callback =
                object: SuCoSocket.Callback {


                    override fun onSuCoUpdate(
                        suCo: SuCoMapResponseDTO
                    ){

                        Log.d(
                            "SUCO_SOCKET",
                            "UPDATE ${suCo.id} ${suCo.mucDoSuCo}"
                        )


                        updateSuCoFromSocket(
                            suCo
                        )
                    }



                    override fun onSuCoDelete(
                        id: Long
                    ){

                        removeSuCoFromSocket(
                            id
                        )
                    }

                }



            suCoSocket.subscribe(
                callback
            )

        }
    }
    fun toggleFilter(type: String) {
        if (type == "SU_CO") {
            _showSuCo.value = !_showSuCo.value
        }
    }


    fun updateSuCoFromSocket(
        suCo: SuCoMapResponseDTO
    ){
        val list =
            _suCoList.value.toMutableList()

        val index =
            list.indexOfFirst {
                it.id == suCo.id
            }


        if(index >=0){
            list[index]=suCo
        }else{
            list.add(suCo)
        }

        _suCoList.value=list


        // thêm đoạn này

        viewModelScope.launch {

            processAndAddSuCo(
                appContext,
                suCo
            )

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

        suCoSocket.clear()

        stopLocationUpdates()

        super.onCleared()

    }
}