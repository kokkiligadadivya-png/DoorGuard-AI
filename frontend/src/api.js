const BASE = "/api";

async function request(path, options) {
  const res = await fetch(BASE + path, options);
  const BASE = import.meta.env.VITE_API_URL || "/api";
  if (!res.ok) throw new Error(`Request failed (${res.status})`);
  return res.json();
}

const post = (path, body) =>
  request(path, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

export const getHealth = () => request("/health");
export const getDashboard = () => request("/dashboard");
export const getEvents = () => request("/events");
export const getAlerts = () => request("/alerts");
export const getLearning = () => request("/learning");
export const getEventAnalysis = (id) => request(`/events/${id}/analysis`);
export const createEvent = (event) => post("/events", event);
export const sendFeedback = (id, verdict) => post(`/events/${id}/feedback`, { verdict }); // "SAFE" | "THREAT"
export const askAssistant = (question) => post("/assistant/ask", { question });