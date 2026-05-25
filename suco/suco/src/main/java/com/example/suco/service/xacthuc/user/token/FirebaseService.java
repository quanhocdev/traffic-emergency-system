package com.example.suco.service.xacthuc.user.token;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;

@Service
public class FirebaseService {

    public String extractUid(String authHeader) throws FirebaseAuthException {

        if (authHeader == null || authHeader.isBlank()) {
            throw new RuntimeException("Authorization header không tồn tại");
        }

        String token = authHeader.replace("Bearer ", "");

        FirebaseToken decodedToken =
                FirebaseAuth.getInstance().verifyIdToken(token);

        return decodedToken.getUid();
    }
}