package com.example.suco.controller.vanhanh.camera;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.example.suco.dto.vanhanh.camera.CameraMapDto;
import com.example.suco.dto.vanhanh.camera.CameraRequestDTO;
import com.example.suco.dto.vanhanh.camera.CameraResponseDTO;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.Camera;
import com.example.suco.repository.suco.baocao.SuCoAdminRepository;
import com.example.suco.repository.vanhanh.CameraRepository;
import com.example.suco.service.vanhanh.camera.CameraNearService;
import com.example.suco.service.vanhanh.camera.CameraService;

@Controller
@RequestMapping("/admin/quan-ly-camera")
public class AdminCameraController {

    @Autowired
    private CameraService cameraService;

    @Autowired
    private CameraRepository cameraRepository;

    @Autowired
    private SuCoAdminRepository baoCaoSuCoRepository;

    @Autowired
private CameraNearService cameraNearService;

    @GetMapping
    public String hienThiDanhSach(Model model) {
        // Gọi Service thay vì Repository
        model.addAttribute("danhSachCamera", cameraService.getAllCameras());
        model.addAttribute("listCameraChuaGan", cameraService.getCamerasChuaGan());
        model.addAttribute("activePage", "quan-ly-camera");
        
        return "admin/quan-ly-camera";
    }
@PostMapping(value = "/them", consumes = "multipart/form-data")
@ResponseBody
public ResponseEntity<CameraResponseDTO> themCamera(
        @ModelAttribute CameraRequestDTO dto
) {

    return ResponseEntity.ok(
            cameraService.createCamera(dto)
    );
}
@PatchMapping("/{id}")
@ResponseBody
public ResponseEntity<CameraResponseDTO> capNhatCamera(
        @PathVariable Long id,
        @ModelAttribute CameraRequestDTO dto
) {
    return ResponseEntity.ok(
            cameraService.updateCamera(id, dto)
    );
}

    @GetMapping("/all-json")
    @ResponseBody
    public List<Camera> getAllCameraJson() {
        return cameraService.getAllCameras();
    }

@DeleteMapping("/{id}")
@ResponseBody
public ResponseEntity<String> xoaCamera(
        @PathVariable Long id
) {

    try {

        cameraService.deleteCamera(id);

        return ResponseEntity.ok(
                "Xóa camera thành công!"
        );

    } catch (Exception e) {

        return ResponseEntity
                .status(404)
                .body(e.getMessage());
    }
}

@GetMapping("/{id}/detail")
@ResponseBody
public ResponseEntity<CameraResponseDTO> getCameraDetail(
        @PathVariable Long id
) {

    return ResponseEntity.ok(
            cameraService.getCameraDetail(id)
    );
}

    // API nhận tọa độ gửi từ frontend để gán cho camera
    @PostMapping("/gan-toa-do/{id}")
    @ResponseBody
    public ResponseEntity<String> ganToaDoCamera(@PathVariable Long id,
                                                 @RequestParam("kinhDo") double kinhDo,
                                                 @RequestParam("viDo") double viDo) {
        return cameraRepository.findById(id).map(cam -> {
            cam.setKinhDo(kinhDo);
            cam.setViDo(viDo);
            cameraService.saveCamera(cam);
            return ResponseEntity.ok("Gán tọa độ thành công");
        }).orElse(ResponseEntity.status(404).body("Không tìm thấy camera"));
    }

@GetMapping("/near-by-incident/{id}")
@ResponseBody
public List<CameraMapDto> getCameraByIncident(@PathVariable Long id) {

    BaoCaoSuCo report = baoCaoSuCoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sự cố"));

    return cameraNearService.getCamerasNearIncident(
            report.getViDo(),
            report.getKinhDo()
    );
}
}