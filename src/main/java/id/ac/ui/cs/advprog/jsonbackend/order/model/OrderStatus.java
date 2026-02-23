package id.ac.ui.cs.advprog.jsonbackend.order.model;

public enum OrderStatus {
    PAID, // Sudah dibayar
    PURCHASED, // Sudah dibeli jastiper
    SHIPPED, // Barang dikirim ke alamat titiper
    COMPLETED, // Barang diterima
    CANCELLED // Pesanan dibatalkan
}