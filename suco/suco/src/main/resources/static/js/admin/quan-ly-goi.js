const toggleBtn = document.querySelector(".toggle-btn");
const sidebar = document.getElementById("sidebar");

toggleBtn.addEventListener("click", () => {
  sidebar.classList.toggle("collapsed");
});

function deleteGoi(id) {
  if (!confirm("Xóa gói này?")) return;

  fetch("/admin/goi", {
    method: "POST",
    body: formData,
  })
    .then((res) => {
      if (res.ok) {
        alert("Tạo thành công!");
        location.reload();
      } else {
        return res.text().then((msg) => {
          alert(msg);
        });
      }
    })
    .catch((err) => {
      console.error(err);
      alert("Lỗi server");
    });
}

document.getElementById("formGoi").addEventListener("submit", function (e) {
  e.preventDefault();

  const formData = new FormData(this);

  fetch("/admin/goi", {
    method: "POST",
    body: formData,
  })
    .then((res) => res.json())
    .then((data) => {
      alert(data.message);
      location.reload();
    });
});
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

  const formData = new FormData();

  formData.append("ten", document.getElementById("editTen").value);

  formData.append("thoiHan", document.getElementById("editThoiHan").value);

  formData.append("gia", document.getElementById("editGia").value);

  formData.append(
    "khoangCachMienPhi",
    document.getElementById("editKhoangCachMienPhi").value,
  );

  formData.append("uuDai", document.getElementById("editUuDai").value);

  fetch(`/admin/goi/${id}`, {
    method: "PATCH",
    body: formData,
  })
    .then((res) => {
      if (res.ok) {
        alert("Cập nhật thành công!");
        location.reload();
      } else {
        return res.text().then((msg) => {
          alert(msg);
        });
      }
    })
    .catch((err) => {
      console.error(err);
      alert("Lỗi server");
    });
}
