# EscapeHub – Οδηγός δοκιμής υποδομής (Issue #18)

Σύντομος οδηγός για να ελέγξετε το κοινό περιβάλλον ανάπτυξης του EscapeHub σε Windows.

## 1. Προαπαιτούμενα

Βεβαιωθείτε ότι έχετε εγκαταστήσει:

- Git
- JDK 25
- Node.js 20.19+ ή 22.12+
- Docker Desktop, ανοιχτό και με Docker Engine σε λειτουργία

## 2. Ρυθμίστε το περιβάλλον

Από το root του repository:

```powershell
Copy-Item .env.example .env
```

Το `.env` είναι τοπικό αρχείο ρυθμίσεων. **Μην το κάνετε commit ή push.**

## 3. Εκκινήστε την PostgreSQL

Από το root του repository:

```powershell
docker compose up -d
docker compose ps
```

Περιμένετε μέχρι η υπηρεσία `postgres` να εμφανιστεί ως `healthy`.

Προαιρετικός έλεγχος σύνδεσης:

```powershell
docker compose exec postgres pg_isready -U escapehub -d escapehub
```

Αναμενόμενο αποτέλεσμα:

```text
accepting connections
```

## 4. Εκκινήστε το backend

Ανοίξτε **δεύτερο** PowerShell στο root του repository και εκτελέστε:

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=dev"
```

Αφήστε το terminal ανοιχτό όσο δοκιμάζετε την εφαρμογή.

Ανοίξτε στο browser:

`http://localhost:8080/api/v1/health`

Αναμενόμενη απάντηση:

```json
{"status":"ok"}
```

## 5. Εκκινήστε το frontend

Ανοίξτε **τρίτο** PowerShell:

```powershell
cd frontend
npm ci
npm run dev
```

Ανοίξτε στο browser:

`http://localhost:5173`

Στην ενότητα **Development Environment** πρέπει να εμφανίζεται:

```text
Backend status: Connected
```

## 6. Εκτελέστε τους ελέγχους build και tests

Για το frontend, από τον φάκελο `../../frontend`:

```powershell
npm run build
```

Για το backend, από το root του repository:

```powershell
.\gradlew.bat clean test
```

Και οι δύο εντολές πρέπει να ολοκληρωθούν επιτυχώς.

## 7. Τι πρέπει να επιβεβαιώσετε

- [ ] Η PostgreSQL ξεκινά μέσω Docker Compose και γίνεται `healthy`.
- [ ] Το Spring Boot ξεκινά με το προφίλ `dev` και συνδέεται στην PostgreSQL.
- [ ] Το `/api/v1/health` επιστρέφει `{"status":"ok"}`.
- [ ] Το React ανοίγει στο `http://localhost:5173`.
- [ ] Το frontend εμφανίζει `Backend status: Connected`.
- [ ] Το `npm run build` ολοκληρώνεται επιτυχώς.
- [ ] Το `./gradlew clean test` (στα Windows: `gradlew.bat clean test`) ολοκληρώνεται επιτυχώς.

Ο development current-user μηχανισμός ενεργοποιείται μόνο με το προφίλ `dev`. Το frontend στέλνει το `X-Dev-User-Id` από τη μεταβλητή `VITE_DEV_USER_ID`. Αυτός ο μηχανισμός είναι μόνο για development/testing και **δεν αποτελεί authentication**.

## 8. Τερματισμός

Στα terminals του frontend και backend πατήστε `Ctrl+C`. Έπειτα, από το root:

```powershell
docker compose down
```

Για να διαγράψετε και τα δεδομένα της τοπικής PostgreSQL, χρησιμοποιήστε μόνο αν είστε βέβαιοι:

```powershell
docker compose down -v
```

> Η επιλογή `-v` διαγράφει το Docker volume και επομένως τα τοπικά δεδομένα της βάσης.
