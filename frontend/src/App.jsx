import { Navigate, Route, Routes } from "react-router-dom";

import { useAuth } from "./context/AuthContext";
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import "./App.css";

function App() {
  const { isLoading } = useAuth();

  if (isLoading) {
    return (
      <main className="app-shell">
        <p>Restoring session...</p>
      </main>
    );
  }

  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;