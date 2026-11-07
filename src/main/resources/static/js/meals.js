const DAYS = [
    { key: "monday", label: "Monday" },
    { key: "tuesday", label: "Tuesday" },
    { key: "wednesday", label: "Wednesday" },
    { key: "thursday", label: "Thursday" },
    { key: "friday", label: "Friday" },
    { key: "saturday", label: "Saturday" },
    { key: "sunday", label: "Sunday" }
];

const MAX_MEALS_PER_DAY = 3;
const MAX_RECIPE_CHOICES = 7;

let activeDay = "monday";
let mealPlan = null;
let recipes = [];

let selections = createEmptySelections();

const recipeDialog = document.getElementById("recipeDialog");
const recipeDialogClose = document.getElementById("recipeDialogClose");

const recipeDialogTitle =
    document.getElementById("recipeDialogTitle");

const recipeTime =
    document.getElementById("recipeTime");

const recipeServings =
    document.getElementById("recipeServings");

const recipeCalories =
    document.getElementById("recipeCalories");

const recipeProtein =
    document.getElementById("recipeProtein");

const recipeIngredients =
    document.getElementById("recipeIngredients");

const recipeInstructions =
    document.getElementById("recipeInstructions");

const generateButton =
    document.getElementById("generateMealPlanButton");

const mealPlanStatus =
    document.getElementById("mealPlanStatus");

const mealPlanner =
    document.getElementById("mealPlanner");

const activeDayTitle =
    document.getElementById("activeDayTitle");

const selectedDayTitle =
    document.getElementById("selectedDayTitle");

const selectedMealCount =
    document.getElementById("selectedMealCount");

const mealChoices =
    document.getElementById("mealChoices");

const selectedMeals =
    document.getElementById("selectedMeals");

const dayButtons =
    document.querySelectorAll(".day-button");


document.addEventListener("DOMContentLoaded", initialisePage);


function initialisePage() {
    generateButton.addEventListener(
        "click",
        generateMealPlan
    );

    dayButtons.forEach(button => {
        button.addEventListener("click", () => {
            setActiveDay(button.dataset.day);
        });
    });

    const storedMealPlan =
        readStoredJson("generatedMealPlan");

    if (storedMealPlan) {
        useMealPlan(storedMealPlan, false);

        setStatus(
            "Your existing generated meal choices were loaded.",
            "success"
        );
    }
}


async function generateMealPlan() {
    const userId = findUserId();

    if (!userId) {
        setStatus(
            "Create your profile in Settings before generating meals.",
            "error"
        );

        return;
    }

    setGeneratingState(true);

    setStatus(
        "Generating meal choices. This may take several seconds."
    );

    try {
        const response = await fetch(
            `/api/meal-plans/${userId}/generate`,
            {
                method: "POST",
                headers: {
                    "Accept": "application/json"
                }
            }
        );

        const responseBody = await readResponseBody(response);

        if (!response.ok) {
            throw new Error(
                findErrorMessage(
                    responseBody,
                    "The backend could not generate a meal plan."
                )
            );
        }

        useMealPlan(responseBody, true);

        localStorage.setItem(
            "generatedMealPlan",
            JSON.stringify(responseBody)
        );

        if (responseBody.shoppingList) {
            localStorage.setItem(
                "shoppingList",
                JSON.stringify(responseBody.shoppingList)
            );
        }

        setStatus(
            "Meal choices generated. Select up to three meals for each day.",
            "success"
        );
    } catch (error) {
        console.error(error);

        setStatus(
            error.message || "Could not generate meal choices.",
            "error"
        );
    } finally {
        setGeneratingState(false);
    }
}


function useMealPlan(newMealPlan, resetSelections) {
    mealPlan = newMealPlan;

    recipes = extractRecipes(newMealPlan)
        .slice(0, MAX_RECIPE_CHOICES);

    if (recipes.length === 0) {
        mealPlanner.hidden = true;

        setStatus(
            "The backend returned no recipes.",
            "error"
        );

        return;
    }

    if (resetSelections) {
        selections = createEmptySelections();
        saveSelections();
    } else {
        loadSelections();
    }

    mealPlanner.hidden = false;

    renderPlanner();
}


function extractRecipes(data) {
    if (!data) {
        return [];
    }

    if (Array.isArray(data.recipes)) {
        return data.recipes;
    }

    /*
    This fallback supports the older backend response where
    each plan contained its own recipe.
    */
    if (Array.isArray(data.plans)) {
        return data.plans
            .map(plan => plan.recipe)
            .filter(recipe => recipe);
    }

    if (
        data.mealPlan &&
        Array.isArray(data.mealPlan.recipes)
    ) {
        return data.mealPlan.recipes;
    }

    return [];
}


function setActiveDay(day) {
    const validDay = DAYS.some(item => item.key === day);

    if (!validDay) {
        return;
    }

    activeDay = day;

    dayButtons.forEach(button => {
        button.classList.toggle(
            "active",
            button.dataset.day === activeDay
        );
    });

    renderPlanner();
}


function renderPlanner() {
    const day = getActiveDay();
    const selectedKeys = selections[activeDay] || [];

    activeDayTitle.textContent =
        `Choose 3 meals for ${day.label}`;

    selectedDayTitle.textContent =
        `${day.label}'s meals`;

    selectedMealCount.textContent =
        `${selectedKeys.length} of ${MAX_MEALS_PER_DAY} selected`;

    renderMealChoices();
    renderSelectedMeals();
}


function renderMealChoices() {
    mealChoices.innerHTML = "";

    const selectedKeys = selections[activeDay] || [];

    recipes.forEach((recipe, index) => {
        const key = createRecipeKey(recipe, index);
        const selected = selectedKeys.includes(key);

        const label = document.createElement("label");
        label.className = "meal-choice";

        const checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.checked = selected;
        checkbox.value = key;

        checkbox.addEventListener("change", event => {
            updateMealSelection(
                key,
                event.target.checked,
                event.target
            );
        });

        const name = document.createElement("span");
        name.className = "meal-choice-name";
        name.textContent =
            recipe.name || `Recipe ${index + 1}`;

        label.append(checkbox, name);
        mealChoices.append(label);
    });
}


function updateMealSelection(key, shouldSelect, checkbox) {
    const currentSelections =
        selections[activeDay] || [];

    if (shouldSelect) {
        if (
            currentSelections.length >=
            MAX_MEALS_PER_DAY
        ) {
            checkbox.checked = false;

            setStatus(
                "You can select a maximum of three meals for one day.",
                "error"
            );

            return;
        }

        if (!currentSelections.includes(key)) {
            currentSelections.push(key);
        }
    } else {
        const selectionIndex =
            currentSelections.indexOf(key);

        if (selectionIndex >= 0) {
            currentSelections.splice(selectionIndex, 1);
        }
    }

    selections[activeDay] = currentSelections;

    saveSelections();
    renderPlanner();

    const day = getActiveDay();

    setStatus(
        `${currentSelections.length} meal(s) selected for ${day.label}.`,
        "success"
    );
}


function renderSelectedMeals() {
    selectedMeals.innerHTML = "";

    const selectedKeys =
        selections[activeDay] || [];

    const mealTypes = [
        "Breakfast",
        "Lunch",
        "Dinner"
    ];

    for (
        let position = 0;
        position < MAX_MEALS_PER_DAY;
        position++
    ) {
        const selectedKey = selectedKeys[position];

        if (!selectedKey) {
            selectedMeals.append(
                createEmptyMealSlot(position)
            );

            continue;
        }

        const recipeEntry =
            findRecipeByKey(selectedKey);

        if (!recipeEntry) {
            selectedMeals.append(
                createEmptyMealSlot(position)
            );

            continue;
        }

        const recipe = recipeEntry.recipe;

        const card =
            document.createElement("button");

        card.type = "button";
        card.className = "selected-meal-card";

        const mealType =
            document.createElement("span");

        mealType.className = "selected-meal-type";
        mealType.textContent = mealTypes[position];

        const information =
            document.createElement("div");

        information.className =
            "selected-meal-information";

        const title =
            document.createElement("h3");

        title.textContent =
            recipe.name ||
            `Recipe ${recipeEntry.index + 1}`;

        const metadata =
            document.createElement("p");

        metadata.className =
            "selected-meal-metadata";

        const time =
            recipe.preparationTimeMinutes ?? "—";

        const calories =
            recipe.caloriesPerServing ?? "—";

        metadata.textContent =
            `${time} min · ${calories} kcal`;

        information.append(title, metadata);

        const chefIcon = createChefIcon();

        card.append(
            mealType,
            information,
            chefIcon
        );

        card.addEventListener("click", () => {
            openRecipeDialog(recipe);
        });

        selectedMeals.append(card);
    }
}

function createChefIcon() {
    const icon = document.createElement("span");

    icon.className = "selected-meal-chef";
    icon.setAttribute("aria-hidden", "true");

    icon.innerHTML = `
        <svg
            viewBox="0 0 24 24"
            width="28"
            height="28"
            fill="none"
            stroke="currentColor"
            stroke-width="1.7"
            stroke-linecap="round"
            stroke-linejoin="round"
        >
            <path d="M6 13.5V20h12v-6.5"></path>
            <path d="M6 16h12"></path>
            <path d="M6.2 13.5C3.7 13.1 3 9.8 4.8 8.2
                     C5.6 7.5 6.6 7.2 7.5 7.4
                     C8.1 5.3 9.8 4 12 4
                     C14.2 4 15.9 5.3 16.5 7.4
                     C17.4 7.2 18.4 7.5 19.2 8.2
                     C21 9.8 20.3 13.1 17.8 13.5">
            </path>
        </svg>
    `;

    return icon;
}


function createEmptyMealSlot(position) {
    const slot = document.createElement("article");
    slot.className = "empty-meal-slot";

    const text = document.createElement("span");
    text.textContent =
        `Choose meal ${position + 1} above`;

    slot.append(text);

    return slot;
}


function findRecipeByKey(key) {
    for (let index = 0; index < recipes.length; index++) {
        const recipe = recipes[index];

        if (createRecipeKey(recipe, index) === key) {
            return {
                recipe,
                index
            };
        }
    }

    return null;
}


function createRecipeKey(recipe, index) {
    const name = recipe.name || "recipe";

    return `${index}:${name}`;
}


function createEmptySelections() {
    return DAYS.reduce((result, day) => {
        result[day.key] = [];
        return result;
    }, {});
}


function saveSelections() {
    const storedValue = {
        userId: findUserId(),
        recipeSignature: createRecipeSignature(),
        days: selections
    };

    localStorage.setItem(
        "weeklyMealSelections",
        JSON.stringify(storedValue)
    );
}


function loadSelections() {
    const stored =
        readStoredJson("weeklyMealSelections");

    if (
        !stored ||
        stored.recipeSignature !== createRecipeSignature() ||
        !stored.days
    ) {
        selections = createEmptySelections();
        return;
    }

    const restoredSelections =
        createEmptySelections();

    DAYS.forEach(day => {
        const storedDay =
            stored.days[day.key];

        if (!Array.isArray(storedDay)) {
            return;
        }

        restoredSelections[day.key] =
            storedDay
                .filter(key => findRecipeByKey(key))
                .slice(0, MAX_MEALS_PER_DAY);
    });

    selections = restoredSelections;
}


function createRecipeSignature() {
    return recipes
        .map((recipe, index) =>
            createRecipeKey(recipe, index)
        )
        .join("|");
}


function getActiveDay() {
    return DAYS.find(day => day.key === activeDay);
}


function findUserId() {
    const possibleKeys = [
        "userId",
        "activeUserId",
        "currentUserId"
    ];

    for (const key of possibleKeys) {
        const value = Number(localStorage.getItem(key));

        if (Number.isInteger(value) && value > 0) {
            return value;
        }
    }

    const possibleObjects = [
        readStoredJson("user"),
        readStoredJson("currentUser"),
        readStoredJson("userSettings")
    ];

    for (const value of possibleObjects) {
        const id = Number(value?.id ?? value?.userId);

        if (Number.isInteger(id) && id > 0) {
            return id;
        }
    }

    return null;
}


function readStoredJson(key) {
    const storedValue = localStorage.getItem(key);

    if (!storedValue) {
        return null;
    }

    try {
        return JSON.parse(storedValue);
    } catch (error) {
        console.warn(
            `Could not read ${key} from localStorage.`,
            error
        );

        return null;
    }
}


async function readResponseBody(response) {
    const text = await response.text();

    if (!text) {
        return {};
    }

    try {
        return JSON.parse(text);
    } catch {
        return {
            message: text
        };
    }
}


function findErrorMessage(responseBody, fallback) {
    if (typeof responseBody === "string") {
        return responseBody;
    }

    return responseBody?.message || fallback;
}


function setGeneratingState(generating) {
    generateButton.disabled = generating;

    generateButton.textContent = generating
        ? "Generating..."
        : "Generate meal choices";
}


function setStatus(message, type = "") {
    mealPlanStatus.textContent = message;

    mealPlanStatus.classList.remove(
        "success",
        "error"
    );

    if (type) {
        mealPlanStatus.classList.add(type);
    }
}

function openRecipeDialog(recipe) {
    if (!recipe) {
        return;
    }

    recipeDialogTitle.textContent =
        recipe.name || "Recipe";

    recipeTime.textContent =
        `${recipe.preparationTimeMinutes ?? "—"} min`;

    recipeServings.textContent =
        `${recipe.servings ?? "—"} ${
            recipe.servings === 1 ? "serving" : "servings"
        }`;

    recipeCalories.textContent =
        `${recipe.caloriesPerServing ?? "—"} kcal`;

    recipeProtein.textContent =
        `${formatRecipeNumber(
            recipe.proteinGramsPerServing
        )} g`;

    renderRecipeIngredients(recipe.ingredients);
    renderRecipeInstructions(recipe.instructions);

    recipeDialog.showModal();
}

function renderRecipeIngredients(ingredients) {
    recipeIngredients.replaceChildren();

    if (!Array.isArray(ingredients) || ingredients.length === 0) {
        const item = document.createElement("li");
        item.textContent = "No ingredients available.";
        recipeIngredients.append(item);
        return;
    }

    ingredients.forEach(ingredient => {
        const item = document.createElement("li");

        const name = document.createElement("span");
        name.className = "recipe-ingredient-name";
        name.textContent =
            ingredient.searchTerm || "Ingredient";

        const amount = document.createElement("span");
        amount.className = "recipe-ingredient-amount";

        const formattedAmount =
            formatRecipeNumber(ingredient.amount);

        amount.textContent =
            `${formattedAmount} ${ingredient.unit || ""}`.trim();

        item.append(name, amount);
        recipeIngredients.append(item);
    });
}

function renderRecipeInstructions(instructions) {
    recipeInstructions.replaceChildren();

    if (!Array.isArray(instructions) || instructions.length === 0) {
        const item = document.createElement("li");
        item.textContent = "No instructions available.";
        recipeInstructions.append(item);
        return;
    }

    instructions.forEach(instruction => {
        const item = document.createElement("li");
        item.textContent = instruction;
        recipeInstructions.append(item);
    });
}

function formatRecipeNumber(value) {
    const number = Number(value);

    if (!Number.isFinite(number)) {
        return "—";
    }

    return new Intl.NumberFormat("nb-NO", {
        maximumFractionDigits: 2
    }).format(number);
}

recipeDialogClose.addEventListener("click", () => {
    recipeDialog.close();
});

recipeDialog.addEventListener("click", event => {
    const dialogBox =
        recipeDialog.getBoundingClientRect();

    const clickedOutside =
        event.clientX < dialogBox.left ||
        event.clientX > dialogBox.right ||
        event.clientY < dialogBox.top ||
        event.clientY > dialogBox.bottom;

    if (clickedOutside) {
        recipeDialog.close();
    }
});