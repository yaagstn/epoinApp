package com.example.aplikasipencatatpelanggaran;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public class PelanggaranSiswaAdapter extends RecyclerView.Adapter<PelanggaranSiswaAdapter.ViewHolder> {

    private final Context context;
    private final List<SiswaPelanggaran> dataList;

    public PelanggaranSiswaAdapter(Context context, List<SiswaPelanggaran> dataList) {
        this.context = context;
        this.dataList = dataList;

        // Urutkan data berdasarkan nama siswa
        Collections.sort(this.dataList, (a, b) ->
                a.getNamaSiswa().trim().compareToIgnoreCase(b.getNamaSiswa().trim()));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.my_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SiswaPelanggaran data = dataList.get(position);

        // Reset tampilan
        holder.txtTotalPoin.setVisibility(View.GONE);
        holder.txtTotalPoin.setText("");

        holder.txtNamaSiswa.setText(data.getNamaSiswa());
        holder.txtTanggal.setText(data.getTanggal());

        if (data.hasPelanggaran()) {
            holder.txtJenisPelanggaran.setText("Jenis Pelanggaran: " + data.getJenisPelanggaran());
            holder.txtPoin.setText(String.valueOf(data.getPoin()));
            holder.txtPoin.setVisibility(View.VISIBLE);
        } else {
            holder.txtJenisPelanggaran.setText("Tidak ada pelanggaran");
            holder.txtPoin.setVisibility(View.GONE);
        }

        // --- LOGIKA TOTAL ---
        String namaSekarang = data.getNamaSiswa().trim();
        String namaBerikutnya = (position < dataList.size() - 1)
                ? dataList.get(position + 1).getNamaSiswa().trim()
                : "";

        boolean isBarisTerakhirUntukSiswa = !namaSekarang.equalsIgnoreCase(namaBerikutnya);

        if (isBarisTerakhirUntukSiswa || dataList.size() == 1) {
            int total = hitungTotalPoinUntukSiswa(namaSekarang);
            holder.txtTotalPoin.setVisibility(View.VISIBLE);
            holder.txtTotalPoin.setText("Total Poin: " + total);
            Log.d("DEBUG_TOTAL", "Total poin " + namaSekarang + ": " + total);
        }
    }

    private int hitungTotalPoinUntukSiswa(String namaSiswa) {
        int total = 0;
        for (SiswaPelanggaran s : dataList) {
            if (s.getNamaSiswa().trim().equalsIgnoreCase(namaSiswa.trim())) {
                total += s.getPoin();
            }
        }
        return total;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNamaSiswa, txtJenisPelanggaran, txtTanggal, txtPoin, txtTotalPoin;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNamaSiswa = itemView.findViewById(R.id.txtNamaSiswa);
            txtJenisPelanggaran = itemView.findViewById(R.id.txtJenisPelanggaran);
            txtTanggal = itemView.findViewById(R.id.txtTanggal);
            txtPoin = itemView.findViewById(R.id.txtPoin);
            txtTotalPoin = itemView.findViewById(R.id.txtTotalPoin);
        }
    }
}
