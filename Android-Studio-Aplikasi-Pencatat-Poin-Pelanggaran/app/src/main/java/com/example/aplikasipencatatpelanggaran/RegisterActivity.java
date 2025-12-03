package com.example.aplikasipencatatpelanggaran;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import com.google.android.material.textfield.TextInputEditText;

// Import API dan Model Anda (tetap ada)
// import com.example.aplikasipencatatpelanggaran.api.ApiClient;
// import com.example.aplikasipencatatpelanggaran.api.ApiInterface;
// import com.example.aplikasipencatatpelanggaran.model.ResponseModel;
// import retrofit2.Call;
// import retrofit2.Callback;
// import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    // Deklarasi Variabel (Memperbaiki error 'Cannot resolve symbol')
    private TextInputEditText etNip;
    private TextInputEditText etNamaLengkap;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private TextInputEditText etKonfirmasiPassword;
    private Button btnRegister;
    private TextView tvLinkLogin;
    private ProgressBar pbRegisterLoading;
    private static final String BASE_URL = "http://10.205.139.129/datapelanggaran/";
    private static final String URL_REGISTER = BASE_URL + "register.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        pbRegisterLoading = findViewById(R.id.pb_register_loading);
        pbRegisterLoading.setVisibility(View.GONE);


        // 1. Inisialisasi komponen UI (Bagian ini sudah benar)
        etNip = findViewById(R.id.et_nip);
        etNamaLengkap = findViewById(R.id.et_nama);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etKonfirmasiPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLinkLogin = findViewById(R.id.tv_link_login);

        // Asumsi Anda punya ID ini di XML. Jika tidak ada, hapus baris ini.
        // Anda perlu menambahkan inisialisasi ProgressBar jika ada di layout Anda.
        // pbRegisterLoading = findViewById(R.id.pb_register_loading);
        // if (pbRegisterLoading != null) {
        //     pbRegisterLoading.setVisibility(View.GONE);
        // }


        // 2. MENAMBAHKAN LOGIKA KLIK (PENTING!)

        // A. Logika untuk Tombol Register
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Panggil fungsi yang menangani proses registrasi
                registerUser();
            }
        });

        // B. Logika untuk Teks Link ke Halaman Login
        tvLinkLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pindah (Intent) ke ActivityLogin
                Intent intent = new Intent(RegisterActivity.this, ActivtyLogin.class);
                startActivity(intent);
                finish(); // Tutup halaman Register agar tidak bisa kembali dengan tombol back
            }
        });
    }

    // Fungsi placeholder untuk menampung logika registrasi
    private void registerUser() {

        String nip = etNip.getText().toString().trim();
        String nama = etNamaLengkap.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String konfirmasiPassword = etKonfirmasiPassword.getText().toString().trim();

        // 1. Validasi Input Dasar
        if (nip.isEmpty() || nama.isEmpty() || username.isEmpty() || password.isEmpty() || konfirmasiPassword.isEmpty()) {
            Toast.makeText(this, "Semua field wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(konfirmasiPassword)) {
            Toast.makeText(this, "Konfirmasi password tidak cocok!", Toast.LENGTH_SHORT).show();
            return;
        }

        pbRegisterLoading.setVisibility(View.VISIBLE);

        // 🔹 Kirim data ke register.php (Volley)
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                URL_REGISTER,
                response -> {
                    pbRegisterLoading.setVisibility(View.GONE);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");

                        if (status.equals("success")) {
                            Toast.makeText(this, "✅ " + message, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(RegisterActivity.this, ActivtyLogin.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "❌ " + message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    pbRegisterLoading.setVisibility(View.GONE);
                    String errorMsg = error.toString();
                    if (error.networkResponse != null) {
                        errorMsg += " | Code: " + error.networkResponse.statusCode;
                    }
                    Toast.makeText(this, "Koneksi gagal: " + errorMsg, Toast.LENGTH_LONG).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("nip", nip);
                params.put("username", username);
                params.put("password", password);
                params.put("nama_lengkap", nama);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);


        // 2. Tampilkan Loading (jika ada ProgressBar)
        // if (pbRegisterLoading != null) {
        //     pbRegisterLoading.setVisibility(View.VISIBLE);
        // }

        // 3. Panggil API Registrasi (Anda harus mengisi bagian ini)
        // Contoh:
        // ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        // Call<ResponseModel> call = api.registerResponse(nip, nama, username, password);
        // call.enqueue(new Callback<ResponseModel>() {
        //     // ... implementasi onResponse dan onFailure
        // });


    }
}
