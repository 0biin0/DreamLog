import express from 'express';
import cors from 'cors';
import cookieParser from 'cookie-parser';
import initSqlJs from 'sql.js';
import jwt from 'jsonwebtoken';
import bcrypt from 'bcryptjs';
import { v4 as uuidv4 } from 'uuid';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';
import { readFileSync, writeFileSync, existsSync } from 'fs';

const __dirname = dirname(fileURLToPath(import.meta.url));
const app = express();
const PORT = 8080;
const JWT_SECRET = 'dreamlog-dev-secret-key-at-least-256-bits-long-for-hmac';
const AT_EXPIRY = '15m';
const RT_DAYS = 7;
const DB_PATH = join(__dirname, 'dreamlog.db');

// --- sql.js DB ---
const SQL = await initSqlJs();
let db;
if (existsSync(DB_PATH)) {
  db = new SQL.Database(readFileSync(DB_PATH));
} else {
  db = new SQL.Database();
}
function save() { writeFileSync(DB_PATH, Buffer.from(db.export())); }

function run(sql, params = []) {
  db.run(sql, params);
  const stmt = db.prepare('SELECT last_insert_rowid() as id');
  stmt.step();
  const lastId = stmt.get()[0];
  stmt.free();
  save();
  return { lastInsertRowid: lastId };
}
function get(sql, params = []) {
  const stmt = db.prepare(sql);
  stmt.bind(params);
  if (stmt.step()) {
    const cols = stmt.getColumnNames();
    const vals = stmt.get();
    stmt.free();
    const row = {};
    cols.forEach((c, i) => row[c] = vals[i]);
    return row;
  }
  stmt.free();
  return undefined;
}
function all(sql, params = []) {
  const stmt = db.prepare(sql);
  stmt.bind(params);
  const rows = [];
  while (stmt.step()) {
    const cols = stmt.getColumnNames();
    const vals = stmt.get();
    const row = {};
    cols.forEach((c, i) => row[c] = vals[i]);
    rows.push(row);
  }
  stmt.free();
  return rows;
}

// --- Schema ---
db.run(`CREATE TABLE IF NOT EXISTS users (
  id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT NOT NULL UNIQUE, password TEXT NOT NULL,
  nickname TEXT NOT NULL, profile_image TEXT, role TEXT DEFAULT 'USER',
  created_at TEXT DEFAULT (datetime('now')), updated_at TEXT DEFAULT (datetime('now')), deleted_at TEXT
)`);
db.run(`CREATE TABLE IF NOT EXISTS refresh_tokens (
  id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, token TEXT NOT NULL UNIQUE,
  expires_at TEXT NOT NULL, created_at TEXT DEFAULT (datetime('now'))
)`);
db.run(`CREATE TABLE IF NOT EXISTS diaries (
  id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, title TEXT NOT NULL,
  content TEXT NOT NULL, unlock_date TEXT NOT NULL, edit_deadline TEXT NOT NULL,
  written_date TEXT NOT NULL, created_at TEXT DEFAULT (datetime('now')),
  updated_at TEXT DEFAULT (datetime('now')), deleted_at TEXT
)`);
db.run(`CREATE TABLE IF NOT EXISTS challenges (
  id INTEGER PRIMARY KEY AUTOINCREMENT, creator_id INTEGER NOT NULL, title TEXT NOT NULL,
  description TEXT, duration_days INTEGER NOT NULL, start_date TEXT NOT NULL, end_date TEXT NOT NULL,
  max_participants INTEGER DEFAULT 50, fail_threshold REAL DEFAULT 0.15,
  created_at TEXT DEFAULT (datetime('now')), updated_at TEXT DEFAULT (datetime('now'))
)`);
db.run(`CREATE TABLE IF NOT EXISTS challenge_participants (
  id INTEGER PRIMARY KEY AUTOINCREMENT, challenge_id INTEGER NOT NULL, user_id INTEGER NOT NULL,
  status TEXT DEFAULT 'ACTIVE', missed_count INTEGER DEFAULT 0,
  joined_at TEXT DEFAULT (datetime('now')), completed_at TEXT, UNIQUE(challenge_id, user_id)
)`);
db.run(`CREATE TABLE IF NOT EXISTS challenge_daily_logs (
  id INTEGER PRIMARY KEY AUTOINCREMENT, participant_id INTEGER NOT NULL,
  log_date TEXT NOT NULL, achieved INTEGER DEFAULT 0, checked_at TEXT,
  UNIQUE(participant_id, log_date)
)`);
db.run(`CREATE TABLE IF NOT EXISTS prompt_templates (
  id INTEGER PRIMARY KEY AUTOINCREMENT, category TEXT NOT NULL,
  content_ko TEXT NOT NULL, is_active INTEGER DEFAULT 1, created_at TEXT DEFAULT (datetime('now'))
)`);
save();

// Seed prompts
const cnt = get('SELECT COUNT(*) as cnt FROM prompt_templates');
if (cnt.cnt === 0) {
  const p = [
    ['MOTIVATION','1년 후의 나는 어떤 사람이 되어 있을까? 그 사람에게 지금의 내가 해주고 싶은 말을 적어보세요.'],
    ['MOTIVATION','지금 가장 이루고 싶은 목표를 미래의 나에게 약속하듯 적어보세요.'],
    ['MOTIVATION','포기하고 싶을 때 읽으면 힘이 될 말을 미래의 나에게 보내보세요.'],
    ['MOTIVATION','오늘 내가 한 작은 노력이 미래에 어떤 결과를 가져올까요?'],
    ['MOTIVATION','미래의 내가 "그때 시작하길 잘했다"고 말할 일은 무엇일까요?'],
    ['MOTIVATION','지금 도전하지 않으면 후회할 일은 무엇인가요?'],
    ['REFLECTION','오늘 하루를 돌아보며, 미래의 나에게 전하고 싶은 교훈을 적어보세요.'],
    ['REFLECTION','지금 고민하고 있는 것이 6개월 후에도 중요할까요?'],
    ['REFLECTION','최근에 실수한 일이 있다면, 미래의 내가 그것을 어떻게 기억하길 바라나요?'],
    ['REFLECTION','지금의 나를 한 단어로 표현한다면? 미래의 나는 어떤 단어가 되어 있을까요?'],
    ['REFLECTION','오늘 배운 가장 중요한 것은 무엇인가요?'],
    ['REFLECTION','지금 느끼는 감정을 솔직하게 적어보세요.'],
    ['GRATITUDE','오늘 감사한 세 가지를 미래의 나에게 알려주세요.'],
    ['GRATITUDE','지금 곁에 있는 소중한 사람에게 하고 싶은 말을 적어보세요.'],
    ['GRATITUDE','당연하다고 생각했지만 사실은 감사한 것은 무엇인가요?'],
    ['GRATITUDE','미래의 내가 지금의 나에게 감사할 일은 무엇일까요?'],
    ['GRATITUDE','오늘 나를 미소 짓게 한 작은 순간을 기록해보세요.'],
    ['GRATITUDE','힘들었지만 성장할 수 있었던 경험에 감사하며 적어보세요.'],
    ['DREAM','꿈을 이룬 미래의 하루를 상상하며 일기를 써보세요.'],
    ['DREAM','5년 후 이상적인 하루의 일과를 구체적으로 적어보세요.'],
    ['DREAM','돈과 시간이 무한하다면 가장 먼저 하고 싶은 일은 무엇인가요?'],
    ['DREAM','미래의 내가 살고 있는 곳, 하고 있는 일을 그려보세요.'],
    ['DREAM','10년 후의 나에게 이것만은 꼭 이루었으면 하는 것을 적어보세요.'],
    ['DREAM','어릴 때 꿈꾸던 미래와 지금 꿈꾸는 미래는 어떻게 다른가요?'],
    ['RANDOM','미래의 나에게 보내는 타임캡슐에 한 가지만 넣을 수 있다면?'],
    ['RANDOM','지금 듣고 있는 노래를 미래의 나에게 추천하며 이유를 적어보세요.'],
    ['RANDOM','오늘의 날씨처럼 미래의 내 마음은 어떤 날씨였으면 좋겠나요?'],
    ['RANDOM','미래의 나와 대화할 수 있다면 가장 먼저 물어보고 싶은 것은?'],
    ['RANDOM','지금 이 순간을 사진 한 장으로 찍는다면 어떻게 설명할까요?'],
    ['RANDOM','오늘 하루를 영화 제목으로 짓는다면?'],
  ];
  for (const [cat, txt] of p) run('INSERT INTO prompt_templates (category, content_ko) VALUES (?, ?)', [cat, txt]);
  console.log('Seeded 30 prompts');
}

// --- Middleware ---
app.use(cors({ origin: 'http://localhost:5173', credentials: true }));
app.use(express.json());
app.use(cookieParser());

const ok = d => ({ success: true, data: d, error: null });
const er = m => ({ success: false, data: null, error: m });
const today = () => new Date().toISOString().split('T')[0];
const now = () => new Date().toISOString().replace('T',' ').slice(0,19);

function auth(req, res, next) {
  const h = req.headers.authorization;
  if (!h?.startsWith('Bearer ')) return res.status(401).json(er('인증이 필요합니다.'));
  try { req.userId = jwt.verify(h.slice(7), JWT_SECRET).sub; next(); }
  catch { return res.status(401).json(er('유효하지 않은 토큰입니다.')); }
}

// === AUTH ===
app.post('/api/auth/signup', async (req, res) => {
  const { email, password, nickname } = req.body;
  if (!email || !password || !nickname) return res.status(400).json(er('필수 입력값이 누락되었습니다.'));
  if (get('SELECT id FROM users WHERE email = ?', [email])) return res.status(409).json(er('이미 사용 중인 이메일입니다.'));
  const hash = await bcrypt.hash(password, 10);
  const { lastInsertRowid: uid } = run('INSERT INTO users (email, password, nickname) VALUES (?, ?, ?)', [email, hash, nickname]);
  const accessToken = jwt.sign({ sub: uid }, JWT_SECRET, { expiresIn: AT_EXPIRY });
  const rt = uuidv4();
  run('INSERT INTO refresh_tokens (user_id, token, expires_at) VALUES (?, ?, ?)', [uid, rt, new Date(Date.now()+RT_DAYS*864e5).toISOString()]);
  res.cookie('refreshToken', rt, { httpOnly: true, maxAge: RT_DAYS*864e5, path: '/' });
  res.json(ok({ token: { accessToken, expiresIn: 900000 }, user: { id: uid, email, nickname, profileImage: null } }));
});

app.post('/api/auth/login', async (req, res) => {
  const { email, password } = req.body;
  const u = get('SELECT * FROM users WHERE email = ? AND deleted_at IS NULL', [email]);
  if (!u) return res.status(404).json(er('사용자를 찾을 수 없습니다.'));
  if (!(await bcrypt.compare(password, u.password))) return res.status(401).json(er('비밀번호가 일치하지 않습니다.'));
  const accessToken = jwt.sign({ sub: u.id }, JWT_SECRET, { expiresIn: AT_EXPIRY });
  const rt = uuidv4();
  run('INSERT INTO refresh_tokens (user_id, token, expires_at) VALUES (?, ?, ?)', [u.id, rt, new Date(Date.now()+RT_DAYS*864e5).toISOString()]);
  res.cookie('refreshToken', rt, { httpOnly: true, maxAge: RT_DAYS*864e5, path: '/' });
  res.json(ok({ token: { accessToken, expiresIn: 900000 }, user: { id: u.id, email: u.email, nickname: u.nickname, profileImage: u.profile_image } }));
});

app.post('/api/auth/refresh', (req, res) => {
  const rt = req.cookies?.refreshToken;
  if (!rt) return res.status(401).json(er('토큰 없음'));
  const row = get('SELECT * FROM refresh_tokens WHERE token = ?', [rt]);
  if (!row) return res.status(401).json(er('토큰 없음'));
  run('DELETE FROM refresh_tokens WHERE id = ?', [row.id]);
  if (new Date(row.expires_at) < new Date()) return res.status(401).json(er('만료됨'));
  const newRt = uuidv4();
  run('INSERT INTO refresh_tokens (user_id, token, expires_at) VALUES (?, ?, ?)', [row.user_id, newRt, new Date(Date.now()+RT_DAYS*864e5).toISOString()]);
  res.cookie('refreshToken', newRt, { httpOnly: true, maxAge: RT_DAYS*864e5, path: '/' });
  res.json(ok({ accessToken: jwt.sign({ sub: row.user_id }, JWT_SECRET, { expiresIn: AT_EXPIRY }), expiresIn: 900000 }));
});

app.post('/api/auth/logout', (req, res) => {
  const rt = req.cookies?.refreshToken;
  if (rt) run('DELETE FROM refresh_tokens WHERE token = ?', [rt]);
  res.clearCookie('refreshToken', { path: '/' });
  res.json(ok(null));
});

// === USERS ===
app.get('/api/users/me', auth, (req, res) => {
  const u = get('SELECT id, email, nickname, profile_image as profileImage FROM users WHERE id = ? AND deleted_at IS NULL', [req.userId]);
  u ? res.json(ok(u)) : res.status(404).json(er('사용자 없음'));
});

// === DIARIES ===
function diaryRes(d) {
  const locked = today() < d.unlock_date;
  const du = locked ? Math.ceil((new Date(d.unlock_date)-new Date(today()))/864e5) : 0;
  return { id:d.id, title:d.title, content: locked?null:d.content, unlockDate:d.unlock_date,
    writtenDate:d.written_date, editDeadline:d.edit_deadline, locked, daysUntilUnlock:du,
    editable: new Date()<new Date(d.edit_deadline), createdAt:d.created_at };
}

app.post('/api/diaries', auth, (req, res) => {
  const { title, content, unlockDate } = req.body;
  if (!title||!content||!unlockDate) return res.status(400).json(er('필수 입력값 누락'));
  const ed = new Date(Date.now()+24*36e5).toISOString().replace('T',' ').slice(0,19);
  const { lastInsertRowid: id } = run('INSERT INTO diaries (user_id,title,content,unlock_date,edit_deadline,written_date) VALUES (?,?,?,?,?,?)',
    [req.userId, title, content, unlockDate, ed, today()]);
  res.json(ok(diaryRes(get('SELECT * FROM diaries WHERE id=?',[id]))));
});

app.get('/api/diaries', auth, (req, res) => {
  const pg = parseInt(req.query.page)||0, sz = parseInt(req.query.size)||10;
  const rows = all('SELECT * FROM diaries WHERE user_id=? AND deleted_at IS NULL ORDER BY created_at DESC LIMIT ? OFFSET ?', [req.userId, sz, pg*sz]);
  const tot = get('SELECT COUNT(*) as c FROM diaries WHERE user_id=? AND deleted_at IS NULL', [req.userId]).c;
  const content = rows.map(d => { const r = diaryRes(d); r.content = null; return r; });
  res.json(ok({ content, totalPages: Math.ceil(tot/sz), totalElements: tot, number: pg, size: sz }));
});

app.get('/api/diaries/arrived', auth, (req, res) => {
  const rows = all('SELECT * FROM diaries WHERE user_id=? AND deleted_at IS NULL AND unlock_date<=? ORDER BY unlock_date DESC', [req.userId, today()]);
  res.json(ok(rows.map(diaryRes)));
});

app.get('/api/diaries/:id', auth, (req, res) => {
  const d = get('SELECT * FROM diaries WHERE id=? AND user_id=? AND deleted_at IS NULL', [req.params.id, req.userId]);
  d ? res.json(ok(diaryRes(d))) : res.status(404).json(er('일기를 찾을 수 없습니다.'));
});

app.patch('/api/diaries/:id', auth, (req, res) => {
  const d = get('SELECT * FROM diaries WHERE id=? AND user_id=? AND deleted_at IS NULL', [req.params.id, req.userId]);
  if (!d) return res.status(404).json(er('일기 없음'));
  if (new Date()>=new Date(d.edit_deadline)) return res.status(403).json(er('수정 시간 초과'));
  if (req.body.title) run('UPDATE diaries SET title=?,updated_at=? WHERE id=?', [req.body.title, now(), d.id]);
  if (req.body.content) run('UPDATE diaries SET content=?,updated_at=? WHERE id=?', [req.body.content, now(), d.id]);
  res.json(ok(diaryRes(get('SELECT * FROM diaries WHERE id=?', [d.id]))));
});

app.delete('/api/diaries/:id', auth, (req, res) => {
  const d = get('SELECT id FROM diaries WHERE id=? AND user_id=? AND deleted_at IS NULL', [req.params.id, req.userId]);
  if (!d) return res.status(404).json(er('일기 없음'));
  run('UPDATE diaries SET deleted_at=? WHERE id=?', [now(), d.id]);
  res.json(ok(null));
});

// === CHALLENGES ===
function chalRes(c, cnt) {
  const t = today();
  let st = 'UPCOMING'; if(t>=c.start_date&&t<=c.end_date) st='ACTIVE'; else if(t>c.end_date) st='ENDED';
  return { id:c.id, creatorId:c.creator_id, title:c.title, description:c.description,
    durationDays:c.duration_days, startDate:c.start_date, endDate:c.end_date,
    maxParticipants:c.max_participants, currentParticipants:cnt,
    failThreshold:c.fail_threshold, maxMissedDays:Math.floor(c.duration_days*c.fail_threshold), status:st };
}

app.post('/api/challenges', auth, (req, res) => {
  const { title, description, durationDays, startDate, maxParticipants, failThreshold } = req.body;
  if (!title||!durationDays||!startDate) return res.status(400).json(er('필수 입력값 누락'));
  const endDate = new Date(new Date(startDate).getTime()+(durationDays-1)*864e5).toISOString().split('T')[0];
  const { lastInsertRowid: cid } = run('INSERT INTO challenges (creator_id,title,description,duration_days,start_date,end_date,max_participants,fail_threshold) VALUES (?,?,?,?,?,?,?,?)',
    [req.userId, title, description||null, durationDays, startDate, endDate, maxParticipants||50, failThreshold||0.15]);
  run('INSERT INTO challenge_participants (challenge_id, user_id) VALUES (?,?)', [cid, req.userId]);
  res.json(ok(chalRes(get('SELECT * FROM challenges WHERE id=?',[cid]), 1)));
});

app.get('/api/challenges', auth, (req, res) => {
  const rows = all('SELECT * FROM challenges WHERE start_date>=? ORDER BY start_date', [today()]);
  res.json(ok(rows.map(c => chalRes(c, get('SELECT COUNT(*) as c FROM challenge_participants WHERE challenge_id=?',[c.id]).c))));
});

app.get('/api/challenges/my', auth, (req, res) => {
  const rows = all('SELECT c.* FROM challenges c JOIN challenge_participants cp ON c.id=cp.challenge_id WHERE cp.user_id=?', [req.userId]);
  res.json(ok(rows.map(c => chalRes(c, get('SELECT COUNT(*) as c FROM challenge_participants WHERE challenge_id=?',[c.id]).c))));
});

app.get('/api/challenges/:id', auth, (req, res) => {
  const c = get('SELECT * FROM challenges WHERE id=?', [req.params.id]);
  if (!c) return res.status(404).json(er('챌린지 없음'));
  res.json(ok(chalRes(c, get('SELECT COUNT(*) as c FROM challenge_participants WHERE challenge_id=?',[c.id]).c)));
});

app.post('/api/challenges/:id/join', auth, (req, res) => {
  const c = get('SELECT * FROM challenges WHERE id=?', [req.params.id]);
  if (!c) return res.status(404).json(er('챌린지 없음'));
  if (get('SELECT id FROM challenge_participants WHERE challenge_id=? AND user_id=?', [c.id, req.userId]))
    return res.status(409).json(er('이미 참가 중'));
  const cnt = get('SELECT COUNT(*) as c FROM challenge_participants WHERE challenge_id=?', [c.id]).c;
  if (cnt >= c.max_participants) return res.status(400).json(er('인원 초과'));
  run('INSERT INTO challenge_participants (challenge_id, user_id) VALUES (?,?)', [c.id, req.userId]);
  res.json(ok(null));
});

app.get('/api/challenges/:id/progress', auth, (req, res) => {
  const c = get('SELECT * FROM challenges WHERE id=?', [req.params.id]);
  if (!c) return res.status(404).json(er('챌린지 없음'));
  const cp = get('SELECT * FROM challenge_participants WHERE challenge_id=? AND user_id=?', [c.id, req.userId]);
  if (!cp) return res.status(404).json(er('미참가'));
  const logs = all('SELECT * FROM challenge_daily_logs WHERE participant_id=? ORDER BY log_date', [cp.id]);
  const achieved = logs.filter(l=>l.achieved).length;
  const pct = c.duration_days>0 ? Math.round(logs.length/c.duration_days*1000)/10 : 0;
  res.json(ok({ participantId:cp.id, status:cp.status, missedCount:cp.missed_count,
    maxMissedDays:Math.floor(c.duration_days*c.fail_threshold), totalDays:c.duration_days,
    achievedDays:achieved, progressPercent:pct,
    dailyLogs: logs.map(l=>({ date:l.log_date, achieved:!!l.achieved })) }));
});

// === PROMPTS ===
app.get('/api/prompts/random', auth, (req, res) => {
  const { category } = req.query;
  let row;
  if (category) row = get('SELECT * FROM prompt_templates WHERE is_active=1 AND category=? ORDER BY RANDOM() LIMIT 1', [category.toUpperCase()]);
  else row = get('SELECT * FROM prompt_templates WHERE is_active=1 ORDER BY RANDOM() LIMIT 1');
  if (!row) return res.json(ok({ id:null, category:'RANDOM', content:'미래의 나에게 하고 싶은 말을 자유롭게 적어보세요.' }));
  res.json(ok({ id:row.id, category:row.category, content:row.content_ko }));
});

app.post('/api/prompts/ai', auth, (req, res) => {
  const { goal } = req.body;
  const prompt = goal ? `'${goal}'라는 목표를 이룬 미래의 나에게, 오늘의 내가 하고 싶은 말은?` : '1년 후의 나에게 지금 가장 하고 싶은 이야기를 적어보세요.';
  res.json(ok({ prompt }));
});

// === START ===
app.listen(PORT, () => console.log(`\n  DreamLog API: http://localhost:${PORT}\n`));
