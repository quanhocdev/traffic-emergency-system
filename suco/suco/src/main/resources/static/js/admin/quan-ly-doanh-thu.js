let detailModal;

document.addEventListener("DOMContentLoaded", () => {
  const modalEl = document.getElementById("detailModal");

  if (modalEl) {
    detailModal = new bootstrap.Modal(modalEl);
  }
});

function formatMoney(value) {
  if (value === null || value === undefined) return "0 VNĐ";
  return new Intl.NumberFormat("vi-VN").format(Number(value)) + " VNĐ";
}

let loadingDetail = false;

async function openDetail(hoaDonId) {
  if (loadingDetail) return;
  loadingDetail = true;

  try {
    document.getElementById("hoaDonInfo").innerText = "Đang tải...";

    const res = await fetch(`/admin/quan-ly-doanh-thu/hoa-don/${hoaDonId}`);

    if (!res.ok) {
      throw new Error("Không lấy được dữ liệu hóa đơn");
    }

    const data = await res.json();

    renderHoaDonInfo(data.hoaDon);
    renderPayments(data.thanhToans);

    detailModal.show();
  } catch (err) {
    console.error(err);
    document.getElementById("hoaDonInfo").innerText = "Lỗi tải dữ liệu hóa đơn";
  } finally {
    loadingDetail = false;
  }
}

function renderHoaDonInfo(hd) {
  if (!hd) return;

  document.getElementById("hoaDonInfo").innerHTML = `
    <div>

      <strong>ID:</strong> ${hd.id}
      <br/>

      <strong>Mã SOS:</strong> ${hd.sosId}
      <br/>

      <strong>Trụ sở:</strong> ${hd.trusoId}
      <br/>

      <strong>Phương thức:</strong> ${hd.phuongThuc || ""}
      <br/>

      <strong>Trạng thái:</strong> ${hd.trangThai}
      <br/>

      <strong>Thành tiền:</strong> ${formatMoney(hd.thanhTien)}

    </div>
  `;
}

function renderPayments(list) {
  const tbody = document.getElementById("paymentTable");

  if (!tbody) return;

  tbody.innerHTML = `
    <tr>
      <td colspan="4" class="text-center text-muted">
        Đang xử lý...
      </td>
    </tr>
  `;

  if (!list || list.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="4" class="text-center text-muted">
          Không có giao dịch
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = list
    .map(
      (p) => `
      <tr>
        <td>${p.createdAt || ""}</td>
        <td>${p.phuongThucThanhToan || ""}</td>
        <td>${formatMoney(p.tongThanhToan)}</td>
        <td>
          <span class="badge ${
            p.trangThai === "SUCCESS"
              ? "bg-success"
              : p.trangThai === "PENDING"
                ? "bg-warning text-dark"
                : "bg-secondary"
          }">
            ${p.trangThai}
          </span>
        </td>
      </tr>
    `,
    )
    .join("");
}
