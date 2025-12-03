package com.example.aplikasipencatatpelanggaran;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.android.volley.VolleyError;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException; // Tambahkan ini

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


// CATATAN PENTING:
// PASTIKAN ANDA SUDAH MENGHAPUS SEMUA DEKLARASI 'public class ApiHelper { ... }'
// YANG ADA DI DALAM FILE INI ATAU FILE SELAIN ApiHelper.java.

public class ActivityKelas extends AppCompatActivity {

    // VARIABEL BARU DAN DIBUTUHKAN
    RecyclerView recyclerView;
    // imgempty (XML: imgempty2), txtempty (XML: txtkosong2), txtView (XML: txtkelas)
    ImageView imgempty;
    TextView txtempty, txtView;
    BottomNavigationView bottomNavigationView;
    public String kelas; // Variabel untuk menyimpan kelas yang dipilih

    // VARIABEL UNTUK ADAPTER (Menggunakan PelanggaranModel sesuai logika asli Anda)
    private List<PelanggaranModel> pelanggaranList;
    private PelanggaranAdapter pelanggaranAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kelas);

        // --- INISIALISASI KOMPONEN (Menggunakan ID yang benar dari XML Anda) ---
        recyclerView = findViewById(R.id.recyclerView);
        imgempty = findViewById(R.id.imgempty2);
        txtempty = findViewById(R.id.txtkosong2);
        txtView = findViewById(R.id.txtkelas);
        bottomNavigationView = findViewById(R.id.bottomNav);

        // Memastikan item yang terpilih sesuai dengan ID menu Anda
        bottomNavigationView.setSelectedItemId(R.id.list);

        // Ambil data 'kelas'
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("kelas")) {
            kelas = extras.getString("kelas");
            txtView.setText("Angkatan " + kelas);
        } else {
            // Ubah default ke nilai yang kemungkinan ada di database
            kelas = "XII MIPA 1";
            txtView.setText("Angkatan " + kelas + " (Default)");
            Toast.makeText(this, "Kelas tidak ditemukan, menggunakan default.", Toast.LENGTH_SHORT).show();
        }

        // --- SETUP RECYCLERVIEW ---
        pelanggaranList = new ArrayList<>();
        pelanggaranAdapter = new PelanggaranAdapter(this, pelanggaranList);
        recyclerView.setAdapter(pelanggaranAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ActivityKelas.this));

        // Sembunyikan elemen kosong saat startup
        imgempty.setVisibility(View.GONE);
        txtempty.setVisibility(View.GONE);

        // --- MEMUAT DATA DARI API ---
        loadDataPelanggaran();

        // --- SETUP BOTTOM NAVIGATION ---
        // Menggunakan listener yang Anda definisikan
        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavMethod);
    }

    // METHOD BARU UNTUK MEMUAT DATA VIA API
    private void loadDataPelanggaran() {
        // PERUBAHAN UTAMA: Menggunakan getDataByAngkatan() dan mengirim variabel 'kelas'
        ApiHelper.getDataByAngkatan(ActivityKelas.this, kelas, new ApiHelper.DataFetchListener() {

            @Override
            // Response sudah berupa JSONArray yang sudah difilter dari server
            public void onSuccess(Object response) {
                // Casting response menjadi JSONArray
                JSONArray jsonArrayResponse = (JSONArray) response;

                pelanggaranList.clear(); // Bersihkan list yang lama

                if (jsonArrayResponse.length() == 0) {
                    // Jika data kosong
                    imgempty.setVisibility(View.VISIBLE);
                    txtempty.setVisibility(View.VISIBLE);
                    txtempty.setText("Tidak ada data pelanggaran untuk Kelas "+kelas);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    // Jika ada data
                    imgempty.setVisibility(View.GONE);
                    txtempty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    // Parsing data yang sudah difilter dari server
                    for (int i = 0; i < jsonArrayResponse.length(); i++) {
                        try {
                            JSONObject obj = jsonArrayResponse.getJSONObject(i);

                            // Kita menggunakan kolom dari PHP (s.id_siswa, s.nama_siswa, p.jenis_pelanggaran, p.poin, p.tanggal)
                            String idDb = obj.getString("id_siswa"); // Kolom 'id_siswa' dari tabel siswa
                            String nama = obj.getString("nama_siswa");
                            String kls = obj.getString("kelas_siswa");

                            // Gunakan optString/optInt untuk menangani nilai NULL dari LEFT JOIN (siswa tanpa pelanggaran)
                            String jenis = obj.optString("jenis_pelanggaran", "Tidak Ada");
                            int poin = obj.optInt("poin", 0);
                            String tanggal = obj.optString("tanggal", "-");

                            // Tambahkan ke list model
                            PelanggaranModel model = new PelanggaranModel(
                                    idDb, nama, kls, jenis, poin, tanggal);
                            pelanggaranList.add(model);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ActivityKelas.this, "Error parsing JSON: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                    HashMap<String, Integer> totalMap = new HashMap<>();
                    for (PelanggaranModel p : pelanggaranList) {
                        int poinSekarang = totalMap.getOrDefault(p.getNamaSiswa(), 0);
                        totalMap.put(p.getNamaSiswa() , poinSekarang + p.getPoin());
                    }

                    // Set total poin ke setiap objek PelanggaranModel agar bisa ditampilkan di layout
                    for (PelanggaranModel p : pelanggaranList) {
                        int total = totalMap.getOrDefault(p.getNamaSiswa(), 0);
                        p.setTotalPoinUntukRingkasan(total);
                    }



                    Collections.sort(pelanggaranList, new Comparator<PelanggaranModel>() {
                        @Override
                        public int compare(PelanggaranModel o1, PelanggaranModel o2) {
                            int total1 = totalMap.getOrDefault(o1.getNamaSiswa () , 0);
                            int total2 = totalMap.getOrDefault(o2.getNamaSiswa(), 0);
                            return Integer.compare(total2, total1);
                        }
                    });


                    pelanggaranAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(VolleyError error) {
                // Gagal mengambil data (Error Volley / Koneksi atau success: false)
                String errorMessage = "Gagal memuat data! Cek koneksi & BASE_URL.";
                if (error != null && error.getMessage() != null) {
                    // Jika pesan error mengandung pesan dari server (dari success: false)
                    errorMessage = error.getMessage();
                }

                Toast.makeText(ActivityKelas.this, errorMessage, Toast.LENGTH_LONG).show();
                // error.printStackTrace(); // Optional: untuk debugging di Logcat
                imgempty.setVisibility(View.VISIBLE);
                txtempty.setText("Gagal koneksi atau data kosong. Pesan: " + errorMessage);
                txtempty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    // method untuk bottomnavigation (Memastikan ID Menu BENAR: main, tambah, list)
    private BottomNavigationView.OnNavigationItemSelectedListener bottomNavMethod = new
            BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();

                    if (itemId == R.id.main) {
                        startActivity(new Intent(getApplicationContext(), ActivityUtama.class));
                        // PERBAIKAN SINTAKS JAVA: overridePendingTransition hanya menerima integer
                        overridePendingTransition(0, 0);
                        return true;
                    } else if (itemId == R.id.tambah) {
                        startActivity(new Intent(getApplicationContext(), ActivityTambah.class));
                        // PERBAIKAN SINTAKS JAVA: overridePendingTransition hanya menerima integer
                        overridePendingTransition(0, 0);
                        return true;
                    } else if (itemId == R.id.list) {
                        // Navigasi ke ActivityDaftarKelas (asumsi ini adalah daftar kelas utama)
                        startActivity(new Intent(getApplicationContext(), ActivityDaftarKelas.class));
                        overridePendingTransition(0, 0);
                        return true;
                    }
                    return false;
                }
            };
}