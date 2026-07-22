package com.example.suco.controller.xacthuc.truso;


import java.util.Optional;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.example.suco.service.xacthuc.RefreshTokenService;
import com.example.suco.model.RefreshTokens;
import com.example.suco.model.TruSo;
import com.example.suco.model.enums.RefreshTokenType;

import com.example.suco.repository.RefreshTokenRepository;
import com.example.suco.repository.vanhanh.TruSoRepository;

import com.example.suco.security.TokenProvider;



@Controller
@RequestMapping("/truso")
public class LoginController {


    private final TruSoRepository truSoRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final TokenProvider tokenProvider;

    private final RefreshTokenService refreshTokenService;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();



    @Value("${jwt.access-expiration}")
    private long accessExpirationMs;


    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;




    public LoginController(
            TruSoRepository truSoRepository,
            RefreshTokenRepository refreshTokenRepository,
            TokenProvider tokenProvider,
            RefreshTokenService refreshTokenService
    ) {

        this.truSoRepository = truSoRepository;

        this.refreshTokenRepository = refreshTokenRepository;

        this.tokenProvider = tokenProvider;

        this.refreshTokenService = refreshTokenService;
    }





    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(
            @RequestParam String username,
            @RequestParam String password
    ) {



        Optional<TruSo> truSo =
                truSoRepository.findByTenDangNhap(username);



        if (truSo.isEmpty()) {

            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "message",
                            "Sai tài khoản hoặc mật khẩu"
                    ));
        }



        TruSo t = truSo.get();



        if (!passwordEncoder.matches(
                password,
                t.getMatKhau()
        )) {

            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "message",
                            "Sai tài khoản hoặc mật khẩu"
                    ));
        }





        /*
         * 1. Tạo Access Token
         */
        String accessToken =
                tokenProvider.generateAccessToken(
                        String.valueOf(t.getId()),
                        "TRUSO"
                );



                /*
 * Xóa refresh token cũ
 */
refreshTokenService.deleteOldRefreshToken(
        String.valueOf(t.getId()),
        RefreshTokenType.TRUSO
);
        /*
         * 2. Tạo Refresh Token
         */
        TokenProvider.RefreshTokenInfo refreshInfo =
                tokenProvider.generateRefreshToken(
                        String.valueOf(t.getId()),
                        RefreshTokenType.TRUSO.name()
                );





        /*
         * 3. Lưu refresh token
         */
        RefreshTokens refreshToken =
                new RefreshTokens();


        refreshToken.setJti(
                refreshInfo.jti()
        );


        refreshToken.setAccountId(
                String.valueOf(t.getId())
        );


        refreshToken.setAccountType(
                RefreshTokenType.TRUSO
        );

        refreshToken.setRole(
        "TRUSO"
);


        refreshToken.setExpiresAt(
                refreshInfo.expiresAt()
        );


        refreshTokenRepository.save(refreshToken);






        /*
         * 4. Tạo Cookie
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
                                "Login success",
                                "id",
                                t.getId(),
                                "tenTruSo",
                                t.getTenTruSo(),
                                "tenDangNhap",
                                t.getTenDangNhap()
                        )
                );

    }

}