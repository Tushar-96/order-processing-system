import { useCallback, useEffect, useState } from "react";

import LoadingSpinner from "../../components/LoadingSpinner";
import { getApiError } from "../../utils/apiError";
import {
  cancelOrder,
  getCurrentUserOrders,
} from "./orderService";
import "./OrderHistoryPage.css";

const priceFormatter = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
});

const cancellableStatuses = ["PENDING", "CONFIRMED"];

function OrderHistoryPage() {
  const [orders, setOrders] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [cancellingId, setCancellingId] = useState(null);
  const [errorMessage, setErrorMessage] = useState("");

  const loadOrders = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage("");

    try {
      const data = await getCurrentUserOrders();
      setOrders(data);
    } catch (error) {
      setErrorMessage(getApiError(error).message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadOrders();
  }, [loadOrders]);

  async function handleCancel(orderId) {
    const confirmed = window.confirm(
      `Cancel order #${orderId}?`,
    );

    if (!confirmed) {
      return;
    }

    setCancellingId(orderId);
    setErrorMessage("");

    try {
      const updatedOrder = await cancelOrder(orderId);

      setOrders((currentOrders) =>
        currentOrders.map((order) =>
          order.id === orderId ? updatedOrder : order,
        ),
      );
    } catch (error) {
      setErrorMessage(getApiError(error).message);
    } finally {
      setCancellingId(null);
    }
  }

  if (isLoading) {
    return <LoadingSpinner message="Loading orders..." />;
  }

  return (
    <main className="orders-page">
      <header className="orders-header">
        <div>
          <h1>Order History</h1>
          <p>View and manage your previous orders.</p>
        </div>

        <button type="button" onClick={loadOrders}>
          Refresh
        </button>
      </header>

      {errorMessage && (
        <div className="orders-error" role="alert">
          {errorMessage}
        </div>
      )}

      {orders.length === 0 ? (
        <section className="empty-orders">
          <h2>No orders yet</h2>
          <p>Your submitted orders will appear here.</p>
        </section>
      ) : (
        <section className="orders-list">
          {orders.map((order) => {
            const canCancel =
              cancellableStatuses.includes(order.status);

            return (
              <article className="order-card" key={order.id}>
                <div className="order-card__header">
                  <div>
                    <h2>Order #{order.id}</h2>
                    <time dateTime={order.createdAt}>
                      {new Date(
                        order.createdAt,
                      ).toLocaleString("en-IN")}
                    </time>
                  </div>

                  <span
                    className={`order-status order-status--${order.status.toLowerCase()}`}
                  >
                    {order.status.replaceAll("_", " ")}
                  </span>
                </div>

                <div className="order-details">
                  <span>Total</span>
                  <strong>
                    {priceFormatter.format(
                      Number(order.totalAmount),
                    )}
                  </strong>
                </div>

                {order.inventoryMessage && (
                  <p className="inventory-message">
                    {order.inventoryMessage}
                  </p>
                )}

                {canCancel && (
                  <button
                    type="button"
                    className="cancel-order-button"
                    disabled={cancellingId === order.id}
                    onClick={() => handleCancel(order.id)}
                  >
                    {cancellingId === order.id
                      ? "Cancelling..."
                      : "Cancel order"}
                  </button>
                )}
              </article>
            );
          })}
        </section>
      )}
    </main>
  );
}

export default OrderHistoryPage;