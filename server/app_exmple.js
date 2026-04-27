const express = require("express");
const mysql = require("mysql2/promise");
const cors = require("cors");

const app = express();

// ====== 按你的宝塔数据库信息修改这里 ======
const DB_CONFIG = {
  host: "127.0.0.1",
  port: 3306,
  user: "",
  password: "",
  database: "",
  charset: "utf8mb4",
};
// =======================================

const PORT = 8080;

app.use(cors());
app.use(express.json());

// 简单健康检查
app.get("/", (req, res) => {
  res.json({ ok: true, message: "AircraftWar server is running." });
});

// 初始化数据库连接池
const pool = mysql.createPool({
  ...DB_CONFIG,
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0,
});

// ---------- 排行榜表 ----------
async function initScoreTable() {
  const sql = `
    CREATE TABLE IF NOT EXISTS scores (
      id BIGINT PRIMARY KEY AUTO_INCREMENT,
      player_name VARCHAR(50) NOT NULL,
      score INT NOT NULL,
      date_time DATETIME NOT NULL,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      INDEX idx_score (score DESC),
      INDEX idx_date (date_time DESC),
      INDEX idx_player_name (player_name)
    )
  `;
  await pool.execute(sql);
  console.log("scores table ready.");
}

// ---------- 联机房间表 ----------
async function initMatchTables() {
  const sql = `
    CREATE TABLE IF NOT EXISTS match_rooms (
      room_id CHAR(6) PRIMARY KEY,
      host_player_id VARCHAR(64) NOT NULL,
      guest_player_id VARCHAR(64) DEFAULT NULL,
      started TINYINT(1) NOT NULL DEFAULT 0,

      host_score INT NOT NULL DEFAULT 0,
      guest_score INT NOT NULL DEFAULT 0,
      host_hp INT NOT NULL DEFAULT 0,
      guest_hp INT NOT NULL DEFAULT 0,

      host_finished TINYINT(1) NOT NULL DEFAULT 0,
      guest_finished TINYINT(1) NOT NULL DEFAULT 0,

      winner VARCHAR(16) DEFAULT NULL,  -- host/guest/draw
      result_ready TINYINT(1) NOT NULL DEFAULT 0,

      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      INDEX idx_host_player (host_player_id),
      INDEX idx_guest_player (guest_player_id),
      INDEX idx_created_at (created_at)
    )
  `;
  await pool.execute(sql);
  console.log("match_rooms table ready.");
}

// ---------- 工具函数 ----------
function randomRoomId() {
  return String(Math.floor(Math.random() * 1000000)).padStart(6, "0");
}

async function generateUniqueRoomId(maxRetry = 30) {
  for (let i = 0; i < maxRetry; i++) {
    const roomId = randomRoomId();
    const [rows] = await pool.execute(
      "SELECT room_id FROM match_rooms WHERE room_id = ? LIMIT 1",
      [roomId]
    );
    if (rows.length === 0) {
      return roomId;
    }
  }
  throw new Error("Unable to generate unique room id.");
}

function clampNonNegativeInt(v) {
  const n = parseInt(v, 10);
  if (Number.isNaN(n) || n < 0) return 0;
  return n;
}

function calcWinnerByRoom(room) {
  const hs = room.host_score || 0;
  const gs = room.guest_score || 0;
  const hh = room.host_hp || 0;
  const gh = room.guest_hp || 0;

  if (hs > gs) return "host";
  if (gs > hs) return "guest";

  // 分数相同，用血量做第二判定
  if (hh > gh) return "host";
  if (gh > hh) return "guest";

  return "draw";
}

async function tryFinalizeResult(roomId) {
  const [rows] = await pool.execute(
    `SELECT host_finished, guest_finished, host_score, guest_score, host_hp, guest_hp
     FROM match_rooms
     WHERE room_id = ?
     LIMIT 1`,
    [roomId]
  );
  if (rows.length === 0) return;

  const room = rows[0];
  const bothFinished = !!room.host_finished && !!room.guest_finished;
  if (!bothFinished) return;

  const winner = calcWinnerByRoom(room);
  await pool.execute(
    "UPDATE match_rooms SET result_ready = 1, winner = ? WHERE room_id = ?",
    [winner, roomId]
  );
}

// -------------------- 排行榜接口 --------------------

// 提交分数
app.post("/api/score/submit", async (req, res) => {
  try {
    const { player_name, score, date_time } = req.body;

    if (
      typeof player_name !== "string" ||
      player_name.trim().length === 0 ||
      !Number.isInteger(score) ||
      score < 0 ||
      typeof date_time !== "string" ||
      date_time.trim().length === 0
    ) {
      return res.status(400).json({
        success: false,
        message: "Invalid params. Need player_name(string), score(int>=0), date_time(string).",
      });
    }

    const safePlayerName = player_name.trim().slice(0, 50);

    const insertSql = `
      INSERT INTO scores (player_name, score, date_time)
      VALUES (?, ?, ?)
    `;
    await pool.execute(insertSql, [safePlayerName, score, date_time.trim()]);

    return res.json({ success: true, message: "Score submitted." });
  } catch (err) {
    console.error("submit error:", err);
    return res.status(500).json({ success: false, message: "Server error." });
  }
});

// 获取前100排行榜
app.get("/api/score/top100", async (req, res) => {
  try {
    const querySql = `
      SELECT player_name, score, DATE_FORMAT(date_time, '%Y-%m-%d %H:%i:%s') AS date_time
      FROM scores
      ORDER BY score DESC, date_time ASC
      LIMIT 100
    `;
    const [rows] = await pool.execute(querySql);
    return res.json(rows);
  } catch (err) {
    console.error("top100 error:", err);
    return res.status(500).json({ success: false, message: "Server error." });
  }
});

// 查找分数（支持姓名模糊、最小分、分页）
// GET /api/score/search?player_name=张&min_score=100&page=1&page_size=20
app.get("/api/score/search", async (req, res) => {
  try {
    const playerName =
      typeof req.query.player_name === "string" ? req.query.player_name.trim() : "";
    const minScoreRaw = req.query.min_score;
    const page = Math.max(parseInt(req.query.page || "1", 10), 1);
    const pageSize = Math.min(Math.max(parseInt(req.query.page_size || "20", 10), 1), 100);
    const offset = (page - 1) * pageSize;

    let where = "WHERE 1=1";
    const params = [];
    const countParams = [];

    if (playerName.length > 0) {
      where += " AND player_name LIKE ?";
      const likeValue = `%${playerName}%`;
      params.push(likeValue);
      countParams.push(likeValue);
    }

    if (minScoreRaw !== undefined && minScoreRaw !== "") {
      const minScore = parseInt(minScoreRaw, 10);
      if (Number.isNaN(minScore) || minScore < 0) {
        return res
          .status(400)
          .json({ success: false, message: "min_score must be a non-negative integer." });
      }
      where += " AND score >= ?";
      params.push(minScore);
      countParams.push(minScore);
    }

    const countSql = `SELECT COUNT(*) AS total FROM scores ${where}`;
    const [countRows] = await pool.execute(countSql, countParams);
    const total = countRows[0].total;

    const dataSql = `
      SELECT id, player_name, score, DATE_FORMAT(date_time, '%Y-%m-%d %H:%i:%s') AS date_time
      FROM scores
      ${where}
      ORDER BY score DESC, date_time ASC
      LIMIT ? OFFSET ?
    `;
    params.push(pageSize, offset);
    const [rows] = await pool.execute(dataSql, params);

    return res.json({
      success: true,
      data: rows,
      pagination: {
        page,
        page_size: pageSize,
        total,
        total_pages: Math.ceil(total / pageSize),
      },
    });
  } catch (err) {
    console.error("search error:", err);
    return res.status(500).json({ success: false, message: "Server error." });
  }
});

// 删除分数（按 id）
// DELETE /api/score/delete/123
app.delete("/api/score/delete/:id", async (req, res) => {
  try {
    const id = parseInt(req.params.id, 10);
    if (Number.isNaN(id) || id <= 0) {
      return res.status(400).json({ success: false, message: "id must be a positive integer." });
    }

    const [result] = await pool.execute("DELETE FROM scores WHERE id = ?", [id]);

    if (result.affectedRows === 0) {
      return res.status(404).json({ success: false, message: "Record not found." });
    }

    return res.json({ success: true, message: "Record deleted by id." });
  } catch (err) {
    console.error("delete by id error:", err);
    return res.status(500).json({ success: false, message: "Server error." });
  }
});

// 删除分数（按 姓名+分数+时间 精确匹配）
// DELETE /api/score/delete
app.delete("/api/score/delete", async (req, res) => {
  try {
    const { player_name, score, date_time } = req.body;

    if (
      typeof player_name !== "string" ||
      player_name.trim().length === 0 ||
      !Number.isInteger(score) ||
      score < 0 ||
      typeof date_time !== "string" ||
      date_time.trim().length === 0
    ) {
      return res.status(400).json({
        success: false,
        message: "Invalid params. Need player_name(string), score(int>=0), date_time(string).",
      });
    }

    const [result] = await pool.execute(
      "DELETE FROM scores WHERE player_name = ? AND score = ? AND date_time = ?",
      [player_name.trim().slice(0, 50), score, date_time.trim()]
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({ success: false, message: "Record not found." });
    }

    return res.json({
      success: true,
      message: "Record deleted by player_name + score + date_time.",
      deleted_count: result.affectedRows,
    });
  } catch (err) {
    console.error("delete by condition error:", err);
    return res.status(500).json({ success: false, message: "Server error." });
  }
});

// -------------------- 联机匹配接口 --------------------

// 创建房间
// POST /api/match/room/create
// body: { "playerId":"uuid" }
app.post("/api/match/room/create", async (req, res) => {
  try {
    const { playerId } = req.body;

    if (typeof playerId !== "string" || playerId.trim().length === 0) {
      return res.status(400).json({ success: false, message: "playerId required." });
    }

    const roomId = await generateUniqueRoomId();

    await pool.execute(
      "INSERT INTO match_rooms (room_id, host_player_id) VALUES (?, ?)",
      [roomId, playerId.trim()]
    );

    return res.json({ success: true, roomId });
  } catch (err) {
    console.error("create room error:", err);
    return res.status(500).json({ success: false, message: "Server error." });
  }
});

// 加入房间
// POST /api/match/room/join
// body: { "roomId":"123456", "playerId":"uuid" }
app.post("/api/match/room/join", async (req, res) => {
  try {
    const { roomId, playerId } = req.body;

    if (
      typeof roomId !== "string" ||
      !/^\d{6}$/.test(roomId) ||
      typeof playerId !== "string" ||
      playerId.trim().length === 0
    ) {
      return res.status(400).json({ success: false, message: "Invalid params." });
    }

    const [rows] = await pool.execute(
      "SELECT room_id, host_player_id, guest_player_id FROM match_rooms WHERE room_id = ? LIMIT 1",
      [roomId]
    );

    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: "Room not found." });
    }

    const room = rows[0];
    const pid = playerId.trim();

    if (room.host_player_id === pid) {
      return res.json({ success: true, message: "Host already in room." });
    }

    if (room.guest_player_id && room.guest_player_id !== pid) {
      return res.status(409).json({ success: false, message: "Room is full." });
    }

    if (!room.guest_player_id) {
      await pool.execute(
        "UPDATE match_rooms SET guest_player_id = ? WHERE room_id = ?",
        [pid, roomId]
      );
    }

    return res.json({ success: true });
  } catch (err) {
    console.error("join room error:", err);
    return res.status(500).json({ success: false, message: "Server error." });
  }
});

// 查询房间状态
// GET /api/match/room/status?room_id=123456&player_id=uuid
app.get("/api/match/room/status", async (req, res) => {
  try {
    const roomId = String(req.query.room_id || "");
    const playerId = String(req.query.player_id || "").trim();

    if (!/^\d{6}$/.test(roomId) || playerId.length === 0) {
      return res.status(400).json({ success: false, message: "Invalid params." });
    }

    const [rows] = await pool.execute(
      "SELECT host_player_id, guest_player_id, started FROM match_rooms WHERE room_id = ? LIMIT 1",
      [roomId]
    );

    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: "Room not found." });
    }

    const room = rows[0];
    const host = room.host_player_id === playerId;
    const matched = !!room.guest_player_id;
    const started = !!room.started;

    return res.json({ matched, started, host });
  } catch (err) {
    console.error("room status error:", err);
    return res.status(500).json({ success: false, message: "Server error." });
  }
});

// 房主开始游戏
// POST /api/match/room/start
// body: { "roomId":"123456", "playerId":"host-uuid" }
app.post("/api/match/room/start", async (req, res) => {
  try {
    const { roomId, playerId } = req.body;
    const pid = typeof playerId === "string" ? playerId.trim() : "";

    if (!/^\d{6}$/.test(String(roomId || "")) || pid.length === 0) {
      return res.status(400).json({ success: false, message: "Invalid params." });
    }

    const [rows] = await pool.execute(
      "SELECT host_player_id, guest_player_id FROM match_rooms WHERE room_id = ? LIMIT 1",
      [roomId]
    );

    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: "Room not found." });
    }

    const room = rows[0];

    if (room.host_player_id !== pid) {
      return res.status(403).json({ success: false, message: "Only host can start." });
    }

    if (!room.guest_player_id) {
      return res.status(409).json({ success: false, message: "Not matched yet." });
    }

    await pool.execute(
      `UPDATE match_rooms
       SET started = 1,
           host_finished = 0,
           guest_finished = 0,
           result_ready = 0,
           winner = NULL,
           host_score = 0,
           guest_score = 0,
           host_hp = 0,
           guest_hp = 0
       WHERE room_id = ?`,
      [roomId]
    );

    return res.json({ success: true });
  } catch (err) {
    console.error("start match error:", err);
    return res.status(500).json({ success: false, message: "Server error." });
  }
});

// 实时同步（分数+血量+结束状态）
// POST /api/match/score/sync
// body: { roomId, playerId, myScore, myHp, finished }
app.post("/api/match/score/sync", async (req, res) => {
  try {
    const { roomId, playerId, myScore, myHp, finished } = req.body;
    const pid = typeof playerId === "string" ? playerId.trim() : "";

    if (!/^\d{6}$/.test(String(roomId || "")) || pid.length === 0) {
      return res.status(400).json({ success: false, message: "Invalid params." });
    }

    const safeScore = clampNonNegativeInt(myScore);
    const safeHp = clampNonNegativeInt(myHp);
    const safeFinished = !!finished;

    const [rows] = await pool.execute(
      `SELECT host_player_id, guest_player_id, host_score, guest_score, host_hp, guest_hp,
              host_finished, guest_finished, winner, result_ready
       FROM match_rooms
       WHERE room_id = ?
       LIMIT 1`,
      [roomId]
    );

    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: "Room not found." });
    }

    const room = rows[0];

    if (room.host_player_id === pid) {
      await pool.execute(
        "UPDATE match_rooms SET host_score = ?, host_hp = ?, host_finished = CASE WHEN host_finished = 1 OR ? = 1 THEN 1 ELSE 0 END WHERE room_id = ?",
        [safeScore, safeHp, safeFinished ? 1 : 0, roomId]
      );
    } else if (room.guest_player_id === pid) {
      await pool.execute(
        "UPDATE match_rooms SET guest_score = ?, guest_hp = ?, guest_finished = CASE WHEN guest_finished = 1 OR ? = 1 THEN 1 ELSE 0 END WHERE room_id = ?",
        [safeScore, safeHp, safeFinished ? 1 : 0, roomId]
      );
    } else {
      return res.status(403).json({ success: false, message: "Player not in room." });
    }

    await tryFinalizeResult(roomId);

    const [rows2] = await pool.execute(
      `SELECT host_player_id, guest_player_id, host_score, guest_score, host_hp, guest_hp,
              host_finished, guest_finished, winner, result_ready
       FROM match_rooms
       WHERE room_id = ?
       LIMIT 1`,
      [roomId]
    );
    const latest = rows2[0];

    const isHost = latest.host_player_id === pid;
    const enemyScore = isHost ? latest.guest_score : latest.host_score;
    const enemyHp = isHost ? latest.guest_hp : latest.host_hp;
    const enemyFinished = isHost ? !!latest.guest_finished : !!latest.host_finished;
    const bothFinished = !!latest.host_finished && !!latest.guest_finished;

    return res.json({
      enemyScore: enemyScore || 0,
      enemyHp: enemyHp || 0,
      enemyFinished,
      bothFinished,
      winner: latest.winner || null,
    });
  } catch (err) {
    console.error("score sync error:", err);
    return res.status(500).json({ success: false, message: "Server error." });
  }
});

// 显式提交结束（一方结束后调用）
// POST /api/match/score/finish
// body: { roomId, playerId, myScore, myHp, finished:true }
app.post("/api/match/score/finish", async (req, res) => {
  try {
    const { roomId, playerId, myScore, myHp } = req.body;
    const pid = typeof playerId === "string" ? playerId.trim() : "";

    if (!/^\d{6}$/.test(String(roomId || "")) || pid.length === 0) {
      return res.status(400).json({ success: false, message: "Invalid params." });
    }

    const safeScore = clampNonNegativeInt(myScore);
    const safeHp = clampNonNegativeInt(myHp);

    const [rows] = await pool.execute(
      "SELECT host_player_id, guest_player_id FROM match_rooms WHERE room_id = ? LIMIT 1",
      [roomId]
    );

    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: "Room not found." });
    }

    const room = rows[0];

    if (room.host_player_id === pid) {
      await pool.execute(
        "UPDATE match_rooms SET host_score = ?, host_hp = ?, host_finished = 1 WHERE room_id = ?",
        [safeScore, safeHp, roomId]
      );
    } else if (room.guest_player_id === pid) {
      await pool.execute(
        "UPDATE match_rooms SET guest_score = ?, guest_hp = ?, guest_finished = 1 WHERE room_id = ?",
        [safeScore, safeHp, roomId]
      );
    } else {
      return res.status(403).json({ success: false, message: "Player not in room." });
    }

    await tryFinalizeResult(roomId);
    return res.json({ success: true });
  } catch (err) {
    console.error("finish error:", err);
    return res.status(500).json({ success: false, message: "Server error." });
  }
});

// 查询结果页状态
// GET /api/match/result/status?room_id=123456&player_id=uuid
app.get("/api/match/result/status", async (req, res) => {
  try {
    const roomId = String(req.query.room_id || "");
    const playerId = String(req.query.player_id || "").trim();

    if (!/^\d{6}$/.test(roomId) || playerId.length === 0) {
      return res.status(400).json({ success: false, message: "Invalid params." });
    }

    const [rows] = await pool.execute(
      `SELECT host_player_id, guest_player_id, host_score, guest_score,
              host_finished, guest_finished, winner, result_ready
       FROM match_rooms
       WHERE room_id = ?
       LIMIT 1`,
      [roomId]
    );

    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: "Room not found." });
    }

    const room = rows[0];
    const isHost = room.host_player_id === playerId;
    const isGuest = room.guest_player_id === playerId;

    if (!isHost && !isGuest) {
      return res.status(403).json({ success: false, message: "Player not in room." });
    }

    const myScore = isHost ? room.host_score : room.guest_score;
    const enemyScore = isHost ? room.guest_score : room.host_score;
    const bothFinished = !!room.host_finished && !!room.guest_finished;

    const youWin =
      room.winner === "draw"
        ? false
        : room.winner === "host"
        ? isHost
        : room.winner === "guest"
        ? isGuest
        : false;

    return res.json({
      bothFinished,
      myScore: myScore || 0,
      enemyScore: enemyScore || 0,
      winner: room.winner || null,
      youWin,
      resultReady: !!room.result_ready,
    });
  } catch (err) {
    console.error("result status error:", err);
    return res.status(500).json({ success: false, message: "Server error." });
  }
});

// （可选）清理老房间
app.post("/api/match/room/cleanup", async (req, res) => {
  try {
    const [result] = await pool.execute(
      "DELETE FROM match_rooms WHERE created_at < (NOW() - INTERVAL 1 DAY)"
    );
    return res.json({ success: true, deleted: result.affectedRows || 0 });
  } catch (err) {
    console.error("cleanup error:", err);
    return res.status(500).json({ success: false, message: "Server error." });
  }
});

/**
 * @param {import("mysql2/promise").Pool} pool
 */
async function migrateMatchRoomsIfNeeded(pool) {
  const [tables] = await pool.execute(
    `SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'match_rooms'`
  );
  if (tables.length === 0) {
    console.log("migrateMatchRoomsIfNeeded: match_rooms 不存在，跳过（请先 initMatchTables）");
    return;
  }

  const [rows] = await pool.execute(
    `SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'match_rooms'`
  );
  const existing = new Set(rows.map((r) => r.COLUMN_NAME));

  /** @type {{ name: string; ddl: string }[]} */
  const migrations = [
    { name: "host_hp", ddl: "ADD COLUMN host_hp INT NOT NULL DEFAULT 0" },
    { name: "guest_hp", ddl: "ADD COLUMN guest_hp INT NOT NULL DEFAULT 0" },
    { name: "host_finished", ddl: "ADD COLUMN host_finished TINYINT(1) NOT NULL DEFAULT 0" },
    { name: "guest_finished", ddl: "ADD COLUMN guest_finished TINYINT(1) NOT NULL DEFAULT 0" },
    { name: "winner", ddl: "ADD COLUMN winner VARCHAR(16) DEFAULT NULL" },
    { name: "result_ready", ddl: "ADD COLUMN result_ready TINYINT(1) NOT NULL DEFAULT 0" },
  ];

  for (const m of migrations) {
    if (existing.has(m.name)) {
      continue;
    }
    await pool.execute(`ALTER TABLE match_rooms ${m.ddl}`);
    console.log(`match_rooms migrate: added column ${m.name}`);
  }

  console.log("match_rooms migrate: done.");
}

module.exports = { migrateMatchRoomsIfNeeded };


// 启动服务
(async () => {
  try {
    await initScoreTable();
    await initMatchTables();
    await migrateMatchRoomsIfNeeded(pool);

    app.listen(PORT, "0.0.0.0", () => {
      console.log(`Server started at http://0.0.0.0:${PORT}`);
    });
  } catch (err) {
    console.error("startup error:", err);
    process.exit(1);
  }
})();