package com.example.canhbao.ui.screens.theodoi.tien.quyengop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.canhbao.viewmodel.tienich.QuyenGopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuyenGopScreen(
    navController: NavController,
    viewModel: QuyenGopViewModel = viewModel()
) {
    val user = viewModel.userDetail
    val currentPoints = user?.totalPoints ?: 0
    var pointInput by remember { mutableStateOf("") }
    var messageInput by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("QUYÊN GÓP TÍCH LŨY", fontWeight = FontWeight.Black, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("lich_su_quyen_gop_ca_nhan") }) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF1F3F6))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- THỎA ĐIỂM CÁ NHÂN ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFD32F2F), Color(0xFFEF5350))))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Điểm tài khoản khả dụng", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Text(
                        text = currentPoints.toString(),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- QUY TẮC ĐỒNG GÓP ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFD32F2F))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Quy tắc đóng góp", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Tỷ lệ quy đổi: 10 điểm = 1.000đ đóng góp quỹ cộng đồng.", fontSize = 13.sp, color = Color.DarkGray)
                    Text("• Tên cá nhân và giá trị đóng góp sẽ được vinh danh công khai.", fontSize = 13.sp, color = Color.DarkGray)
                    Text("• Mức đóng góp tối thiểu cho một giao dịch là 10 điểm.", fontSize = 13.sp, color = Color.DarkGray)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- XÁC NHẬN QUYÊN GÓP ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Nhập thông tin quyên góp", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pointInput,
                        onValueChange = { pointInput = it.filter { char -> char.isDigit() } },
                        placeholder = { Text("Số điểm (Tối thiểu 10)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        placeholder = { Text("Lời nhắn kèm theo (Không bắt buộc)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (viewModel.message.isNotEmpty()) {
                        Text(text = viewModel.message, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    }

                    Button(
                        onClick = {
                            val pts = pointInput.toIntOrNull()
                            if (pts != null && pts >= 10 && pts <= currentPoints) {
                                viewModel.thucHienQuyenGop(pts, messageInput)
                            } else {
                                showErrorDialog = true
                            }
                        },
                        enabled = !viewModel.isLoading && pointInput.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        if (viewModel.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("Xác nhận quyên góp", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Số điểm không hợp lệ") },
            text = { Text("Vui lòng đảm bảo số điểm quyên góp từ 10 điểm trở lên và không vượt quá số dư tài khoản của bạn.") },
            confirmButton = { TextButton(onClick = { showErrorDialog = false }) { Text("Đã hiểu") } }
        )
    }
}