package org.delcom.app.repositories;

import java.util.UUID;

import org.delcom.app.entities.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {

    // Query untuk mencari token berdasarkan userId dan token value
    // Digunakan untuk mengecek apakah token masih aktif/ada di database
    @Query("SELECT at FROM AuthToken at WHERE at.userId = ?1 AND at.token = ?2")
    AuthToken findUserToken(UUID userId, String token);

    // Menghapus semua token milik user tertentu (biasanya saat logout atau force logout)
    @Modifying // menandakan bahwa query ini melakukan perubahan data (DELETE)
    @Transactional // memastikan operasi delete berjalan dalam transaksi
    @Query("DELETE FROM AuthToken at WHERE at.userId = ?1")
    void deleteByUserId(UUID userId);
}
