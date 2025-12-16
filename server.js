const path = require("path");
const crypto = require("crypto");

require("dotenv").config({ path: path.join(__dirname, ".env") });

const express = require("express");
const { pool } = require("./db");

const app = express();
app.use(express.json());
app.use(express.static(path.join(__dirname, "public")));

function toItem(row) {
  return {
    id: row.id,
    author: row.author,
    content: row.content,
    visibility: row.visibility,
    likes: row.likes,
    createdAt: row.created_at instanceof Date ? row.created_at.toISOString() : row.created_at,
    updatedAt: row.updated_at instanceof Date ? row.updated_at.toISOString() : row.updated_at,
  };
}

function parseVisibility(raw, fallback = "PUBLIC") {
  const v = String(raw || "").trim().toUpperCase();
  if (["PUBLIC", "FRIENDS", "PRIVATE"].includes(v)) return v;
  return fallback;
}

// Required endpoints:
// GET /items
app.get("/items", async (_req, res) => {
  const { rows } = await pool.query(
    "SELECT id, author, content, visibility, likes, created_at, updated_at FROM posts ORDER BY created_at DESC"
  );
  res.json(rows.map(toItem));
});

// GET /items/:id
app.get("/items/:id", async (req, res) => {
  const id = req.params.id;
  const { rows } = await pool.query(
    "SELECT id, author, content, visibility, likes, created_at, updated_at FROM posts WHERE id = $1",
    [id]
  );
  if (rows.length === 0) return res.status(404).json({ error: "Not found" });
  res.json(toItem(rows[0]));
});

// POST /items
app.post("/items", async (req, res) => {
  const author = String(req.body.author || "").trim();
  const content = String(req.body.content || "").trim();
  const visibility = parseVisibility(req.body.visibility, "PUBLIC");
  const likes = Number.isFinite(req.body.likes) ? Number(req.body.likes) : Number(req.body.likes || 0);

  if (!author) return res.status(400).json({ error: "author is required" });
  if (!content) return res.status(400).json({ error: "content is required" });
  if (!Number.isInteger(likes) || likes < 0) return res.status(400).json({ error: "likes must be an integer >= 0" });

  const id = crypto.randomUUID();
  const now = new Date();

  const { rows } = await pool.query(
    "INSERT INTO posts (id, author, content, visibility, likes, created_at, updated_at) VALUES ($1,$2,$3,$4,$5,$6,$7) RETURNING *",
    [id, author, content, visibility, likes, now, now]
  );

  res.status(201).json(toItem(rows[0]));
});

// PUT /items/:id
app.put("/items/:id", async (req, res) => {
  const id = req.params.id;

  const existing = await pool.query("SELECT * FROM posts WHERE id = $1", [id]);
  if (existing.rows.length === 0) return res.status(404).json({ error: "Not found" });

  const old = existing.rows[0];
  const author = String(req.body.author ?? old.author).trim();
  const content = String(req.body.content ?? old.content).trim();
  const visibility = parseVisibility(req.body.visibility ?? old.visibility, old.visibility);
  const likesRaw = req.body.likes ?? old.likes;
  const likes = Number.isFinite(likesRaw) ? Number(likesRaw) : Number(likesRaw);

  if (!author) return res.status(400).json({ error: "author is required" });
  if (!content) return res.status(400).json({ error: "content is required" });
  if (!Number.isInteger(likes) || likes < 0) return res.status(400).json({ error: "likes must be an integer >= 0" });

  const now = new Date();
  const updated = await pool.query(
    "UPDATE posts SET author=$1, content=$2, visibility=$3, likes=$4, updated_at=$5 WHERE id=$6 RETURNING *",
    [author, content, visibility, likes, now, id]
  );

  res.json(toItem(updated.rows[0]));
});

// DELETE /items/:id
app.delete("/items/:id", async (req, res) => {
  const id = req.params.id;
  const r = await pool.query("DELETE FROM posts WHERE id = $1", [id]);
  if (r.rowCount === 0) return res.status(404).json({ error: "Not found" });
  res.status(204).send();
});

// Basic error handler
app.use((err, _req, res, _next) => {
  console.error(err);
  res.status(500).json({ error: "Internal server error" });
});

const port = process.env.PORT ? Number(process.env.PORT) : 3000;
app.listen(port, () => {
  console.log(`Server running on http://localhost:${port}`);
});


