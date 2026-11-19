const PRODUCT_SEARCH_ENDPOINT = "/api/products/search";

const searchForm =
    document.getElementById("product-search-form");

const searchInput =
    document.getElementById("product-search-input");

const searchButton =
    document.getElementById("product-search-button");

const searchStatus =
    document.getElementById("product-search-status");

const resultsContainer =
    document.getElementById("product-results");

const currency = new Intl.NumberFormat("nb-NO", {
    style: "currency",
    currency: "NOK"
});

let searching = false;

searchForm.addEventListener("submit", searchProducts);

async function searchProducts(event) {
    event.preventDefault();

    if (searching) {
        return;
    }

    const query = searchInput.value.trim();

    if (query.length < 2) {
        showSearchStatus("Enter at least two characters.", true);
        return;
    }

    searching = true;
    searchButton.disabled = true;
    searchButton.textContent = "Searching...";
    resultsContainer.setAttribute("aria-busy", "true");
    resultsContainer.replaceChildren();

    showSearchStatus("Searching for products...");

    try {
        const parameters = new URLSearchParams({
            query: query
        });

        const response = await fetch(
            `${PRODUCT_SEARCH_ENDPOINT}?${parameters}`,
            {
                headers: {
                    Accept: "application/json"
                }
            }
        );

        const body = await response.json().catch(() => null);

        if (!response.ok) {
            throw new Error(
                body?.message ||
                `Product search failed. Status: ${response.status}`
            );
        }

        let products = Array.isArray(body) ? body : body?.data;

        if (!Array.isArray(products)) {
            throw new Error(
                "The backend returned an unexpected product format."
            );
        }

        products = products
            .filter(product => Number.isFinite(getProductPrice(product)))
            .sort((a, b) => getProductPrice(a) - getProductPrice(b))
            .slice(0, 20);

        if (products.length === 0) {
            showSearchStatus(
                `No products found for "${query}". Try another name.`
            );
            return;
        }

        renderProducts(products);

        showSearchStatus(
            `Showing ${products.length} ${
                products.length === 1 ? "product" : "products"
            } for "${query}".`
        );
    } catch (error) {
        console.error("Product search failed:", error);
        showSearchStatus(error.message, true);
    } finally {
        searching = false;
        searchButton.disabled = false;
        searchButton.textContent = "Search";
        resultsContainer.setAttribute("aria-busy", "false");
    }
}

function renderProducts(products) {
    const fragment = document.createDocumentFragment();

    products.forEach(product => {
        const card = document.createElement("article");
        card.className = "product-result";

        const information = document.createElement("div");
        information.className = "product-result-info";

        const title = document.createElement("h2");
        title.textContent = product.name || "Unnamed product";

        const store = document.createElement("p");
        store.className = "product-result-store";
        store.textContent =
            typeof product.store === "string"
                ? product.store
                : product.store?.name || "Store unavailable";

        const price = document.createElement("span");
        price.className = "product-result-price";

        // Support a Java DTO or the original API field name.
        const rawPrice =
            product.currentPrice ?? product.current_price;

        const value =
            rawPrice && typeof rawPrice === "object"
                ? rawPrice.price
                : rawPrice;

        const numericPrice =
            value == null || value === ""
                ? NaN
                : Number(value);

        price.textContent = Number.isFinite(numericPrice)
            ? currency.format(numericPrice)
            : "Price unavailable";

        information.append(title, store);
        card.append(information, price);
        fragment.append(card);
    });

    resultsContainer.append(fragment);
}

function showSearchStatus(message, isError = false) {
    searchStatus.textContent = message;
    searchStatus.classList.toggle("is-error", isError);
}

function getProductPrice(product) {
    const rawPrice = product.currentPrice ?? product.current_price;

    const value =
        rawPrice !== null && typeof rawPrice === "object"
            ? rawPrice.price
            : rawPrice;

    if (value == null || value === "") {
        return NaN;
    }

    const price = Number(value);

    return Number.isFinite(price) && price >= 0
        ? price
        : NaN;
}