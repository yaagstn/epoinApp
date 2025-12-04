package com.example.aplikasipencatatpelanggaran;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class ActivityDaftarKelas extends AppCompatActivity {

    private ListView listView;
    private BottomNavigationView bottomNavigationView;

    // Kunci untuk Intent harus didefinisikan sebagai konstanta untuk menghindari typo
    public static final String KEY_KELAS = "kelas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_kelas);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Daftar Angkatan");
        }
        // Inisialisasi View
        listView = findViewById(R.id.lisview);
        bottomNavigationView = findViewById(R.id.bottomNav);

        // Atur item yang terpilih
        bottomNavigationView.setSelectedItemId(R.id.list);

        // Data untuk ListView (Gunakan nama yang lebih deskriptif: classLevels)
        String[] classLevel = new String[]{
                "Angkatan 2023", "Angkatan 2024", "Angkatan 2025"
        };

        // Memasukkan data ke dalam ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1, classLevels);
        listView.setAdapter(adapter);

        // Setup Listener untuk klik item ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Ambil kelas yang diklik
                String kelasDipilih = adapterView.getItemAtPosition(position).toString();

                // Pindah ke ActivityKelas dan kirim data kelas
                Intent intent = new Intent(getApplicationContext(), ActivityKelas.class);
                intent.putExtra(KEY_KELAS, kelasDipilih);
                startActivity(intent);

                // Menonaktifkan animasi transisi (opsional, tapi bagus untuk UX)
                overridePendingTransition(0, 0);
            }
        });

        // Setup Listener untuk Bottom Navigation
        setupBottomNav();
    }

    // Pisahkan logika BottomNav ke metode terpisah (seperti ActivityUtama Anda)
    private void setupBottomNav() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.main) {
                    startActivity(new Intent(getApplicationContext(), ActivityUtama.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.tambah) {
                    startActivity(new Intent(getApplicationContext(), ActivityTambah.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.list) {
                    return true; // Sudah berada di halaman ini
                }
                return false;
            }
        });
    }
}
