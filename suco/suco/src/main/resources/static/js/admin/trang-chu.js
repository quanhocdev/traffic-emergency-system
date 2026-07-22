// --- 1. BIẾN TOÀN CỤC & ĐIỀU KHIỂN BẢN ĐỒ ---
map.addControl(new mapboxgl.NavigationControl(), "bottom-right");

const geocoder = new MapboxGeocoder({
  accessToken: mapboxgl.accessToken,
  mapboxgl: mapboxgl,
  placeholder: "Tìm địa chỉ...",
  countries: "vn",
  marker: false,
});
map.addControl(geocoder, "top-left");

// Marker đỏ để chọn vị trí
let pickerMarker = new mapboxgl.Marker({
  color: "#ff0000",
  draggable: true,
});

let currentMarkers = [];

function saveMapState(lng, lat) {
  localStorage.setItem("map_lng", lng);
  localStorage.setItem("map_lat", lat);
}

function updateLocationFields(lng, lat) {
  const formattedLat = lat.toFixed(6);
  const formattedLng = lng.toFixed(6);
  document
    .querySelectorAll(".lat-field")
    .forEach((el) => (el.value = formattedLat));
  document
    .querySelectorAll(".lng-field")
    .forEach((el) => (el.value = formattedLng));
  saveMapState(lng, lat);
}

// Xử lý chuyển đổi qua lại giữa các Tab Panel bên sườn
const tabs = document.querySelectorAll(".control-tab");
const panels = document.querySelectorAll(".side-panel");

tabs.forEach((tab) => {
  tab.addEventListener("click", () => {
    const targetId = tab.getAttribute("data-target");
    const targetPanel = document.getElementById(targetId);

    if (tab.classList.contains("active")) {
      tab.classList.remove("active");
      targetPanel.classList.remove("open");
    } else {
      closeIncidentPanel();
      tabs.forEach((t) => t.classList.remove("active"));
      panels.forEach((p) => p.classList.remove("open"));
      tab.classList.add("active");
      targetPanel.classList.add("open");
    }
  });
});

// --- 3. SỰ KIỆN LÊN BẢN ĐỒ (MAP EVENTS) ---
map.on("load", () => {
  if (
    typeof savedLng !== "undefined" &&
    typeof savedLat !== "undefined" &&
    savedLng &&
    savedLat
  ) {
    pickerMarker
      .setLngLat([parseFloat(savedLng), parseFloat(savedLat)])
      .addTo(map);
  }

  // Hiển thị tòa nhà 3D
  const layers = map.getStyle().layers;
  const labelLayerId = layers.find(
    (layer) => layer.type === "symbol" && layer.layout["text-field"],
  )?.id;

  if (labelLayerId) {
    map.addLayer(
      {
        id: "3d-buildings",
        source: "composite",
        "source-layer": "building",
        filter: ["==", "extrude", "true"],
        type: "fill-extrusion",
        minzoom: 15,
        paint: {
          "fill-extrusion-color": "#e0e0e0",
          "fill-extrusion-height": ["get", "height"],
          "fill-extrusion-base": ["get", "min_height"],
          "fill-extrusion-opacity": 0.8,
        },
      },
      labelLayerId,
    );
  }

  // Tải toàn bộ Marker ban đầu
  loadIncidentMarkers();
  loadTruSoMarkers();
  loadCameraMarkers();
});

map.on("click", (e) => {
  if (e.originalEvent.target.closest(".mapboxgl-marker")) return;

  const { lng, lat } = e.lngLat;
  pickerMarker.setLngLat([lng, lat]).addTo(map);
  updateLocationFields(lng, lat);
});

geocoder.on("result", (e) => {
  const [lng, lat] = e.result.geometry.coordinates;
  pickerMarker.setLngLat([lng, lat]).addTo(map);
  updateLocationFields(lng, lat);
});

pickerMarker.on("dragend", () => {
  const lngLat = pickerMarker.getLngLat();
  updateLocationFields(lngLat.lng, lngLat.lat);
});

// --- 4. HÀM TẢI DỮ LIỆU TỪ BACKEND API ---
async function loadIncidentMarkers() {
  try {
    const response = await fetch("/api/su-co/map");
    const incidents = await response.json();

    Object.values(incidentMarkersMap || {}).forEach((m) => m.remove());
    incidentMarkersMap = {};

    incidents.forEach((suCo) => renderSingleIncident(suCo));
  } catch (error) {
    console.error("Lỗi tải marker sự cố:", error);
  }
}

async function loadTruSoMarkers() {
  try {
    const response = await fetch("/admin/quan-ly-tru-so/all");
    const danhSachTruSo = await response.json();

    truSoMarkersList.forEach((m) => m.remove());
    truSoMarkersList = [];

    danhSachTruSo.forEach((ts) => {
      const lng = parseFloat(ts.kinhDo);
      const lat = parseFloat(ts.viDo);
      if (isNaN(lng) || isNaN(lat) || (lng === 0 && lat === 0)) return;

      const el = document.createElement("div");
      el.className = "tru-so-marker";
      el.innerHTML = `<i class="fa-solid fa-house-chimney-medical" style="color: #27ae60; font-size: 30px; filter: drop-shadow(0 2px 4px rgba(0,0,0,0.3));"></i>`;

      const marker = new mapboxgl.Marker({
        element: el,
        anchor: "bottom",
      })
        .setLngLat([lng, lat])
        .setPopup(
          new mapboxgl.Popup().setHTML(
            `<h4>${ts.tenTruSo}</h4><p>${ts.diaChi || ""}</p>`,
          ),
        )
        .addTo(map);

      truSoMarkersList.push(marker);
    });
  } catch (error) {
    console.error("Lỗi tải marker trụ sở:", error);
  }
}

async function loadCameraMarkers() {
  try {
    const response = await fetch("/admin/quan-ly-camera/all-json");
    const cameras = await response.json();

    cameraMarkersList.forEach((m) => m.remove());
    cameraMarkersList = [];

    cameras.forEach((cam) => {
      const lng = parseFloat(cam.kinhDo);
      const lat = parseFloat(cam.viDo);
      if (isNaN(lng) || isNaN(lat) || (lng === 0 && lat === 0)) return;

      const el = document.createElement("div");
      el.className = "camera-marker";
      el.innerHTML = `<i class="fa-solid fa-camera-retro" style="color: #3b82f6; font-size: 24px;"></i>`;

      const marker = new mapboxgl.Marker({
        element: el,
        anchor: "center",
      })
        .setLngLat([lng, lat])
        .setPopup(
          new mapboxgl.Popup().setHTML(
            `<b>${cam.tenCamera}</b><p>${cam.moTa || ""}</p>`,
          ),
        )
        .addTo(map);

      cameraMarkersList.push(marker);
    });
  } catch (error) {
    console.error("Lỗi tải marker camera:", error);
  }
}

// --- 5. XỬ LÝ GỬI FORM (SUBMIT FORM LOGIC) ---
async function handleAdminSubmit() {
  const form = document.getElementById("adminSubmitForm");
  const formData = new FormData(form);

  if (!formData.get("viDo") || !formData.get("kinhDo")) {
    alert("Vui lòng chọn vị trí trên bản đồ bằng cách click!");
    return;
  }

  try {
    const response = await fetch("/admin/quan-ly-su-co/admin-submit", {
      method: "POST",
      body: formData,
    });

    if (response.ok) {
      alert("Đã tạo sự cố thành công!");
      form.reset();
      pickerMarker.remove();
      loadIncidentMarkers();

      document
        .querySelector('.control-tab[data-target="form-su-co"]')
        ?.classList.remove("active");
      document.getElementById("form-su-co")?.classList.remove("open");
    } else {
      alert("Lỗi khi tạo báo cáo.");
    }
  } catch (error) {
    console.error("Error:", error);
    alert("Lỗi kết nối server.");
  }
}

async function handleSosSubmit() {
  const form = document.getElementById("sosSubmitForm");
  const select = document.getElementById("select-tru-so");
  const truSoId = select.value;
  const formData = new FormData(form);

  const kinhDo = formData.get("kinhDo");
  const viDo = formData.get("viDo");

  if (!kinhDo || !viDo) {
    alert("Vui lòng click chọn vị trí trên bản đồ trước!");
    return;
  }

  // CHẾ ĐỘ 1: Gán tọa độ cho Trụ sở cũ sẵn có
  if (truSoId !== "") {
    const url = `/admin/quan-ly-tru-so/gan-toa-do/${truSoId}?kinhDo=${kinhDo}&viDo=${viDo}`;
    try {
      const response = await fetch(url, { method: "POST" });
      if (response.ok) {
        alert("✅ Đã gán vị trí cho trụ sở thành công!");
        loadTruSoMarkers();
        pickerMarker.remove();
        form.reset();
      } else {
        alert("❌ Lỗi gán tọa độ trụ sở.");
      }
    } catch (err) {
      console.error(err);
    }
    return;
  }

  // CHẾ ĐỘ 2: Tạo trụ sở mới hoàn toàn
  try {
    const response = await fetch("/admin/quan-ly-tru-so/them", {
      method: "POST",
      body: formData,
    });

    if (response.ok) {
      alert("✅ Thêm mới trụ sở thành công!");
      loadTruSoMarkers();
      form.reset();
      toggleNewStationFields();
      if (pickerMarker) pickerMarker.remove();
    } else {
      alert("❌ Lỗi: " + (await response.text()));
    }
  } catch (error) {
    console.error("Lỗi kết nối SOS:", error);
    alert("❌ Không thể kết nối đến máy chủ");
  }
}

async function handleCameraSubmit() {
  const form = document.getElementById("cameraSubmitForm");
  const select = document.getElementById("select-camera");
  const formData = new FormData(form);

  const lat = formData.get("viDo");
  const lng = formData.get("kinhDo");

  if (!lat || !lng) {
    alert("Vui lòng click chọn vị trí trên bản đồ trước!");
    return;
  }

  let url = "";
  let options = {};

  // THÊM MỚI CAMERA
  if (select.value === "") {
    if (!formData.get("tenCamera")) {
      alert("Vui lòng nhập tên Camera mới!");
      return;
    }
    url = "/admin/quan-ly-camera/them";
    options = {
      method: "POST",
      body: formData,
    };
  }
  // GÁN TOẠ ĐỘ CAMERA CŨ
  else {
    const params = new URLSearchParams({
      kinhDo: lng,
      viDo: lat,
    });
    url = `/admin/quan-ly-camera/gan-toa-do/${select.value}?${params.toString()}`;
    options = {
      method: "POST",
    };
  }

  try {
    const response = await fetch(url, options);

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText);
    }

    const result = await response.text();
    alert("✅ Thành công: " + result);

    if (pickerMarker) pickerMarker.remove();
    await loadCameraMarkers();

    const cameraPanel = document.getElementById("form-camera");
    const cameraTab = document.querySelector(
      '.control-tab[data-target="form-camera"]',
    );

    if (cameraPanel) cameraPanel.classList.remove("open");
    if (cameraTab) cameraTab.classList.remove("active");

    form.reset();
    toggleFormMode();
  } catch (err) {
    console.error("Chi tiết lỗi:", err);
    alert("❌ Có lỗi xảy ra: " + err.message);
  }
}

// --- 6. LOGIC ẨN/HIỆN TRƯỜNG FORM NHẬP LIỆU ---
function toggleFormMode() {
  const select = document.getElementById("select-camera");
  const fieldsGroup = document.getElementById("camera-fields-group");
  const tenInput = document.getElementById("inputTenCamera");

  if (!select || !fieldsGroup) return;

  if (select.value !== "") {
    fieldsGroup.style.display = "none";
    if (tenInput) tenInput.required = false;
  } else {
    fieldsGroup.style.display = "block";
    if (tenInput) tenInput.required = true;
  }
}

function toggleNewStationFields() {
  const select = document.getElementById("select-tru-so");
  const fields = document.getElementById("new-station-fields");
  if (!select || !fields) return;
  const inputs = fields.querySelectorAll("input");

  if (select.value !== "") {
    fields.style.display = "none";
    inputs.forEach((i) => i.removeAttribute("required"));
  } else {
    fields.style.display = "block";
    inputs.forEach((i) => i.setAttribute("required", "required"));
  }
}

function playNotificationSound() {
  const audio = new Audio(
    "https://assets.mixkit.co/active_storage/sfx/951/951-preview.mp3",
  );
  audio.volume = 0.7;
  audio.play().catch((e) => {
    console.log("Cần tương tác người dùng trước khi phát âm thanh:", e);
  });
}

// --- 7. REALTIME WEBSOCKETS ---
function connectWebSocket() {
  const socket = new SockJS("/ws-suco-web");
  const stompClient = Stomp.over(socket);

  stompClient.connect(
    {},
    function (frame) {
      stompClient.subscribe("/topic/admin-notifications", function (message) {
        updateNotificationList(message.body);
      });

      stompClient.subscribe("/topic/su-co", function (message) {
        const suCoDto = JSON.parse(message.body);
        console.log("Realtime Update:", suCoDto);

        if (suCoDto.trangThaiXuLy === "HUY_BO") {
          if (incidentMarkersMap && incidentMarkersMap[suCoDto.id]) {
            incidentMarkersMap[suCoDto.id].remove();
            delete incidentMarkersMap[suCoDto.id];
          }
        } else {
          renderSingleIncident(suCoDto);
          if (suCoDto.trangThaiXuLy === "DA_TIEP_NHAN") {
            playNotificationSound();
          }
        }
      });
    },
    function (err) {
      console.log("WebSocket disconnected. Reconnecting in 5s...");
      setTimeout(connectWebSocket, 5000);
    },
  );
}

function updateNotificationList(message) {
  notificationCount++;
  const bell = document.getElementById("notification-bell");
  const badge = bell?.querySelector(".badge");
  const list = document.getElementById("notification-list");

  if (badge) {
    badge.innerText = notificationCount;
    badge.style.display = "block";
  }
  if (bell) bell.classList.add("ring-animation");

  const noNoti = list?.querySelector(".no-notification");
  if (noNoti) noNoti.remove();

  const newItem = document.createElement("div");
  newItem.className = "notification-item";
  newItem.innerHTML = `
        <div style="display: flex; gap: 10px; align-items: flex-start;">
            <i class="fa-solid fa-circle-exclamation" style="color: #ef4444; margin-top: 4px;"></i>
            <div>
                <div style="font-weight: 600; font-size: 13px;">Sự cố mới cần duyệt</div>
                <div style="font-size: 12px; color: #555;">${message}</div>
                <div class="time">${new Date().toLocaleTimeString("vi-VN")}</div>
            </div>
        </div>`;

  if (list) list.insertBefore(newItem, list.firstChild);
}

function renderSingleIncident(suCo) {
  const id = suCo.id;
  const trangThai = suCo.trangThaiXuLy || suCo.trangThai || "";

  if (trangThai === "HOAN_THANH" || trangThai === "HUY_BO") {
    if (incidentMarkersMap && incidentMarkersMap[id]) {
      incidentMarkersMap[id].remove();
      delete incidentMarkersMap[id];
    }
    return;
  }

  const lng = parseFloat(suCo.kinhDo);
  const lat = parseFloat(suCo.viDo);
  if (isNaN(lng) || isNaN(lat)) return;

  let mucDo = (suCo.mucDoSuCo || suCo.mucDo || "NONE").toUpperCase().trim();
  let borderColor = "#94a3b8";
  if (mucDo === "HIGH") borderColor = "#e74c3c";
  else if (mucDo === "MEDIUM") borderColor = "#f1c40f";
  else if (mucDo === "LOW") borderColor = "#2ecc71";

  const iconPath = suCo.iconUrl || (suCo.loaiSuCo ? suCo.loaiSuCo.iconUrl : "");

  if (incidentMarkersMap && incidentMarkersMap[id]) {
    const markerEl = incidentMarkersMap[id].getElement();
    incidentMarkersMap[id].setLngLat([lng, lat]);

    const pin = markerEl.querySelector(".marker-pin");
    const tail = markerEl.querySelector(".marker-tail");
    const icon = markerEl.querySelector(".marker-icon");

    if (pin) pin.style.border = `3px solid ${borderColor}`;
    if (tail) tail.style.backgroundColor = borderColor;
    if (icon) icon.style.backgroundImage = `url('${iconPath}')`;
    return;
  }

  const el = document.createElement("div");
  el.className = "custom-marker";

  el.innerHTML = `
        <div class="marker-pin">
            <div class="marker-icon" style="background-image: url('${iconPath}');"></div>
        </div>
        <div class="marker-tail"></div>`;

  const pinEl = el.querySelector(".marker-pin");
  const tailEl = el.querySelector(".marker-tail");

  if (pinEl) pinEl.style.border = `3px solid ${borderColor}`;
  if (tailEl) tailEl.style.backgroundColor = borderColor;

  const marker = new mapboxgl.Marker({
    element: el,
    anchor: "bottom",
  })
    .setLngLat([lng, lat])
    .addTo(map);

  el.addEventListener("click", (e) => {
    e.stopPropagation();
    loadIncidentDetail(id);
  });

  incidentMarkersMap[id] = marker;
}

async function loadIncidentDetail(id) {
  try {
    const response = await fetch(`/api/su-co/${id}`);
    if (!response.ok) throw new Error("Không lấy được chi tiết sự cố");

    const detail = await response.json();
    showIncidentPanel(detail);
  } catch (e) {
    console.error("Lỗi load chi tiết:", e);
  }
}

function showIncidentPanel(data) {
  const panel = document.getElementById("incident-detail-panel");
  const content = document.getElementById("incident-detail-content");
  const badgeContainer = document.getElementById("incident-id-badge-container");
  if (!panel || !content) return;

  tabs.forEach((t) => t.classList.remove("active"));
  panels.forEach((p) => p.classList.remove("open"));

  panel.classList.add("open");

  if (badgeContainer) {
    badgeContainer.innerHTML = `<span class="incident-id-badge">#${data.id}</span>`;
  }

  // ... (phần nội dung HTML trong showIncidentPanel giữ nguyên như bạn đã viết)

  const statusMap = {
    AI_APPROVED: "AI xác thực",
    DANG_DI_CHUYEN: "Đang di chuyển",
    DA_TIEP_NHAN: "Đang xử lý",
    HOAN_THANH: "Đã hoàn thành",
    HUY_BO: "Đã hủy bỏ",
  };

  const levelMap = { HIGH: "🔴 Cao", MEDIUM: "🟡 Trung bình", LOW: "🟢 Thấp" };

  const trangThaiVn = statusMap[data.trangThaiXuLy] || data.trangThaiXuLy;
  const mucDoVn = levelMap[data.mucDoSuCo] || data.mucDoSuCo;

  let thoiGianTaoStr = "Đang cập nhật...";
  if (data.thoiGianTao) {
    thoiGianTaoStr = new Date(data.thoiGianTao).toLocaleString("vi-VN");
  }

  const percent = data.doTinCay != null ? data.doTinCay : 0;
  let reliabilityColor =
    percent >= 80 ? "#22c55e" : percent >= 50 ? "#f59e0b" : "#ef4444";

  content.innerHTML = `
        <img class="main-img" src="${data.hinhAnhUrl || "https://placehold.co/600x400?text=Khong+Co+Hinh+Anh"}" alt="Ảnh hiện trường sự cố">
        
        <div class="info-section-card">
            <div class="info-section-title"><i class="fa-solid fa-circle-info"></i> Thông tin chung</div>
            <table class="detail-table">
                <tr><td class="label">Loại sự cố</td><td class="value">${data.tenLoai || "Chưa rõ loại"}</td></tr>
                <tr><td class="label">Mức độ nguy hiểm</td><td class="value">${mucDoVn}</td></tr>
                <tr><td class="label">Trạng thái xử lý</td><td class="value">${trangThaiVn}</td></tr>
                <tr><td class="label">Thời gian báo</td><td class="value">${thoiGianTaoStr}</td></tr>
            </table>
        </div>

        <!-- Phần còn lại bạn có thể copy từ code cũ của mình vào đây -->
        <button class="panel-action-btn" onclick="window.location.href='/admin/quan-ly-su-co?id=${data.id}'">
            ĐẾN TRANG ĐIỀU PHỐI XỬ LÝ NGAY
        </button>
    `;
}

function closeIncidentPanel() {
  document.getElementById("incident-detail-panel")?.classList.remove("open");
}

// --- 8. KHỞI CHẠY KHI ĐÃ SẴN SÀNG DOM ---
document.addEventListener("DOMContentLoaded", () => {
  connectWebSocket();
  toggleNewStationFields();
  toggleFormMode();

  const bellIcon = document.getElementById("notification-bell");
  const dropdown = document.getElementById("notification-dropdown");

  if (bellIcon && dropdown) {
    bellIcon.addEventListener("click", (e) => {
      e.stopPropagation();
      dropdown.classList.toggle("show");
      if (dropdown.classList.contains("show")) {
        bellIcon.classList.remove("ring-animation");
      }
    });

    document.addEventListener("click", (e) => {
      if (!dropdown.contains(e.target) && !bellIcon.contains(e.target)) {
        dropdown.classList.remove("show");
      }
    });
  }
});
