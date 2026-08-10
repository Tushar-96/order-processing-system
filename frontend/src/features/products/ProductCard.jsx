import { useState } from "react";

import { useCart } from "../cart/CartContext";
import "./ProductCard.css";

function ProductCard({ product }) {
  const { addToCart, cartItems } = useCart();
  const [message, setMessage] = useState("");

  const existingItem = cartItems.find(
    (item) => item.productId === product.id,
  );

  const quantityInCart = existingItem?.quantity || 0;

  const isOutOfStock = product.availableQuantity <= 0;

  const reachedStockLimit =
    quantityInCart >= product.availableQuantity;

  const formattedPrice = new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
  }).format(Number(product.price));

  function handleAddToCart() {
    if (reachedStockLimit) {
      setMessage("Maximum available quantity reached.");
      return;
    }

    const wasAdded = addToCart(product);

    if (wasAdded) {
      setMessage("Added to cart.");
    }
  }

  return (
    <article className="product-card">
      <div className="product-card__content">
        <h2>{product.name}</h2>

        <p className="product-description">
          {product.description}
        </p>

        <p className="product-price">
          {formattedPrice}
        </p>

        <p
          className={
            isOutOfStock
              ? "stock-status stock-status--empty"
              : "stock-status"
          }
        >
          {isOutOfStock
            ? "Out of stock"
            : `${product.availableQuantity} available`}
        </p>

        {quantityInCart > 0 && (
          <p>{quantityInCart} currently in cart</p>
        )}

        {message && (
          <p className="cart-feedback" role="status">
            {message}
          </p>
        )}
      </div>

      <button
        type="button"
        onClick={handleAddToCart}
        disabled={isOutOfStock || reachedStockLimit}
      >
        {isOutOfStock
          ? "Unavailable"
          : reachedStockLimit
            ? "Stock limit reached"
            : "Add to cart"}
      </button>
    </article>
  );
}

export default ProductCard;