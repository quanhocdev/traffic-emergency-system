// package com.example.suco.mapper.vanhanh.truso;

// import org.springframework.stereotype.Component;

// import com.example.suco.dto.vanhanh.truso.TruSoCreateRequestDTO;
// import com.example.suco.dto.vanhanh.truso.TruSoResponseDTO;
// import com.example.suco.model.TruSo;

// @Component
// public class TruSoMapper {

//     public TruSo toEntity(TruSoCreateRequestDTO dto) {
//         if (dto == null) {
//             return null;
//         }
//         TruSo truSo = new TruSo();
//         truSo.setTenDangNhap(dto.getTenDangNhap());
//         truSo.setMatKhau(dto.getMatKhau());
//         truSo.setTenTruSo(dto.getTenTruSo());
//         truSo.setKinhDo(dto.getKinhDo());
//         truSo.setViDo(dto.getViDo());
//         return truSo;
//     }

//     public TruSoResponseDTO toResponseDTO(TruSo truSo) {
//         if (truSo == null) {
//             return null;
//         }
//         TruSoResponseDTO dto = new TruSoResponseDTO();

//         dto.setId(truSo.getId());
//         dto.setTenDangNhap(truSo.getTenDangNhap());
//         dto.setTenTruSo(truSo.getTenTruSo());
//         dto.setKinhDo(truSo.getKinhDo());
//         dto.setViDo(truSo.getViDo());
//         dto.setDiaChi(truSo.getDiaChi());

//         return dto;
//     }
// }