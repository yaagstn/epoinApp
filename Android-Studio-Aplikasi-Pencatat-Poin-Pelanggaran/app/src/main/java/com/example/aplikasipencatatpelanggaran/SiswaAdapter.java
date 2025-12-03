package com.example.aplikasipencatatpelanggaran;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import java.util.ArrayList;
import java.util.List;

public class SiswaAdapter extends ArrayAdapter<Siswa> implements Filterable {

    private final List<Siswa> originalList;
    private List<Siswa> suggestions;
    private final String selectedAngkatan; // Angkatan yang dipilih dari Spinner

    // Constructor
    public SiswaAdapter(Context context, List<Siswa> siswaList, String angkatan) {
        // Menggunakan android.R.layout.simple_dropdown_item_1line untuk tampilan default
        super(context, android.R.layout.simple_dropdown_item_1line, siswaList);
        this.originalList = new ArrayList<>(siswaList);
        this.suggestions = new ArrayList<>(siswaList);
        this.selectedAngkatan = angkatan;
    }

    // Metode ini dipanggil saat AutoCompleteTextView ingin mendapatkan saran
    @Override
    public int getCount() {
        return suggestions.size();
    }

    @Override
    public Siswa getItem(int position) {
        return suggestions.get(position);
    }

    // Mendapatkan Filter Kustom
    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    private final Filter nameFilter = new Filter() {

        // Logika filtering (di background thread)
        @Override
        public String convertResultToString(Object resultValue) {
            // Mengatur apa yang akan muncul di EditText setelah dipilih
            // Defaultnya menggunakan hasil dari toString() di Class Siswa
            return ((Siswa) resultValue).getNamaSiswa();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();

            if (constraint != null) {
                List<Siswa> tempSuggestions = new ArrayList<>();
                String filterPattern = constraint.toString().toLowerCase().trim();

                // Iterasi melalui daftar SISWA DARI ANGKATAN YANG SUDAH DIPILIH
                // originalList saat ini sudah berisi daftar siswa hanya untuk angkatan tertentu
                for (Siswa siswa : originalList) {

                    // Kriteria 1: Cocokkan Angkatan (Filter dari Spinner)
                    boolean angkatanMatch = siswa.getKelasSiswa().equals(selectedAngkatan);

                    // Kriteria 2: Cocokkan Nama Siswa dengan input teks (misal 'H')
                    boolean nameMatch = siswa.getNamaSiswa().toLowerCase().contains(filterPattern);

                    if (angkatanMatch && nameMatch) {
                        tempSuggestions.add(siswa);
                    }
                }

                filterResults.values = tempSuggestions;
                filterResults.count = tempSuggestions.size();
            } else {
                // Jika constraint (teks input) kosong, tampilkan semua siswa di angkatan tersebut
                filterResults.values = originalList;
                filterResults.count = originalList.size();
            }
            return filterResults;
        }

        // Memublikasikan hasil filtering (di UI thread)
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            suggestions.clear();
            if (results != null && results.count > 0) {
                // Menambahkan hasil filtering yang berupa List<Siswa> ke daftar suggestions
                suggestions.addAll((List<Siswa>) results.values);
            }
            notifyDataSetChanged();
        }
    };
}