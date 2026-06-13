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

// --- HÀM FORMAT ITEM (BỊ THIẾU TRONG CODE CŨ CỦA BẠN) ---
function formatItem(s, type) {
  const upperType = String(type).toUpperCase().trim();
  const timeRaw = s.thoiGianTao || s.createdAt || s.thoiGian;
  let statusRaw =
    upperType === "SOS"
      ? s.trangThai || s.trangThaiXuLy
      : s.trangThaiXuLy || s.trangThai;

  let statusClean = String(statusRaw || "")
    .toUpperCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/Đ/g, "D")
    .trim()
    .replace(/\s+/g, "_");

  // Quy chuẩn các trạng thái đang xử lý của SOS về DANG_XU_LY để bộ lọc render nhận diện được
  if (
    upperType === "SOS" &&
    (statusClean === "DA_TIEP_NHAN" ||
      statusClean === "DANG_CUU_TRO" ||
      statusClean === "CHO_XU_LY")
  ) {
    statusClean = "DANG_XU_LY";
  }

  let isVipUser = false;
  if (upperType === "SOS") {
    if (s.nguoiGui) isVipUser = s.nguoiGui.vip || s.nguoiGui.totalPoints >= 100;
    else if (s.user) isVipUser = s.user.vip;
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
    hoaDon: upperType === "SOS" ? s.hoaDon || null : null,
    thanhToan: upperType === "SOS" ? s.thanhToan || null : null,
    reporterUid:
      upperType === "SOS"
        ? s.reporterUid || (s.nguoiGui ? s.nguoiGui.uid : null)
        : s.userUid || null,
    userPoints:
      upperType === "SOS" ? (s.nguoiGui ? s.nguoiGui.totalPoints : 0) : 0,
    _raw: s,
  };
}

// 1. Sửa hàm render để hiển thị giao diện động chuẩn xác
function renderRescueItem(sos) {
  if (!sos.id) {
    console.error("renderRescueItem: Missing ID!", sos);
  }

  const displayLocation =
    sos._raw?.diaChi || `${sos.viDo || ""}, ${sos.kinhDo || ""}`;
  const points = sos.userPoints || 0;
  const userUid = sos.reporterUid;

  // --- LOGIC NÚT GỌI ĐIỆN ---
  let callBtn = "";
  if (userUid) {
    callBtn = `
          <button class="btn btn-sm btn-primary mb-2 w-100" onclick="startCallWithUser('${userUid}')">
              <i class="fa-solid fa-phone"></i> Gọi cho khách
          </button>`;
  } else {
    callBtn = `
          <button class="btn btn-sm btn-outline-secondary mb-2 w-100" disabled>
              <i class="fa-solid fa-user-slash"></i> Ẩn danh
          </button>`;
  }

  // --- LOGIC NÚT HÀNH ĐỘNG (XỬ LÝ / TẠO HÓA ĐƠN) ---
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
      if (sos.hoaDon.trangThai === "PAID") {
        actionBtn = `
              <button class="btn btn-sm btn-primary w-100" onclick="completeRescue(${sos.id || 0}, '${sos.itemType}')">
                  <i class="fa-solid fa-check-double"></i> Hoàn thành
              </button>`;
      } else {
        actionBtn = `
              <button class="btn btn-sm btn-outline-secondary w-100" disabled>
                  <i class="fa-solid fa-hourglass-half fa-spin"></i> Đang đợi trả tiền
              </button>`;
      }
    } else {
      actionBtn = `
            <button class="btn btn-sm btn-success w-100" onclick="openPayment(${sos.id || 0})">
                <i class="fa-solid fa-file-invoice"></i> Tạo hóa đơn
            </button>`;
    }
  }

  const totalActions = callBtn + actionBtn;

  return `
      <div class="card-sos" id="rescue-${sos.id}" data-item-type="${sos.itemType}">
          <span class="badge bg-warning text-dark me-2">
              <i class="fa-solid fa-star"></i> ${points}
              ${sos.isVip ? '<i class="fa-solid fa-crown text-danger ms-1"></i>' : ""}
          </span>
          <div class="flex-grow-1 d-flex flex-column" style="overflow: hidden;">
              <div class="text-truncate"><strong>Vị trí:</strong> ${displayLocation}</div>
              <div class="text-truncate text-muted small">
                  <strong>Ghi chú:</strong> ${sos.ghiChu}
                  <i class="fa-solid fa-circle-info ms-1" style="cursor: pointer; color: #0d6efd;" onclick="openDetailFromRow(${sos.id})"></i>
              </div>
          </div>
          <div class="ms-auto d-flex flex-column align-items-end" style="min-width: 140px;">
              ${totalActions}
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
        ? `/api/tin-hieu-sos/cap-nhat-trang-thai/${id}`
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

  if (data.isVip) {
    statusArea.innerHTML = `<span class="vip-badge"><i class="fa-solid fa-crown"></i> Khách hàng VIP (Hệ thống tự tính tiền)</span>`;
    amountInput.value = 0;
    amountInput.readOnly = true;
    vipNote.innerText = "Giá 0đ nếu < hạn mức km. Backend sẽ tự xử lý.";
  } else {
    statusArea.innerHTML = `<span class="normal-badge"><i class="fa-solid fa-user"></i> Khách hàng vãng lai</span>`;
    amountInput.value = "";
    amountInput.readOnly = false;
    vipNote.innerText = "Vui lòng nhập giá thỏa thuận với khách.";
  }

  document.getElementById("overlay").style.display = "block";
  document.getElementById("payment-panel").style.display = "block";
}

function closePayment() {
  document.getElementById("payment-panel").style.display = "none";
  document.getElementById("overlay").style.display = "none";
}

async function submitPayment(e) {
  if (e) e.preventDefault();

  const payload = {
    sosId: currentSosId,
    noiDungXuLy: document.getElementById("pay-treatment").value,
    giaThuCong: document.getElementById("pay-amount").value,
    trusoId: TRUSO_ID,
  };
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
        allData[idx].hoaDon = hoaDonMoi;
        renderData();
      }
      loadActiveRescues();
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

      if (!Array.isArray(data)) {
        console.error(
          "❌ LỖI: Dữ liệu API trả về không phải là một Mảng (Array)!",
          data,
        );
        return;
      }

      const formattedSosList = data.map((item) => {
        return formatItem(item, "SOS");
      });

      console.log(
        "✨ Danh sách SOS sau khi chạy qua formatItem:",
        formattedSosList,
      );

      const truocKhiGop = allData.length;
      allData = allData
        .filter((it) => it.itemType !== "SOS")
        .concat(formattedSosList);
      console.log(
        `🔄 Đồng bộ allData: Trước gộp (${truocKhiGop} items) -> Sau khi gộp (${allData.length} items)`,
      );

      renderData();
    })
    .catch((err) => {
      console.error(
        "❌ LỖI hệ thống khi fetch /truso/api/sos/dang-xu-ly:",
        err,
      );
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
    const hopLe = st === "DANG_XU_LY" || st.includes("XU_LY") || st === "";

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
    body.innerHTML = '<div class="text-muted">Không có dữ liệu</div>';
  } else {
    const realData = obj._raw || {};
    const imgPath = fixUrl(realData.hinhAnh || realData.hinhAnhUrl);
    const audioPath = fixUrl(realData.ghiAm);
    const fullAddress = realData.diaChi || `${obj.viDo}, ${obj.kinhDo}`;

    const imgHtml = imgPath
      ? `<img src="${imgPath}" class="img-fluid rounded mb-3" style="width:100%; cursor:zoom-in; border:1px solid #ddd;" onclick="window.open(this.src)">`
      : '<p class="text-muted small">Không có hình ảnh</p>';

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
        <div class="mb-3 small"><strong>Nội dung:</strong> ${obj.ghiChu}</div>
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
          if (data.trangThai === "PAID") {
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
  const idx = allData.findIndex((it) => it.id === paymentInfo.sosId);
  if (idx !== -1) {
    if (!allData[idx].hoaDon) allData[idx].hoaDon = {};
    allData[idx].hoaDon.trangThai = "PAID";
    allData[idx].hoaDon.tongThanhToan = paymentInfo.tongThanhToan;
    renderData();
  } else {
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
  showCallPanel("Đang gọi Android...");
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
    console.error("Lỗi khởi tạo gọi:", e);
    alert("Không thể truy cập Microphone hoặc trình duyệt không hỗ trợ!");
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
  showCallPanel("Cuộc gọi đến...");
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
        .catch((err) => console.error("Trình duyệt chặn phát âm thanh:", err));
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
      console.error("Không thể truy cập Mic:", e);
      throw e;
    }
  }
}

function showCallPanel(status) {
  document.getElementById("call-panel").style.display = "block";
  document.getElementById("call-status").innerText = status;
}

// Bổ sung từ khóa đóng/ngắt cuộc gọi
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
