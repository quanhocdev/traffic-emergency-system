mapboxgl.accessToken = MAPBOX_TOKEN;

window.map = new mapboxgl.Map({
  container: "map",
  style: "mapbox://styles/mapbox/streets-v12",
  center: [tsLng, tsLat],
  zoom: 15,
});

const activeMarkers = {};
let bellCount = 0;

map.on("load", () => {
  // 1. Vẽ vị trí trụ sở (Hiện ngay lập tức)
  new mapboxgl.Marker({ color: "#10b981" })
    .setLngLat([tsLng, tsLat])
    .setPopup(new mapboxgl.Popup().setHTML("<b>Vị trí trụ sở</b>"))
    .addTo(map);

  // 2. Nạp dữ liệu song song (Parallel Loading)
  Promise.all([loadExistingSOS(), loadExistingSuCo()]).then(() => {
    console.log("Tất cả icon đã nạp xong");
    connectWebSocket();
    handleRedirectParams();
  });
});

map.on("click", () => {
  closeDetail();
});

function closeDetail() {
  const panel = document.getElementById("sos-detail-panel");
  if (panel) panel.style.display = "none";
  removeRoute();
}

function removeRoute() {
  // Phải xóa Layer trước khi xóa Source
  if (map.getLayer("route")) map.removeLayer("route");
  if (map.getSource("route")) map.removeSource("route");
}

async function drawRoute(targetLng, targetLat) {
  try {
    const query = await fetch(
      `https://api.mapbox.com/directions/v5/mapbox/driving/${tsLng},${tsLat};${targetLng},${targetLat}?geometries=geojson&access_token=${mapboxgl.accessToken}`,
    );

    const json = await query.json();
    if (!json.routes || !json.routes[0]) {
      // Nếu không tìm thấy đường đi, hãy xóa route cũ đang hiển thị (nếu có)
      removeRoute();
      return;
    }

    const geojsonData = {
      type: "Feature",
      geometry: json.routes[0].geometry,
    };

    // KIỂM TRA: Nếu đã có Source "route" trên bản đồ rồi
    if (map.getSource("route")) {
      // Chỉ cần cập nhật lại dữ liệu tọa độ mới, không tạo lại nữa
      map.getSource("route").setData(geojsonData);

      // Nếu Layer bị mất (do hàm khác xóa nhầm), tạo lại Layer
      if (!map.getLayer("route")) {
        map.addLayer({
          id: "route",
          type: "line",
          source: "route",
          layout: { "line-join": "round", "line-cap": "round" },
          paint: {
            "line-color": "#3b82f6",
            "line-width": 6,
            "line-opacity": 0.8,
          },
        });
      }
    } else {
      // TRƯỜNG HỢP CHƯA CÓ: Tạo mới hoàn toàn cả Source và Layer
      map.addSource("route", {
        type: "geojson",
        data: geojsonData,
      });

      map.addLayer({
        id: "route",
        type: "line",
        source: "route",
        layout: { "line-join": "round", "line-cap": "round" },
        paint: {
          "line-color": "#3b82f6",
          "line-width": 6,
          "line-opacity": 0.8,
        },
      });
    }
  } catch (err) {
    console.error("Lỗi vẽ route:", err);
  }
}

function anKhoiBanDo(id) {
  const markerKey = "SOS_" + id;
  if (activeMarkers[markerKey]) {
    // Xóa marker vật lý trên bản đồ Mapbox
    activeMarkers[markerKey].marker.remove();
    // Xóa khỏi object lưu trữ dữ liệu
    delete activeMarkers[markerKey];
    // Đóng panel thông tin chi tiết lại
    closeDetail();
    console.log(`Đã ẩn SOS bản ghi số [${id}] khỏi bản đồ.`);
  }
}
function showSOSDetail(item) {
  const panel = document.getElementById("sos-detail-panel");
  const content = document.getElementById("panel-content");
  if (!panel || !content) return;
  panel.style.display = "flex";

  const user = item.nguoiGui || {};
  const userName = user.name || "Khách vãng lai";
  const userEmail = user.email || "Không có email";
  const isPriority = user.vip === true;

  const id = item.id;
  const kinhDo = (item.kinhDo || 0).toFixed(6);
  const viDo = (item.viDo || 0).toFixed(6);
  const ghiChu = item.ghiChu || "Không có ghi chú";
  const thoiGian = item.thoiGianTao
    ? new Date(item.thoiGianTao).toLocaleString("vi-VN")
    : "Vừa xong";

  const fixUrl = (path) => {
    if (!path) return null;
    if (path.startsWith("/uploads") || path.startsWith("http")) return path;
    return `/uploads/sos/${path}`;
  };

  const imgUrl = fixUrl(item.hinhAnhUrl);
  const audioUrl = fixUrl(item.ghiAmUrl);

  // 1. Chuẩn hóa dữ liệu đầu vào: Ép in hoa và xóa sạch khoảng trắng thừa
  let trangThai = item.trangThai || "DANG_DI_CHUYEN";
  if (typeof trangThai === "string") {
    trangThai = trangThai.toUpperCase().trim();
  }

  const statusLabel = {
    DA_TIEP_NHAN: "Đã tiếp nhận",
    DANG_DI_CHUYEN: "Đang di chuyển",
    DANG_XU_LY: "Đang xử lý",
    HOAN_THANH: "Hoàn thành",
    HUY_BO: "Hủy bỏ",
  };

  // 2. Kiểm tra và render chính xác nút bấm theo chuỗi đã chuẩn hóa
  let actionButton = "";
  if (trangThai === "DA_TIEP_NHAN") {
    actionButton = `<button class="btn-approve" onclick="doiTrangThai(${id}, 'DANG_DI_CHUYEN')">
                        <i class="fa-solid fa-bell"></i> XUẤT PHÁT CỨU HỘ</button>`;
  } else if (trangThai === "DANG_DI_CHUYEN") {
    actionButton = `<button class="btn-approve" style="background:#f59e0b" onclick="doiTrangThai(${id}, 'DANG_XU_LY')">
                        <i class="fa-solid fa-truck-fast"></i> BẮT ĐẦU CỨU HỘ</button>`;
  } else if (trangThai === "DANG_XU_LY") {
    actionButton = `<button class="btn-approve" style="background:#10b981" onclick="doiTrangThai(${id}, 'HOAN_THANH')">
                        <i class="fa-solid fa-check-double"></i> CỨU HỘ HOÀN TẤT</button>`;
  } else if (trangThai === "HOAN_THANH") {
    actionButton = `<button class="btn-approve" style="background:#64748b; width:100%;" onclick="anKhoiBanDo(${id})">
                        <i class="fa-solid fa-eye-slash"></i> ẨN KHỎI BẢN ĐỒ NGAY</button>`;
  } else {
    // Trường hợp phòng hờ nếu có trạng thái lạ, hiển thị nút vô hiệu hóa để không bị tàng hình
    actionButton = `<button class="btn-approve" style="background:#94a3b8; cursor:not-allowed;" disabled>
                        <i class="fa-solid fa-exclamation-triangle"></i> TRẠNG THÁI KHÔNG XÁC ĐỊNH (${trangThai})</button>`;
  }

  content.innerHTML = `
        ${isPriority ? `<div style="background: linear-gradient(90deg, #FFD700, #FFA500); color: #000; padding: 5px 10px; border-radius: 5px; font-weight: bold; margin-bottom: 15px; text-align: center; font-size: 0.8rem;"><i class="fa-solid fa-gem"></i> NGƯỜI DÙNG ƯU TIÊN (KIM CƯƠNG)</div>` : ""}

        <div class="info-group">
            <div class="info-label">NGƯỜI GỬI TÍN HIỆU</div>
            <div class="info-value" style="font-weight: bold; font-size: 1.1rem; color: #1e293b;">${userName}</div>
            <div class="info-value" style="font-size: 0.85rem; color: #64748b;">${userEmail}</div>
        </div>

        <div class="info-group">
            <div class="info-label">Trạng thái & Thời gian</div>
            <div class="info-value">
                <span class="badge" style="background: #fee2e2; color: #ef4444; padding: 2px 8px; border-radius: 4px;"> ${statusLabel[trangThai] || trangThai}</span>
                <span style="margin-left: 10px; font-size: 0.85rem;"><i class="fa-regular fa-clock"></i> ${thoiGian}</span>
            </div>
        </div>

        <div class="info-group">
            <div class="info-label">Ghi chú hiện trường</div>
            <div class="info-value" style="background: #fff; border: 1px solid #e2e8f0; padding: 10px; border-radius: 6px; font-style: italic;">"${ghiChu}"</div>
        </div>

        <div class="info-group">
            <div class="info-label">Hình ảnh hiện trường</div>
            ${
              imgUrl
                ? `<img src="${imgUrl}" class="sos-image" style="width:100%; border-radius:8px; margin-top:5px; cursor:pointer;" onerror="this.src='https://placehold.co/400x300?text=Không+tìm+thấy+ảnh'" onclick="window.open(this.src)">`
                : '<div class="info-value">Không có ảnh</div>'
            }
        </div>

        <div class="info-group">
            <div class="info-label">Ghi âm khẩn cấp</div>
            ${
              audioUrl
                ? `<audio controls style="width: 100%; margin-top:5px;"><source src="${audioUrl}" type="audio/mpeg">Trình duyệt không hỗ trợ nghe audio.</audio>`
                : '<div class="info-value">Không có ghi âm</div>'
            }
        </div>

        <div style="margin-top: 20px; border-top: 1px solid #eee; padding-top: 15px;">
            ${actionButton}
        </div>
    `;

  map.flyTo({
    center: [parseFloat(kinhDo), parseFloat(viDo)],
    zoom: 17,
    speed: 1.2,
  });
  drawRoute(parseFloat(kinhDo), parseFloat(viDo));
}
function showSuCoDetail(item) {
  console.log("Dữ liệu sự cố xử lý:", item);

  const panel = document.getElementById("sos-detail-panel");
  const content = document.getElementById("panel-content");
  if (!panel || !content) return;
  panel.style.display = "flex";

  const id = item.id;
  const tenLoai = item.tenLoai || "Sự cố";
  const moTa = item.moTa || "Không có mô tả";
  const icon = item.iconUrl || "https://placehold.co/24x24?text=⚠";

  // =========================================================================
  // XỬ LÝ ĐỒNG BỘ MỨC ĐỘ (Chấp nhận cả trường tiếng Anh từ WebSocket lẫn tiếng Việt từ DTO)
  // =========================================================================
  const rawMucDo = item.mucDoSuCo;

  let mucDoKey = "LOW";
  let mucDoHienThi = "Thấp";

  if (rawMucDo === "HIGH") {
    mucDoKey = "HIGH";
    mucDoHienThi = "Cao";
  } else if (rawMucDo === "MEDIUM") {
    mucDoKey = "MEDIUM";
    mucDoHienThi = "Trung bình";
  } else if (rawMucDo === "NONE") {
    mucDoKey = "NONE";
    mucDoHienThi = "Không có";
  } else if (rawMucDo === "LOW") {
    mucDoKey = "LOW";
    mucDoHienThi = "Thấp";
  }

  const badgeColor =
    mucDoKey === "HIGH"
      ? "#ef4444"
      : mucDoKey === "MEDIUM"
        ? "#f59e0b"
        : "#10b981";

  // =========================================================================
  // XỬ LÝ TRẠNG THÁI XỬ LÝ
  // =========================================================================
  let trangThai = item.trangThaiXuLy || item.trangThai || "DANG_DI_CHUYEN";
  if (typeof trangThai === "string") {
    trangThai = trangThai.toUpperCase();
  }

  console.log("Trạng thái thực tế khi vẽ giao diện:", trangThai);
  const diaChi = item.diaChi || "Không có địa chỉ";
  const tenNguoiBao = item.tenNguoiBao || "Khách vãng lai";
  const doTinCay = item.doTinCay !== undefined ? item.doTinCay : 0;

  const thoiGian = item.thoiGianTao
    ? new Date(item.thoiGianTao).toLocaleString("vi-VN")
    : "Vừa xong";

  const fixUrl = (path) => {
    if (!path) return null;
    if (path.startsWith("/uploads") || path.startsWith("http")) return path;
    return `/uploads/suco/${path}`;
  };
  const imgUrl = fixUrl(item.hinhAnhUrl);

  const statusLabels = {
    DANG_DI_CHUYEN: "Đang di chuyển",
    "ĐANG DI CHUYỂN": "Đang di chuyển",
    DANG_XU_LY: "Đang xử lý",
    "ĐANG XỬ LÝ": "Đang xử lý",
    HOAN_THANH: "Hoàn thành",
    "HOÀN THÀNH": "Hoàn thành",
    HUY_BO: "Hủy bỏ",
    "HỦY BỎ": "Hủy bỏ",
  };

  let actionButton = "";

  const laChoXuLy =
    trangThai === "DANG_DI_CHUYEN" || trangThai === "ĐANG DI CHUYỂN";
  const laDangXuLy = trangThai === "DANG_XU_LY" || trangThai === "ĐANG XỬ LÝ";
  const laHoanThanh = trangThai === "HOAN_THANH" || trangThai === "HOÀN THÀNH";

  if (laChoXuLy) {
    actionButton = `
      <button class="btn-approve" style="background:#1f2937; width:100%; padding:12px; color:white; font-weight:bold; border:none; border-radius:6px; cursor:pointer;" 
              onclick="doiTrangThaiSuCo(${id}, 'DANG_XU_LY')">
          <i class="fa-solid fa-person-digging"></i> TIẾP NHẬN XỬ LÝ
      </button>`;
  } else if (laDangXuLy) {
    actionButton = `
      <div style="border: 1px dashed #f59e0b; padding: 12px; border-radius: 8px; background: #fffdf5;">
          <div class="info-label" style="font-weight:bold; margin-bottom:8px; color:#b45309;">CẬP NHẬT MỨC ĐỘ NGUY HIỂM</div>
          <select id="select-muc-do-${id}" class="form-control" style="width: 100%; padding: 8px; border-radius: 5px; margin-bottom: 10px; border: 1px solid #ddd; color: #000;">
              <option value="LOW" ${mucDoKey === "LOW" ? "selected" : ""}>THẤP (LOW)</option>
              <option value="MEDIUM" ${mucDoKey === "MEDIUM" ? "selected" : ""}>TRUNG BÌNH (MEDIUM)</option>
              <option value="HIGH" ${mucDoKey === "HIGH" ? "selected" : ""}>CAO (HIGH)</option>
          </select>
          
          <button class="btn-approve" style="background:#f59e0b; color:white; width:100%; padding:8px; margin-bottom: 8px; border:none; border-radius:5px; cursor:pointer; font-size: 0.85rem;" 
                  onclick="updateMucDo(${id})">
              <i class="fa-solid fa-pen-to-square"></i> CẬP NHẬT MỨC ĐỘ
          </button>
          
          <button class="btn-approve" style="background:#10b981; color:white; width:100%; padding:10px; border:none; border-radius:5px; cursor:pointer; font-weight:bold;" 
                  onclick="doiTrangThaiSuCo(${id}, 'HOAN_THANH')">
              <i class="fa-solid fa-check-double"></i> XÁC NHẬN HOÀN THÀNH
          </button>
      </div>`;
  } else if (laHoanThanh) {
    actionButton = `
      <div style="text-align:center; color: #10b981; font-weight: bold; padding: 12px; border: 1px solid #10b981; border-radius: 8px; background: #f0fdf4;">
          <i class="fa-solid fa-circle-check"></i> SỰ CỐ ĐÃ ĐƯỢC XỬ LÝ HOÀN THÀNH
      </div>`;
  }

  content.innerHTML = `
      <div class="info-group">
          <div class="info-label">NGƯỜI BÁO SỰ CỐ</div>
          <div class="info-value" style="font-weight: bold; font-size: 1.05rem; color: #1e293b;">${tenNguoiBao}</div>
      </div>

      <div class="info-group">
          <div class="info-label">ĐỘ TIN CẬY (ĐÁNH GIÁ THỰC TẾ)</div>
          <div style="display: flex; align-items: center; gap: 8px; margin-top: 5px;">
              <div style="flex: 1; background: #e2e8f0; height: 8px; border-radius: 4px; overflow: hidden;">
                  <div style="background: linear-gradient(90deg, #3b82f6, #10b981); width: ${Math.min(doTinCay, 100)}%; height: 100%;"></div>
              </div>
              <span style="font-weight: bold; font-size: 0.85rem; color: #3b82f6;">${doTinCay}%</span>
          </div>
      </div>

      <div class="info-group">
          <div class="info-label">LOẠI SỰ CỐ</div>
          <div class="info-value" style="font-weight: bold; display: flex; align-items: center; gap: 8px; color: #0f172a;">
              <img src="${icon}" width="24" height="24" onerror="this.src='https://placehold.co/24x24?text=⚠'">
              <span>${tenLoai}</span>
          </div>
      </div>

      <div class="info-group">
          <div class="info-label">Mức độ & Trạng thái</div>
          <div style="margin-top: 5px;">
              <span id="badge-muc-do" style="background: ${badgeColor}; color: white; padding: 3px 10px; border-radius: 4px; font-size: 11px; font-weight: bold;">${mucDoHienThi}</span>
              <span style="background: #e2e8f0; color: #475569; padding: 3px 10px; border-radius: 4px; font-size: 11px; margin-left: 5px; font-weight: bold;">${statusLabels[trangThai] || trangThai}</span>
          </div>
          <div style="font-size: 0.8rem; color: #64748b; margin-top: 6px;">
              <i class="fa-regular fa-clock"></i> Thời gian: ${thoiGian}
          </div>
      </div>

      <div class="info-group">
          <div class="info-label">Địa chỉ hiện trường</div>
          <div class="info-value" style="font-size: 0.9rem; color: #334155;"><i class="fa-solid fa-location-dot" style="color:#ef4444;"></i> ${diaChi}</div>
      </div>

      <div class="info-group">
          <div class="info-label">Mô tả chi tiết</div>
          <div class="info-value" style="background: #fffbeb; padding: 12px; border-radius: 8px; border-left: 4px solid #f59e0b; font-style: italic; color: #78350f;">
              "${moTa}"
          </div>
      </div>

      <div class="info-group">
          <div class="info-label">Hình ảnh hiện trường</div>
          ${
            imgUrl
              ? `<img src="${imgUrl}" class="sos-image" style="width:100%; border-radius:8px; margin-top:5px; cursor:pointer;" onerror="this.src='https://placehold.co/400x300?text=Không+tìm+thấy+hình+ảnh'" onclick="window.open(this.src)">`
              : '<div class="info-value" style="color: #94a3b8; font-style: italic;">Không có ảnh đính kèm</div>'
          }
      </div>

      <div style="margin-top: 20px; border-top: 1px solid #e2e8f0; padding-top: 15px;">
          ${actionButton}
      </div>
  `;

  if (item.kinhDo && item.viDo) {
    const kDo = parseFloat(item.kinhDo);
    const vDo = parseFloat(item.viDo);

    map.flyTo({
      center: [kDo, vDo],
      zoom: 17,
      speed: 1.2,
    });
    drawRoute(kDo, vDo);
  }
}
function updateMucDo(id) {
  const mucDoSelect = document.getElementById(`select-muc-do-${id}`);
  if (!mucDoSelect) return;
  const mucDo = mucDoSelect.value; // Lấy ra "LOW", "MEDIUM", hoặc "HIGH"

  console.log("Dữ liệu chuẩn bị gửi lên Backend:", { mucDo: mucDo });

  fetch(`/su-co/cap-nhat-muc-do/${id}`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    // BẮT BUỘC: Key phải viết đúng là "mucDo" để trùng khớp với MucDoSuCoRequestDTO ở Backend
    body: JSON.stringify({ mucDo: mucDo }),
  }).then(async (res) => {
    if (res.ok) {
      alert("Đã cập nhật mức độ nghiêm trọng thành công!");

      const markerKey = "SU_CO_" + id;
      if (activeMarkers[markerKey]) {
        // Tải lại chi tiết mới nhất từ DB về render lại giao diện
        const detailRes = await fetch(`/su-co/chi-tiet/${id}`);
        if (detailRes.ok) {
          const fullData = await detailRes.json();
          activeMarkers[markerKey].data = fullData;
          showSuCoDetail(fullData); // Hàm hiển thị (đã đồng bộ trường ở câu trả lời trước)
          addSOSMarker(fullData, "SU_CO"); // Vẽ lại marker đổi màu
        }
      }
    } else {
      res.text().then((text) => alert("Lỗi từ hệ thống: " + text));
    }
  });
}
function addSOSMarker(item, type = "SOS") {
  const markerKey = type + "_" + item.id;
  const trangThai =
    type === "SOS" ? item.trangThai : item.trangThaiXuLy || item.trangThai;

  // 1. Quản lý xóa bỏ ghim khi trạng thái kết thúc/bị hủy
  const forceDeleteStatus = ["HUY_BO", "DA_HUY", "DA_AN", "SPAM"];
  if (forceDeleteStatus.includes(trangThai)) {
    if (activeMarkers[markerKey]) {
      activeMarkers[markerKey].marker.remove();
      delete activeMarkers[markerKey];
      if (typeof closeDetail === "function") closeDetail();
    }
    return;
  }

  // 2. Đọc tọa độ linh hoạt (hỗ trợ cả trường tiếng Anh lẫn tiếng Việt từ API)
  const lng = parseFloat(item.kinhDo || item.longitude);
  const lat = parseFloat(item.viDo || item.latitude);

  if (isNaN(lng) || isNaN(lat)) {
    console.warn(`Bỏ qua marker ${markerKey} do sai tọa độ:`, item);
    return;
  }

  let themeColor = "#94a3b8"; // Mặc định xám khi chưa có mức độ
  let isPulse = "";

  // =========================================================================
  // PHÂN CHIA LOGIC MÀU SẮC RIÊNG BIỆT CHO SỰ CỐ VÀ SOS
  // =========================================================================
  if (type === "SU_CO") {
    const mDo = item.mucDoSuCo || item.mucDo;
    if (mDo && mDo !== "NONE") {
      if (mDo === "HIGH")
        themeColor = "#ef4444"; // Đỏ - Cao
      else if (mDo === "MEDIUM")
        themeColor = "#f59e0b"; // Vàng - Trung bình
      else if (mDo === "LOW") themeColor = "#10b981"; // Xanh lá - Thấp
    }
  } else {
    // SOS không có mức độ -> chỉ ăn màu và hiệu ứng bung theo TRẠNG THÁI
    if (trangThai === "DANG_DI_CHUYEN") {
      themeColor = "#ff0000"; // Đỏ tĩnh
      isPulse = "";
    } else if (trangThai === "DA_TIEP_NHAN") {
      themeColor = "#ff0000"; // Vẫn đỏ
      isPulse = "marker-pulse"; // Bật hiệu ứng sóng bung ra ra ngoài
    } else if (trangThai === "DANG_XU_LY" || trangThai === "DANG_CUU_TRO") {
      themeColor = "#f59e0b"; // Vàng / Cam
      isPulse = "";
    } else if (trangThai === "HOAN_THANH") {
      themeColor = "#10b981"; // Xanh lá
      isPulse = "";
    }
  }

  // =========================================================================
  // XỬ LÝ CẬP NHẬT TRỰC TIẾP NẾU MARKER ĐÃ TỒN TẠI TRÊN DOM
  // =========================================================================
  if (activeMarkers[markerKey]) {
    activeMarkers[markerKey].data = item;
    const el = activeMarkers[markerKey].marker.getElement();

    if (type === "SU_CO") {
      const pin = el.querySelector(".marker-pin-main");
      const tail = el.querySelector(".marker-tail-fix");
      if (pin) pin.style.borderColor = themeColor;
      if (tail) tail.style.borderTopColor = themeColor;
    } else {
      const dot = el.querySelector(".simple-dot");
      if (dot) {
        dot.style.backgroundColor = themeColor;
        if (isPulse) {
          dot.classList.add("marker-pulse");
        } else {
          dot.classList.remove("marker-pulse");
        }
      }
    }
    return;
  }

  // =========================================================================
  // XỬ LÝ TẠO MỚI HOÀN TOÀN CẤU TRÚC GIAO DIỆN MARKER (HTML INNER)
  // =========================================================================
  const el = document.createElement("div");
  el.className = "custom-marker-wrapper";

  if (type === "SU_CO") {
    // Trả lại nguyên vẹn thiết kế Giọt nước + Đuôi nhọn + Icon loại sự cố của bạn
    const pulseClass = trangThai === "DANG_DI_CHUYEN" ? "marker-pulse" : "";
    const iconUrl =
      item.iconUrl || "https://cdn-icons-png.flaticon.com/512/564/564619.png";

    el.innerHTML = `
        <div class="marker-pin-main ${pulseClass}" style="border-color: ${themeColor}">
            <img src="${iconUrl}" alt="icon">
        </div>
        <div class="marker-tail-fix" style="border-top-color: ${themeColor}"></div>
    `;
  } else {
    // Thiết kế chấm tròn đơn giản của SOS (Có vương miện nếu là tài khoản VIP)
    const isVip = item.isVip || item.vip;
    const crownHtml = isVip
      ? `<div style="position: absolute; top: -20px; left: 50%; transform: translateX(-50%); color: #FFD700; font-size: 18px; text-shadow: 1px 1px 3px rgba(0,0,0,0.6); z-index: 10;"><i class="fa-solid fa-crown"></i></div>`
      : "";

    el.innerHTML = `
        ${crownHtml}
        <div class="simple-dot ${isPulse}" 
             style="background-color: ${themeColor}; width: 24px; height: 24px; border-radius: 50%; border: 3px solid white; box-shadow: 0 0 10px rgba(0,0,0,0.5); cursor: pointer;">
        </div>
    `;
  }

  // Khởi tạo Marker lên bản đồ Mapbox với anchor chuẩn xác từng loại
  const marker = new mapboxgl.Marker({
    element: el,
    anchor: type === "SU_CO" ? "bottom" : "center",
  })
    .setLngLat([lng, lat])
    .addTo(map);

  activeMarkers[markerKey] = { marker, data: item, type };

  // Đăng ký sự kiện click mở Panel chi tiết (Đã đồng bộ tên hàm sang tiếng Anh)
  el.addEventListener("click", async (e) => {
    e.stopPropagation();
    activateMarkerDetail(markerKey, type);
  });

  // Tự động ẩn khỏi bản đồ sau 3 phút nếu trạng thái là HOAN_THANH
  if (type === "SOS" && trangThai === "HOAN_THANH") {
    setTimeout(() => {
      if (typeof anKhoiBanDo === "function") {
        anKhoiBanDo(item.id);
      } else if (activeMarkers[markerKey]) {
        activeMarkers[markerKey].marker.remove();
        delete activeMarkers[markerKey];
      }
    }, 180000);
  }
}

async function activateMarkerDetail(markerKey, type) {
  const latestData = activeMarkers[markerKey].data;

  if (type === "SU_CO") {
    try {
      const res = await fetch(`/su-co/chi-tiet/${latestData.id}`);
      if (res.ok) {
        const fullDetailData = await res.json();
        activeMarkers[markerKey].data = fullDetailData;
        showSuCoDetail(fullDetailData);
      } else {
        showSuCoDetail(latestData);
      }
    } catch (err) {
      console.error("Lỗi kết nối API chi tiết sự cố:", err);
      showSuCoDetail(latestData);
    }
  } else {
    try {
      const res = await fetch(`/sos/chi-tiet/${latestData.id}`);

      if (res.ok) {
        const fullDetailSOS = await res.json(); // TruSoSOSDetailResponseDTO đầy đủ trường
        activeMarkers[markerKey].data = fullDetailSOS; // Ghi đè vào bộ nhớ tạm
        showSOSDetail(fullDetailSOS); // Đổ dữ liệu lên Panel bên phải
      } else {
        showSOSDetail(latestData); // Dự phòng dữ liệu cũ nếu lỗi
      }
    } catch (err) {
      console.error("Lỗi khi kết nối API chi tiết SOS:", err);
      showSOSDetail(latestData);
    }
  }
}
function handleRedirectParams() {
  const params = new URLSearchParams(window.location.search);
  const toLat = params.get("toLat");
  const toLng = params.get("toLng");
  const sosId = params.get("sosId");

  if (toLat && toLng) {
    drawRoute(parseFloat(toLng), parseFloat(toLat));
    map.flyTo({
      center: [parseFloat(toLng), parseFloat(toLat)],
      zoom: 17,
      speed: 1.2,
    });
  }

  if (sosId) {
    const markerKey = "SOS_" + sosId;
    let retryCount = 0;
    const maxRetries = 10; // Tăng số lần thử (Tổng thời gian chờ tối đa 5 giây)

    const showIfReady = () => {
      const entry = activeMarkers[markerKey];
      if (entry) {
        // ĐỒNG BỘ: Kích hoạt hàm gọi API chi tiết chuẩn giống như khi click vào Marker
        activateMarkerDetail(markerKey, "SOS");
      } else if (retryCount < maxRetries) {
        retryCount++;
        setTimeout(showIfReady, 500); // Thử lại sau mỗi 500ms nếu bản đồ chưa nạp xong ghim
      } else {
        console.warn(
          `Không tìm thấy Marker cứu hộ với Key: ${markerKey} sau 5 giây.`,
        );
      }
    };

    showIfReady();
  }
}

function toggleNotiList() {
  const list = document.getElementById("noti-list");
  if (list)
    list.style.display = list.style.display === "none" ? "block" : "none";
}

function addNotiItem(data, type, isInitialLoad = false) {
  const container = document.getElementById("noti-items-container");
  const emptyMsg = document.getElementById("empty-noti");
  if (!container) return;

  const notiId = `noti-item-${type}-${data.id}`;
  if (document.getElementById(notiId)) return;

  if (emptyMsg) emptyMsg.remove();

  if (!isInitialLoad) {
    bellCount++;
    const countEl = document.getElementById("noti-count");
    if (countEl) {
      countEl.style.display = "flex";
      countEl.innerText = bellCount;
    }
  }

  const time = data.thoiGianTao
    ? new Date(data.thoiGianTao).toLocaleTimeString("vi-VN")
    : "Vừa xong";
  const isVip =
    data.isVip || data.vip || (data.user && data.user.totalPoints >= 500);

  const itemHtml = `
  <div class="noti-item" id="${notiId}" onclick="focusOnMarker('${type}_${data.id}')" 
       style="padding: 12px; border-bottom: 1px solid #f1f5f9; cursor: pointer; border-left: 4px solid ${type === "SOS" ? "#ef4444" : "#f59e0b"};">
      <div style="display: flex; gap: 10px; align-items: center;">
          <div style="flex: 1;">
              <div style="font-size: 13px; font-weight: 600;">${type === "SOS" ? "Tín hiệu SOS mới" : "Sự cố mới"}</div>
              <div style="font-size: 11px; color: #64748b;">${isVip ? "💎 Gói ưu tiên" : "Thông thường"} • ${time}</div>
          </div>
      </div>
  </div>`;

  container.insertAdjacentHTML("afterbegin", itemHtml);
}

function clearNoti(e) {
  e.stopPropagation();
  bellCount = 0;
  const countEl = document.getElementById("noti-count");
  if (countEl) countEl.style.display = "none";
  document.getElementById("noti-items-container").innerHTML =
    '<div id="empty-noti" style="padding: 20px; text-align: center; color: #94a3b8; font-size: 13px;">Không có thông báo mới</div>';
}

function focusOnMarker(markerKey) {
  const entry = activeMarkers[markerKey];
  if (entry) {
    kíchHoạtChiTiếtTừMarker(markerKey, entry.type);
  }
}

const playAlarm = () => {
  const audio = new Audio(
    "https://actions.google.com/sounds/v1/alarms/beep_short.ogg",
  );
  audio.volume = 1.0;
  audio.play().catch((e) => {
    console.warn("Trình duyệt chặn tự động phát âm thanh.");
  });
};

function connectWebSocket() {
  const socket = new SockJS("/ws-suco-web");
  const stompClient = Stomp.over(socket);
  stompClient.debug = null;
  stompClient.connect({}, () => {
    // --- 1. KÊNH SOS ---
    stompClient.subscribe("/topic/truso/" + idTruSo, (msg) => {
      const sosData = JSON.parse(msg.body);
      const markerKey = "SOS_" + sosData.id;

      if (
        !activeMarkers[markerKey] &&
        !document.getElementById(`noti-item-SOS-${sosData.id}`)
      ) {
        playAlarm();
        addNotiItem(sosData, "SOS", false);
      }

      if (sosData.trangThai === "HOAN_THANH") {
        if (activeMarkers[markerKey]) {
          activeMarkers[markerKey].marker.remove();
          delete activeMarkers[markerKey];
          closeDetail();
        }
      } else {
        addSOSMarker(sosData, "SOS");
        if (activeMarkers[markerKey]) activeMarkers[markerKey].data = sosData;
      }

      const panel = document.getElementById("sos-detail-panel");
      if (panel && panel.style.display === "flex" && activeMarkers[markerKey]) {
        showSOSDetail(sosData);
      }
    });

    // --- 2. KÊNH ĐIỀU PHỐI ---
    stompClient.subscribe("/topic/truso/" + idTruSo + "/dieu-phoi", (msg) => {
      try {
        const thongBao = JSON.parse(msg.body);
        xuLyThongBaoDieuPhoiBanDo(thongBao);
      } catch (e) {
        console.error("Lỗi xử lý điều phối:", e);
      }
    });

    // --- 3. KÊNH XÓA SỰ CỐ (SPAM) ---
    stompClient.subscribe("/topic/su-co-delete", (msg) => {
      const reportId = JSON.parse(msg.body);
      const markerKey = "SU_CO_" + reportId;
      if (activeMarkers[markerKey]) {
        activeMarkers[markerKey].marker.remove();
        delete activeMarkers[markerKey];
        closeDetail();
      }
    });

    // --- 4. KÊNH SỰ CỐ TỔNG ---
    stompClient.subscribe("/topic/su-co", (msg) => {
      const updatedSuCo = JSON.parse(msg.body);
      const tThai = updatedSuCo.trangThaiXuLy || updatedSuCo.trangThai;

      if (tThai === "HUY_BO") return;
      const markerKey = "SU_CO_" + updatedSuCo.id;

      if (
        !activeMarkers[markerKey] &&
        tThai !== "HOAN_THANH" &&
        !document.getElementById(`noti-item-SU_CO-${updatedSuCo.id}`)
      ) {
        playAlarm();
        addNotiItem(updatedSuCo, "SU_CO", false);
      }

      if (tThai === "HOAN_THANH") {
        if (activeMarkers[markerKey]) {
          activeMarkers[markerKey].marker.remove();
          delete activeMarkers[markerKey];
          closeDetail();
        }
      } else {
        addSOSMarker(updatedSuCo, "SU_CO");
        if (activeMarkers[markerKey])
          activeMarkers[markerKey].data = updatedSuCo;
      }
    });

    // --- 5. KÊNH SỰ CỐ RIÊNG CỦA TRỤ SỞ ---
    stompClient.subscribe("/topic/tru-so/" + idTruSo + "/su-co", (msg) => {
      const data = JSON.parse(msg.body);
      const markerKey = "SU_CO_" + data.id;
      const tThai = data.trangThaiXuLy || data.trangThai;

      if (tThai === "HOAN_THANH") {
        if (activeMarkers[markerKey]) {
          activeMarkers[markerKey].marker.remove();
          delete activeMarkers[markerKey];
          closeDetail();
        }
        return;
      }

      addSOSMarker(data, "SU_CO");
      if (activeMarkers[markerKey]) activeMarkers[markerKey].data = data;

      const panel = document.getElementById("sos-detail-panel");
      if (panel && panel.style.display === "flex" && activeMarkers[markerKey]) {
        showSuCoDetail(data);
      }
    });
  });
}

function xuLyThongBaoDieuPhoiBanDo(thongBao) {
  const markerKey = "SOS_" + thongBao.idSos;
  if (thongBao.loaiThongBao === "XOA_SOS" && activeMarkers[markerKey]) {
    activeMarkers[markerKey].marker.remove();
    delete activeMarkers[markerKey];
    closeDetail();
  }
}

function doiTrangThaiSuCo(id, status) {
  fetch(`/su-co/cap-nhat-trang-thai/${id}`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ status: status }),
  }).then((res) => {
    if (res.ok) {
      const markerKey = "SU_CO_" + id;
      if (status === "HOAN_THANH") {
        if (activeMarkers[markerKey]) {
          activeMarkers[markerKey].marker.remove();
          delete activeMarkers[markerKey];
        }
        closeDetail();
        alert("Đã hoàn thành và gỡ bỏ sự cố!");
      } else {
        if (activeMarkers[markerKey]) {
          // Gán cả 2 kiểu để chắc chắn hàm vẽ lại nhận diện được trạng thái mới
          activeMarkers[markerKey].data.trangThaiXuLy = status;
          activeMarkers[markerKey].data.trangThai = status;

          // Vẽ lại Marker & vẽ lại Panel chi tiết ngay lập tức
          addSOSMarker(activeMarkers[markerKey].data, "SU_CO");
          showSuCoDetail(activeMarkers[markerKey].data);
        }
        alert("Đã tiếp nhận sự cố thành công!");
      }
    }
  });
}

function doiTrangThai(id, status) {
  if (
    !confirm("Bạn có chắc chắn muốn điều động lực lượng xử lý mục này chứ?")
  ) {
    return;
  }

  // 1. Tạo đúng cấu trúc Object tương ứng với TrangThaiSOSRequestDTO ở Backend
  const requestBody = {
    status: status, // Ví dụ: "DANG_XU_LY"
  };

  const url = `/sos/cap-nhat-trang-thai/${id}`;
  console.log("Đang gửi yêu cầu điều động SOS lên:", url, requestBody);

  // 2. Thực hiện FETCH cấu hình Method PATCH và gửi kèm JSON Body
  fetch(url, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json", // Bắt buộc khai báo để Spring Boot hiểu
    },
    body: JSON.stringify(requestBody), // Chuyển đổi Object thành chuỗi JSON
  })
    .then((res) => {
      if (res.ok) {
        alert("Điều động lực lượng xuất phát cứu hộ thành công!");

        // Cập nhật lại UI map nếu có quản lý danh sách marker đang chạy
        const markerKey = "SOS_" + id;
        if (typeof activeMarkers !== "undefined" && activeMarkers[markerKey]) {
          activeMarkers[markerKey].data.trangThai = status;

          if (status === "HOAN_THANH" || status === "DA_TIEP_NHAN") {
            // Tùy theo logic nghiệp vụ của bạn: ẩn đi hoặc load lại marker màu mới
            activeMarkers[markerKey].marker.remove();
            delete activeMarkers[markerKey];
            closeDetail();
          } else {
            showSOSDetail(activeMarkers[markerKey].data);
          }
        } else {
          // Nếu không quản lý mảng, chỉ cần reload lại trang hoặc cập nhật UI cục bộ
          if (typeof loadIncidents === "function") loadIncidents();
        }
      } else {
        // Nếu Backend trả về lỗi (Ví dụ: 401 do hết hạn session, hoặc lỗi logic service)
        res.text().then((text) => {
          alert(`Hệ thống từ chối thực thi (Mã lỗi ${res.status}): ` + text);
        });
      }
    })
    .catch((err) => {
      console.error("Lỗi kết nối mạng:", err);
      alert("Không thể kết nối đến máy chủ cập nhật trạng thái!");
    });
}
function loadExistingSuCo() {
  return fetch(`/api/su-co/map`)
    .then((res) => {
      if (!res.ok) {
        // Nếu API lỗi (400, 500...), ném lỗi ra để xử lý ở catch
        throw new Error(`API lỗi với status: ${res.status}`);
      }
      return res.json();
    })
    .then((data) => {
      // Kiểm tra chắc chắn data trả về phải là một Mảng
      if (Array.isArray(data)) {
        data.forEach((item) => {
          addSOSMarker(item, "SU_CO");
          addNotiItem(item, "SU_CO", true);
        });
      } else {
        console.error("Dữ liệu trả về không phải là mảng:", data);
      }
    })
    .catch((err) => console.error("Lỗi khi tải sự cố:", err));
}

function loadExistingSOS() {
  return fetch("/sos/hoat-dong")
    .then((res) => res.json())
    .then((data) => {
      Object.keys(activeMarkers).forEach((id) => {
        // Kiểm tra nếu đúng là loại SOS và có tồn tại instance marker thì mới xóa
        if (
          activeMarkers[id] &&
          activeMarkers[id].type === "SOS" &&
          activeMarkers[id].marker
        ) {
          activeMarkers[id].marker.remove(); // Thêm .marker vào đây
          delete activeMarkers[id];
        }
      });

      // Duyệt qua danh sách dữ liệu mới từ Backend trả về
      data.forEach((item) => {
        addSOSMarker(item, "SOS");
        if (typeof addNotiItem === "function") {
          addNotiItem(item, "SOS", true);
        }
      });
    })
    .catch((err) => console.error("Lỗi khi load danh sách SOS:", err));
}
async function saveStationConfig() {
  const payload = {
    trangThaiHoatDong: document.getElementById("trangThaiHoatDong").value,
  };
  const response = await fetch("/truso/config", {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    alert("Cập nhật thất bại");
    return;
  }
  alert("Đã cập nhật trạng thái");
}

function toggleScpDropdown(event) {
  event.stopPropagation();
  const panel = document.getElementById("station-control-panel");
  if (panel) panel.classList.toggle("show");
}

function updatePreviewDot() {
  const select = document.getElementById("trangThaiHoatDong");
  if (!select) return;
  const selectedText = select.options[select.selectedIndex].text;
  document.getElementById("station-status-text").innerText = selectedText;
}

function handleSaveStationConfig(event) {
  event.stopPropagation();
  saveStationConfig();
  const panel = document.getElementById("station-control-panel");
  if (panel) panel.classList.remove("show");
}

document.addEventListener("click", function (event) {
  const panel = document.getElementById("station-control-panel");
  if (panel && !panel.contains(event.target)) {
    panel.classList.remove("show");
  }
});
