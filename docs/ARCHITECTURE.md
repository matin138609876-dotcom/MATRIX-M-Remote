# MATRIX-M-Remote
## Technical Architecture and Product Specification

Version: 1.0
Status: Initial Architecture
Primary Test Device: Samsung Galaxy A24
Platform: Android 16
Architecture: ARM64
Language: Kotlin

---

## 1. Project Vision

MATRIX-M-Remote is a modular Android application for securely connecting and managing multiple Android devices remotely.

The first implementation and primary test environment will be Samsung Galaxy A24.

The architecture must support additional Android devices in future releases.

## 2. Device Roles

- Host: The device that shares explicitly authorized resources.
- Controller: The device that requests access and uses approved remote features.
- A device may support both roles.

Initial configuration:
- Primary host: Samsung Galaxy A24.
- Additional devices: Paired Android phones.

## 3. Device Pairing

Initial pairing uses a QR code or a one-time pairing code.

Pairing workflow:

1. Host generates a short-lived pairing QR code.
2. Controller scans the QR code.
3. Controller displays host identity and connection request.
4. Host displays controller identity and requested permissions.
5. Host owner reviews and approves the request.
6. Android system permissions are requested through official system interfaces when needed.
7. Both devices establish an authenticated pairing.
8. Pairing credentials are stored securely.

A QR scan alone must never grant access.

A successfully used one-time QR code must expire and become invalid.

## 4. Persistent Pairing

Paired devices must remain registered after:

- Application restart.
- Host reboot.
- Controller reboot.
- Temporary network disconnection.

Future connections should use stored cryptographic credentials instead of requiring another QR scan.

Users must be able to revoke a paired device at any time.

## 5. Permissions

Permissions are stored separately for each paired device.

Planned permission categories:

- Remote screen viewing.
- Remote input control.
- Front camera.
- Rear camera.
- Microphone.
- File transfer.
- Clipboard access.
- Notification access.
- Screen recording.
- Device status information.

Each permission must have an explicit state:

- Not requested.
- Requested.
- Approved.
- Denied.
- Revoked.
- Temporarily unavailable.

Android runtime permissions and special permissions must use official Android authorization flows.

MediaProjection authorization may need to be repeated after a restart or when Android requires it.

No permission may be silently granted or obtained by bypassing Android security.

## 6. Remote Screen

Planned capabilities:

- Live screen sharing.
- Screen quality selection.
- Connection status.
- Explicit start and stop controls.
- Visible indication while screen capture is active.

Technology: Android MediaProjection and WebRTC.

## 7. Camera

Planned capabilities:

- Front camera streaming.
- Rear camera streaming.
- Camera selection.
- Explicit camera permission.
- Visible active-camera indication.
- Immediate stop control.

Camera access must never be hidden or silently activated.

## 8. Microphone

Microphone streaming is optional and disabled by default.

It requires explicit authorization and a visible active-use indication.

## 9. Remote Control

Remote input is limited to Android-supported and user-authorized mechanisms.

Accessibility Service, if required, must be enabled manually by the device owner.

The application must not bypass Android security restrictions.

## 10. File Transfer

Planned capabilities:

- Send files.
- Receive files.
- Display transfer progress.
- Cancel transfers.
- Verify file integrity.
- Apply configurable file-size limits.

## 11. Messaging and Clipboard

Planned capabilities:

- Device-to-device text chat.
- Explicit clipboard sharing.
- Text transfer.
- Optional file sharing through the transfer module.

Clipboard sharing must be visible and user-controlled.

## 12. Device Information

Display only authorized information:

- Device name.
- Android version.
- Battery level.
- Network status.
- Connection status.
- Application version.

## 13. Notifications

Notification access is optional.

Notification Listener permission must be granted manually by the device owner.

## 14. Sessions and Multiple Devices

The system must support:

- Multiple paired devices.
- Multiple simultaneous sessions.
- Per-device access control.
- Session history.
- Individual disconnection.
- Disconnect all.
- Configurable simultaneous connection limit.

## 15. Security Architecture

Security requirements:

- TLS-protected signaling.
- Encrypted peer-to-peer communication.
- Strong device authentication.
- Secure credential storage using Android Keystore.
- Expiring pairing codes.
- Replay protection.
- Per-device authorization.
- Session expiration.
- Permission revocation.
- Visible sensitive-access indicators.
- Immediate session termination.

No credentials or private keys may be stored in plaintext.

## 16. Network Architecture

Planned components:

- Signaling server.
- WebRTC connections.
- STUN support.
- TURN relay support when direct connectivity fails.
- Local network discovery as an optional feature.
- Internet connectivity support.

The signaling protocol must be versioned.

## 17. Android Application Modules

Planned modular structure:

- app
- core
- security
- devices
- pairing
- permissions
- sessions
- remote-screen
- remote-control
- camera
- microphone
- file-transfer
- messaging
- clipboard
- notifications
- device-info
- settings
- plugins

Modules will be implemented and tested incrementally.

## 18. Persistence

Persistent application data includes:

- Paired device identities.
- Encrypted pairing credentials.
- Per-device permission preferences.
- Device names.
- Session history.
- Application settings.

Sensitive credentials must be protected using Android Keystore.

## 19. User Interface

Main screens:

1. Dashboard.
2. Paired devices.
3. Add device / QR scanner.
4. Incoming connection request.
5. Permission review.
6. Remote screen.
7. Camera.
8. File transfer.
9. Messaging.
10. Session management.
11. Security settings.

The host must always be able to see and revoke active access.

## 20. Development Roadmap

Phase 1:
- Android project foundation.
- Modular Gradle structure.
- Application navigation.
- Device identity.

Phase 2:
- QR pairing.
- Secure credential storage.
- Persistent paired-device registry.

Phase 3:
- Permission management.
- Session management.
- Reconnection after reboot.

Phase 4:
- WebRTC signaling.
- Remote screen sharing.

Phase 5:
- Camera streaming.
- File transfer.
- Messaging and clipboard.

Phase 6:
- Authorized remote input.
- Notifications.
- Advanced session controls.

Phase 7:
- Security testing.
- Performance optimization.
- Compatibility testing across Android devices.

## 21. Development Principles

- Security before convenience.
- Explicit user consent.
- No hidden access.
- No permission bypass.
- Modular implementation.
- Backward-compatible protocol versioning.
- Incremental testing on Samsung Galaxy A24.
- Support for future Android devices.
