package org.delcom.app.services;

import java.util.List;
import java.util.UUID;

import org.delcom.app.entities.Todo;
import org.delcom.app.repositories.TodoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TodoService {

    // Inject repository untuk operasi database pada entity Todo
    private final TodoRepository todoRepository;

    // Constructor injection
    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    // Membuat todo baru dan menyimpannya ke database
    @Transactional
    public Todo createTodo(UUID userId, String title, String description) {
        // Default isFinished = false saat membuat todo
        Todo todo = new Todo(userId, title, description, false);
        return todoRepository.save(todo);
    }

    // Mengambil semua todo milik user tertentu
    // Jika terdapat query search, maka pencarian difilter berdasarkan keyword
    public List<Todo> getAllTodos(UUID userId, String search) {
        // Jika search tidak kosong → lakukan pencarian berdasarkan title/description
        if (search != null && !search.trim().isEmpty()) {
            return todoRepository.findByKeyword(userId, search);
        }

        // Kalau tidak ada search → ambil semua data (⚠️ catatan: ini mengambil semua TOD0 tanpa filter user)
        return todoRepository.findAll();
    }

    // Mengambil todo spesifik berdasarkan userId dan id todo
    // Supaya user tidak bisa ambil todo milik orang lain
    public Todo getTodoById(UUID userId, UUID id) {
        return todoRepository.findByUserIdAndId(userId, id).orElse(null);
    }

    // Update todo berdasarkan id dan user yang memiliki todo tersebut
    @Transactional
    public Todo updateTodo(UUID userId, UUID id, String title, String description, Boolean isFinished) {
        // Check apakah todo ada dan memang milik user terkait
        Todo todo = todoRepository.findByUserIdAndId(userId, id).orElse(null);
        if (todo != null) {
            // Update semua field yang perlu
            todo.setTitle(title);
            todo.setDescription(description);
            todo.setFinished(isFinished);
            return todoRepository.save(todo);
        }
        return null; // return null jika todo tidak ditemukan
    }

    // Menghapus todo, tetapi hanya jika todo tersebut milik user yang sedang login
    @Transactional
    public boolean deleteTodo(UUID userId, UUID id) {
        // Validasi ownership todo
        Todo todo = todoRepository.findByUserIdAndId(userId, id).orElse(null);
        if (todo == null) {
            return false; // tidak ada todo atau bukan milik user
        }

        // Jika ada, hapus todo
        todoRepository.deleteById(id);
        return true;
    }
}
