/**
 * Logic điều khiển Modal xử lý tín hiệu SOS dành cho Admin
 */

// Mở Modal và nạp dữ liệu từ hàng được chọn
function openAdminModal(btn) {
  const id = btn.getAttribute("data-id");
  const trangThai = btn.getAttribute("data-trangthai");

  document.getElementById("adminSosId").value = id;
  document.getElementById("adminSosStatus").value = trangThai;

  document.getElementById("adminSosModal").style.display = "block";
}

// Đóng Modal công cụ
function closeAdminModal() {
  document.getElementById("adminSosModal").style.display = "none";
}

// Gửi yêu cầu cập nhật trạng thái lên Server qua Rest API (Fetch)
function submitAdminUpdate() {
  const id = document.getElementById("adminSosId").value;
  const targetStatus = document.getElementById("adminSosStatus").value;

  // Tạo cấu trúc URL API tùy theo dự án của bạn (ví dụ: /api/admin/quan-ly-sos/v1/update-status)
  const apiUrl = `/api/admin/quan-ly-sos/${id}/status?trangThai=${targetStatus}`;

  fetch(apiUrl, {
    method: "PUT", // Hoặc PATCH tùy thuộc vào thiết kế RestAPI của bạn
    headers: {
      "Content-Type": "application/json",
    },
  })
    .then((response) => {
      if (response.ok) {
        alert(
          `Cập nhật tín hiệu SOS #${id} sang trạng thái [${targetStatus}] thành công!`,
        );
        closeAdminModal();
        window.location.reload(); // Tải lại trang để cập nhật giao diện hiển thị mới
      } else {
        alert("Có lỗi xảy ra khi cập nhật trạng thái hệ thống.");
      }
    })
    .catch((error) => {
      console.error("Error updating SOS status:", error);
      alert("Không thể kết nối đến máy chủ.");
    });
}

// Đóng modal tự động nếu nhấn chuột ra ngoài vùng Content của Modal
window.onclick = function (event) {
  const modal = document.getElementById("adminSosModal");
  if (event.target === modal) {
    closeAdminModal();
  }
};
