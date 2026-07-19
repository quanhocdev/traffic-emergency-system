function toggleSidebar() {
  const sidebar = document.getElementById("sidebar");
  const icon = document.getElementById("toggle-icon");

  sidebar.classList.toggle("collapsed");

  if (sidebar.classList.contains("collapsed")) {
    icon.classList.replace("fa-chevron-left", "fa-chevron-right");
  } else {
    icon.classList.replace("fa-chevron-right", "fa-chevron-left");
  }

  // FIX MAPBOX RESIZE
  setTimeout(() => {
    if (window.map) {
      window.map.resize();
    }
  }, 310);
}
async function logoutTruSo() {
  const ok = confirm("Bạn có chắc muốn đăng xuất?");
  if (!ok) return;

  try {
    const res = await fetch("/truso/logout", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
    });

    if (res.ok) {
      window.location.href = "/truso/login";
    } else {
      alert("Không thể đăng xuất!");
    }
  } catch (e) {
    console.error("Logout error:", e);
    alert("Đã xảy ra lỗi khi đăng xuất!");
  }
}
