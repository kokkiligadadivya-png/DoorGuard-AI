import { Component } from "react";

export default class ErrorBoundary extends Component {
  state = { error: null };

  static getDerivedStateFromError(error) {
    return { error };
  }

  render() {
    if (this.state.error) {
      return (
        <div className="card">
          <h2 className="error">This page crashed</h2>
          <pre style={{ whiteSpace: "pre-wrap" }}>{String(this.state.error.message)}</pre>
        </div>
      );
    }
    return this.props.children;
  }
}