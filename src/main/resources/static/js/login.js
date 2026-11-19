const loginForm = document.getElementById("login-form");
const usernameInput = document.getElementById("username");
const passwordInput = document.getElementById("password");
const csrfInput = document.getElementById("csrf-token");
const signInButton = document.getElementById("sign-in-button");
const togglePassword = document.getElementById("toggle-password");
const messageElement = document.getElementById("login-message");

let submitting = false;

const parameters = new URLSearchParams(window.location.search);

if (parameters.has("error")) {
    showMessage("Incorrect username or password.");
} else if (parameters.has("logout")) {
    showMessage("You have been signed out.", "success");
} else if (parameters.has("registered")) {
    showMessage(
        "Account created. Sign in with your username and password.",
        "success"
    );
}

togglePassword.addEventListener("click", () => {
    const showPassword = passwordInput.type === "password";

    passwordInput.type = showPassword ? "text" : "password";

    const label = showPassword
        ? "Hide password"
        : "Show password";

    togglePassword.textContent = label;
    togglePassword.setAttribute("aria-label", label);
    togglePassword.setAttribute(
        "aria-pressed",
        String(showPassword)
    );
});

loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    if (submitting) {
        return;
    }

    usernameInput.value = usernameInput.value.trim();

    if (!loginForm.reportValidity()) {
        return;
    }

    submitting = true;
    signInButton.disabled = true;
    signInButton.textContent = "Signing in...";
    messageElement.hidden = true;

    try {
        const response = await fetch("/api/auth/csrf", {
            credentials: "same-origin",
            cache: "no-store",
            headers: {
                Accept: "application/json"
            }
        });

        if (!response.ok) {
            throw new Error("Could not prepare a secure sign-in.");
        }

        const csrf = await response.json();

        if (
            typeof csrf.parameterName !== "string" ||
            typeof csrf.token !== "string" ||
            !csrf.parameterName ||
            !csrf.token
        ) {
            throw new Error("The server returned an invalid CSRF token.");
        }

        csrfInput.name = csrf.parameterName;
        csrfInput.value = csrf.token;

        // Native form submission: Spring Security handles authentication.
        // Calling submit this way avoids triggering this listener again.
        HTMLFormElement.prototype.submit.call(loginForm);
    } catch (error) {
        showMessage(
            "Could not connect securely. Check that the server is running and try again."
        );

        submitting = false;
        signInButton.disabled = false;
        signInButton.textContent = "Sign in";
    }
});

function showMessage(message, type = "error") {
    messageElement.textContent = message;
    messageElement.className = `login-message ${type}`;
    messageElement.hidden = false;
}

// Restore the button if the browser restores this page from its history.
window.addEventListener("pageshow", () => {
    submitting = false;
    signInButton.disabled = false;
    signInButton.textContent = "Sign in";
    passwordInput.value = "";
});