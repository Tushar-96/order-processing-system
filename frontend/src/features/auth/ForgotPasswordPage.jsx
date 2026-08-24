import { useState } from "react";
import { Link } from "react-router-dom";

import { getApiError } from "../../utils/apiError";
import { requestPasswordReset } from "./passwordResetService";
import "./auth.css";

function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const [generalError, setGeneralError] = useState("");
  const [successMessage, setSuccessMessage] =
    useState("");
  const [isSubmitting, setIsSubmitting] =
    useState(false);

  function handleEmailChange(event) {
    setEmail(event.target.value);

    setFieldErrors((currentErrors) => ({
      ...currentErrors,
      email: undefined,
    }));

    setGeneralError("");
  }

  async function handleSubmit(event) {
    event.preventDefault();

    setFieldErrors({});
    setGeneralError("");
    setSuccessMessage("");
    setIsSubmitting(true);

    try {
      const response =
        await requestPasswordReset(email);

      setSuccessMessage(response.message);
      setEmail("");
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
        <h1>Forgot password</h1>

        <p>
          Enter your account email and we’ll send you a
          password-reset link.
        </p>

        {generalError && (
          <div className="error-alert" role="alert">
            {generalError}
          </div>
        )}

        {successMessage && (
          <div className="success-alert" role="status">
            {successMessage}
          </div>
        )}

        {!successMessage && (
          <form onSubmit={handleSubmit} noValidate>
            <div className="form-group">
              <label htmlFor="email">Email</label>

              <input
                id="email"
                name="email"
                type="email"
                value={email}
                onChange={handleEmailChange}
                autoComplete="email"
                aria-invalid={Boolean(
                  fieldErrors.email
                )}
              />

              {fieldErrors.email && (
                <span className="field-error">
                  {fieldErrors.email}
                </span>
              )}
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
            >
              {isSubmitting
                ? "Sending reset link..."
                : "Send reset link"}
            </button>
          </form>
        )}

        <p>
          <Link to="/login">Return to login</Link>
        </p>
      </section>
    </main>
  );
}

export default ForgotPasswordPage;