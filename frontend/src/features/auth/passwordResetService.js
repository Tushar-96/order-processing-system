import api from "../../api/axiosInstance";

export async function requestPasswordReset(email) {
  const response = await api.post(
    "/api/v1/auth/forgot-password",
    { email },
  );

  return response.data;
}

export async function resetPassword({
  token,
  newPassword,
  confirmPassword,
}) {
  const response = await api.post(
    "/api/v1/auth/reset-password",
    {
      token,
      newPassword,
      confirmPassword,
    },
  );

  return response.data;
}