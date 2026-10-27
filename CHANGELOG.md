# Changelog

- Initial equipment booking API is available.
- Documentation line: update this sentence on your feature branch.
- Feature branch: document booking validation.
- Main branch: document baseline validation.
## [Set B] - 2026-10-27
### Fixed
- Off-by-one in booking capacity validation (`quantity > available + 1` changed to `quantity > available`).
### Changed
- Cancellation is restricted to CONFIRMED bookings; a repeat cancel returns 409 and does not restore inventory twice.
### Added
- Boundary and cancellation unit tests, Dockerfile, Kubernetes Deployment and Service manifests.
