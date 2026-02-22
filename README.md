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

## Database Setup (PostgreSQL) – Manual & Docker

Backend menggunakan PostgreSQL sebagai database utama.
Ikuti langkah berikut untuk setup di lokal masing-masing.

### Buat file `.env` di root project
```bash
# Database configuration
DB_URL=jdbc:postgresql://localhost:5432/json_db
DB_USERNAME=postgres
DB_PASSWORD=<your_password_here>
```

Jangan commit `.env` ke repo supaya password aman.
Tiap anggota tim bisa ganti DB_PASSWORD sesuai lokal masing-masing.

### Setup PostgreSQL via Docker

#### 1️⃣ Install Docker

- Windows / macOS

  Download Docker Desktop: https://www.docker.com/products/docker-desktop

- Install & jalankan Docker Desktop

  Pastikan Docker aktif:

  ```bash
  docker --version
  docker compose version
  ```

#### 2️⃣ Pastikan `docker-compose.yml` ada di direktori root proyek

<img width="461" height="517" alt="image" src="https://github.com/user-attachments/assets/7a384d84-f55d-4128-8a15-505d97496c59" />

#### 3️⃣ Jalankan container PostgreSQL di cmd/powershell

  ```
  docker compose up -d
  ```

#### 4️⃣ Konfigurasi application.properties

- Pastikan file src/main/resources/application.properties berisi:

  ```
  spring.datasource.url=${DB_URL}
  spring.datasource.username=${DB_USERNAME}
  spring.datasource.password=${DB_PASSWORD}
  
  spring.jpa.hibernate.ddl-auto=update
  spring.jpa.show-sql=true
  ```

#### 5️⃣ Jalankan Project via Intellij

- Jika berhasil, log akan menampilkan:
  
  ```
  HikariPool-1 - Start completed.
  Tomcat started on port 8080
  ```

### Setup PostgreSQL via Manual

#### 1️⃣ Install PostgreSQL

  Download dan install PostgreSQL sesuai OS masing-masing.

  Saat instalasi, catat:

  - Username (default postgres)

  - Password

  - Port (default 5432)

  Pastikan service PostgreSQL berjalan.

#### 2️⃣ Buat Database

- Masuk ke psql (versi terminal), lalu jalankan:

  ```CREATE DATABASE json_db;```

- atau pgAdmin (versi GUI):
  klik kanan pada Servers/PostgreSQL/Databases

- Pastikan database berhasil dibuat.

#### 4️⃣ Konfigurasi application.properties

- Pastikan file src/main/resources/application.properties berisi:

  ```
  spring.datasource.url=${DB_URL}
  spring.datasource.username=${DB_USERNAME}
  spring.datasource.password=${DB_PASSWORD}
  
  spring.jpa.hibernate.ddl-auto=update
  spring.jpa.show-sql=true
  ```

#### 5️⃣ Jalankan Project via Intellij

- Jika berhasil, log akan menampilkan:

  ```
  HikariPool-1 - Start completed.
  Tomcat started on port 8080
  ```
  
  ## API Documentation
