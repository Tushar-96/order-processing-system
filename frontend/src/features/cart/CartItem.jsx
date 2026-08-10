import "./CartItem.css";

const priceFormatter = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
});

function CartItem({
  item,
  onUpdateQuantity,
  onRemove,
}) {
  function decreaseQuantity() {
    onUpdateQuantity(
      item.productId,
      item.quantity - 1,
    );
  }

  function increaseQuantity() {
    onUpdateQuantity(
      item.productId,
      item.quantity + 1,
    );
  }

  function removeItem() {
    onRemove(item.productId);
  }

  return (
    <article className="cart-item">
      <div className="cart-item__details">
        <h2>{item.name}</h2>

        <p>
          {priceFormatter.format(item.price)} each
        </p>

        <p>
          Available stock: {item.availableQuantity}
        </p>
      </div>

      <div
        className="quantity-controls"
        aria-label={`Change quantity for ${item.name}`}
      >
        <button
          type="button"
          aria-label={`Decrease ${item.name} quantity`}
          disabled={item.quantity <= 1}
          onClick={decreaseQuantity}
        >
          &minus;
        </button>

        <span aria-label={`Quantity: ${item.quantity}`}>
          {item.quantity}
        </span>

        <button
          type="button"
          aria-label={`Increase ${item.name} quantity`}
          disabled={
            item.quantity >= item.availableQuantity
          }
          onClick={increaseQuantity}
        >
          +
        </button>
      </div>

      <strong className="cart-item__subtotal">
        {priceFormatter.format(
          item.price * item.quantity,
        )}
      </strong>

      <button
        type="button"
        className="remove-button"
        onClick={removeItem}
      >
        Remove
      </button>
    </article>
  );
}

export default CartItem;