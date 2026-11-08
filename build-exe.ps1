# CyberLab - construit l'application Windows (.exe) avec jpackage.
# Prerequis : JDK 17+ (jpackage inclus) et Maven dans le PATH.
#   .\build-exe.ps1             -> dossier dist\CyberLab\ avec CyberLab.exe (pas d'installation, aucun outil en plus)
#   .\build-exe.ps1 -Installer  -> dist\CyberLab-1.0.0.exe (installateur ; necessite WiX Toolset 3.x)
#   .\build-exe.ps1 -Console    -> ajoute une fenetre console (pour voir les erreurs au premier test)
param([switch]$Installer, [switch]$Console)
$ErrorActionPreference = "Stop"

mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) { throw "Echec de la construction Maven" }
if (-not (Test-Path target\cyberlab.jar)) { throw "target\cyberlab.jar introuvable (plugin maven-shade ?)" }

Remove-Item -Recurse -Force target\jpackage-input, dist -ErrorAction SilentlyContinue
New-Item -ItemType Directory target\jpackage-input | Out-Null
Copy-Item target\cyberlab.jar target\jpackage-input\

$args = @(
  "--type", $(if ($Installer) { "exe" } else { "app-image" }),
  "--name", "CyberLab",
  "--app-version", "1.0.0",
  "--vendor", "Genie Telecom",
  "--input", "target\jpackage-input",
  "--main-jar", "cyberlab.jar",
  "--main-class", "com.cyberlab.Launcher",
  "--dest", "dist",
  "--add-modules", "java.base,java.desktop,java.logging,java.management,java.naming,java.net.http,java.prefs,java.security.jgss,java.security.sasl,java.sql,java.xml,jdk.jsobject,jdk.unsupported,jdk.xml.dom"
)
if ($Installer) { $args += @("--win-menu", "--win-shortcut", "--win-dir-chooser") }
if ($Console)   { $args += "--win-console" }

jpackage @args
if ($LASTEXITCODE -ne 0) { throw "Echec de jpackage" }

Write-Host ""
Write-Host "Termine. Placez db.properties (et mail.properties) dans : $env:APPDATA\CyberLab\"
