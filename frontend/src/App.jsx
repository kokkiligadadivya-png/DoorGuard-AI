import { useState } from "react";
import { getDashboard, getEventAnalysis } from "./api";
import { useApi } from "./useApi";
import ErrorBoundary from "./components/ErrorBoundary";
import Sidebar from "./components/Sidebar";
import TopBar from "./components/TopBar";
import DashboardPage from "./pages/DashboardPage";
import LiveViewPage from "./pages/LiveViewPage";
import AlertsPage from "./pages/AlertsPage";
import HistoryPage from "./pages/HistoryPage";
import AssistantPage from "./pages/AssistantPage";
import SettingsPage from "./pages/SettingsPage";

// Dashboard summary + AI analysis of the latest critical event, in one fetch
async function fetchDashboard() {
  const dash = await getDashboard();
  const critical = dash.latestCriticalEvent
    ? await getEventAnalysis(dash.latestCriticalEvent.id)
    : null;
  return { dash, critical };
}

export default function App() {
  const [page, setPage] = useState("dashboard");
  const { data, error, loading, reload } = useApi(fetchDashboard, 10000);

  if (loading) return <p className="center">Loading DoorGuard AI...</p>;
  if (!data) {
    return (
      <p className="center error">
        Cannot reach the backend ({error}). Start it with <code>.\mvnw.cmd spring-boot:run</code>
      </p>
    );
  }

  const alertCount = data.dash.activeAlerts;

  const pages = {
    dashboard: <DashboardPage data={data} reload={reload} />,
    live: <LiveViewPage />,
    alerts: <AlertsPage onChanged={reload} />,
    history: <HistoryPage />,
    assistant: <AssistantPage />,
    settings: <SettingsPage />,
  };

  return (
    <div className="app">
      <TopBar alertCount={alertCount} online={!error} />
      <Sidebar page={page} setPage={setPage} alertCount={alertCount} />
   <main className="content">
  <ErrorBoundary key={page}>{pages[page]}</ErrorBoundary>
</main>
    </div>
  );
}