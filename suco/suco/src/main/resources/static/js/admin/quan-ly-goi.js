document.getElementById("formGoi").addEventListener("submit", function (e) {
  e.preventDefault();

  const formData = new FormData(this);
  const plainFormData = Object.fromEntries(formData.entries());

  // Ép kiểu dữ liệu số để khớp DTO của Spring Boot
  plainFormData.thoiHan = Number(plainFormData.thoiHan);
  plainFormData.gia = Number(plainFormData.gia);
  plainFormData.khoangCachMienPhi = Number(plainFormData.khoangCachMienPhi);

  fetch("/admin/goi", {
    method: "POST",
    headers: {
      "Content-Type": "application/json", // Bắt buộc để map với @RequestBody
    },
    body: JSON.stringify(plainFormData),
  })
    .then(async (res) => {
      if (res.ok) {
        alert("Tạo thành công!");
        location.reload();
      } else {
        const msg = await res.text();
        alert("Lỗi: " + msg);
      }
    })
    .catch((err) => {
      console.error(err);
      alert("Lỗi kết nối server");
    });
});

// 2. XỬ LÝ XÓA GÓI (DELETE /{id})
function deleteGoi(id) {
  if (!confirm("Xóa gói này?")) return;

  // Gọi đúng method DELETE và truyền ID lên đường dẫn giống Controller
  fetch(`/admin/goi/${id}`, {
    method: "DELETE",
  })
    .then(async (res) => {
      if (res.ok) {
        alert("Xóa thành công!");
        location.reload();
      } else {
        const msg = await res.text();
        alert("Xóa thất bại: " + msg);
      }
    })
    .catch((err) => {
      console.error(err);
      alert("Lỗi server khi xóa");
    });
}

// 3. XỬ LÝ MODAL CHỈNH SỬA & CẬP NHẬT (PATCH)
let editModal;

document.addEventListener("DOMContentLoaded", () => {
  const modalElement = document.getElementById("editModal");
  if (modalElement) {
    editModal = new bootstrap.Modal(modalElement);
  }
});

function openEdit(btn) {
  document.getElementById("editId").value = btn.getAttribute("data-id");
  document.getElementById("editTen").value = btn.getAttribute("data-ten");
  document.getElementById("editThoiHan").value =
    btn.getAttribute("data-thoihan");
  document.getElementById("editGia").value = btn.getAttribute("data-gia");
  document.getElementById("editKhoangCachMienPhi").value =
    btn.getAttribute("data-khoangcach");
  document.getElementById("editUuDai").value = btn.getAttribute("data-uudai");

  editModal.show();
}

function closeEdit() {
  editModal.hide();
}

function submitEdit() {
  const id = document.getElementById("editId").value;

  // Gom dữ liệu từ các input thành object JSON
  const updateData = {
    ten: document.getElementById("editTen").value,
    thoiHan: Number(document.getElementById("editThoiHan").value),
    gia: Number(document.getElementById("editGia").value),
    khoangCachMienPhi: Number(
      document.getElementById("editKhoangCachMienPhi").value,
    ),
    uuDai: document.getElementById("editUuDai").value,
  };

  fetch(`/admin/goi/${id}`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(updateData),
  })
    .then(async (res) => {
      if (res.ok) {
        alert("Cập nhật thành công!");
        location.reload();
      } else {
        const msg = await res.text();
        alert("Cập nhật thất bại: " + msg);
      }
    })
    .catch((err) => {
      console.error(err);
      alert("Lỗi server khi cập nhật");
    });
}
