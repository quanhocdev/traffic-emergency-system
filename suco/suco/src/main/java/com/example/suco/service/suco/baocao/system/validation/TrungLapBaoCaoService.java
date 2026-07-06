package com.example.suco.service.suco.baocao.system.validation;

import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.BaoCaoTrungLap;
import com.example.suco.model.enums.TrangThaiXuLy;
import com.example.suco.repository.suco.baocao.SuCoAdminRepository;
import com.example.suco.repository.suco.baocao.BaoCaoTrungLapRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrungLapBaoCaoService {

    @Autowired
    private SuCoAdminRepository reportRepository;

    @Autowired
    private BaoCaoTrungLapRepository baoCaoTrungLapRepository;

    public BaoCaoSuCo findDuplicateReport(BaoCaoSuCo report) {
        if (report.getViDo() == null || report.getKinhDo() == null || report.getLoaiSuCo() == null) {
            return null;
        }

        List<BaoCaoSuCo> allReports = reportRepository.findAllForAdminDashboard();

        // Lọc lấy các sự cố đang hoạt động (không phải HOAN_THANH hay HUY_BO) trực tiếp trên Stream
        List<BaoCaoSuCo> activeReports = allReports.stream()
                .filter(s -> s.getTrangThaiXuLy() != TrangThaiXuLy.HOAN_THANH 
                          && s.getTrangThaiXuLy() != TrangThaiXuLy.HUY_BO)
                .toList();

        for (BaoCaoSuCo ex : activeReports) {
            if (ex.getLoaiSuCo() == null) {
                continue;
            }

            // So sánh ID của loại sự cố xem có trùng khớp nhau không
            if (!ex.getLoaiSuCo().getId().equals(report.getLoaiSuCo().getId())) {
                continue;
            }

            double distanceMeters = calculateDistance(
                    report.getViDo(),
                    report.getKinhDo(),
                    ex.getViDo(),
                    ex.getKinhDo()
            );

            // Bán kính trùng lặp trong khoảng 20 mét
            if (distanceMeters <= 20) {
                return ex;
            }
        }

        return null;
    }

    public Double calculateMatchedDistance(BaoCaoSuCo report, BaoCaoSuCo existingReport) {
        if (report.getViDo() == null || report.getKinhDo() == null || 
            existingReport.getViDo() == null || existingReport.getKinhDo() == null) {
            return 0.0;
        }
        return calculateDistance(
                report.getViDo(),
                report.getKinhDo(),
                existingReport.getViDo(),
                existingReport.getKinhDo()
        );
    }

    public boolean isUserAlreadyContributed(Long reportId, String uid) {
        return baoCaoTrungLapRepository.existsByBaoCao_IdAndUserId(reportId, uid);
    }

    public void saveDuplicateContributor(BaoCaoSuCo existingReport, String uid) {
        baoCaoTrungLapRepository.save(new BaoCaoTrungLap(existingReport, uid));
    }

    public void recalculateTrust(BaoCaoSuCo report) {
        // Độ tin cậy = số lượng người báo trùng + 1 (người báo đầu tiên)
        int trust = baoCaoTrungLapRepository.countByBaoCaoId(report.getId()) + 1;
        report.setDoTinCay(trust);
    }

    /**
     * Tính khoảng cách giữa 2 tọa độ theo công thức Haversine (Mét)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371e3; // Bán kính Trái Đất tính bằng mét
        double dPhi = Math.toRadians(lat2 - lat1);
        double dLambda = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dPhi / 2) * Math.sin(dPhi / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLambda / 2)
                * Math.sin(dLambda / 2);

        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}