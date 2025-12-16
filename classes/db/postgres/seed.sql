-- Seed data for posts table

INSERT INTO posts (id, author, content, visibility, likes, created_at, updated_at)
VALUES
  ('11111111-1111-1111-1111-111111111111', 'alice', 'Hello, world! This is my first post.', 'PUBLIC', 3, NOW(), NOW()),
  ('22222222-2222-2222-2222-222222222222', 'bob', 'Studying ICT repository pattern today.', 'FRIENDS', 1, NOW(), NOW()),
  ('33333333-3333-3333-3333-333333333333', 'carol', 'Private note: This is is some prviate post.', 'PRIVATE', 0, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;


