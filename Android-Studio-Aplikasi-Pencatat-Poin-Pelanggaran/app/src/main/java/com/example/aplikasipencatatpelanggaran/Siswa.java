package com.example.aplikasipencatatpelanggaran;
// Pastikan package sesuai dengan proyek Anda

public class Siswa {
    private String idSiswa;
    private String namaSiswa;
    private String kelasSiswa;

    public Siswa(String idSiswa, String namaSiswa, String kelasSiswa) {
        this.idSiswa = idSiswa;
        this.namaSiswa = namaSiswa;
        this.kelasSiswa = kelasSiswa;
    }

    // --- GETTERS ---
    public String getIdSiswa() {
        return idSiswa;
    }

    public String getNamaSiswa() {
        return namaSiswa;
    }

    public String getKelasSiswa() {
        return kelasSiswa;
    }

    // 🔴 KRITIS: Metode ini menentukan teks yang muncul di AutoCompleteTextView.
    // Tujuannya agar pengguna melihat NAMA (KELAS).
    @Override
    public String toString() {
        return namaSiswa;
    }

    // 🔴 KRITIS: Metode ini membantu membandingkan input teks dengan item di daftar.
    // Jika input yang diketik pengguna sama dengan output toString() atau namaSiswa saja,
    // maka dianggap cocok.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Siswa siswa = (Siswa) o;
        return idSiswa.equals(siswa.idSiswa);
    }

    @Override
    public int hashCode() {
        return idSiswa.hashCode();
    }
}
