package com.example.aplikasipencatatpelanggaran;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ApiHelper {

    private static RequestQueue requestQueue;

    // 🚨 Pastikan IP ini selalu benar
    private static final String URL_HOST = "http://10.205.139.129/datapelanggaran/";

    private static final String URL_READ = URL_HOST + "read.php";
    private static final String URL_INSERT = URL_HOST + "insert.php";
    private static final String URL_UPDATE = URL_HOST + "update.php";
    private static final String URL_DELETE = URL_HOST + "delete.php";
    private static final String REGISTER_URL = URL_HOST + "register.php";

    // URL untuk mengambil data siswa
    private static final String URL_GET_SISWA = URL_HOST + "getsiswaangkatan.php";

    private static final String TAG = "ApiHelperLog";

    // ========================
    // Interface Listener
    // ========================
    public interface DataFetchListener<T> {
        void onSuccess(T result);
        void onError(VolleyError error);
    }

    // Interface baru khusus untuk mengambil daftar siswa (hanya JSONArray)
    public interface SiswaDataListener {
        void onSuccess(JSONArray dataArray);
        void onError(String message);
    }

    // ========================
    // Request Queue Singleton
    // ========================
    private static RequestQueue getRequestQueue(Context context) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public static void getDataByAngkatan(Context context, final String angkatan, final DataFetchListener listener) {
        String url = "http://10.205.139.129/datapelanggaran/getsiswaangkatan.php";
        ;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        // Response harusnya berupa JSON Object { "success": true, "data": [...] }
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            // Mengirimkan array data ke listener
                            listener.onSuccess(jsonResponse.getJSONArray("data"));
                        } else {
                            // Menangani kasus success: false (Misalnya: data kosong atau parameter salah)
                            // Walaupun bukan error Volley, kita tangani sebagai kegagalan
                            listener.onError(new VolleyError(jsonResponse.getString("message")));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // Menangani jika respons bukan JSON yang valid
                        listener.onError(new VolleyError("Error parsing server response: " + e.getMessage()));
                    }
                },
                error -> {
                    // Menangani error koneksi Volley
                    listener.onError(error);
                }) {
            // Mengirimkan parameter 'angkatan' (sesuai dengan PHP yang sudah diperbaiki)
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // Pastikan parameter ini SAMA dengan yang dicari di PHP ($_POST['angkatan'])
                params.put("angkatan", angkatan);
                return params;
            }
        };

        // Menambahkan request ke Volley Queue
        Volley.newRequestQueue(context).add(stringRequest);
    }

    // ==========================================================
    // FUNGSI BARU untuk mengambil data siswa
    // ==========================================================
    public static void getAllSiswaData(Context context, final SiswaDataListener listener) {
        // Menggunakan JsonObjectRequest karena respons dari getdata.php diawali dengan objek ({})
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL_GET_SISWA,
                null, // Tidak ada body/parameter untuk GET
                response -> {
                    try {
                        String status = response.getString("status");
                        if ("success".equals(status)) {
                            // Mengambil array dari kunci "data"
                            JSONArray dataArray = response.getJSONArray("data");
                            listener.onSuccess(dataArray);
                        } else {
                            // Log jika statusnya bukan 'success' tapi JSON valid
                            String message = response.optString("message", "Status respons dari server tidak 'success'.");
                            Log.w(TAG, "Server status not success: " + message);
                            listener.onError(message);
                        }
                    } catch (JSONException e) {
                        // Error ini terjadi jika respons bukan JSON valid atau kunci 'status'/'data' tidak ada
                        Log.e(TAG, "Error parsing Siswa response: " + e.getMessage());
                        listener.onError("Gagal memproses respons JSON siswa (kunci 'status' atau 'data' hilang): " + e.getMessage());
                    } catch (Exception e) {
                        Log.e(TAG, "Unexpected Error Siswa: " + e.getMessage());
                        listener.onError("Terjadi kesalahan tak terduga saat memproses data siswa.");
                    }
                },
                error -> {
                    String message = "Gagal mengambil data siswa (Koneksi Volley Error)";
                    Log.e(TAG, "Volley Error Siswa: " + error.toString());
                    listener.onError(message);
                }
        );

        getRequestQueue(context).add(jsonObjectRequest);
    }


    // ==========================================================
    // INSERT DATA (Perlu update untuk menggunakan id_siswa)
    // ==========================================================
    public static void insertData(
            Context context,
            final String id_siswa, // 🚨 Tambahkan ID Siswa
            final String nama,
            final String kelas,
            final String jenis,
            final String poin,
            final String tanggal,
            final DataFetchListener<String> listener) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_INSERT,
                response -> {
                    Log.d(TAG, "Response Insert: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.optString("status");
                        String message = jsonResponse.optString("message");

                        if ("success".equalsIgnoreCase(status)) {
                            listener.onSuccess(message);
                        } else {
                            listener.onError(new VolleyError("Server menolak data: " + message));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Parsing Error: " + e.getMessage());
                        listener.onError(new VolleyError("Gagal memproses respon server (non-JSON): " + response));
                    }
                },
                error -> {
                    Log.e(TAG, "Volley Error Insert: " + error.toString());
                    listener.onError(error);
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                // Key harus sama persis dengan insert.php
                params.put("id_siswa", id_siswa); // 🚨 Tambahkan parameter ID
                params.put("nama_siswa", nama);
                params.put("kelas_siswa", kelas);
                params.put("pelanggaran", jenis);
                params.put("poin", poin);
                params.put("tanggal_pelanggaran", tanggal);
                return params;
            }
        };

        getRequestQueue(context).add(stringRequest);
    }

    // ==========================================================
    // GET / READ DATA (Tetap Sama)
    // ==========================================================
    public static void getAllData(Context context, final DataFetchListener<JSONArray> listener) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL_READ, null,
                listener::onSuccess,
                error -> {
                    Log.e(TAG, "Volley Error Read: " + error.toString());
                    listener.onError(error);
                });
        getRequestQueue(context).add(jsonArrayRequest);
    }

    // ==========================================================
    // UPDATE DATA (Tetap Sama)
    // ==========================================================
    public static void updateData(
            Context context,
            final String id,
            final String nama,
            final String kelas,
            final String jenis,
            final String poin,
            final String tanggal,
            final DataFetchListener<String> listener) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPDATE,
                response -> {
                    Log.d(TAG, "Response Update: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.optString("status");
                        String message = jsonResponse.optString("message");

                        if ("success".equalsIgnoreCase(status)) {
                            listener.onSuccess(message);
                        } else {
                            listener.onError(new VolleyError("Server menolak update: " + message));
                        }
                    } catch (JSONException e) {
                        listener.onError(new VolleyError("Respon non-JSON dari server: " + response));
                    }
                },
                error -> {
                    Log.e(TAG, "Volley Error Update: " + error.toString());
                    listener.onError(error);
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("nama_siswa", nama);
                params.put("kelas_siswa", kelas);
                params.put("pelanggaran", jenis);
                params.put("poin", poin);
                params.put("tanggal_pelanggaran", tanggal);
                return params;
            }
        };

        getRequestQueue(context).add(stringRequest);
    }

    // ==========================================================
    // DELETE DATA (Tetap Sama)
    // ==========================================================
    public static void deleteData(Context context, final String id, final DataFetchListener<String> listener) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_DELETE,
                response -> {
                    Log.d(TAG, "Response Delete: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.optString("status");
                        String message = jsonResponse.optString("message");

                        if ("success".equalsIgnoreCase(status)) {
                            listener.onSuccess(message);
                        } else {
                            listener.onError(new VolleyError("Server menolak hapus: " + message));
                        }
                    } catch (JSONException e) {
                        listener.onError(new VolleyError("Respon non-JSON dari server: " + response));
                    }
                },
                error -> {
                    Log.e(TAG, "Volley Error Delete: " + error.toString());
                    listener.onError(error);
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                return params;
            }
        };

        getRequestQueue(context).add(stringRequest);
    }
}
