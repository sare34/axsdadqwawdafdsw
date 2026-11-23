# حارس الهاتف - Phone Guardian 🛡️
### Autonomous On-Device AI Security Agent for Android (Kotlin + Jetpack Compose)

**Phone Guardian (حارس الهاتف)** is a native Android application built using Kotlin and Jetpack Compose. It acts as an autonomous, always-on AI security agent that continuously monitors, audits, and mitigates device security threats without requiring root access.

---

## 🌟 Key Features (المميزات الرئيسية)

### 1. الاستقلالية والعمل الدائم (Autonomy & Always-On Agent)
- **Foreground Service (`PhoneGuardianService`)**: A persistent background sentinel that maintains real-time situational awareness and provides an ongoing notification with current security score and quick actions.
- **WorkManager (`SecurityScanWorker`)**: Performs continuous deep scans at customizable intervals (1 hour, 6 hours, 24 hours) with zero user intervention.
- **Autonomous Decision Engine (`AutonomousDecisionEngine`)**: Synthesizes multi-vector audit data, interfaces with Gemini AI (using strictly anonymized metadata), executes platform-permitted remediations automatically, and logs all outcomes.

### 2. الفحص الأعمق بدون روت (Deepest Checks Possible Without Root)
- **Apps Audit (`AppAuditor`)**:
  - Detection of the **Triple Threat** combination: SMS/Call Logs + Accessibility Service + Overlay Window (classic signature of modern banking trojans & spyware).
  - Hidden apps detection (apps installed without launcher activities).
  - Privileged abuse detection: Active Device Administrator (`DevicePolicyManager`), Notification Listener (risk of 2FA/OTP token theft), and battery optimization bypasses.
  - Sideloaded / recently installed app inspection.
  - SHA-256 APK fingerprinting.
- **System Posture Audit (`SystemPostureAuditor`)**:
  - Security patch date verification vs kernel vulnerabilities.
  - Developer options, USB debugging (ADB), and Wireless ADB over Wi-Fi detection.
  - Screen lock enforcement status (`KeyguardManager.isDeviceSecure`).
  - Detection of user-installed root CA certificates (SSL/TLS MITM interception).
  - Private DNS encryption status (`Settings.Global.PRIVATE_DNS_MODE`).
- **Network Security (`NetworkAuditor` & `LocalVpnFirewallService`)**:
  - Rogue / loopback DNS tampering detection.
  - On-Device **Local VPN Firewall**: Intercepts and parses IPv4 headers on-device to drop and block outgoing connections to known malicious command-and-control (C2) servers without sending data to any external VPN server.
- **Behavioral & AppOps Audit (`BehaviorAuditor`)**:
  - Background camera, microphone, and fine location access audits via `AppOpsManager`.
- **Threat Intelligence & Hash Matching (`ThreatIntelligenceAuditor`)**:
  - Local database of known malware and stalkerware package signatures.
  - Optional **VirusTotal API** integration: verifies SHA-256 hashes against global threat engines (hashes only).

### 3. المعالجة الذاتية والتعلم (Autonomous Remediation & Learning)
- **Automatic Enforcement**: Blocks malicious destinations via the local VPN packet monitor, drops rogue connections, and alerts instantly.
- **Assisted Deep Links**: For actions restricted by Android security boundaries, provides one-tap deep links directly to the target system settings page.
- **Advanced Shizuku Mode**: When Shizuku is authorized, autonomously revokes dangerous permissions (`pm revoke`) and disables unsafe settings (`settings put ...`) with zero user clicks.
- **Room Database Learning**: Records all past findings, AI proposals, and outcomes. Learns user dismissal decisions to permanently stop repeating warnings the user dismissed.

### 4. قواعد الأمان والخصوصية (Strict Non-Negotiable Safety Rules)
- **No Deletion Without User Consent**: The agent **NEVER** uninstalls, deletes, or disables any app or file without explicit user approval each time.
- **Zero Personal Data Transmission**: Only anonymized metadata (package identifiers, permission names, and system setting states) is sent to Gemini. Messages, photos, contacts, and personal files are **never** read or transmitted.
- **100% On-Device Data**: All logs, findings, and history reside inside the local Room database (`phone_guardian_security.db`).

---

## 🎨 User Interface (واجهة المستخدم)

- **Arabic-First RTL**: Designed natively with Arabic right-to-left typography and strings (`values-ar/strings.xml`).
- **Cyber Dark Theme**: Modern Material 3 dark palette with glowing emerald (`#10B981`) secure states, amber warnings, and crimson threats.
- **Security Score Radial Gauge**: Dynamic animated 0-100 gauge reflecting real-time security posture.
- **6 Core Screens**:
  1. **Dashboard (الرئيسية)**: Radial score, status chips, severity counters, quick scan trigger, and top threats.
  2. **Findings (المخاطر المكتشفة)**: Categorized & filtered threat cards with one-tap auto-fix, deep links, and AI consultation.
  3. **Action Log (سجل الإجراءات)**: Full chronological ledger of everything the agent executed.
  4. **Scan History (سجل الفحص)**: Historical timeline of scan scores and duration.
  5. **AI Security Copilot (المستشار الذكي)**: Interactive conversational security advice powered by Gemini 1.5.
  6. **Settings (الإعدادات)**: Secure configuration of Gemini & VirusTotal keys, scan intervals, VPN toggle, and Shizuku integration.

---

## 🛠️ Project Structure (الهيكل المعماري)

```
phone-guardian/
├── .github/
│   └── workflows/
│       └── build.yml               # Automated APK Build CI
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml    # Minimal permissions & service registrations
│   │   ├── java/com/phoneguardian/security/
│   │   │   ├── PhoneGuardianApp.kt
│   │   │   ├── data/
│   │   │   │   ├── local/          # Room DB, Entities, DAOs, SharedPreferences
│   │   │   │   └── remote/         # Gemini & VirusTotal HTTP clients
│   │   │   ├── domain/
│   │   │   │   ├── model/          # Pure Domain models & Enums
│   │   │   │   └── engine/         # App, System, Network, Behavior, Threat Auditors
│   │   │   ├── service/
│   │   │   │   ├── PhoneGuardianService.kt    # Foreground service
│   │   │   │   ├── LocalVpnFirewallService.kt # On-device TUN packet inspector
│   │   │   │   ├── SecurityScanWorker.kt      # WorkManager periodic worker
│   │   │   │   ├── BootReceiver.kt            # Auto-start on boot
│   │   │   │   └── shizuku/ShizukuManager.kt  # Rootless privileged execution
│   │   │   └── ui/
│   │   │       ├── MainActivity.kt
│   │   │       ├── theme/          # Cyber dark colors, RTL typography
│   │   │       ├── components/     # SecurityScoreGauge, FindingCard, ActionLogItem
│   │   │       └── screens/        # Dashboard, Findings, ActionLog, History, Chat, Settings
│   │   └── res/
│   │       ├── values/strings.xml
│   │       ├── values-ar/strings.xml
│   │       └── drawable/           # Adaptive vector icons & shield graphics
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 🚀 Build & Run Instructions (طريقة البناء والتشغيل)

### Option 1: Automatic Build via GitHub Actions CI (الموصى به)
1. Push this repository to GitHub (`main` or `master` branch).
2. The included GitHub Actions workflow at [build.yml](file:///.github/workflows/build.yml) automatically sets up JDK 17, compiles the project using Gradle, and uploads the compiled APK (`PhoneGuardian-Debug-APK`) directly under the GitHub Actions **Artifacts** tab.

### Option 2: Build in Android Studio
1. Clone or open the `/phone-guardian` directory in **Android Studio Hedgehog / Iguana / Jellyfish (or newer)**.
2. Ensure **JDK 17** is configured in `File -> Settings -> Build, Execution, Deployment -> Build Tools -> Gradle`.
3. Allow Gradle to sync.
4. Select `Run -> Run 'app'` on an Android device or emulator running **Android 8.0 (API 26) through Android 14+ (API 34+)**.

### Option 3: Command Line (Gradle CLI)
```bash
# Generate wrapper if not present
gradle wrapper

# Build Debug APK
./gradlew assembleDebug

# The compiled APK will be located at:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔑 Configuration & Permissions (الإعدادات والصلاحيات)
- **Gemini API Key**: Obtain a free API key from [Google AI Studio](https://aistudio.google.com/) and enter it under **Settings -> مفتاح Gemini API**.
- **VirusTotal API Key**: Obtain an API key from [VirusTotal](https://www.virustotal.com/) and enter it under **Settings -> مفتاح VirusTotal**.
- **Shizuku (Optional)**: If you have [Shizuku](https://shizuku.rikka.app/) installed and running via wireless ADB, enable **الوضع المتقدم (Shizuku)** in Settings to grant Phone Guardian one-click autonomous permission revocation.
