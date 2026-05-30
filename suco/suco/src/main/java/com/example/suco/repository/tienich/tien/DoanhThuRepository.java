package com.example.suco.repository.tienich.tien;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.suco.model.HoaDon;
import com.example.suco.model.ThanhToanHoaDon;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Repository
public class DoanhThuRepository {
    
    @PersistenceContext
    private EntityManager entityManager;

   public BigDecimal getTongDoanhThu() {
        String jpql =
            "SELECT SUM(t.tongThanhToan) FROM ThanhToanHoaDon t WHERE t.trangThai = 'SUCCESS'";

        BigDecimal result = (BigDecimal) entityManager.createQuery(jpql)
                .getSingleResult();

        return result != null ? result : BigDecimal.ZERO;
    }

    public List<ThanhToanHoaDon> getDanhSachDoanhThu() {
        String jpql =
            "SELECT t FROM ThanhToanHoaDon t ORDER BY t.id DESC";

        return entityManager.createQuery(jpql, ThanhToanHoaDon.class)
                .getResultList();
    }
}
