const sidebar = document.querySelector(".app-sidebar");
const toggle = document.querySelector(".sidebar-toggle");

if (sidebar && toggle) {
    // Keep the icon navigation clickable.
    sidebar.inert = false;

    let collapsed = false;

    try {
        collapsed =
            localStorage.getItem("sidebarCollapsed") === "true";
    } catch {
        // Storage is optional.
    }

    function updateSidebar() {
        document.body.classList.toggle(
            "sidebar-collapsed",
            collapsed
        );

        const label = collapsed
            ? "Expand sidebar"
            : "Minimize sidebar";

        toggle.setAttribute("aria-expanded", String(!collapsed));
        toggle.setAttribute("aria-label", label);
        toggle.title = label;
    }

    toggle.addEventListener("click", () => {
        collapsed = !collapsed;
        updateSidebar();

        try {
            localStorage.setItem(
                "sidebarCollapsed",
                String(collapsed)
            );
        } catch {
            // Storage is optional.
        }
    });

    updateSidebar();
}