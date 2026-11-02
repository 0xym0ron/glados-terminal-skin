# Aperture Science Terminal — Kiosk Satellite voice skin

An original Aperture-inspired animated voice overlay for Kiosk Satellite. Native Android Canvas graphics; no external assets or network calls at runtime.

## Features
- Full-screen and docked voice overlays; touch-through to Kiosk Satellite
- Orange central AI core, counter-rotating diagnostic arcs, reactive radial telemetry
- Conversation command and answer text (respects Kiosk Satellite display settings)
- Follows Appearance theme, background opacity, text scaling and reactive toggle
- Lightweight 30 FPS rendering; no background activity when overlay is hidden

## Requirements
- Kiosk Satellite 2026.10.30 or newer with native Voice Satellite enabled
- JDK 17+, Python 3, Android SDK (platforms and build-tools)
- Official Kiosk Satellite plugin build kit

## Build
```bash
git clone https://github.com/jxlarrea/kiosk-satellite-plugin-hello-world /tmp/ks-template
mkdir -p .build-kit
cp -r /tmp/ks-template/tools /tmp/ks-template/sdk .build-kit/
export ANDROID_HOME=/absolute/path/to/android-sdk
python3 .build-kit/tools/build.py .
```

The plugin package and checksum should be generated in `dist/`. This repository is **source only** until built; do not try to install the source ZIP directly.

## Install
Publish this source repository to GitHub, build a release using the official toolchain, then in Kiosk Satellite: **Settings → Plugin Manager → Enable Plugins → Add plugin → GitHub repository URL → Preview → Trust and install**. Select **Aperture Science Terminal** under **Voice Satellite → Appearance → Assistant 1 skin / Assistant 2 skin**.

## Notes
The skin affects visuals only. It does not replace the wake word, Home Assistant Assist, OpenAI, or GLaDOS Wyoming TTS. This is a fan-inspired design, not affiliated with Valve.

## Status
The source has not been compiled or tested against the official Android SDK in the authoring environment. Run the build and verify on your Echo Show before relying on it.
