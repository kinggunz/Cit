# ChatBotG

Aplikasi chat bot Android (Kotlin). Bot: **GunzAPK**.

## Cara build APK dengan GitHub Actions
1. Buat repository baru di GitHub, lalu upload SEMUA isi folder ini (termasuk folder `.github`).
2. Buka tab **Actions** -> workflow **Build APK ChatBotG** -> otomatis jalan (atau klik *Run workflow*).
3. Setelah hijau, buka run tersebut -> bagian **Artifacts** -> download `ChatBotG-APK`.
4. Di dalamnya ada `app-debug.apk` dan `app-release.apk`. Install di HP.

## Fitur
- Splash logo CG 5 detik, Login, Buat akun (konfirmasi password + dialog konfirmasi)
- Chat: `.bot`, `.menu`, `.profile`, `.uptime`, `.clear`, `.logout`
- `.tiktok <link>` download video TikTok tanpa watermark (+HD, +MP3)
- `.ping`, `.waktu`, `.calc`, `.pw`, `.reverse`, `.upper`, `.lower`, `.count`, `.base64`
- Game: `.tictactoe` (kolom bisa dipencet), `.tebak`, `.suit`, `.dadu`, `.koin`, `.8ball`
- Hiburan: `.quote`, `.fakta`, `.lelucon`

Catatan: akun disimpan lokal di HP. Downloader TikTok memakai API publik tikwm.com.
