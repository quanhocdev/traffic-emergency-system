// ==============================
// BIẾN MODAL BOOTSTRAP
// ==============================

let editModal;

document.addEventListener("DOMContentLoaded", function () {
  const modalElement = document.getElementById("editModal");

  if (modalElement) {
    editModal = new bootstrap.Modal(modalElement);
  }

  // CREATE FORM

  const createForm = document.getElementById("createForm");

  if (createForm) {
    createForm.addEventListener("submit", function (e) {
      e.preventDefault();

      const formData = new FormData(this);

      fetch("/admin/quan-ly-loai-su-co", {
        method: "POST",

        body: formData,
      })
        .then((res) => {
          if (res.ok) {
            alert("Tạo thành công!");

            location.reload();
          } else {
            return res.json().then((data) => {
              alert(data.message || "Có lỗi xảy ra");
            });
          }
        })

        .catch((err) => {
          console.error(err);

          alert("Lỗi kết nối server");
        });
    });
  }
});

// ==============================
// MỞ MODAL EDIT
// ==============================

function openEdit(btn) {
  const id = btn.dataset.id;

  const ten = btn.dataset.ten;

  const icon = btn.dataset.icon;

  document.getElementById("editId").value = id;

  document.getElementById("editTen").value = ten;

  const preview = document.getElementById("previewIcon");

  if (icon) {
    preview.src = icon;

    preview.classList.remove("d-none");
  } else {
    preview.classList.add("d-none");
  }

  // mở bootstrap modal

  editModal.show();
}

// ==============================
// ĐÓNG MODAL
// ==============================

function closeEdit() {
  editModal.hide();
}

// ==============================
// PREVIEW ICON MỚI
// ==============================

document.addEventListener("change", function (e) {
  if (e.target.id === "editFile") {
    const file = e.target.files[0];

    if (file) {
      const img = document.getElementById("previewIcon");

      img.src = URL.createObjectURL(file);

      img.classList.remove("d-none");
    }
  }
});

// ==============================
// UPDATE
// ==============================

function submitEdit() {
  const id = document.getElementById("editId").value;

  const ten = document.getElementById("editTen").value;

  const file = document.getElementById("editFile").files[0];

  const formData = new FormData();

  formData.append("ten", ten);

  if (file) {
    formData.append("iconFile", file);
  }

  fetch(`/admin/quan-ly-loai-su-co/${id}`, {
    method: "PATCH",

    body: formData,
  })
    .then((res) => {
      if (res.ok) {
        alert("Cập nhật thành công!");

        location.reload();
      } else {
        return res.json().then((data) => {
          alert(data.message || "Cập nhật thất bại");
        });
      }
    })

    .catch((err) => {
      console.error(err);

      alert("Lỗi kết nối server");
    });
}

// ==============================
// DELETE
// ==============================

function deleteSuCo(id) {
  if (!confirm("Bạn có chắc muốn xóa loại sự cố này?")) return;

  fetch(`/admin/quan-ly-loai-su-co/${id}`, {
    method: "DELETE",
  })
    .then((res) => {
      if (res.ok) {
        alert("Xóa thành công!");

        location.reload();
      } else {
        alert("Xóa thất bại!");
      }
    })

    .catch((err) => {
      console.error(err);

      alert("Lỗi kết nối server");
    });
}
