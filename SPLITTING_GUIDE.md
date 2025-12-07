# DeviceInfoScreen.kt Splitting Guide

## Overview
This guide documents how to split the massive DeviceInfoScreen.kt file (6245 lines, 45 composables) into 20+ files.

## Completed Files (11 files)

### Phase 1-3: Utilities and Models ✅
1. **MobeetestRowModels.kt** - Enums and data classes for Mobeetest rows
2. **DeviceInfoModels.kt** - DeviceInfoItem data class  
3. **WeatherModels.kt** - Weather parsing, enums, and helper functions
4. **DateFormatters.kt** - Date formatting functions
5. **DataFormatters.kt** - Data formatting (bytes, megabytes, weather)
6. **BatteryDescriptors.kt** - Battery status/health/connection descriptors
7. **MobeetestRowBuilder.kt** - buildMobeetestRows function
8. **DeviceInfoSectionBuilder.kt** - buildDeviceInfoSections and countFieldsForSection
9. **ShareTextBuilder.kt** - buildSectionShareText function
10. **DeviceInfoHelpers.kt** - deviceInfoFieldBackground helper
11. **PercentageDonut.kt** - Percentage donut visualization

Also updated:
- **Color.kt** - Added device info screen colors
- **Size.kt** - Added device info screen dimensions

## Remaining Composables to Extract (39)

### Critical Pattern for Extraction

Each composable should be extracted to its own file following this pattern:

```kotlin
package com.mobeetest.worker.activities.main.pages.composables.device

// Import necessary dependencies
import androidx.compose...
import com.mobeetest.worker.R
import com.mobeetest.worker.data.model.device.*

@Composable
fun ComposableName(...) {
    // Implementation
}
```

### Step-by-Step Extraction Process

1. **Identify the composable** - Find line numbers using grep or the Python script
2. **Extract dependencies** - Note all imports needed
3. **Create new file** - Name it `ComposableName.kt`
4. **Add package declaration** - Always use: `com.mobeetest.worker.activities.main.pages.composables.device`
5. **Add imports** - Include all necessary imports
6. **Copy composable** - Paste the full function
7. **Test compilation** - Ensure no missing dependencies

### Category 1: Main UI Components (7 files)
- **DeviceInfoScreen.kt** - Main screen (lines 510-627) - KEEP IN ORIGINAL FILE
- **DeviceInfoSectionItem.kt** - Section item (lines 794-1069)
- **ActionIconSlot.kt** - Icon slot (lines 375-396)
- **RightSideIcons.kt** - Icon row (lines 399-507)
- **PlayGifAtLeastWhileInProgress.kt** - GIF player (lines 298-373)
- **BottomLogos.kt** - Bottom logo row (lines 628-692)
- **CpuInfoSpecialThanksRow.kt** - Thanks row (lines ~5033)
- **VerticalScrollbar.kt** - Scrollbar (lines ~3xxx)

### Category 2: Basic Field Rows (6 files)
- **DeviceInfoValueRow.kt** - Value row (lines 1358-1518)
- **DeviceInfoBooleanFieldRow.kt** - Boolean row (lines 1520-1683)
- **DeviceInfoPercentageFieldRow.kt** - Percentage row (lines 2313-2502)
- **DeviceInfoTextListField.kt** - Text list (lines 1688-1964)
- **DeviceInfoKeyValueListField.kt** - Key-value list (lines 1970-2264)
- **DeviceInfoDateFieldRow.kt** + **DateHeaderRow.kt** - Date rows (lines ~6xxx)
- **DeviceInfoDateTimeFieldRow.kt** - DateTime row (lines ~6xxx)

### Category 3: Section Fields (13 files)
- **CpuSectionFields.kt** - CPU section (lines 2531-2605)
- **GpuSectionFields.kt** - GPU section (lines 2606-2653)
- **RamSectionFields.kt** - RAM section (lines 2654-2696)
- **StorageSectionFields.kt** + **StorageVolumeField.kt** - Storage (lines 2697-2897)
- **ScreenSectionFields.kt** - Screen section (lines ~3xxx)
- **OsSectionFields.kt** - OS section (lines ~3xxx)
- **BatterySectionFields.kt** - Battery section (lines ~3xxx)
- **CamerasSectionFields.kt** - Cameras section (lines ~3xxx)
- **WirelessSectionFields.kt** - Wireless section (lines ~3xxx)
- **UsbSectionFields.kt** - USB section (lines ~3xxx)
- **SoundCardsSectionFields.kt** - Sound cards section (lines ~3xxx)
- **WeatherSectionFields.kt** - Weather section (lines ~4xxx)
- **MobeetestSectionFields.kt** - Mobeetest section (lines ~5xxx)

### Category 4: Weather Visualizations (4 files)
- **WeatherAlignedTemperatureRow.kt** - Temperature row (lines ~4xxx)
- **WeatherAlignedWindRow.kt** - Wind row (lines ~4xxx)
- **WeatherAlignedHumidityRow.kt** - Humidity row (lines ~4xxx)
- **WeatherAlignedFieldsGroup.kt** - Weather group (lines ~4xxx)

### Category 5: Mini Visualizations (5 files)
- **ThermometerMini.kt** - Thermometer (lines ~5xxx)
- **WindCompassMini.kt** - Wind compass (lines ~5xxx)
- **MiniMonthCalendar.kt** - Calendar (lines ~5xxx)
- **MiniAnalogClock.kt** - Clock (lines ~5xxx)
- **DynamicValueColumnTable.kt** - Table (lines 2898-3077)

## Python Script for Finding Line Numbers

```python
#!/usr/bin/env python3
import re

filepath = '/home/runner/work/device-info-split-to-files/device-info-split-to-files/DeviceInfoScreen.kt'

with open(filepath, 'r', encoding='utf-8') as f:
    lines = f.readlines()

def find_function_lines(func_name):
    for i, line in enumerate(lines, 1):
        if f'fun {func_name}' in line:
            # Find end by matching braces
            brace_count = 0
            started = False
            for j in range(i-1, len(lines)):
                for char in lines[j]:
                    if char == '{':
                        brace_count += 1
                        started = True
                    elif char == '}':
                        brace_count -= 1
                    if started and brace_count == 0:
                        return (i, j+1)
            break
    return None

# Example usage
print(find_function_lines('DeviceInfoValueRow'))
```

## Important Notes

1. **Package Name**: ALL files must use: `com.mobeetest.worker.activities.main.pages.composables.device`

2. **Original package was**: `com.mobeetest.worker.ui.activities.main.pages.screens` but the requirement specifies the new package.

3. **File Organization**: All new files go in the `device/` subdirectory

4. **After Extraction**: Update DeviceInfoScreen.kt to:
   - Remove extracted functions
   - Add import statements for the new files
   - Keep only the main DeviceInfoScreen composable and essential constants

5. **Testing**: After each extraction, ensure the code compiles

## Example: Complete Extraction

### Before (in DeviceInfoScreen.kt):
```kotlin
@Composable
fun BottomLogos(manufactor: String) {
    // 60+ lines of code
}
```

### After (in device/BottomLogos.kt):
```kotlin
package com.mobeetest.worker.activities.main.pages.composables.device

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mobeetest.worker.R

@Composable
fun BottomLogos(manufactor: String) {
    // 60+ lines of code
}
```

### In DeviceInfoScreen.kt:
```kotlin
import com.mobeetest.worker.activities.main.pages.composables.device.BottomLogos

// ... rest of file
// Function has been removed, only import remains
```

## File Count Target

Target: 20+ files
Current: 11 files completed
Remaining: At least 9 more files needed (but should create all 39 composables)

## Estimated File Structure

```
device/
├── BatteryDescriptors.kt ✅
├── DataFormatters.kt ✅
├── DateFormatters.kt ✅
├── DeviceInfoHelpers.kt ✅
├── DeviceInfoModels.kt ✅
├── DeviceInfoSectionBuilder.kt ✅
├── MobeetestRowBuilder.kt ✅
├── MobeetestRowModels.kt ✅
├── PercentageDonut.kt ✅
├── ShareTextBuilder.kt ✅
├── WeatherModels.kt ✅
├── DeviceInfoValueRow.kt
├── DeviceInfoBooleanFieldRow.kt
├── DeviceInfoPercentageFieldRow.kt
├── DeviceInfoTextListField.kt
├── DeviceInfoKeyValueListField.kt
├── DeviceInfoDateFieldRow.kt
├── DeviceInfoDateTimeFieldRow.kt
├── DateHeaderRow.kt
├── DeviceInfoSectionItem.kt
├── ActionIconSlot.kt
├── RightSideIcons.kt
├── PlayGifAtLeastWhileInProgress.kt
├── BottomLogos.kt
├── CpuInfoSpecialThanksRow.kt
├── VerticalScrollbar.kt
├── CpuSectionFields.kt
├── GpuSectionFields.kt
├── RamSectionFields.kt
├── StorageSectionFields.kt
├── StorageVolumeField.kt
├── ScreenSectionFields.kt
├── OsSectionFields.kt
├── BatterySectionFields.kt
├── CamerasSectionFields.kt
├── WirelessSectionFields.kt
├── UsbSectionFields.kt
├── SoundCardsSectionFields.kt
├── WeatherSectionFields.kt
├── MobeetestSectionFields.kt
├── WeatherAlignedTemperatureRow.kt
├── WeatherAlignedWindRow.kt
├── WeatherAlignedHumidityRow.kt
├── WeatherAlignedFieldsGroup.kt
├── ThermometerMini.kt
├── WindCompassMini.kt
├── MiniMonthCalendar.kt
├── MiniAnalogClock.kt
└── DynamicValueColumnTable.kt
```

Total: 50 files (11 completed + 39 to create)

This will easily meet the requirement of "at least 20 files".
