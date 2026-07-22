package com.example.suco.controller.xacthuc.admin;

import com.example.suco.model.RefreshTokens;
import com.example.suco.model.User;
import com.example.suco.model.enums.RefreshTokenType;
import com.example.suco.repository.RefreshTokenRepository;
import com.example.suco.repository.vanhanh.UserRepository;
import com.example.suco.security.TokenProvider;
import com.example.suco.service.xacthuc.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Controller
@RequestMapping("/admin")
public class AdminAuthController {


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private TokenProvider tokenProvider;


    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;   

    @Value("${jwt.access-expiration}")
    private long accessExpirationMs;


    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;



    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();



    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> req
    ) {


        String email = req.get("email");

        String password = req.get("password");



        User user =
                userRepository.findByEmail(email)
                        .orElse(null);



        if (user == null) {

            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "message",
                            "Sai tài khoản hoặc mật khẩu"
                    ));
        }



        if (!passwordEncoder.matches(
                password,
                user.getPassword()
        )) {

            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "message",
                            "Sai tài khoản hoặc mật khẩu"
                    ));
        }



        if (!"ADMIN".equals(user.getRole())) {

            return ResponseEntity
                    .status(403)
                    .body(Map.of(
                            "message",
                            "Không có quyền truy cập vùng quản trị"
                    ));
        }



        /*
         * 1. Tạo Access Token
         */
        String accessToken =
                tokenProvider.generateAccessToken(
                        user.getUid(),
                        user.getRole()
                );

/*
 * 2. Xóa refresh token cũ
 */
refreshTokenService.deleteOldRefreshToken(
        user.getUid(),
        RefreshTokenType.USER
);

        /*
         * 3. Tạo Refresh Token
         */
        TokenProvider.RefreshTokenInfo refreshInfo =
                tokenProvider.generateRefreshToken(
                        user.getUid(),
                        RefreshTokenType.USER.name()
                );



                

        /*
         * 4. Lưu jti refresh token xuống DB
         */
        RefreshTokens refreshToken = new RefreshTokens();

        refreshToken.setJti(
                refreshInfo.jti()
        );


        refreshToken.setAccountId(
                user.getUid()
        );


        refreshToken.setAccountType(
                RefreshTokenType.USER
        );


        refreshToken.setExpiresAt(
                refreshInfo.expiresAt()
        );


        refreshTokenRepository.save(refreshToken);



        /*
         * 5. Lưu JWT vào Cookie
         */

        ResponseCookie accessCookie =
                ResponseCookie.from(
                        "accessToken",
                        accessToken
                )
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(accessExpirationMs / 1000)
                .build();



        ResponseCookie refreshCookie =
                ResponseCookie.from(
                        "refreshToken",
                        refreshInfo.token()
                )
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(refreshExpirationMs / 1000)
                .build();



        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        accessCookie.toString()
                )
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookie.toString()
                )
                .body(
                        Map.of(
                                "message",
                                "Đăng nhập admin thành công"
                        )
                );
    }




    @GetMapping("/login")
    public String loginPage() {

        return "admin/login";
    }
}