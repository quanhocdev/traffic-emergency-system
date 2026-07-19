// Thêm sự kiện lắng nghe nút Đăng xuất sau khi DOM đã tải xong
document.addEventListener("DOMContentLoaded", function () {
  const logoutBtn = document.getElementById("adminLogoutBtn");

  if (logoutBtn) {
    logoutBtn.addEventListener("click", function (e) {
      // Chặn form gửi đi ngay lập tức để hiện confirm trước
      e.preventDefault();

      if (confirm("Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?")) {
        // Nếu chọn OK, tìm thẻ form cha bao quanh nút này và submit thuần backend
        const form = logoutBtn.closest("form");
        if (form) {
          form.submit();
        }
      }
      // Nếu chọn Cancel, không làm gì cả, form sẽ không bị gửi đi
    });
  }
});

// Hàm toggle cũ của bạn giữ nguyên
function toggleSidebar() {
  const sidebar = document.getElementById("sidebar");
  const icon = document.getElementById("toggle-icon");

  sidebar.classList.toggle("collapsed");

  if (sidebar.classList.contains("collapsed")) {
    icon.classList.replace("fa-chevron-left", "fa-chevron-right");
  } else {
    icon.classList.replace("fa-chevron-right", "fa-chevron-left");
  }
}
