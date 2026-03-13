/**
 * VectorScout26 QR Code Data Processor
 * Auto-processes scanned QR codes into separate tables:
 * - Match scouting: MatchSummary + ActionDetails
 * - Pit scouting: PitScout + AutonPath
 *
 * Setup:
 * 1. In Google Sheets, go to Extensions > Apps Script
 * 2. Paste this code
 * 3. Run setupSheets() once to create the sheets
 * 4. The onEdit trigger will auto-process QR codes scanned into QRInput sheet
 */

const MATCH_SHEET_NAME = "MatchSummary";
const ACTION_SHEET_NAME = "ActionDetails";
const PIT_SHEET_NAME = "PitScout";
const AUTON_PATH_SHEET_NAME = "AutonPath";
const INPUT_SHEET_NAME = "QRInput";
const INPUT_COLUMN = 1; // Column A

/**
 * Triggered automatically when a cell is edited
 */
function onEdit(e) {
  const sheet = e.source.getActiveSheet();
  const range = e.range;

  if (sheet.getName() !== INPUT_SHEET_NAME) return;
  if (range.getColumn() !== INPUT_COLUMN) return;
  if (range.getRow() < 2) return;

  const jsonString = e.value;
  if (!jsonString || !jsonString.startsWith("{")) return;

  try {
    processQRCode(jsonString);
    range.setValue("✓ Processed");
    SpreadsheetApp.getActiveSpreadsheet().toast("QR processed successfully!", "VectorScout");
  } catch (err) {
    range.setValue("✗ Error: " + err.message);
  }
}

/**
 * Process a QR code JSON string - routes to match or pit processor based on type
 */
function processQRCode(jsonString) {
  const data = JSON.parse(jsonString);

  // Route based on type
  if (data.type === "pit") {
    processPitQRCode(data);
  } else if (data.type === "foul") {
    processFoulQRCode(data);
  } else {
    processMatchQRCode(data);
  }
}

/**
 * Process match scouting QR code into MatchSummary and ActionDetails
 */
function processMatchQRCode(data) {
  const ss = SpreadsheetApp.getActiveSpreadsheet();
  const matchSheet = ss.getSheetByName(MATCH_SHEET_NAME);
  const actionSheet = ss.getSheetByName(ACTION_SHEET_NAME);

  if (!matchSheet || !actionSheet) {
    throw new Error("Missing sheets. Run setupSheets() first.");
  }

  // Check for duplicate (same event + match + team)
  const matchData = matchSheet.getDataRange().getValues();
  for (let i = 1; i < matchData.length; i++) {
    if (matchData[i][0] === data.e &&
        matchData[i][1] === data.m &&
        matchData[i][4] === data.t) {
      throw new Error("Duplicate: " + data.e + " M" + data.m + " T" + data.t);
    }
  }

  // Write to Match Summary (Table A)
  matchSheet.appendRow([
    data.e,                    // event
    data.m,                    // matchNumber
    data.rd,                   // robotDesignation
    data.sn,                   // scoutName
    data.t,                    // teamNumber
    data.sp,                   // startPosition
    data.l,                    // loaded
    data.ns || false           // noShow
  ]);

  // Write to Action Details (Table B) - one row per action
  const actions = data.a || [];
  actions.forEach((action, index) => {
    const qd = action.qd ? parseQualData(action.qd) : {};

    actionSheet.appendRow([
      data.e,                                    // event
      data.m,                                    // matchNumber
      data.t,                                    // teamNumber
      index + 1,                                 // actionNumber (sequence)
      action.p,                                  // phase
      action.at,                                 // actionType
      action.d,                                  // duration (ms)
      // Shoot data
      qd.location || "",
      // Load data
      qd.loadLocation || "",
      // Ferry data
      qd.ferryType || "",
      qd.ferryDelivery || "",
      // Climb data
      qd.result || "",
      // Defense data
      qd.types ? qd.types.join(", ") : "",
      qd.targetRobot || "",
      // Foul data
      qd.type || "",
      // Damaged data
      qd.components ? qd.components.join(", ") : ""
    ]);
  });
}

/**
 * Process pit scouting QR code into PitScout and AutonPath tables
 */
function processPitQRCode(data) {
  const ss = SpreadsheetApp.getActiveSpreadsheet();
  const pitSheet = ss.getSheetByName(PIT_SHEET_NAME);
  const pathSheet = ss.getSheetByName(AUTON_PATH_SHEET_NAME);

  if (!pitSheet || !pathSheet) {
    throw new Error("Missing pit sheets. Run setupSheets() first.");
  }

  const pit = data.pit;
  const paths = data.paths || [];

  // Check for duplicate (same event + team)
  const pitData = pitSheet.getDataRange().getValues();
  for (let i = 1; i < pitData.length; i++) {
    if (pitData[i][0] === pit.e && pitData[i][1] === pit.t) {
      throw new Error("Duplicate pit scout: " + pit.e + " Team " + pit.t);
    }
  }

  // Write to PitScout table
  pitSheet.appendRow([
    pit.e,                     // event
    pit.t,                     // teamNumber
    pit.dt,                    // drivetrainType
    pit.pr,                    // preferredRole
    pit.pp                     // preferredPath
  ]);

  // Write to AutonPath table - one row per path
  paths.forEach((path) => {
    pathSheet.appendRow([
      pit.e,                   // event (for filtering)
      path.t,                  // teamNumber
      path.n,                  // pathName (A1, A2, etc.)
      path.p                   // pathText (e.g., "3 > Load Depot > Score H")
    ]);
  });
}

/**
 * Process foul scouting QR code into ActionDetails table.
 * One row per individual foul action (5 = minor, 15 = major).
 */
function processFoulQRCode(data) {
  const ss = SpreadsheetApp.getActiveSpreadsheet();
  const actionSheet = ss.getSheetByName(ACTION_SHEET_NAME);

  if (!actionSheet) {
    throw new Error("Missing ActionDetails sheet. Run setupSheets() first.");
  }

  // Check for duplicate (same event + match already in ActionDetails as a Foul row)
  const existingData = actionSheet.getDataRange().getValues();
  for (let i = 1; i < existingData.length; i++) {
    if (existingData[i][0] === data.e &&
        existingData[i][1] === data.m &&
        existingData[i][5] === "Foul") {
      throw new Error("Duplicate foul scout: " + data.e + " M" + data.m);
    }
  }

  const actions = data.actions || [];
  actions.forEach((action, index) => {
      actionSheet.appendRow([
      data.e,         // event
      data.m,         // matchNumber
      action.t,       // teamNumber
      index + 1,      // actionNumber
      "",             // phase
      "Foul",         // actionType
      0,              // durationMs
      "",             // shootLocation
      "",             // loadLocation
      "",             // ferryType
      "",             // ferryDelivery
      "",             // climbResult
      "",             // defenseTypes
      "",             // targetRobot
      action.pts,     // foulPoints (5 = minor, 15 = major)
      ""              // damagedComponents
    ]);
  });
}

function parseQualData(qdString) {
  try {
    return JSON.parse(qdString);
  } catch (e) {
    return {};
  }
}

/**
 * Run once to create sheets with headers
 */
function setupSheets() {
  const ss = SpreadsheetApp.getActiveSpreadsheet();

  // Match Summary sheet
  let matchSheet = ss.getSheetByName(MATCH_SHEET_NAME);
  if (!matchSheet) matchSheet = ss.insertSheet(MATCH_SHEET_NAME);
  matchSheet.getRange(1, 1, 1, 8).setValues([[
    "Event", "Match", "Robot", "Scout", "Team", "StartPos", "Loaded", "NoShow"
  ]]).setFontWeight("bold");

  // Action Details sheet - one row per action
  let actionSheet = ss.getSheetByName(ACTION_SHEET_NAME);
  if (!actionSheet) actionSheet = ss.insertSheet(ACTION_SHEET_NAME);
  actionSheet.getRange(1, 1, 1, 16).setValues([[
    "Event", "Match", "Team", "ActionNum", "Phase", "ActionType", "DurationMs",
    "ShootLocation",
    "LoadLocation",
    "FerryType", "FerryDelivery",
    "ClimbResult",
    "DefenseTypes", "TargetRobot",
    "FoulPoints",
    "DamagedComponents"
  ]]).setFontWeight("bold");

  // PitScout sheet - one row per team
  let pitSheet = ss.getSheetByName(PIT_SHEET_NAME);
  if (!pitSheet) pitSheet = ss.insertSheet(PIT_SHEET_NAME);
  pitSheet.getRange(1, 1, 1, 5).setValues([[
    "Event", "Team", "Drivetrain", "PreferredRole", "PreferredPath"
  ]]).setFontWeight("bold");

  // AutonPath sheet - one row per auto path
  let pathSheet = ss.getSheetByName(AUTON_PATH_SHEET_NAME);
  if (!pathSheet) pathSheet = ss.insertSheet(AUTON_PATH_SHEET_NAME);
  pathSheet.getRange(1, 1, 1, 4).setValues([[
    "Event", "Team", "PathName", "PathText"
  ]]).setFontWeight("bold");
  pathSheet.setColumnWidth(4, 400); // Make PathText column wider

  // QR Input sheet
  let inputSheet = ss.getSheetByName(INPUT_SHEET_NAME);
  if (!inputSheet) inputSheet = ss.insertSheet(INPUT_SHEET_NAME);
  inputSheet.getRange("A1").setValue("Scan QR codes below (click A2 first):");
  inputSheet.getRange("A1").setFontWeight("bold");
  inputSheet.setColumnWidth(1, 400);
  inputSheet.setActiveSelection("A2");

  SpreadsheetApp.getUi().alert("Setup complete! Click cell A2 on QRInput sheet and start scanning.");
}

function onOpen() {
  SpreadsheetApp.getUi()
    .createMenu("VectorScout")
    .addItem("Setup Sheets", "setupSheets")
    .addItem("Reprocess Pending QR Codes", "reprocessPending")
    .addToUi();
}

Add

function generateActionDetails() {
  const ss = SpreadsheetApp.getActiveSpreadsheet();
  let sheet = ss.getSheetByName("ActionDetails");
  if (!sheet) {
    sheet = ss.insertSheet("ActionDetails");
  } else {
    sheet.clearContents();
  }

  const headers = [
    "Event", "Match", "Team", "Phase", "ActionType", "DurationMs",
    "ShootLocation", "LoadLocation", "FerryType", "FerryDelivery",
    "ClimbResult", "DefenseTypes", "TargetRobot", "FoulType", "DamagedComponents"
  ];
  sheet.appendRow(headers);

  const EVENT = "2025misjo";
  const MATCH = 1;
  const TEAMS = [7256, 2611, 205, 8767, 4956, 567];

  const ALLIANCE_A = [7256, 2611, 205];
  const ALLIANCE_B = [8767, 4956, 567];

  const SHOOT_LOCATIONS  = ["Hub", "R1", "R2", "L1", "L2"];
  const LOAD_LOCATIONS   = ["Depot", "Outpost", "Alliance", "Neutral", "Opponent"];
  const FERRY_TYPES      = ["Dump", "Shoot"];
  const FERRY_DELIVERIES = ["Neutral", "Alliance", "Outpost"];
  const DEFENSE_TYPES    = ["Block", "Pin", "Altered Shot"];

  const climbUsed = {};
  TEAMS.forEach(t => climbUsed[t] = false);

  function pick(arr) { return arr[Math.floor(Math.random() * arr.length)]; }
  function randInt(min, max) { return Math.floor(Math.random() * (max - min + 1)) + min; }

  const allRows = [];
  const rowsPerTeam = Math.floor(200 / TEAMS.length);
  const extras = 200 - rowsPerTeam * TEAMS.length;

  TEAMS.forEach((team, idx) => {
    const count = rowsPerTeam + (idx < extras ? 1 : 0);
    for (let i = 0; i < count; i++) {
      const phase = Math.random() < 0.10 ? "AUTON" : "TELEOP";
      let actions;
      if (phase === "AUTON") {
        actions = climbUsed[team] ? ["Shoot", "Load"] : ["Shoot", "Load", "Climb"];
      } else {
        actions = climbUsed[team] ? ["Shoot", "Load", "Ferry", "Defense"] : ["Shoot", "Load", "Ferry", "Defense", "Climb"];
      }
      const action = pick(actions);
      if (action === "Climb") climbUsed[team] = true;

      const duration      = randInt(1000, 20000);
      const shootLocation = action === "Shoot"   ? pick(SHOOT_LOCATIONS)  : "";
      const loadLocation  = action === "Load"    ? pick(LOAD_LOCATIONS)   : "";
      const ferryType     = action === "Ferry"   ? pick(FERRY_TYPES)      : "";
      const ferryDelivery = action === "Ferry"   ? pick(FERRY_DELIVERIES) : "";
      const defenseTypes  = action === "Defense" ? pick(DEFENSE_TYPES)    : "";
      let climbResult = "";
      if (action === "Climb") {
        climbResult = phase === "AUTON" ? pick(["L1", "Fail"]) : pick(["L1", "L2", "L3", "Fail"]);
      }
      let targetRobot = "";
      if (action === "Defense") {
        const opposing = ALLIANCE_A.includes(team) ? ALLIANCE_B : ALLIANCE_A;
        targetRobot = pick(opposing);
      }
      allRows.push([
        EVENT, MATCH, team, phase, action, duration,
        shootLocation, loadLocation, ferryType, ferryDelivery,
        climbResult, defenseTypes, targetRobot, "", ""
      ]);
    }
  });

  for (let i = allRows.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [allRows[i], allRows[j]] = [allRows[j], allRows[i]];
  }

  sheet.getRange(2, 1, allRows.length, headers.length).setValues(allRows);
  SpreadsheetApp.getUi().alert(`Done! ${allRows.length} rows written to ActionDetails.`);
}
