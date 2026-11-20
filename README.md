# LittleCare v3.0.0 (`com.littlecare.babytracker`)

Minimalist, offline-first Android baby tracker built with **Jetpack Compose**, **Room Database**, and **Material 3** (Pure OLED Black + Low-Glare Red-Amber Night Vision mode).

## What's Included in v3.0.0
- **Updated Layered Adaptive App Icon (`ic_launcher` & `ic_launcher_round`)**: Midnight-indigo halo background with a warm golden crescent, ivory heart cradle, and sky-blue starlight accents (`mipmap-anydpi-v26` + `mipmap-xhdpi` fallback + Android 13+ monochrome support).
- **1-Tap Care Logging & Live Session Timer**: Breast (Left/Right) & Sleep live timers, Bottle volume logger (Breastmilk / Formula), Wet/Dirty diaper quick-logs, and Medication/Temperature log.
- **Weekly 1st-Year Growth Tracker (Weeks 0–52)**: Weight (kg) and Length/Height (cm) plotted directly against **WHO Child Growth Standards (50th percentile)** for Boys and Girls.
- **Indian Immunization Schedule (MoHFW / IAP)**: Auto-calculated vaccination due dates from birth date (At Birth, 6w, 10w, 14w, 6m, 9m, 12m, 16–18m) with completion tracking and overdue alerts.
- **Multi-Baby Support, Milestones & Pediatric Export**: Switch between multiple profiles, track 1st-year milestones, enable optional Biometric/PIN lock, and share a complete CSV health summary.
- **GitHub Actions CI (`.github/workflows/build-apk.yml`)**: Automatically builds and uploads `app-debug.apk` on every push to `main`.

## How to Update GitHub & Build the APK
1. Extract this zip and copy its contents over your repository folder (or push directly as a fresh repo).
2. Commit and push to GitHub:
   ```bash
   git add -A
   git commit -m "Update LittleCare to v3.0.0 with new adaptive icon and full feature set"
   git push origin main
   ```
3. **Build APK**:
   - **On GitHub**: Open the **Actions** tab in your repository → select **Build LittleCare APK** → download the `LittleCare-v3.0.0-debug-apk` artifact.
   - **Locally in Android Studio**: Open the folder → **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
   - **Uninstall old build first** (or install v3.0.0 `versionCode 3` over it) so your Android launcher refreshes the cached app icon.
