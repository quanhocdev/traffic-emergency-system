document.addEventListener("DOMContentLoaded", function () {
  const logoutBtn = document.getElementById("truSoLogoutBtn");

  if (logoutBtn) {
    logoutBtn.addEventListener("click", async function (e) {
      e.preventDefault();

      if (!confirm("Bạn có chắc chắn muốn đăng xuất?")) {
        return;
      }

      try {
        console.log("CALL LOGOUT");

        const res = await fetch("/logout", {
          method: "POST",
          credentials: "include",
        });

        console.log("LOGOUT STATUS:", res.status);

        if (res.ok) {
          window.location.href = "/truso/login";
        } else {
          alert("Đăng xuất thất bại!");
        }
      } catch (err) {
        console.error("LOGOUT ERROR:", err);

        alert("Không thể kết nối đến máy chủ.");
      }
    });
  }
});
