# PLATECH Calculator

Two Neon Glass Java calculators — available as **Android APKs** (installable side-by-side) and **Desktop Java Swing apps**.

## Download for Android

Click either button below to download the `.apk` directly:

| App | Features | Direct Download |
| :--- | :--- | :--- |
| **Mini Calculator** | Basic arithmetic (`+`, `−`, `×`, `÷`, `%`, `( )`, `DEL`, `AC`) | [![Download Mini Calculator APK](https://img.shields.io/badge/Download-Mini--Calculator.apk-a855f7?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Ralds-Es6/PLATECH-CALCULATOR/releases/latest/download/Mini-Calculator.apk) |
| **Scientific Calculator** | Trig (`sin`, `cos`, `tan`, `DEG`/`RAD`), `ln`/`log`, `x²`/`xʸ`/`√`, `n!`, `π`/`e`, `ANS`, Memory (`MC`/`MR`/`M+`/`M−`) | [![Download Scientific Calculator APK](https://img.shields.io/badge/Download-Scientific--Calculator.apk-3b82f6?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Ralds-Es6/PLATECH-CALCULATOR/releases/latest/download/Scientific-Calculator.apk) |

### Direct Links
- **Mini Calculator APK:** https://github.com/Ralds-Es6/PLATECH-CALCULATOR/releases/latest/download/Mini-Calculator.apk
- **Scientific Calculator APK:** https://github.com/Ralds-Es6/PLATECH-CALCULATOR/releases/latest/download/Scientific-Calculator.apk

### How to Install on Android
1. Tap one of the download buttons above on your Android phone.
2. Open the downloaded `.apk` file.
3. If prompted, allow **"Install unknown apps"** for your browser/files app, then tap **Install**.
4. Both apps have separate package IDs (`com.quinto.minicalculator` and `com.quinto.scientificcalculator`), so you can install both on the same device.

---

## Run on Desktop (Linux / Windows / macOS)

Requires **Java 17+**:

```bash
# Mini Calculator (GUI)
java Mini-calculator/CalculatorGUI.java

# Scientific Calculator (GUI)
java Calculator/ScientificCalculator.java

# Console Calculator
java Mini-calculator/Calculator.java
```

## Project Structure
- `Mini-calculator/` — Desktop Java Swing Mini Calculator & console version
- `Calculator/` — Desktop Java Swing Scientific Calculator
- `android/` — Multi-module Android project (`:core`, `:mini-calculator`, `:scientific-calculator`)
- `.github/workflows/build-apks.yml` — Automatically builds both APKs and publishes them to GitHub Releases
