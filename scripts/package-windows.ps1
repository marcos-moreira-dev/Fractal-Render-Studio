param(
    [ValidateSet("exe", "msi", "app-image")]
    [string]$Type = "exe"
)

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$targetDir = Join-Path $projectRoot "target"
$dependencyDir = Join-Path $targetDir "dependency"
$inputDir = Join-Path $targetDir "jpackage-input-dir"
$outputDir = Join-Path $targetDir "installer"
$mainJar = "fractal-render-studio-0.1.0-SNAPSHOT.jar"
$mainClass = "com.marcos.fractalstudio.FractalStudioApplication"
$appName = "FractalRenderStudio"
$displayName = "Fractal Render Studio"
$version = "0.1.0"
$iconPng = Join-Path $projectRoot "src\main\resources\assets\icons\app-icon.png"
$iconIco = Join-Path $projectRoot "src\main\resources\assets\icons\app-icon.ico"

Write-Host "Preparando artefactos para el instalador..."
if (Test-Path $inputDir) {
    Remove-Item -Path $inputDir -Recurse -Force
}
if (Test-Path $dependencyDir) {
    Remove-Item -Path $dependencyDir -Recurse -Force
}

& (Join-Path $projectRoot "mvnw.cmd") -q package dependency:copy-dependencies "-DskipTests" "-DincludeScope=runtime" "-DoutputDirectory=$dependencyDir"

python (Join-Path $projectRoot "scripts\generate-windows-icon.py")

New-Item -ItemType Directory -Path $inputDir -Force | Out-Null
New-Item -ItemType Directory -Path $outputDir -Force | Out-Null

if ($Type -eq "app-image") {
    $existingAppImageDir = Join-Path $outputDir $appName
    if (Test-Path $existingAppImageDir) {
        Remove-Item -Path $existingAppImageDir -Recurse -Force
    }
}
elseif ($Type -eq "exe") {
    $existingExe = Join-Path $outputDir "$appName-$version.exe"
    if (Test-Path $existingExe) {
        Remove-Item -Path $existingExe -Force
    }
}
elseif ($Type -eq "msi") {
    $existingMsi = Join-Path $outputDir "$appName-$version.msi"
    if (Test-Path $existingMsi) {
        Remove-Item -Path $existingMsi -Force
    }
}

Copy-Item -Path (Join-Path $targetDir $mainJar) -Destination $inputDir -Force
Get-ChildItem -Path $dependencyDir -Filter *.jar | ForEach-Object {
    Copy-Item -Path $_.FullName -Destination $inputDir -Force
}

if (-not (Test-Path (Join-Path $inputDir $mainJar))) {
    throw "No se pudo preparar el JAR principal dentro del directorio de entrada para jpackage."
}

if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) {
    throw "jpackage no esta disponible. Usa un JDK 21 que incluya jpackage."
}

$arguments = @(
    "--type", $Type,
    "--dest", $outputDir,
    "--input", $inputDir,
    "--name", $appName,
    "--main-jar", $mainJar,
    "--main-class", $mainClass,
    "--vendor", $displayName,
    "--app-version", $version,
    "--icon", $iconIco,
    "--description", "Exploracion profunda de fractales, puntos de camara y render de video MP4.",
    "--copyright", "Fractal Render Studio",
    "--java-options", "--module-path",
    "--java-options", '$APPDIR',
    "--java-options", "--add-modules",
    "--java-options", "javafx.controls,javafx.graphics,javafx.base"
)

if ($Type -ne "app-image") {
    $arguments += @(
        "--win-dir-chooser",
        "--win-shortcut",
        "--win-shortcut-prompt",
        "--win-menu",
        "--win-menu-group", $displayName,
        "--install-dir", $displayName
    )
}

Write-Host "Generando paquete Windows tipo $Type ..."
& jpackage @arguments

Write-Host "Instalador generado en $outputDir"
