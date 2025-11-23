package org.delcom.app.interceptors;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    protected AuthContext authContext;

    @Autowired
    protected AuthTokenService authTokenService;

    @Autowired
    protected UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // Mengecek apakah endpoint yang diakses adalah endpoint public (tidak butuh autentikasi)
        if (isPublicEndpoint(request)) {
            return true; // jika endpoint public, lanjutkan request
        }

        // Mengambil header Authorization (Bearer Token)
        String rawAuthToken = request.getHeader("Authorization");
        // Mengambil hanya token-nya saja tanpa kata "Bearer "
        String token = extractToken(rawAuthToken);

        // Jika token kosong atau null, langsung tolak
        if (token == null || token.isEmpty()) {
            sendErrorResponse(response, 401, "Token autentikasi tidak ditemukan");
            return false;
        }

        // Validasi format JWT token
        if (!JwtUtil.validateToken(token, true)) {
            sendErrorResponse(response, 401, "Token autentikasi tidak valid");
            return false;
        }

        // Ekstrak userId dari token JWT
        UUID userId = JwtUtil.extractUserId(token);
        if (userId == null) {
            sendErrorResponse(response, 401, "Format token autentikasi tidak valid");
            return false;
        }

        // Cek apakah token ini ada dan masih aktif di database
        AuthToken authToken = authTokenService.findUserToken(userId, token);
        if (authToken == null) {
            sendErrorResponse(response, 401, "Token autentikasi sudah expired");
            return false;
        }

        // Ambil data user berdasarkan userId pada token
        User authUser = userService.getUserById(authToken.getUserId());
        if (authUser == null) {
            sendErrorResponse(response, 404, "User tidak ditemukan");
            return false;
        }

        // Simpan user yang sedang login ke AuthContext untuk dipakai di seluruh request
        authContext.setAuthUser(authUser);
        return true; // izinkan proses lanjut
    }

    // Mengambil token dari header Authorization
    private String extractToken(String rawAuthToken) {
        // Pastikan format header adalah "Bearer <token>"
        if (rawAuthToken != null && rawAuthToken.startsWith("Bearer ")) {
            return rawAuthToken.substring(7); // ambil setelah kata "Bearer "
        }
        return null;
    }

    // Menentukan endpoint mana saja yang tidak memerlukan autentikasi
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();

        // /api/auth untuk login/register
        // /error untuk fallback error handler Spring
        return path.startsWith("/api/auth") || path.equals("/error");
    }

    // Mengirimkan response error custom dalam format JSON
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"status\":\"fail\",\"message\":\"%s\",\"data\":null}",
                message);
        response.getWriter().write(jsonResponse);
    }
}
