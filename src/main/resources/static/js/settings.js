const USER_ID_KEY = "userId";
const LOCATION_SETTINGS_KEY = "locationSettings";

const settingsForm = document.getElementById("settings-form");
const saveButton = document.getElementById("save-settings-button");
const messageElement = document.getElementById("settings-message");
const profileStatusElement = document.getElementById("profile-status");

const numberOfPeopleInput =
    document.getElementById("number-of-people");

const householdSizeElement =
    document.getElementById("household-size");

const householdSizeLabel =
    document.getElementById("household-size-label");

const locationAddressInput =
    document.getElementById("location-address");

const locationRadiusInput =
    document.getElementById("location-radius");

let profileLoaded = false;
let saving = false;
let currentUser = null;

saveButton.disabled = true;
saveButton.textContent = "Loading...";

settingsForm.addEventListener("submit", saveSettings);

document.getElementById("decrease-adults")
    .addEventListener("click", () => changeAdultCount(-1));

document.getElementById("increase-adults")
    .addEventListener("click", () => changeAdultCount(1));

numberOfPeopleInput.addEventListener(
    "input",
    updateHouseholdDisplay
);

updateHouseholdDisplay();

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", loadExistingUser);
} else {
    loadExistingUser();
}

function changeAdultCount(change) {
    const currentValue = Number(numberOfPeopleInput.value) || 1;

    numberOfPeopleInput.value = Math.min(
        20,
        Math.max(1, currentValue + change)
    );

    updateHouseholdDisplay();
}

function updateHouseholdDisplay() {
    const count = Number(numberOfPeopleInput.value) || 1;

    householdSizeElement.textContent = count;
    householdSizeLabel.textContent = count === 1 ? "person" : "people";
}

async function loadExistingUser() {
    try {
        const user = await requestJson("/api/users/me");

        currentUser = user;
        populateForm(user);
        cacheCurrentUser(user);

        profileLoaded = true;
        updateProfileStatus();

        if (!user.settingsCompleted) {
            showMessage(
                "Your account is ready. Complete your settings before generating meals.",
                "warning"
            );
        }
    } catch (error) {
        profileStatusElement.textContent = "Could not load your account.";

        showMessage(
            `${error.message} Reload this page to try again.`,
            "error"
        );
    } finally {
        saveButton.disabled = !profileLoaded;
        saveButton.textContent = profileLoaded
            ? "Save settings"
            : "Settings unavailable";
    }
}

async function saveSettings(event) {
    event.preventDefault();

    if (!profileLoaded || saving || !settingsForm.reportValidity()) {
        return;
    }

    const requestBody = createRequestBody();

    if (
        !Number.isFinite(requestBody.radiusKm) ||
        requestBody.radiusKm < 1 ||
        requestBody.radiusKm > 50
    ) {
        showMessage("Radius must be between 1 and 50 km.", "error");
        return;
    }

    saving = true;
    saveButton.disabled = true;
    saveButton.textContent = "Saving...";
    hideMessage();

    try {
        // Obtain a token for the current session.
        const csrf = await requestJson("/api/auth/csrf");

        if (
            typeof csrf.headerName !== "string" ||
            typeof csrf.token !== "string" ||
            !csrf.headerName ||
            !csrf.token
        ) {
            throw new Error("Could not obtain a valid security token.");
        }

        const savedUser = await requestJson("/api/users/me", {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                [csrf.headerName]: csrf.token
            },
            body: JSON.stringify(requestBody)
        });

        currentUser = savedUser;
        populateForm(savedUser);

        // Old plans may not match the new settings.
        clearGeneratedPlanData();
        cacheCurrentUser(savedUser);

        updateProfileStatus();

        showMessage(
            "Settings saved. You can now generate meals on the Meals page.",
            "success"
        );
    } catch (error) {
        showMessage(error.message, "error");
    } finally {
        saving = false;
        saveButton.disabled = !profileLoaded;
        saveButton.textContent = "Save settings";
    }
}

function createRequestBody() {
    return {
        numberOfPeople: Number(numberOfPeopleInput.value),

        weeklyBudget: Number(
            document.getElementById("weekly-budget").value
        ),

        maximumStores: Number(
            document.getElementById("maximum-stores").value
        ),

        locationText: locationAddressInput.value.trim(),
        radiusKm: Number(locationRadiusInput.value),

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
    // New accounts have null grocery settings.
    numberOfPeopleInput.value = user.numberOfPeople ?? 1;

    document.getElementById("weekly-budget").value =
        user.weeklyBudget ?? "";

    document.getElementById("maximum-stores").value =
        user.maximumStores ?? 1;

    locationAddressInput.value = user.locationText ?? "";
    locationRadiusInput.value = user.radiusKm ?? 3;

    updateHouseholdDisplay();

    const preferenceCodes = new Set(
        (user.preferences ?? []).map(preference => preference.code)
    );

    document.querySelectorAll('input[name="preferenceCodes"]')
        .forEach(input => {
            input.checked = preferenceCodes.has(input.value);
        });

    const allergenCodes = new Set(
        (user.allergens ?? []).map(allergen => allergen.code)
    );

    document.querySelectorAll('input[name="allergenCodes"]')
        .forEach(input => {
            input.checked = allergenCodes.has(input.value);
        });
}

function updateProfileStatus() {
    profileStatusElement.textContent = currentUser.settingsCompleted
        ? `Signed in as ${currentUser.username}. Your settings are saved.`
        : `Signed in as ${currentUser.username}. Complete your settings below.`;
}

async function requestJson(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        credentials: "same-origin",
        cache: "no-store",
        headers: {
            Accept: "application/json",
            ...(options.headers ?? {})
        }
    });

    // Form-login redirects can otherwise look like successful HTML responses.
    const redirectedToLogin =
        response.redirected &&
        ["/login", "/login.html"].includes(
            new URL(response.url).pathname
        );

    if (response.status === 401 || redirectedToLogin) {
        profileLoaded = false;
        clearUserData();
        window.location.replace("/login.html");

        throw new Error("Your session has ended. Please sign in again.");
    }

    if (!response.ok) {
        if (response.status === 403) {
            throw new Error(
                "The request was denied. Reload the page and sign in again if needed."
            );
        }

        throw new Error(await readError(response));
    }

    const contentType = response.headers.get("content-type") ?? "";

    if (!contentType.includes("application/json")) {
        throw new Error("The server returned an unexpected response.");
    }

    return response.json();
}

function cacheCurrentUser(user) {
    try {
        const previousUserId = localStorage.getItem(USER_ID_KEY);

        if (previousUserId !== String(user.id)) {
            clearGeneratedPlanData();
            localStorage.removeItem(LOCATION_SETTINGS_KEY);
        }

        // Compatibility only: the server remains responsible for authorization.
        localStorage.setItem(USER_ID_KEY, String(user.id));

        localStorage.setItem(
            LOCATION_SETTINGS_KEY,
            JSON.stringify({
                address: user.locationText ?? "",
                radius: user.radiusKm ?? 3
            })
        );
    } catch {
        // Browser storage failure must not turn a successful save into an error.
        console.warn("Could not update the local settings cache.");
    }
}

function clearGeneratedPlanData() {
    try {
        [
            "generatedMealPlan",
            "weeklyMealPlan",
            "plannedMeals",
            "shoppingList",
            "shoppingSelections",
            "shoppingCalculation"
        ].forEach(key => localStorage.removeItem(key));
    } catch {
        console.warn("Could not clear the local meal-plan cache.");
    }
}

function clearUserData() {
    clearGeneratedPlanData();

    try {
        localStorage.removeItem(USER_ID_KEY);
        localStorage.removeItem(LOCATION_SETTINGS_KEY);
    } catch {
        console.warn("Could not clear the local account cache.");
    }
}

async function readError(response) {
    try {
        const body = await response.json();

        if (body.errors) {
            return Object.values(body.errors).flat().join(", ");
        }

        if (body.message) {
            return body.message;
        }

        if (body.detail) {
            return body.detail;
        }
    } catch {
        // Response did not contain JSON.
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