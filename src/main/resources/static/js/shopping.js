let shoppingList = null;
let userSettings = null;

const selectedOptions = new Map();

document.addEventListener("DOMContentLoaded", async () => {
    document
        .getElementById("calculateButton")
        .addEventListener(
            "click",
            calculateShoppingList
        );

    document
        .getElementById("useRecommendationButton")
        .addEventListener(
            "click",
            useRecommendation
        );

    await initialiseShoppingPage();
});

async function initialiseShoppingPage() {
    const generatedMealPlan =
        readStoredJson("generatedMealPlan");

    shoppingList =
        generatedMealPlan?.shoppingList ||
        readStoredJson("shoppingList");

    if (
        !shoppingList ||
        !Array.isArray(shoppingList.items)
    ) {
        document.getElementById(
            "emptyShoppingList"
        ).hidden = false;

        return;
    }

    document.getElementById(
        "shoppingContent"
    ).hidden = false;

    await loadUserSettings();
    initialiseSelections();
    renderShoppingItems();
    renderMissingIngredients();
    updatePreview();
}

async function loadUserSettings() {
    const userId = Number(shoppingList.userId);

    try {
        const response = await fetch(
            `/api/users/${userId}`
        );

        if (response.ok) {
            userSettings = await response.json();
        }
    } catch (error) {
        console.error(
            "Could not load user settings",
            error
        );
    }
}

function initialiseSelections() {
    const saved = readStoredJson(
        "shoppingSelections"
    );

    const recommendation =
        shoppingList.recommendation?.selections || [];

    shoppingList.items.forEach((item, itemIndex) => {
        let selectedOption = null;

        if (
            saved &&
            Number(saved.userId) ===
            Number(shoppingList.userId)
        ) {
            const savedSelection =
                saved.selections?.find(
                    selection =>
                        selection.itemIndex === itemIndex
                );

            selectedOption = item.options.find(
                option =>
                    Number(option.productId) ===
                    Number(savedSelection?.productId)
            );
        }

        if (!selectedOption) {
            const recommendedSelection =
                recommendation.find(
                    selection =>
                        selection.ingredient ===
                        item.ingredient
                );

            selectedOption = item.options.find(
                option =>
                    Number(option.productId) ===
                    Number(
                        recommendedSelection?.productId
                    )
            );
        }

        if (!selectedOption) {
            selectedOption = item.options[0];
        }

        if (selectedOption) {
            selectedOptions.set(
                itemIndex,
                selectedOption
            );
        }
    });

    saveSelections();
}

function renderShoppingItems() {
    const container = document.getElementById(
        "shoppingItems"
    );

    container.replaceChildren();

    shoppingList.items.forEach((item, itemIndex) => {
        const card = document.createElement("article");
        card.className = "shopping-item";

        const heading =
            document.createElement("div");

        heading.className = "shopping-item-heading";

        const title = document.createElement("h3");
        title.textContent = item.ingredient;

        const required =
            document.createElement("span");

        required.textContent =
            `Required: ${item.amount} ${item.unit}`;

        heading.append(title, required);
        card.appendChild(heading);

        const optionGrid =
            document.createElement("div");

        optionGrid.className = "product-option-grid";

        item.options.forEach(option => {
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
                    selectedOptions
                        .get(itemIndex)
                        ?.productId
                ) === Number(option.productId);

            radio.addEventListener("change", () => {
                selectedOptions.set(
                    itemIndex,
                    option
                );

                localStorage.removeItem(
                    "shoppingCalculation"
                );

                saveSelections();
                updatePreview();

                document.getElementById(
                    "shoppingStatus"
                ).textContent =
                    "Selection changed. Calculate again to confirm the total.";
            });

            const content =
                document.createElement("div");

            content.className =
                "product-option-content";

            const name =
                document.createElement("strong");

            name.textContent = option.productName;

            const store =
                document.createElement("span");

            store.className = "store-badge";
            store.textContent = option.store;

            const details =
                document.createElement("p");

            details.textContent =
                `${formatMoney(option.price)} each · ` +
                `${formatPackage(option)}`;

            const total =
                document.createElement("p");

            total.className = "option-total";
            total.textContent =
                `${option.requiredPackages} package(s) · ` +
                `${formatMoney(
                    option.calculatedPrice
                )} total`;

            content.append(
                name,
                store,
                details,
                total
            );

            label.append(radio, content);
            optionGrid.appendChild(label);
        });

        card.appendChild(optionGrid);
        container.appendChild(card);
    });
}

function useRecommendation() {
    const recommendation =
        shoppingList.recommendation;

    if (
        !recommendation ||
        !recommendation.possible
    ) {
        document.getElementById(
            "shoppingStatus"
        ).textContent =
            "No complete recommendation is available.";

        return;
    }

    shoppingList.items.forEach((item, itemIndex) => {
        const recommendedSelection =
            recommendation.selections.find(
                selection =>
                    selection.ingredient ===
                    item.ingredient
            );

        const recommendedOption = item.options.find(
            option =>
                Number(option.productId) ===
                Number(
                    recommendedSelection?.productId
                )
        );

        if (recommendedOption) {
            selectedOptions.set(
                itemIndex,
                recommendedOption
            );
        }
    });

    localStorage.removeItem(
        "shoppingCalculation"
    );

    saveSelections();
    renderShoppingItems();
    updatePreview();

    document.getElementById(
        "shoppingStatus"
    ).textContent =
        "The recommended products are selected.";
}

async function calculateShoppingList() {
    const selections = getSelectedProducts();

    if (
        selections.length !==
        shoppingList.items.length
    ) {
        document.getElementById(
            "shoppingStatus"
        ).textContent =
            "Choose one product for every ingredient.";

        return;
    }

    const calculateButton = document.getElementById(
        "calculateButton"
    );

    calculateButton.disabled = true;
    calculateButton.textContent = "Calculating...";

    try {
        const response = await fetch(
            "/api/shopping-lists/calculate",
            {
                method: "POST",
                headers: {
                    "Content-Type":
                        "application/json"
                },
                body: JSON.stringify({
                    userId: shoppingList.userId,
                    selections: selections.map(
                        selection => ({
                            ingredient:
                            selection.ingredient,
                            productId:
                            selection.productId,
                            productName:
                            selection.productName,
                            price:
                            selection.price,
                            store:
                            selection.store,
                            quantity:
                            selection.quantity
                        })
                    )
                })
            }
        );

        const responseBody = await response
            .json()
            .catch(() => null);

        if (!response.ok) {
            throw new Error(
                responseBody?.message ||
                `Calculation failed with status ${response.status}`
            );
        }

        localStorage.setItem(
            "shoppingCalculation",
            JSON.stringify(responseBody)
        );

        renderCalculation(responseBody);
    } catch (error) {
        console.error(error);

        document.getElementById(
            "shoppingStatus"
        ).textContent =
            error.message ||
            "Could not calculate the shopping list.";
    } finally {
        calculateButton.disabled = false;
        calculateButton.textContent =
            "Calculate total";
    }
}

function getSelectedProducts() {
    return shoppingList.items
        .map((item, itemIndex) => {
            const option =
                selectedOptions.get(itemIndex);

            if (!option) {
                return null;
            }

            return {
                ingredient: item.ingredient,
                productId: option.productId,
                productName: option.productName,
                price: Number(option.price),
                store: option.store,
                quantity:
                    Number(option.requiredPackages),
                calculatedPrice:
                    Number(option.calculatedPrice)
            };
        })
        .filter(Boolean);
}

function updatePreview() {
    const selections = getSelectedProducts();

    const budget = Number(
        shoppingList.weeklyBudget
    );

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

function renderCalculation(calculation) {
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

    document.getElementById(
        "shoppingStatus"
    ).textContent =
        problems.length === 0
            ? "The shopping list is within the budget and store limit."
            : problems.join(" ");

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
    document.getElementById(
        "shoppingBudget"
    ).textContent = formatMoney(budget);

    document.getElementById(
        "shoppingTotal"
    ).textContent = formatMoney(total);

    document.getElementById(
        "shoppingRemaining"
    ).textContent = formatMoney(remaining);

    document.getElementById(
        "storesUsedCount"
    ).textContent = storesUsed;

    document.getElementById(
        "maximumStores"
    ).textContent =
        maximumStores || "—";
}

function renderStoreBreakdown(selections) {
    const container = document.getElementById(
        "storeBreakdown"
    );

    container.replaceChildren();

    const stores = new Map();

    selections.forEach(selection => {
        if (!stores.has(selection.store)) {
            stores.set(selection.store, []);
        }

        stores.get(selection.store).push(selection);
    });

    [...stores.entries()]
        .sort(([storeA], [storeB]) =>
            storeA.localeCompare(storeB)
        )
        .forEach(([store, products]) => {
            const card =
                document.createElement("article");

            card.className = "store-card";

            const heading =
                document.createElement("h3");

            heading.textContent = store;

            const list =
                document.createElement("ul");

            products.forEach(product => {
                const item =
                    document.createElement("li");

                const quantity =
                    product.quantity ||
                    product.requiredPackages ||
                    1;

                item.textContent =
                    `${product.productName} × ${quantity}`;

                list.appendChild(item);
            });

            card.append(heading, list);
            container.appendChild(card);
        });
}

function renderMissingIngredients() {
    const missing =
        shoppingList.missingIngredients || [];

    if (missing.length === 0) {
        return;
    }

    const section = document.getElementById(
        "missingSection"
    );

    const list = document.getElementById(
        "missingIngredients"
    );

    section.hidden = false;
    list.replaceChildren();

    missing.forEach(ingredient => {
        const item = document.createElement("li");
        item.textContent = ingredient;
        list.appendChild(item);
    });
}

function saveSelections() {
    const selections = shoppingList.items.map(
        (item, itemIndex) => ({
            itemIndex: itemIndex,
            productId:
            selectedOptions.get(itemIndex)?.productId
        })
    );

    localStorage.setItem(
        "shoppingSelections",
        JSON.stringify({
            userId: shoppingList.userId,
            selections: selections
        })
    );
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

function readStoredJson(key) {
    try {
        const value = localStorage.getItem(key);

        return value
            ? JSON.parse(value)
            : null;
    } catch {
        return null;
    }
}