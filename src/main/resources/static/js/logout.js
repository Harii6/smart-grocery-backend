(() => {
    const button = document.querySelector(".sidebar-logout");
    const message = document.querySelector(".sidebar-logout-message");

    if (!button) {
        return;
    }

    button.addEventListener("click", async () => {
        if (button.disabled) {
            return;
        }

        button.disabled = true;

        if (message) {
            message.hidden = true;
        }

        try {
            const csrfResponse = await fetch("/api/auth/csrf", {
                credentials: "same-origin",
                cache: "no-store",
                headers: {
                    Accept: "application/json"
                }
            });

            if (!csrfResponse.ok) {
                throw new Error("Could not prepare logout.");
            }

            const csrf = await csrfResponse.json();

            if (
                typeof csrf.headerName !== "string" ||
                typeof csrf.token !== "string" ||
                !csrf.headerName ||
                !csrf.token
            ) {
                throw new Error("Invalid security token.");
            }

            const response = await fetch("/logout", {
                method: "POST",
                credentials: "same-origin",
                headers: {
                    [csrf.headerName]: csrf.token
                }
            });

            if (response.status !== 204) {
                throw new Error("Logout was not confirmed.");
            }
        } catch (error) {
            button.disabled = false;

            if (message) {
                message.textContent =
                    "Could not log out. Please try again.";
                message.hidden = false;
            }

            return;
        }

        // Clear this app's cached account data, not unrelated storage.
        try {
            [
                "userId",
                "locationSettings",
                "generatedMealPlan",
                "weeklyMealPlan",
                "plannedMeals",
                "shoppingList",
                "shoppingSelections",
                "shoppingCalculation"
            ].forEach(key => localStorage.removeItem(key));
        } catch {
            // The server session has already ended even if storage is blocked.
        }

        window.location.replace("/welcome.html");
    });
})();