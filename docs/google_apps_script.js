/**
 * VectorScout26 QR Code Data Processor
 * Auto-processes scanned QR codes into Match Summary and Action Details tables
 * Each action is recorded as a separate row for location tracking
 *
 * Setup:
 * 1. In Google Sheets, go to Extensions > Apps Script
 * 2. Paste this code
 * 3. Run setupSheets() once to create the sheets
 * 4. The onEdit trigger will auto-process QR codes scanned into QRInput sheet
 */

const MATCH_SHEET_NAME = "MatchSummary";
const ACTION_SHEET_NAME = "ActionDetails";
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
 * Process a QR code JSON string and split into two tables
 */
function processQRCode(jsonString) {
  const ss = SpreadsheetApp.getActiveSpreadsheet();
  const matchSheet = ss.getSheetByName(MATCH_SHEET_NAME);
  const actionSheet = ss.getSheetByName(ACTION_SHEET_NAME);

  if (!matchSheet || !actionSheet) {
    throw new Error("Missing sheets. Run setupSheets() first.");
  }

  const data = JSON.parse(jsonString);

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
    "FoulType",
    "DamagedComponents"
  ]]).setFontWeight("bold");

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
    .addToUi();
}
