document
  .getElementById("loginForm")
  .addEventListener("submit", async function (e) {
    e.preventDefault();

    const form = e.target;
    const errorBox = document.getElementById("error");

    errorBox.style.display = "none";

    try {
      const res = await fetch("/admin/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          email: form.email.value,
          password: form.password.value,
        }),
      });

      // Backend mới của chúng ta luôn trả về JSON dạng: { "message": "..." } hoặc { "token": "..." }
      const data = await res.json();

      if (res.ok) {
        // [ĐÃ SỬA] Bỏ dòng lưu localStorage cũ đi.
        // Token lúc này nằm trong Cookie HttpOnly "accessToken", trình duyệt tự lo hết rồi nhé.

        // Chuyển thẳng sang trang chủ quản trị admin
        window.location.href = "/admin/trang-chu";
      } else {
        errorBox.style.display = "block";
        // [ĐÃ SỬA] Backend luôn trả về JSON nên lấy trực tiếp data.message
        errorBox.innerText = data.message || "Đăng nhập thất bại";
      }
    } catch (err) {
      errorBox.style.display = "block";
      errorBox.innerText = "Lỗi kết nối server";
    }
  });
