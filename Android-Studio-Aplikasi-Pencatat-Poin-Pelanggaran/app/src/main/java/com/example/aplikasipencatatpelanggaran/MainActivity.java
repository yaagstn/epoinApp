package com.example.aplikasipencatatpelanggaran;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Pastikan R.layout.activity_main adalah layout untuk halaman landing yang ada tombol "Masuk"
        setContentView(R.layout.activity_main);
    }

    /**
     * Method ini dipanggil saat tombol "Masuk" diklik (menggunakan atribut android:onClick di XML).
     * Fungsi diubah untuk pindah ke halaman LoginActivity.
     */
    public void tampilActivityUtama(View v){
        // MENDAPATKAN: Intent sekarang diarahkan ke LoginActivity.class
        Intent in = new Intent(this, ActivtyLogin.class);
        startActivity(in);

        // Opsional: Gunakan finish() jika Anda tidak ingin pengguna bisa
        // kembali ke halaman landing (splash screen) dengan tombol back.
        // finish();
    }
}
