package uas.indomaret;

public class Produk {
    private int id;
    private String nama;
    private double harga;
    private int stok;
    private int idKategori;
    private String namaKategori; 

    public Produk(int id, String nama, double harga, int stok, int idKategori, String namaKategori) {
        this.id = id;
        this.nama = nama;
        this.harga = harga;
        this.stok = stok;
        this.idKategori = idKategori;
        this.namaKategori = namaKategori; 
    }
    public int getId() { return id; }
    public String getNama() { return nama; }
    public double getHarga() { return harga; }
    public int getStok() { return stok; }
    public int getIdKategori() { return idKategori; }
    public String getNamaKategori() { return namaKategori; } 
}