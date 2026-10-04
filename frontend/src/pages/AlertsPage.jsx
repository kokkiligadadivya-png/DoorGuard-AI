import { getAlerts, sendFeedback } from "../api";
import { useApi } from "../useApi";
import Badge from "../components/Badge";
import { fmtTime } from "../utils";

export default function AlertsPage({ onChanged }) {
  const { data: alerts, error, loading, reload } = useApi(getAlerts, 10000);

  async function markSafe(eventId) {
    await sendFeedback(eventId, "SAFE");
    await reload();
    onChanged(); // refresh dashboard + sidebar count
  }

  if (loading) return <p>Loading alerts...</p>;
  if (error) return <p className="error">{error}</p>;

  return (
    <section className="card">
      <h2>Alerts</h2>
      {alerts.length === 0 && <p className="muted">No alerts.</p>}
      {alerts.map((a) => (
        <div key={a.id} className={`row ${a.status === "RESOLVED" ? "resolved" : ""}`}>
          <div>
            <Badge level={a.severity} /> <b>{a.title}</b>
            <p className="analysis">{a.message}</p>
            <small className="muted">
              {fmtTime(a.timestamp)} · score {a.riskScore} · {a.status}
            </small>
          </div>
          {a.status === "ACTIVE" && (
            <button className="btn safe" onClick={() => markSafe(a.eventId)}>Mark Safe</button>
          )}
        </div>
      ))}
    </section>
  );
}