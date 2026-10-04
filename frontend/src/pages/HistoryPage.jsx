import { getEvents } from "../api";
import { useApi } from "../useApi";
import Badge from "../components/Badge";
import { fmtTime } from "../utils";

export default function HistoryPage() {
  const { data: events, error, loading } = useApi(getEvents, 15000);

  if (loading) return <p>Loading history...</p>;
  if (error) return <p className="error">{error}</p>;

  return (
    <section className="card">
      <h2>Event History</h2>
      <div className="table-wrap">
        <table>
          <thead>
            <tr><th>Time</th><th>Location</th><th>Visitor</th><th>What happened</th><th>Risk</th><th>Status</th></tr>
          </thead>
          <tbody>
            {events.map((e) => (
              <tr key={e.id}>
                <td>{fmtTime(e.timestamp)}</td>
                <td>{e.location}</td>
                <td>{e.visitorType.replace("_", " ")}</td>
                <td>{e.description}</td>
                <td><Badge level={e.riskLevel} /> {e.riskScore}</td>
                <td>{e.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}