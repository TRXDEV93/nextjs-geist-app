# Android AutoClick Application

A comprehensive Android auto-click application with no-code script builder interface.

## Features

- **Task Management**: Create, edit, delete, and organize automation tasks
- **No-Code Script Builder**: Visual interface for creating automation sequences
- **Multiple Action Types**: 
  - System actions (Home, Back, Recent Apps)
  - Touch gestures (Tap, Long Press, Swipe)
  - Text and image search with actions
  - Conditional logic and delays
- **Import/Export**: Save and share automation scripts as JSON
- **Real-time Execution**: Monitor task progress with detailed logs
- **Modern UI**: Material Design 3 with smooth animations

## Architecture

### Core Components

1. **Application Layer**
   - `AutoClickApplication.java` - Main application class with dependency injection
   - `PreferenceManager.java` - Centralized settings management

2. **UI Layer**
   - `MainActivity.java` - Task list and management
   - `TaskEditActivity.java` - Script builder interface
   - `ExecutionLogActivity.java` - Real-time execution monitoring
   - `StepDetailActivity.java` - Step configuration details

3. **Data Layer**
   - `TaskDatabase.java` - Room database configuration
   - `TaskDao.java` - Data access operations
   - `Task.java` & `Step.java` - Core data models

4. **Service Layer**
   - `AutoClickAccessibilityService.java` - Main accessibility service
   - `TaskExecutor.java` - Task execution engine
   - `AutoClickService.java` - Alternative execution service

5. **Utilities**
   - `AccessibilityUtils.java` - Accessibility helper methods
   - `FileUtils.java` - Import/export operations
   - `NotificationUtils.java` - System notifications
   - `PermissionUtils.java` - Runtime permission handling

## Setup Instructions

1. Enable Accessibility Service in Android Settings
2. Grant overlay permissions if required
3. Import sample tasks or create new ones
4. Configure step sequences using the visual builder
5. Execute tasks and monitor progress

## Supported Android Versions

- Minimum SDK: 21 (Android 5.0)
- Target SDK: 34 (Android 14)
- Compiled SDK: 34

## Permissions Required

- Accessibility Service (for automation)
- System Alert Window (for overlay UI)
- Storage (for import/export)

## Known Limitations

- Image recognition requires OpenCV integration (planned)
- Some system actions may require root access
- Performance varies by device capabilities
