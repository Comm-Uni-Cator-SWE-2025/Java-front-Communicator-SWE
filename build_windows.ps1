Param(
    [string]$AppName = "CoreCommunicator",
    [string]$Version = "1.0.0",
    [string]$BackendJar = "",
    [string]$EnvFile = "",
    [string]$OutputDir = ""
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Require-Command {
    Param([string]$Name)
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Missing required command: $Name"
    }
}

Require-Command "mvn"
Require-Command "jpackage"

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
if (-not $scriptRoot) {
    $scriptRoot = Get-Location
}

$javaDir = Join-Path $scriptRoot "java"
$moduleName = "module-ux"
$moduleDir = Join-Path $javaDir $moduleName
$targetDir = Join-Path $moduleDir "target"
if (-not $OutputDir) {
    $OutputDir = Join-Path $moduleDir "output"
}

Write-Host "Repository root : $scriptRoot"
Write-Host "Java directory  : $javaDir"
Write-Host "Target module   : $moduleName"
Write-Host ""

Write-Host "[1/4] Building shaded jar with Maven..."
Push-Location $javaDir
try {
    $mvnArgs = @(
        "clean", "package",
        "-pl", $moduleName,
        "-am",
        "-DskipTests",
        "-Dcheckstyle.skip=true",
        "-Denforcer.skip=true",
        "-Djacoco.skip=true"
    )
    mvn @mvnArgs
} finally {
    Pop-Location
}

$frontJar = Join-Path $targetDir "module-ux-1.0-SNAPSHOT.jar"
if (-not (Test-Path $frontJar)) {
    throw "Front-end jar not found: $frontJar"
}
Write-Host "Shaded jar ready at $frontJar"

$backendDest = Join-Path $targetDir "core-backend.jar"
if ($BackendJar) {
    if (-not (Test-Path $BackendJar)) {
        throw "Backend jar not found: $BackendJar"
    }
    $resolvedBackend = Resolve-Path $BackendJar
    Copy-Item $resolvedBackend $backendDest -Force
    Write-Host "Copied backend jar to $backendDest"
} else {
    Write-Warning "No backend jar supplied. VartalLauncher will skip auto-starting the core service."
}

$envSource = $null
if ($EnvFile) {
    if (-not (Test-Path $EnvFile)) {
        throw "Env file not found: $EnvFile"
    }
    $envSource = Resolve-Path $EnvFile
} else {
    $defaultEnv = Join-Path $javaDir ".env"
    if (Test-Path $defaultEnv) {
        $envSource = Resolve-Path $defaultEnv
    }
}
if ($envSource) {
    Copy-Item $envSource (Join-Path $targetDir ".env") -Force
    Write-Host "Copied env file to target directory."
} else {
    Write-Warning "No env file detected. Runtime will rely on real environment variables."
}

Write-Host ""
Write-Host "[2/4] Preparing output directories..."
New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
$appImageDir = Join-Path $OutputDir $AppName

Write-Host ""
Write-Host "[3/4] Running jpackage..."
$iconPath = Join-Path $moduleDir "packaging\\VARTAL.ico"
$jpackageArgs = @(
    "--input", $targetDir,
    "--name", $AppName,
    "--app-version", $Version,
    "--main-jar", "module-ux-1.0-SNAPSHOT.jar",
    "--main-class", "com.swe.launcher.VartalLauncher",
    "--type", "app-image",
    "--dest", $OutputDir,
    "--win-console",
    "--win-dir-chooser",
    "--win-shortcut"
)
if (Test-Path $iconPath) {
    $jpackageArgs += @("--icon", $iconPath)
}
jpackage @jpackageArgs

if ($envSource -and (Test-Path $appImageDir)) {
    Copy-Item $envSource (Join-Path $appImageDir ".env") -Force
    Write-Host "Copied env file to $appImageDir\.env"
}
if ($BackendJar -and (Test-Path $backendDest) -and (Test-Path $appImageDir)) {
    Copy-Item $backendDest (Join-Path $appImageDir "core-backend.jar") -Force
    Write-Host "Copied backend jar to packaged image."
}

Write-Host ""
Write-Host "[4/4] Done."
Write-Host "App image directory: $appImageDir"
Write-Host "Launch using: $($appImageDir)\\$AppName.exe"
