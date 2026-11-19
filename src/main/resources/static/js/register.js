const registerForm = document.getElementById("register-form");
const usernameInput = document.getElementById("username");
const passwordInput = document.getElementById("password");
const confirmPasswordInput =
    document.getElementById("confirm-password");

const registerButton = document.getElementById("register-button");
const togglePassword = document.getElementById("toggle-password");
const messageElement = document.getElementById("register-message");

let submitting = false;

registerButton.disabled = false;

togglePassword.addEventListener("click", () => {
    const showPassword = passwordInput.type === "password";
    const inputType = showPassword ? "text" : "password";

    passwordInput.type = inputType;
    confirmPasswordInput.type = inputType;

    togglePassword.textContent = showPassword
        ? "Hide password"
        : "Show password";

    togglePassword.setAttribute(
        "aria-label",
        showPassword ? "Hide passwords" : "Show passwords"
    );

    togglePassword.setAttribute(
        "aria-pressed",
        String(showPassword)
    );
});

function validateConfirmation() {
    confirmPasswordInput.setCustomValidity(
        confirmPasswordInput.value === passwordInput.value
            ? ""
            : "The passwords do not match."
    );
}

passwordInput.addEventListener("input", validateConfirmation);
confirmPasswordInput.addEventListener("input", validateConfirmation);

registerForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    if (submitting) {
        return;
    }

    usernameInput.value = usernameInput.value.trim();
    validateConfirmation();

    if (!registerForm.reportValidity()) {
        return;
    }

    // Matches the BCrypt byte limit enforced by your backend.
    if (new TextEncoder().encode(passwordInput.value).length > 72) {
        showMessage(
            "This password exceeds the supported byte length. " +
            "Use fewer multi-byte characters, such as emoji."
        );
        return;
    }

    submitting = true;
    registerButton.disabled = true;
    registerButton.textContent = "Creating account...";
    messageElement.hidden = true;

    let accountCreated = false;

    try {
        const csrfResponse = await fetch("/api/auth/csrf", {
            credentials: "same-origin",
            cache: "no-store",
            headers: {
                Accept: "application/json"
            }
        });

        if (!csrfResponse.ok) {
            throw new Error(
                "Could not prepare secure registration. Please try again."
            );
        }

        const csrf = await csrfResponse.json();

        if (
            typeof csrf.headerName !== "string" ||
            typeof csrf.token !== "string" ||
            !csrf.headerName ||
            !csrf.token
        ) {
            throw new Error("The server returned an invalid CSRF token.");
        }

        const response = await fetch("/api/auth/register", {
            method: "POST",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json",
                Accept: "application/json",
                [csrf.headerName]: csrf.token
            },
            body: JSON.stringify({
                username: usernameInput.value,
                password: passwordInput.value
            })
        });

        // Do not mistake a redirected login page for registration success.
        if (response.redirected) {
            throw new Error(
                "Registration was redirected. Check that " +
                "/api/auth/register is permitted in SecurityConfig."
            );
        }

        if (!response.ok) {
            if (response.status === 409) {
                throw new Error(
                    "That username is unavailable. Choose another."
                );
            }

            if (response.status === 403) {
                throw new Error(
                    "The security check failed. Please try submitting again."
                );
            }

            if (response.status === 400) {
                throw new Error(
                    "Check your username and password against the requirements."
                );
            }

            throw new Error(
                "The server could not create your account. Please try again."
            );
        }

        accountCreated = true;

        passwordInput.value = "";
        confirmPasswordInput.value = "";

        // Registration does not automatically sign you in.
        window.location.replace("/login.html?registered");
    } catch (error) {
        showMessage(
            error instanceof TypeError
                ? "Could not reach the server. Check your connection."
                : error.message
        );
    } finally {
        if (!accountCreated) {
            submitting = false;
            registerButton.disabled = false;
            registerButton.textContent = "Create account";
        }
    }
});

function showMessage(message) {
    messageElement.textContent = message;
    messageElement.hidden = false;
}