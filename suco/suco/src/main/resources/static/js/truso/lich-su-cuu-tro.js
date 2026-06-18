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
      fetch("/sos/lich-su").then((r) => r.json()),
      fetch("/truso/api/su-co/da-xu-ly").then((r) => (r.ok ? r.json() : [])),
    ]);

    console.log("Dữ liệu Sự cố:", sucoRes);

    const sosMapped = (sosRes || []).map((s) => ({
      ...s,
      itemType: "SOS",
      status: s.trangThai,
      // Kiểm tra cả 2 trường thời gian phổ biến
      time: s.createdAt || s.thoiGianTao,
      isVip: s.isVip === true,
      points: s.user?.totalPoints || 0,
      hinhAnh: fixUrl(s.hinhAnh),
      ghiChu: s.ghiChu || "Cứu trợ khẩn cấp",
    }));

    const sucoMapped = (sucoRes || []).map((s) => ({
      id: s.id,
      viDo: s.viDo,
      kinhDo: s.kinhDo,
      diaChi: s.diaChi || "Không xác định",
      ghiChu: s.moTa || "Không có mô tả",
      status: s.trangThaiXuLy,
      itemType: "SUCO",
      time: s.thoiGianTao || s.createdAt,
      hinhAnh: fixUrl(s.hinhAnhUrl),
      loai: s.loaiSuCo ? s.loaiSuCo.ten : "Sự cố",
      mucDo: s.mucDoNghiemTrong || "NONE",
    }));

    allHistoryData = [...sosMapped, ...sucoMapped];

    // Sắp xếp an toàn: Nếu không có time thì cho xuống cuối
    allHistoryData.sort((a, b) => {
      const timeA = a.time ? new Date(a.time) : 0;
      const timeB = b.time ? new Date(b.time) : 0;
      return timeB - timeA;
    });

    renderHistory();
  } catch (err) {
    console.error("Lỗi script:", err);
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

  // --- RENDER HEADER ---
  if (currentFilter === "sos") {
    header.innerHTML = `<th>Phân loại</th><th>Thời gian</th><th>Địa chỉ</th><th>Hình ảnh</th><th>Ghi chú</th><th>Giá tiền</th><th>Trạng thái</th>`;
  } else {
    // Thêm cột Mức độ trước cột Trạng thái
    header.innerHTML = `<th>Thời gian</th><th>Loại sự cố</th><th>Địa chỉ</th><th>Hình ảnh</th><th>Ghi chú</th><th>Mức độ</th><th>Trạng thái</th>`;
  }

  // --- RENDER BODY ---
  filtered.forEach((item) => {
    const row = document.createElement("tr");
    const displayAddress = item.diaChi || `${item.viDo}, ${item.kinhDo}`;

    if (currentFilter === "sos") {
      // Ưu tiên hiển thị tongThanhToan (Số tiền thực trả sau giảm giá)
      const actualPrice = item.hoaDon
        ? item.hoaDon.tongThanhToan !== undefined
          ? item.hoaDon.tongThanhToan
          : item.hoaDon.thanhTien
        : 0;

      // Tìm đoạn sinh priceHtml trong `if (currentFilter === "sos")` và thay bằng:
      const priceHtml = item.hoaDon
        ? `<div class="d-flex flex-column align-items-start">
      <strong class="text-danger">${new Intl.NumberFormat("vi-VN").format(actualPrice)} đ</strong>
      <button class="btn btn-xs btn-outline-primary py-0 px-2 mt-1" style="font-size: 11px;" 
              onclick="viewInvoice(${item.id}, ${item.hoaDon.id})">
         <i class="fa-solid fa-eye"></i> Xem hóa đơn
      </button>
     </div>`
        : `<span class="text-muted">Miễn phí</span>`;

      row.innerHTML = `
                <td>${item.isVip ? '<span class="badge bg-danger"><i class="fa-solid fa-crown"></i> VIP</span>' : '<span class="badge bg-secondary">Thường</span>'}</td>
                <td><small>${formatTime(item.time)}</small></td>
                <td><div class="address-cell">${displayAddress}</div></td>
                <td>${item.hinhAnh ? `<img src="${item.hinhAnh}" class="img-preview" onclick="window.open(this.src)">` : "N/A"}</td>
                <td><small>${item.ghiChu || ""}</small></td>
                <td>${priceHtml}</td>
                <td><span class="badge bg-success">Hoàn thành</span></td>
            `;
    } else {
      // Logic hiển thị Mức độ với Badge màu
      // Tìm đoạn render mức độ và sửa lại giá trị khớp với DB của bạn
      let mucDoHtml = "";
      if (item.mucDo === "HIGH" || item.mucDo === "CAO") {
        mucDoHtml = '<span class="badge bg-danger">Cao</span>';
      } else if (item.mucDo === "MEDIUM" || item.mucDo === "TRUNG_BINH") {
        mucDoHtml =
          '<span class="badge bg-warning text-dark">Trung bình</span>';
      } else {
        mucDoHtml = '<span class="badge bg-success">Thấp</span>';
      }

      row.innerHTML = `
                <td><small>${formatTime(item.time)}</small></td>
                <td><span class="badge bg-info text-dark">${item.loai || "Sự cố"}</span></td>
                <td><div class="address-cell">${displayAddress}</div></td>
                <td>${item.hinhAnh ? `<img src="${item.hinhAnh}" class="img-preview" onclick="window.open(this.src)">` : "N/A"}</td>
                <td><small>${item.ghiChu || "Không có mô tả"}</small></td>
                <td>${mucDoHtml}</td>
                <td><span class="badge bg-success">Đã xử lý</span></td>
            `;
    }
    body.appendChild(row);
  });
}

// ĐĂNG KÝ SỰ KIỆN CLICK CHO NÚT BẤM (QUAN TRỌNG)
document.addEventListener("DOMContentLoaded", () => {
  const btnSuCo = document.getElementById("btn-filter-su-co");
  const btnSos = document.getElementById("btn-filter-sos");

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

  loadHistory();
});

// Hàm hiển thị Modal Hóa đơn đẹp mắt bằng cách ghép cả dữ liệu HoaDon và ThanhToan
async function viewInvoice(sosId, hoaDonId) {
  try {
    // 1. Tìm thông tin SOS và Hóa đơn cục bộ từ allHistoryData
    const sosItem = allHistoryData.find(
      (it) => it.id === sosId && it.itemType === "SOS",
    );
    if (!sosItem || !sosItem.hoaDon) {
      alert("Không tìm thấy dữ liệu hóa đơn!");
      return;
    }
    const hoaDon = sosItem.hoaDon;

    // 2. Gọi API để lấy nốt dữ liệu đối tác ThanhToanResponseDTO bằng hoaDonId
    // (Ông nhớ cấu hình endpoint này ở Controller bên Back-end nhé)
    const paymentRes = await fetch(`/api/thanh-toan/hoa-don/${hoaDonId}`);
    if (!paymentRes.ok) throw new Error("Không thể tải thông tin thanh toán");
    const payment = await paymentRes.json(); // Nhận về ThanhToanResponseDTO

    // 3. Đổ dữ liệu lên màn hình Modal thật đẹp
    document.getElementById("inv-id").innerText = `#HD-${hoaDon.id}`;
    document.getElementById("inv-time").innerText = formatTime(
      hoaDon.createdAt || payment.createdAt,
    );
    document.getElementById("inv-content").innerText =
      hoaDon.noiDungXuLy || "Cứu hộ và di chuyển phương tiện khẩn cấp.";

    // Tiền nong định dạng chuẩn VND
    const formatter = new Intl.NumberFormat("vi-VN");
    document.getElementById("inv-base").innerText =
      `${formatter.format(payment.thanhTien || hoaDon.thanhTien)} đ`;
    document.getElementById("inv-discount").innerText =
      `-${formatter.format(payment.soTienGiam || 0)} đ`;
    document.getElementById("inv-total").innerText =
      `${formatter.format(payment.tongThanhToan || hoaDon.thanhTien)} đ`;

    // Giao dịch thông tin
    document.getElementById("inv-method").innerText =
      payment.phuongThucThanhToan || "Tiền mặt";
    document.getElementById("inv-transaction").innerText =
      payment.maGiaoDich || "N/A";
    document.getElementById("inv-status").innerText =
      payment.trangThai || "ĐÃ THANH TOÁN";

    // Mở popup hiển thị lên
    document.getElementById("invoice-modal").style.display = "flex";
  } catch (err) {
    console.error("Lỗi xem hóa đơn:", err);
    alert("Có lỗi xảy ra: " + err.message);
  }
}

function closeInvoiceModal() {
  document.getElementById("invoice-modal").style.display = "none";
}
