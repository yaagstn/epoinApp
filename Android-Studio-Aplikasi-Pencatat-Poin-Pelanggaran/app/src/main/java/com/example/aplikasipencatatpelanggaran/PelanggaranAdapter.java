package com.example.aplikasipencatatpelanggaran;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PelanggaranAdapter extends RecyclerView.Adapter<PelanggaranAdapter.MyViewHolder> {

    private final Context context;
    private final List<PelanggaranModel> dataList;

    public PelanggaranAdapter(Context context, List<PelanggaranModel> dataList) {
        this.context = context;
        this.dataList = dataList;

        // Urutkan agar data dengan nama siswa yang sama berkelompok
        Collections.sort(this.dataList, Comparator.comparing(
                PelanggaranModel::getNamaSiswa, String.CASE_INSENSITIVE_ORDER));
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.my_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        PelanggaranModel currentItem = dataList.get(position);

        holder.tvID.setText(currentItem.getId());
        holder.tvNamaSiswa.setText(currentItem.getNamaSiswa());
        holder.tvKelasSiswa.setText("Kelas: " + currentItem.getKelasSiswa());
        holder.tvJenisPelanggaran.setText("Jenis Pelanggaran: " + currentItem.getJenisPelanggaran());
        holder.tvTanggal.setText(currentItem.getTanggal());
        holder.tvPoin.setText(String.valueOf(currentItem.getPoin()));

        // --- Reset dulu biar tidak ketimpa ViewHolder lain ---
        holder.tvTotalPoin.setVisibility(View.GONE);
        holder.tvTotalPoin.setText("");

        // --- Cek apakah ini baris terakhir untuk siswa ini ---
        String namaSekarang = currentItem.getNamaSiswa().trim();
        String namaBerikutnya = (position < dataList.size() - 1)
                ? dataList.get(position + 1).getNamaSiswa().trim()
                : "";

        if (!namaSekarang.equalsIgnoreCase(namaBerikutnya)) {
            int total = hitungTotalPoinUntukSiswa(namaSekarang);
            holder.tvTotalPoin.setVisibility(View.VISIBLE);
            holder.tvTotalPoin.setText("Total Poin: " + total);
            Log.d("DEBUG_TOTAL", "Total poin " + namaSekarang + ": " + total);
        }

        // --- Klik item untuk update ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ActivityUpdate.class);
            intent.putExtra("id_asli", currentItem.getId());
            intent.putExtra("nama_siswa", currentItem.getNamaSiswa());
            intent.putExtra("kelas", currentItem.getKelasSiswa());
            intent.putExtra("jenis_pelanggaran", currentItem.getJenisPelanggaran());
            intent.putExtra("poin", String.valueOf(currentItem.getPoin()));
            intent.putExtra("tanggal", currentItem.getTanggal());
            context.startActivity(intent);
        });
    }

    // --- Fungsi untuk menghitung total poin per siswa ---
    private int hitungTotalPoinUntukSiswa(String namaSiswa) {
        int total = 0;
        for (PelanggaranModel item : dataList) {
            if (item.getNamaSiswa() != null &&
                    item.getNamaSiswa().trim().equalsIgnoreCase(namaSiswa)) {
                total += item.getPoin();
            }
        }
        return total;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvID, tvNamaSiswa, tvKelasSiswa, tvPoin, tvJenisPelanggaran, tvTanggal, tvTotalPoin;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvID = itemView.findViewById(R.id.data_id_txt);
            tvNamaSiswa = itemView.findViewById(R.id.data_nama_txt);
            tvKelasSiswa = itemView.findViewById(R.id.data_kelas_txt);
            tvPoin = itemView.findViewById(R.id.poin_txt);
            tvJenisPelanggaran = itemView.findViewById(R.id.jenis_pelanggaran_txt);
            tvTanggal = itemView.findViewById(R.id.tanggal_txt);
            tvTotalPoin = itemView.findViewById(R.id.txtTotalPoin);
        }
    }
}
