import { Link } from "react-router-dom";

import { useAuth } from "../context/AuthContext";

function HomePage() {
  const { user, isAuthenticated, logout } = useAuth();

  return (
    <main className="app-shell">
      <h1>Order Processing System</h1>

      {isAuthenticated ? (
        <>
          <p>Logged in as {user.email}</p>

          <button type="button" onClick={logout}>
            Logout
          </button>
        </>
      ) : (
        <>
          <p>Please log in or create an account.</p>

          <nav>
            <Link to="/login">Login</Link>
            {" | "}
            <Link to="/register">Register</Link>
          </nav>
        </>
      )}
    </main>
  );
}

export default HomePage;