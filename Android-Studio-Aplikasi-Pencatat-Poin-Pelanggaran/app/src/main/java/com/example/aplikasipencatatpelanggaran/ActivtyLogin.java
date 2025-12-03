package com.example.aplikasipencatatpelanggaran;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView; // IMPORT DITAMBAHKAN
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

// MENGGUNAKAN NAMA KELAS ActivtyLogin
public class ActivtyLogin extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;

    // PERBAIKAN 1: Deklarasi TextView untuk tautan daftar
    private TextView tvLinkRegister;

    // PERBAIKAN: Menggunakan nama folder/database yang benar: data_pelanggaran
    private static final String BASE_URL = "http://10.205.139.129/datapelanggaran/";
    private static final String URL_LOGIN = "http://10.205.139.129/datapelanggaran/login.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Pastikan R.layout.activity_activty_login adalah file XML layout yang benar
        setContentView(R.layout.activity_activty_login);

        // 1. Hubungkan komponen utama
        etUsername = findViewById(R.id.editTextTextEmailAddress);
        etPassword = findViewById(R.id.editTextTextPassword);
        btnLogin = findViewById(R.id.button);

        // PERBAIKAN 2: Inisialisasi TextView untuk tautan daftar
        tvLinkRegister = findViewById(R.id.RegisterActivity);

        // 2. Tambahkan Listener pada Tombol LOGIN
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifikasiLogin();
            }
        });

        // PERBAIKAN 3: Tambahkan OnClickListener untuk TextView Daftar
        tvLinkRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pindah ke Activity Pendaftaran
                pindahKeDaftar();
            }
        });
    }

    /**
     * Method untuk memproses verifikasi login dengan mengirim data ke API PHP/MySQL.
     */
    private void verifikasiLogin() {
        final String email = etUsername.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        // Cek 1: Validasi Input Kosong
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(ActivtyLogin.this, "Email dan Password harus diisi!", Toast.LENGTH_SHORT).show();
            return;
        }



        // Cek 3: KIRIM DATA KE SERVER VIA VOLLEY
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Server (PHP) akan memproses data dan membalas status
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");

                            if (status.equals("success")) {
                                // Login Berhasil
                                Toast.makeText(ActivtyLogin.this, "Login Berhasil: " + message, Toast.LENGTH_SHORT).show();
                                pindahKeUtama();
                            } else {
                                // Login Gagal (password salah atau email tidak ditemukan)
                                Toast.makeText(ActivtyLogin.this, "Login Gagal: " + message, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Log.e("ActivtyLogin", "JSON Error: " + response, e);
                            Toast.makeText(ActivtyLogin.this, "Error: Format balasan server tidak valid. Respons: " + response, Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Error koneksi, timeout, atau server tidak merespons
                        Log.e("ActivtyLogin", "Volley Error: ", error);
                        // Pesan yang lebih spesifik untuk membantu debugging
                        Toast.makeText(ActivtyLogin.this, "Koneksi Gagal. Cek URL: " + URL_LOGIN, Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Mengirim parameter email dan password ke script PHP
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        // Tambahkan permintaan ke antrian Volley
        Volley.newRequestQueue(ActivtyLogin.this).add(stringRequest);
    }


    /**
     * Metode pembantu untuk navigasi ke ActivityUtama.
     */
    private void pindahKeUtama() {
        Intent intent = new Intent(ActivtyLogin.this, ActivityUtama.class);
        // Flag ini menutup semua activity sebelumnya
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void pindahKeDaftar() {
        // Ganti 'RegisterActivity.class' dengan nama kelas Activity pendaftaran Anda yang sebenarnya
        Intent intent = new Intent(ActivtyLogin.this, RegisterActivity.class);
        startActivity(intent);
    }


}
