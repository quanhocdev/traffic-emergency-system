function fixUrl(path) {
  if (!path) return null;
  if (path.startsWith("http") || path.startsWith("/uploads/")) {
    return path;
  }
  return `/uploads/sos/${path}`;
}

var TRUSO_ID = window.TRUSO_ID || 0;

let currentFilter = "sos"; // Mặc định chuyển thành "sos" để khớp với logic tải trang đầu tiên
let allData = [];

function formatTime(iso) {
  try {
    const d = new Date(iso);
    return d.toLocaleString();
  } catch (e) {
    return iso;
  }
}

function formatItem(s, type) {
  const upperType = String(type).toUpperCase().trim();
  const timeRaw = s.thoiGianTao || s.createdAt || s.thoiGian;

  let statusRaw =
    upperType === "SOS"
      ? s.trangThai || s.trangThaiXuLy
      : s.trangThaiXuLy || s.trangThai;
  let statusClean = String(statusRaw || "")
    .toUpperCase()
    .trim();

  let isVipUser = false;
  let userName = "Chưa rõ tên";
  let userEmail = "Không có email";

  const userData = s.user || null;

  if (userData) {
    isVipUser = userData.vip === true; // Thuộc tính 'vip' kiểu boolean từ DTO mới
    userName = userData.name || "Ẩn danh";
    userEmail = userData.email || "Không có email";
  } else {
    // Dự phòng nếu dữ liệu cũ nằm phẳng ở ngoài
    isVipUser = s.isVip || s.vip || false;
    userName = s.userName || "Ẩn danh";
  }

  return {
    id: s.id,
    viDo: s.viDo,
    kinhDo: s.kinhDo,
    createdAt: timeRaw,
    ghiChu:
      upperType === "SOS"
        ? s.ghiChu || "SOS Khẩn cấp"
        : s.moTa || "Sự cố đường bộ",
    trangThaiXuLy: statusClean,
    itemType: upperType,
    isVip: isVipUser,
    userName: userName,
    userEmail: userEmail,
    hoaDon: upperType === "SOS" ? s.hoaDon || null : null,
    thanhToan: upperType === "SOS" ? s.thanhToan || null : null,
    // 🌟 LƯU Ý: Giữ lại trường s.userUid hoặc s.reporterUid gốc từ thực thể SOS (nếu có)
    // để làm kênh định danh cuộc gọi WebRTC, tách biệt hoàn toàn khỏi Hóa Đơn DTO
    reporterUid: s.reporterUid || s.userUid || s.userId || null,
    _raw: s,
  };
}

function renderRescueItem(sos) {
  if (!sos.id) console.error("renderRescueItem: Thiếu ID!", sos);

  const displayLocation =
    sos._raw?.diaChi || `${sos.viDo || ""}, ${sos.kinhDo || ""}`;
  const userUid = sos.reporterUid;

  // Nút gọi đàm thoại WebRTC qua Android
  let callBtn = userUid
    ? `<button class="btn btn-sm btn-primary mb-2 w-100" onclick="startCallWithUser('${userUid}')">
          <i class="fa-solid fa-phone"></i> Gọi đàm thoại
       </button>`
    : `<button class="btn btn-sm btn-outline-secondary mb-2 w-100" disabled>
          <i class="fa-solid fa-user-slash"></i> Không có UID gọi
       </button>`;

  let actionBtn = "";
  if (sos.itemType === "SUCO") {
    actionBtn = `
          <button class="btn btn-sm btn-outline-success mb-2 w-100" onclick="gotoMap(${sos.id || 0})">
              <i class="fa-solid fa-map-location-dot"></i> Dẫn đường
          </button>
          <button class="btn btn-sm btn-primary w-100" onclick="completeRescue(${sos.id || 0}, '${sos.itemType}')">
              <i class="fa-solid fa-check"></i> Hoàn thành
          </button>`;
  } else {
    if (sos.hoaDon) {
      if (
        sos.hoaDon.trangThai === "PAID" ||
        sos.hoaDon.trangThai === "SUCCESS"
      ) {
        actionBtn = `
              <button class="btn btn-sm btn-primary w-100" onclick="completeRescue(${sos.id || 0}, '${sos.itemType}')">
                  <i class="fa-solid fa-check-double"></i> Hoàn thành
              </button>`;
      } else {
        actionBtn = `
              <button class="btn btn-sm btn-outline-secondary w-100" disabled>
                  <i class="fa-solid fa-hourglass-half fa-spin"></i> Chờ khách trả tiền
              </button>`;
      }
    } else {
      actionBtn = `
            <button class="btn btn-sm btn-success w-100" onclick="openPayment(${sos.id || 0})">
                <i class="fa-solid fa-file-invoice"></i> Tạo hóa đơn
            </button>`;
    }
  }

  // Giao diện thẻ hiển thị danh sách cứu hộ
  return `
      <div class="card-sos" id="rescue-${sos.id}" data-item-type="${sos.itemType}">
          <div class="mb-1">
              ${sos.isVip ? '<span class="badge bg-danger"><i class="fa-solid fa-crown"></i> Hội viên VIP</span>' : '<span class="badge bg-secondary">Khách vãng lai</span>'}
          </div>
          <div class="flex-grow-1 d-flex flex-column" style="overflow: hidden;">
              <div class="text-truncate"><strong>Khách hàng:</strong> ${sos.userName}</div>
              <div class="text-truncate text-muted small">
                  <strong>Vị trí:</strong> ${displayLocation}
                  <i class="fa-solid fa-circle-info ms-1" style="cursor: pointer; color: #0d6efd;" onclick="openDetailFromRow(${sos.id})"></i>
              </div>
          </div>
          <div class="ms-auto d-flex flex-column align-items-end" style="min-width: 140px;">
              ${callBtn + actionBtn}
          </div>
      </div>`;
}

async function completeRescue(id, type) {
  if (!id || id === 0) {
    alert("Lỗi: Không tìm thấy ID của yêu cầu cứu trợ!");
    return;
  }

  if (!confirm("Xác nhận đã hoàn thành cứu trợ và đóng yêu cầu này?")) return;

  try {
    const url =
      type === "SOS"
        ? `/sos/cap-nhat-trang-thai/${id}`
        : `/su-co/cap-nhat-trang-thai/${id}`;

    const res = await fetch(url, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ status: "HOAN_THANH" }),
    });

    if (res.ok) {
      const idx = allData.findIndex(
        (it) => it.id === id && it.itemType === type,
      );
      if (idx !== -1) {
        allData[idx].trangThaiXuLy = "HOAN_THANH";
        renderData();
      }
      alert("Đã hoàn tất ca cứu trợ!");
    } else {
      const errorText = await res.text();
      alert(`Lỗi khi cập nhật: ${res.status} - ${errorText}`);
    }
  } catch (e) {
    console.error("Lỗi hoàn thành cứu trợ:", e);
    alert("Đã xảy ra lỗi: " + e.message);
  }
}

// 2. Các hàm xử lý Form Thanh toán mới
let currentSosId = null;

function openPayment(id) {
  const el = document.getElementById("rescue-" + id);
  if (!el) return;
  const itemType = el.getAttribute("data-item-type");

  const data = allData.find((it) => it.id === id && it.itemType === itemType);
  if (!data) return;

  currentSosId = id;
  const amountInput = document.getElementById("pay-amount");
  const statusArea = document.getElementById("status-area");
  const vipNote = document.getElementById("vip-note");

  // 🌟 HIỂN THỊ TÊN ĐỘNG: Hiển thị đích danh tên khách hàng lên Form hóa đơn
  if (data.isVip) {
    statusArea.innerHTML = `<span class="vip-badge" style="background-color: #FEF3C7; color: #92400E; padding: 6px 12px; border-radius: 6px; font-weight: bold;"><i class="fa-solid fa-crown"></i> Hội viên VIP: ${data.userName}</span>`;
    amountInput.value = 0;
    amountInput.readOnly = true;
    vipNote.innerText =
      "Giá 0đ do tài khoản thuộc diện miễn phí cứu hộ theo hạn mức.";
  } else {
    statusArea.innerHTML = `<span class="normal-badge" style="background-color: #E5E7EB; color: #374151; padding: 6px 12px; border-radius: 6px; font-weight: bold;"><i class="fa-solid fa-user"></i> Khách vãng lai: ${data.userName}</span>`;
    amountInput.value = "";
    amountInput.readOnly = false;
    vipNote.innerText =
      "Vui lòng nhập giá chi phí thỏa thuận trực tiếp với khách hàng.";
  }

  document.getElementById("overlay").style.display = "block";
  document.getElementById("payment-panel").style.display = "block";
}

function closePayment() {
  document.getElementById("payment-panel").style.display = "none";
  document.getElementById("overlay").style.display = "none";
}

async function submitPayment() {
  const amountInput = document.getElementById("pay-amount").value;

  const payload = {
    sosId: Number(currentSosId),
    noiDungXuLy: document.getElementById("pay-treatment").value,
    giaThuCong: amountInput ? parseFloat(amountInput) : 0,
  };

  console.log("Payload chuẩn bị gửi đi:", payload);

  try {
    const res = await fetch("/truso/hoa-don/tao", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    if (res.ok) {
      const hoaDonMoi = await res.json();
      alert("Đã tạo hóa đơn thành công!");
      closePayment();

      const idx = allData.findIndex(
        (it) => it.id === currentSosId && it.itemType === "SOS",
      );

      if (idx !== -1) {
        // 🌟 BẢO VỆ DỮ LIỆU: Giữ lại bản sao user đang chạy tốt ở Frontend
        const backupUser = allData[idx].userName;
        const backupEmail = allData[idx].userEmail;
        const backupPoints = allData[idx].userPoints;
        const backupIsVip = allData[idx].isVip;
        const backupUid = allData[idx].reporterUid;

        // Gán hóa đơn mới trả về vào phần tử dữ liệu chung
        allData[idx].hoaDon = hoaDonMoi;

        // 🌟 Kiểm tra: Nếu object hoaDonMoi từ backend bị thiếu hoặc trống thông tin user bọc bên ngoài
        if (hoaDonMoi.user) {
          allData[idx].isVip =
            hoaDonMoi.user.vip === true ||
            hoaDonMoi.user.isVip === true ||
            false;
          allData[idx].userPoints = hoaDonMoi.user.totalPoints || 0;
          allData[idx].userName =
            hoaDonMoi.user.name || hoaDonMoi.user.hoTen || "N/A";
          allData[idx].userEmail = hoaDonMoi.user.email || "N/A";
          allData[idx].reporterUid =
            hoaDonMoi.user.id || hoaDonMoi.user.uid || backupUid;
        } else {
          // Khôi phục lại bản sao cũ, không cho phép hiển thị đè thành vãng lai/ẩn danh
          allData[idx].userName = backupUser;
          allData[idx].userEmail = backupEmail;
          allData[idx].userPoints = backupPoints;
          allData[idx].isVip = backupIsVip;
          allData[idx].reporterUid = backupUid;
        }

        renderData();
      }

      // Tải lại danh sách để đồng bộ trạng thái mới nhất tự động từ DB
      loadActiveRescues();
    } else {
      const errorText = await res.text();
      alert(`Lỗi từ Server (${res.status}): ${errorText}`);
    }
  } catch (err) {
    console.error("Lỗi gửi hóa đơn:", err);
  }
}

function loadActiveRescues() {
  if (!TRUSO_ID || TRUSO_ID === 0) {
    console.error("❌ LỖI: TRUSO_ID chưa được khởi tạo hoặc bằng 0!", TRUSO_ID);
    return;
  }

  console.log("=== 🔥 BẮT ĐẦU FETCH DỮ LIỆU ĐANG XỬ LÝ ===");
  fetch("/truso/api/sos/dang-xu-ly")
    .then((res) => {
      console.log(`📡 Phản hồi từ Server: Status code = ${res.status}`);
      return res.json();
    })
    .then((data) => {
      console.log("📦 Dữ liệu GỐC (Raw JSON) nhận từ API:", data);

      if (Array.isArray(data)) {
        console.log("🔍 DANH SÁCH TRẠNG THÁI THỰC TẾ TỪ SERVER:");
        data.forEach((item) => {
          console.log(
            `--- SOS ID #${item.id}: trangThai gốc = "${item.trangThai}", trangThaiXuLy gốc = "${item.trangThaiXuLy}"`,
          );
        });
        console.table(
          data.map((item) => ({
            id: item.id,
            trangThai: item.trangThai,
            trangThaiXuLy: item.trangThaiXuLy,
          })),
        );
      }

      if (!Array.isArray(data)) {
        console.error(
          "❌ LỖI: Dữ liệu API trả về không phải là một Mảng (Array)!",
          data,
        );
        return;
      }

      allData = data.map((item) => {
        return formatItem(item, "SOS");
      });

      renderData(); // Đảm bảo gọi hàm renderData() để vẽ lại danh sách sau khi map xong
    })
    .catch((err) => {
      console.error("Lỗi khi tải dữ liệu cứu hộ:", err);
    });
}
function renderData() {
  console.log("=== 🛠️ BẮT ĐẦU CHẠY HÀM RENDERDATA ===");
  console.log("Current Filter (Tab đang chọn) =", currentFilter);

  const list = document.getElementById("rescue-list");
  if (!list) {
    console.error(
      "❌ LỖI: Không tìm thấy phần tử DOM nào có id là 'rescue-list' trên giao diện HTML!",
    );
    return;
  }

  list.innerHTML = "";

  let filtered = allData.filter((item) => {
    // 1. Kiểm tra bộ lọc Tab
    const map = { sos: "SOS", "su-co": "SUCO" };
    if (item.itemType !== map[currentFilter]) {
      return false;
    }

    // 2. Kiểm tra bộ lọc Trạng thái xử lý hợp lệ công việc hiện trường
    const st = String(item.trangThaiXuLy ?? "")
      .trim()
      .toUpperCase();
    const hopLe = st === "DANG_XU_LY";

    if (!hopLe) {
      console.warn(
        `⚠️ Item #${item.id} [${item.itemType}] bị loại do trạng thái xử lý không khớp: "${st}"`,
      );
    }

    return hopLe;
  });

  console.log("🎯 Danh sách sau khi BỊ LỌC =", filtered);

  if (filtered.length === 0) {
    list.innerHTML = `
      <div class="alert alert-secondary py-4 text-center">
        Hiện không có ca cứu trợ nào đang diễn ra trong danh mục này.
      </div>`;
    return;
  }

  // Sắp xếp ưu tiên VIP lên đầu, sau đó đến thời gian tạo mới nhất
  filtered.sort((a, b) => {
    if (a.isVip !== b.isVip) return a.isVip ? -1 : 1;
    return new Date(b.createdAt) - new Date(a.createdAt);
  });

  filtered.forEach((it) => {
    const wrapper = document.createElement("div");
    wrapper.innerHTML = renderRescueItem(it);
    if (wrapper.firstElementChild) {
      list.appendChild(wrapper.firstElementChild);
    }
  });
  console.log(`✅ Render thành công ${filtered.length} thẻ lên giao diện!`);
}

function filterType(type) {
  currentFilter = type;
  const bSuCo = document.getElementById("btn-filter-su-co");
  const bSOS = document.getElementById("btn-filter-sos");

  if (bSuCo) bSuCo.classList.remove("active");
  if (bSOS) bSOS.classList.remove("active");

  if (type === "su-co") {
    bSuCo?.classList.add("active");
  } else {
    bSOS?.classList.add("active");
  }
  renderData();
}

function openDetailFromRow(id) {
  const el = document.getElementById("rescue-" + id);
  if (!el) return;
  const itemType = el.getAttribute("data-item-type");

  const obj = allData.find((it) => it.id === id && it.itemType === itemType);
  const body = document.getElementById("detail-body");

  if (!obj) {
    body.innerHTML = '<div class="text-muted">Không có dữ liệu chi tiết</div>';
  } else {
    const realData = obj._raw || {};
    const imgPath = fixUrl(realData.hinhAnh || realData.hinhAnhUrl);
    const audioPath = fixUrl(realData.ghiAm);
    const fullAddress =
      realData.diaChi || `${obj.viDo || ""}, ${obj.kinhDo || ""}`;

    const imgHtml = imgPath
      ? `<img src="${imgPath}" class="img-fluid rounded mb-3" style="width:100%; cursor:zoom-in; border:1px solid #ddd;" onclick="window.open(this.src)">`
      : '<p class="text-muted small">Không có hình ảnh hiện trường</p>';

    const audioHtml = audioPath
      ? `<div class="mb-3">
            <label class="small fw-bold">Ghi âm hiện trường:</label>
            <audio controls style="width:100%"><source src="${audioPath}" type="audio/mp4"></audio>
           </div>`
      : "";

    body.innerHTML = `
        <div class="mb-3">
            <h5 class="text-primary mb-0">${obj.itemType === "SOS" ? "🆘 SOS Khẩn cấp" : "⚠️ Sự cố"} #${obj.id}</h5>
            <small class="text-muted">${formatTime(obj.createdAt)}</small>
        </div>
        <div class="mb-2 bg-light p-2 rounded">
            <i class="fa-solid fa-location-dot text-danger"></i> <strong>Địa chỉ:</strong><br>
            <span class="text-dark">${fullAddress}</span>
        </div>
        <div class="mb-3 small"><strong>Nội dung ghi chú:</strong> ${obj.ghiChu}</div>
        <hr>
        ${imgHtml}
        ${audioHtml}
        <div class="mt-3">
            <button class="btn btn-outline-success w-100 mb-2" onclick="gotoMap(${obj.id})">
                <i class="fa-solid fa-map-location-dot"></i> Dẫn đường trên bản đồ
            </button>
        </div>`;
  }
  document.getElementById("overlay").style.display = "block";
  document.getElementById("detail-panel").style.display = "block";
}

function gotoMap(id) {
  const el = document.getElementById("rescue-" + id);
  if (!el) return;
  const itemType = el.getAttribute("data-item-type");

  const obj = allData.find((it) => it.id === id && it.itemType === itemType);
  const lat = obj ? obj.viDo : null;
  const lng = obj ? obj.kinhDo : null;

  if (lat && lng) {
    window.location.href = `/truso/trang-chu?toLat=${encodeURIComponent(lat)}&toLng=${encodeURIComponent(lng)}&sosId=${id}`;
  } else {
    window.location.href = "/truso/trang-chu?sosId=" + id;
  }
}

if (document.getElementById) {
  document.addEventListener("click", function (e) {
    if (e.target && e.target.id === "detail-close") {
      document.getElementById("detail-panel").style.display = "none";
      document.getElementById("overlay").style.display = "none";
    }
  });
}

setInterval(() => {
  const c = document.getElementById("clock");
  if (c) c.innerText = new Date().toLocaleTimeString();
}, 1000);

// --- CẤU HÌNH SOCKET & WEBRTC ---
let stompClient = null;

function connectWebSocket() {
  const socket = new SockJS("/ws-suco-web");
  stompClient = Stomp.over(socket);
  stompClient.debug = null;

  stompClient.connect(
    {},
    function (frame) {
      console.log("Connected to WebSocket: " + frame);
      stompClient.subscribe(
        "/topic/truso/" + TRUSO_ID,
        function (messageOutput) {
          const data = JSON.parse(messageOutput.body);
          // Chấp nhận đồng bộ khi trạng thái là PAID hoặc đang xử lý hóa đơn
          if (
            data.trangThai === "PAID" ||
            data.trangThai === "SUCCESS" ||
            data.id
          ) {
            updateLocalDataAfterPaid(data);
          }
        },
      );
      setupCallSubscription();
    },
    function (error) {
      console.error("Socket Error, retrying in 5s...", error);
      setTimeout(connectWebSocket, 5000);
    },
  );
}

function updateLocalDataAfterPaid(paymentInfo) {
  // Xác định ID của ca cứu hộ đi kèm hóa đơn
  const targetSosId =
    paymentInfo.sosId || (paymentInfo._raw ? paymentInfo._raw.id : null);

  const idx = allData.findIndex(
    (it) =>
      (it.id === targetSosId || it.id === paymentInfo.id) &&
      it.itemType === "SOS",
  );

  if (idx !== -1) {
    // Giữ lại dữ liệu cũ để phòng hờ trường hợp dữ liệu socket bị khuyết
    const backupUser = allData[idx].userName;
    const backupEmail = allData[idx].userEmail;
    const backupIsVip = allData[idx].isVip;

    if (!allData[idx].hoaDon) allData[idx].hoaDon = {};

    allData[idx].hoaDon.id = paymentInfo.id;
    allData[idx].hoaDon.trangThai = paymentInfo.trangThai || "PAID";
    allData[idx].hoaDon.thanhTien =
      paymentInfo.thanhTien || paymentInfo.tongThanhToan;

    // 🌟 ĐỒNG BỘ: Cập nhật lại thông tin tài khoản dựa theo cấu trúc UserInfoResponseDTO mới nhận qua Socket
    if (paymentInfo.user) {
      allData[idx].isVip = paymentInfo.user.vip === true;
      allData[idx].userName = paymentInfo.user.name || "Ẩn danh";
      allData[idx].userEmail = paymentInfo.user.email || "Không có email";
    } else {
      allData[idx].userName = backupUser;
      allData[idx].userEmail = backupEmail;
      allData[idx].isVip = backupIsVip;
    }

    renderData(); // Vẽ lại màn hình ngay lập tức để cập nhật nút bấm/badge
  } else {
    // Nếu không tìm thấy trong mảng hiện tại, chủ động tải lại toàn bộ danh sách active cho chắc chắn
    loadActiveRescues();
  }
}

window.addEventListener("load", () => {
  const bSuCo = document.getElementById("btn-filter-su-co");
  const bSOS = document.getElementById("btn-filter-sos");
  if (bSuCo) bSuCo.addEventListener("click", () => filterType("su-co"));
  if (bSOS) bSOS.addEventListener("click", () => filterType("sos"));

  currentFilter = "sos";
  if (bSOS) bSOS.classList.add("active");
  if (bSuCo) bSuCo.classList.remove("active");

  loadActiveRescues();
  connectWebSocket();
});

let peerConnection = null;
let localStream = null;
let targetUserId = null;
const configuration = {
  iceServers: [{ urls: "stun:stun.l.google.com:19302" }],
};

async function startCallWithUser(userUid) {
  targetUserId = userUid;
  showCallPanel("Đang kết nối Android...");
  try {
    await initLocalStream();
    createPeerConnection();
    const offer = await peerConnection.createOffer();
    await peerConnection.setLocalDescription(offer);
    sendSignal({
      type: "OFFER",
      to: targetUserId,
      from: "TRU_SO_" + TRUSO_ID,
      sdp: offer.sdp,
    });
  } catch (e) {
    console.error("Lỗi khởi tạo gọi WebRTC:", e);
    alert("Không thể truy cập Microphone hoặc thiết bị phần cứng bị chặn!");
  }
}

function sendSignal(data) {
  if (stompClient && stompClient.connected) {
    stompClient.send("/app/call-signal", {}, JSON.stringify(data));
  }
}

function setupCallSubscription() {
  stompClient.subscribe(
    "/topic/tru-so/TRU_SO_" + TRUSO_ID + "/call",
    async (message) => {
      const signal = JSON.parse(message.body);
      const type = signal.type ? signal.type.toUpperCase() : "";

      if (type === "OFFER") {
        targetUserId = signal.from;
        handleOffer(signal);
      } else if (type === "ANSWER") {
        await peerConnection.setRemoteDescription(
          new RTCSessionDescription(signal),
        );
        document.getElementById("call-status").innerText = "Đang đàm thoại";
      } else if (type === "CANDIDATE") {
        if (peerConnection && signal.candidate) {
          try {
            await peerConnection.addIceCandidate(
              new RTCIceCandidate(signal.candidate),
            );
          } catch (e) {}
        }
      } else if (type === "BYE") {
        cleanupCallLocally();
      }
    },
  );
}

async function handleOffer(offer) {
  showCallPanel("Cuộc gọi đến từ ứng dụng...");
  await initLocalStream();
  createPeerConnection();
  await peerConnection.setRemoteDescription(new RTCSessionDescription(offer));
  const answer = await peerConnection.createAnswer();
  await peerConnection.setLocalDescription(answer);
  sendSignal({
    type: "ANSWER",
    to: targetUserId,
    from: "TRU_SO_" + TRUSO_ID,
    sdp: answer.sdp,
  });
  document.getElementById("call-status").innerText = "Đang đàm thoại";
}

function createPeerConnection() {
  if (peerConnection) peerConnection.close();
  peerConnection = new RTCPeerConnection(configuration);

  localStream
    .getTracks()
    .forEach((track) => peerConnection.addTrack(track, localStream));

  peerConnection.ontrack = (event) => {
    const remoteAudio = document.getElementById("remoteAudio");
    if (remoteAudio) {
      remoteAudio.srcObject = event.streams[0];
      remoteAudio.volume = 0.8;
      remoteAudio
        .play()
        .catch((err) =>
          console.error("Trình duyệt chặn phát âm thanh tự động:", err),
        );
    }
  };

  peerConnection.onicecandidate = (event) => {
    if (event.candidate) {
      sendSignal({
        type: "CANDIDATE",
        to: targetUserId,
        from: "TRU_SO_" + TRUSO_ID,
        candidate: {
          candidate: event.candidate.candidate,
          sdpMid: event.candidate.sdpMid,
          sdpMLineIndex: event.candidate.sdpMLineIndex,
        },
      });
    }
  };
}

async function initLocalStream() {
  if (!localStream) {
    try {
      localStream = await navigator.mediaDevices.getUserMedia({
        audio: {
          echoCancellation: true,
          noiseSuppression: true,
          autoGainControl: true,
        },
        video: false,
      });
    } catch (e) {
      console.error("Không thể lấy quyền Microphone:", e);
      throw e;
    }
  }
}

function showCallPanel(status) {
  document.getElementById("call-panel").style.display = "block";
  document.getElementById("call-status").innerText = status;
}

function endCall() {
  if (targetUserId) {
    sendSignal({ type: "BYE", to: targetUserId, from: "TRU_SO_" + TRUSO_ID });
  }
  cleanupCallLocally();
}

function cleanupCallLocally() {
  if (peerConnection) peerConnection.close();
  if (localStream) localStream.getTracks().forEach((t) => t.stop());
  localStream = null;
  peerConnection = null;
  document.getElementById("call-panel").style.display = "none";
  targetUserId = null;
}

function toggleMic() {
  if (localStream) {
    const audioTrack = localStream.getAudioTracks()[0];
    audioTrack.enabled = !audioTrack.enabled;
    const btn = document.getElementById("btn-mic");
    btn.classList.toggle("btn-danger");
    btn.innerHTML = audioTrack.enabled
      ? '<i class="fa-solid fa-microphone"></i>'
      : '<i class="fa-solid fa-microphone-slash"></i>';
  }
}
