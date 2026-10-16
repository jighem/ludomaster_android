#!/usr/bin/env python3
"""Import the authorized native Android reference as a replacement game source.
Runs only in a temporary GitHub Actions workspace on a separate feature branch.
"""
from pathlib import Path
import os, shutil, subprocess

root = Path.cwd()
up = Path(os.environ["UPSTREAM_DIR"])
dst = root / "app"
for item in ("src/main", "src/test", "src/androidTest"):
    shutil.rmtree(dst / item, ignore_errors=True)
shutil.copytree(up / "app/src/main", dst / "src/main", dirs_exist_ok=True)
# Retain our tested Android Gradle build config for now; replace Java sources and resources.
# Remove any incompatible tests from the previous board (new tests will be written later).
src = dst / "src/main"
for f in list(src.rglob("*")):
    if not f.is_file(): continue
    old_path = f
    new_path = Path(str(f).replace("vinaykpro/ludoking", "ludomaster/reference"))
    if new_path != old_path:
        new_path.parent.mkdir(parents=True, exist_ok=True)
        shutil.move(old_path, new_path)
        f = new_path
    if f.suffix.lower() not in (".java", ".xml", ".json", ".txt", ".html", ".css", ".js", ".properties"):
        continue
    try:
        content = f.read_text(encoding="utf-8")
    except UnicodeError:
        continue
    replacements = [
        ("com.vinaykpro.ludoking", "com.ludomaster.reference"),
        ("com.vinaykpro", "com.ludomaster"),
        ("Ludo King", "Ludo Masters"),
        ("LUDO KING", "LUDO MASTERS"),
        ("LudoKing", "LudoMasters"),
        ("ludo king", "ludo masters"),
        ("ludoking", "ludomasters"),
    ]
    new = content
    for a,b in replacements:
        new=new.replace(a,b)
    if new!=content: f.write_text(new,encoding="utf-8")
# Original project's launcher manifest has two MAIN/LAUNCHER intent filters.
manifest = src / "AndroidManifest.xml"
s = manifest.read_text()
s = s.replace('package="com.ludomaster.reference"', 'package="com.ludomaster.reference"')
# Remove duplicate launcher filter from HomeActivity, keep SplashActivity launcher.
needle='''        <activity
            android:name=".HomeActivity"
            android:exported="true"
            android:screenOrientation="portrait">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>'''
s=s.replace(needle, '''        <activity android:name=".HomeActivity" android:exported="false" android:screenOrientation="portrait" />''')
s=s.replace('android:allowBackup="true"','android:allowBackup="false"')
s=s.replace('android:theme="@style/Theme.LudoMasters"', 'android:theme="@style/Theme.LudoMasters"')
manifest.write_text(s)
gradle = dst/"build.gradle"
g=gradle.read_text()
g=g.replace("namespace 'com.ludomaster'", "namespace 'com.ludomaster.reference'")
# Preserve our app ID but include required reference UI dependencies.
dep = """    implementation 'androidx.appcompat:appcompat:1.7.0'
    implementation 'com.google.android.material:material:1.12.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.2.1'
    implementation 'com.github.bumptech.glide:glide:4.16.0'
"""
g=g.replace("dependencies {", "dependencies {\n"+dep)
gradle.write_text(g)
# Remove the outdated starter code tree and any tests that relied on it.
shutil.rmtree(src/"java/com/ludomaster/game",ignore_errors=True)
shutil.rmtree(src/"java/com/ludomaster/board",ignore_errors=True)
starter=src/"java/com/ludomaster/MainActivity.java"
if starter.exists(): starter.unlink()
# No internet permission is introduced. No new external server URL or online API.
readme = root / "docs" / "MIGRATION_FROM_REFERENCE.md"
readme.parent.mkdir(parents=True,exist_ok=True)
readme.write_text("""# Native Android source migration

The user reports the upstream author expressly permitted modification and reuse.
This migration copies the reference app's native Java activities, layouts and game assets rather than using the previous simplified board.

## Important next steps
- Audit and remove online/remote-control entry points, tracking, and any unwanted endpoints.
- Replace all embedded raster-image text and original branding with the user's Ludo Masters artwork and LM shield.
- Confirm all prior optional rules (combat, immunity, dynamic home shuffle) are re-integrated into the imported game engine; they are **not** currently wired into the imported source.
- Verify 2–4 player offline pass-and-play with emulator and real devices.
- Keep repository private until rights and redistribution scope are documented.

Do not distribute APK until these issues are complete.
""")
print("Imported native reference source files:",sum(1 for x in src.rglob("*") if x.is_file()))
