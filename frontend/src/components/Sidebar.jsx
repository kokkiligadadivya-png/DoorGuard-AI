const ITEMS = [
  ["dashboard", "Dashboard", "🏠"],
  ["live", "Live View", "📹"],
  ["alerts", "Alerts", "🚨"],
  ["history", "History", "🕘"],
  ["assistant", "AI Assistant", "🤖"],
  ["settings", "Settings", "⚙️"],
];

export default function Sidebar({ page, setPage, alertCount }) {
  return (
    <nav className="sidebar">
      {ITEMS.map(([key, label, icon]) => (
        <button
          key={key}
          className={page === key ? "nav active" : "nav"}
          onClick={() => setPage(key)}
        >
          <span>{icon} {label}</span>
          {key === "alerts" && alertCount > 0 && <b className="count">{alertCount}</b>}
        </button>
      ))}
    </nav>
  );
}