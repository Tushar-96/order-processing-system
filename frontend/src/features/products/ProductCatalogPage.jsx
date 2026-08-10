import { useCallback, useEffect, useState } from "react";

import ProductCard from "./ProductCard";
import { getProducts } from "./productService";
import { getApiError } from "../../utils/apiError";
import "./ProductCatalogPage.css";

function ProductCatalogPage() {
  const [products, setProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  const loadProducts = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage("");

    try {
      const productData = await getProducts();
      setProducts(productData);
    } catch (error) {
      const apiError = getApiError(error);

      setErrorMessage(apiError.message);
      setProducts([]);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadProducts();
  }, [loadProducts]);

  if (isLoading) {
    return (
      <main className="catalog-page">
        <p>Loading products...</p>
      </main>
    );
  }

  if (errorMessage) {
    return (
      <main className="catalog-page">
        <section className="catalog-message" role="alert">
          <h1>Products unavailable</h1>
          <p>{errorMessage}</p>

          <button type="button" onClick={loadProducts}>
            Try again
          </button>
        </section>
      </main>
    );
  }

  return (
    <main className="catalog-page">
      <header className="catalog-header">
        <div>
          <h1>Product Catalog</h1>
          <p>Browse products currently available to order.</p>
        </div>

        <button type="button" onClick={loadProducts}>
          Refresh
        </button>
      </header>

      {products.length === 0 ? (
        <section className="catalog-message">
          <h2>No products available</h2>
          <p>The catalog is currently empty.</p>
        </section>
      ) : (
        <section
          className="product-grid"
          aria-label="Available products"
        >
          {products.map((product) => (
            <ProductCard
              key={product.id}
              product={product}
            />
          ))}
        </section>
      )}
    </main>
  );
}

export default ProductCatalogPage;