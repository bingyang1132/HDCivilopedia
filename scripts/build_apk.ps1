# Mirrors output_android/ into the Android WebView project's assets, builds the APK, and
# files it into the run's archive snapshot as HDCivilopedia_<yyyyMMdd>.apk.
# Run `Main after_init` first; this script only packages what is already generated.
#
#   powershell -ExecutionPolicy Bypass -File scripts\build_apk.ps1
#
param(
    [string]$AndroidProject = "E:\MyApplication"
)

$ErrorActionPreference = "Stop"

$repo    = Split-Path -Parent $PSScriptRoot
$source  = Join-Path $repo "output_android"
$assets  = Join-Path $AndroidProject "app\src\main\assets"
$gradlew = Join-Path $AndroidProject "gradlew.bat"

if (-not (Test-Path (Join-Path $source "index.html"))) {
    throw "$source has no index.html -- run ``Main after_init`` first"
}
if (-not (Test-Path $gradlew)) { throw "gradlew.bat not found under $AndroidProject" }

Write-Host "syncing $source -> $assets"
robocopy $source $assets /MIR /NFL /NDL /NJH /NP | Out-Null
# robocopy uses 0-7 for success (files copied, extras deleted, ...); 8 and up are failures
if ($LASTEXITCODE -ge 8) { throw "robocopy failed with exit code $LASTEXITCODE" }

Write-Host "running assembleDebug"
& $gradlew -p $AndroidProject --console=plain assembleDebug
if ($LASTEXITCODE -ne 0) { throw "gradle assembleDebug failed" }

$apk = Get-ChildItem (Join-Path $AndroidProject "app\build\outputs\apk\debug") -Filter *.apk |
    Sort-Object LastWriteTime -Descending | Select-Object -First 1
"{0}  {1:N1} MB  {2}" -f $apk.FullName, ($apk.Length / 1MB), $apk.LastWriteTime

# File it next to the snapshot Archive.java wrote for this run, under the folder's own name --
# that is what the hand-made archives already do (HDCivilopedia_20260807/HDCivilopedia_20260807.apk).
$archiveRoot = (Select-String -Path (Join-Path $repo "config.properties") -Pattern '^\s*archive\.folder\s*=\s*(.+?)\s*$' |
    Select-Object -First 1).Matches.Groups[1].Value
if (-not $archiveRoot -or -not (Test-Path $archiveRoot)) {
    Write-Host "archive.folder is unset or missing, not archiving the apk"
    return
}

# Normally today's folder, which `Main after_init` just created. Falling back to the newest
# one keeps a build run past midnight out of an orphan folder holding only an apk.
$snapshot = Join-Path $archiveRoot ("HDCivilopedia_" + (Get-Date -Format "yyyyMMdd"))
if (-not (Test-Path $snapshot)) {
    $snapshot = (Get-ChildItem $archiveRoot -Directory |
        Where-Object { $_.Name -match '^HDCivilopedia_\d{8}$' } |
        Sort-Object Name -Descending | Select-Object -First 1).FullName
    if (-not $snapshot) { throw "no HDCivilopedia_<yyyyMMdd> snapshot under $archiveRoot" }
    Write-Host "no snapshot for today, filing under $(Split-Path -Leaf $snapshot)"
}

$target = Join-Path $snapshot ((Split-Path -Leaf $snapshot) + ".apk")
Copy-Item $apk.FullName $target -Force
Write-Host "archived -> $target"
