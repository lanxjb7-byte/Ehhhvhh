# Lixxzy Lite

Native Android Java project. Package: `com.lixxzy.lite`.

## Build with GitHub Actions
1. Upload the contents of this ZIP to a GitHub repository (keep `.github/workflows/build.yml`).
2. Open **Actions** and run **Build Lixxzy Lite APK**.
3. Download the `Lixxzy-Lite-APK` artifact after the workflow succeeds.

## Important: system setting access
Changing global display density and refresh-rate settings requires elevated shell access. This app uses Shizuku; it does not bypass Android security.
1. Install and start Shizuku using its official setup instructions.
2. Pair/start its service using Wireless debugging (or use root).
3. Open Lixxzy Lite and grant its Shizuku permission when prompted.
4. Turn the switch ON. It requests density 567 and 90 Hz; OFF restores the saved values.

90 Hz is a requested target, not a guarantee: the phone display and firmware must support it. Some OEMs ignore or override refresh-rate settings. If Shizuku is unavailable, the switch will not apply system changes.

Game launch buttons open the installed package:
- Free Fire: `com.dts.freefireth`
- Free Fire MAX: `com.dts.freefiremax`

The supplied logo is included as the app icon and in the main screen.
