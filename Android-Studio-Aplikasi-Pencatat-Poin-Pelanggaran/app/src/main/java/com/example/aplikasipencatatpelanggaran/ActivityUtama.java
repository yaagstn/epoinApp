package com.example.aplikasipencatatpelanggaran;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ActivityUtama extends AppCompatActivity {

    private static final String TAG = "ActivityUtamaLog";

    private RecyclerView recyclerView;
    private PelanggaranAdapter pelanggaranAdapter;
    private ArrayList<PelanggaranModel> pelanggaranModelList;
    private ArrayList<PelanggaranModel> displayedList;

    private ImageView imgempty;
    private TextView txtkosong, totalPelanggaranTextView;
    private BottomNavigationView bottomNavigationView;
    private Button btnNextPage, btnPrevPage;
    private LinearLayout paginationLayout;

    // 🔹 Pagination variable
    private int currentPage = 1;
    private final int limit = 10;
    private int totalPages = 1;
    private TextView txtPageInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_utama);

        // Inisialisasi View
        bottomNavigationView = findViewById(R.id.bottomNav);
        recyclerView = findViewById(R.id.recyclerView);
        imgempty = findViewById(R.id.imgempty);
        txtkosong = findViewById(R.id.txtkosong);
        totalPelanggaranTextView = findViewById(R.id.totalPelanggaran);
        btnNextPage = findViewById(R.id.btnNext);
        btnPrevPage = findViewById(R.id.btnPrev);
        paginationLayout = findViewById(R.id.paginationContainer);
        txtPageInfo = findViewById(R.id.tvPageNumber);
        pelanggaranModelList = new ArrayList<>();
        displayedList = new ArrayList<>();
        pelanggaranAdapter = new PelanggaranAdapter(this, displayedList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(pelanggaranAdapter);
        recyclerView.setNestedScrollingEnabled(false);

        setupBottomNav();
        setupPaginationButtons();

        masukanDataKeArray();

        bottomNavigationView.setSelectedItemId(R.id.main);
    }

    private void setupPaginationButtons() {
        btnNextPage.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++;
                tampilkanHalaman();
            } else {
                Toast.makeText(this, "Sudah di halaman terakhir", Toast.LENGTH_SHORT).show();
            }
        });

        btnPrevPage.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                tampilkanHalaman();
            } else {
                Toast.makeText(this, "Sudah di halaman pertama", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void masukanDataKeArray() {
        Log.d(TAG, "Mengambil data dari API...");

        ApiHelper.getAllData(ActivityUtama.this, new ApiHelper.DataFetchListener<JSONArray>() {
            @Override
            public void onSuccess(JSONArray response) {
                pelanggaranModelList.clear();

                if (response == null || response.length() == 0) {
                    Log.d(TAG, "Data kosong dari server.");
                    toggleEmptyState(true, "Belum ada data pelanggaran tercatat.");
                    totalPelanggaranTextView.setText("0");
                    paginationLayout.setVisibility(View.GONE);
                } else {
                    toggleEmptyState(false, null);
                    Log.d(TAG, "Data diterima: " + response.length() + " entri");

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            PelanggaranModel model = new PelanggaranModel(
                                    obj.optString("id", "N/A"),
                                    obj.optString("nama_siswa", "Nama Tidak Ada"),
                                    obj.optString("kelas_siswa", "Kelas Tidak Ada"),
                                    obj.optString("jenis_pelanggaran", "Tidak diketahui"),
                                    obj.optInt("poin", 0),
                                    obj.optString("tanggal", "Tanggal Tidak Ada")
                            );
                            pelanggaranModelList.add(model);
                        } catch (Exception e) {
                            Log.e(TAG, "Gagal parsing JSON index " + i + ": " + e.getMessage());
                        }
                    }

                    totalPelanggaranTextView.setText(String.valueOf(pelanggaranModelList.size()));
                    totalPages = (int) Math.ceil((double) pelanggaranModelList.size() / limit);
                    currentPage = 1;
                    tampilkanHalaman();
                }
            }

            @Override
            public void onError(VolleyError error) {
                Log.e(TAG, "Error API: " + error.getMessage());
                String errorMessage = error.getMessage() != null ? error.getMessage() : "Kesalahan koneksi atau server.";
                Toast.makeText(ActivityUtama.this, "Gagal mengambil data: " + errorMessage, Toast.LENGTH_LONG).show();
                toggleEmptyState(true, "Gagal memuat data. Cek koneksi atau Logcat.");
                totalPelanggaranTextView.setText("0");
                paginationLayout.setVisibility(View.GONE);
            }
        });
    }

    private void tampilkanHalaman() {
        displayedList.clear();
        int start = (currentPage - 1) * limit;
        int end = Math.min(start + limit, pelanggaranModelList.size());

        for (int i = start; i < end; i++) {
            displayedList.add(pelanggaranModelList.get(i));
        }

        pelanggaranAdapter.notifyDataSetChanged();

        // 🔹 Update status tombol
        paginationLayout.setVisibility(totalPages > 1 ? View.VISIBLE : View.GONE);
        btnPrevPage.setEnabled(currentPage > 1);
        btnPrevPage.setAlpha(currentPage > 1 ? 1.0f : 0.5f);
        btnNextPage.setEnabled(currentPage < totalPages);
        btnNextPage.setAlpha(currentPage < totalPages ? 1.0f : 0.5f);

        // 🔹 Update teks halaman
        txtPageInfo.setText("Page " + currentPage + " dari " + totalPages);

    }


    private void toggleEmptyState(boolean isEmpty, String message) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            imgempty.setVisibility(View.VISIBLE);
            txtkosong.setVisibility(View.VISIBLE);
            if (message != null) txtkosong.setText(message);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            imgempty.setVisibility(View.GONE);
            txtkosong.setVisibility(View.GONE);
        }
    }

    private void setupBottomNav() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.main) {
                    masukanDataKeArray();
                    return true;
                } else if (itemId == R.id.list) {
                    startActivity(new Intent(getApplicationContext(), ActivityDaftarKelas.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.tambah) {
                    startActivity(new Intent(getApplicationContext(), ActivityTambah.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        masukanDataKeArray();
        bottomNavigationView.setSelectedItemId(R.id.main);
    }
}
