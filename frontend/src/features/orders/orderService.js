import api from "../../api/axiosInstance";

export async function getCurrentUserOrders() {
  const response = await api.get("/api/v1/orders/user");
  return response.data;
}

export async function getOrderById(orderId) {
  const response = await api.get(
    `/api/v1/orders/${orderId}`,
  );

  return response.data;
}

export async function createOrder(cartItems) {
  const items = cartItems.map((item) => ({
    productId: item.productId,
    quantity: item.quantity,
  }));

  const response = await api.post("/api/v1/orders", {
    items,
  });

  return response.data;
}

export async function cancelOrder(orderId) {
  const response = await api.delete(
    `/api/v1/orders/${orderId}`,
  );

  return response.data;
}