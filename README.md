# JaStip Online Nasional (JSON) - Backend

Backend service untuk sistem JaStip Online Nasional (JSON) — platform jasa titip & war barang limited.

Proyek Akhir Mata Kuliah Pemrograman Lanjut 2025/2026

---

## Deskripsi Sistem

JaStip Online Nasional (JSON) adalah platform layanan titip (jastip) yang menghubungkan Jastiper (Traveler) dengan Titipers (Buyer) untuk transaksi barang limited edition atau eksklusif antarwilayah.

Backend ini bertanggung jawab atas:
- Autentikasi & otorisasi pengguna
- Manajemen katalog & stok
- Manajemen order & war engine
- Manajemen wallet & transaksi
- Manajemen voucher & promo
- Validasi constraint bisnis & integritas data

## Tech Stack

- React (Next.js)	
- Java 21
- Spring Boot
- PostgreSQL

## Setup & Installation

## Database Setup (PostgreSQL)

Backend menggunakan PostgreSQL sebagai database utama.
Ikuti langkah berikut untuk setup di lokal masing-masing.

### 1️⃣ Install PostgreSQL

  Download dan install PostgreSQL sesuai OS masing-masing.

  Saat instalasi, catat:

  - Username (default postgres)

  - Password

  - Port (default 5432)

  Pastikan service PostgreSQL berjalan.

### 2️⃣ Buat Database

Masuk ke psql (versi terminal), lalu jalankan:

```CREATE DATABASE json_db;```

atau pgAdmin (versi GUI):
- klik kanan pada Servers/PostgreSQL/Databases

Pastikan database berhasil dibuat.

### 3️⃣ Set Environment Variables (Windows)

Jalankan di PowerShell (sekali saja):

```
setx DB_URL "jdbc:postgresql://localhost:5432/json_db"
setx DB_USERNAME "postgres"
setx DB_PASSWORD "<isi dengan password PostgreSQL lokal masing-masing>"
```

Setelah itu:

Tutup terminal

Buka terminal baru

4️⃣ Konfigurasi application.properties

Pastikan file src/main/resources/application.properties berisi:

```
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

5️⃣ Jalankan Project

Jika berhasil, log akan menampilkan:

```
HikariPool-1 - Start completed.
Tomcat started on port 8080
```

## API Documentation
