document.addEventListener("DOMContentLoaded", function () {
  const logoutBtn = document.getElementById("truSoLogoutBtn");

  if (logoutBtn) {
    logoutBtn.addEventListener("click", function (e) {
      e.preventDefault();

      if (confirm("Bạn có chắc chắn muốn đăng xuất?")) {
        const form = logoutBtn.closest("form");
        if (form) {
          form.submit();
        }
      }
    });
  }
});
