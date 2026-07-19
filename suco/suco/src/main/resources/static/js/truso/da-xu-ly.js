const TRUSO_ID = window.TRUSO_ID;
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
      fetch("/truso/api/sos/da-xu-ly").then((r) => {
        if (!r.ok) throw new Error(`SOS API trả về mã lỗi: ${r.status}`);
        return r.json();
      }),
      // 2. Kiểm tra luôn đường dẫn Sự cố xem đã chuẩn /truso/api/su-co/da-xu-ly chưa
      fetch("/truso/api/su-co/da-xu-ly").then((r) => {
        if (!r.ok) throw new Error(`Sự cố API trả về mã lỗi: ${r.status}`);
        return r.json();
      }),
    ]);

    console.log("Dữ liệu Sự cố:", sucoRes);

    const sosMapped = (sosRes || []).map((s) => ({
      ...s,
      itemType: "SOS",
      status: s.trangThai,
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

  if (currentFilter === "sos") {
    header.innerHTML = `<th>Phân loại</th><th>Thời gian</th><th>Địa chỉ</th><th>Hình ảnh</th><th>Ghi chú</th><th>Giá tiền</th><th>Trạng thái</th>`;
  } else {
    header.innerHTML = `<th>Thời gian</th><th>Loại sự cố</th><th>Địa chỉ</th><th>Hình ảnh</th><th>Ghi chú</th><th>Mức độ</th><th>Trạng thái</th>`;
  }

  filtered.forEach((item) => {
    const row = document.createElement("tr");
    const displayAddress = item.diaChi || `${item.viDo}, ${item.kinhDo}`;

    if (currentFilter === "sos") {
      const actualPrice = item.hoaDon
        ? item.hoaDon.tongThanhToan !== undefined
          ? item.hoaDon.tongThanhToan
          : item.hoaDon.thanhTien
        : 0;

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

// Hàm hiển thị dữ liệu lấy từ HoaDonDetailDTO (1 API duy nhất)
async function viewInvoice(sosId, hoaDonId) {
  try {
    // Gọi API tổng hợp trả về HoaDonDetailDTO
    const response = await fetch(
      `/truso/hoa-don/${hoaDonId}/chi-tiet-tong-hop`,
    );
    if (!response.ok)
      throw new Error("Không thể tải chi tiết hóa đơn tổng hợp từ hệ thống");

    const data = await response.json(); // Nhận về dạng { hoaDon: {...}, thanhToans: [...] }
    const hoaDon = data.hoaDon;
    const thanhToans = data.thanhToans || [];

    // Đổ dữ liệu cơ bản của hóa đơn
    document.getElementById("inv-id").innerText = `#HD-${hoaDon.id}`;
    document.getElementById("inv-time").innerText = formatTime(
      hoaDon.createdAt,
    );
    document.getElementById("inv-content").innerText =
      hoaDon.noiDungXuLy || "Cứu hộ và di chuyển phương tiện khẩn cấp.";

    const formatter = new Intl.NumberFormat("vi-VN");

    // Nếu mảng thanhToans có phần tử (Đã có lịch sử xử lý giao dịch)
    if (thanhToans.length > 0) {
      const activePayment = thanhToans[0]; // Lấy transaction mới nhất do repo sắp xếp Desc rồi

      document.getElementById("inv-base").innerText =
        `${formatter.format(activePayment.thanhTien)} đ`;
      document.getElementById("inv-discount").innerText =
        `-${formatter.format(activePayment.soTienGiam || 0)} đ`;
      document.getElementById("inv-total").innerText =
        `${formatter.format(activePayment.tongThanhToan)} đ`;

      document.getElementById("inv-method").innerText =
        activePayment.phuongThucThanhToan || "Thẻ/Ví điện tử";
      document.getElementById("inv-transaction").innerText =
        activePayment.maGiaoDich || "N/A";
      document.getElementById("inv-status").innerText =
        activePayment.trangThai || "SUCCESS";
    } else {
      // Trường hợp dự phòng nếu chưa phát sinh transaction thanh toán nào ngoài DB
      document.getElementById("inv-base").innerText =
        `${formatter.format(hoaDon.thanhTien)} đ`;
      document.getElementById("inv-discount").innerText = "0 đ";
      document.getElementById("inv-total").innerText =
        `${formatter.format(hoaDon.thanhTien)} đ`;

      document.getElementById("inv-method").innerText = "Chưa thanh toán";
      document.getElementById("inv-transaction").innerText = "N/A";
      document.getElementById("inv-status").innerText =
        hoaDon.trangThai || "PENDING";
    }

    document.getElementById("invoice-modal").style.display = "flex";
  } catch (err) {
    console.error("Lỗi xem hóa đơn:", err);
    alert("Có lỗi xảy ra: " + err.message);
  }
}

function closeInvoiceModal() {
  document.getElementById("invoice-modal").style.display = "none";
}
