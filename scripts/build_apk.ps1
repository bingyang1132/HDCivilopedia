# Mirrors output_android/ into the Android WebView project's assets, builds a signed release
# APK, and files it into the run's archive snapshot as HDCivilopedia_<yyyyMMdd>.apk.
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

# Which run this apk is cut from, resolved before the build because it is also the version:
# normally today's snapshot, the one `Main after_init` just wrote. Falling back to the newest
# keeps a build run past midnight out of an orphan folder holding only an apk.
$archiveRoot = (Select-String -Path (Join-Path $repo "config.properties") -Pattern '^\s*archive\.folder\s*=\s*(.+?)\s*$' |
    Select-Object -First 1).Matches.Groups[1].Value
$snapshot = $null
if ($archiveRoot -and (Test-Path $archiveRoot)) {
    $snapshot = Join-Path $archiveRoot ("HDCivilopedia_" + (Get-Date -Format "yyyyMMdd"))
    if (-not (Test-Path $snapshot)) {
        $snapshot = (Get-ChildItem $archiveRoot -Directory |
            Where-Object { $_.Name -match '^HDCivilopedia_\d{8}$' } |
            Sort-Object Name -Descending | Select-Object -First 1).FullName
        if ($snapshot) { Write-Host "no snapshot for today, filing under $(Split-Path -Leaf $snapshot)" }
    }
}
if ($snapshot) {
    $stamp = (Split-Path -Leaf $snapshot) -replace '^HDCivilopedia_', ''
} else {
    Write-Host "archive.folder is unset or missing, versioning by today and not archiving the apk"
    $stamp = Get-Date -Format "yyyyMMdd"
}

Write-Host "syncing $source -> $assets"
robocopy $source $assets /MIR /NFL /NDL /NJH /NP | Out-Null
# robocopy uses 0-7 for success (files copied, extras deleted, ...); 8 and up are failures
if ($LASTEXITCODE -ge 8) { throw "robocopy failed with exit code $LASTEXITCODE" }

# hdVersion becomes both versionCode and versionName, so a version identifies its snapshot
Write-Host "running assembleRelease (version $stamp)"
& $gradlew -p $AndroidProject --console=plain "-PhdVersion=$stamp" assembleRelease
if ($LASTEXITCODE -ne 0) { throw "gradle assembleRelease failed" }

$apk = Join-Path $AndroidProject "app\build\outputs\apk\release\release-$stamp.apk"
if (-not (Test-Path $apk)) { throw "expected apk not found: $apk" }

# An unsigned release apk builds fine and installs on nothing, so check rather than assume
$sdk = ((Select-String -Path (Join-Path $AndroidProject "local.properties") -Pattern '^\s*sdk\.dir\s*=\s*(.+?)\s*$' |
    Select-Object -First 1).Matches.Groups[1].Value) -replace '\\\\', '\' -replace '\\:', ':'
$apksigner = Get-ChildItem (Join-Path $sdk "build-tools") -Directory -ErrorAction SilentlyContinue |
    Sort-Object Name -Descending | ForEach-Object { Join-Path $_.FullName "apksigner.bat" } |
    Where-Object { Test-Path $_ } | Select-Object -First 1
if ($apksigner) {
    & $apksigner verify --print-certs $apk | Select-String "Signer #1 certificate DN"
    if ($LASTEXITCODE -ne 0) { throw "$apk is not properly signed" }
} else {
    Write-Host "apksigner not found under $sdk, skipping the signature check"
}

$file = Get-Item $apk
"{0}  {1:N1} MB  {2}" -f $file.FullName, ($file.Length / 1MB), $file.LastWriteTime

# File it next to the snapshot Archive.java wrote for this run, under the folder's own name --
# that is what the hand-made archives already do (HDCivilopedia_20260807/HDCivilopedia_20260807.apk).
if ($snapshot) {
    $target = Join-Path $snapshot ((Split-Path -Leaf $snapshot) + ".apk")
    Copy-Item $apk $target -Force
    Write-Host "archived -> $target"
}
