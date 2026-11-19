const USER_ID_KEY = "userId";
const LOCATION_SETTINGS_KEY = "locationSettings";

document.addEventListener("DOMContentLoaded", () => {
    loadLocation();
    loadDashboard();
});


/* ========================================
   Location
======================================== */

function loadLocation() {
    const location = readStoredJson(
        LOCATION_SETTINGS_KEY
    );

    const locationElement =
        document.getElementById("home-location");

    const radiusElement =
        document.getElementById("home-radius");

    if (!location || !location.address) {
        locationElement.textContent =
            "No location selected";

        radiusElement.textContent = "—";

        showStoreMessage(
            "Add your location in Settings to find nearby grocery stores."
        );

        return;
    }

    locationElement.textContent =
        location.address;

    radiusElement.textContent =
        Number(location.radius) || 3;
}


/* ========================================
   Main dashboard
======================================== */

async function loadDashboard() {
    const userId = Number(
        localStorage.getItem(USER_ID_KEY)
    );

    if (!userId) {
        showBudgetMessage(
            "Create a profile before viewing your weekly budget."
        );

        return;
    }

    try {
        const response = await fetch(
            `/api/users/${userId}`
        );

        if (!response.ok) {
            throw new Error(
                `Could not load user: ${response.status}`
            );
        }

        const user = await response.json();

        const shoppingCalculation =
            readStoredJson("shoppingCalculation");

        const selections = getCurrentSelections();

        updateBudget(
            user,
            shoppingCalculation,
            selections,
            userId
        );

        renderBudgetCategories(
            selections,
            shoppingCalculation
        );

        renderStores(selections);
    } catch (error) {
        console.error(error);

        showBudgetMessage(
            "Could not load the weekly budget."
        );
    }
}


/* ========================================
   Budget totals
======================================== */

function updateBudget(
    user,
    shoppingCalculation,
    selections,
    userId
) {
    const weeklyBudget =
        Number(user.weeklyBudget) || 0;

    document.getElementById(
        "weeklyBudget"
    ).textContent = formatCurrency(weeklyBudget);

    const validCalculation =
        shoppingCalculation &&
        Number(shoppingCalculation.userId) === userId &&
        Number.isFinite(
            Number(shoppingCalculation.totalPrice)
        );

    const hasGeneratedSelections =
        Array.isArray(selections) &&
        selections.length > 0;

    if (
        !validCalculation &&
        !hasGeneratedSelections
    ) {
        document.getElementById(
            "selectedTotal"
        ).textContent = "—";

        document.getElementById(
            "remainingBudget"
        ).textContent =
            formatCurrency(weeklyBudget);

        return;
    }

    const selectedTotal =
        validCalculation
            ? Number(shoppingCalculation.totalPrice)
            : selections.reduce(
                (sum, selection) =>
                    sum + getSelectionPrice(selection),
                0
            );

    document.getElementById(
        "selectedTotal"
    ).textContent =
        formatCurrency(selectedTotal);

    document.getElementById(
        "remainingBudget"
    ).textContent =
        formatCurrency(
            weeklyBudget - selectedTotal
        );
}

/* ========================================
   Category spending
======================================== */

function renderBudgetCategories(
    selections,
    shoppingCalculation
) {
    const container =
        document.getElementById(
            "budget-category-list"
        );

    container.innerHTML = "";

    const categorizedSelections =
        selections.filter(selection => {
            return Boolean(
                getSelectionCategory(selection)
            );
        });

    if (categorizedSelections.length === 0) {
        showBudgetMessage(
            "Generate meals to see the latest categories and stores."
        );

        return;
    }

    const categoryTotals = new Map();

    for (const selection of categorizedSelections) {
        const categoryCode =
            normaliseCategoryCode(
                getSelectionCategory(selection)
            );

        const currentTotal =
            categoryTotals.get(categoryCode) || 0;

        categoryTotals.set(
            categoryCode,
            currentTotal +
            getSelectionPrice(selection)
        );
    }

    const selectedTotal =
        Number(
            shoppingCalculation?.totalPrice
        ) || Array.from(
            categoryTotals.values()
        ).reduce(
            (sum, value) => sum + value,
            0
        );

    for (
        const [categoryCode, amount]
        of categoryTotals
        ) {
        const category =
            getCategoryInformation(categoryCode);

        const percentage =
            selectedTotal > 0
                ? Math.min(
                    100,
                    Math.max(
                        0,
                        (amount / selectedTotal) * 100
                    )
                )
                : 0;

        const item =
            document.createElement("div");

        item.className =
            "budget-category-item";

        const header =
            document.createElement("div");

        header.className =
            "budget-category-header";

        const name =
            document.createElement("div");

        name.className =
            "budget-category-name";

        const symbol =
            document.createElement("span");

        symbol.className =
            "budget-category-symbol";

        symbol.textContent =
            category.symbol;

        const label =
            document.createElement("span");

        label.textContent =
            category.label;

        name.append(symbol, label);

        const amountElement =
            document.createElement("span");

        amountElement.className =
            "budget-category-amount";

        amountElement.textContent =
            `${formatCurrency(amount)} · ${Math.round(percentage)}%`;

        header.append(
            name,
            amountElement
        );

        const progressTrack =
            document.createElement("div");

        progressTrack.className =
            "category-progress-track";

        const progressValue =
            document.createElement("div");

        progressValue.className =
            "category-progress-value";

        progressValue.style.width =
            `${percentage}%`;

        progressTrack.append(progressValue);

        item.append(
            header,
            progressTrack
        );

        container.append(item);
    }
}


function getSelectionCategory(selection) {
    return (
        selection.category ||
        selection.ingredientCategory ||
        selection.ingredient?.category ||
        selection.product?.category ||
        null
    );
}


function normaliseCategoryCode(category) {
    return String(category)
        .trim()
        .toUpperCase()
        .replaceAll(" ", "_")
        .replaceAll("&", "AND");
}


function getCategoryInformation(code) {
    const categories = {
        MEAT_FISH: {
            label: "Meat & fish",
            symbol: "🥩"
        },

        DAIRY: {
            label: "Dairy",
            symbol: "🧀"
        },

        FRUIT_VEGETABLES: {
            label: "Fruit & vegetables",
            symbol: "🥬"
        },

        BAKERY: {
            label: "Bakery",
            symbol: "🍞"
        },

        PANTRY_OTHER: {
            label: "Pantry & other",
            symbol: "🧺"
        }
    };

    return (
        categories[code] ||
        categories.PANTRY_OTHER
    );
}


/* ========================================
   Store list
======================================== */

function renderStores(selections) {
    const location =
        readStoredJson(LOCATION_SETTINGS_KEY);

    if (!location || !location.address) {
        return;
    }

    const container =
        document.getElementById(
            "nearby-stores-list"
        );

    container.innerHTML = "";

    const stores = new Map();

    for (const selection of selections) {
        const storeName =
            getStoreName(selection);

        if (!storeName) {
            continue;
        }

        if (!stores.has(storeName)) {
            stores.set(storeName, []);
        }

        stores.get(storeName).push(
            getProductName(selection)
        );
    }

    if (stores.size === 0) {
        showStoreMessage(
            "Generate meals to see the latest categories and stores."
        );

        return;
    }

    for (
        const [storeName, products]
        of stores
        ) {
        const item =
            document.createElement("div");

        item.className =
            "nearby-store-item";

        const marker =
            document.createElement("span");

        marker.className =
            "store-marker";

        const information =
            document.createElement("div");

        information.className =
            "nearby-store-information";

        const title =
            document.createElement("h3");

        title.textContent = storeName;

        const description =
            document.createElement("p");

        description.textContent =
            getProductSummary(products);

        information.append(
            title,
            description
        );

        const count =
            document.createElement("span");

        count.className =
            "nearby-store-distance";

        count.textContent =
            products.length === 1
                ? "1 product"
                : `${products.length} products`;

        item.append(
            marker,
            information,
            count
        );

        container.append(item);
    }
}


/* ========================================
   Shopping data helpers
======================================== */

function extractShoppingSelections(
    shoppingCalculation
) {
    const possibleSelections = [
        shoppingCalculation?.selections,
        shoppingCalculation?.selectedProducts,
        shoppingCalculation?.items,
        readStoredJson("shoppingSelections")
    ];

    for (const value of possibleSelections) {
        if (Array.isArray(value)) {
            return value;
        }

        if (
            value &&
            typeof value === "object"
        ) {
            const selections =
                Object.values(value);

            if (selections.length > 0) {
                return selections;
            }
        }
    }

    return [];
}


function getStoreName(selection) {
    return (
        selection.store ||
        selection.storeName ||
        selection.product?.store?.name ||
        selection.product?.store ||
        null
    );
}


function getProductName(selection) {
    return (
        selection.productName ||
        selection.name ||
        selection.product?.name ||
        selection.ingredient?.searchTerm ||
        selection.ingredient ||
        "Product"
    );
}


function getSelectionPrice(selection) {
    const calculatedPrice =
        Number(selection.calculatedPrice);

    if (Number.isFinite(calculatedPrice)) {
        return calculatedPrice;
    }

    const totalPrice =
        Number(selection.totalPrice);

    if (Number.isFinite(totalPrice)) {
        return totalPrice;
    }

    const price =
        Number(selection.price) || 0;

    const quantity =
        Number(selection.quantity) || 1;

    return price * quantity;
}


function getProductSummary(products) {
    const cleanedProducts = products
        .filter(Boolean)
        .map(String);

    if (cleanedProducts.length <= 3) {
        return cleanedProducts.join(", ");
    }

    return (
        cleanedProducts
            .slice(0, 3)
            .join(", ") +
        ` and ${cleanedProducts.length - 3} more`
    );
}


/* ========================================
   Messages and formatting
======================================== */

function showStoreMessage(message) {
    const container =
        document.getElementById(
            "nearby-stores-list"
        );

    container.innerHTML = "";

    const messageElement =
        document.createElement("p");

    messageElement.className =
        "card-empty-message";

    messageElement.textContent = message;

    container.append(messageElement);
}


function showBudgetMessage(message) {
    const container =
        document.getElementById(
            "budget-category-list"
        );

    container.innerHTML = "";

    const messageElement =
        document.createElement("p");

    messageElement.className =
        "card-empty-message";

    messageElement.textContent = message;

    container.append(messageElement);
}


function formatCurrency(value) {
    return new Intl.NumberFormat(
        "nb-NO",
        {
            style: "currency",
            currency: "NOK",
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }
    ).format(Number(value) || 0);
}


function readStoredJson(key) {
    try {
        const value =
            localStorage.getItem(key);

        return value
            ? JSON.parse(value)
            : null;
    } catch {
        return null;
    }
}

function getCurrentSelections() {
    const userId = Number(
        localStorage.getItem(USER_ID_KEY)
    );

    const calculation =
        readStoredJson("shoppingCalculation");

    if (
        Number(calculation?.userId) === userId &&
        Array.isArray(calculation?.selections) &&
        calculation.selections.length > 0
    ) {
        return calculation.selections;
    }

    const generatedPlan =
        readStoredJson("generatedMealPlan");

    const shoppingList =
        generatedPlan?.shoppingList ??
        readStoredJson("shoppingList");

    if (
        Number(shoppingList?.userId) !== userId ||
        !Array.isArray(shoppingList?.items)
    ) {
        return [];
    }

    const saved =
        readStoredJson("shoppingSelections");

    const savedSelections =
        Number(saved?.userId) === userId &&
        Array.isArray(saved?.selections)
            ? saved.selections
            : [];

    const recommendedSelections =
        shoppingList.recommendation?.selections || [];

    return shoppingList.items.map((item, itemIndex) => {
        const options = item.options || [];

        const savedSelection = savedSelections.find(
            selection => selection.itemIndex === itemIndex
        );

        const recommendedSelection =
            recommendedSelections.find(
                selection =>
                    selection.ingredient === item.ingredient
            );

        // Same selection order as your Shopping page.
        const option =
            options.find(
                option =>
                    Number(option.productId) ===
                    Number(savedSelection?.productId)
            ) ??
            options.find(
                option =>
                    Number(option.productId) ===
                    Number(recommendedSelection?.productId)
            ) ??
            options[0];

        if (!option) {
            return null;
        }

        return {
            ingredient: item.ingredient,
            category: item.category,
            productId: option.productId,
            productName: option.productName,
            store: option.store,
            price: Number(option.price),
            quantity: Number(option.requiredPackages),
            calculatedPrice: Number(option.calculatedPrice)
        };
    }).filter(Boolean);
}