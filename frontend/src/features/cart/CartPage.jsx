import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { getApiError } from "../../utils/apiError";
import { createOrder } from "../orders/orderService";
import CartItem from "./CartItem";
import { useCart } from "./CartContext";
import "./CartPage.css";

const priceFormatter = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
});

function CartPage() {
  const navigate = useNavigate();

  const {
    cartItems,
    itemCount,
    cartTotal,
    updateQuantity,
    removeFromCart,
    clearCart,
  } = useCart();

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  function handleClearCart() {
    const confirmed = window.confirm(
      "Remove all products from your cart?",
    );

    if (confirmed) {
      clearCart();
    }
  }

  async function handlePlaceOrder() {
    if (cartItems.length === 0 || isSubmitting) {
      return;
    }

    setIsSubmitting(true);
    setErrorMessage("");

    try {
      const createdOrder = await createOrder(cartItems);

      clearCart();

      navigate("/orders", {
        replace: true,
        state: {
          createdOrderId: createdOrder.id,
          message: `Order #${createdOrder.id} was submitted successfully.`,
        },
      });
    } catch (error) {
      const apiError = getApiError(error);
      setErrorMessage(apiError.message);
    } finally {
      setIsSubmitting(false);
    }
  }

  if (cartItems.length === 0) {
    return (
      <main className="cart-page">
        <section className="empty-cart">
          <h1>Your cart is empty</h1>

          <p>
            Add products to your cart before placing an order.
          </p>

          <Link to="/products">Browse products</Link>
        </section>
      </main>
    );
  }

  return (
    <main className="cart-page">
      <header className="cart-header">
        <div>
          <h1>Shopping Cart</h1>

          <p>
            {itemCount} {itemCount === 1 ? "item" : "items"}
          </p>
        </div>

        <button
          type="button"
          className="clear-button"
          onClick={handleClearCart}
          disabled={isSubmitting}
        >
          Clear cart
        </button>
      </header>

      {errorMessage && (
        <div className="checkout-error" role="alert">
          <strong>Order could not be submitted.</strong>
          <p>{errorMessage}</p>
        </div>
      )}

      <section className="cart-content">
        <div
          className="cart-items"
          aria-label="Products in your cart"
        >
          {cartItems.map((item) => (
            <CartItem
              key={item.productId}
              item={item}
              onUpdateQuantity={updateQuantity}
              onRemove={removeFromCart}
              disabled={isSubmitting}
            />
          ))}
        </div>

        <aside className="cart-summary">
          <h2>Order Summary</h2>

          <div className="summary-row">
            <span>Products</span>
            <span>{cartItems.length}</span>
          </div>

          <div className="summary-row">
            <span>Total quantity</span>
            <span>{itemCount}</span>
          </div>

          <div className="summary-row summary-total">
            <span>Estimated total</span>

            <strong>
              {priceFormatter.format(cartTotal)}
            </strong>
          </div>

          <button
            type="button"
            className="checkout-button"
            onClick={handlePlaceOrder}
            disabled={isSubmitting}
          >
            {isSubmitting
              ? "Submitting order..."
              : "Place order"}
          </button>

          <p className="summary-note">
            The Inventory Service will verify current prices
            and stock after submission.
          </p>
        </aside>
      </section>
    </main>
  );
}

export default CartPage;