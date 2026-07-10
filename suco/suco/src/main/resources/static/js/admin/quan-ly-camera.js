function openModal() {
  document.getElementById("cameraModal").style.display = "flex";
}

function closeModal() {
  document.getElementById("cameraModal").style.display = "none";
}

function filterTable() {
  let input = document.getElementById("searchInput").value.toUpperCase();

  let rows = document
    .querySelector("#cameraTable tbody")
    .getElementsByTagName("tr");

  for (let row of rows) {
    let text = row.innerText.toUpperCase();

    row.style.display = text.includes(input) ? "" : "none";
  }
}

/*
    ============================
        CREATE CAMERA
    ============================
*/

function handleFormSubmit(event) {
  event.preventDefault();

  const form = event.target;

  const formData = new FormData(form);

  fetch("/admin/quan-ly-camera/them", {
    method: "POST",

    body: formData,
  })
    .then((response) => {
      if (response.ok) {
        alert("Thêm camera thành công!");

        closeModal();

        window.location.reload();
      } else {
        return response.text().then((txt) => {
          throw new Error(txt);
        });
      }
    })

    .catch((error) => {
      console.error(error);

      alert(error.message);
    });
}

/*
    ============================
        EDIT CAMERA
    ============================
*/

function openEditCamera(id) {
  fetch(`/admin/quan-ly-camera/${id}/detail`)
    .then((response) => {
      if (!response.ok) throw new Error("Không tìm thấy camera");

      return response.json();
    })

    .then((data) => {
      document.getElementById("editCameraId").value = data.id;

      document.getElementById("editTenCamera").value = data.tenCamera ?? "";

      document.getElementById("editKinhDo").value = data.kinhDo ?? "";

      document.getElementById("editViDo").value = data.viDo ?? "";

      document.getElementById("editCameraModal").style.display = "flex";
    })

    .catch((error) => {
      console.error(error);

      alert("Không thể tải camera");
    });
}

function closeEditCamera() {
  document.getElementById("editCameraModal").style.display = "none";
}

function handleEditSubmit(event) {
  event.preventDefault();

  let id = document.getElementById("editCameraId").value;

  let form = document.getElementById("editCameraForm");

  let formData = new FormData(form);

  fetch(`/admin/quan-ly-camera/${id}`, {
    method: "PATCH",

    body: formData,
  })
    .then((response) => {
      if (response.ok) {
        alert("Cập nhật camera thành công");

        closeEditCamera();

        window.location.reload();
      } else {
        return response.text().then((txt) => {
          throw new Error(txt);
        });
      }
    })

    .catch((error) => {
      console.error(error);

      alert(error.message);
    });
}

/*
    ============================
        DETAIL CAMERA
    ============================
*/

function openCameraDetail(id) {
  fetch(`/admin/quan-ly-camera/${id}/detail`)
    .then((response) => {
      if (!response.ok) throw new Error();

      return response.json();
    })

    .then((data) => {
      displayCameraDetail(data);
    })

    .catch((error) => {
      console.error(error);

      alert("Không thể tải thông tin camera");
    });
}

function displayCameraDetail(data) {
  const modal = document.getElementById("cameraDetailModal");

  const videoContainer = document.getElementById("cameraDetailVideoContainer");

  const imageContainer = document.getElementById("cameraDetailImageContainer");

  const video = document.getElementById("cameraDetailVideo");

  const videoSource = document.getElementById("videoSource");

  const image = document.getElementById("cameraDetailImage");

  document.getElementById("detailCameraName").textContent =
    data.tenCamera ?? "Camera";

  document.getElementById("detailName").textContent = data.tenCamera ?? "-";

  document.getElementById("detailDiaChi").textContent =
    data.diaChi ?? "Chưa xác định";

  let kinhDo = data.kinhDo && data.kinhDo != 0 ? data.kinhDo : null;

  let viDo = data.viDo && data.viDo != 0 ? data.viDo : null;

  document.getElementById("detailKinhDo").textContent = kinhDo ?? "Chưa có";

  document.getElementById("detailViDo").textContent = viDo ?? "Chưa có";

  if (data.videoUrl) {
    videoSource.src = data.videoUrl;

    video.load();

    videoContainer.style.display = "block";

    imageContainer.style.display = "none";
  } else if (data.anhCamera) {
    image.src = data.anhCamera;

    imageContainer.style.display = "block";

    videoContainer.style.display = "none";
  } else {
    videoContainer.style.display = "none";

    imageContainer.style.display = "none";
  }

  const mapLink = document.getElementById("detailMapLink");

  if (kinhDo && viDo) {
    mapLink.href = `https://www.google.com/maps?q=${viDo},${kinhDo}`;

    mapLink.style.display = "inline";
  } else {
    mapLink.style.display = "none";
  }

  modal.style.display = "flex";
}

function closeCameraDetail() {
  const modal = document.getElementById("cameraDetailModal");

  const video = document.getElementById("cameraDetailVideo");

  if (video) {
    video.pause();

    video.currentTime = 0;
  }

  modal.style.display = "none";
}

/*
    ============================
        DELETE CAMERA
    ============================
*/

function confirmDelete(id) {
  if (!confirm("Bạn có chắc chắn muốn xóa camera này?")) return;

  fetch(`/admin/quan-ly-camera/${id}`, {
    method: "DELETE",
  })
    .then((response) => {
      if (response.ok) {
        alert("Xóa camera thành công!");

        window.location.reload();
      } else {
        return response.text().then((txt) => {
          throw new Error(txt);
        });
      }
    })

    .catch((error) => {
      console.error(error);

      alert(error.message);
    });
}

/*
    ============================
        MAP PICKER
    ============================
*/

function openMapPicker() {
  window.open(
    "/admin/trang-chu?pickLocation=true",
    "_blank",
    "width=1200,height=800",
  );

  window.addEventListener("message", function (event) {
    if (event.data.type === "locationPicked") {
      document.getElementById("kinhDo").value = event.data.lng;

      document.getElementById("viDo").value = event.data.lat;
    }
  });
}

window.onclick = function (e) {
  const cameraModal = document.getElementById("cameraModal");

  const detailModal = document.getElementById("cameraDetailModal");

  const editModal = document.getElementById("editCameraModal");

  if (e.target === cameraModal) closeModal();

  if (e.target === detailModal) closeCameraDetail();

  if (e.target === editModal) closeEditCamera();
};
