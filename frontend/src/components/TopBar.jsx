export default function TopBar({ alertCount, online }) {
  return (
    <header className="topbar">
      <h1>🛡️ DoorGuard AI</h1>
      <div className="topbar-right">
        <span className={online ? "pill ok" : "pill bad"}>
          {online ? "● Backend online" : "● Backend offline"}
        </span>
        <span className="bell">🔔{alertCount > 0 && <b className="count">{alertCount}</b>}</span>
        <span>👤 Divya</span>
      </div>
    </header>
  );
}