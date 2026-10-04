import { useState } from "react";
import CriticalCard from "../components/CriticalCard";
import { sendFeedback } from "../api";

export default function DashboardPage({ data, reload }) {
  const [notice, setNotice] = useState("");
  const { dash, critical } = data;

  async function handleFeedback(verdict) {
    const result = await sendFeedback(critical.id, verdict);
    setNotice(result.message); // keep the message even after the card disappears
    await reload();
  }

  return (
    <>
      {notice && <div className="notice">🧠 {notice}</div>}

      {critical ? (
        <CriticalCard event={critical} onFeedback={handleFeedback} />
      ) : (
        <div className="card clear">🟢 All clear. No critical events need attention.</div>
      )}

      <section className="card">
        <h2>Today's Security</h2>
        <div className="stats">
          <div className="stat normal"><b>{dash.normalCount}</b> Normal</div>
          <div className="stat unusual"><b>{dash.unusualCount}</b> Unusual</div>
          <div className="stat critical"><b>{dash.criticalCount}</b> Critical</div>
        </div>
        <p className="muted">
          AI filtered {dash.noiseFiltered} harmless events (pets, wind, cars) <i>(simulated)</i>
        </p>
      </section>

      <section className="card">
        <h2>AI Daily Summary <span className="tag">{dash.summarySource}</span></h2>
        <p className="analysis">{dash.dailySummary}</p>
        <small className="muted">🔒 {dash.privacyNote}</small>
      </section>
    </>
  );
}