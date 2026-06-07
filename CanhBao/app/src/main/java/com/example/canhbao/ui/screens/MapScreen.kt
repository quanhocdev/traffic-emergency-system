package com.example.canhbao.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import android.graphics.BitmapFactory
import com.example.canhbao.R
import com.example.canhbao.data.model.CameraMapDto
import com.example.canhbao.data.model.suco.baocao.SuCoMapResponseDTO
import com.example.canhbao.data.model.suco.baocao.UserSuCoDetailResponseDTO
import com.example.canhbao.data.network.AppConfig
import com.example.canhbao.viewmodel.AlertViewModel
import com.example.canhbao.viewmodel.CallViewModel
import com.example.canhbao.viewmodel.MapViewModel
import com.example.canhbao.viewmodel.RealtimeSocketManager
import com.example.canhbao.viewmodel.SOSViewModel
import com.example.canhbao.viewmodel.SearchViewModel
import com.example.canhbao.viewmodel.WebRTCViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ua.naiksoftware.stomp.StompClient
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    mapViewModel: MapViewModel,
    webrtcViewModel: WebRTCViewModel,
    alertViewModel: AlertViewModel = viewModel(),
    searchViewModel: SearchViewModel = viewModel(),
    sosViewModel: SOSViewModel = viewModel(),
    callViewModel: CallViewModel = viewModel(),
    stompClient: StompClient,
    isLoggedIn: Boolean,
    onReportClick: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val callState by webrtcViewModel.callState.collectAsState()
    val maThietBi = remember {
        android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
    }

    var mapView: MapView? by remember { mutableStateOf(null) }
    var lastUserLocation by remember { mutableStateOf<Point?>(null) }

    // --- STATE QUẢN LÝ DETAIL BOTTOM SHEET ---
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedMarkerType by remember { mutableStateOf("") } // "SU_CO", "TRU_SO", "CAMERA"
    var selectedSuCo by remember { mutableStateOf<SuCoMapResponseDTO?>(null) }
    // Đổi kiểu dữ liệu từ SuCoMapResponseDTO sang UserSuCoDetailResponseDTO
    var selectedSuCoDetail by remember { mutableStateOf<UserSuCoDetailResponseDTO?>(null) }
    var selectedTruSo by remember { mutableStateOf<com.example.canhbao.data.model.TruSoMapDto?>(null) } // Thay thế chính xác package Dto của bạn
    var selectedCamera by remember { mutableStateOf<CameraMapDto?>(null) }

    // Thu thập State từ MapViewModel
    val routePoints by mapViewModel.routePoints.collectAsState()
    val distance by mapViewModel.routeDistance.collectAsState()
    val duration by mapViewModel.routeDuration.collectAsState()
    val mode by mapViewModel.travelMode.collectAsState()
    val allRoutes by mapViewModel.allRoutes.collectAsState()
    val selectedIndex by mapViewModel.selectedRouteIndex.collectAsState()
    val suCoWithIcons by mapViewModel.suCoWithIcons.collectAsState()
    val truSoWithIcons by mapViewModel.truSoWithIcons.collectAsState()
    val cameraWithIcons by mapViewModel.cameraWithIcons.collectAsState()
    val suCoList by mapViewModel.suCoList.collectAsState()

    val showSuCo by mapViewModel.showSuCo.collectAsState()
    val showTruSo by mapViewModel.showTruSo.collectAsState()
    val showCamera by mapViewModel.showCamera.collectAsState()

    val suggestions by searchViewModel.placeSuggestions.collectAsState()
    var searchText by remember { mutableStateOf("") }

    val sosMessage by sosViewModel.sosResponse.collectAsState()
    val handlingTruSoId by sosViewModel.currentHandlingTruSoId.collectAsState()
    val isSoundOn by alertViewModel.isSoundEnabled.collectAsState()

    var showRangeCircle by remember { mutableStateOf(true) }

    val modeIcon = when (mode) {
        "Xe máy" -> Icons.Default.DirectionsBike
        "Ô tô" -> Icons.Default.DirectionsCar
        else -> Icons.Default.DirectionsWalk
    }

    var polygonManager: PolygonAnnotationManager? by remember { mutableStateOf(null) }
    var pointManager: PointAnnotationManager? by remember { mutableStateOf(null) }
    var polylineManager: PolylineAnnotationManager? by remember { mutableStateOf(null) }
    var truSoManager: PointAnnotationManager? by remember { mutableStateOf(null) }
    var cameraManager: PointAnnotationManager? by remember { mutableStateOf(null) }

    var userHeading by remember { mutableStateOf(0f) }
    var isMicroMuted by remember { mutableStateOf(false) }

    val tts = remember {
        android.speech.tts.TextToSpeech(context) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                println("TTS: Khởi tạo thành công!")
            }
        }
    }

    LaunchedEffect(tts) {
        tts.setLanguage(java.util.Locale("vi", "VN"))
        alertViewModel.ttsCommand.collect { message ->
            tts.speak(message, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "MessageID")
        }
    }

    val callerName by webrtcViewModel.callerName.collectAsState()
    val ringtonePlayer = remember {
        val notification = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_RINGTONE)
        android.media.RingtoneManager.getRingtone(context, notification)
    }

    LaunchedEffect(callState) {
        if (callState == "INCOMING") {
            ringtonePlayer.play()
            val textToSpeak = "${callerName ?: "Trụ sở cứu hộ"} đang gọi tới. Nhắc lại, ${callerName ?: "Trụ sở cứu hộ"} đang gọi tới."
            tts.speak(textToSpeak, android.speech.tts.TextToSpeech.QUEUE_ADD, null, "RingtoneTTS")
        } else {
            if (ringtonePlayer.isPlaying) ringtonePlayer.stop()
            if (callState == "IDLE") isMicroMuted = false
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        android.util.Log.d("MAP_DEBUG", "🔄 Màn hình Map được mở lại - Tiến hành đồng bộ làm sạch dữ liệu...")

        // Luôn gọi API làm mới danh sách sự cố để loại bỏ những gì đã bị xóa/hủy dưới DB
        mapViewModel.loadSuCoForMap(context)
        mapViewModel.loadTruSoData(context)
        mapViewModel.loadCameraData(context)

        // Khởi chạy lại kết nối đàm thoại và socket nếu bị sập nửa chừng
        callViewModel.start(context, stompClient, webrtcViewModel)
        mapViewModel.startRealtimeSocket(context, stompClient)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (!audioGranted) {
            Toast.makeText(context, "Bạn cần cấp quyền Micro để đàm thoại!", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
    }

    val positionListener = remember(suCoList) {
        OnIndicatorPositionChangedListener { point ->
            lastUserLocation = point
            alertViewModel.checkProximityAndAlert(point, suCoList)
        }
    }

    LaunchedEffect(lastUserLocation, showRangeCircle) {
        val manager = polygonManager ?: return@LaunchedEffect
        manager.deleteAll()
        if (showRangeCircle && lastUserLocation != null) {
            val circlePoints = createCirclePoints(lastUserLocation!!, 100.0)
            val polygonOptions = PolygonAnnotationOptions()
                .withPoints(listOf(circlePoints))
                .withFillColor("#4285F4")
                .withFillOpacity(0.15)
                .withFillOutlineColor("#4285F4")
            manager.create(polygonOptions)
        }
    }

    LaunchedEffect(truSoWithIcons, truSoManager, showTruSo) {
        val manager = truSoManager ?: return@LaunchedEffect
        manager.deleteAll()
        if (showTruSo) {
            val options = truSoWithIcons.map { (ts, bmp) ->
                PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(ts.kinhDo, ts.viDo))
                    .withIconImage(bmp)
                    .withIconSize(1.0)
                    .withTextField(ts.tenTruSo)
                    .withTextOffset(listOf(0.0, 2.0))
                    .withTextColor(android.graphics.Color.rgb(0, 82, 204))
                    .withTextSize(12.0)
                    .withTextHaloColor(android.graphics.Color.WHITE)
                    .withTextHaloWidth(2.0)
            }
            manager.create(options)
        }
    }

    LaunchedEffect(cameraWithIcons, cameraManager, showCamera) {
        val manager = cameraManager ?: return@LaunchedEffect
        manager.deleteAll()
        if (showCamera) {
            val options = cameraWithIcons.map { (cam, bmp) ->
                PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(cam.kinhDo, cam.viDo))
                    .withIconImage(bmp)
                    .withIconSize(1.0)
                    .withTextField(cam.tenCamera)
                    .withTextSize(10.0)
                    .withTextOffset(listOf(0.0, 2.0))
                    .withTextColor(android.graphics.Color.DKGRAY)
                    .withTextHaloColor(android.graphics.Color.WHITE)
                    .withTextHaloWidth(1.5)
            }
            manager.create(options)
            mapView?.invalidate()
        }
    }

    LaunchedEffect(suCoWithIcons, pointManager, showSuCo) {
        val manager = pointManager ?: return@LaunchedEffect
        try {
            // 💡 BƯỚC 1: Xóa sạch bách toàn bộ marker cũ trên bản đồ trước để giải phóng bộ nhớ Native
            manager.deleteAll()

            // Nếu bộ lọc đang bật và danh sách thực sự có phần tử thì mới vẽ mới
            if (showSuCo && suCoWithIcons.isNotEmpty()) {
                val optionsList = suCoWithIcons.map { (suCo, bmp) ->
                    PointAnnotationOptions()
                        .withPoint(Point.fromLngLat(suCo.kinhDo, suCo.viDo))
                        .withIconImage(bmp)
                }

                // 💡 BƯỚC 2: Tạo mới hoàn toàn dựa trên danh sách đã được loại bỏ phần tử xóa
                manager.create(optionsList)
            }

            // 💡 BƯỚC 3: Ép buộc Mapbox phải vẽ lại view ngay lập tức trong chu kỳ frame này
            mapView?.invalidate()

        } catch (e: Exception) {
            android.util.Log.e("MAP_ERROR", "Lỗi khi cập nhật Marker: ${e.message}")
        }
    }
    LaunchedEffect(allRoutes, selectedIndex) {
        polylineManager?.deleteAll()
        allRoutes.forEachIndexed { idx, pts ->
            val color = if (idx == selectedIndex) "#4285F4" else "#888888"
            polylineManager?.create(PolylineAnnotationOptions().withPoints(pts).withLineColor(color).withLineWidth(5.0))
        }
    }

    var isPressingSOS by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(5) }
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator

    val sosScale by animateFloatAsState(
        targetValue = if (isPressingSOS) 2.5f else 1f,
        animationSpec = if (isPressingSOS) tween(5000, easing = LinearEasing) else tween(300),
        label = "SOSScale"
    )
    val sosBlurAlpha by animateFloatAsState(
        targetValue = if (isPressingSOS) 0.6f else 0f,
        animationSpec = tween(500), label = "BlurAlpha"
    )

    DisposableEffect(Unit) {
        onDispose {
            // 1. Tắt giọng nói nói chung (Code cũ của bạn)
            tts.shutdown()

            // 2. ✅ THÊM VÀO ĐÂY: Khai tử luồng định vị ngầm ngay khi thoát màn hình Map!
            try {
                mapViewModel.stopLocationUpdates()
                android.util.Log.w("GPS_DEBUG", "🧹 Đã giải phóng luồng GPS đồng thời với TTS thành công!")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Map, null) }, label = { Text("Bản đồ") })
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        if (isLoggedIn) onReportClick()
                        else Toast.makeText(context, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
                    },
                    icon = { Icon(Icons.Default.AddAlert, null, tint = if (isLoggedIn) Color(0xFFD32F2F) else Color.Gray) },
                    label = { Text("Báo cáo") }
                )
                NavigationBarItem(selected = false, onClick = { navController.navigate("home") }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Cá nhân") })
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        mapboxMap.setCamera(CameraOptions.Builder().center(Point.fromLngLat(106.6297, 10.8231)).zoom(12.0).build())
                        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) { style ->
                            mapboxMap.addOnMapClickListener {
                                focusManager.clearFocus()
                                true
                            }
                            pointManager = annotations.createPointAnnotationManager()
                            polylineManager = annotations.createPolylineAnnotationManager()
                            truSoManager = annotations.createPointAnnotationManager()
                            cameraManager = annotations.createPointAnnotationManager()
                            polygonManager = annotations.createPolygonAnnotationManager()

                            pointManager?.addClickListener(OnPointAnnotationClickListener { annotation ->
                                suCoWithIcons.find { it.first.kinhDo == annotation.point.longitude() && it.first.viDo == annotation.point.latitude() }?.first?.let { mapSuCo ->
                                    // 🔍 Tìm phần tử trong suCoList có ID trùng với marker vừa click
                                    val foundSuCo = suCoList.find { it.id == mapSuCo.id } ?: mapSuCo

                                    // ✅ Chuyển đổi tường minh sang UserSuCoDetailResponseDTO để gán vào State
                                    selectedSuCoDetail = UserSuCoDetailResponseDTO(
                                        id = foundSuCo.id,
                                        viDo = foundSuCo.viDo,
                                        kinhDo = foundSuCo.kinhDo,
                                        moTa = "Không có mô tả chi tiết", // Dữ liệu mặc định vì DTO này không có mô tả
                                        tenLoai = "Sự cố giao thông",      // Gán tên tạm thời cho tiêu đề sheet
                                        iconUrl = foundSuCo.iconUrl,
                                        trangThaiDuyet = null,
                                        trangThaiXuLy = foundSuCo.trangThaiXuLy,
                                        mucDoNghiemTrong = foundSuCo.mucDoNghiemTrong,
                                        hinhAnhUrl = null,
                                        doTinCay = null,
                                        tenNguoiBao = null,
                                        diaChi = null,
                                        thoiGianTao = null
                                    )

                                    selectedMarkerType = "SU_CO"
                                    showBottomSheet = true
                                }
                                true
                            })

                            truSoManager?.addClickListener(OnPointAnnotationClickListener { annotation ->
                                truSoWithIcons.find { it.first.kinhDo == annotation.point.longitude() && it.first.viDo == annotation.point.latitude() }?.first?.let { item ->
                                    selectedTruSo = item
                                    selectedMarkerType = "TRU_SO"
                                    showBottomSheet = true
                                }
                                true
                            })

                            cameraManager?.addClickListener(OnPointAnnotationClickListener { annotation ->
                                cameraWithIcons.find { it.first.kinhDo == annotation.point.longitude() && it.first.viDo == annotation.point.latitude() }?.first?.let { item ->
                                    selectedCamera = item
                                    selectedMarkerType = "CAMERA"
                                    showBottomSheet = true
                                }
                                true
                            })
                        }

                        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            location.updateSettings { enabled = true; pulsingEnabled = true }
                            location.addOnIndicatorBearingChangedListener { bearing -> userHeading = bearing.toFloat() }
                            location.addOnIndicatorPositionChangedListener(positionListener)
                        }
                        mapView = this
                    }
                }
            )

            Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                if (routePoints.isEmpty()) {
                    SearchUI(
                        searchText = searchText,
                        onValueChange = {
                            searchText = it
                            searchViewModel.onSearchQueryChanged(it)
                        },
                        suggestions = suggestions,
                        onSelected = { name ->
                            searchViewModel.getPointForName(name)?.let { pt ->
                                mapView?.camera?.easeTo(
                                    CameraOptions.Builder().center(pt).zoom(15.0).build(),
                                    MapAnimationOptions.mapAnimationOptions { duration(1200) }
                                )
                            }
                            focusManager.clearFocus()
                        },
                        onMyLocationClick = {
                            lastUserLocation?.let { currentPoint ->
                                mapView?.camera?.easeTo(
                                    CameraOptions.Builder().center(currentPoint).zoom(16.0).build(),
                                    MapAnimationOptions.mapAnimationOptions { duration(1000) }
                                )
                            } ?: Toast.makeText(context, "Đang chờ GPS...", Toast.LENGTH_SHORT).show()
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            modifier = Modifier.weight(1f),
                            selected = showSuCo,
                            onClick = { mapViewModel.toggleFilter("SU_CO") },
                            label = { Text("Sự cố", fontSize = 11.sp, maxLines = 1) },
                            leadingIcon = { Icon(Icons.Default.Warning, null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(containerColor = Color.White, labelColor = Color.Red, iconColor = Color.Red, selectedContainerColor = Color.Red, selectedLabelColor = Color.White),
                            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = showSuCo, borderColor = Color.Red, borderWidth = 1.dp)
                        )

                        FilterChip(
                            modifier = Modifier.weight(1f),
                            selected = showTruSo,
                            onClick = { mapViewModel.toggleFilter("TRU_SO") },
                            label = { Text("Trụ sở", fontSize = 11.sp, maxLines = 1) },
                            leadingIcon = { Icon(Icons.Default.Business, null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(containerColor = Color.White, labelColor = Color(0xFF2196F3), iconColor = Color(0xFF2196F3), selectedContainerColor = Color(0xFF2196F3), selectedLabelColor = Color.White),
                            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = showTruSo, borderColor = Color(0xFF2196F3), borderWidth = 1.dp)
                        )

                        FilterChip(
                            modifier = Modifier.weight(1f),
                            selected = showCamera,
                            onClick = { mapViewModel.toggleFilter("CAMERA") },
                            label = { Text("Camera", fontSize = 11.sp, maxLines = 1) },
                            leadingIcon = { Icon(Icons.Default.Videocam, null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(containerColor = Color.White, labelColor = Color.Black, iconColor = Color.Black, selectedContainerColor = Color.Black, selectedLabelColor = Color.White),
                            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = showCamera, borderColor = Color.Black, borderWidth = 1.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 24.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { showRangeCircle = !showRangeCircle },
                    containerColor = if (showRangeCircle) Color(0xFF4285F4) else Color.White,
                    contentColor = if (showRangeCircle) Color.White else Color.Gray,
                    shape = CircleShape
                ) { Icon(Icons.Default.BlurCircular, "Vòng 100m") }

                SmallFloatingActionButton(
                    onClick = { alertViewModel.toggleSound() },
                    containerColor = if (isSoundOn) Color(0xFF4CAF50) else Color.White,
                    contentColor = if (isSoundOn) Color.White else Color.Gray,
                    shape = CircleShape
                ) { Icon(if (isSoundOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff, "Giọng nói") }

                FloatingActionButton(
                    onClick = { navController.navigate("di_chuyen") },
                    containerColor = Color(0xFF4285F4),
                    contentColor = Color.White,
                    shape = CircleShape
                ) { Icon(Icons.Default.Directions, "Chỉ đường") }
            }

            if (routePoints.isNotEmpty()) {
                RouteInfoUI(mode = mode, icon = modeIcon, duration = duration, distance = distance, onBack = { mapViewModel.clearRoute() }, onStartNav = {})
            }

            sosMessage?.let { message ->
                ElevatedCard(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp, start = 16.dp, end = 16.dp).fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.elevatedCardElevation(8.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "HỆ THỐNG PHẢN HỒI", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = Color.Black, fontWeight = FontWeight.Medium)

                            if (handlingTruSoId != null && callState == "IDLE") {
                                Button(
                                    onClick = {
                                        val rawId = handlingTruSoId!!
                                        val targetId = if (rawId.startsWith("TRU_SO_")) rawId else "TRU_SO_$rawId"
                                        if (stompClient.isConnected) {
                                            webrtcViewModel.startCall(stompClient, targetId, maThietBi)
                                        } else {
                                            Toast.makeText(context, "Kết nối máy chủ bận, hãy thử lại", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                ) {
                                    Icon(Icons.Default.Call, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("GỌI CHO TRỤ SỞ TIẾP NHẬN")
                                }
                            }
                        }
                        IconButton(onClick = {
                            webrtcViewModel.endCall(stompClient, maThietBi)
                            sosViewModel.clear()
                        }) {
                            Icon(Icons.Default.CallEnd, null, tint = Color.Red)
                        }
                    }
                }
            }

            if (isPressingSOS) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = sosBlurAlpha)).zIndex(5f).pointerInput(Unit) {})
                Box(modifier = Modifier.fillMaxSize().zIndex(6f), contentAlignment = Alignment.Center) {
                    Text(
                        text = countdown.toString(),
                        modifier = Modifier.offset(y = (-100).dp),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 130.sp, fontWeight = FontWeight.Black, color = Color.Red)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
                    .size(80.dp)
                    .scale(sosScale)
                    .background(if (isPressingSOS) Color.Red else Color(0xFFD32F2F), CircleShape)
                    .zIndex(7f)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressingSOS = true
                                countdown = 5
                                val timerJob = coroutineScope.launch {
                                    for (i in 5 downTo 1) { countdown = i; delay(1000) }
                                }
                                val startTime = System.currentTimeMillis()
                                try {
                                    val released = try { awaitRelease(); true } catch (e: Exception) { false }
                                    val duration = System.currentTimeMillis() - startTime
                                    timerJob.cancel()
                                    if (released && duration >= 5000) {
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                            vibrator.vibrate(android.os.VibrationEffect.createOneShot(500, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                                        } else { vibrator.vibrate(500) }
                                        val lat = lastUserLocation?.latitude() ?: 0.0
                                        val lng = lastUserLocation?.longitude() ?: 0.0
                                        if (lat != 0.0) navController.navigate("tin_hieu_sos/$lat/$lng")
                                    } else if (released) {
                                        Toast.makeText(context, "Giữ thêm một chút nữa!", Toast.LENGTH_SHORT).show()
                                    }
                                } finally { isPressingSOS = false; countdown = 5 }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = if (isPressingSOS) "THẢ ĐỂ GỬI" else "SOS", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = if (isPressingSOS) 10.sp else 18.sp)
            }

            if (callState != "IDLE") {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).zIndex(10f).pointerInput(Unit) { detectTapGestures {} },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
                        val pulseScale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 1.25f, animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "Scale")

                        Box(contentAlignment = Alignment.Center) {
                            Box(Modifier.size(140.dp).scale(pulseScale).background(if (callState == "CONNECTED") Color.Green.copy(0.2f) else Color.Red.copy(0.2f), CircleShape))
                            Surface(shape = CircleShape, color = if (callState == "CONNECTED") Color(0xFF4CAF50) else Color(0xFFFF9800), modifier = Modifier.size(100.dp), shadowElevation = 10.dp) {
                                Icon(imageVector = if (callState == "CONNECTED") Icons.Default.Call else Icons.Default.RingVolume, contentDescription = null, modifier = Modifier.padding(25.dp).size(50.dp), tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        Text(text = (callerName ?: "TRỤ SỞ CỨU HỘ").uppercase(), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                        Text(text = when(callState) { "INCOMING" -> "CUỘC GỌI ĐẾN..."; "CONNECTED" -> "ĐANG ĐÀM THOẠI"; else -> "ĐANG KẾT NỐI" }, color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(80.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            ActionCallButton(icon = Icons.Default.CallEnd, label = if (callState == "INCOMING") "Từ chối" else "Kết thúc", color = Color.Red, onClick = { webrtcViewModel.endCall(stompClient, maThietBi) })
                        }
                    }
                }
            }

            // =========================================================================
            // ✅ THÀNH PHẦN CHÍNH: MODAL BOTTOM SHEET PHÂN LOẠI CHI TIẾT (MATERIAL 3)
            // =========================================================================
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false), // Vuốt trượt nửa màn hình linh hoạt
                    containerColor = Color.White,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(start = 24.dp, end = 24.dp, bottom = 32.dp, top = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .width(45.dp)
                                .height(5.dp)
                                .background(Color.LightGray, RoundedCornerShape(3.dp))
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        when (selectedMarkerType) {
                            "SU_CO" -> {
                                selectedSuCoDetail?.let { suCo ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(28.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("THÔNG TIN SỰ CỐ", fontSize = 14.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // ✅ ĐÃ SỬA: Dùng suCo.tenLoai thay cho loaiSuCo cũ bị lỗi
                                    Text(text = suCo.tenLoai ?: "Sự cố không xác định", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)

                                    // ✅ ĐÃ SỬA: Trường moTa đã tồn tại hợp lệ trong UserSuCoDetailResponseDTO
                                    Text(text = "Mô tả: ${suCo.moTa ?: "Không có mô tả chi tiết từ người dân."}", color = Color.DarkGray, fontSize = 15.sp, modifier = Modifier.padding(vertical = 4.dp))

                                    Spacer(modifier = Modifier.height(6.dp))
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("Mức độ: ${suCo.mucDoNghiemTrong ?: "Chưa đánh giá"}") },
                                        leadingIcon = { Icon(Icons.Default.ErrorOutline, null, tint = Color.Red) }
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            mapViewModel.setEndPoint(Point.fromLngLat(suCo.kinhDo, suCo.viDo), "Sự cố: ${suCo.tenLoai}")
                                            coroutineScope.launch {
                                                mapViewModel.getFreeDirections()
                                            }
                                            showBottomSheet = false
                                        },
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Navigation, null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("TỚI HIỆN TRƯỜNG SỰ CỐ", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            "TRU_SO" -> {
                                selectedTruSo?.let { truSo ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Business, null, tint = Color(0xFF2196F3), modifier = Modifier.size(28.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("HỆ THỐNG CỨU HỘ", fontSize = 14.sp, color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(text = truSo.tenTruSo, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                // Trigger cuộc gọi thoại qua socket
                                                if (stompClient.isConnected) {
                                                    webrtcViewModel.startCall(stompClient, "TRU_SO_${truSo.id}", maThietBi)
                                                } else {
                                                    Toast.makeText(context, "Mất kết nối máy chủ", Toast.LENGTH_SHORT).show()
                                                }
                                                showBottomSheet = false
                                            },
                                            modifier = Modifier.weight(1f).height(50.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Call, null)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Gọi cứu hộ")
                                        }

                                        Button(
                                            onClick = {
                                                mapViewModel.setEndPoint(Point.fromLngLat(truSo.kinhDo, truSo.viDo), truSo.tenTruSo)
                                                coroutineScope.launch {
                                                    mapViewModel.getFreeDirections()
                                                }
                                                showBottomSheet = false
                                            },
                                            modifier = Modifier.weight(1f).height(50.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Directions, null)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Chỉ đường")
                                        }
                                    }
                                }
                            }

                            "CAMERA" -> {
                                selectedCamera?.let { camera ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Videocam, null, tint = Color.Black, modifier = Modifier.size(28.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("CAMERA AN NINH GIAO THÔNG", fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(text = camera.tenCamera, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)

                                    // 📝 Hiển thị mô tả camera nếu có từ backend
                                    if (!camera.moTa.isNullOrBlank()) {
                                        Text(text = camera.moTa, color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp))
                                    }

                                    // ✅ ĐÃ SỬA: Kiểm tra trạng thái hoạt động dựa vào sự tồn tại của luồng videoUrl
                                    val isLiveAvailable = !camera.videoUrl.isNullOrBlank()
                                    val statusColor = if (isLiveAvailable) Color(0xFF4CAF50) else Color.Red
                                    val statusText = if (isLiveAvailable) "Đang hoạt động (Sẵn sàng kết nối)" else "Ngoại tuyến (Không có luồng phát)"

                                    Text(
                                        text = "Trạng thái: $statusText",
                                        color = statusColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            // ✅ ĐÃ SỬA: Dùng hàm startCall có sẵn trong WebRTCViewModel của bạn
                                            // Truyền chính xác: stompClient, targetId dạng "CAMERA_id", và maThietBi (myId)
                                            webrtcViewModel.startCall(
                                                stompClient = stompClient,
                                                targetId = "CAMERA_${camera.id}",
                                                myId = maThietBi
                                            )
                                            showBottomSheet = false
                                        },
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                        shape = RoundedCornerShape(12.dp),
                                        enabled = isLiveAvailable
                                    ) {
                                        Icon(Icons.Default.LiveTv, null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("XEM STREAM LIVE REALTIME", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUI(
    searchText: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    onSelected: (String) -> Unit,
    onMyLocationClick: () -> Unit
) {
    var active by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchBar(
                query = searchText,
                onQueryChange = onValueChange,
                onSearch = { active = false; onSelected(searchText) },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text("Tìm kiếm vị trí, sự cố...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { onValueChange("") }) { Icon(Icons.Default.Clear, contentDescription = null) }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                suggestions.forEach { suggestion ->
                    Row(
                        modifier = Modifier.fillMaxWidth().pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                onValueChange(suggestion)
                                onSelected(suggestion)
                                active = false
                            })
                        }.padding(16.dp)
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = suggestion)
                    }
                }
            }

            if (!active) {
                Spacer(modifier = Modifier.width(8.dp))
                FloatingActionButton(
                    onClick = onMyLocationClick,
                    containerColor = Color.White,
                    contentColor = Color(0xFF4285F4),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Vị trí của tôi")
                }
            }
        }
    }
}

// --- COMPOSE UI COMPONENT CHI TIẾT SỰ CỐ NHẬN USER_SUCO_DETAIL_RESPONSE_DTO ---
@Composable
fun InfoDetailContent(
    data: UserSuCoDetailResponseDTO,
    userLocation: Point?,
    onNavigate: (Point, String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()).navigationBarsPadding()
    ) {
        val (levelText, advice, color) = when (data.mucDoNghiemTrong?.uppercase()) {
            "HIGH" -> Triple("CAO", "Không nên tiến đến gần!", Color.Red)
            "MEDIUM" -> Triple("TRUNG BÌNH", "Hạn chế khi lưu thông qua khu vực.", Color(0xFFFFA000))
            "LOW" -> Triple("THẤP", "Cảnh giác khi lưu thông.", Color(0xFF2E7D32))
            else -> Triple("KHÔNG XÁC ĐỊNH", "Cẩn thận khi di chuyển.", Color.Gray)
        }

        Text(text = (data.tenLoai ?: data.moTa ?: "SỰ CỐ KHẨN CẤP").uppercase(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = color, modifier = Modifier.padding(bottom = 4.dp))

        Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp), border = androidx.compose.foundation.BorderStroke(1.dp, color)) {
            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.6.dp))
                Text(text = "Mức độ $levelText: $advice", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
            }
        }

        if (!data.diaChi.isNullOrBlank()) {
            Text(text = "Địa điểm: ${data.diaChi}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
        }
        if (!data.tenNguoiBao.isNullOrBlank()) {
            Text(text = "Người báo cáo: ${data.tenNguoiBao} (Độ tin cậy: ${data.doTinCay ?: 0}%)", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onNavigate(Point.fromLngLat(data.kinhDo, data.viDo), data.tenLoai ?: data.moTa ?: "Hiện trường sự cố") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = color)
        ) {
            Icon(Icons.Default.Navigation, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("DẪN ĐƯỜNG ĐẾN HIỆN TRƯỜNG")
        }
    }
}

@Composable
fun ActionCallButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick, modifier = Modifier.size(64.dp).background(color, CircleShape)) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
@Composable
fun RouteInfoUI(mode: String, icon: androidx.compose.ui.graphics.vector.ImageVector, duration: String, distance: String, onBack: () -> Unit, onStartNav: () -> Unit) {
    // Đặt component thiết kế route cũ của bạn ở đây nếu cần thiết
}

fun createCameraIcon(context: Context): Bitmap {
    return try {
        // 1. Đọc trực tiếp file camera.jpg từ res/drawable thành Bitmap
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = false }
        val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.camera, options)

        // 2. Scale kích thước marker cho vừa vặn với bản đồ (ví dụ 90x90 px)
        val targetSize = 90
        Bitmap.createScaledBitmap(originalBitmap, targetSize, targetSize, true)
    } catch (e: Exception) {
        e.printStackTrace()
        // Fallback: Tạo một bitmap trống màu đen nếu lỡ file bị lỗi
        val fallback = Bitmap.createBitmap(70, 70, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(fallback)
        canvas.drawColor(android.graphics.Color.BLACK)
        fallback
    }
}
fun createTruSoMarkerBitmap(context: Context): Bitmap {
    return try {
        // 1. Đọc trực tiếp file tru_so.png từ res/drawable thành Bitmap
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = false }
        val originalBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.tru_so, options)

        // 2. Scale kích thước marker cho vừa vặn với bản đồ (ví dụ 100x100 px)
        val targetSize = 100
        Bitmap.createScaledBitmap(originalBitmap, targetSize, targetSize, true)
    } catch (e: Exception) {
        e.printStackTrace()
        // Fallback: Tạo một bitmap trống màu xanh nếu lỡ file bị lỗi/không tìm thấy để tránh crash app
        val fallback = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(fallback)
        canvas.drawColor(android.graphics.Color.BLUE)
        fallback
    }
}
fun createCirclePoints(center: Point, radiusInMeters: Double): List<Point> {
    val degreesBetweenPoints = 8
    val numberOfPoints = 360 / degreesBetweenPoints
    val distRadians = radiusInMeters / 6371008.8
    val centerLatRadians = Math.toRadians(center.latitude())
    val centerLonRadians = Math.toRadians(center.longitude())
    val points = mutableListOf<Point>()

    for (index in 0 until numberOfPoints) {
        val angle = Math.toRadians((index * degreesBetweenPoints).toDouble())
        val latitudeRadians = Math.asin(Math.sin(centerLatRadians) * Math.cos(distRadians) + Math.cos(centerLatRadians) * Math.sin(distRadians) * Math.cos(angle))
        val longitudeRadians = centerLonRadians + Math.atan2(Math.sin(angle) * Math.sin(distRadians) * Math.cos(centerLatRadians), Math.cos(distRadians) - Math.sin(centerLatRadians) * Math.sin(latitudeRadians))
        points.add(Point.fromLngLat(Math.toDegrees(longitudeRadians), Math.toDegrees(latitudeRadians)))
    }
    points.add(points[0])
    return points
}
