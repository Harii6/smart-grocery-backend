import {
    calculateShoppingList,
    fetchUserSettings
} from "./shopping-api.js";

import {
    applyRecommendation,
    getSelectedProducts,
    initialiseSelections,
    selectProduct,
    setShoppingList,
    setUserSettings,
    shoppingState
} from "./shopping-state.js";

import {
    readStoredJson,
    removeStoredValue,
    saveShoppingSelections,
    writeStoredJson
} from "./shopping-storage.js";

import {
    renderCalculation,
    renderMissingIngredients,
    renderPreview,
    renderShoppingItems,
    setCalculateButtonLoading,
    setShoppingStatus,
    showEmptyShoppingList,
    showShoppingContent
} from "./shopping-render.js";

document.addEventListener(
    "DOMContentLoaded",
    async () => {
        document
            .getElementById("calculateButton")
            .addEventListener(
                "click",
                handleCalculateShoppingList
            );

        document
            .getElementById(
                "useRecommendationButton"
            )
            .addEventListener(
                "click",
                handleUseRecommendation
            );

        await initialiseShoppingPage();
    }
);

async function initialiseShoppingPage() {
    const generatedMealPlan =
        readStoredJson("generatedMealPlan");

    const shoppingList =
        generatedMealPlan?.shoppingList ??
        readStoredJson("shoppingList");

    if (
        !shoppingList ||
        !Array.isArray(shoppingList.items)
    ) {
        showEmptyShoppingList();
        return;
    }

    setShoppingList(shoppingList);
    showShoppingContent();

    await loadUserSettings();

    const savedSelections =
        readStoredJson("shoppingSelections");

    initialiseSelections(savedSelections);
    saveCurrentSelections();
    renderCurrentShoppingList();
}

async function loadUserSettings() {
    try {
        const userSettings =
            await fetchUserSettings(
                shoppingState.shoppingList.userId
            );

        setUserSettings(userSettings);
    } catch (error) {
        console.error(
            "Could not load user settings",
            error
        );

        setUserSettings(null);
    }
}

function renderCurrentShoppingList() {
    renderShoppingItems(
        shoppingState.shoppingList,
        shoppingState.selectedOptions,
        handleSelectionChange
    );

    renderMissingIngredients(
        shoppingState.shoppingList
    );

    updateCurrentPreview();
}

function handleSelectionChange(
    itemIndex,
    productOption
) {
    selectProduct(
        itemIndex,
        productOption
    );

    removeStoredValue(
        "shoppingCalculation"
    );

    saveCurrentSelections();
    updateCurrentPreview();

    setShoppingStatus(
        "Selection changed. Calculate again to confirm the total."
    );
}

function handleUseRecommendation() {
    const recommendationWasApplied =
        applyRecommendation();

    if (!recommendationWasApplied) {
        setShoppingStatus(
            "No complete recommendation is available."
        );

        return;
    }

    removeStoredValue(
        "shoppingCalculation"
    );

    saveCurrentSelections();

    renderShoppingItems(
        shoppingState.shoppingList,
        shoppingState.selectedOptions,
        handleSelectionChange
    );

    updateCurrentPreview();

    setShoppingStatus(
        "The recommended products are selected."
    );
}

async function handleCalculateShoppingList() {
    const selections =
        getSelectedProducts();

    if (
        selections.length !==
        shoppingState.shoppingList.items.length
    ) {
        setShoppingStatus(
            "Choose one product for every ingredient."
        );

        return;
    }

    setCalculateButtonLoading(true);

    try {
        const calculation =
            await calculateShoppingList(
                shoppingState.shoppingList.userId,
                selections
            );

        writeStoredJson(
            "shoppingCalculation",
            calculation
        );

        renderCalculation(calculation);
    } catch (error) {
        console.error(error);

        setShoppingStatus(
            error.message ||
            "Could not calculate the shopping list."
        );
    } finally {
        setCalculateButtonLoading(false);
    }
}

function updateCurrentPreview() {
    const selections =
        getSelectedProducts();

    renderPreview(
        shoppingState.shoppingList,
        shoppingState.userSettings,
        selections
    );
}

function saveCurrentSelections() {
    saveShoppingSelections(
        shoppingState.shoppingList,
        shoppingState.selectedOptions
    );
}