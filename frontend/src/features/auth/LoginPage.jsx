import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";

import { useAuth } from "../../context/AuthContext";
import { getApiError } from "../../utils/apiError";
import "./auth.css";

function LoginPage() {
  const navigate = useNavigate();
  const { login, isAuthenticated } = useAuth();

  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });

  const [fieldErrors, setFieldErrors] = useState({});
  const [generalError, setGeneralError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  function handleChange(event) {
    const { name, value } = event.target;

    setFormData((currentData) => ({
      ...currentData,
      [name]: value,
    }));

    setFieldErrors((currentErrors) => ({
      ...currentErrors,
      [name]: undefined,
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();

    setGeneralError("");
    setFieldErrors({});
    setIsSubmitting(true);

    try {
      await login(formData);
      navigate("/", { replace: true });
    } catch (error) {
      const apiError = getApiError(error);

      setGeneralError(apiError.message);
      setFieldErrors(apiError.fieldErrors);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-card">
        <h1>Login</h1>
        <p>Sign in to access your account.</p>

        {generalError && (
          <div className="error-alert" role="alert">
            {generalError}
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label htmlFor="email">Email</label>

            <input
              id="email"
              name="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              autoComplete="email"
              aria-invalid={Boolean(fieldErrors.email)}
            />

            {fieldErrors.email && (
              <span className="field-error">
                {fieldErrors.email}
              </span>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>

            <input
              id="password"
              name="password"
              type="password"
              value={formData.password}
              onChange={handleChange}
              autoComplete="current-password"
              aria-invalid={Boolean(fieldErrors.password)}
            />

            {fieldErrors.password && (
              <span className="field-error">
                {fieldErrors.password}
              </span>
            )}
          </div>

          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Signing in..." : "Login"}
          </button>
        </form>

        <p>
          Need an account? <Link to="/register">Register</Link>
        </p>
      </section>
    </main>
  );
}

export default LoginPage;