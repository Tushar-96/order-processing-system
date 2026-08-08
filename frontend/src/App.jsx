import { useAuth } from "./context/AuthContext";
import "./App.css";

function App() {
  const { user, isLoading, isAuthenticated, logout } = useAuth();

  if (isLoading) {
    return <p>Restoring session...</p>;
  }

  return (
    <main className="app-shell">
      <h1>Order Processing System</h1>

      {isAuthenticated ? (
        <>
          <p>Logged in as: {user.email}</p>
          <p>Role: {user.role ?? user.authorities?.[0]}</p>

          <button type="button" onClick={logout}>
            Logout
          </button>
        </>
      ) : (
        <p>You are not logged in.</p>
      )}
    </main>
  );
}

export default App;

// import AppRoutes from "./routes/AppRoutes";

// export default function App() {
//   return <AppRoutes />;
// }

// import "./App.css";

// function App() {
//   return (
//     <main className="app-shell">
//       <h1>Order Processing System</h1>
//       <p>Frontend API configuration is ready.</p>
//     </main>
//   );
// }

// export default App;
