package com.example.aplikasipencatatpelanggaran;

/**
 * Kelas ini digunakan untuk memproses respons dari server
 * terutama pada operasi Register dan Login, yang hanya mengembalikan
 * status ("success" atau "error") dan message.
 * Catatan: Karena Anda menggunakan Volley, ini mungkin tidak diperlukan
 * jika Anda langsung memproses JSON di Activity, tapi akan membuat kode lebih rapi.
 */
public class ResponModel {
    private String status;
    private String message;

    // Tambahkan konstruktor (Jika Anda tidak menggunakan library JSON parsing seperti Gson)
    public ResponModel(String status, String message) {
        this.status = status;
        this.message = message;
    }

    // Getter
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    // Metode bantuan
    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }
}
