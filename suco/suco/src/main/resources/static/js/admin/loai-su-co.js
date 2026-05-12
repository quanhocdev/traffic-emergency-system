function openEdit(btn) {
  const id = btn.getAttribute("data-id");
  const ten = btn.getAttribute("data-ten");

  document.getElementById("editId").value = id;
  document.getElementById("editTen").value = ten;

  document.getElementById("editModal").style.display = "block";
}
// Đóng modal chỉnh sửa
function closeEdit() {
  document.getElementById("editModal").style.display = "none";
}
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
function submitEdit() {
  const id = document.getElementById("editId").value;
  const ten = document.getElementById("editTen").value;
  const file = document.getElementById("editFile").files[0];

  const formData = new FormData();
  if (ten) formData.append("ten", ten);
  if (file) formData.append("iconFile", file);

  fetch(`/admin/loai-su-co/api/update/${id}`, {
    method: "PUT",
    body: formData,
  }).then((res) => {
    if (res.ok) {
      alert("Cập nhật thành công!");
      location.reload();
    } else {
      res.json().then((data) => alert(data.message));
    }
  });
}
document.getElementById("editFile").onchange = function (e) {
  const file = e.target.files[0];
  if (file) {
    const img = document.getElementById("previewIcon");
    img.src = URL.createObjectURL(file);
    img.style.display = "block";
  }
};
function deleteSuCo(id) {
  if (!confirm("Bạn có chắc muốn xóa loại sự cố này?")) return;

  fetch(`/admin/loai-su-co/delete/${id}`, {
    method: "DELETE",
  })
    .then((response) => {
      if (response.ok) {
        alert("Xóa thành công!");
        location.reload(); // Load lại trang để cập nhật danh sách
      } else {
        alert("Xóa thất bại!");
      }
    })
    .catch((error) => console.error("Error:", error));
}
