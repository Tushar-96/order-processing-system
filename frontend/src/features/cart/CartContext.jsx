import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";

import { useAuth } from "../../context/AuthContext";

const CartContext = createContext(null);
const LEGACY_CART_KEY = "shoppingCart";

function getCartStorageKey(user) {
  if (!user?.email) {
    return null;
  }

  return `shoppingCart:${user.email
    .trim()
    .toLowerCase()}`;
}

function readStoredCart(storageKey) {
  if (!storageKey) {
    return [];
  }

  try {
    const storedCart = localStorage.getItem(storageKey);

    if (!storedCart) {
      return [];
    }

    const parsedCart = JSON.parse(storedCart);

    return Array.isArray(parsedCart)
      ? parsedCart
      : [];
  } catch {
    localStorage.removeItem(storageKey);
    return [];
  }
}

export function CartProvider({ children }) {
  const { user } = useAuth();

  const storageKey = useMemo(
    () => getCartStorageKey(user),
    [user?.email],
  );

  const [cartItems, setCartItems] = useState([]);
  const [loadedStorageKey, setLoadedStorageKey] =
    useState(null);

  /*
   * Load the correct cart whenever the authenticated
   * user changes.
   */
  useEffect(() => {
    if (!storageKey) {
      setCartItems([]);
      setLoadedStorageKey(null);
      return;
    }

    const storedCart = readStoredCart(storageKey);

    setCartItems(storedCart);
    setLoadedStorageKey(storageKey);
  }, [storageKey]);

  /*
   * Save only after the cart belonging to the current
   * user has finished loading.
   *
   * The loadedStorageKey check prevents User 1's cart
   * from accidentally being written into User 2's key
   * during account switching.
   */
  useEffect(() => {
    if (
      !storageKey ||
      loadedStorageKey !== storageKey
    ) {
      return;
    }

    localStorage.setItem(
      storageKey,
      JSON.stringify(cartItems),
    );
  }, [cartItems, storageKey, loadedStorageKey]);

  /*
   * Remove the old shared cart key because it does not
   * belong to any specific account.
   */
  useEffect(() => {
    localStorage.removeItem(LEGACY_CART_KEY);
  }, []);

  function addToCart(product) {
    if (!storageKey || product.availableQuantity <= 0) {
      return false;
    }

    setCartItems((currentItems) => {
      const existingItem = currentItems.find(
        (item) => item.productId === product.id,
      );

      if (existingItem) {
        if (
          existingItem.quantity >=
          product.availableQuantity
        ) {
          return currentItems;
        }

        return currentItems.map((item) =>
          item.productId === product.id
            ? {
                ...item,
                quantity: item.quantity + 1,
                availableQuantity:
                  product.availableQuantity,
              }
            : item,
        );
      }

      return [
        ...currentItems,
        {
          productId: product.id,
          name: product.name,
          price: Number(product.price),
          availableQuantity:
            product.availableQuantity,
          quantity: 1,
        },
      ];
    });

    return true;
  }

  function updateQuantity(productId, quantity) {
    if (!storageKey) {
      return;
    }

    setCartItems((currentItems) =>
      currentItems.map((item) => {
        if (item.productId !== productId) {
          return item;
        }

        const safeQuantity = Math.min(
          Math.max(Number(quantity), 1),
          item.availableQuantity,
        );

        return {
          ...item,
          quantity: safeQuantity,
        };
      }),
    );
  }

  function removeFromCart(productId) {
    if (!storageKey) {
      return;
    }

    setCartItems((currentItems) =>
      currentItems.filter(
        (item) => item.productId !== productId,
      ),
    );
  }

  function clearCart() {
    setCartItems([]);
  }

  const itemCount = cartItems.reduce(
    (total, item) => total + item.quantity,
    0,
  );

  const cartTotal = cartItems.reduce(
    (total, item) =>
      total + item.price * item.quantity,
    0,
  );

  const contextValue = useMemo(
    () => ({
      cartItems,
      itemCount,
      cartTotal,
      addToCart,
      updateQuantity,
      removeFromCart,
      clearCart,
    }),
    [cartItems, itemCount, cartTotal],
  );

  return (
    <CartContext.Provider value={contextValue}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const context = useContext(CartContext);

  if (!context) {
    throw new Error(
      "useCart must be used inside CartProvider",
    );
  }

  return context;
}