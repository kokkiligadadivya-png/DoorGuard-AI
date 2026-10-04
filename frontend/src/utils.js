export const fmtTime = (iso) =>
  new Date(iso).toLocaleString("en-IN", {
    day: "numeric",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  });

export const levelClass = (level) => (level || "NORMAL").toLowerCase();
export const levelIcon = { NORMAL: "🟢", UNUSUAL: "🟠", CRITICAL: "🔴" };