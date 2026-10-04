import { getEvents } from "../api";
import { useApi } from "../useApi";
import Badge from "../components/Badge";
import { fmtTime } from "../utils";

export default function LiveViewPage() {
  const { data: events } = useApi(getEvents, 10000);

  // newest event per camera (events already come newest-first)
  const latest = {};
  (events || []).forEach((e) => { if (!latest[e.deviceName]) latest[e.deviceName] = e; });

  return (
    <section className="card">
      <h2>Live View <span className="tag">simulated feeds</span></h2>
      <div className="cameras">
        {Object.values(latest).map((e) => (
          <div key={e.deviceName} className="camera tall">
            <b>📹 {e.deviceName}</b>
            <small>Last: {e.description} ({fmtTime(e.timestamp)})</small>
            <Badge level={e.riskLevel} />
          </div>
        ))}
      </div>
    </section>
  );
}