const TOTAL_MEAL_SLOTS = 21;

document.addEventListener("DOMContentLoaded", () => {
    loadBudget();
    loadMealCount();
});

async function loadBudget() {
    const userId = Number(
        localStorage.getItem("userId") || 1
    );

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

        const weeklyBudget = Number(
            user.weeklyBudget
        );

        const shoppingCalculation =
            readStoredJson("shoppingCalculation");

        let totalPrice = 0;

        if (
            shoppingCalculation &&
            Number(shoppingCalculation.userId) === userId
        ) {
            totalPrice = Number(
                shoppingCalculation.totalPrice || 0
            );
        }

        const remainingBudget =
            weeklyBudget - totalPrice;

        updateBudgetCard(
            weeklyBudget,
            totalPrice,
            remainingBudget
        );
    } catch (error) {
        console.error(error);

        document.getElementById(
            "budgetStatus"
        ).textContent =
            "Could not load the weekly budget.";
    }
}

function updateBudgetCard(
    weeklyBudget,
    totalPrice,
    remainingBudget
) {
    const currency = new Intl.NumberFormat(
        "nb-NO",
        {
            style: "currency",
            currency: "NOK",
            maximumFractionDigits: 0
        }
    );

    document.getElementById(
        "weeklyBudget"
    ).textContent = currency.format(weeklyBudget);

    document.getElementById(
        "remainingBudget"
    ).textContent = currency.format(remainingBudget);

    const usedPercentage =
        weeklyBudget > 0
            ? (totalPrice / weeklyBudget) * 100
            : 0;

    document.getElementById(
        "budgetProgress"
    ).style.width =
        `${Math.min(100, Math.max(0, usedPercentage))}%`;

    const status = document.getElementById(
        "budgetStatus"
    );

    if (totalPrice === 0) {
        status.textContent =
            "No shopping list calculated yet.";
    } else if (remainingBudget >= 0) {
        status.textContent =
            `${currency.format(totalPrice)} planned for shopping`;
    } else {
        status.textContent =
            `${currency.format(
                Math.abs(remainingBudget)
            )} over budget`;
    }
}

function loadMealCount() {
    const weeklyMealPlan =
        readStoredJson("weeklyMealPlan");

    let plannedMeals = 0;

    if (Array.isArray(weeklyMealPlan)) {
        plannedMeals = weeklyMealPlan.filter(
            slot => slot && slot.recipe
        ).length;
    } else {
        plannedMeals = Number(
            localStorage.getItem("plannedMeals") || 0
        );
    }

    plannedMeals = Math.min(
        TOTAL_MEAL_SLOTS,
        Math.max(0, plannedMeals)
    );

    const remaining =
        TOTAL_MEAL_SLOTS - plannedMeals;

    document.getElementById(
        "plannedMeals"
    ).textContent = plannedMeals;

    document.getElementById(
        "mealsMessage"
    ).textContent =
        remaining === 1
            ? "1 meal still unplanned"
            : `${remaining} meals still unplanned`;
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