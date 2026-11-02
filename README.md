# Aperture Science Terminal — v0.2.0

Larger animated core (84% of shorter display dimension), expanded docked core, additional diagnostic ring details and markers, subtle orange illumination, and existing voice-reactive behaviors.

## Update on GitHub

Replace `src/com/oxytuned/aperture/ApertureSkin.java` and `kiosk-satellite-plugin.json` in your existing repository. Keep the official SDK, tools, and workflow. Commit and publish a new release tagged `v0.2.0` to trigger your GitHub Actions build.

This is source code, not an installable plugin. Device preview is required to verify sizing.


## v0.3.0 responsive transcript
Full-screen mode uses weighted height allocation for the AI core and a scrollable transcript. When no conversation text is present the core expands. When text arrives, the core yields space without increasing overlay height. Long responses scroll instead of being ellipsized. Docked layout remains unchanged.
