package com.example.aplikasipencatatpelanggaran;

import com.google.gson.annotations.SerializedName;
// Pastikan library GSON sudah ditambahkan di file build.gradle (app) Anda

public class PelanggaranModel {

    // Gunakan @SerializedName untuk memetakan nama kolom di JSON (SQL)
    // ke nama variabel Java (camelCase)

    @SerializedName("id")
    private String id;

    @SerializedName("nama_siswa")
    private String namaSiswa;

    @SerializedName("kelas_siswa") // Perbaikan 1: Gunakan nama kolom SQL yang sebenarnya
    private String kelasSiswa;

    @SerializedName("jenis_pelanggaran") // Perbaikan 2: Wajib ada underscore untuk match SQL
    private String jenisPelanggaran;

    @SerializedName("poin")
    private int poin;

    @SerializedName("tanggal") // Key ini sudah benar (sesuai koreksi PHP)
    private String tanggal;

    // --- FIELD BARU UNTUK KEPENTINGAN RINGKASAN KELAS ---
    private int totalPoinKelas;
    // -----------------------------------------------------------------------


    // Konstruktor Utama (Untuk data pelanggaran detail)
    public PelanggaranModel(String id, String namaSiswa, String kelasSiswa, String jenisPelanggaran, int poin, String tanggal) {
        this.id = id;
        this.namaSiswa = namaSiswa;
        this.kelasSiswa = kelasSiswa;
        this.jenisPelanggaran = jenisPelanggaran;
        this.poin = poin;
        this.tanggal = tanggal;
    }

    // Konstruktor Ringkasan Kelas (Untuk ActivityKelas)
    public PelanggaranModel(String namaKelas, int totalPoin) {
        this.namaSiswa = namaKelas; // Simpan Nama Kelas di field namaSiswa
        this.totalPoinKelas = totalPoin; // Simpan Total Poin di field baru
    }


    // --- GETTER UTAMA ---
    // Getter tidak berubah, karena mereka mengambil dari variabel private di kelas ini
    public String getId() {
        return id;
    }

    public String getNamaSiswa() {
        return namaSiswa;
    }

    public String getKelasSiswa() {
        return kelasSiswa;
    }

    public String getJenisPelanggaran() {
        return jenisPelanggaran;
    }

    public int getPoin() {
        return poin;
    }

    public String getTanggal() {
        return tanggal;
    }

    // --- GETTER KHUSUS UNTUK RINGKASAN KELAS ---
    public String getNamaKelasUntukRingkasan() {
        return namaSiswa;
    }

    public int getTotalPoinUntukRingkasan() {
        return totalPoinKelas;
    }

    public void setTotalPoinUntukRingkasan(int totalPoinKelas) {
        this.totalPoinKelas = totalPoinKelas;
    }

}
