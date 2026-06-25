// --- KHỞI TẠO HỆ THỐNG LẮNG NGHE REALTIME (WEBSOCKET) ---
const socket = new SockJS("/ws-suco-web");
const stompClient = Stomp.over(socket);
const cameraLoaded = new Set();

stompClient.connect({}, function (frame) {
  console.log("Connected to Control Room WebSocket");

  stompClient.subscribe("/topic/su-co", function (message) {
    const suCoDto = JSON.parse(message.body);
    console.log(" DỮ LIỆU REALTIME NHẬN ĐƯỢC:", suCoDto);
    const id = suCoDto.id;

    let tableRow = document.getElementById("report-" + id);

    if (!tableRow) {
      renderNewRow(suCoDto);
      tableRow = document.getElementById("report-" + id);
      playNotificationSound();
      loadNearbyCameras(id);
    } else {
      updateExistingRow(tableRow, suCoDto);
    }

    updateTabCounts();
  });
});

function renderNewRow(dto) {
  const tableBody = document.getElementById("table-body-main");
  if (!tableBody || document.getElementById("report-" + dto.id)) return;

  const row = document.createElement("tr");
  row.className = "incident-row";
  row.id = "report-" + dto.id;

  row.setAttribute("data-status", dto.trangThaiXuLy);
  row.setAttribute("data-lat", dto.viDo);
  row.setAttribute("data-lng", dto.kinhDo);

  if (dto.trangThaiXuLy === "HUY_BO") {
    row.style.opacity = "0.5";
  }

  // Định dạng hiển thị thời gian từ LocalDateTime
  let formattedTime = "Vừa xong";
  if (dto.thoiGianTao) {
    try {
      const date = new Date(dto.thoiGianTao);
      formattedTime = date.toLocaleString("vi-VN");
    } catch (e) {
      formattedTime = dto.thoiGianTao;
    }
  }

  // SỬA TẠI ĐÂY: Trích xuất an toàn từ Object reporter và truSoTiepNhan giống như Backend gửi qua
  const reporterName =
    dto.reporter && dto.reporter.name ? dto.reporter.name : "Người dân";
  const reporterEmail =
    dto.reporter && dto.reporter.email
      ? dto.reporter.email
      : "Ẩn danh / Không email";

  row.innerHTML = `
    <td class="row-id-cell">${dto.id}</td>
    <td>
      <img src="${dto.hinhAnhUrl || "/images/no-image.png"}" class="img-table"/>
    </td>
    <td style="font-weight: 600; color: #1e293b;">${dto.tenLoai || "Sự cố"}</td>
    <td>
      <small class="badge-level ${dto.mucDoSuCo || "NONE"}">${dto.mucDoSuCo || "NONE"}</small>
    </td>
    <td>
      <div style="font-weight: 500;">${reporterName}</div>
      <div class="reporter-email">${reporterEmail}</div>
    </td>
    <td>
      <div class="address-text">${dto.diaChi || "Không xác định"}</div>
      <div class="coordinate-subtext">
        <i class="fa-solid fa-location-dot"></i> Lat: ${dto.viDo || 0.0}, Lng: ${dto.kinhDo || 0.0}
      </div>
    </td>
    <td class="time-cell">${formattedTime}</td>
    <td class="confidence-val">${dto.doTinCay !== undefined ? dto.doTinCay : 1}</td>
    <td class="status-cell">
      ${generateStatusBadge(dto.trangThaiXuLy, dto)}
    </td>
    <td>
      <div id="cam-area-${dto.id}" class="camera-zone-wrapper" style="display:none">
        <div id="cam-list-${dto.id}" class="camera-badge-flex"></div>
      </div>
    </td>
  `;

  tableBody.prepend(row);

  row.style.backgroundColor = "#eff6ff";
  setTimeout(() => (row.style.backgroundColor = ""), 2000);
}

function updateExistingRow(row, dto) {
  row.setAttribute("data-status", dto.trangThaiXuLy);
  row.style.opacity = dto.trangThaiXuLy === "HUY_BO" ? "0.5" : "1";

  // Ở bảng mới 10 cột, ô Trạng thái nằm ở index số 8 (cột số 9)
  const statusCell = row.cells[8];
  if (statusCell) {
    statusCell.innerHTML = generateStatusBadge(dto.trangThaiXuLy, dto);
  }

  row.style.backgroundColor = "#f0f9ff";
  setTimeout(() => (row.style.backgroundColor = ""), 1500);
}

function generateStatusBadge(status, dto) {
  switch (status) {
    case "DA_TIEP_NHAN":
      return `<span class="status-label status-received">
                <i class="fa-solid fa-bell"></i> Đã tiếp nhận 
                ${dto.truSoTiepNhan?.id ? `(Trụ sở #${dto.truSoTiepNhan.id})` : "(Chưa gán)"}
              </span>`;
    case "DANG_DI_CHUYEN":
      return `<span class="status-label status-waiting">
                <i class="fa-solid fa-clock"></i> Đang di chuyển
              </span>`;
    case "DANG_XU_LY":
      return `<span class="status-label status-processing">
                <i class="fa-solid fa-spinner fa-spin"></i> 
                Trụ sở #${dto.truSoTiepNhan?.id || "tổng"} đang xử lý
              </span>`;
    case "HOAN_THANH":
      return `<span class="status-label status-done">
                <i class="fa-solid fa-circle-check"></i> Đã hoàn thành
              </span>`;
    case "HUY_BO":
      return `<span class="status-label status-cancelled">
                <i class="fa-solid fa-circle-xmark"></i> Đã hủy bỏ
              </span>`;
    default:
      return `<span class="status-label" style="color:#64748b">Không rõ</span>`;
  }
}

function playNotificationSound() {
  const audio = new Audio(
    "https://assets.mixkit.co/active_storage/sfx/951/951-preview.mp3",
  );
  audio.volume = 0.5;
  audio.play().catch(() => {});
}

window.switchTab = function (type, element) {
  const tabItems = document.querySelectorAll(".system-tabs .tab-item");
  tabItems.forEach((tab) => tab.classList.remove("active"));

  if (element) {
    element.classList.add("active");
  } else {
    const targetTab = Array.from(tabItems).find((tab) =>
      tab.getAttribute("onclick").includes(`'${type}'`),
    );
    if (targetTab) targetTab.classList.add("active");
  }

  const rows = document.querySelectorAll("#table-body-main .incident-row");
  rows.forEach((row) => {
    const status = row.getAttribute("data-status");

    switch (type) {
      case "all":
        row.style.display = "";
        break;
      case "pending":
        row.style.display = status === "DA_TIEP_NHAN" ? "" : "none";
        break;
      case "waiting":
        row.style.display = status === "DANG_DI_CHUYEN" ? "" : "none";
        break;
      case "processing":
        row.style.display = status === "DANG_XU_LY" ? "" : "none";
        break;
      case "done":
        row.style.display = status === "HOAN_THANH" ? "" : "none";
        break;
      case "cancel":
        row.style.display = status === "HUY_BO" ? "" : "none";
        break;
    }
  });
};

function updateTabCounts() {
  const rows = document.querySelectorAll("#table-body-main .incident-row");

  let total = rows.length;
  let daTiepNhan = 0;
  let dangDiChuyen = 0;
  let dangXuLy = 0;
  let hoanThanh = 0;
  let huyBo = 0;

  rows.forEach((row) => {
    const status = row.getAttribute("data-status");
    if (status === "DA_TIEP_NHAN") daTiepNhan++;
    else if (status === "DANG_DI_CHUYEN") dangDiChuyen++;
    else if (status === "DANG_XU_LY") dangXuLy++;
    else if (status === "HOAN_THANH") hoanThanh++;
    else if (status === "HUY_BO") huyBo++;
  });

  const tabItems = document.querySelectorAll(".system-tabs .tab-item");
  if (tabItems.length >= 6) {
    tabItems[0].querySelector(".tab-count").innerText = total;
    tabItems[1].querySelector(".tab-count").innerText = daTiepNhan;
    tabItems[2].querySelector(".tab-count").innerText = dangDiChuyen;
    tabItems[3].querySelector(".tab-count").innerText = dangXuLy;
    tabItems[4].querySelector(".tab-count").innerText = hoanThanh;
    tabItems[5].querySelector(".tab-count").innerText = huyBo;
  }
}

document.addEventListener("DOMContentLoaded", function () {
  document.querySelectorAll(".incident-row").forEach((row) => {
    const reportId = row.id.replace("report-", "");
    loadNearbyCameras(reportId);
  });

  updateTabCounts();

  const urlParams = new URLSearchParams(window.location.search);
  const highlightId = urlParams.get("id");
  if (highlightId) {
    const targetRow = document.getElementById("report-" + highlightId);
    if (targetRow) {
      targetRow.style.backgroundColor = "#fef3c7";
      setTimeout(() => {
        targetRow.scrollIntoView({ behavior: "smooth", block: "center" });
      }, 300);
      setTimeout(() => {
        targetRow.style.backgroundColor = "";
      }, 4000);
    }
  }
});

async function loadNearbyCameras(reportId) {
  if (cameraLoaded.has(reportId)) return;
  cameraLoaded.add(reportId);

  const row = document.getElementById("report-" + reportId);
  if (!row) return;

  const lat = row.getAttribute("data-lat");
  const lng = row.getAttribute("data-lng");
  if (!lat || !lng || lat === "0.0") return;

  try {
    const res = await fetch(
      `/admin/quan-ly-camera/near-by-incident/${reportId}`,
    );
    if (!res.ok) return;
    const cameras = await res.json();

    const tableArea = document.getElementById("cam-area-" + reportId);
    const tableList = document.getElementById("cam-list-" + reportId);

    if (tableArea && tableList && cameras && cameras.length > 0) {
      tableArea.style.display = "flex";
      tableList.innerHTML = "";
      cameras.forEach((cam) => {
        const camItem = document.createElement("div");
        camItem.innerHTML = `
          <i class="fa-solid fa-camera" style="color: #475569;"></i>
          <span style="font-weight: 500;">${cam.tenCamera}</span>
        `;
        camItem.onclick = (e) => {
          e.stopPropagation();
          cam.videoUrl
            ? window.open(cam.videoUrl, "_blank")
            : alert("Camera mất tín hiệu!");
        };
        tableList.appendChild(camItem);
      });
    }
  } catch (e) {
    console.error("Lỗi tải camera cho sự cố ID: " + reportId, e);
  }
}
