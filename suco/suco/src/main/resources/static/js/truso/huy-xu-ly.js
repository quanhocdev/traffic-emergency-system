/*<![CDATA[*/
var TRUSO_ID = /*[[${session.currentTruSo != null ? session.currentTruSo.id : 0}]]*/ 0;
/*]]>*/

var currentFilter = "su-co";
var allHistoryData = [];

function fixUrl(path) {
  if (!path) return null;
  if (path.startsWith("http") || path.startsWith("/uploads/")) return path;
  return `/uploads/sos/${path}`;
}

function formatTime(iso) {
  if (!iso) return "N/A";
  const d = new Date(iso);
  return d.toLocaleString("vi-VN");
}

async function loadHistory() {
  try {
    const [sosRes, sucoRes] = await Promise.all([
      fetch("/truso/api/sos/huy-xu-ly").then((r) => (r.ok ? r.json() : [])),
      fetch("/truso/api/su-co/huy-xu-ly").then((r) => (r.ok ? r.json() : [])),
    ]);

    console.log("Dữ liệu SOS đã hủy:", sosRes);
    console.log("Dữ liệu Sự cố đã hủy:", sucoRes);

    const safeSosRes = Array.isArray(sosRes) ? sosRes : [];
    const safeSucoRes = Array.isArray(sucoRes) ? sucoRes : [];

    const sosMapped = safeSosRes.map((s) => ({
      id: s.id,
      viDo: s.viDo,
      kinhDo: s.kinhDo,
      diaChi: s.diaChi || "Không xác định",
      itemType: "SOS",
      status: s.trangThai, // Vẫn lưu trạng thái từ Backend
      time: s.createdAt || s.thoiGianTao,
      isVip: s.isVip === true,
      hinhAnh: fixUrl(s.hinhAnh),
      ghiChu: s.ghiChu || "Cứu trợ khẩn cấp",
    }));

    const sucoMapped = safeSucoRes.map((s) => ({
      id: s.id,
      viDo: s.viDo,
      kinhDo: s.kinhDo,
      diaChi: s.diaChi || "Không xác định",
      ghiChu: s.moTa || "Không có mô tả",
      status: s.trangThaiXuLy, // Vẫn lưu trạng thái từ Backend
      itemType: "SUCO",
      time: s.thoiGianTao || s.createdAt,
      hinhAnh: fixUrl(s.hinhAnhUrl),
      loai: s.loaiSuCo ? s.loaiSuCo.ten : "Sự cố",
    }));

    allHistoryData = [...sosMapped, ...sucoMapped];

    // Sắp xếp bản ghi mới nhất lên đầu danh sách
    allHistoryData.sort((a, b) => {
      const timeA = a.time ? new Date(a.time) : 0;
      const timeB = b.time ? new Date(b.time) : 0;
      return timeB - timeA;
    });

    renderHistory();
  } catch (err) {
    console.error("Lỗi script khi tải lịch sử:", err);
  }
}

function renderHistory() {
  const body = document.getElementById("history-body");
  const header = document.getElementById("table-header");
  const noData = document.getElementById("no-data");

  body.innerHTML = "";

  const filtered = allHistoryData.filter((item) =>
    currentFilter === "sos"
      ? item.itemType === "SOS"
      : item.itemType === "SUCO",
  );

  if (filtered.length === 0) {
    noData.style.display = "block";
    header.innerHTML = "";
    return;
  }
  noData.style.display = "none";

  // --- RENDER HEADER (Đã loại bỏ cột Trạng thái, Giá tiền, Mức độ) ---
  if (currentFilter === "sos") {
    header.innerHTML = `<th>Phân loại</th><th>Thời gian</th><th>Địa chỉ</th><th>Hình ảnh</th><th>Ghi chú</th>`;
  } else {
    header.innerHTML = `<th>Thời gian</th><th>Loại sự cố</th><th>Địa chỉ</th><th>Hình ảnh</th><th>Ghi chú</th>`;
  }

  // --- RENDER BODY ---
  filtered.forEach((item) => {
    const row = document.createElement("tr");
    const displayAddress = item.diaChi || `${item.viDo}, ${item.kinhDo}`;
    const imgHtml = item.hinhAnh
      ? `<img src="${item.hinhAnh}" class="img-preview" onclick="window.open(this.src)">`
      : "N/A";

    if (currentFilter === "sos") {
      const vipBadge = item.isVip
        ? '<span class="badge bg-danger"><i class="fa-solid fa-crown"></i> VIP</span>'
        : '<span class="badge bg-secondary">Thường</span>';

      row.innerHTML = `
        <td>${vipBadge}</td>
        <td><small>${formatTime(item.time)}</small></td>
        <td><div class="address-cell">${displayAddress}</div></td>
        <td>${imgHtml}</td>
        <td><small>${item.ghiChu}</small></td>
      `;
    } else {
      row.innerHTML = `
        <td><small>${formatTime(item.time)}</small></td>
        <td><span class="badge bg-info text-dark">${item.loai}</span></td>
        <td><div class="address-cell">${displayAddress}</div></td>
        <td>${imgHtml}</td>
        <td><small>${item.ghiChu}</small></td>
      `;
    }
    body.appendChild(row);
  });
}

// ĐĂNG KÝ SỰ KIỆN CLICK CHO TABS FILTER
document.addEventListener("DOMContentLoaded", () => {
  const btnSuCo = document.getElementById("btn-filter-su-co");
  const btnSos = document.getElementById("btn-filter-sos");

  if (btnSuCo && btnSos) {
    btnSuCo.addEventListener("click", () => {
      currentFilter = "su-co";
      btnSuCo.classList.add("active");
      btnSos.classList.remove("active");
      renderHistory();
    });

    btnSos.addEventListener("click", () => {
      currentFilter = "sos";
      btnSos.classList.add("active");
      btnSuCo.classList.remove("active");
      renderHistory();
    });
  }

  loadHistory();
});
