import { useState } from "react";
import {
  Link,
  useSearchParams,
} from "react-router-dom";

import { getApiError } from "../../utils/apiError";
import { resetPassword } from "./passwordResetService";
import "./auth.css";

function ResetPasswordPage() {
  const [searchParams] = useSearchParams();

  const token = searchParams.get("token") || "";

  const [formData, setFormData] = useState({
    newPassword: "",
    confirmPassword: "",
  });

  const [fieldErrors, setFieldErrors] = useState({});
  const [generalError, setGeneralError] = useState("");
  const [successMessage, setSuccessMessage] =
    useState("");
  const [isSubmitting, setIsSubmitting] =
    useState(false);

  const hasToken = token.trim().length > 0;

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

    setGeneralError("");
  }

  async function handleSubmit(event) {
    event.preventDefault();

    setFieldErrors({});
    setGeneralError("");
    setSuccessMessage("");

    if (!hasToken) {
      setGeneralError(
        "The password-reset link is invalid or incomplete.",
      );

      return;
    }

    if (
      formData.newPassword !==
      formData.confirmPassword
    ) {
      setFieldErrors({
        confirmPassword:
          "New password and confirmation do not match",
      });

      return;
    }

    setIsSubmitting(true);

    try {
      const response = await resetPassword({
        token,
        newPassword: formData.newPassword,
        confirmPassword: formData.confirmPassword,
      });

      setSuccessMessage(response.message);

      setFormData({
        newPassword: "",
        confirmPassword: "",
      });
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
        <h1>Reset password</h1>

        <p>
          Choose a new password containing between 8 and
          72 characters.
        </p>

        {!hasToken && (
          <div className="error-alert" role="alert">
            The password-reset link is invalid or incomplete.
          </div>
        )}

        {generalError && hasToken && (
          <div className="error-alert" role="alert">
            {generalError}
          </div>
        )}

        {successMessage && (
          <div className="success-alert" role="status">
            {successMessage}
          </div>
        )}

        {hasToken && !successMessage && (
          <form onSubmit={handleSubmit} noValidate>
            <div className="form-group">
              <label htmlFor="newPassword">
                New password
              </label>

              <input
                id="newPassword"
                name="newPassword"
                type="password"
                value={formData.newPassword}
                onChange={handleChange}
                autoComplete="new-password"
                aria-invalid={Boolean(
                  fieldErrors.newPassword
                )}
              />

              {fieldErrors.newPassword && (
                <span className="field-error">
                  {fieldErrors.newPassword}
                </span>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="confirmPassword">
                Confirm new password
              </label>

              <input
                id="confirmPassword"
                name="confirmPassword"
                type="password"
                value={formData.confirmPassword}
                onChange={handleChange}
                autoComplete="new-password"
                aria-invalid={Boolean(
                  fieldErrors.confirmPassword
                )}
              />

              {fieldErrors.confirmPassword && (
                <span className="field-error">
                  {fieldErrors.confirmPassword}
                </span>
              )}
            </div>

            <button
              type="submit"
              disabled={isSubmitting}
            >
              {isSubmitting
                ? "Resetting password..."
                : "Reset password"}
            </button>
          </form>
        )}

        <p>
          {successMessage ? (
            <Link to="/login">
              Continue to login
            </Link>
          ) : (
            <Link to="/forgot-password">
              Request a new reset link
            </Link>
          )}
        </p>
      </section>
    </main>
  );
}

export default ResetPasswordPage;