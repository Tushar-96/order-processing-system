import LoadingSpinner from "./components/LoadingSpinner";
import { useAuth } from "./context/AuthContext";
import AppRoutes from "./routes/AppRoutes";
import "./App.css";

function App() {
  const { isLoading } = useAuth();

  if (isLoading) {
    return (
      <LoadingSpinner message="Restoring session..." />
    );
  }

  return <AppRoutes />;
}

export default App;