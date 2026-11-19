# Privacy Policy for Auto Check-In for Creality

**Last Updated:** November 19, 2026

This Privacy Policy explains how **Auto Check-In for Creality** ("the Application", "we", "our") handles your data. The Application is an open-source, unofficial community utility designed to help users automate their daily check-in tasks on Creality Cloud.

---

### 1. Zero Personal Data Collection
We do **not** collect, harvest, monitor, sell, or transmit any personally identifiable information, usage statistics, analytics, or browsing habits. We operate no external developer servers or tracking telemetry.

---

### 2. Local Storage and Credentials Handling
- **Session Cookies & Tokens:** When you log in via the in-app Web Login or manually enter your session cookies, those values (including `model_token`, `model_user_id`, and `model_device_id`) are stored **strictly on your local device** using Android's encrypted private DataStore storage.
- **On-Device Execution:** The background scheduler (`WorkManager`) and manual check-in requests communicate **directly and exclusively** with Creality Cloud's official servers (`https://www.crealitycloud.com`) via HTTPS/TLS encryption.
- **Zero Cloud Backup:** By design, local session cookies and DataStore preferences are excluded from Android OS cloud backups to safeguard your credentials.

---

### 3. Device Permissions Explained
- **INTERNET (`android.permission.INTERNET`)**: Required to establish encrypted HTTPS connections to Creality Cloud API endpoints to execute check-ins and load the web login page.
- **ACCESS_NETWORK_STATE (`android.permission.ACCESS_NETWORK_STATE`)**: Used by the background scheduler to ensure automated check-in jobs only run when your device has an active internet connection.
- **POST_NOTIFICATIONS (`android.permission.POST_NOTIFICATIONS`)**: Used optionally on Android 13+ devices to alert you of check-in outcomes (e.g. daily streak gained or expired session notification).

---

### 4. Third-Party Websites & Services
When using the in-app Web Login feature, the Application displays the official web portal of Creality Cloud (`https://www.crealitycloud.com`). Interacting with Creality Cloud is governed by Creality's own Terms of Service and Privacy Policy.

---

### 5. Disclaimer & Trademarks
**Auto Check-In for Creality** is an independent, unofficial utility developed by and for the 3D printing community. It is **not** developed, sponsored, affiliated with, or endorsed by Shenzhen Creality 3D Technology Co., Ltd. or Creality Cloud. "Creality" is a trademark of Shenzhen Creality 3D Technology Co., Ltd.

---

### 6. Contact & Open Source
If you have any questions or concerns regarding this policy, you can contact the developer directly or inspect the source code on GitHub.
