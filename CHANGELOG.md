# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog and this project adheres to Semantic Versioning.

- Keep a Changelog: https://keepachangelog.com/en/1.1.0/
- Semantic Versioning: https://semver.org/spec/v2.0.0.html


## [Unreleased]
### Fixed
- Drop MMCE main thread tasks queued while connected to a remote server, which were kept with their worlds in memory.

## [1.0.0] - 2026-11-05
### Added
- Add glue and solvent to control automatic item hatch transfers.
- Add configurable ME fluid and gas hatch capacities.
- Add adjustable ME hatch polling rates to reduce lag.
- Add in-game configuration with independent feature toggles.
