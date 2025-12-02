package com.swe.launcher;

import com.swe.ux.App;

import java.io.File;
import java.nio.file.*;

public class VartalLauncher {

    public static void main(String[] args) {
        // Launch UI first — keep users happy
        App.main(args);

        // Start backend after delay, silently if missing
        new Thread(() -> {
            try {
                Thread.sleep(5000); // wait for UI login
                launchBackend();
            } catch (Exception ignored) {}
        }, "backend-launcher").start();
    }

    private static void launchBackend() {
        try {
            Path jarPath = Paths.get(
                    VartalLauncher.class.getProtectionDomain()
                            .getCodeSource().getLocation().toURI()
            );

            Path appDir = jarPath.getParent();
            Path coreJar = appDir.resolve("core-backend.jar");

            if (!Files.exists(coreJar)) {
                System.err.println("⚠ Backend JAR missing: " + coreJar);
                return;
            }

            Path javaBin = Paths.get(System.getProperty("java.home"), "bin", "java");

            File outLog = appDir.resolve("backend.log").toFile();
            File errLog = appDir.resolve("backend-error.log").toFile();

            new ProcessBuilder(
                    javaBin.toString(), "-jar", coreJar.toString()
            )
            .directory(appDir.toFile())
            .redirectOutput(outLog)
            .redirectError(errLog)
            .start();

            System.out.println("🚀 Backend started successfully!");

        } catch (Exception ignored) {}
    }
}
