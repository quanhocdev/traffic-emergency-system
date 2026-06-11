document.addEventListener("DOMContentLoaded", function () {
  // Chạy đồng hồ hệ thống
  startClock();

  // Lắng nghe sự kiện chuyển đổi bộ lọc Tab
  const btnSuCo = document.getElementById("btn-filter-su-co");
  const btnSos = document.getElementById("btn-filter-sos");

  if (btnSuCo && btnSos) {
    btnSuCo.addEventListener("click", () => {
      currentFilter = "su-co";
      btnSuCo.classList.add("active");
      btnSos.classList.remove("active");
      renderIncidents();
    });

    btnSos.addEventListener("click", () => {
      currentFilter = "sos";
      btnSos.classList.add("active");
      btnSuCo.classList.remove("active");
      renderIncidents();
    });
  }

  // Tải danh sách dữ liệu tích hợp lần đầu
  loadIncidents();

  // Lắng nghe sự kiện đóng panel chi tiết
  document
    .getElementById("detail-close")
    .addEventListener("click", closeDetail);
  document.getElementById("overlay").addEventListener("click", closeDetail);
});

// Biến toàn cục quản lý bộ lọc và dữ liệu gộp
var currentFilter = "su-co";
var allIncidentData = [];

// Hàm quản lý đồng hồ góc màn hình
function startClock() {
  setInterval(() => {
    const clock = document.getElementById("clock");
    if (clock) {
      const now = new Date();
      clock.innerText = now.toTimeString().split(" ")[0];
    }
  }, 1000);
}

// Hàm xử lý đường dẫn ảnh từ server
function fixUrl(path) {
  if (!path) return "/images/default-incident.jpg"; // Ảnh mặc định nếu null
  if (path.startsWith("http") || path.startsWith("/uploads/")) return path;
  return `/uploads/sos/${path}`;
}

// Hàm format thời gian hiển thị thân thiện
function formatTime(iso) {
  if (!iso) return "N/A";
  const d = new Date(iso);
  return d.toLocaleString("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    day: "2-digit",
    month: "2-digit",
  });
}

async function loadIncidents() {
  const listContainer = document.getElementById("incident-list");
  try {
    listContainer.innerHTML = `
      <div class="alert alert-secondary py-4" id="no-data">
         <span class="spinner"></span> Đang tải danh sách sự cố từ hệ thống...
      </div>`;

    // 🌟 CHỈ GỌI API SỰ CỐ
    const res = await fetch("/truso/api/su-co/da-tiep-nhan");
    const sucoRes = res.ok ? await res.json() : [];

    // Chuẩn hóa dữ liệu Sự cố
    allIncidentData = (sucoRes || []).map((s) => ({
      ...s,
      itemType: "SUCO",
      id: s.id,
      time: s.thoiGianTao || s.createdAt,
      diaChi: s.diaChi || `${s.viDo}, ${s.kinhDo}`,
      ghiChu: s.moTa || "Không có mô tả",
      hinhAnh: fixUrl(s.hinhAnhUrl),
      loai: s.loaiSuCo ? s.loaiSuCo.tenLoai : "Sự cố",
      mucDo: s.mucDoSuCo || "NONE",
    }));

    // Sắp xếp thời gian mới nhất lên đầu
    allIncidentData.sort(
      (a, b) => new Date(b.time || 0) - new Date(a.time || 0),
    );

    renderIncidents(); // Hàm render lúc này không cần biến filter nữa, cứ thế vẽ ra thôi
  } catch (error) {
    console.error("Lỗi khi tải dữ liệu sự cố:", error);
    listContainer.innerHTML = `<div class="alert alert-danger py-3">Không thể tải dữ liệu: ${error.message}</div>`;
  }
}
async function loadIncidents() {
  const listContainer = document.getElementById("incident-list");
  try {
    listContainer.innerHTML = `<div class="alert alert-secondary py-4"><span class="spinner"></span> Đang tải dữ liệu...</div>`;

    // 1. Tự động nhận diện API theo đường dẫn trang web hiện tại
    const isSosPage = window.location.pathname.includes("sos");
    const apiUrl = isSosPage
      ? "/truso/api/sos/da-tiep-nhan"
      : "/truso/api/su-co/da-tiep-nhan";

    const response = await fetch(apiUrl);
    const rawData = response.ok ? await response.json() : [];

    // 2. Map dữ liệu chuẩn hóa tùy theo loại trang
    allIncidentData = (rawData || []).map((item) => {
      if (isSosPage) {
        return {
          ...item,
          itemType: "SOS",
          id: item.id,
          time: item.createdAt || item.thoiGianTao,
          diaChi: item.diaChi || `${item.viDo}, ${item.kinhDo}`,
          ghiChu: item.ghiChu || "Yêu cầu cứu trợ khẩn cấp",
          hinhAnh: fixUrl(item.hinhAnh),
        };
      } else {
        return {
          ...item,
          itemType: "SUCO",
          id: item.id,
          time: item.thoiGianTao || item.createdAt,
          diaChi: item.diaChi || `${item.viDo}, ${item.kinhDo}`,
          ghiChu: item.moTa || "Không có mô tả",
          hinhAnh: fixUrl(item.hinhAnhUrl),
          loai: item.loaiSuCo ? item.loaiSuCo.tenLoai : "Sự cố",
          mucDo: item.mucDoSuCo || "NONE",
        };
      }
    });

    // 3. Sắp xếp thời gian
    allIncidentData.sort(
      (a, b) => new Date(b.time || 0) - new Date(a.time || 0),
    );

    // 4. Render (Lúc này hàm renderIncidents() bên dưới cứ vẽ hết mảng allIncidentData, không cần filter() nữa)
    renderIncidents();
  } catch (error) {
    console.error("Lỗi hệ thống:", error);
    listContainer.innerHTML = `<div class="alert alert-danger py-3">Không thể tải dữ liệu: ${error.message}</div>`;
  }
}

// 2. RENDER DỮ LIỆU RA LAYOUT CARD THEO TAB ĐANG CHỌN
function renderIncidents() {
  const container = document.getElementById("incident-list");
  container.innerHTML = "";

  // Lọc theo tab hiện tại
  const filtered = allIncidentData.filter((item) =>
    currentFilter === "sos"
      ? item.itemType === "SOS"
      : item.itemType === "SUCO",
  );

  if (filtered.length === 0) {
    container.innerHTML = `
      <div class="alert alert-secondary py-4 text-center">
        <i class="fa-solid fa-folder-open mb-2 fs-4 d-block"></i>
        Không có ${currentFilter === "sos" ? "yêu cầu SOS" : "sự cố"} nào hiển thị.
      </div>`;
    return;
  }

  // Duyệt qua danh sách dữ liệu và tạo Card HTML đầy đủ chức năng
  container.innerHTML = filtered
    .map((item, index) => {
      // Lưu tạm item vào window để hàm onclick có thể đọc được Object phức tạp tránh lỗi JSON stringify
      const globalItemKey = `incident_item_${item.itemType}_${item.id}`;
      window[globalItemKey] = item;

      if (item.itemType === "SOS") {
        // --- Thẻ hiển thị cho SOS ---
        const vipBadge = item.isVip
          ? '<span class="badge bg-danger ms-2"><i class="fa-solid fa-crown"></i> VIP</span>'
          : "";
        return `
        <div class="incident-card d-flex justify-content-between align-items-start">
            <div class="d-flex gap-3">
                <img src="${item.hinhAnh}" alt="SOS Image" 
                     style="width: 110px; height: 80px; object-fit: cover; border-radius: 8px;"
                     class="border" onclick="window.open('${item.hinhAnh}', '_blank')"/>
                <div>
                    <h5 class="mb-1 text-dark fw-bold">Yêu cầu cứu hộ SOS</h5>
                    <p class="mb-1 text-secondary small"><i class="fa-solid fa-location-dot text-danger"></i> ${item.diaChi}</p>
                    <p class="mb-0 text-muted italic small">"${item.ghiChu}"</p>
                </div>
            </div>
            
            <div class="text-end d-flex flex-column align-items-end gap-2">
                <div class="d-flex align-items-center">
                    <span class="badge-level level-high">SOS KHẨN CẤP</span>
                    ${vipBadge}
                </div>
                <small class="text-muted"><i class="fa-regular fa-clock"></i> ${formatTime(item.time)}</small>
                <div class="d-flex gap-2 mt-2">
                    <button class="btn btn-sm btn-outline-primary" onclick="viewDetail('${globalItemKey}')">
                        <i class="fa-solid fa-eye"></i> Chi tiết
                    </button>
                    <button class="btn btn-sm btn-success shadow-sm" onclick="dispatchIncident('${item.itemType}', ${item.id}, this)">
                        <i class="fa-solid fa-truck-fast"></i> Di chuyển ngay
                    </button>
                </div>
            </div>
        </div>
      `;
      } else {
        // --- Thẻ hiển thị cho SỰ CỐ ---
        let badgeStyle = "level-none";
        let badgeText = "Thấp";
        if (item.mucDo === "HIGH") {
          badgeStyle = "level-high";
          badgeText = "Cao";
        }
        if (item.mucDo === "MEDIUM") {
          badgeStyle = "level-medium";
          badgeText = "Trung bình";
        }
        if (item.mucDo === "LOW") {
          badgeStyle = "level-low";
          badgeText = "Thấp";
        }

        return `
        <div class="incident-card d-flex justify-content-between align-items-start">
            <div class="d-flex gap-3">
                <img src="${item.hinhAnh}" alt="Incident image" 
                     style="width: 110px; height: 80px; object-fit: cover; border-radius: 8px;"
                     class="border" onclick="window.open('${item.hinhAnh}', '_blank')"/>
                <div>
                    <h5 class="mb-1 text-dark fw-bold">${item.loai}</h5>
                    <p class="mb-1 text-secondary small"><i class="fa-solid fa-location-dot text-danger"></i> ${item.diaChi}</p>
                    <p class="mb-0 text-muted italic small">"${item.ghiChu}"</p>
                </div>
            </div>
            
            <div class="text-end d-flex flex-column align-items-end gap-2">
                <span class="badge-level ${badgeStyle}">Mức độ: ${badgeText}</span>
                <small class="text-muted"><i class="fa-regular fa-clock"></i> ${formatTime(item.time)}</small>
                <div class="d-flex gap-2 mt-2">
                    <button class="btn btn-sm btn-outline-primary" onclick="viewDetail('${globalItemKey}')">
                        <i class="fa-solid fa-eye"></i> Chi tiết
                    </button>
                    <button class="btn btn-sm btn-success shadow-sm" onclick="dispatchIncident('${item.itemType}', ${item.id}, this)">
                        <i class="fa-solid fa-truck-fast"></i> Di chuyển ngay
                    </button>
                </div>
            </div>
        </div>
      `;
      }
    })
    .join("");
}

// 3. XỬ LÝ ĐIỀU ĐỘNG "DI CHUYỂN NGAY" LÊN BACKEND CHO CẢ 2 PHÂN LOẠI
async function dispatchIncident(type, id, buttonEl) {
  if (
    !confirm(
      "Bạn có chắc chắn muốn điều động lực lượng di chuyển xử lý mục này chứ?",
    )
  )
    return;

  buttonEl.disabled = true;
  buttonEl.innerHTML = `<span class="spinner"></span> Đang xử lý...`;

  // Tự động nhận diện endpoint tương ứng theo đối tượng click
  const apiUrl =
    type === "SOS"
      ? `/truso/api/sos/${id}/di-chuyen-ngay` // Thêm/sửa URL API điều hướng SOS của bạn tại đây nếu khác
      : `/truso/api/su-co/${id}/di-chuyen-ngay`;

  try {
    const response = await fetch(apiUrl, { method: "POST" });

    if (response.ok) {
      loadIncidents(); // Tải lại danh sách sau khi đổi trạng thái thành công
    } else {
      const msg = await response.text();
      alert("Lỗi thực thi từ hệ thống: " + msg);
      buttonEl.disabled = false;
      buttonEl.innerHTML = `<i class="fa-solid fa-truck-fast"></i> Di chuyển ngay`;
    }
  } catch (err) {
    alert("Lỗi đường truyền internet!");
    buttonEl.disabled = false;
    buttonEl.innerHTML = `<i class="fa-solid fa-truck-fast"></i> Di chuyển ngay`;
  }
}

// 4. QUẢN LÝ PANEL HIỂN THỊ CHI TIẾT
function viewDetail(globalItemKey) {
  const item = window[globalItemKey];
  if (!item) return;

  const body = document.getElementById("detail-body");

  if (item.itemType === "SOS") {
    // Panel Chi tiết cho dạng SOS
    body.innerHTML = `
        <div class="text-center mb-3">
             <img src="${item.hinhAnh}" class="img-fluid rounded border" style="max-height: 220px; object-fit: contain;"/>
        </div>
        <table class="table table-sm table-striped small">
             <tr><th>Mã cứu hộ:</th><td>#${item.id}</td></tr>
             <tr><th>Phân loại:</th><td><span class="badge bg-danger">Yêu cầu SOS Khẩn cấp</span></td></tr>
             <tr><th>Tọa độ:</th><td>${item.viDo}, ${item.kinhDo}</td></tr>
             <tr><th>Địa chỉ:</th><td>${item.diaChi}</td></tr>
             <tr><th>Tài khoản gửi:</th><td>${item.user ? item.user.hoTen : "Ẩn danh"}</td></tr>
             <tr><th>Ghi chú từ dân:</th><td>${item.ghiChu}</td></tr>
        </table>`;
  } else {
    // Panel Chi tiết cho dạng SỰ CỐ AI
    body.innerHTML = `
        <div class="text-center mb-3">
             <img src="${item.hinhAnh}" class="img-fluid rounded border" style="max-height: 220px; object-fit: contain;"/>
        </div>
        <table class="table table-sm table-striped small">
             <tr><th>Mã sự cố:</th><td>#${item.id}</td></tr>
             <tr><th>Loại sự cố:</th><td>${item.loai}</td></tr>
             <tr><th>Tọa độ:</th><td>${item.viDo}, ${item.kinhDo}</td></tr>
             <tr><th>Độ tin cậy:</th><td><span class="badge bg-dark">${item.doTinCay || 0}/5</span></td></tr>
             <tr><th>Nguồn tin:</th><td><span class="badge bg-secondary">${item.nguonBaoCao || "AI"}</span></td></tr>
             <tr><th>AI xác thực:</th><td><i class="fa-solid ${item.aiXacNhan ? "fa-circle-check text-success" : "fa-circle-xmark text-muted"}"></i></td></tr>
        </table>`;
  }

  document.getElementById("overlay").style.display = "block";
  document.getElementById("detail-panel").style.display = "block";
}

function closeDetail() {
  document.getElementById("overlay").style.display = "none";
  document.getElementById("detail-panel").style.display = "none";
}
