package com.example.canhbao.ui.screens.call

import android.media.Ringtone
import android.media.RingtoneManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.canhbao.viewmodel.call.WebRTCViewModel
@Composable
fun CallScreen(
    navController: NavController,
    webrtcViewModel: WebRTCViewModel
) {
    val context = LocalContext.current
    val callState by webrtcViewModel.callState.collectAsState()
    val callerName by webrtcViewModel.callerName.collectAsState()
    var isMicroMuted by remember { mutableStateOf(false) }

    val maThietBi = remember {
        android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
    }

    DisposableEffect(callState) {
        var ringtone: Ringtone? = null

        if (callState == "INCOMING") {
            try {
                // Lấy đường dẫn nhạc chuông cuộc gọi tiêu chuẩn của hệ thống Android
                val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ringtone = RingtoneManager.getRingtone(context, ringtoneUri)

                // Bật vòng lặp chuông liên tục nếu chạy Android 9 trở lên, hoặc play thông thường
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    ringtone?.isLooping = true
                }
                ringtone?.play()
                android.util.Log.d("CallSound", "🎵 Đang đổ chuông cuộc gọi khẩn cấp...")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Tự động tắt chuông ngay khi chuyển trạng thái (Bấm nghe / Cúp máy / Thoát trang)
        onDispose {
            if (ringtone?.isPlaying == true) {
                ringtone?.stop()
                android.util.Log.w("CallSound", "🔇 Đã tắt chuông cuộc gọi.")
            }
        }
    }

    // TỰ ĐỘNG THOÁT MÀN HÌNH
    LaunchedEffect(callState) {
        if (callState == "IDLE") {
            navController.popBackStack()
        }
    }

    // --- HIỆU ỨNG SÓNG LAN TỎA (RIPPLE ANIMATION) CHO AVATAR ---
    val infiniteTransition = rememberInfiniteTransition(label = "Ripple")
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RippleScale"
    )
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RippleAlpha"
    )

    val backgroundBrush = remember(callState) {
        val baseColors = when (callState) {
            "INCOMING" -> listOf(Color(0xFF1F1C18), Color(0xFF121212))
            "CONNECTED" -> listOf(Color(0xFF111E16), Color(0xFF121212))
            else -> listOf(Color(0xFF1C1D21), Color(0xFF121212))
        }
        Brush.verticalGradient(baseColors)
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(vertical = 60.dp, horizontal = 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Surface(
                    color = when (callState) {
                        "INCOMING" -> Color(0xFFFFB300).copy(alpha = 0.15f)
                        "CONNECTED" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                        else -> Color.White.copy(alpha = 0.1f)
                    },
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = when (callState) {
                            "INCOMING" -> "CUỘC GỌI ĐẾN ĐANG CHỜ"
                            "CONNECTED" -> "ĐANG ĐÀM THOẠI REALTIME"
                            else -> "ĐANG KẾT NỐI MẠNG"
                        },
                        color = if (callState == "INCOMING") Color(0xFFFFC107) else Color(0xFF81C784),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = callerName ?: "Trụ sở cứu hộ",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                // Vòng sóng thứ nhất
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(rippleScale)
                        .background(
                            color = if (callState == "INCOMING") Color(0xFFFFB300).copy(alpha = rippleAlpha)
                            else Color(0xFF4CAF50).copy(alpha = rippleAlpha),
                            shape = CircleShape
                        )
                )
                // Vòng sóng thứ hai trễ nhịp (chỉ hiện khi đổ chuông để tăng tính sống động)
                if (callState == "INCOMING") {
                    val rippleScale2 by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.5f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, delayMillis = 400, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ), label = "RippleScale2"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(rippleScale2)
                            .background(Color(0xFFFFB300).copy(alpha = rippleAlpha), shape = CircleShape)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .background(Color(0xFF222222), CircleShape)
                        .border(3.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = "Trụ sở",
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E).copy(alpha = 0.9f)),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp)
                ) {
                    // NÚT MICROPHONE (Chỉ hiện khi đang đàm thoại CONNECTED)
                    if (callState == "CONNECTED") {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FilledIconButton(
                                onClick = {
                                    isMicroMuted = !isMicroMuted
                                    webrtcViewModel.toggleMic(isMicroMuted)
                                },
                                modifier = Modifier.size(60.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (isMicroMuted) Color(0xFFEF5350) else Color(0xFF37474F),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = if (isMicroMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Mute Mic",
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = if (isMicroMuted) "Mở Mic" else "Tắt Mic", color = Color.Gray, fontSize = 12.sp)
                        }
                    }

                    // NÚT CHẤP NHẬN CUỘC GỌI (Chỉ xuất hiện khi INCOMING)
                    if (callState == "INCOMING") {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FilledIconButton(
                                onClick = { webrtcViewModel.acceptCall( maThietBi) },
                                modifier = Modifier.size(68.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Chấp nhận",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Trả lời", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    // NÚT CÚP MÁY VÀ TỪ CHỐI (Luôn hiển thị)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FilledIconButton(
                            onClick = { webrtcViewModel.endCall( maThietBi) },
                            modifier = Modifier.size(68.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFC62828))
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "Cúp máy",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (callState == "INCOMING") "Từ chối" else "Kết thúc",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}