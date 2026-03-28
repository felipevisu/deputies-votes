"""
Batch jobs dashboard — trigger and monitor sync jobs in real time.

Usage:
    python dashboard.py          # Starts on http://localhost:5055
"""

import sys
import threading
import time
import uuid

from dotenv import load_dotenv
load_dotenv()

from flask import Flask, Response, jsonify, request, stream_with_context

app = Flask(__name__)

# In-memory job store
jobs = {}


class LiveWriter:
    """File-like object that appends to a shared buffer in real time."""

    def __init__(self):
        self._lock = threading.Lock()
        self._buf = ""

    def write(self, text):
        if text:
            with self._lock:
                self._buf += text

    def flush(self):
        pass

    def read_from(self, offset):
        with self._lock:
            return self._buf[offset:]

    def get_all(self):
        with self._lock:
            return self._buf


class Job:
    def __init__(self, name, target, args=(), kwargs=None):
        self.id = str(uuid.uuid4())[:8]
        self.name = name
        self.target = target
        self.args = args
        self.kwargs = kwargs or {}
        self.status = "pending"
        self.writer = LiveWriter()
        self.cancel_event = threading.Event()
        self.thread = None

    def start(self):
        self.status = "running"
        self.thread = threading.Thread(target=self._run, daemon=True)
        self.thread.start()

    def _run(self):
        old_stdout = sys.stdout
        sys.stdout = self.writer
        try:
            self.kwargs["cancel_check"] = self.cancel_event.is_set
            self.target(*self.args, **self.kwargs)
            if self.cancel_event.is_set():
                self.status = "cancelled"
            else:
                self.status = "done"
        except Exception as e:
            self.writer.write(f"\n!!! Error: {e}\n")
            self.status = "failed"
        finally:
            sys.stdout = old_stdout

    def cancel(self):
        self.cancel_event.set()


def run_job(name, target, args=(), kwargs=None):
    job = Job(name, target, args, kwargs)
    jobs[job.id] = job
    job.start()
    return job


@app.route("/")
def index():
    return HTML_PAGE


@app.route("/api/jobs", methods=["GET"])
def list_jobs():
    return jsonify([
        {"id": j.id, "name": j.name, "status": j.status}
        for j in reversed(jobs.values())
    ])


@app.route("/api/run/<job_name>", methods=["POST"])
def start_job(job_name):
    body = request.get_json(silent=True) or {}
    start_date = body.get("startDate")
    end_date = body.get("endDate")

    targets = {
        "sync_deputies": ("Sync Deputies", _sync_deputies),
        "sync_votes": ("Sync Votes", _sync_votes),
        "sync_proposals": ("Sync Proposals", _sync_proposals),
        "sync_events": ("Sync Events", _sync_events),
        "enrich_all": ("Enrich All Activities", _enrich_all),
        "full_sync": ("Full Sync", _full_sync),
    }
    if job_name not in targets:
        return jsonify({"error": f"Unknown job: {job_name}"}), 404

    name, target = targets[job_name]

    kwargs = {}
    if start_date and end_date and job_name in ("sync_votes", "sync_proposals", "sync_events", "full_sync"):
        kwargs["start_date"] = start_date
        kwargs["end_date"] = end_date

    job = run_job(name, target, kwargs=kwargs)
    return jsonify({"id": job.id, "name": job.name, "status": job.status})


@app.route("/api/jobs/<job_id>/cancel", methods=["POST"])
def cancel_job(job_id):
    job = jobs.get(job_id)
    if not job:
        return jsonify({"error": "Job not found"}), 404
    if job.status != "running":
        return jsonify({"error": "Job is not running"}), 400
    job.cancel()
    return jsonify({"id": job.id, "status": "cancelling"})


@app.route("/api/jobs/<job_id>/stream")
def stream_job(job_id):
    job = jobs.get(job_id)
    if not job:
        return jsonify({"error": "Job not found"}), 404

    def generate():
        offset = 0
        while True:
            new_text = job.writer.read_from(offset)
            if new_text:
                offset += len(new_text)
                yield f"data: {_sse_encode(new_text)}\n\n"
            if job.status in ("done", "failed", "cancelled"):
                # Final flush
                new_text = job.writer.read_from(offset)
                if new_text:
                    yield f"data: {_sse_encode(new_text)}\n\n"
                yield f"event: done\ndata: {job.status}\n\n"
                break
            time.sleep(0.2)

    return Response(
        stream_with_context(generate()),
        mimetype="text/event-stream",
        headers={"Cache-Control": "no-cache", "X-Accel-Buffering": "no"},
    )


def _sse_encode(text):
    return text.replace("\n", "\ndata: ")


# Job targets
def _sync_deputies(cancel_check=None):
    from sync_deputies import sync
    sync(cancel_check=cancel_check)


def _sync_votes(start_date=None, end_date=None, cancel_check=None):
    from sync_votes import sync
    sync(start_date=start_date, end_date=end_date, cancel_check=cancel_check)


def _sync_proposals(start_date=None, end_date=None, cancel_check=None):
    from sync_proposals import sync
    sync(start_date=start_date, end_date=end_date, cancel_check=cancel_check)


def _sync_events(start_date=None, end_date=None, cancel_check=None):
    from sync_events import sync
    sync(start_date=start_date, end_date=end_date, cancel_check=cancel_check)


def _enrich_all(cancel_check=None):
    from enrich_activities import enrich_activity, fetch_all_activities, ANTHROPIC_API_KEY
    if not ANTHROPIC_API_KEY:
        print("Error: ANTHROPIC_API_KEY not set in .env")
        return
    activities = fetch_all_activities()
    print(f"Found {len(activities)} activities\n")
    enriched = 0
    for activity in activities:
        if cancel_check and cancel_check():
            print("\n⛔ Cancelled by user")
            break
        print(f"[{activity['id']}] {activity['title']}")
        try:
            if enrich_activity(activity):
                enriched += 1
        except Exception as e:
            print(f"  ! Error: {e}")
    print(f"\nEnriched: {enriched}/{len(activities)}")


def _full_sync(start_date=None, end_date=None, cancel_check=None):
    print("=== Step 1/5: Deputies ===\n")
    _sync_deputies(cancel_check=cancel_check)
    if cancel_check and cancel_check():
        return
    print("\n=== Step 2/5: Votes ===\n")
    _sync_votes(start_date=start_date, end_date=end_date, cancel_check=cancel_check)
    if cancel_check and cancel_check():
        return
    print("\n=== Step 3/5: Proposals ===\n")
    _sync_proposals(start_date=start_date, end_date=end_date, cancel_check=cancel_check)
    if cancel_check and cancel_check():
        return
    print("\n=== Step 4/5: Events ===\n")
    _sync_events(start_date=start_date, end_date=end_date, cancel_check=cancel_check)
    if cancel_check and cancel_check():
        return
    print("\n=== Step 5/5: Enrich ===\n")
    _enrich_all(cancel_check=cancel_check)
    print("\n=== All done! ===")


HTML_PAGE = """<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>De Olho Neles — Batch Dashboard</title>
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    background: #0f0f0f; color: #e0e0e0; min-height: 100vh;
  }
  .header {
    background: #1a1a2e; padding: 20px 24px;
    border-bottom: 1px solid #2a2a3e;
  }
  .header h1 { font-size: 20px; font-weight: 700; color: #fff; }
  .header p { font-size: 13px; color: #888; margin-top: 4px; }
  .container { max-width: 900px; margin: 0 auto; padding: 24px; }

  .date-range {
    display: flex; gap: 12px; align-items: center;
    margin-bottom: 20px; flex-wrap: wrap;
  }
  .date-range label { font-size: 13px; color: #888; }
  .date-range input[type="date"] {
    background: #1a1a2e; border: 1px solid #2a2a3e; color: #e0e0e0;
    padding: 8px 12px; border-radius: 8px; font-size: 13px;
  }
  .date-range input[type="date"]:focus {
    outline: none; border-color: #7c3aed;
  }

  .jobs-grid {
    display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 12px; margin-bottom: 24px;
  }
  .job-btn {
    padding: 16px; border: 1px solid #2a2a3e; border-radius: 12px;
    background: #1a1a2e; cursor: pointer; text-align: left;
    transition: all 0.2s;
  }
  .job-btn:hover { border-color: #7c3aed; background: #1e1e36; }
  .job-btn:active { transform: scale(0.98); }
  .job-btn .name { font-size: 14px; font-weight: 700; color: #fff; }
  .job-btn .desc { font-size: 12px; color: #888; margin-top: 4px; }
  .job-btn .icon { font-size: 24px; margin-bottom: 8px; }

  .log-panel {
    background: #111; border: 1px solid #2a2a3e; border-radius: 12px;
    overflow: hidden; display: none;
  }
  .log-panel.active { display: block; }
  .log-header {
    padding: 12px 16px; background: #1a1a2e;
    display: flex; align-items: center; justify-content: space-between;
    border-bottom: 1px solid #2a2a3e; gap: 12px;
  }
  .log-header .title { font-size: 14px; font-weight: 700; }
  .log-header .controls { display: flex; align-items: center; gap: 8px; }
  .log-header .status {
    font-size: 11px; font-weight: 700; padding: 3px 10px;
    border-radius: 99px; text-transform: uppercase; letter-spacing: 0.5px;
  }
  .status.running { background: #7c3aed22; color: #a78bfa; }
  .status.done { background: #10b98122; color: #34d399; }
  .status.failed { background: #ef444422; color: #f87171; }
  .status.cancelled { background: #f59e0b22; color: #fbbf24; }
  .cancel-btn {
    background: #ef4444; color: #fff; border: none; padding: 4px 14px;
    border-radius: 8px; font-size: 12px; font-weight: 600; cursor: pointer;
    transition: all 0.2s;
  }
  .cancel-btn:hover { background: #dc2626; }
  .cancel-btn:disabled { opacity: 0.3; cursor: default; }
  .log-body {
    padding: 16px; font-family: 'JetBrains Mono', 'Fira Code', monospace;
    font-size: 12px; line-height: 1.6; max-height: 500px;
    overflow-y: auto; white-space: pre-wrap; color: #aaa;
  }
  .history { margin-top: 24px; }
  .history h3 { font-size: 14px; color: #888; margin-bottom: 12px; }
  .history-item {
    padding: 8px 12px; border-bottom: 1px solid #1a1a2e;
    display: flex; align-items: center; gap: 12px;
    font-size: 13px; cursor: pointer;
  }
  .history-item:hover { background: #1a1a2e; }
  .history-item .dot {
    width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0;
  }
  .dot.running { background: #a78bfa; }
  .dot.done { background: #34d399; }
  .dot.failed { background: #f87171; }
  .dot.cancelled { background: #fbbf24; }
  .dot.pending { background: #555; }
</style>
</head>
<body>
<div class="header">
  <h1>De Olho Neles</h1>
  <p>Batch Jobs Dashboard</p>
</div>
<div class="container">
  <div class="date-range">
    <label>Data inicio:</label>
    <input type="date" id="startDate">
    <label>Data fim:</label>
    <input type="date" id="endDate">
  </div>

  <div class="jobs-grid">
    <div class="job-btn" onclick="runJob('sync_deputies')">
      <div class="icon">👥</div>
      <div class="name">Sync Deputies</div>
      <div class="desc">Fetch all deputies from Camara API</div>
    </div>
    <div class="job-btn" onclick="runJob('sync_votes')">
      <div class="icon">🗳️</div>
      <div class="name">Sync Votes</div>
      <div class="desc">Fetch voting sessions</div>
    </div>
    <div class="job-btn" onclick="runJob('sync_proposals')">
      <div class="icon">📜</div>
      <div class="name">Sync Proposals</div>
      <div class="desc">Fetch proposals by deputies</div>
    </div>
    <div class="job-btn" onclick="runJob('sync_events')">
      <div class="icon">📅</div>
      <div class="name">Sync Events</div>
      <div class="desc">Fetch committee sessions and hearings</div>
    </div>
    <div class="job-btn" onclick="runJob('enrich_all')">
      <div class="icon">🤖</div>
      <div class="name">Enrich Activities</div>
      <div class="desc">Generate AI summaries for all activities</div>
    </div>
    <div class="job-btn" onclick="runJob('full_sync')" style="grid-column: 1 / -1;">
      <div class="icon">🚀</div>
      <div class="name">Full Sync</div>
      <div class="desc">Run all jobs: deputies → votes → proposals → events → enrich</div>
    </div>
  </div>

  <div class="log-panel" id="logPanel">
    <div class="log-header">
      <span class="title" id="logTitle">—</span>
      <div class="controls">
        <button class="cancel-btn" id="cancelBtn" onclick="cancelJob()" style="display:none">Cancel</button>
        <span class="status running" id="logStatus">running</span>
      </div>
    </div>
    <div class="log-body" id="logBody"></div>
  </div>

  <div class="history" id="historySection" style="display:none">
    <h3>History</h3>
    <div id="historyList"></div>
  </div>
</div>
<script>
let currentEventSource = null;
let currentJobId = null;

// Set default dates: last 30 days
(function() {
  const now = new Date();
  const end = now.toISOString().slice(0, 10);
  const start = new Date(now - 30 * 86400000).toISOString().slice(0, 10);
  document.getElementById('startDate').value = start;
  document.getElementById('endDate').value = end;
})();

async function runJob(name) {
  const startDate = document.getElementById('startDate').value;
  const endDate = document.getElementById('endDate').value;

  const res = await fetch(`/api/run/${name}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ startDate, endDate })
  });
  const job = await res.json();
  currentJobId = job.id;
  showLog(job.id, job.name, true);
  refreshHistory();
}

async function cancelJob() {
  if (!currentJobId) return;
  const btn = document.getElementById('cancelBtn');
  btn.disabled = true;
  btn.textContent = 'Cancelling...';
  await fetch(`/api/jobs/${currentJobId}/cancel`, { method: 'POST' });
}

function showLog(jobId, jobName, isRunning) {
  const panel = document.getElementById('logPanel');
  const title = document.getElementById('logTitle');
  const status = document.getElementById('logStatus');
  const body = document.getElementById('logBody');
  const cancelBtn = document.getElementById('cancelBtn');

  panel.classList.add('active');
  title.textContent = jobName || jobId;
  status.textContent = 'running';
  status.className = 'status running';
  body.textContent = '';

  if (isRunning) {
    cancelBtn.style.display = 'inline-block';
    cancelBtn.disabled = false;
    cancelBtn.textContent = 'Cancel';
  } else {
    cancelBtn.style.display = 'none';
  }

  if (currentEventSource) currentEventSource.close();

  const es = new EventSource(`/api/jobs/${jobId}/stream`);
  currentEventSource = es;

  es.onmessage = (e) => {
    body.textContent += e.data + '';
    body.scrollTop = body.scrollHeight;
  };

  es.addEventListener('done', (e) => {
    status.textContent = e.data;
    status.className = `status ${e.data}`;
    cancelBtn.style.display = 'none';
    es.close();
    currentEventSource = null;
    currentJobId = null;
    refreshHistory();
  });

  es.onerror = () => {
    status.textContent = 'disconnected';
    status.className = 'status failed';
    cancelBtn.style.display = 'none';
    es.close();
    currentEventSource = null;
    currentJobId = null;
  };
}

async function refreshHistory() {
  const res = await fetch('/api/jobs');
  const jobs = await res.json();
  const section = document.getElementById('historySection');
  const list = document.getElementById('historyList');

  if (jobs.length === 0) { section.style.display = 'none'; return; }
  section.style.display = 'block';
  list.innerHTML = jobs.map(j => `
    <div class="history-item" onclick="showLog('${j.id}', '${j.name}', ${j.status === 'running'})">
      <span class="dot ${j.status}"></span>
      <span>${j.name}</span>
      <span style="color:#555;margin-left:auto">${j.id}</span>
    </div>
  `).join('');
}

refreshHistory();
</script>
</body>
</html>
"""

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5055, debug=False, threaded=True)
