package com.example.canhbao.ui.screens.theodoi.tien.thongke

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.canhbao.viewmodel.tienich.ThongKeQuyViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuyenGopCongDongScreen(
    navController: NavController,
    viewModel: ThongKeQuyViewModel = viewModel()
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    // Sắp xếp danh sách đóng góp từ sớm đến trễ theo yêu cầu
    val sortedList = viewModel.thongKe.bangVinhDanh.sortedBy { it.tongDongGop }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "QUYÊN GÓP NGÀY ${viewModel.selectedDate.format(formatter)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                },
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
                .padding(16.dp)
        ) {
            Text(
                text = "Danh sách tấm lòng vàng đóng góp trong ngày. Sắp xếp từ sớm đến trễ dựa vào luồng xử lý thời gian thực.",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (sortedList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không có hoạt động quyên góp nào vào ngày này.", color = Color.Gray, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(horizontal = 16.dp)
                ) {
                    items(sortedList) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.tenHienThi, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(text = "Đã quyên góp quỹ cộng đồng", fontSize = 12.sp, color = Color.Gray)
                            }
                            Text(
                                text = "+${String.format("%,d", item.tongDongGop)}đ",
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF2E7D32),
                                fontSize = 16.sp
                            )
                        }
                        HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 1.dp)
                    }
                }
            }
        }
    }
}