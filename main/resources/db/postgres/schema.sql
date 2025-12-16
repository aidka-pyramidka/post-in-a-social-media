-- PostgreSQL schema for "Post in a Social Media App"

CREATE TABLE IF NOT EXISTS posts (
  id UUID PRIMARY KEY,
  author VARCHAR(100) NOT NULL,
  content TEXT NOT NULL,
  visibility VARCHAR(16) NOT NULL CHECK (visibility IN ('PUBLIC', 'FRIENDS', 'PRIVATE')),
  likes INT NOT NULL DEFAULT 0 CHECK (likes >= 0),
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_author ON posts (author);


