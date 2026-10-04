import { getHealth, getLearning } from "../api";
import { useApi } from "../useApi";

export default function SettingsPage() {
  const { data: health } = useApi(getHealth);
  const { data: learned } = useApi(getLearning, 5000);
  const rules = Object.entries(learned || {});

  return (
    <section className="card">
      <h2>Settings</h2>
      <p>Backend: <b>{health ? health.message : "offline"}</b></p>

      <h3>🧠 What DoorGuard has learned from your feedback</h3>
      {rules.length === 0 && <p className="muted">Nothing yet. Use "Mark Safe" on an event to teach it.</p>}
      <ul>
        {rules.map(([key, adj]) => {
          const [location, visitor] = key.split("|");
          return (
            <li key={key}>
              {visitor.replace("_", " ")} at {location}:{" "}
              <b className={adj < 0 ? "minus" : "plus"}>{adj > 0 ? "+" : ""}{adj} risk</b>
            </li>
          );
        })}
      </ul>
    </section>
  );
}