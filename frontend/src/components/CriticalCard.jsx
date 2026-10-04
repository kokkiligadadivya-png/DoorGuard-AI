import { useState } from "react";
import { fmtTime } from "../utils";

export default function CriticalCard({ event, onFeedback }) {
  const [busy, setBusy] = useState(false);

  async function give(verdict) {
    setBusy(true);
    try {
      await onFeedback(verdict);
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="card critical">
      <div className="card-head">
        <h2>🔴 CRITICAL SECURITY EVENT</h2>
        <span className="muted">{fmtTime(event.timestamp)}</span>
      </div>

      <div className="critical-grid">
        <div>
          <p className="big">👤 {event.visitorType.replace("_", " ")}</p>
          <p className="muted">{event.location} · {event.deviceName}</p>
          <p className="score">Risk Score: <b>{event.riskScore}</b>/100</p>
          <div className="camera">📹 LIVE CAMERA <small>(simulated feed)</small></div>
        </div>

        <div>
          <h3>Why this score?</h3>
          <ul className="factors">
            {event.riskFactors.map((f) => (
              <li key={f.name} title={f.reason}>
                <b className={f.points >= 0 ? "plus" : "minus"}>
                  {f.points > 0 ? "+" : ""}{f.points}
                </b>
                <span>{f.name}<small>{f.reason}</small></span>
              </li>
            ))}
          </ul>
        </div>
      </div>

      <h3>AI Analysis <span className="tag">{event.analysisSource}</span></h3>
      <p className="analysis">{event.aiAnalysis}</p>

      <h3>Recommended actions</h3>
      <div className="chips">
        {event.recommendedActions.map((a) => <span key={a} className="chip">{a}</span>)}
      </div>

      <div className="actions">
        <button className="btn danger" disabled={busy} onClick={() => give("THREAT")}>
          Confirm Threat
        </button>
        <button className="btn safe" disabled={busy} onClick={() => give("SAFE")}>
          Mark Safe
        </button>
      </div>
    </section>
  );
}