export function getApiError(error) {
  const responseData = error.response?.data;

  if (!error.response) {
    return {
      message:
        "Unable to reach the server. Check that the API Gateway is running.",
      fieldErrors: {},
    };
  }

  return {
    message: responseData?.message || "Something went wrong",
    fieldErrors: responseData?.errors || {},
  };
}