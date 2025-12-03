## Packaging the Java Frontend for Windows

The frontend module (`java/module-ux`) already produces a shaded (fat) JAR via the Maven Shade Plugin.  
This guide mirrors the process that was previously used for the .NET frontend and Java core so you can build a native Windows app image with `jpackage`.

---

### 1. Prerequisites

1. JDK 21 or newer on Windows with `jpackage` available on the `PATH` (Temurin or Oracle builds work).
2. Apache Maven 3.9+ on the `PATH`.
3. Optional: the backend/core shaded jar (from the Java-core repo) so `VartalLauncher` can auto-start it. If you do not supply one, the packaged app will run the UI only.
4. Optional: a `.env` file (for secrets/config) placed in `java/.env` or passed explicitly to the script.

---

### 2. Quick packaging via script

From the repo root, run PowerShell:

```powershell
pwsh ./build_windows.ps1 `
  -AppName "CoreCommunicator" `
  -Version "1.0.0" `
  -BackendJar "C:\path\to\java-core\module-app\target\module-app-1.0-SNAPSHOT.jar" `
  -EnvFile "C:\path\to\java-core\.env"
```

What the script does:

1. Runs `mvn clean package -pl module-ux -am -DskipTests -Dcheckstyle.skip=true -Denforcer.skip=true -Djacoco.skip=true` inside `/java`.
2. Ensures `java/module-ux/target/module-ux-1.0-SNAPSHOT.jar` exists (this is the shaded UI jar).
3. Copies the backend jar (if provided) to `java/module-ux/target/core-backend.jar` so it is bundled beside the UI.
4. Copies `.env` into both `target/.env` and the final app image.
5. Invokes `jpackage --type app-image --name <AppName> --main-jar module-ux-1.0-SNAPSHOT.jar --main-class com.swe.launcher.VartalLauncher --input java/module-ux/target --dest java/module-ux/output --win-console`.
6. Drops the finished Windows app image in `java/module-ux/output/<AppName>`. Launch `<AppName>.exe` from there.

You can override the name/version/output folder via the script parameters. If you omit `-BackendJar`, the launcher will warn and skip auto-starting the core service.

---

### 3. Manual commands (if you prefer)

1. Build the shaded jar:

   ```powershell
   cd java
   mvn clean package -pl module-ux -am -DskipTests -Dcheckstyle.skip=true -Denforcer.skip=true -Djacoco.skip=true
   ```

2. (Optional) Copy your backend jar and `.env` beside the shaded jar:

   ```powershell
   Copy-Item C:\path\to\module-app\target\module-app-1.0-SNAPSHOT.jar `
     java\module-ux\target\core-backend.jar

   Copy-Item java\.env java\module-ux\target\.env
   ```

3. Run `jpackage` (same flags shown in the script):

   ```powershell
   jpackage --input java\module-ux\target `
     --name CoreCommunicator `
     --app-version 1.0.0 `
     --main-jar module-ux-1.0-SNAPSHOT.jar `
     --main-class com.swe.launcher.VartalLauncher `
     --type app-image `
     --dest java\module-ux\output `
     --win-console `
     --win-dir-chooser `
     --win-shortcut
   ```

4. Copy `.env` into the app image after packaging:

   ```powershell
   Copy-Item java\.env java\module-ux\output\CoreCommunicator\.env
   ```

5. Launch `java\module-ux\output\CoreCommunicator\CoreCommunicator.exe`.

---

### 4. Troubleshooting tips

- If Maven cannot download dependencies, ensure you have network access or a populated local `.m2` cache.
- `jpackage` requires a full JDK install. If you only have a JRE, install a JDK and set `JAVA_HOME`.
- When building on Windows for the first time you might need to enable execution of PowerShell scripts: `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned`.
- For signed installers (`--type msi`/`exe`) you need WiX Toolset on the `PATH`. The provided script sticks to `app-image`, which does not need WiX.

This matches the workflow your teammate used for the Java core repo, but targets the Java frontend (`module-ux`). Adjust names/paths as needed for release builds.
