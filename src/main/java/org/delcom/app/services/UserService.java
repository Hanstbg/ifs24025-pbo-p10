package org.delcom.app.services;

import java.util.UUID;

import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    // Dependency injection repository untuk operasi database user
    private final UserRepository userRepository;

    // Constructor injection (lebih direkomendasikan daripada @Autowired)
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Membuat user baru, disimpan dalam database
    // @Transactional memastikan operasi aman dan rollback jika gagal
    @Transactional
    public User createUser(String name, String email, String password) {
        User user = new User(name, email, password);
        return userRepository.save(user);
    }

    // Mengambil user berdasarkan email
    // Menggunakan Optional, jika tidak ada dikembalikan null
    public User getUserByEmail(String email) {
        return userRepository.findFirstByEmail(email).orElse(null);
    }

    // Mengambil user berdasarkan ID
    public User getUserById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    // Update nama dan email user berdasarkan ID
    // @Transactional untuk memastikan perubahan tersimpan dengan benar
    @Transactional
    public User updateUser(UUID id, String name, String email) {
        User user = userRepository.findById(id).orElse(null);

        // Jika user tidak ditemukan, return null
        if (user == null) {
            return null;
        }

        // Update field user lalu simpan kembali
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    // Update password user
    @Transactional
    public User updatePassword(UUID id, String newPassword) {
        User user = userRepository.findById(id).orElse(null);

        // Jika user tidak ditemukan, return null
        if (user == null) {
            return null;
        }

        // Set password baru lalu simpan
        user.setPassword(newPassword);
        return userRepository.save(user);
    }

}
