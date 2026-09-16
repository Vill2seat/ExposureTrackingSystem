# Project Instructions

Project: Exposure Tracking System

Read and follow:
Docs/Exposure_Tracking_System_v0.1.md

The technical document is the source of truth.

Use exactly the terminology defined there.

Examples:
- LocationPoint
- Route
- RouteSegment
- Activity
- Exposure
- Exposure Score
- Confidence Score
- Pollution Factor
- Time Factor
- Activity Factor
- Road/Source Factor
- Weather Factor
- Source/Context Factor

Use the formulas defined in the technical document.
Do not invent alternative formulas unless explicitly requested.

Architecture:
GPS / Motion
→ Movement Engine
→ Environment Engine
→ Exposure Engine
→ Exposure Score
→ UI

MVP scope must be respected.

Before implementing a feature:
1. Read the relevant section of the technical document.
2. Reuse existing models and terminology.
3. Avoid duplicating existing functionality.
4. Keep implementation modular.
5. Explain important architectural decisions briefly.