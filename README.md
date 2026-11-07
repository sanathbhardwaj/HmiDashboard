# HMI Dashboard – Android Home/Ride screen

Landscape Android HMI dashboard that shows **simulated** vehicle speed (km/h), motor status
(ON/OFF) and kill-switch status (Active/Inactive). It updates reactively from a coroutine/Flow data
source – no manual refresh. No hardware, CAN or backend is used.

Stack: Kotlin · Jetpack Compose (Material 3) · Coroutines · Flow/StateFlow · ViewModel.

## Build and run

Requirements: Android Studio Ladybug (2024.2) or newer, JDK 17+ (Android Studio's bundled JDK is fine),
Android SDK 35.

1. Open the project folder in Android Studio and let Gradle sync.
2. Create/choose an emulator (any phone or tablet image, API 26+).
3. Run the `app` configuration. The activity is locked to landscape (`sensorLandscape`), so the
   emulator rotates automatically.

Command line:

```bash
./gradlew :app:assembleDebug        # builds app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:installDebug         # installs on the running emulator/device
./gradlew :app:testDebugUnitTest    # runs the unit tests
```

## What you will see

The simulator loops forever, emitting one sample per second and holding each scenario for 3 samples:

| Scenario        | Speed  | Motor | Kill switch |
|-----------------|--------|-------|-------------|
| Parked          | 0      | OFF   | Inactive    |
| Ready           | 0      | ON    | Inactive    |
| Riding          | 25     | ON    | Inactive    |
| Accelerating    | 50     | ON    | Inactive    |
| Motor disabled  | 0      | OFF   | **Active**  |
| *Edge: invalid* | -15    | ON    | Inactive    |
| *Edge: dropout* | (no data for 5 s) | | |

The two edge-case steps are extra demonstrations of graceful handling (see below). Switch them
off with `SimulatedVehicleDataSource(includeEdgeCases = false)` in `MainActivity`.

UI layout (modelled on the reference dashboard, landscape):

- **Centre:** huge speed value with `km/h`, ride-state panel with hex logo above, and a glowing "dash"
  below whose strip fills outwards from the centre in proportion to speed (full at 100 km/h). The strip
  is orange normally, red when the motor is disabled, grey when data is unavailable.
- **Right:** kill-switch tile. ACTIVE = solid red tile with a white cross; INACTIVE = dark tile with a
  green tick; unknown = amber `--`.
- **Bottom-right:** motor pill. ON = green, OFF = grey, unknown = amber `--`.
- **Left edge:** vertical tab showing the current ride state (e.g. PARKED), like the reference "PARKING" slider.
- **Top:** phone/clock/LTE/signal status bar plus a LIVE / SIGNAL LOST indicator.

State is conveyed by text, shape and colour, never colour alone. Positions are proportional to the
screen size so it scales across landscape emulators.

## Architecture

```
data/    VehicleData (raw signals, nullable)
         VehicleDataSource (interface)
         SimulatedVehicleDataSource (cold Flow, coroutine delay loop)
domain/  VehicleUiStateMapper (pure: validate + derive ride state)
         toUiStates() (Flow operator: mapping + stale detection)
         VehicleViewModel (stateIn -> StateFlow<VehicleUiState>)
         VehicleUiState (immutable UI model)
ui/      Dashboard.kt (stateless DashboardScreen + canvas-drawn components), theme
```

Data flow: `SimulatedVehicleDataSource.data` → `toUiStates` → `StateFlow<VehicleUiState>` in the
`VehicleViewModel` → `collectAsStateWithLifecycle()` → stateless `DashboardScreen(state)`.

Design choices:
- **Unidirectional data flow.** The UI is a pure function of `VehicleUiState`; it owns no logic.
- **Raw vs. interpreted data.** The data source emits unvalidated values (nullable, possibly
  out of range, as a real bus would). Validation lives in one pure, unit-tested mapper.
- **Lifecycle-aware.** `stateIn(WhileSubscribed(5s))` pauses the simulation when the app is in
  the background and survives rotation without restarting it.
- **Testable time.** Staleness uses `transformLatest { ...; delay(timeout); emit(Stale) }`, so a new
  sample cancels the pending timer, and tests use virtual time (`runTest`).
- **Swappable source.** A real CAN/BLE implementation only needs to implement `VehicleDataSource`.
  Constructor injection is used by hand; no DI framework (optional in the brief).

## Edge cases handled

- **Invalid speed** (negative, NaN, infinite, > 200 km/h): speed shown as `--` with an
  "Invalid speed signal" note; motor and kill-switch status are still shown.
- **Missing signal** (a null field): that field shows `--` in amber, ride state becomes `NO DATA`.
- **Stale / no data** (no sample for 3 s, or none ever received): the top-right indicator shows
  `SIGNAL LOST` and every value becomes `--`, rather than leaving a stale number on screen.
  Recovers automatically on the next sample.
- **Source failure:** an exception in the data source is caught and degrades to the stale state; no crash.

## Assumptions and limitations

- Ride state is derived, not received: kill switch active → *Motor disabled*; motor OFF and speed 0 →
  *Parked*; motor ON and speed 0 → *Ready*; motor ON and below 40 km/h → *Riding*; 40 km/h or more →
  *Accelerating*. Thresholds are constants in `VehicleUiStateMapper`.
- Motor OFF does not force speed to 0: motor OFF with speed above 0 is shown as *Coasting* with the real speed.
  Kill switch active likewise keeps showing the real speed.
- Valid speed range is 0–200 km/h; the gauge arc is scaled to 120 km/h.
- The phone name/battery, LTE/signal, ODO (1324 km) and battery (100%) items are **static decorative
  placeholders** to match the reference; only the clock is live. Range, rapid-brake, SuperHold,
  cruise, indicators and warning lamps from the reference are not implemented (optional extras).
- The layout follows the reference closely but is not a pixel-exact copy (no 3D rendering, custom icons are simple vector shapes).
- Portrait is not supported (landscape-locked by design). No UI/instrumentation tests.

## Tests

`./gradlew :app:testDebugUnitTest` (JUnit4 + `kotlinx-coroutines-test`, virtual time):

- `VehicleUiStateMapperTest` – all five scenarios, coasting, invalid (negative/NaN/∞/huge), missing signals, rounding.
- `VehicleStateFlowTest` – state updates per sample, stale after timeout and recovery, no-data → stale, source exception → stale.
- `VehicleViewModelTest` – initial state; `StateFlow` follows the full scenario sequence; invalid speed then stale.
- `SimulatedVehicleDataSourceTest` – scenario order, one sample per tick, edge-case steps.
