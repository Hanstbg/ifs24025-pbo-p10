package org.delcom.app.services;

import java.util.UUID;

import org.delcom.app.entities.AuthToken;
import org.delcom.app.repositories.AuthTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthTokenService {

    // Repository untuk operasi database auth token
    private final AuthTokenRepository authTokenRepository;

    // Constructor injection
    public AuthTokenService(AuthTokenRepository authTokenRepository) {
        this.authTokenRepository = authTokenRepository;
    }

    // Mengecek token milik user tertentu
    // readOnly = true untuk performa, karena tidak mengubah database
    @Transactional(readOnly = true)
    public AuthToken findUserToken(UUID userId, String token) {
        return authTokenRepository.findUserToken(userId, token);
    }

    // Menyimpan token baru ke database (biasanya saat login)
    @Transactional
    public AuthToken createAuthToken(AuthToken authToken) {
        return authTokenRepository.save(authToken);
    }

    // Menghapus semua token milik user tertentu (logout atau force logout)
    @Transactional
    public void deleteAuthToken(UUID userId) {
        authTokenRepository.deleteByUserId(userId);
    }
}
