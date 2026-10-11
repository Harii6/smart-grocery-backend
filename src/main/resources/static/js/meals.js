const DAYS = [
    "Monday",
    "Tuesday",
    "Wednesday",
    "Thursday",
    "Friday",
    "Saturday",
    "Sunday"
];

const MEAL_TYPES = [
    {
        key: "breakfast",
        label: "Breakfast"
    },
    {
        key: "lunch",
        label: "Lunch"
    },
    {
        key: "dinner",
        label: "Dinner"
    }
];

let recipes = [];
let schedule = [];

document.addEventListener("DOMContentLoaded", () => {
    loadSavedData();
    renderPlanner();
    renderRecipeLibrary();
    updatePlannedCount();

    document
        .getElementById("generateMealsButton")
        .addEventListener("click", generateMeals);

    document
        .getElementById("clearPlanButton")
        .addEventListener("click", clearSchedule);
});

function loadSavedData() {
    const generatedMealPlan =
        readStoredJson("generatedMealPlan");

    recipes = extractRecipes(generatedMealPlan);

    const savedSchedule =
        readStoredJson("weeklyMealPlan");

    schedule = createEmptySchedule().map(emptySlot => {
        if (!Array.isArray(savedSchedule)) {
            return emptySlot;
        }

        const savedSlot = savedSchedule.find(slot =>
            slot.day === emptySlot.day &&
            slot.mealType === emptySlot.mealType
        );

        return savedSlot || emptySlot;
    });
}

function createEmptySchedule() {
    return DAYS.flatMap(day =>
        MEAL_TYPES.map(mealType => ({
            day: day,
            mealType: mealType.key,
            recipe: null
        }))
    );
}

function extractRecipes(mealPlanResponse) {
    if (!mealPlanResponse) {
        return [];
    }

    if (Array.isArray(mealPlanResponse.recipes)) {
        return mealPlanResponse.recipes;
    }

    // Support the older response structure if it still exists.
    if (Array.isArray(mealPlanResponse.plans)) {
        return mealPlanResponse.plans
            .map(plan => plan.recipe)
            .filter(Boolean);
    }

    return [];
}

async function generateMeals() {
    const generateButton = document.getElementById(
        "generateMealsButton"
    );

    const status = document.getElementById(
        "mealStatus"
    );

    const plannedCount = countPlannedMeals();

    if (
        plannedCount > 0 &&
        !window.confirm(
            "Generating new recipes will clear your current schedule. Continue?"
        )
    ) {
        return;
    }

    const userId = Number(
        localStorage.getItem("userId") || 1
    );

    generateButton.disabled = true;
    generateButton.textContent = "Generating...";

    status.textContent =
        "Generating recipes and finding products. This may take a little while.";

    try {
        const response = await fetch(
            `/api/meal-plans/${userId}/generate`,
            {
                method: "POST"
            }
        );

        const responseBody = await response
            .json()
            .catch(() => null);

        if (!response.ok) {
            throw new Error(
                responseBody?.message ||
                `Request failed with status ${response.status}`
            );
        }

        const generatedRecipes =
            extractRecipes(responseBody);

        if (generatedRecipes.length === 0) {
            throw new Error(
                "The backend returned no recipes."
            );
        }

        recipes = generatedRecipes;
        schedule = createEmptySchedule();

        localStorage.setItem(
            "generatedMealPlan",
            JSON.stringify(responseBody)
        );

        saveSchedule();
        renderPlanner();
        renderRecipeLibrary();
        updatePlannedCount();

        status.textContent =
            `${recipes.length} recipes generated successfully.`;
    } catch (error) {
        console.error(error);

        status.textContent =
            error.message || "Could not generate recipes.";
    } finally {
        generateButton.disabled = false;
        generateButton.textContent = "Generate recipes";
    }
}

function renderPlanner() {
    const planner = document.getElementById(
        "weekPlanner"
    );

    planner.replaceChildren();

    for (const day of DAYS) {
        const dayCard = document.createElement("article");
        dayCard.className = "day-card";

        const heading = document.createElement("h3");
        heading.textContent = day;

        dayCard.appendChild(heading);

        for (const mealType of MEAL_TYPES) {
            const slot = schedule.find(currentSlot =>
                currentSlot.day === day &&
                currentSlot.mealType === mealType.key
            );

            const slotContainer =
                document.createElement("label");

            slotContainer.className = "meal-slot";

            const slotTitle =
                document.createElement("span");

            slotTitle.textContent = mealType.label;

            const select =
                document.createElement("select");

            select.disabled = recipes.length === 0;

            const emptyOption =
                document.createElement("option");

            emptyOption.value = "";
            emptyOption.textContent = "Not planned";

            select.appendChild(emptyOption);

            for (const recipe of recipes) {
                const option =
                    document.createElement("option");

                option.value = recipe.name;
                option.textContent = recipe.name;

                select.appendChild(option);
            }

            select.value = slot?.recipe?.name || "";

            select.addEventListener("change", () => {
                const selectedRecipe = recipes.find(
                    recipe =>
                        recipe.name === select.value
                );

                if (slot) {
                    slot.recipe = selectedRecipe
                        ? {
                            name: selectedRecipe.name
                        }
                        : null;
                }

                saveSchedule();
                updatePlannedCount();
            });

            slotContainer.append(
                slotTitle,
                select
            );

            dayCard.appendChild(slotContainer);
        }

        planner.appendChild(dayCard);
    }
}

function renderRecipeLibrary() {
    const library = document.getElementById(
        "recipeLibrary"
    );

    library.replaceChildren();

    if (recipes.length === 0) {
        const emptyMessage =
            document.createElement("p");

        emptyMessage.className = "empty-message";
        emptyMessage.textContent =
            "No recipes have been generated yet.";

        library.appendChild(emptyMessage);
        return;
    }

    for (const recipe of recipes) {
        const card = document.createElement("details");
        card.className = "recipe-card";

        const summary = document.createElement("summary");

        const title = document.createElement("strong");
        title.textContent = recipe.name;

        const meta = document.createElement("span");
        meta.textContent =
            `${recipe.servings} servings · ` +
            `${recipe.ingredients?.length || 0} ingredients`;

        summary.append(title, meta);
        card.appendChild(summary);

        const content = document.createElement("div");
        content.className = "recipe-content";

        const ingredientHeading =
            document.createElement("h4");

        ingredientHeading.textContent = "Ingredients";

        const ingredientList =
            document.createElement("ul");

        for (const ingredient of recipe.ingredients || []) {
            const item = document.createElement("li");

            item.textContent =
                `${ingredient.amount} ` +
                `${ingredient.unit} ` +
                `${ingredient.searchTerm}`;

            ingredientList.appendChild(item);
        }

        const instructionHeading =
            document.createElement("h4");

        instructionHeading.textContent = "Instructions";

        const instructionList =
            document.createElement("ol");

        for (const instruction of recipe.instructions || []) {
            const item = document.createElement("li");
            item.textContent = instruction;

            instructionList.appendChild(item);
        }

        content.append(
            ingredientHeading,
            ingredientList,
            instructionHeading,
            instructionList
        );

        card.appendChild(content);
        library.appendChild(card);
    }
}

function clearSchedule() {
    if (
        countPlannedMeals() > 0 &&
        !window.confirm(
            "Remove every recipe from the weekly schedule?"
        )
    ) {
        return;
    }

    schedule = createEmptySchedule();

    saveSchedule();
    renderPlanner();
    updatePlannedCount();

    document.getElementById(
        "mealStatus"
    ).textContent =
        "The weekly schedule has been cleared.";
}

function countPlannedMeals() {
    return schedule.filter(
        slot => slot.recipe !== null
    ).length;
}

function updatePlannedCount() {
    const planned = countPlannedMeals();

    document.getElementById(
        "plannedCount"
    ).textContent = planned;

    localStorage.setItem(
        "plannedMeals",
        String(planned)
    );
}

function saveSchedule() {
    localStorage.setItem(
        "weeklyMealPlan",
        JSON.stringify(schedule)
    );
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