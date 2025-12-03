package com.example.aplikasipencatatpelanggaran;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    //inisialisasi
    private Context context;
    Activity activity;
    // Asumsi: id, data_id, data_nama, data_kelas, data_poin
    private ArrayList id, data_id, data_nama, data_kelas, data_poin;
    // Tambahkan variabel untuk data yang hilang (Jenis Pelanggaran String dan Tanggal)
    // Jika Anda sudah memiliki array data untuk Jenis Pelanggaran dan Tanggal di ActivityUtama,
    // Anda harus menambahkan array tersebut ke konstruktor di bawah.

    Animation translate_anim;
    int position;

    // constructor customadapter
    // CATATAN: Idealnya, Anda harus menambahkan ArrayList untuk Jenis Pelanggaran (string) dan Tanggal di sini.
    CustomAdapter(Activity activity, Context context, ArrayList id , ArrayList data_id, ArrayList data_nama, ArrayList data_kelas, ArrayList data_poin){

        // mengisi variabel
        this.activity = activity;
        this.context = context;
        this.id = id; // ID Asli DB
        this.data_id = data_id; // ID tampilan
        this.data_nama = data_nama; // ASUMSI: Ini berisi Jenis Pelanggaran (string) atau Nama Siswa
        this.data_kelas = data_kelas;
        this.data_poin = data_poin;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row,parent,false);
        return new MyViewHolder(view);
    }

    // method untuk menegeset isi text dan jika recyclerview ditekan
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.data_id_txt.setText(String.valueOf(data_id.get(position)));
        holder.data_nama_txt.setText(String.valueOf(data_nama.get(position)));
        holder.data_kelas_txt.setText(String.valueOf(data_kelas.get(position)));
        holder.data_poin_txt.setText(String.valueOf(data_poin.get(position)));

        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ActivityUpdate.class);

                // --- PERBAIKAN KUNCI INTENT AGAR SESUAI DENGAN ActivityUpdate.java ---

                // 1. Kunci ID Asli
                intent.putExtra("id_asli", String.valueOf(id.get(position)));

                // 2. Kunci Kelas (sesuai)
                intent.putExtra("kelas", String.valueOf(data_kelas.get(position)));

                // 3. Kunci Poin (sesuai)
                intent.putExtra("poin", String.valueOf(data_poin.get(position)));

                // 4. MAPPING KUNCI: data_nama (Nama Siswa) DIASUMSIKAN MEMBAWA JENIS PELANGGARAN
                // Kunci 'jenis_pelanggaran' HARUS ada agar ActivityUpdate tidak error.
                // Jika data_nama berisi NAMA SISWA, maka Jenis Pelanggaran tidak terkirim.
                // ASUMSI: data_nama = Jenis Pelanggaran string
                intent.putExtra("jenis_pelanggaran", String.valueOf(data_nama.get(position)));

                // 5. DATA TANGGAL: Tidak ada di array Anda. Mengirim string kosong/default
                // Kunci 'tanggal' HARUS ada agar ActivityUpdate tidak error.
                // *PENTING: Anda harus memperbarui ActivityUtama untuk mengambil data Tanggal yang benar.*
                intent.putExtra("tanggal", "01/01/2024"); // Mengirim nilai default sementara

                // Data lain yang tidak digunakan untuk pengisian EditText (tapi mungkin diperlukan)
                intent.putExtra("id", String.valueOf(data_id.get(position)));

                activity.startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data_id.size();
    }

    // untuk menampilkan recyclerview dengan isi data dari database
    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView data_id_txt, data_nama_txt, data_kelas_txt, data_poin_txt;
        ConstraintLayout mainLayout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            data_id_txt = itemView.findViewById(R.id.data_id_txt);
            data_nama_txt = itemView.findViewById(R.id.data_nama_txt);
            data_kelas_txt = itemView.findViewById(R.id.data_kelas_txt);
            data_poin_txt = itemView.findViewById(R.id.poin_txt);
            mainLayout = itemView.findViewById(R.id.mainLayout);
            translate_anim = AnimationUtils.loadAnimation(context, R.anim.translate_anim);
            mainLayout.setAnimation(translate_anim);
        }
    }
}
