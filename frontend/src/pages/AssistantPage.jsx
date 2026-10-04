import { useEffect, useRef, useState } from "react";
import { askAssistant } from "../api";

const SUGGESTIONS = [
  "Was anything unusual last night?",
  "Did any delivery person visit today?",
  "Show me all unknown visitors",
  "Were there any critical events?",
];

export default function AssistantPage() {
  const [messages, setMessages] = useState([
    { role: "ai", text: "Hi Divya! Ask me anything about your door activity." },
  ]);
  const [input, setInput] = useState("");
  const [busy, setBusy] = useState(false);
  const endRef = useRef(null);

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  async function ask(question) {
    if (!question.trim() || busy) return;
    setMessages((m) => [...m, { role: "user", text: question }]);
    setInput("");
    setBusy(true);
    try {
      const r = await askAssistant(question);
      setMessages((m) => [
        ...m,
        {
          role: "ai",
          text: r.answer ?? "No answer returned.",
          source: r.source,
          count: r.relatedEventIds?.length ?? 0,
        },
      ]);
    } catch (e) {
      setMessages((m) => [...m, { role: "ai", text: "Sorry, something went wrong: " + e.message }]);
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="card chat">
      <h2>🤖 AI Assistant</h2>
      <div className="chips">
        {SUGGESTIONS.map((s) => (
          <button key={s} className="chip clickable" onClick={() => ask(s)}>{s}</button>
        ))}
      </div>

      <div className="messages">
        {messages.map((m, i) => (
          <div key={i} className={`msg ${m.role}`}>
            {m.text}
            {m.source && <small className="tag">{m.source} · {m.count} related events</small>}
          </div>
        ))}
        {busy && <div className="msg ai">Thinking...</div>}
        <div ref={endRef} />
      </div>

      <form className="chat-form" onSubmit={(e) => { e.preventDefault(); ask(input); }}>
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Ask about your door activity..."
        />
        <button type="submit" className="btn safe" disabled={busy}>Send</button>
      </form>
    </section>
  );
}