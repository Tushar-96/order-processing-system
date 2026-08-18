import { Route, Routes } from "react-router-dom";

import LoginPage from "../features/auth/LoginPage";
import RegisterPage from "../features/auth/RegisterPage";
import CartPage from "../features/cart/CartPage";
import OrderHistoryPage from "../features/orders/OrderHistoryPage";
import ProductCatalogPage from "../features/products/ProductCatalogPage";
import AppLayout from "../layouts/AppLayout";
import HomePage from "../pages/HomePage";
import NotFoundPage from "../pages/NotFoundPage";
import ProtectedRoute from "./ProtectedRoute";
import ForgotPasswordPage from "../features/auth/ForgotPasswordPage";
import ResetPasswordPage from "../features/auth/ResetPasswordPage";

function AppRoutes() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        {/* Public routes */}
        <Route path="/" element={<HomePage />} />

        <Route
          path="/products"
          element={<ProductCatalogPage />}
        />

        <Route path="/login" element={<LoginPage />} />

        <Route
          path="/register"
          element={<RegisterPage />}
        />

        {/* Authentication required */}
        <Route element={<ProtectedRoute />}>
          <Route path="/cart" element={<CartPage />} />

          <Route
            path="/orders"
            element={<OrderHistoryPage />}
          />
        </Route>

        <Route
          path="/forgot-password"
          element={<ForgotPasswordPage />}
        />

        <Route
          path="/reset-password"
          element={<ResetPasswordPage />}
        />

        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}

export default AppRoutes;