import api from "../../api/axiosInstance";

export async function getProducts() {
  const response = await api.get("/api/v1/products");
  return response.data;
}

export async function getProductById(productId) {
  const response = await api.get(
    `/api/v1/products/${productId}`,
  );

  return response.data;
}