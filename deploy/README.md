# Production Docker Deploy (EC2)

Dokumen ini menjelaskan setup sekali saja untuk auto-deploy backend ke EC2 menggunakan GitHub Actions.

## 1. One-time setup di EC2

Jalankan sebagai `ec2-user`:

```bash
sudo dnf update -y
sudo dnf install -y docker git
sudo systemctl enable --now docker
sudo usermod -aG docker ec2-user
```

Logout lalu login ulang SSH agar group `docker` aktif.

Pastikan docker compose tersedia:

```bash
docker compose version
```

Jika belum ada, install plugin compose terlebih dahulu.

Jika sebelumnya backend pernah dijalankan sebagai `systemd service`, matikan dulu:

```bash
sudo systemctl disable --now json-backend || true
```

## 2. Secrets yang wajib di GitHub Repository

Set di `Settings -> Secrets and variables -> Actions`:

- `PROD_EC2_HOST` (contoh: `54.x.x.x`)
- `PROD_EC2_USER` (contoh: `ec2-user`)
- `PROD_EC2_SSH_KEY` (isi private key `.pem`)
- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`
- `DOCKERHUB_REPO` (contoh: `json-backend`)
- `PROD_DB_NAME`
- `PROD_DB_USER`
- `PROD_DB_PASSWORD`
- `PROD_JWT_SECRET_KEY` (base64 key)

## 3. Cara kerja deploy

- Workflow test (`Backend Test & Quality`) jalan dulu.
- Jika push ke `main` dan test sukses, workflow `Deploy Production` akan:
  1. Build image backend di GitHub Actions
  2. Push image ke Docker Hub
  3. SSH ke EC2
  4. Pull source terbaru branch `main`
  5. Generate `deploy/.env.prod` dari secrets
  6. `docker login`, `docker compose pull backend`, lalu `docker compose up -d`

## 4. Operasional di server

Lokasi app di EC2:

- `/home/ec2-user/apps/json-backend`

Perintah inspeksi:

```bash
cd /home/ec2-user/apps/json-backend
docker compose -f deploy/docker-compose.prod.yml --env-file deploy/.env.prod ps
docker compose -f deploy/docker-compose.prod.yml --env-file deploy/.env.prod logs -f backend
docker compose -f deploy/docker-compose.prod.yml --env-file deploy/.env.prod logs -f db
```

## 5. Local t3.small-like setup for load test/profiling

Tujuan: simulasi keterbatasan resource EC2 `t3.small` (2 vCPU, 2 GiB RAM) di environment lokal untuk profiling.

1) Siapkan env lokal:

```bash
cp deploy/.env.prod.example deploy/.env.prod.local
```

Isi minimal:

```env
APP_ENV=development
POSTGRES_DB=json_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=<your-local-db-password>
JWT_SECRET_KEY=<your-jwt-secret>
DOCKER_IMAGE=json-backend:local-smoke
```

2) Build image backend lokal:

```bash
docker build -t json-backend:local-smoke .
```

3) Jalankan stack dengan override limit resource:

```bash
docker compose \
  -f deploy/docker-compose.prod.yml \
  -f deploy/docker-compose.t3small.yml \
  --env-file deploy/.env.prod.local \
  up -d
```

4) Verifikasi:

```bash
docker compose \
  -f deploy/docker-compose.prod.yml \
  -f deploy/docker-compose.t3small.yml \
  --env-file deploy/.env.prod.local \
  ps

curl -i http://127.0.0.1:8080/api/products
```

5) Stop setelah test:

```bash
docker compose \
  -f deploy/docker-compose.prod.yml \
  -f deploy/docker-compose.t3small.yml \
  --env-file deploy/.env.prod.local \
  down -v
```

Catatan: ini simulasi mendekati resource `t3.small`, bukan reproduksi burst-credit behavior AWS T3 secara persis.
