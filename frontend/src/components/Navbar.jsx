import { NavLink, useNavigate } from "react-router-dom";

import { useAuth } from "../context/AuthContext";
import { useCart } from "../features/cart/CartContext";
import "./Navbar.css";

function Navbar() {
  const navigate = useNavigate();

  const {
    user,
    isAuthenticated,
    logout,
  } = useAuth();

  const { itemCount } = useCart();

  function handleLogout() {
    logout();
    navigate("/login", { replace: true });
  }

  function getLinkClass({ isActive }) {
    return isActive
      ? "navbar-link navbar-link--active"
      : "navbar-link";
  }

  return (
    <header className="navbar">
      <NavLink to="/" className="navbar-brand">
        OrderFlow
      </NavLink>

      <nav className="navbar-links" aria-label="Main navigation">
        <NavLink to="/products" className={getLinkClass}>
          Products
        </NavLink>

        {isAuthenticated && (
          <>
            <NavLink to="/cart" className={getLinkClass}>
              Cart
              {itemCount > 0 && (
                <span className="cart-count">
                  {itemCount}
                </span>
              )}
            </NavLink>

            <NavLink to="/orders" className={getLinkClass}>
              Orders
            </NavLink>
          </>
        )}
      </nav>

      <div className="navbar-account">
        {isAuthenticated ? (
          <>
            <span className="navbar-user">
              {user?.fullName || user?.email}
            </span>

            <button type="button" onClick={handleLogout}>
              Logout
            </button>
          </>
        ) : (
          <>
            <NavLink to="/login" className={getLinkClass}>
              Login
            </NavLink>

            <NavLink
              to="/register"
              className="register-link"
            >
              Register
            </NavLink>
          </>
        )}
      </div>
    </header>
  );
}

export default Navbar;