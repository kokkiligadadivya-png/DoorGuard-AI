import { useEffect, useState } from "react";
import { getDashboard, getEventAnalysis, sendFeedback } from "./api";

export default function CriticalCard() {
  const [dash, setDash] = useState(null);
  const [event, setEvent] = useState(null);
  const [msg, setMsg] = useState("");

  async function load() {
    const d = await getDashboard();
    setDash(d);
    if (d.latestCriticalEvent) {
      setEvent(await getEventAnalysis(d.latestCriticalEvent.id)); // LLM analysis
    } else {
      setEvent(null);
    }
  }

  useEffect(() => { load().catch(console.error); }, []);

  async function markSafe() {
    const r = await sendFeedback(event.id, "SAFE");
    setMsg(r.message);
    load();
  }

  if (!dash) return <p>Loading...</p>;

  return (
    <div>
      {event ? (
        <div>
          <h2>🔴 CRITICAL SECURITY EVENT</h2>
          <p>{event.location} · Risk Score: {event.riskScore}/100</p>

          <h3>Why this score?</h3>
          <ul>
            {event.riskFactors.map((f) => (
              <li key={f.name}>{f.points > 0 ? "+" : ""}{f.points} {f.name}: {f.reason}</li>
            ))}
          </ul>

          <h3>AI Analysis ({event.analysisSource})</h3>
          <p>{event.aiAnalysis}</p>

          <h3>Recommended actions</h3>
          <ul>{event.recommendedActions.map((a) => <li key={a}>{a}</li>)}</ul>

          <button onClick={markSafe}>Mark Safe</button>
          <button onClick={() => sendFeedback(event.id, "THREAT").then(load)}>Confirm Threat</button>
          {msg && <p>{msg}</p>}
        </div>
      ) : (
        <p>🟢 All clear</p>
      )}

      <h3>Today's Security</h3>
      <p>🟢 {dash.normalCount} Normal · 🟠 {dash.unusualCount} Unusual · 🔴 {dash.criticalCount} Critical</p>
      <p>AI filtered {dash.noiseFiltered} harmless events (simulated)</p>

      <h3>AI Daily Summary</h3>
      <p>{dash.dailySummary}</p>
      <small>🔒 {dash.privacyNote}</small>
    </div>
  );
}