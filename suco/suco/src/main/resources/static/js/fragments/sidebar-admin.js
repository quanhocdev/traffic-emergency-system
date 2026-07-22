// ===========================
// SIDEBAR ADMIN JS
// ===========================

document.addEventListener("DOMContentLoaded", () => {
  const sidebar = document.getElementById("sidebar");
  const toggleBtn = document.getElementById("toggleSidebarBtn");
  const icon = document.getElementById("toggle-icon");

  // ===== Desktop collapse =====
  if (toggleBtn && sidebar) {
    toggleBtn.addEventListener("click", () => {
      sidebar.classList.toggle("collapsed");

      if (sidebar.classList.contains("collapsed")) {
        icon.classList.replace("fa-chevron-left", "fa-chevron-right");
        localStorage.setItem("admin_sidebar_collapsed", "true");
      } else {
        icon.classList.replace("fa-chevron-right", "fa-chevron-left");
        localStorage.setItem("admin_sidebar_collapsed", "false");
      }
    });

    // Khôi phục trạng thái đã lưu
    const saved = localStorage.getItem("admin_sidebar_collapsed");

    if (saved === "true") {
      sidebar.classList.add("collapsed");
      icon.classList.replace("fa-chevron-left", "fa-chevron-right");
    }
  }

  // ===== Mobile toggle =====
  const mobileBtn = document.querySelector(".mobile-toggle");
  const overlay = document.querySelector(".mobile-overlay");

  function closeMobileSidebar() {
    sidebar?.classList.remove("show");
    overlay?.classList.remove("show");
  }

  mobileBtn?.addEventListener("click", () => {
    sidebar?.classList.toggle("show");
    overlay?.classList.toggle("show");
  });

  overlay?.addEventListener("click", closeMobileSidebar);

  // Tự đóng khi bấm menu trên mobile
  document.querySelectorAll("#sidebar .menu-item").forEach((item) => {
    item.addEventListener("click", () => {
      if (window.innerWidth <= 768) {
        closeMobileSidebar();
      }
    });
  });

  // ===== Logout =====
  const logoutBtn = document.getElementById("adminLogoutBtn");

  if (logoutBtn) {
    logoutBtn.addEventListener("click", async (e) => {
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
