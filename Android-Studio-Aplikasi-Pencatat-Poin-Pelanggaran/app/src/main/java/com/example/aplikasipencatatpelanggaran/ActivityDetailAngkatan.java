package com.example.aplikasipencatatpelanggaran;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ActivityDetailAngkatan bertanggung jawab menampilkan daftar siswa dari satu angkatan
 * dan menghitung total poin kumulatif SELURUH angkatan tersebut.
 */
public class ActivityDetailAngkatan extends AppCompatActivity {

    private static final String URL_GET_SISWA_BY_ANGKATAN = "http://10.205.139.129/getsiswaangkatan.php";

    // Deklarasi komponen UI
    private TextView txtHeaderAngkatan;
    private TextView txtStatusData;

    // KOREKSI JAVA 1: Deklarasi TextView untuk menampung total poin angkatan.
    private TextView txtTotalPoin;

    private RecyclerView recyclerViewSiswa;
    private PelanggaranSiswaAdapter adapter;
    private List<SiswaPelanggaran> dataSiswaList;

    private RequestQueue requestQueue;
    private String selectedAngkatan;

    public static final String EXTRA_ANGKATAN = "ANGKATAN_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_angkatan);

        // --- INISIALISASI UI DAN VARIABEL ---

        txtHeaderAngkatan = findViewById(R.id.txtHeaderAngkatan);
        txtStatusData = findViewById(R.id.txtStatusData);

        // KOREKSI JAVA 2: Inisialisasi txtTotalPoin menggunakan ID R.id.txtTotalPoin.
        // Asumsi: ID ini ada di layout utama ActivityDetailAngkatan.


        // Setup RecyclerView
        recyclerViewSiswa = findViewById(R.id.recyclerViewSiswa);
        dataSiswaList = new ArrayList<>();
        adapter = new PelanggaranSiswaAdapter(this, dataSiswaList);
        recyclerViewSiswa.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSiswa.setAdapter(adapter);

        // Setup Volley
        requestQueue = Volley.newRequestQueue(this);

        // --- PENGAMBILAN DATA INTENT ---
        if (getIntent().hasExtra(EXTRA_ANGKATAN)) {
            selectedAngkatan = getIntent().getStringExtra(EXTRA_ANGKATAN);
            txtHeaderAngkatan.setText("Daftar Pelanggaran Angkatan " + selectedAngkatan);
        } else {
            selectedAngkatan = "TIDAK DIKETAHUI";
            Toast.makeText(this, "Angkatan tidak ditemukan.", Toast.LENGTH_LONG).show();
        }

        // Mulai mengambil data
        fetchDataSiswaByAngkatan();
    }

    /**
     * Mengambil data siswa (beserta poin pelanggaran) dari server menggunakan Volley (POST Request).
     */
    private void fetchDataSiswaByAngkatan() {
        txtStatusData.setText("Memuat data Angkatan " + selectedAngkatan + "...");
        txtStatusData.setVisibility(View.VISIBLE);
        recyclerViewSiswa.setVisibility(View.GONE);
        // Sembunyikan txtTotalPoin saat loading
        txtTotalPoin.setVisibility(View.GONE);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_GET_SISWA_BY_ANGKATAN,
                response -> {
                    txtStatusData.setVisibility(View.GONE);
                    Log.d("API_Response", response);

                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        if (jsonResponse.getBoolean("success")) {
                            JSONArray jsonArray = jsonResponse.getJSONArray("data");
                            dataSiswaList.clear();

                            int totalPoinKelasKumulatif = 0; // Variabel penghitung total poin

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);

                                String idSiswa = obj.getString("id_siswa");
                                String namaSiswa = obj.getString("nama_siswa");
                                String kelasSiswa = obj.getString("kelas_siswa");
                                String jenisPelanggaran = obj.optString("jenis_pelanggaran", null);
                                int poin = obj.optInt("poin", 0);
                                String tanggal = obj.optString("tanggal", "-");

                                // LOGIKA PENGHITUNGAN TOTAL POIN
                                totalPoinKelasKumulatif += poin;

                                SiswaPelanggaran siswa = new SiswaPelanggaran(
                                        idSiswa, namaSiswa, kelasSiswa, jenisPelanggaran, poin, tanggal
                                );
                                dataSiswaList.add(siswa);
                            }

                            if (dataSiswaList.isEmpty()) {
                                txtStatusData.setText("Tidak ada catatan pelanggaran tercatat untuk angkatan ini.");
                                txtStatusData.setVisibility(View.VISIBLE);
                                txtTotalPoin.setVisibility(View.GONE); // Pastikan tersembunyi jika kosong
                            } else {
                                // KOREKSI JAVA 3: Set teks dan tampilkan txtTotalPoin setelah perhitungan berhasil
                                txtTotalPoin.setText("Total Poin Pelanggaran Angkatan: " + totalPoinKelasKumulatif + " Poin");
                                txtTotalPoin.setVisibility(View.VISIBLE);

                                Collections.sort(dataSiswaList, (a, b) ->
                                        a.getNamaSiswa().compareToIgnoreCase(b.getNamaSiswa())
                                );

                                recyclerViewSiswa.setVisibility(View.VISIBLE);
                                adapter.notifyDataSetChanged();
                            }


                        } else {
                            String message = jsonResponse.optString("message", "Gagal mengambil data dari server.");
                            txtStatusData.setText(message);
                            txtStatusData.setVisibility(View.VISIBLE);
                            txtTotalPoin.setVisibility(View.GONE);
                            Toast.makeText(ActivityDetailAngkatan.this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e("JSON_Error", "Parsing error: " + e.getMessage());
                        txtStatusData.setText("Kesalahan format data (JSON Error). Silakan cek respons server.");
                        txtStatusData.setVisibility(View.VISIBLE);
                        txtTotalPoin.setVisibility(View.GONE);
                        Toast.makeText(ActivityDetailAngkatan.this, "Kesalahan data JSON.", Toast.LENGTH_LONG).show();
                    }
                },

                error -> {
                    Log.e("Volley_Error", "Network error: " + error.getMessage());
                    txtStatusData.setText("Gagal terhubung ke server. Periksa koneksi atau URL.");
                    txtStatusData.setVisibility(View.VISIBLE);
                    txtTotalPoin.setVisibility(View.GONE);
                    Toast.makeText(ActivityDetailAngkatan.this, "Kesalahan Jaringan: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("angkatan", selectedAngkatan);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }
}