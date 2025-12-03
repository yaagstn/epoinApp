package com.example.aplikasipencatatpelanggaran;

public class SiswaPelanggaran {
    private String idSiswa;
    private String namaSiswa;
    private String kelasSiswa;
    private String jenisPelanggaran; // Bisa NULL jika tidak ada pelanggaran
    private int poin; // Akan 0 jika tidak ada pelanggaran
    private String tanggal; // Bisa NULL jika tidak ada pelanggaran

    // Constructor
    public SiswaPelanggaran(String idSiswa, String namaSiswa, String kelasSiswa, String jenisPelanggaran, int poin, String tanggal) {
        this.idSiswa = idSiswa;
        this.namaSiswa = namaSiswa;
        this.kelasSiswa = kelasSiswa;
        this.jenisPelanggaran = jenisPelanggaran;
        this.poin = poin;
        this.tanggal = tanggal;
    }

    // Getters
    public String getIdSiswa() { return idSiswa; }
    public String getNamaSiswa() { return namaSiswa; }
    public String getKelasSiswa() { return kelasSiswa; }
    public String getJenisPelanggaran() { return jenisPelanggaran; }
    public int getPoin() { return poin; }
    public String getTanggal() { return tanggal; }

    // Helper untuk cek apakah ada pelanggaran (jika jenisPelanggaran adalah NULL/kosong dari PHP)
    public boolean hasPelanggaran() {
        return jenisPelanggaran != null && !jenisPelanggaran.equalsIgnoreCase("null") && !jenisPelanggaran.isEmpty();
    }
}