# Cash FF Esports + Donation Support — Local Full-Stack Setup

React + Vite frontend, Java 21 / Spring Boot REST API, JWT admin login, and MySQL persistence. The project combines the supplied `fullok.html` gaming UI and `suport.html` donation dashboard into one application while retaining the dark RGB/pink-purple-cyan theme.

## Included functionality

- Main Cash FF page with Top Army Supporters for 10:00 AM, 3:00 PM, and 6:00 PM slots.
- Admin-only supporter editing, redeem-code bulk publishing/deletion, custom-room creation/deletion, three-minute room ID unlock timer, winner photo upload, and daily banner upload.
- Public donation form with merchant QR, WhatsApp number, amount, screenshot upload, and non-refundable acknowledgement.
- Donation totals aggregated by WhatsApp number, with multiple screenshots per donor.
- Admin donation dashboard with start/end filters, amount sorting, screenshot preview, and record deletion.
- JWT-protected admin API; public users cannot unlock admin operations by changing frontend state.
- Uploads are saved locally under `backend/uploads` and served by the API.

## Requirements

- Java JDK 21
- Maven 3.9 or newer
- Node.js 22.12 or newer and npm
- Either a local MySQL 8.x server **or** Docker Desktop (to run the included MySQL container)

Check installed tools:

```powershell
java -version
mvn -version
node -v
npm -v
```

## Option A — Use your local MySQL installation

This configuration uses database username `root` and password `Sairam@1` as requested. Make sure your local MySQL root account is actually configured with that password and MySQL is listening on port `3306`.

Open Command Prompt or PowerShell and run:

```powershell
mysql -u root -p
```

When prompted, enter `Sairam@1`, then run this SQL inside the MySQL shell:

```sql
CREATE DATABASE IF NOT EXISTS cashff_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

If the password for your local MySQL root account is different, either set it to `Sairam@1` or change `DB_PASSWORD` in the backend start commands below to the correct existing password. Do not reset a database password blindly on an existing machine.

## Option B — Run MySQL locally with Docker

Open a terminal in the project root (the folder containing `docker-compose.yml`):

```powershell
docker compose up -d mysql
docker compose ps
```

The container database is `cashff_db`, username `root`, password `Sairam@1`, and port `3306`. The Compose file is configured to initialize these credentials on the first creation of its data volume. If an earlier container already initialized a persistent volume with another password, changing the Compose file does **not** change that existing MySQL account. Only if you are comfortable deleting that container's local database data, reset the volume with:

```powershell
docker compose down -v
docker compose up -d mysql
```

`docker compose down -v` permanently deletes the Compose database volume, so do not run it if you need existing data.

Choose **Option A or Option B**, not both, if a local MySQL already occupies port `3306`.

## Run the Spring Boot backend (Terminal 1)

From the project root in **Windows PowerShell**, run:

```powershell
cd .ackend
$env:DB_URL = "jdbc:mysql://localhost:3306/cashff_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "Sairam@1"
$env:ADMIN_USERNAME = "admin"
$env:ADMIN_PASSWORD = "CashFFAdmin@123"
$env:JWT_SECRET = "cashff-local-jwt-secret-change-this-to-a-long-random-secret"
$env:FRONTEND_ORIGIN = "http://localhost:5173"
mvn spring-boot:run
```

Keep this terminal open. Once started, the API is at `http://localhost:8080`.

The admin user is seeded from `ADMIN_USERNAME` and `ADMIN_PASSWORD` only when the admin table is empty (normally on first run). Sign in to the website using username `admin` and password `CashFFAdmin@123`. If you already created an admin in the database, changing these environment variables will not change that existing user's password.

### Windows Command Prompt alternative

Run these commands from the project root in **Command Prompt (cmd.exe)**:

```bat
cd backend
set DB_URL=jdbc:mysql://localhost:3306/cashff_db?createDatabaseIfNotExist=true^&useSSL=false^&allowPublicKeyRetrieval=true^&serverTimezone=UTC
set DB_USERNAME=root
set DB_PASSWORD=Sairam@1
set ADMIN_USERNAME=admin
set ADMIN_PASSWORD=CashFFAdmin@123
set JWT_SECRET=cashff-local-jwt-secret-change-this-to-a-long-random-secret
set FRONTEND_ORIGIN=http://localhost:5173
mvn spring-boot:run
```

Keep that terminal open as well.

## Run the React frontend (Terminal 2)

Open a second terminal in the project root:

```powershell
cd .rontend
npm install
npm run dev
```

Open the local site: **http://localhost:5173**

The frontend's default API URL is `http://localhost:8080`, so no frontend `.env` file is needed for normal local development. The backend must be running at the same time.

To verify the API process responds, open `http://localhost:8080/api/public/home` in your browser. Then open `http://localhost:5173` for the full website.

## First admin setup

1. Click **Admin Login** in the site header and sign in with the local admin credentials above.
2. Open the donation dashboard and upload the actual merchant QR code. No genuine merchant QR is preconfigured; the QR in the original HTML was a demo.
3. Replace the sample supporter names and links, sample redeem code, and custom room.
4. Upload current daily banner and winner screenshots where appropriate.

## API summary

Public:

- `POST /api/auth/login`
- `GET /api/public/home`
- `GET /api/public/redeem-codes`
- `GET /api/public/rooms`
- `GET /api/public/support/qr`
- `POST /api/public/support/donations` (multipart: `whatsapp`, `amount`, `screenshot`, `agree`)

Admin (JWT Bearer token required):

- `GET /api/admin/donations?startTime=&endTime=&sortBy=latest|highToLow|lowToHigh`
- `DELETE /api/admin/donations/{whatsapp}`
- `POST /api/admin/support/qr`
- `GET /api/admin/donor-slots`, `PUT /api/admin/donor-slots/{slot}`
- `POST /api/admin/redeem-codes`, `DELETE /api/admin/redeem-codes`
- `GET /api/admin/rooms`, `POST /api/admin/rooms`, `DELETE /api/admin/rooms/{id}`, `DELETE /api/admin/rooms`
- `POST /api/admin/rooms/{id}/photos` (multipart `p1`, `p2`, `p3`)
- `POST /api/admin/banner`, `DELETE /api/admin/banner`

## Important notes

- For local convenience, the requested database password appears in `application.yml` defaults and `docker-compose.yml`. Do not expose this configuration or reuse this password in production.
- Donation screenshots are submitted by users. The app records donation claims; it does not verify that a payment was actually received from a bank/UPI provider.
- Notification toggles currently update the UI only; they do not send push notifications while the browser is closed.
- The included supporter, code, and room content is demo seed data. Replace or remove it before putting the site online.
