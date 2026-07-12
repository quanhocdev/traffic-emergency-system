package com.example.canhbao.ui.screens.theodoi.tien.thongke

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.canhbao.data.model.tien.vinhdanh.VinhDanhDTO
import com.example.canhbao.viewmodel.tienich.FilterMode
import com.example.canhbao.viewmodel.tienich.ThongKeQuyViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThongKeQuyScreen(
    navController: NavController,
    viewModel: ThongKeQuyViewModel = viewModel()
) {
    val context = LocalContext.current
    val thongKe = viewModel.thongKe
    var expandedMenu by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            viewModel.selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            viewModel.fetchThongKe()
            // Bấm chọn lọc ngày cụ thể sẽ chuyển sang màn hình Quyên góp cộng đồng
            navController.navigate("quyen_gop_cong_dong")
        },
        viewModel.selectedDate.year,
        viewModel.selectedDate.monthValue - 1,
        viewModel.selectedDate.dayOfMonth
    )

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("BẢNG VINH DANH QUỸ", fontWeight = FontWeight.Black, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- TỔNG TIỀN QUỸ ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Tổng giá trị quỹ thiện nguyện", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${String.format("%,d", thongKe.tongGiaTri)}đ",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD32F2F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- THANH ĐIỀU KHIỂN BỘ LỌC ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bảng đóng góp công khai 🏆", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                Box {
                    TextButton(onClick = { expandedMenu = true }) {
                        val label = when (viewModel.filterMode) {
                            FilterMode.DAY -> "Ngày"
                            FilterMode.MONTH -> "Tháng"
                            FilterMode.YEAR -> "Năm"
                        }
                        Text(label, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1976D2))
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFF1976D2))
                    }
                    DropdownMenu(expanded = expandedMenu, onDismissRequest = { expandedMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Theo Ngày (Xem chi tiết đóng góp)") },
                            onClick = {
                                viewModel.filterMode = FilterMode.DAY
                                expandedMenu = false
                                datePickerDialog.show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Theo Tháng") },
                            onClick = { viewModel.filterMode = FilterMode.MONTH; expandedMenu = false; viewModel.fetchThongKe() }
                        )
                        DropdownMenuItem(
                            text = { Text("Theo Năm") },
                            onClick = { viewModel.filterMode = FilterMode.YEAR; expandedMenu = false; viewModel.fetchThongKe() }
                        )
                    }
                }
            }

            // Thanh điều hướng thời gian nhanh
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = { viewModel.prevPeriod() }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Trước", tint = Color(0xFF1976D2))
                }

                Surface(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        val timeText = when (viewModel.filterMode) {
                            FilterMode.DAY -> viewModel.selectedDate.format(dateFormatter)
                            FilterMode.MONTH -> "Tháng " + viewModel.selectedDate.format(DateTimeFormatter.ofPattern("MM / yyyy"))
                            FilterMode.YEAR -> "Năm " + viewModel.selectedDate.year.toString()
                        }
                        Text(timeText, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    }
                }

                IconButton(onClick = { viewModel.nextPeriod() }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Sau", tint = Color(0xFF1976D2))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- DANH SÁCH DANH DÂN (Mặc định từ trước tới nay top từ trên xuống) ---
            if (viewModel.isLoading) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1976D2))
                }
            } else if (thongKe.bangVinhDanh.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("Không có dữ liệu vinh danh", color = Color.Gray, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(horizontal = 16.dp)
                ) {
                    items(thongKe.bangVinhDanh) { item ->
                        VinhDanhListRow(item)
                    }
                }
            }
        }
    }
}

@Composable
fun VinhDanhListRow(item: VinhDanhDTO) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    item.tenHienThi.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }

            Spacer(Modifier.width(16.dp))

            Text(
                text = item.tenHienThi,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Text(
                text = "${String.format("%,d", item.tongDongGop)}đ",
                color = Color(0xFF388E3C),
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        }
        HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 1.dp)
    }
}