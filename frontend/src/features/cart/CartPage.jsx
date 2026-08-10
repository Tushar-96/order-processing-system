import { Link } from "react-router-dom";

import CartItem from "./CartItem";
import { useCart } from "./CartContext";
import "./CartPage.css";

const priceFormatter = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
});

function CartPage() {
  const {
    cartItems,
    itemCount,
    cartTotal,
    updateQuantity,
    removeFromCart,
    clearCart,
  } = useCart();

  function handleClearCart() {
    const confirmed = window.confirm(
      "Remove all products from your cart?",
    );

    if (confirmed) {
      clearCart();
    }
  }

  function handlePlaceOrder() {
    // We will connect this to POST /api/v1/orders
    // in the next step.
  }

  if (cartItems.length === 0) {
    return (
      <main className="cart-page">
        <section className="empty-cart">
          <h1>Your cart is empty</h1>

          <p>
            Add products to your cart before placing an
            order.
          </p>

          <Link to="/products">
            Browse products
          </Link>
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
            {itemCount}{" "}
            {itemCount === 1 ? "item" : "items"}
          </p>
        </div>

        <button
          type="button"
          className="clear-button"
          onClick={handleClearCart}
        >
          Clear cart
        </button>
      </header>

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
            <span>Total</span>

            <strong>
              {priceFormatter.format(cartTotal)}
            </strong>
          </div>

          <button
            type="button"
            className="checkout-button"
            onClick={handlePlaceOrder}
          >
            Place order
          </button>

          <p className="summary-note">
            Stock and prices will be verified when the order
            is submitted.
          </p>
        </aside>
      </section>
    </main>
  );
}

export default CartPage;