# BleachHack 1.21.11

A fork of [BleachDev/BleachHack](https://github.com/BleachDev/BleachHack), ported to **Minecraft 1.21.11**.

> ⚠️ **Vibe code:** this fork was ported and maintained with heavy AI assistance ("vibecoded"). It works, but expect rough edges, unpolished code, and the occasional bug. Don't treat it as production-grade. Pull requests and fixes are welcome.

## About

This is an unofficial fork of the original **BleachHack** utility mod. The goal is simple: keep the original feature set alive on the latest Minecraft version (**1.21.11**) with as little friction as possible.

- Original project: [https://github.com/BleachDev/BleachHack](https://github.com/BleachDev/BleachHack)
- This fork: [https://github.com/Legonin-dev/BleachHack-1.21.11](https://github.com/Legonin-dev/BleachHack-1.21.11)

## Download

Grab the latest build from the **[Releases](../../releases)** page.

If there is no release yet, you can build it yourself — see below.

## Requirements

- **Minecraft:** 1.21.11
- **Java:** 21+
- **Fabric Loader** for 1.21.11
- **Fabric API** (matching 1.21.11)

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for **Minecraft 1.21.11**.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.11 and drop it into your `mods` folder.
3. Download the latest `BleachHack-1.21.11.jar` from [Releases](../../releases).
4. Put the jar into `.minecraft/mods/`.
5. Launch the game with the Fabric profile.

## Build from source

### Prerequisites

- **JDK 21** ([Adoptium Temurin 21](https://adoptium.net/) recommended)
- **Git**

### Steps

```bash
# 1. Clone the repo
git clone https://github.com/Legonin-dev/BleachHack-1.21.11.git
cd BleachHack-1.21.11

# 2. Build (Linux/macOS)
./gradlew build

# Windows
gradlew.bat build
