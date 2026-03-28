"""
Batch jobs dashboard — trigger and monitor sync jobs in real time.

Usage:
    python dashboard.py          # Starts on http://localhost:5000
"""

import io
import sys
import threading
import time
import uuid
from contextlib import redirect_stdout

from dotenv import load_dotenv
load_dotenv()

from flask import Flask, Response, jsonify, stream_with_context

app = Flask(__name__)

# In-memory job store
jobs = {}


class Job:
    def __init__(self, name, target, args=()):
        self.id = str(uuid.uuid4())[:8]
        self.name = name
        self.target = target
        self.args = args
        self.status = "pending"
        self.logs = []
        self.thread = None

    def start(self):
        self.status = "running"
        self.thread = threading.Thread(target=self._run, daemon=True)
        self.thread.start()

    def _run(self):
        buf = io.StringIO()
        try:
            with redirect_stdout(buf):
                self.target(*self.args)
            self.status = "done"
        except Exception as e:
            buf.write(f"\n!!! Error: {e}\n")
            self.status = "failed"
        finally:
            self.logs.append(buf.getvalue())

    def get_all_output(self):
        return "".join(self.logs)


def run_job(name, target, args=()):
    job = Job(name, target, args)
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
    targets = {
        "sync_deputies": ("Sync Deputies", _sync_deputies),
        "sync_votes": ("Sync Votes", _sync_votes),
        "sync_proposals": ("Sync Proposals", _sync_proposals),
        "enrich_all": ("Enrich All Activities", _enrich_all),
        "full_sync": ("Full Sync", _full_sync),
    }
    if job_name not in targets:
        return jsonify({"error": f"Unknown job: {job_name}"}), 404

    name, target = targets[job_name]
    job = run_job(name, target)
    return jsonify({"id": job.id, "name": job.name, "status": job.status})


@app.route("/api/jobs/<job_id>/stream")
def stream_job(job_id):
    job = jobs.get(job_id)
    if not job:
        return jsonify({"error": "Job not found"}), 404

    def generate():
        last_len = 0
        while True:
            output = job.get_all_output()
            if len(output) > last_len:
                new_text = output[last_len:]
                last_len = len(output)
                yield f"data: {_sse_encode(new_text)}\n\n"
            if job.status in ("done", "failed"):
                # Final flush
                output = job.get_all_output()
                if len(output) > last_len:
                    yield f"data: {_sse_encode(output[last_len:])}\n\n"
                yield f"event: done\ndata: {job.status}\n\n"
                break
            time.sleep(0.3)

    return Response(
        stream_with_context(generate()),
        mimetype="text/event-stream",
        headers={"Cache-Control": "no-cache", "X-Accel-Buffering": "no"},
    )


def _sse_encode(text):
    return text.replace("\n", "\ndata: ")


# Job targets — import lazily to avoid circular issues
def _sync_deputies():
    from sync_deputies import sync
    sync()


def _sync_votes():
    from sync_votes import sync
    sync(days_back=30)


def _sync_proposals():
    from sync_proposals import sync
    sync(days_back=30)


def _enrich_all():
    from enrich_activities import enrich_activity, fetch_all_activities, ANTHROPIC_API_KEY
    if not ANTHROPIC_API_KEY:
        print("Error: ANTHROPIC_API_KEY not set in .env")
        return
    activities = fetch_all_activities()
    print(f"Found {len(activities)} activities\n")
    enriched = 0
    for activity in activities:
        print(f"[{activity['id']}] {activity['title']}")
        try:
            if enrich_activity(activity):
                enriched += 1
        except Exception as e:
            print(f"  ! Error: {e}")
    print(f"\nEnriched: {enriched}/{len(activities)}")


def _full_sync():
    print("=== Step 1/4: Deputies ===\n")
    _sync_deputies()
    print("\n=== Step 2/4: Votes ===\n")
    _sync_votes()
    print("\n=== Step 3/4: Proposals ===\n")
    _sync_proposals()
    print("\n=== Step 4/4: Enrich ===\n")
    _enrich_all()
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
    border-bottom: 1px solid #2a2a3e;
  }
  .log-header .title { font-size: 14px; font-weight: 700; }
  .log-header .status {
    font-size: 11px; font-weight: 700; padding: 3px 10px;
    border-radius: 99px; text-transform: uppercase; letter-spacing: 0.5px;
  }
  .status.running { background: #7c3aed22; color: #a78bfa; }
  .status.done { background: #10b98122; color: #34d399; }
  .status.failed { background: #ef444422; color: #f87171; }
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
  .dot.pending { background: #555; }
</style>
</head>
<body>
<div class="header">
  <h1>De Olho Neles</h1>
  <p>Batch Jobs Dashboard</p>
</div>
<div class="container">
  <div class="jobs-grid">
    <div class="job-btn" onclick="runJob('sync_deputies')">
      <div class="icon">👥</div>
      <div class="name">Sync Deputies</div>
      <div class="desc">Fetch all deputies from Camara API</div>
    </div>
    <div class="job-btn" onclick="runJob('sync_votes')">
      <div class="icon">🗳️</div>
      <div class="name">Sync Votes</div>
      <div class="desc">Fetch voting sessions (last 30 days)</div>
    </div>
    <div class="job-btn" onclick="runJob('sync_proposals')">
      <div class="icon">📜</div>
      <div class="name">Sync Proposals</div>
      <div class="desc">Fetch proposals by deputies (last 30 days)</div>
    </div>
    <div class="job-btn" onclick="runJob('enrich_all')">
      <div class="icon">🤖</div>
      <div class="name">Enrich Activities</div>
      <div class="desc">Generate AI summaries for all activities</div>
    </div>
    <div class="job-btn" onclick="runJob('full_sync')" style="grid-column: 1 / -1;">
      <div class="icon">🚀</div>
      <div class="name">Full Sync</div>
      <div class="desc">Run all jobs: deputies → votes → proposals → enrich</div>
    </div>
  </div>

  <div class="log-panel" id="logPanel">
    <div class="log-header">
      <span class="title" id="logTitle">—</span>
      <span class="status running" id="logStatus">running</span>
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

async function runJob(name) {
  const res = await fetch(`/api/run/${name}`, { method: 'POST' });
  const job = await res.json();
  showLog(job.id, job.name);
  refreshHistory();
}

function showLog(jobId, jobName) {
  const panel = document.getElementById('logPanel');
  const title = document.getElementById('logTitle');
  const status = document.getElementById('logStatus');
  const body = document.getElementById('logBody');

  panel.classList.add('active');
  title.textContent = jobName || jobId;
  status.textContent = 'running';
  status.className = 'status running';
  body.textContent = '';

  if (currentEventSource) currentEventSource.close();

  const es = new EventSource(`/api/jobs/${jobId}/stream`);
  currentEventSource = es;

  es.onmessage = (e) => {
    body.textContent += e.data.replace(/\\ndata: /g, '') + '';
    body.scrollTop = body.scrollHeight;
  };

  es.addEventListener('done', (e) => {
    status.textContent = e.data;
    status.className = `status ${e.data}`;
    es.close();
    currentEventSource = null;
    refreshHistory();
  });

  es.onerror = () => {
    status.textContent = 'disconnected';
    status.className = 'status failed';
    es.close();
    currentEventSource = null;
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
    <div class="history-item" onclick="showLog('${j.id}', '${j.name}')">
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
