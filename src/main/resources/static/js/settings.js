const USER_ID_KEY = "userId";

const settingsForm = document.getElementById("settings-form");
const saveButton = document.getElementById("save-settings-button");
const messageElement = document.getElementById("settings-message");
const profileStatusElement = document.getElementById("profile-status");

document.addEventListener("DOMContentLoaded", loadExistingUser);
settingsForm.addEventListener("submit", saveSettings);

async function loadExistingUser() {
    const userId = localStorage.getItem(USER_ID_KEY);

    if (!userId) {
        showCreateMode();
        return;
    }

    try {
        const response = await fetch(`/api/users/${userId}`);

        if (response.status === 404) {
            clearUserData();
            showCreateMode();

            showMessage(
                "The previous profile no longer exists. Create a new profile.",
                "warning"
            );

            return;
        }

        if (!response.ok) {
            throw new Error(await readError(response));
        }

        const user = await response.json();

        populateForm(user);
        showUpdateMode(user.id);
    } catch (error) {
        showMessage(
            `Could not load your profile: ${error.message}`,
            "error"
        );
    }
}

async function saveSettings(event) {
    event.preventDefault();

    if (!settingsForm.reportValidity()) {
        return;
    }

    const existingUserId = localStorage.getItem(USER_ID_KEY);
    const requestBody = createRequestBody();

    const url = existingUserId
        ? `/api/users/${existingUserId}`
        : "/api/users";

    const method = existingUserId ? "PUT" : "POST";

    saveButton.disabled = true;
    saveButton.textContent = existingUserId
        ? "Saving..."
        : "Creating...";

    hideMessage();

    try {
        const response = await fetch(url, {
            method: method,
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(requestBody)
        });

        if (!response.ok) {
            throw new Error(await readError(response));
        }

        const savedUser = await response.json();

        if (!savedUser.id) {
            throw new Error("The backend did not return a user ID.");
        }

        localStorage.setItem(USER_ID_KEY, String(savedUser.id));

        /*
         * Existing recipes and shopping lists may no longer match
         * the new budget, preferences or allergies.
         */
        clearGeneratedPlanData();

        populateForm(savedUser);
        showUpdateMode(savedUser.id);

        showMessage(
            "Settings saved. You can now generate recipes on the Meals page.",
            "success"
        );
    } catch (error) {
        showMessage(error.message, "error");
    } finally {
        saveButton.disabled = false;

        saveButton.textContent =
            localStorage.getItem(USER_ID_KEY)
                ? "Save changes"
                : "Create profile";
    }
}

function createRequestBody() {
    return {
        numberOfPeople: Number(
            document.getElementById("number-of-people").value
        ),

        weeklyBudget: Number(
            document.getElementById("weekly-budget").value
        ),

        maximumStores: Number(
            document.getElementById("maximum-stores").value
        ),

        preferenceCodes: getCheckedValues("preferenceCodes"),
        allergenCodes: getCheckedValues("allergenCodes")
    };
}

function getCheckedValues(name) {
    return Array.from(
        document.querySelectorAll(`input[name="${name}"]:checked`)
    ).map(input => input.value);
}

function populateForm(user) {
    document.getElementById("number-of-people").value =
        user.numberOfPeople;

    document.getElementById("weekly-budget").value =
        user.weeklyBudget;

    document.getElementById("maximum-stores").value =
        user.maximumStores;

    const preferenceCodes = new Set(
        (user.preferences || []).map(preference => preference.code)
    );

    document
        .querySelectorAll('input[name="preferenceCodes"]')
        .forEach(input => {
            input.checked = preferenceCodes.has(input.value);
        });

    const allergenCodes = new Set(
        (user.allergens || []).map(allergen => allergen.code)
    );

    document
        .querySelectorAll('input[name="allergenCodes"]')
        .forEach(input => {
            input.checked = allergenCodes.has(input.value);
        });
}

function showCreateMode() {
    profileStatusElement.textContent =
        "No profile has been created yet.";

    saveButton.textContent = "Create profile";
}

function showUpdateMode(userId) {
    profileStatusElement.textContent =
        `Profile ${userId} is active.`;

    saveButton.textContent = "Save changes";
}

function clearGeneratedPlanData() {
    localStorage.removeItem("generatedMealPlan");
    localStorage.removeItem("weeklyMealPlan");
    localStorage.removeItem("plannedMeals");
    localStorage.removeItem("shoppingList");
    localStorage.removeItem("shoppingSelections");
    localStorage.removeItem("shoppingCalculation");
}

function clearUserData() {
    localStorage.removeItem(USER_ID_KEY);
    clearGeneratedPlanData();
}

async function readError(response) {
    try {
        const errorBody = await response.json();

        if (errorBody.message) {
            return errorBody.message;
        }

        if (errorBody.errors) {
            return Object.values(errorBody.errors).join(", ");
        }

        if (errorBody.error) {
            return errorBody.error;
        }
    } catch {
        // The response did not contain JSON.
    }

    return `Request failed with status ${response.status}.`;
}

function showMessage(message, type) {
    messageElement.textContent = message;
    messageElement.className = `settings-message ${type}`;
    messageElement.hidden = false;
}

function hideMessage() {
    messageElement.hidden = true;
}