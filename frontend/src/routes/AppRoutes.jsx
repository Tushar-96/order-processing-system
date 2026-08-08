import { Route, Routes } from "react-router-dom";

import NotFoundPage from "../pages/NotFoundPage";
import OrderHistoryPage from "../pages/OrderHistoryPage";
import ProductCatalogPage from "../pages/ProductCatalogPage";

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<ProductCatalogPage />} />
      <Route
        path="/products"
        element={<ProductCatalogPage />}
      />
      <Route
        path="/orders"
        element={<OrderHistoryPage />}
      />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}