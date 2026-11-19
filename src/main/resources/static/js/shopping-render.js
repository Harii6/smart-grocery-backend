function getElement(id) {
    const element = document.getElementById(id);

    if (!element) {
        throw new Error(
            `Required HTML element was not found: ${id}`
        );
    }

    return element;
}

export function showEmptyShoppingList() {
    getElement("emptyShoppingList").hidden = false;
    getElement("shoppingContent").hidden = true;
}

export function showShoppingContent() {
    getElement("emptyShoppingList").hidden = true;
    getElement("shoppingContent").hidden = false;
}

export function setShoppingStatus(message) {
    getElement("shoppingStatus").textContent =
        message;
}

export function setCalculateButtonLoading(
    loading
) {
    const button =
        getElement("calculateButton");

    button.disabled = loading;

    button.textContent = loading
        ? "Calculating..."
        : "Calculate total";
}

export function renderShoppingItems(
    shoppingList,
    selectedOptions,
    onSelectionChange
) {
    const container =
        getElement("shoppingItems");

    container.replaceChildren();

    shoppingList.items.forEach(
        (item, itemIndex) => {
            const card =
                document.createElement("article");

            card.className = "shopping-item";

            const heading =
                document.createElement("div");

            heading.className =
                "shopping-item-heading";

            const title =
                document.createElement("h3");

            title.textContent = item.ingredient;

            const required =
                document.createElement("span");

            required.textContent =
                `Required: ${item.amount} ${item.unit}`;

            heading.append(title, required);
            card.appendChild(heading);

            const optionGrid =
                document.createElement("div");

            optionGrid.className =
                "product-option-grid";

            item.options.forEach(option => {
                const label =
                    createProductOption(
                        itemIndex,
                        option,
                        selectedOptions,
                        onSelectionChange
                    );

                optionGrid.appendChild(label);
            });

            card.appendChild(optionGrid);
            container.appendChild(card);
        }
    );
}

function createProductOption(
    itemIndex,
    option,
    selectedOptions,
    onSelectionChange
) {
    const label =
        document.createElement("label");

    label.className = "product-option";

    const radio =
        document.createElement("input");

    radio.type = "radio";
    radio.name = `ingredient-${itemIndex}`;
    radio.value = option.productId;

    radio.checked =
        Number(
            selectedOptions.get(itemIndex)?.productId
        ) === Number(option.productId);

    radio.addEventListener("change", () => {
        onSelectionChange(itemIndex, option);
    });

    const main =
        document.createElement("div");

    main.className = "product-option-main";

    const name =
        document.createElement("strong");

    name.className = "product-option-name";
    name.textContent = option.productName;

    const metadata =
        document.createElement("div");

    metadata.className = "product-option-metadata";

    const store =
        document.createElement("span");

    store.className = "product-option-store";
    store.textContent =
        option.store || "Unknown store";

    const packageSize =
        document.createElement("span");

    packageSize.className = "product-option-package";
    packageSize.textContent =
        formatPackage(option);

    metadata.append(store, packageSize);
    main.append(name, metadata);

    const price =
        document.createElement("strong");

    price.className = "product-option-price";

    price.textContent = formatMoney(
        option.calculatedPrice ?? option.price
    );

    label.append(radio, main, price);

    return label;
}

export function renderPreview(
    shoppingList,
    userSettings,
    selections
) {
    const budget =
        Number(shoppingList.weeklyBudget);

    const total = selections.reduce(
        (sum, selection) =>
            sum + selection.calculatedPrice,
        0
    );

    const remaining = budget - total;

    const stores = [
        ...new Set(
            selections.map(
                selection => selection.store
            )
        )
    ];

    const maximumStores =
        Number(
            userSettings?.maximumStores || 0
        );

    updateSummaryValues(
        budget,
        total,
        remaining,
        stores.length,
        maximumStores
    );

    renderStoreBreakdown(selections);
}

export function renderCalculation(calculation) {
    updateSummaryValues(
        Number(calculation.weeklyBudget),
        Number(calculation.totalPrice),
        Number(calculation.remainingBudget),
        calculation.storesUsed.length,
        Number(calculation.maximumStores)
    );

    const problems = [];

    if (!calculation.withinBudget) {
        problems.push(
            "The shopping list exceeds the weekly budget."
        );
    }

    if (!calculation.withinStoreLimit) {
        problems.push(
            "The selected products use too many stores."
        );
    }

    setShoppingStatus(
        problems.length === 0
            ? "The shopping list is within the budget and store limit."
            : problems.join(" ")
    );

    renderStoreBreakdown(
        calculation.selections
    );
}

function updateSummaryValues(
    budget,
    total,
    remaining,
    storesUsed,
    maximumStores
) {
    getElement("shoppingBudget").textContent =
        formatMoney(budget);

    getElement("shoppingTotal").textContent =
        formatMoney(total);

    getElement("shoppingRemaining").textContent =
        formatMoney(remaining);

    getElement("storesUsedCount").textContent =
        storesUsed;

    getElement("maximumStores").textContent =
        maximumStores || "—";
}

export function renderStoreBreakdown(selections) {
    const container = getElement("storeBreakdown");

    container.replaceChildren();

    if (!Array.isArray(selections) || selections.length === 0) {
        return;
    }

    const stores = new Map();

    selections.forEach(selection => {
        const storeName =
            selection.store || "Unknown store";

        if (!stores.has(storeName)) {
            stores.set(storeName, []);
        }

        stores.get(storeName).push(selection);
    });

    [...stores.entries()]
        .sort(([storeA], [storeB]) =>
            storeA.localeCompare(storeB)
        )
        .forEach(([store, products]) => {
            const card =
                document.createElement("details");

            card.className = "store-card";

            const summary =
                document.createElement("summary");

            summary.className = "store-card-summary";

            const storeName =
                document.createElement("span");

            storeName.className = "store-card-name";
            storeName.textContent = store;

            const itemCount =
                document.createElement("span");

            itemCount.className = "store-card-count";

            itemCount.textContent =
                products.length === 1
                    ? "1 item"
                    : `${products.length} items`;

            summary.append(storeName, itemCount);

            const productList =
                document.createElement("div");

            productList.className = "store-product-list";

            products.forEach(product => {
                const row =
                    document.createElement("div");

                row.className = "store-product-row";

                const productName =
                    document.createElement("span");

                productName.className =
                    "store-product-name";

                productName.textContent =
                    product.productName;

                const quantity =
                    product.quantity ??
                    product.requiredPackages ??
                    1;

                const quantityElement =
                    document.createElement("span");

                quantityElement.className =
                    "store-product-quantity";

                quantityElement.textContent =
                    `× ${quantity}`;

                row.append(
                    productName,
                    quantityElement
                );

                productList.appendChild(row);
            });

            card.append(summary, productList);
            container.appendChild(card);
        });
}

export function renderMissingIngredients(
    shoppingList
) {
    const section =
        getElement("missingSection");

    const list =
        getElement("missingIngredients");

    const missing =
        shoppingList.missingIngredients || [];

    list.replaceChildren();

    if (missing.length === 0) {
        section.hidden = true;
        return;
    }

    section.hidden = false;

    missing.forEach(ingredient => {
        const item =
            document.createElement("li");

        item.textContent = ingredient;
        list.appendChild(item);
    });
}

function formatPackage(option) {
    if (
        Number(option.packageSize) <= 0 ||
        !option.packageUnit
    ) {
        return "package size unavailable";
    }

    return `${option.packageSize} ${option.packageUnit}`;
}

function formatMoney(value) {
    return new Intl.NumberFormat(
        "nb-NO",
        {
            style: "currency",
            currency: "NOK"
        }
    ).format(Number(value));
}