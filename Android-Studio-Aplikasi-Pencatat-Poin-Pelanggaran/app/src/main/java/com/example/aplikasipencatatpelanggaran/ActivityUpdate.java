package com.example.aplikasipencatatpelanggaran;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log; // PENTING: Tambahkan ini untuk debugging
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import java.util.Calendar;

public class ActivityUpdate extends AppCompatActivity {

    // --- VARIABEL VIEW DENGAN TIPE YANG TEPAT ---
    // Mengubah nama variabel agar lebih jelas, tapi ID di onCreate tetap sama
    private EditText etUpdateNamaSiswa;
    private Spinner spinnerJenisPelanggaran;
    private EditText etTanggal;
    private TextView tvPoinPelanggaran;
    private Button btnUbah;
    private Button btnHapus;

    // Variabel Data
    String id_data, nama_siswa_data, kelas_siswa_data, jenis_pelanggaran_data, poin_data, id;
    String tanggal_data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        // --- INISIALISASI VARIABEL VIEWS ---
        // etUpdateNamaSiswa mengacu pada ID etUpdateNamaKelas di XML
        etUpdateNamaSiswa = findViewById(R.id.etUpdateNamaKelas);
        spinnerJenisPelanggaran = findViewById(R.id.spinnerUpdateJenisPelanggaran);
        etTanggal = findViewById(R.id.etUpdateTanggal);
        tvPoinPelanggaran = findViewById(R.id.tvUpdatePoinPelanggaran);
        btnUbah = findViewById(R.id.btnUbah);
        btnHapus = findViewById(R.id.btnHapus);

        // Atur Poin Pelanggaran menjadi tidak dapat diedit
        // Ini memastikan TextView tidak diklik, meskipun kode TextView sudah benar
        tvPoinPelanggaran.setFocusable(false);
        tvPoinPelanggaran.setClickable(false);

        // Panggil fungsi untuk memuat data dari Intent dan mengisi Views
        getAndSetIntentData();

        // 1. Tambahkan DatePicker untuk Tanggal
        etTanggal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // 2. Listener untuk tombol Ubah
        btnUbah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Tambahkan log untuk memastikan tombol diklik
                Log.d("UpdateActivity", "Tombol Ubah diklik");
                updateData();
            }
        });

        // 3. Listener untuk tombol Hapus
        btnHapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                konfirmasi();
            }
        });
    }

    // --- FUNGSI MENGAMBIL DATA INTENT DAN MENGATUR VIEW ---
    void getAndSetIntentData() {
        // Pengecekan Intent
        if (getIntent().hasExtra("id_asli") && getIntent().hasExtra("nama_siswa") &&
                getIntent().hasExtra("kelas") && getIntent().hasExtra("jenis_pelanggaran") &&
                getIntent().hasExtra("tanggal") && getIntent().hasExtra("poin")) {

            id = getIntent().getStringExtra("id_asli");
            nama_siswa_data = getIntent().getStringExtra("nama_siswa");
            kelas_siswa_data = getIntent().getStringExtra("kelas");
            jenis_pelanggaran_data = getIntent().getStringExtra("jenis_pelanggaran");
            tanggal_data = getIntent().getStringExtra("tanggal");
            poin_data = getIntent().getStringExtra("poin");

            // Tambahkan log untuk debugging
            Log.d("UpdateActivity", "Data Intent Loaded. ID: " + id + ", Nama: " + nama_siswa_data);


            // 1. SET Nama Siswa
            etUpdateNamaSiswa.setText(nama_siswa_data);

            // 2. SET Jenis Pelanggaran (PENTING: Memilih item di Spinner)
            setSpinnerSelection(jenis_pelanggaran_data);

            // 3. SET Tanggal
            etTanggal.setText(tanggal_data);

            // 4. SET Poin
            tvPoinPelanggaran.setText(poin_data);

        } else {
            Toast.makeText(this, "Tidak ada data yang dimuat untuk diubah. Pastikan semua Kunci (Keys) Intent terkirim.", Toast.LENGTH_LONG).show();
            // Tambahkan log jika intent gagal
            Log.e("UpdateActivity", "Gagal memuat Intent data.");
        }
    }

    // FUNGSI UNTUK MENGATUR SPINNER BERDASARKAN DATA LAMA
    private void setSpinnerSelection(String jenisPelanggaran) {
        // Adaptor Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.pelanggaran,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerJenisPelanggaran.setAdapter(adapter);

        // Mencari posisi data lama di dalam Spinner
        if (adapter != null && jenisPelanggaran != null) {
            int spinnerPosition = adapter.getPosition(jenisPelanggaran);
            if (spinnerPosition >= 0) {
                spinnerJenisPelanggaran.setSelection(spinnerPosition);
            }
        }
    }


    // --- FUNGSI DATE PICKER ---
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();

        if (!TextUtils.isEmpty(etTanggal.getText().toString())) {
            try {
                // Asumsi format tanggal DD/MM/YYYY
                String[] dateParts = etTanggal.getText().toString().split("/");
                int day = Integer.parseInt(dateParts[0]);
                // -1 karena bulan di Calendar dimulai dari 0 (Januari)
                int month = Integer.parseInt(dateParts[1]) - 1;
                int year = Integer.parseInt(dateParts[2]);
                calendar.set(year, month, day);
            } catch (Exception e) {
                // Log jika gagal parsing
                Log.e("UpdateActivity", "Gagal parsing tanggal lama", e);
            }
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    etTanggal.setText(formattedDate);
                }, year, month, day);

        datePickerDialog.show();
    }


    // --- FUNGSI UPDATE DATA ---
    private void updateData() {
        // 1. Ambil semua nilai baru
        String namaSiswaBaru = etUpdateNamaSiswa.getText().toString().trim();
        String jenisPelanggaran = spinnerJenisPelanggaran.getSelectedItem().toString();
        String tanggalSiswa = etTanggal.getText().toString().trim();

        // KELAS SISWA tetap menggunakan data lama
        String kelasSiswaLama = kelas_siswa_data;

        // ID Data harus dipastikan ada
        if (id == null || id.isEmpty()) {
            Toast.makeText(this, "Kesalahan sistem: ID data tidak ditemukan.", Toast.LENGTH_SHORT).show();
            Log.e("UpdateActivity", "ID data null atau kosong.");
            return;
        }


        // 2. VALIDASI INPUT

        // VALIDASI SPINNER (menggunakan nilai array index 0, biasanya "Pilih Jenis Pelanggaran")
        if (jenisPelanggaran.equals(getResources().getStringArray(R.array.pelanggaran)[0])) {
            Toast.makeText(this, "Silahkan pilih jenis pelanggaran yang valid.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validasi input Nama Siswa dan Tanggal
        if (namaSiswaBaru.isEmpty() || tanggalSiswa.isEmpty()) {
            Toast.makeText(ActivityUpdate.this, "Nama Siswa dan Tanggal harus diisi.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Hitung Poin Otomatis
        final int poinBaruInt = hitungPoin(jenisPelanggaran);
        final String poinBaruString = String.valueOf(poinBaruInt);

        tvPoinPelanggaran.setText(poinBaruString);

        // Tambahkan log data yang akan dikirim
        Log.i("UpdateActivity", "Data akan di-update. Nama: " + namaSiswaBaru + ", Jenis: " + jenisPelanggaran + ", Poin: " + poinBaruString);

        // 4. Kirim ke API menggunakan DataFetchListener
        ApiHelper.updateData(
                ActivityUpdate.this,
                id, // 1. ID Pelanggaran
                namaSiswaBaru, // 2. Nama Siswa (Data baru)
                kelasSiswaLama, // 3. Kelas Siswa (Data lama dari Intent)
                jenisPelanggaran, // 4. Jenis Pelanggaran (Dari Spinner)
                poinBaruString, // 5. Poin (String)
                tanggalSiswa, // 6. Tanggal
                new ApiHelper.DataFetchListener<String>() {
                    @Override
                    public void onSuccess(String response) {
                        Log.d("UpdateActivity", "Update Berhasil: " + response);
                        Toast.makeText(ActivityUpdate.this, "Data Berhasil Diupdate", Toast.LENGTH_SHORT).show();
                        // Kembali ke Activity Utama
                        Intent intent = new Intent(ActivityUpdate.this, ActivityUtama.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        // Jika ada VolleyError, Toast ini akan muncul
                        String errorMessage = error.getMessage() != null ? error.getMessage() : "Kesalahan koneksi atau server.";
                        Toast.makeText(ActivityUpdate.this, "Gagal Mengupdate Data: " + errorMessage, Toast.LENGTH_LONG).show();
                        Log.e("UpdateActivity", "Gagal Update: " + errorMessage, error);
                    }
                }
        );
    }
    // --- AKHIR FUNGSI UPDATE DATA ---

    // Fungsi hitung poin otomatis
    private int hitungPoin(String jenisPelanggaran) {
        // Logika penghitungan poin
        switch (jenisPelanggaran.toLowerCase()) {
            case "bolos":
                return 70;
            case "merokok":
                return 60;
            case "membuat kekacauan":
                return 50;
            case "merusak fasilitas":
                return 40;
            case "tawuran":
                return 100;
            case "berkelahi":
                return 30;
            case "bullying":
                return 50;
            case "asusila":
                return 80;
            default:
                return 0;
        }
    }

    // --- FUNGSI DELETE DATA ---
    private void deleteData() {
        if (id == null || id.isEmpty()) {
            Toast.makeText(this, "Kesalahan sistem: ID data tidak ditemukan untuk dihapus.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiHelper.deleteData(
                ActivityUpdate.this,
                id,
                new ApiHelper.DataFetchListener<String>() {
                    @Override
                    public void onSuccess(String response) {
                        Toast.makeText(ActivityUpdate.this, "Data Berhasil Dihapus", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ActivityUpdate.this, ActivityUtama.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(VolleyError error) {
                        String errorMessage = error.getMessage() != null ? error.getMessage() : "Kesalahan koneksi atau server.";
                        Toast.makeText(ActivityUpdate.this, "Gagal Menghapus Data: " + errorMessage, Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
                }
        );
    }
    // --- AKHIR FUNGSI DELETE DATA ---

    void konfirmasi() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hapus data");
        builder.setMessage("Yakin ingin menghapus data untuk " + nama_siswa_data + " kelas " + kelas_siswa_data + " ?");
        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteData();
            }
        });
        builder.setNegativeButton("Tidak", null);
        builder.create().show();
    }
}
