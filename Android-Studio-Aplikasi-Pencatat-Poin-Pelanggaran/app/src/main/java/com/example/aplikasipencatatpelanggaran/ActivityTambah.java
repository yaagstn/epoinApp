package com.example.aplikasipencatatpelanggaran;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Catatan: Anda harus memastikan kelas 'Siswa' dan 'SiswaAdapter' sudah tersedia
// di package com.example.aplikasipencatatpelanggaran.

public class ActivityTambah extends AppCompatActivity {

    private static final String TAG = "ActivityTambah";

    Spinner spn_kelas;
    AutoCompleteTextView edit_nama_siswa;
    Spinner spn_jenis_pelanggaran;
    TextView txt_poin_pelanggaran;
    EditText edit_tanggal;
    Button btn_tambah;

    // 🚨 PASTIKAN IP INI BENAR DAN SESUAI DENGAN FUNGSI getdatasiswa.php
    private static final String BASE_URL = "http://10.205.139.129/datapelanggaran/";
    private static final String URL_INSERT = BASE_URL + "insert.php";
    private static final String URL_GET_SISWA_BY_CLASS = BASE_URL + "getdatasiswa.php";

    // Kunci: Nama Siswa (misal: "Andi"), Nilai: Map<String, String> { "id": "S001", "kelas": "X IPA 1" }
    // Digunakan sebagai fallback jika user mengetik manual (tidak memilih dari dropdown)
    private Map<String, Map<String, String>> mapNamaToData = new HashMap<>();

    // ⭐️ BARU: List untuk menyimpan objek Siswa yang akan diumpankan ke SiswaAdapter
    private List<Siswa> currentAngkatanSiswaList = new ArrayList<>();

    // Daftar jenis pelanggaran dan poin otomatis
    private final Map<String, Integer> poinMap = new HashMap<>();

    // Temporary storage untuk menyimpan ID dan Kelas dari objek Siswa yang dipilih/divalidasi
    private String validatedSiswaId = null;
    private String validatedSiswaClass = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah);

        // 🔹 1. Inisialisasi Map Pelanggaran
        poinMap.put("Bolos", 70);
        poinMap.put("Merokok", 60);
        poinMap.put("Membuat Kekacauan", 50);
        poinMap.put("Merusak Fasilitas", 40);
        poinMap.put("Tawuran", 100);
        poinMap.put("Berkelahi", 30);
        poinMap.put("Bullying", 50);
        poinMap.put("Asusila", 95);

        // 🔹 2. Inisialisasi View
        spn_kelas = findViewById(R.id.spnKelas);
        edit_nama_siswa = findViewById(R.id.nama);
        spn_jenis_pelanggaran = findViewById(R.id.spnPoin);
        txt_poin_pelanggaran = findViewById(R.id.txtPoinPelanggaran);
        edit_tanggal = findViewById(R.id.tanggal);
        btn_tambah = findViewById(R.id.btnTambah);

        // 🔹 3. Setup Spinner Kelas (LOGIKA UTAMA FILTERING)
        setupKelasSpinnerForFiltering();

        // 🔹 4. Setup Spinner Jenis Pelanggaran
        setupJenisPelanggaranSpinner();

        // ⭐️ PENTING: Setup Listener untuk AutoCompleteTextView agar ID siswa terambil saat dipilih
        setupAutoCompleteTextViewListener();

        // 5. Bottom Navigation
        setupBottomNavigation();

        // 6. Kalender tanggal
        edit_tanggal.setOnClickListener(v -> showDatePickerDialog());

        // 🟢 Menghubungkan tombol Tambah ke method tambahData
        btn_tambah.setOnClickListener(this::tambahData);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setSelectedItemId(R.id.tambah);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.main) {
                startActivity(new Intent(getApplicationContext(), ActivityUtama.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.tambah) {
                return true;
            } else if (itemId == R.id.list) {
                startActivity(new Intent(getApplicationContext(), ActivityDaftarKelas.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    /**
     * Menambahkan listener agar ketika user memilih nama dari dropdown,
     * informasi ID siswa yang dipilih langsung disimpan dari objek Siswa.
     */
    private void setupAutoCompleteTextViewListener() {
        // Ketika item dipilih dari daftar saran
        edit_nama_siswa.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Objek yang dipilih adalah Siswa karena kita menggunakan SiswaAdapter
                Siswa selectedSiswa = (Siswa) parent.getItemAtPosition(position);

                // Simpan data Siswa yang valid ke variabel temporary
                validatedSiswaId = selectedSiswa.getIdSiswa();
                validatedSiswaClass = selectedSiswa.getKelasSiswa();

                Log.d(TAG, "Siswa dipilih dari dropdown: ID=" + validatedSiswaId + ", Kelas=" + validatedSiswaClass);

                // Tampilkan nama siswa yang dipilih di EditText
                edit_nama_siswa.setText(selectedSiswa.getNamaSiswa());
                // Set kursor di akhir teks
                edit_nama_siswa.setSelection(edit_nama_siswa.getText().length());
            }
        });

        // Tambahkan listener ini untuk mengantisipasi jika user mengubah input setelah memilih
        // Ini memastikan ID siswa di-reset jika user mengubah teks
        edit_nama_siswa.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // Setelah focus hilang, cek apakah teksnya cocok dengan salah satu Siswa.
                    // Jika tidak cocok, reset validatedSiswaId
                    String currentText = edit_nama_siswa.getText().toString().trim();
                    if (!mapNamaToData.containsKey(currentText)) {
                        validatedSiswaId = null;
                        validatedSiswaClass = null;
                        Log.d(TAG, "Input nama siswa diubah/tidak valid setelah fokus hilang. ID direset.");
                    }
                }
            }
        });
    }


    private void setupKelasSpinnerForFiltering() {
        final String[] daftarKelas = getResources().getStringArray(R.array.kelas);

        ArrayAdapter<String> adapterKelas = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                daftarKelas);
        spn_kelas.setAdapter(adapterKelas);


        spn_kelas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String kelasTerpilih = parent.getItemAtPosition(position).toString();

                if (position > 0) {
                    // Panggil Volley untuk memuat siswa HANYA dari kelas terpilih
                    loadDataSiswaByKelas(kelasTerpilih);
                    // Reset nama siswa saat kelas berubah
                    edit_nama_siswa.setText("");
                    validatedSiswaId = null;
                    validatedSiswaClass = null;
                } else {
                    // Jika memilih item default/reset
                    mapNamaToData.clear();
                    currentAngkatanSiswaList.clear();
                    edit_nama_siswa.setAdapter(null);
                    edit_nama_siswa.setText("");
                    validatedSiswaId = null;
                    validatedSiswaClass = null;
                    Toast.makeText(ActivityTambah.this, "Pilih angkatan terlebih dahulu.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupJenisPelanggaranSpinner() {
        List<String> jenisPelanggaranList = new ArrayList<>(poinMap.keySet());
        jenisPelanggaranList.add(0, "Pilih Jenis Pelanggaran");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                jenisPelanggaranList
        );
        spn_jenis_pelanggaran.setAdapter(spinnerAdapter);

        spn_jenis_pelanggaran.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (position > 0) {
                    Integer poin = poinMap.get(selected);
                    txt_poin_pelanggaran.setText(String.valueOf(poin != null ? poin : 0));
                } else {
                    txt_poin_pelanggaran.setText("0");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                txt_poin_pelanggaran.setText("0");
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    calendar.set(year, monthOfYear, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    edit_tanggal.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }


    /**
     * Memuat data siswa HANYA berdasarkan kelas yang dipilih dari server (Volley).
     * Data disimpan dalam List<Siswa> dan SiswaAdapter di-set.
     *
     * @param selectedClass Kelas yang dipilih dari Spinner.
     */
    private void loadDataSiswaByKelas(final String selectedClass) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_GET_SISWA_BY_CLASS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Bersihkan list dan map lama
                        mapNamaToData.clear();
                        currentAngkatanSiswaList.clear();

                        Log.d(TAG, "Raw Response Siswa by Class: " + response);

                        try {
                            JSONArray jsonArray = new JSONArray(response);

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);
                                String id = obj.getString("id_siswa");
                                String nama = obj.getString("nama_siswa");
                                String kelas = obj.getString("kelas_siswa");

                                // ⭐️ 1. Buat Objek Siswa (PENTING untuk SiswaAdapter)
                                Siswa siswa = new Siswa(id, nama, kelas);
                                currentAngkatanSiswaList.add(siswa);

                                // 2. Isi mapNamaToData (untuk fallback validasi)
                                Map<String, String> data = new HashMap<>();
                                data.put("id", id);
                                data.put("kelas", kelas);
                                mapNamaToData.put(nama, data);
                            }

                            // ⭐️ 3. Set SiswaAdapter (menggunakan List<Siswa>)
                            // Asumsikan SiswaAdapter sudah dibuat dan diimport
                            SiswaAdapter adapter = new SiswaAdapter(
                                    ActivityTambah.this,
                                    currentAngkatanSiswaList, // Umpan List<Siswa>
                                    selectedClass // Umpan Angkatan
                            );
                            edit_nama_siswa.setThreshold(1);
                            edit_nama_siswa.setAdapter(adapter);

                            Toast.makeText(ActivityTambah.this, "Siswa kelas " + selectedClass + " dimuat: " + mapNamaToData.size() + " siswa.", Toast.LENGTH_SHORT).show();

                        } catch (JSONException e) {
                            String errorMsg = "Gagal parsing JSON data siswa. Periksa Raw Response di Logcat untuk memastikan JSON valid.";
                            Toast.makeText(ActivityTambah.this, "Gagal memuat daftar siswa: " + errorMsg, Toast.LENGTH_LONG).show();
                            Log.e(TAG, errorMsg + " Error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMsg = "Koneksi ke server gagal saat memuat siswa kelas " + selectedClass + " (URL: " + URL_GET_SISWA_BY_CLASS + ")";
                        Toast.makeText(ActivityTambah.this, errorMsg, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Volley Error Get Siswa By Class: " + error.toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("kelas", selectedClass);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }


    /**
     * Mencari ID dan Kelas Siswa berdasarkan nama yang diketik (digunakan jika user TIDAK memilih dari dropdown).
     */
    private Map<String, String> getSiswaIdAndClass(String typedName) {
        String inputNormalized = typedName.trim();

        // Cari di map yang berisi data siswa yang sudah dimuat
        for (Map.Entry<String, Map<String, String>> entry : mapNamaToData.entrySet()) {
            String namaInMap = entry.getKey().trim();

            if (namaInMap.equalsIgnoreCase(inputNormalized)) {
                Log.d(TAG, "Match Ditemukan di mapNamaToData. Menggunakan data Siswa: " + namaInMap);
                return entry.getValue();
            }
        }

        Log.d(TAG, "Pencarian Siswa Gagal untuk input: " + inputNormalized);
        return null;
    }


    // === Tambah Data (Action) ===
    public void tambahData(View v) {

        // 1. Ambil data
        final String kelasSelected = spn_kelas.getSelectedItem().toString();
        final String inputNamaTyped = edit_nama_siswa.getText().toString().trim();
        final String jenis = spn_jenis_pelanggaran.getSelectedItem().toString();
        final String poin = txt_poin_pelanggaran.getText().toString().trim();
        final String tanggal = edit_tanggal.getText().toString().trim();

        // 2. Validasi Input Dasar (Kelas, Nama Siswa dan Tanggal)
        if (kelasSelected.equals(getResources().getStringArray(R.array.kelas)[0]) ||
                inputNamaTyped.isEmpty() ||
                tanggal.isEmpty()) {
            Toast.makeText(this, "Kelas, Nama Siswa, dan Tanggal wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Validasi Jenis Pelanggaran
        if (jenis.equals("Pilih Jenis Pelanggaran") || poin.equals("0")) {
            Toast.makeText(this, "Harap pilih Jenis Pelanggaran.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Validasi ID Siswa: Jika ID belum tersimpan dari onItemClick (berarti user mengetik manual)
        if (validatedSiswaId == null) {
            Map<String, String> siswaData = getSiswaIdAndClass(inputNamaTyped);

            if (siswaData != null) {
                // SISWA DITEMUKAN (berdasarkan nama yang diketik)
                validatedSiswaId = siswaData.get("id");
                validatedSiswaClass = siswaData.get("kelas");
                Log.d(TAG, "ID Siswa didapatkan via lookup manual.");
            } else {
                // SISWA TIDAK DITEMUKAN
                Toast.makeText(this, "Nama siswa '" + inputNamaTyped + "' tidak ditemukan di kelas " + kelasSelected + ". Harap pilih dari saran.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        // 5. Pastikan data ID dan Kelas ada setelah semua validasi
        if (validatedSiswaId == null || validatedSiswaClass == null) {
            Toast.makeText(this, "Kesalahan validasi data siswa. Coba pilih ulang dari daftar.", Toast.LENGTH_LONG).show();
            return;
        }

        // 6. Validasi akhir: Pastikan kelas di Spinner sama dengan kelas di DB
        if (!validatedSiswaClass.equalsIgnoreCase(kelasSelected)) {
            Toast.makeText(this, "Kesalahan data: Siswa ini terdaftar di kelas " + validatedSiswaClass + " di database. Pastikan input kelas sudah benar.", Toast.LENGTH_LONG).show();
            return;
        }


        // 7. Data yang akan dikirim ke server
        final String kelasToSend = validatedSiswaClass;
        final String namaToSend = inputNamaTyped; // Nama yang diketik/dipilih

        // Jika kode mencapai sini, semua validasi berhasil dan data siap dikirim.
        Log.d(TAG, "Data Siap Kirim: ID=" + validatedSiswaId + ", Nama=" + namaToSend + ", Kelas=" + kelasToSend + ", Jenis=" + jenis + ", Poin=" + poin + ", Tgl=" + tanggal);


        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_INSERT,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");

                        if (status.equals("success")) {
                            Toast.makeText(this, "Berhasil: " + message, Toast.LENGTH_SHORT).show();

                            // Reset state dan navigasi
                            edit_nama_siswa.setText("");
                            spn_kelas.setSelection(0);
                            spn_jenis_pelanggaran.setSelection(0);
                            txt_poin_pelanggaran.setText("0");
                            edit_tanggal.setText("");
                            validatedSiswaId = null;
                            validatedSiswaClass = null;
                            currentAngkatanSiswaList.clear();

                            Intent intent = new Intent(ActivityTambah.this, ActivityUtama.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Gagal menambah data! Pesan: " + message, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Server Error: " + message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error parsing JSON response: " + e.getMessage());
                        Log.e(TAG, "Raw Response (max 500 chars): " + response.substring(0, Math.min(response.length(), 500)));
                        Toast.makeText(this, "Kesalahan Server: Gagal memproses respon (bukan format JSON). Cek Logcat 'Raw Response'.", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Volley Error Insert: " + error.toString());
                    Toast.makeText(this, "Koneksi error: Server tidak terjangkau (URL: " + URL_INSERT + ")", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("id_siswa", validatedSiswaId);
                params.put("nama_siswa", namaToSend);
                params.put("kelas", kelasToSend);
                params.put("jenis_pelanggaran", jenis);
                params.put("poin", poin);
                params.put("tanggal_pelanggaran", tanggal);

                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }
}