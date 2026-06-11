// --- KHỞI TẠO WEBSOCKET GIÁM SÁT SỰ CỐ ---
const socket = new SockJS("/ws-suco-web");
const stompClient = Stomp.over(socket);
const cameraLoaded = new Set();

stompClient.connect({}, function (frame) {
  console.log("Connected to Incident Management WebSocket");

  stompClient.subscribe("/topic/su-co", function (message) {
    const suCoDto = JSON.parse(message.body);
    const id = suCoDto.id;
    const tableRow = document.getElementById("report-" + id);
    const status = suCoDto.trangThaiXuLy;

    // Phát âm thanh báo động nếu có sự cố MỚI đổ về (Đã tiếp nhận)
    if (!tableRow && status === "DA_TIEP_NHAN") {
      playNotificationSound();
      renderNewRow(suCoDto);
      loadNearbyCameras(id);
    }
    // Nếu dòng đã tồn tại, tiến hành cập nhật trạng thái trực tiếp
    else if (tableRow) {
      tableRow.setAttribute("data-status", status);
      const statusCell = tableRow.querySelector(".status-cell");

      if (statusCell) {
        statusCell.innerHTML = renderStatus(status, suCoDto);
      }

      // Xử lý hiệu ứng mờ nếu bị Hủy bỏ giữa chừng
      if (status === "HUY_BO") {
        tableRow.style.opacity = "0.5";
      } else {
        tableRow.style.opacity = "1";
      }

      // Highlight hiệu ứng nhấp nháy khi có cập nhật tiến độ từ Trụ sở
      tableRow.style.backgroundColor = "#f0f9ff";
      setTimeout(() => (tableRow.style.backgroundColor = ""), 1500);
    }

    updateTabCounts();
  });
});

// Hàm sinh dòng mới khi có sự cố vừa được gán từ AI/Hệ thống
function renderNewRow(suCoDto) {
  const tableBody = document.getElementById("table-body-main");
  if (!tableBody) return;
  if (document.getElementById("report-" + suCoDto.id)) return;

  const row = document.createElement("tr");
  row.className = "incident-row";
  row.id = "report-" + suCoDto.id;

  row.setAttribute("data-status", suCoDto.trangThaiXuLy);
  row.setAttribute("data-lat", suCoDto.viDo);
  row.setAttribute("data-lng", suCoDto.kinhDo);

  row.innerHTML = `
    <td>${suCoDto.id}</td>
    <td>
      <img src="${suCoDto.hinhAnhUrl || "/images/no-image.png"}" class="img-table"/>
    </td>
    <td>
      <div style="font-weight: 600;">${suCoDto.tenLoai || "Sự cố"}</div>
      <small class="badge" style="background: #fee2e2; color: #ef4444;">${suCoDto.mucDoSuCo || "NONE"}</small>
    </td>
    <td>
      <div>${suCoDto.tenNguoiBao || "Người dân"}</div>
      <div style="font-size:11px;color:#94a3b8">${suCoDto.reporterUid || ""}</div>
    </td>
    <td>${suCoDto.diaChi || "Không rõ"}</td>
    <td class="confidence-val">${suCoDto.doTinCay || 1}</td>
    <td class="status-cell">
      ${renderStatus(suCoDto.trangThaiXuLy, suCoDto)}
    </td>
    <td>
      <div id="cam-area-${suCoDto.id}" style="display:none">
        <div id="cam-list-${suCoDto.id}" style="display:flex; flex-wrap:wrap; gap:5px"></div>
      </div>
    </td>
  `;

  tableBody.prepend(row);
}

// Chuẩn hóa hiển thị trạng thái theo đúng Enum Backend
function renderStatus(status, dto) {
  const truSoId = dto.truSoTiepNhan ? dto.truSoTiepNhan.id : null;
  const tenTruSo = dto.truSoTiepNhan ? `Trụ sở #${truSoId}` : "Chưa rõ trụ sở";

  switch (status) {
    case "DA_TIEP_NHAN":
      return `<span style="color:#f59e0b; font-style:italic; font-weight:500;">
                <i class="fa-solid fa-bell"></i> Đã tiếp nhận (${tenTruSo})
              </span>`;
    case "CHO_XU_LY":
      return `<span style="color:#d97706; font-weight:500;">
                <i class="fa-solid fa-clock"></i> Chờ xử lý
              </span>`;
    case "DANG_XU_LY":
      return `<span style="color:#3b82f6; font-weight:600;">
                <i class="fa-solid fa-spinner fa-spin"></i> ${tenTruSo} đang xử lý
              </span>`;
    case "HOAN_THANH":
      return `<span style="color:#10b981; font-weight:600;">
                <i class="fa-solid fa-circle-check"></i> Đã hoàn thành
              </span>`;
    case "HUY_BO":
      return `<span style="color:#ef4444; font-weight:500;">
                <i class="fa-solid fa-circle-xmark"></i> Đã hủy bỏ
              </span>`;
    default:
      return `<span class="text-muted">Không rõ</span>`;
  }
}

// Thay đổi trạng thái chủ động từ phía Admin (nếu điều phối thủ công bằng nút bấm)
async function updateIncidentStatus(id, nextStatus) {
  if (!confirm(`Xác nhận chuyển sự cố #${id} sang trạng thái này?`)) return;
  try {
    const res = await fetch(
      `/admin/bao-cao-su-co/${id}/status?status=${nextStatus}`,
      {
        method: "POST",
      },
    );
    if (!res.ok)
      alert("Không thể chuyển trạng thái. Vui lòng kiểm tra lại quy tắc Enum!");
  } catch (error) {
    console.error("Lỗi cập nhật trạng thái:", error);
  }
}

// Tính toán chính xác số lượng trên các tab bộ lọc
function updateTabCounts() {
  const rows = document.querySelectorAll("#table-body-main .incident-row");

  let daTiepNhan = 0;
  let choXuLy = 0;
  let dangXuLy = 0;
  let hoanThanh = 0;
  let huyBo = 0;

  rows.forEach((row) => {
    const status = row.getAttribute("data-status");
    if (status === "DA_TIEP_NHAN") daTiepNhan++;
    else if (status === "CHO_XU_LY") choXuLy++;
    else if (status === "DANG_XU_LY") dangXuLy++;
    else if (status === "HOAN_THANH") hoanThanh++;
    else if (status === "HUY_BO") huyBo++;
  });

  const tabCounts = document.querySelectorAll(".tab-count");
  if (tabCounts.length >= 5) {
    tabCounts[0].innerText = rows.length; // Tất cả
    tabCounts[1].innerText = daTiepNhan;
    tabCounts[2].innerText = choXuLy;
    tabCounts[3].innerText = dangXuLy;
    tabCounts[4].innerText = hoanThanh;
    if (tabCounts[5]) tabCounts[5].innerText = huyBo; // Cập nhật cho tab Hủy bỏ nếu có
  }
}

// Hàm quét Camera xung quanh dựa vào tọa độ thực tế của sự cố
async function loadNearbyCameras(reportId) {
  if (cameraLoaded.has(reportId)) return;
  cameraLoaded.add(reportId);

  const sourceElement = document.getElementById("report-" + reportId);
  if (!sourceElement) return;

  const lat = sourceElement.getAttribute("data-lat");
  const lng = sourceElement.getAttribute("data-lng");
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
        camItem.className = "cam-badge-item";
        camItem.style = `
          background: #f1f5f9; border: 1px solid #cbd5e1;
          padding: 3px 8px; border-radius: 4px; font-size: 11px;
          cursor: pointer; display: flex; align-items: center; gap: 5px;
          white-space: nowrap;
        `;
        camItem.innerHTML = `
          <i class="fa-solid fa-video" style="color: #475569;"></i>
          <span style="font-weight: 500;">${cam.tenCamera}</span>
        `;
        camItem.onclick = () =>
          cam.videoUrl
            ? window.open(cam.videoUrl, "_blank")
            : alert("Không có luồng trực tiếp!");

        tableList.appendChild(camItem);
      });
    }
  } catch (e) {
    console.error("Lỗi quét camera cho ID: " + reportId, e);
  }
}

// Lắng nghe sự kiện switch tab lọc dữ liệu
window.switchTab = function (type) {
  const rows = document.querySelectorAll(".incident-row");
  rows.forEach((row) => {
    const status = row.getAttribute("data-status");
    if (type === "all") row.style.display = "";
    else if (type === "pending")
      row.style.display = status === "DA_TIEP_NHAN" ? "" : "none";
    else if (type === "waiting")
      row.style.display = status === "CHO_XU_LY" ? "" : "none";
    else if (type === "processing")
      row.style.display = status === "DANG_XU_LY" ? "" : "none";
    else if (type === "done")
      row.style.display = status === "HOAN_THANH" ? "" : "none";
    else if (type === "cancel")
      row.style.display = status === "HUY_BO" ? "" : "none";
  });
};

// Khởi chạy khi DOM sẵn sàng
document.addEventListener("DOMContentLoaded", function () {
  document.querySelectorAll(".incident-row").forEach((row) => {
    const reportId = row.id.replace("report-", "");
    loadNearbyCameras(reportId);
  });
  updateTabCounts();
});

function playNotificationSound() {
  const audio = new Audio(
    "https://assets.mixkit.co/active_storage/sfx/951/951-preview.mp3",
  );
  audio.volume = 0.6;
  audio
    .play()
    .catch(() => console.log("Yêu cầu tương tác để phát còi báo động"));
}
