// Thêm sự kiện lắng nghe nút Đăng xuất sau khi DOM đã tải xong
document.addEventListener("DOMContentLoaded", function () {
  const logoutBtn = document.getElementById("adminLogoutBtn");

  if (logoutBtn) {
    logoutBtn.addEventListener("click", async function (e) {
      e.preventDefault();

      if (!confirm("Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?")) {
        return;
      }

      try {
        const res = await fetch("/logout", {
          method: "POST",
          credentials: "include",
        });

        if (res.ok) {
          window.location.href = "/admin/login";
        } else {
          alert("Đăng xuất thất bại!");
        }
      } catch (err) {
        console.error(err);
        alert("Không thể kết nối đến máy chủ.");
      }
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
