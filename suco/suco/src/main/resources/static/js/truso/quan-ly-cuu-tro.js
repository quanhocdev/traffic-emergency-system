var CURRENT_VIEW = "sos";
function fixUrl(path) {
  if (!path) return null;
  if (path.startsWith("http") || path.startsWith("/uploads/")) {
    return path;
  }
  return `/uploads/sos/${path}`;
}
function formatTime(iso) {
  try {
    const d = new Date(iso);
    return d.toLocaleString();
  } catch (e) {
    return iso;
  }
}

const handledSOS = new Set();

function renderSOSItem(sos) {
  // Đồng bộ thông tin người gửi để tránh null lỗi
  const user = sos.user || sos.nguoiGui || {};
  const userName = user.name || "Khách vãng lai";
  const userEmail = user.email || "Không có email";
  const userPoints = user.totalPoints ?? 0;

  // Trích xuất thời gian và địa chỉ
  const time =
    sos.createdAt || sos.thoiGianTao
      ? new Date(sos.createdAt || sos.thoiGianTao).toLocaleString("vi-VN")
      : "";
  const displayAddress = sos.diaChi || `${sos.viDo}, ${sos.kinhDo}`;

  return `
    <div class="col-12 mb-2" id="sos-card-${sos.id}"
         data-lat="${sos.viDo || ""}"
         data-lng="${sos.kinhDo || ""}"
         data-points="${userPoints}"
         data-json='${encodeURIComponent(JSON.stringify(sos))}'>
      <div class="card card-sos ${userPoints > 100 ? "border-primary" : ""}" style="padding: 15px;">
        <div class="d-flex align-items-start w-100">
          
          <span class="badge bg-warning text-dark me-3 mt-1" style="min-width: 70px;">
            <i class="fa-solid fa-star"></i> ${userPoints} pts
          </span>

          <div class="flex-grow-1 d-flex flex-column">
              <div class="d-flex align-items-center flex-wrap mb-1" style="gap: 15px;">
                <span style="font-size: 1.05rem;"><strong>Người gửi:</strong> ${userName}</span>
                <span class="text-muted" style="font-size: 0.9rem;"><i class="fa-regular fa-envelope"></i> ${userEmail}</span>
                <i class="fa-solid fa-circle-info info-icon text-primary ms-2" style="cursor:pointer;" title="Xem chi tiết" onclick="openSOSDetail(${sos.id})"></i>
              </div>
              
              <div class="text-truncate mb-1" style="max-width: 100%;" title="${displayAddress}">
                <i class="fa-solid fa-location-dot text-danger"></i>
                <strong>Địa chỉ:</strong> ${displayAddress}
              </div>
          </div>

          <small class="text-muted ms-auto me-3 mt-1">${time}</small>

          <div class="btn-group-action d-flex mt-1">
            <button id="btn-accept-${sos.id}" onclick="confirmRescue(${sos.id})" class="btn btn-primary btn-sm shadow-sm me-1">
              <i class="fa-solid fa-check"></i> Bắt đầu xử lý
            </button>
            <button id="btn-decline-${sos.id}" onclick="tuChoiTiepNhan(${sos.id})" class="btn btn-outline-secondary btn-sm">
              <i class="fa-solid fa-xmark"></i> Không Xử lý
            </button>
          </div>

        </div>
      </div>
    </div>`;
}
// Lưu trữ interval đếm ngược cho từng SOS (để có thể clear khi cần)
var demNguocIntervals = {};

function appendSOS(sos) {
  const list = document.getElementById("sos-list");
  const noDataElem = document.getElementById("no-data");
  if (noDataElem) noDataElem.style.display = "none";
  // prepend so newest is first
  // If this SOS was just handled by this client, don't duplicate
  if (handledSOS.has(sos.id)) return;
  list.insertAdjacentHTML("afterbegin", renderSOSItem(sos));
}

function loadPendingSOS() {
  if (!TRUSO_ID || TRUSO_ID === 0) return;

  fetch("/truso/api/sos/dang-di-chuyen")
    .then((res) => res.json())
    .then((data) => {
      console.log(" Dữ liệu SOS ĐANG DI CHUYỂN (DANG_DI_CHUYEN) =", data);

      const list = document.getElementById("sos-list");

      if (!Array.isArray(data) || data.length === 0) {
        if (document.getElementById("no-data"))
          document.getElementById("no-data").style.display = "block";
        list.innerHTML = "";
        return;
      }

      // --- LOGIC SẮP XẾP THEO ĐIỂM VIP ---
      data.sort((a, b) => {
        const pointsA =
          a.nguoiGui && a.nguoiGui.totalPoints ? a.nguoiGui.totalPoints : 0;
        const pointsB =
          b.nguoiGui && b.nguoiGui.totalPoints ? b.nguoiGui.totalPoints : 0;
        if (pointsB !== pointsA) return pointsB - pointsA;
        return (
          new Date(b.thoiGianTao || b.createdAt) -
          new Date(a.thoiGianTao || a.createdAt)
        );
      });

      const pendingSOS = data;

      if (document.getElementById("no-data"))
        document.getElementById("no-data").style.display = "none";

      list.innerHTML = ""; // Xóa dòng chữ loading...

      // 2. Điền logic render thật vào đây để nó vẽ Card lên màn hình
      pendingSOS.forEach((sos) => {
        list.innerHTML += renderSOSItem(sos); // Gọi hàm render items gán vào list
      });
    })
    .catch((err) =>
      console.error("Lỗi khi load danh sách đang di chuyển:", err),
    );
}

// --- SỰ CỐ VIEW ---
function renderSuCoItem(suCo) {
  const id = suCo.id;
  const tenLoai = suCo.tenLoaiSuCo || suCo.tenLoai || "Sự cố";
  const displayAddress = suCo.diaChi || `${suCo.viDo}, ${suCo.kinhDo}`;

  // Lấy số lượt báo cáo trùng lặp / xác nhận thực tế (mặc định là 0 nếu chưa có ai xác nhận thêm)
  const doTinCay = suCo.doTinCay !== undefined ? suCo.doTinCay : 0;

  const time =
    suCo.createdAt || suCo.thoiGianTao || suCo.thoiGian
      ? new Date(
          suCo.createdAt || suCo.thoiGianTao || suCo.thoiGian,
        ).toLocaleString("vi-VN")
      : "Vừa xong";

  return `
    <div class="col-12 mb-2" id="suco-card-${id}" 
         data-lat="${suCo.viDo || ""}" 
         data-lng="${suCo.kinhDo || ""}" 
         data-json='${encodeURIComponent(JSON.stringify(suCo))}'>
      <div class="card card-sos" style="padding: 12px;">
        <div class="d-flex align-items-center w-100">
          
          <span class="badge bg-secondary me-3" style="min-width: 110px; text-align: center; padding: 8px; font-size: 0.85rem;">
              <i class="fa-solid fa-triangle-exclamation"></i> ${tenLoai}
          </span>
          
          <div class="flex-grow-1 d-flex align-items-center flex-wrap" style="gap: 20px;">
              
              <span class="text-truncate" style="max-width: 350px;" title="${displayAddress}">
                  <i class="fa-solid fa-location-dot text-danger"></i>
                  <strong>Địa chỉ:</strong> ${displayAddress}
              </span>
              
              <div class="d-flex align-items-center" style="gap: 6px;">
                  <i class="fa-solid fa-shield-halved text-primary"></i>
                  <strong>Độ tin cậy:</strong> 
                  <span class="badge bg-light text-primary border border-primary fw-bold" style="font-size: 0.85rem; padding: 3px 8px;">
                      ${doTinCay} lượt báo cáo
                  </span>
                  <i class="fa-solid fa-circle-info info-icon text-secondary ms-1" style="cursor:pointer;" title="Xem chi tiết" onclick="openSuCoDetail(${id})"></i>
              </div>

          </div>
          
          <small class="text-muted ms-auto me-3">${time}</small>
          
          <button class="btn btn-warning btn-sm fw-bold px-3 shadow-sm" onclick="doiTrangThaiSuCo(${id}, 'DANG_XU_LY')">
              <i class="fa-solid fa-truck-fire"></i> XUẤT PHÁT
          </button>
          
        </div>
      </div>
    </div>`;
}
function loadPendingSuCo() {
  if (!TRUSO_ID || TRUSO_ID === 0) return;

  fetch(`/truso/api/su-co/dang-di-chuyen`)
    .then((res) => {
      if (!res.ok) throw new Error("Mã lỗi: " + res.status);
      return res.json();
    })
    .then((data) => {
      console.log("Dữ liệu Sự cố nhận được từ Backend:", data);
      const list = document.getElementById("sos-list");
      list.innerHTML = "";

      const pendingData = data;

      const noDataElem = document.getElementById("no-data");
      if (!pendingData || pendingData.length === 0) {
        if (noDataElem) {
          noDataElem.style.display = "block";
          noDataElem.innerHTML = "Hiện không có sự cố nào đang chờ đề xuất!";
        }
        return;
      }

      if (noDataElem) noDataElem.style.display = "none";

      pendingData.forEach((suCo) => {
        list.innerHTML += renderSuCoItem(suCo);
      });
    })
    .catch((err) => {
      console.error("Lỗi tải sự cố:", err);
      const noDataElem = document.getElementById("no-data");
      if (noDataElem) {
        noDataElem.style.display = "block";
        noDataElem.innerHTML = "Lỗi kết nối hệ thống khi tải sự cố!";
      }
    });
}

function setActiveView(view) {
  CURRENT_VIEW = view;
  // Dọn dẹp tất cả các bộ đếm ngược đang chạy khi chuyển view
  Object.keys(demNguocIntervals).forEach((id) => dungDemNguocInline(id));
  Object.keys(demNguocSOS).forEach((id) => dungDemNguoc(id));
  const btnSuCo = document.getElementById("btn-su-co");
  const btnSos = document.getElementById("btn-sos");
  const list = document.getElementById("sos-list");

  // Reset danh sách hiển thị
  list.innerHTML =
    '<div class="text-center p-5"><i class="fa-solid fa-spinner fa-spin fa-2x"></i><p>Đang tải dữ liệu...</p></div>';
  if (view === "suco") {
    btnSuCo.classList.remove("btn-outline-secondary");
    btnSuCo.classList.add("btn-secondary");
    btnSos.classList.remove("btn-danger");
    btnSos.classList.add("btn-outline-danger");
    // load sự cố
    loadPendingSuCo();
  } else {
    // default to sos
    btnSos.classList.remove("btn-outline-danger");
    btnSos.classList.add("btn-danger");
    btnSuCo.classList.remove("btn-secondary");
    btnSuCo.classList.add("btn-outline-secondary");
    // load sos
    const list = document.getElementById("sos-list");
    list.innerHTML = "";
    loadPendingSOS();
  }
}

// Handle realtime messages for SỰ CỐ
function handleRealtimeSuCo(suCo) {
  const id = suCo.id;
  const status = suCo.trangThaiXuLy || suCo.status;
  const card = document.getElementById("suco-card-" + id);

  console.log(">>> WebSocket nhận sự cố:", id, "Trạng thái:", status);

  if (
    status === "HUY_BO" ||
    status === "DANG_XU_LY" ||
    status === "HOAN_THANH"
  ) {
    if (card) {
      dungDemNguocInline(id);
      card.remove();
      checkEmptyList();
    }
    return;
  }

  if (status === "DANG_DI_CHUYEN" && CURRENT_VIEW === "suco") {
    if (!card) {
      const list = document.getElementById("sos-list");
      const noData = document.getElementById("no-data");
      if (noData) noData.style.display = "none";
      if (list) {
        list.insertAdjacentHTML("afterbegin", renderSuCoItem(suCo));
      }
    }
  }
}

function handleRealtimeSOS(sos) {
  const status = sos.trangThai || sos.status;

  if (status === "DANG_DI_CHUYEN" && CURRENT_VIEW === "sos") {
    const list = document.getElementById("sos-list");
    const cardExist = document.getElementById("sos-card-" + sos.id);

    // Chỉ thêm nếu card này chưa tồn tại trong danh sách
    if (!cardExist) {
      const noData = document.getElementById("no-data");
      if (noData) noData.style.display = "none";

      // Chèn vào đầu danh sách
      list.insertAdjacentHTML("afterbegin", renderSOSItem(sos));

      // Phát âm thanh
      const audio = new Audio("/assets/sounds/alert.mp3");
      audio.play().catch((e) => {
        console.warn(
          "Âm thanh bị chặn bởi trình duyệt. Cần tương tác người dùng.",
        );
      });
    }
  } else if (status !== "DANG_DI_CHUYEN") {
    const card = document.getElementById("sos-card-" + sos.id);
    if (card) {
      dungDemNguocInline(sos.id);
      card.remove();
    }
    checkEmptyList();
  }
}
function checkEmptyList() {
  const list = document.getElementById("sos-list");
  // Đếm số lượng card thực tế còn lại (không tính div no-data)
  const cards = list.querySelectorAll(
    '.col-12[id^="sos-card-"], .col-12[id^="suco-card-"]',
  );

  if (cards.length === 0) {
    const noData = document.getElementById("no-data");
    if (noData) {
      noData.style.display = "block";
      noData.innerHTML =
        CURRENT_VIEW === "sos"
          ? '<i class="fa-solid fa-circle-check text-success fa-3x mb-3"></i><p>Hiện không có yêu cầu cứu trợ nào đang chờ!</p>'
          : '<i class="fa-solid fa-circle-check text-success fa-3x mb-3"></i><p>Hiện không có sự cố nào đang chờ!</p>';
    }
  }
}
// STOMP realtime subscription
// Lưu trữ thông tin đếm ngược cho mỗi SOS
var demNguocSOS = {};

function connectRealtime() {
  if (!TRUSO_ID || TRUSO_ID === 0) return;
  const socket = new SockJS("/ws-suco-web");
  const stompClient = Stomp.over(socket);
  stompClient.connect(
    {},
    function (frame) {
      stompClient.subscribe("/topic/truso/" + TRUSO_ID, function (message) {
        console.log("RAW MESSAGE =", message.body);

        try {
          const sos = JSON.parse(message.body);
          console.log("PARSED SOS =", sos);
          handleRealtimeSOS(sos);
        } catch (e) {
          console.error("Invalid SOS message", e, message.body);
        }
      });
      // Sự cố channel (matches trang-chu subscription)
      stompClient.subscribe(
        "/topic/tru-so/" + TRUSO_ID + "/su-co",
        function (message) {
          try {
            const suco = JSON.parse(message.body);
            handleRealtimeSuCo(suco);
          } catch (e) {
            console.error("Invalid su-co message", e);
          }
        },
      );
    },
    function (err) {
      console.error("STOMP error", err);
    },
  );
}

// ========== TỪ CHỐI TIẾP NHẬN ==========
function tuChoiTiepNhan(idSos, tuDong = false) {
  if (
    !tuDong &&
    !confirm("Bạn chắc chắn không muốn tiếp nhận tín hiệu SOS này?")
  ) {
    return;
  }

  const btnDecline = document.getElementById("btn-decline-" + idSos);
  const btnAccept = document.getElementById("btn-accept-" + idSos);

  if (btnDecline) {
    btnDecline.disabled = true;
    btnDecline.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i>';
  }

  if (btnAccept) {
    btnAccept.disabled = true;
  }

  fetch(`/sos/cap-nhat-trang-thai/${idSos}`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      status: "TU_CHOI",
    }),
  })
    .then((res) => {
      if (!res.ok) throw new Error("Lỗi server");
      return res.json();
    })
    .then((data) => {
      console.log("[TỪ CHỐI] Kết quả:", data);

      const card = document.getElementById("sos-card-" + idSos);

      if (card) {
        card.style.transition = "opacity 0.3s, transform 0.3s";
        card.style.opacity = "0";
        card.style.transform = "translateX(100%)";

        setTimeout(() => card.remove(), 300);
      }
    })
    .catch((err) => {
      console.error("[LỖI] Không thể từ chối:", err);

      alert("Có lỗi xảy ra. Vui lòng thử lại.");

      if (btnDecline) {
        btnDecline.disabled = false;
        btnDecline.innerHTML =
          '<i class="fa-solid fa-xmark"></i> Không tiếp nhận';
      }

      if (btnAccept) {
        btnAccept.disabled = false;
      }
    });
}

// Hiển thị thông báo khi có SOS mới
function hienThiThongBaoSOS(idSos, viTri, tongSo) {
  // Phát âm thanh cảnh báo
  try {
    const audio = new Audio("/assets/sounds/alert.mp3");
    audio.play().catch(() => {});
  } catch (e) {}

  // Có thể thêm toast notification ở đây
  console.log(`SOS mới! Bạn là trụ sở thứ ${viTri}/${tongSo} nhận tin này.`);
}

/* Detail panel for list page + overlay */
(function insertDetailPanel() {
  const main = document.querySelector(".main-content");
  if (!main) return;

  // overlay
  const overlay = document.createElement("div");
  overlay.id = "overlay";
  overlay.style.display = "none";
  document.body.appendChild(overlay);

  const panel = document.createElement("div");
  panel.id = "detail-panel";
  panel.style.overflow = "auto";
  panel.style.background = "#fff";
  panel.style.border = "1px solid #e5e7eb";
  panel.style.boxShadow = "0 10px 25px rgba(0,0,0,0.08)";
  panel.style.borderRadius = "8px";
  panel.style.display = "none";
  panel.innerHTML = `
                <div style="padding:12px 16px; display:flex; justify-content:space-between; align-items:center; border-bottom:1px solid #eee;">
                  <strong>Chi tiết</strong>
                  <button id="detail-close" style="border:none;background:none;cursor:pointer;font-size:16px">✖</button>
                </div>
                <div id="detail-body" style="padding:12px"></div>
              `;
  document.body.appendChild(panel);

  const btn = document.getElementById("detail-close");
  if (btn) {
    btn.addEventListener("click", () => {
      panel.style.display = "none";
      overlay.style.display = "none";
    });
  }

  overlay.addEventListener("click", function () {
    panel.style.display = "none";
    overlay.style.display = "none";
  });
})();

function openSOSDetail(id) {
  const el = document.getElementById("sos-card-" + id);
  if (!el) return;
  const raw = el.getAttribute("data-json");
  let obj = null;
  try {
    obj = JSON.parse(decodeURIComponent(raw));
  } catch (e) {
    console.error("Lỗi giải mã JSON:", e);
    return;
  }

  const panel = document.getElementById("detail-panel");
  const body = document.getElementById("detail-body");
  if (!panel || !body) return;

  if (!obj) {
    body.innerHTML = '<div class="text-muted">Không có dữ liệu chi tiết</div>';
    return;
  }

  // --- HÀM FIXED URL ĐỒNG BỘ TỪ TRANG CHỦ ---
  const fixUrlLocal = (path) => {
    if (!path) return null;
    if (path.startsWith("/uploads") || path.startsWith("http")) {
      return path;
    }
    return `/uploads/sos/${path}`;
  };

  // --- LOGIC MAPPING THÔNG MINH (Chống lỗi lệch tên trường giữa các trang) ---
  const user = obj.user || obj.nguoiGui || {};
  const userName = user.name || "Khách vãng lai";
  const userEmail = user.email || "Không có email";
  const isPriority = user.vip === true || obj.isVip === true;

  // Hỗ trợ cả trường hinhAnh (Trang quản lý) và hinhAnhUrl (Trang chủ)
  const rawImg = obj.hinhAnhUrl || obj.hinhAnh;
  const rawAudio = obj.ghiAmUrl || obj.ghiAm;
  const imgUrl = fixUrlLocal(rawImg);
  const audioUrl = fixUrlLocal(rawAudio);

  const ghiChu = obj.ghiChu || "Không có ghi chú hiện trường";
  const trangThai = obj.trangThai || obj.status || "DANG_DI_CHUYEN";
  const displayAddress = obj.diaChi || `${obj.viDo}, ${obj.kinhDo}`;
  const thoiGian =
    obj.createdAt || obj.thoiGianTao
      ? new Date(obj.createdAt || obj.thoiGianTao).toLocaleString("vi-VN")
      : "Vừa xong";

  const statusLabel = {
    DANG_DI_CHUYEN: "Đang di chuyển",
    DANG_XU_LY: "Đang xử lý",
    HOAN_THANH: "Hoàn thành",
    HUY_BO: "Hủy bỏ",
  };

  // --- ĐỔ DỮ LIỆU CHUẨN VÀO HTML PANEL CHI TIẾT ---
  body.innerHTML = `
    ${isPriority ? `<div style="background: linear-gradient(90deg, #FFD700, #FFA500); color: #000; padding: 6px; border-radius: 5px; font-weight: bold; margin-bottom: 15px; text-align: center; font-size: 0.8rem;"><i class="fa-solid fa-gem"></i> NGƯỜI DÙNG ƯU TIÊN (KIM CƯƠNG)</div>` : ""}

    <div class="info-group" style="margin-bottom: 12px;">
        <div class="info-label" style="font-size: 0.75rem; color: #64748b; font-weight: bold;">NGƯỜI GỬI TÍN HIỆU</div>
        <div class="info-value" style="font-weight: bold; font-size: 1.1rem; color: #1e293b;">${userName}</div>
        <div class="info-value" style="font-size: 0.85rem; color: #64748b;">${userEmail}</div>
    </div>

    <div class="info-group" style="margin-bottom: 12px;">
        <div class="info-label" style="font-size: 0.75rem; color: #64748b; font-weight: bold;">TRẠNG THÁI & THỜI GIAN</div>
        <div class="info-value">
            <span class="badge bg-danger text-white" style="padding: 3px 8px; border-radius: 4px; font-size: 0.8rem;">${statusLabel[trangThai] || trangThai}</span>
            <span style="margin-left: 10px; font-size: 0.85rem; color: #475569;"><i class="fa-regular fa-clock"></i> ${thoiGian}</span>
        </div>
    </div>

    <div class="info-group" style="margin-bottom: 12px;">
        <div class="info-label" style="font-size: 0.75rem; color: #64748b; font-weight: bold;">ĐỊA CHỈ HIỆN TRƯỜNG</div>
        <div class="info-value" style="font-size: 0.95rem; color: #334155;"><i class="fa-solid fa-location-dot text-danger"></i> ${displayAddress}</div>
    </div>

    <div class="info-group" style="margin-bottom: 12px;">
        <div class="info-label" style="font-size: 0.75rem; color: #64748b; font-weight: bold;">GHI CHÚ HIỆN TRƯỜNG</div>
        <div class="info-value" style="background: #f8fafc; border: 1px solid #e2e8f0; padding: 10px; border-radius: 6px; font-style: italic; color: #334155;">"${ghiChu}"</div>
    </div>

    <div class="info-group" style="margin-bottom: 12px;">
        <div class="info-label" style="font-size: 0.75rem; color: #64748b; font-weight: bold;">HÌNH ẢNH HIỆN TRƯỜNG</div>
        ${
          imgUrl
            ? `<img src="${imgUrl}" style="width:100%; border-radius:8px; margin-top:5px; cursor:pointer;" onerror="this.src='https://placehold.co/400x300?text=Không+tìm+thấy+ảnh'" onclick="window.open(this.src)">`
            : '<div class="text-muted small italic">Không có ảnh hiện trường</div>'
        }
    </div>

    <div class="info-group" style="margin-bottom: 15px;">
        <div class="info-label" style="font-size: 0.75rem; color: #64748b; font-weight: bold;">GHI ÂM KHẨN CẤP</div>
        ${
          audioUrl
            ? `<audio controls style="width: 100%; margin-top:5px;"><source src="${audioUrl}" type="audio/mpeg">Trình duyệt không hỗ trợ nghe audio.</audio>`
            : '<div class="text-muted small italic">Không có ghi âm từ hiện trường</div>'
        }
    </div>

    ${
      trangThai === "DANG_DI_CHUYEN"
        ? `
      <div style="margin-top: 20px; border-top: 1px solid #eee; padding-top: 15px;">
          <button class="btn btn-primary w-100" onclick="confirmRescue(${obj.id})">
              <i class="fa-solid fa-truck-fast"></i> TIẾP NHẬN CỨU TRỢ
          </button>
      </div>
    `
        : ""
    }
  `;

  // Hiển thị Panel và Overlay nền mờ
  panel.style.display = "block";
  const overlayElem = document.getElementById("overlay");
  if (overlayElem) overlayElem.style.display = "block";
}
function openSuCoDetail(id) {
  const el = document.getElementById("suco-card-" + id);
  if (!el) return;
  const raw = el.getAttribute("data-json");
  let obj = null;
  try {
    obj = JSON.parse(decodeURIComponent(raw));
  } catch (e) {
    console.error("Lỗi parse JSON sự cố:", e);
    return;
  }

  const panel = document.getElementById("detail-panel");
  const body = document.getElementById("detail-body");
  if (!panel || !body) return;

  if (!obj) {
    body.innerHTML =
      '<div class="text-muted">Không tìm thấy dữ liệu chi tiết sự cố</div>';
    return;
  }

  const tenLoai = obj.tenLoaiSuCo || obj.tenLoai || "Sự cố";
  const moTa = obj.moTa || "Không có mô tả chi tiết";
  const icon = obj.iconUrl || "https://placehold.co/24x24?text=⚠";
  const mucDo = obj.mucDoNghiemTrong || obj.mucDo || "LOW";
  const trangThai = obj.trangThaiXuLy || obj.trangThai || "DANG_DI_CHUYEN";
  const diaChi = obj.diaChi || `${obj.viDo}, ${obj.kinhDo}`;
  const tenNguoiBao = obj.tenNguoiBao || obj.reporterName || "Khách vãng lai";

  // Số lượt báo cáo trùng lặp thực tế
  const doTinCay = obj.doTinCay !== undefined ? obj.doTinCay : 0;

  const thoiGian =
    obj.createdAt || obj.thoiGianTao || obj.thoiGian
      ? new Date(
          obj.createdAt || obj.thoiGianTao || obj.thoiGian,
        ).toLocaleString("vi-VN")
      : "Vừa xong";

  const fixUrlLocal = (path) => {
    if (!path) return null;
    if (path.startsWith("/uploads") || path.startsWith("http")) return path;
    return `/uploads/suco/${path}`;
  };
  const imgUrl = fixUrlLocal(obj.hinhAnhUrl || obj.hinhAnh);

  const badgeColor =
    mucDo === "HIGH" ? "#ef4444" : mucDo === "MEDIUM" ? "#f59e0b" : "#10b981";

  const statusLabels = {
    DANG_DI_CHUYEN: "Đang di chuyển",
    DANG_XU_LY: "Đang xử lý",
    HOAN_THANH: "Hoàn thành",
    HUY_BO: "Hủy bỏ",
  };

  let actionButtonHtml = "";
  if (trangThai === "DANG_DI_CHUYEN") {
    actionButtonHtml = `
        <button class="btn btn-warning w-100 p-2 fw-bold" onclick="doiTrangThaiSuCo(${obj.id}, 'DANG_XU_LY')">
            <i class="fa-solid fa-truck-fire"></i> XUẤT PHÁT NGAY
        </button>`;
  } else if (trangThai === "DANG_XU_LY") {
    actionButtonHtml = `
        <div style="border: 1px dashed #f59e0b; padding: 12px; border-radius: 8px; background: #fffdf5;">
            <div style="font-weight:bold; margin-bottom:6px; color:#b45309; font-size:0.8rem;">CẬP NHẬT MỨC ĐỘ NGUY HIỂM</div>
            <select id="select-muc-do-${obj.id}" class="form-select form-select-sm mb-2">
                <option value="LOW" ${mucDo === "LOW" ? "selected" : ""}>THẤP (LOW)</option>
                <option value="MEDIUM" ${mucDo === "MEDIUM" ? "selected" : ""}>TRUNG BÌNH (MEDIUM)</option>
                <option value="HIGH" ${mucDo === "HIGH" ? "selected" : ""}>CAO (HIGH)</option>
            </select>
            <button class="btn btn-sm btn-outline-warning w-100 mb-1" style="font-size: 0.8rem;" onclick="updateMucDo(${obj.id})">
                <i class="fa-solid fa-pen-to-square"></i> Cập nhật mức độ
            </button>
            <button class="btn btn-sm btn-success w-100 fw-bold" onclick="doiTrangThaiSuCo(${obj.id}, 'HOAN_THANH')">
                <i class="fa-solid fa-check-double"></i> XÁC NHẬN HOÀN THÀNH
            </button>
        </div>`;
  } else {
    actionButtonHtml = `
        <div class="alert alert-success text-center p-2 fw-bold mb-0" style="font-size:0.85rem;">
            <i class="fa-solid fa-circle-check"></i> SỰ CỐ ĐÃ XỬ LÝ HOÀN THÀNH
        </div>`;
  }

  body.innerHTML = `
      <div class="info-group mb-2">
          <div style="font-size: 0.75rem; color: #64748b; font-weight: bold;">NGƯỜI BÁO SỰ CỐ TÀI CHỖ</div>
          <div style="font-weight: bold; font-size: 1.05rem; color: #1e293b;">${tenNguoiBao}</div>
      </div>

      <div class="info-group mb-2">
          <div style="font-size: 0.75rem; color: #64748b; font-weight: bold;">MỨC ĐỘ TIN CẬY THỰC TẾ</div>
          <div style="font-size: 0.95rem; font-weight: bold; color: #2563eb; margin-top: 3px;">
              <i class="fa-solid fa-users text-primary"></i> Đã nhận được <span style="font-size:1.1rem; color:#1d4ed8;">${doTinCay}</span> lượt báo cáo trùng lặp
          </div>
      </div>

      <div class="info-group mb-2">
          <div style="font-size: 0.75rem; color: #64748b; font-weight: bold;">LOẠI SỰ CỐ</div>
          <div style="font-weight: bold; display: flex; align-items: center; gap: 8px; color: #0f172a; margin-top:3px;">
              <img src="${icon}" width="20" height="20" onerror="this.src='https://placehold.co/24x24?text=⚠'">
              <span>${tenLoai}</span>
          </div>
      </div>

      <div class="info-group mb-2">
          <div style="font-size: 0.75rem; color: #64748b; font-weight: bold;">MỨC ĐỘ & TRẠNG THÁI</div>
          <div style="margin-top: 3px;">
              <span style="background: ${badgeColor}; color: white; padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: bold;">${mucDo}</span>
              <span style="background: #e2e8f0; color: #475569; padding: 2px 8px; border-radius: 4px; font-size: 11px; margin-left: 5px; font-weight: bold;">${statusLabels[trangThai] || trangThai}</span>
          </div>
          <div style="font-size: 0.8rem; color: #64748b; margin-top: 4px;">
              <i class="fa-regular fa-clock"></i> Thời gian báo cáo: ${thoiGian}
          </div>
      </div>

      <div class="info-group mb-2">
          <div style="font-size: 0.75rem; color: #64748b; font-weight: bold;">ĐỊA CHỈ HIỆN TRƯỜNG</div>
          <div style="font-size: 0.9rem; color: #334155; margin-top: 2px;"><i class="fa-solid fa-location-dot text-danger"></i> ${diaChi}</div>
      </div>

      <div class="info-group mb-2">
          <div style="font-size: 0.75rem; color: #64748b; font-weight: bold;">MÔ TẢ CHI TIẾT</div>
          <div style="background: #fffbeb; padding: 10px; border-radius: 6px; border-left: 4px solid #f59e0b; font-style: italic; color: #78350f; margin-top: 2px; font-size: 0.9rem;">
              "${moTa}"
          </div>
      </div>

      <div class="info-group mb-3">
          <div style="font-size: 0.75rem; color: #64748b; font-weight: bold;">HÌNH ẢNH HIỆN TRƯỜNG</div>
          ${
            imgUrl
              ? `<img src="${imgUrl}" style="width:100%; border-radius:8px; margin-top:5px; cursor:pointer;" onerror="this.src='https://placehold.co/400x300?text=Không+tìm+thấy+hình+ảnh'" onclick="window.open(this.src)">`
              : '<div style="color: #94a3b8; font-style: italic; font-size:0.85rem; margin-top:3px;">Không có ảnh đính kèm</div>'
          }
      </div>

      <div style="margin-top: 15px; border-top: 1px solid #e2e8f0; padding-top: 12px;">
          ${actionButtonHtml}
      </div>
  `;

  panel.style.display = "block";
  const overlayElem = document.getElementById("overlay");
  if (overlayElem) overlayElem.style.display = "block";
}
function doiTrangThaiSuCo(id, status) {
  // 1. Trích xuất tọa độ từ card sự cố tương tự SOS
  const card = document.getElementById(`suco-card-${id}`);
  let lat = "",
    lng = "";
  if (card) {
    lat = card.getAttribute("data-lat");
    lng = card.getAttribute("data-lng");
  }

  if (status === "DANG_XU_LY") {
    if (!confirm("Xác nhận xử lý sự cố này?")) return;
  } else {
    if (!confirm("Xác nhận sự cố này ĐÃ HOÀN THÀNH xử lý thực tế?")) return;
  }

  const btn = event?.target;
  if (btn && btn.tagName === "BUTTON") {
    btn.disabled = true;
    btn.innerHTML =
      '<span class="spinner-border spinner-border-sm"></span> Đang xử lý...';
  }

  fetch(`/su-co/cap-nhat-trang-thai/${id}`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ status: status }),
  })
    .then((res) => {
      if (!res.ok) throw new Error("Lỗi Server: " + res.status);
      return res.json().catch(() => ({}));
    })
    .then(() => {
      alert(
        status === "DANG_XU_LY"
          ? "Xuất phát thành công! Hệ thống đang thiết lập lộ trình."
          : "Đã cập nhật trạng thái hoàn thành!",
      );

      if (status === "DANG_XU_LY") {
        // TÌM KIẾM BẢN ĐỒ NGAY TẠI TRANG HIỆN TẠI
        if (typeof map !== "undefined" && map && lat && lng) {
          const kDo = parseFloat(lng);
          const vDo = parseFloat(lat);

          map.flyTo({
            center: [kDo, vDo],
            zoom: 17,
            speed: 1.2,
          });

          if (typeof drawRoute === "function") {
            drawRoute(kDo, vDo);
          }
          if (typeof closeDetail === "function") closeDetail();
        } else {
          // CHUYỂN HƯỚNG RA MAP TRANG CHỦ (Sửa từ /truso/dang-cuu-tro thành /truso/trang-chu)
          // Truyền đúng tham số suCoId để hàm handleRedirectParams() ở trang chủ bắt được và hiển thị panel
          window.location.href = `/truso/trang-chu?toLat=${lat}&toLng=${lng}&suCoId=${id}`;
        }
      } else {
        window.location.reload();
      }
    })
    .catch((err) => {
      console.error("Lỗi cập nhật trạng thái sự cố:", err);
      alert("Không thể cập nhật. Vui lòng kiểm tra lại.");
      if (btn && btn.tagName === "BUTTON") {
        btn.disabled = false;
        btn.innerHTML =
          status === "DANG_XU_LY"
            ? '<i class="fa-solid fa-truck-fire"></i> XUẤT PHÁT'
            : "Xác nhận hoàn thành";
      }
    });
}
function confirmRescue(id) {
  // 1. Lấy tọa độ từ card SOS để truyền sang bản đồ trang chủ
  const card = document.getElementById("sos-card-" + id);
  let lat = "",
    lng = "";
  if (card) {
    lat = card.getAttribute("data-lat");
    lng = card.getAttribute("data-lng");
  }

  if (!confirm("Xác nhận xử lý SOS này?")) return;

  const btn = document.getElementById("btn-accept-" + id);
  if (btn) {
    btn.disabled = true;
    btn.innerHTML =
      '<i class="fa-solid fa-spinner fa-spin"></i> Đang điều phối xe...';
  }

  fetch(`/sos/cap-nhat-trang-thai/${id}`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      status: "DANG_XU_LY",
    }),
  })
    .then((res) => {
      if (!res.ok) throw new Error("Lỗi server");
      return res.json().catch(() => ({}));
    })
    .then((data) => {
      console.log("ACCEPT RESULT =", data);
      alert(
        "Tiếp nhận SOS thành công! Hệ thống đang thiết lập lộ trình di chuyển.",
      );

      // 2. CHUYỂN HƯỚNG VÀ TRUYỀN THAM SỐ CHUẨN ĐỂ TRANG CHỦ TỰ VẼ ĐƯỜNG
      // Đổi sang '/truso/trang-chu' hoặc '/truso/dang-cuu-tro' tùy thuộc vào trang nào chứa Mapbox của bạn
      const targetUrl = "/truso/trang-chu";
      window.location.href = `${targetUrl}?toLat=${lat}&toLng=${lng}&sosId=${id}`;
    })
    .catch((err) => {
      console.error(err);
      alert("Có lỗi kết nối hệ thống.");
      if (btn) {
        btn.disabled = false;
        btn.innerHTML = '<i class="fa-solid fa-check"></i> Tiếp nhận';
      }
    });
}

document.addEventListener("DOMContentLoaded", function () {
  // 1. Kiểm tra ID trụ sở ngay lập tức (Lấy từ window đã hứng ở HTML)
  if (typeof window.TRUSO_ID !== "undefined") {
    TRUSO_ID = window.TRUSO_ID;
  }

  if (!TRUSO_ID || TRUSO_ID === 0) {
    console.error("❌ LỖI: Không tìm thấy ID trụ sở từ Session!");
    return;
  }

  // 2. Gán sự kiện click cho các nút chuyển view (Tab) công cụ điều khiển
  const btnSuCo = document.getElementById("btn-su-co");
  const btnSos = document.getElementById("btn-sos");
  if (btnSuCo) btnSuCo.addEventListener("click", () => setActiveView("suco"));
  if (btnSos) btnSos.addEventListener("click", () => setActiveView("sos"));

  // 3. Bây giờ DOM đã sẵn sàng 100%, gọi an toàn không lo bị null nữa
  setActiveView("sos");

  // 4. Kết nối WebSocket realtime
  setTimeout(() => {
    connectRealtime();
  }, 300);
});
