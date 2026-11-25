(() => {
  "use strict";

  const STORAGE_KEYS = {
    sidebarCollapsed: "abz-admin-sidebar-collapsed",
    colorScheme: "abz-admin-color-scheme",
  };

  const shell = document.getElementById("admin-shell");
  const sidebar = document.getElementById("admin-sidebar");
  const backdrop = document.getElementById("sidebar-backdrop");
  const mobileMenuBtn = document.getElementById("mobile-menu-btn");
  const collapseBtn = document.getElementById("sidebar-collapse-btn");
  const themeToggleBtn = document.getElementById("theme-toggle-btn");
  const themeToggleLabel = document.getElementById("theme-toggle-label");
  const toastRegion = document.getElementById("admin-toast-region");
  const confirmDialog = document.getElementById("admin-confirm-dialog");
  const confirmMessageEl = document.getElementById("admin-confirm-message");
  const confirmAcceptBtn = document.getElementById("admin-confirm-accept");
  const confirmCancelBtn = document.getElementById("admin-confirm-cancel");
  const moduleFilterInput = document.getElementById("module-filter-input");
  const modulesTableBody = document.getElementById("modules-table-body");
  const modulesEmptyRow = document.getElementById("modules-empty-row");
  const modulesCountEl = document.getElementById("modules-visible-count");

  const mobileMediaQuery = window.matchMedia("(max-width: 959px)");
  const darkMediaQuery = window.matchMedia("(prefers-color-scheme: dark)");

  /* ------------------------------------------------------------------------
     Storage seguro
     ------------------------------------------------------------------------ */
  function readStorage(key) {
    try {
      return window.localStorage.getItem(key);
    } catch {
      return null;
    }
  }

  function writeStorage(key, value) {
    try {
      if (value === null) {
        window.localStorage.removeItem(key);
      } else {
        window.localStorage.setItem(key, value);
      }
    } catch {
      // Ignora erro quando armazenamento local está bloqueado
    }
  }

  /* ------------------------------------------------------------------------
     Tema (light / dark seguindo <meta name="color-scheme">)
     ------------------------------------------------------------------------ */
  const colorSchemeMeta = document.querySelector('meta[name="color-scheme"]');

  function getEffectiveScheme() {
    const stored = readStorage(STORAGE_KEYS.colorScheme);
    if (stored === "light" || stored === "dark") {
      return stored;
    }
    return darkMediaQuery.matches ? "dark" : "light";
  }

  function syncThemeUI() {
    if (!themeToggleBtn || !themeToggleLabel) return;
    const effective = getEffectiveScheme();
    const nextLabel = effective === "dark" ? "Tema claro" : "Tema escuro";
    themeToggleLabel.textContent = nextLabel;
    themeToggleBtn.setAttribute("aria-label", `Alternar para ${nextLabel.toLowerCase()}`);
  }

  function applyColorScheme(value) {
    if (colorSchemeMeta) {
      colorSchemeMeta.content = value || "light dark";
    }
    syncThemeUI();
  }

  if (themeToggleBtn) {
    syncThemeUI();
    themeToggleBtn.addEventListener("click", () => {
      const systemScheme = darkMediaQuery.matches ? "dark" : "light";
      const targetScheme = getEffectiveScheme() === "dark" ? "light" : "dark";

      if (targetScheme === systemScheme) {
        writeStorage(STORAGE_KEYS.colorScheme, null);
        applyColorScheme("light dark");
      } else {
        writeStorage(STORAGE_KEYS.colorScheme, targetScheme);
        applyColorScheme(targetScheme);
      }
    });

    darkMediaQuery.addEventListener("change", syncThemeUI);
    window.addEventListener("storage", (event) => {
      if (event.key === STORAGE_KEYS.colorScheme) {
        applyColorScheme(event.newValue || "light dark");
      }
    });
  }

  /* ------------------------------------------------------------------------
     Sidebar: colapso no desktop e gaveta acessível no mobile
     ------------------------------------------------------------------------ */
  function setDesktopCollapsed(collapsed) {
    if (!shell || !collapseBtn) return;
    shell.classList.toggle("is-sidebar-collapsed", collapsed);
    collapseBtn.setAttribute("aria-expanded", String(!collapsed));
    collapseBtn.setAttribute(
      "aria-label",
      collapsed ? "Expandir menu lateral" : "Recolher menu lateral"
    );
    writeStorage(STORAGE_KEYS.sidebarCollapsed, collapsed ? "1" : "0");
  }

  function setMobileMenuOpen(open) {
    if (!shell || !mobileMenuBtn) return;
    shell.classList.toggle("is-mobile-menu-open", open);
    mobileMenuBtn.setAttribute("aria-expanded", String(open));

    const mainEl = shell.querySelector("main");
    const topbarEl = shell.querySelector("header");
    if (mainEl) mainEl.inert = open;
    if (topbarEl) topbarEl.inert = open;

    if (open && sidebar) {
      const firstFocusable = sidebar.querySelector("a, button");
      if (firstFocusable) firstFocusable.focus();
    } else if (!open && mobileMediaQuery.matches) {
      mobileMenuBtn.focus();
    }
  }

  if (shell && collapseBtn) {
    const savedCollapsed = readStorage(STORAGE_KEYS.sidebarCollapsed) === "1";
    if (savedCollapsed && !mobileMediaQuery.matches) {
      setDesktopCollapsed(true);
    }

    collapseBtn.addEventListener("click", () => {
      const isCollapsed = shell.classList.contains("is-sidebar-collapsed");
      setDesktopCollapsed(!isCollapsed);
    });
  }

  if (mobileMenuBtn && backdrop) {
    mobileMenuBtn.addEventListener("click", () => {
      const isOpen = shell.classList.contains("is-mobile-menu-open");
      setMobileMenuOpen(!isOpen);
    });

    backdrop.addEventListener("click", () => setMobileMenuOpen(false));

    mobileMediaQuery.addEventListener("change", (event) => {
      if (!event.matches && shell.classList.contains("is-mobile-menu-open")) {
        setMobileMenuOpen(false);
      }
    });

    document.addEventListener("keydown", (event) => {
      if (event.key === "Escape" && shell.classList.contains("is-mobile-menu-open")) {
        setMobileMenuOpen(false);
      }
    });
  }

  /* ------------------------------------------------------------------------
     Toast acessível (aria-live="polite")
     ------------------------------------------------------------------------ */
  let toastTimeoutId = null;

  function showToast(message) {
    if (!toastRegion) return;
    toastRegion.innerHTML = "";

    const toast = document.createElement("div");
    toast.className = "admin-toast";

    const text = document.createElement("span");
    text.textContent = message;

    const closeBtn = document.createElement("button");
    closeBtn.type = "button";
    closeBtn.className = "btn btn-ghost btn-sm";
    closeBtn.textContent = "Fechar";
    closeBtn.addEventListener("click", () => toast.remove());

    toast.append(text, closeBtn);
    toastRegion.appendChild(toast);

    window.clearTimeout(toastTimeoutId);
    toastTimeoutId = window.setTimeout(() => {
      toast.remove();
    }, 4500);
  }

  /* ------------------------------------------------------------------------
     Interceptação amigável para módulos ainda não conectados ao backend
     ------------------------------------------------------------------------ */
  document.addEventListener("click", (event) => {
    const link = event.target.closest('a[data-module-pending="true"]');
    if (!link) return;

    event.preventDefault();
    const moduleName = link.dataset.moduleName || link.textContent.trim();
    if (shell && shell.classList.contains("is-mobile-menu-open")) {
      setMobileMenuOpen(false);
    }
    showToast(`O módulo "${moduleName}" ainda será conectado ao banco de dados.`);
  });

  /* ------------------------------------------------------------------------
     Filtro rápido da tabela de módulos + atalho "/"
     ------------------------------------------------------------------------ */
  if (moduleFilterInput && modulesTableBody) {
    const rows = Array.from(modulesTableBody.querySelectorAll("tr[data-module-row]"));

    function applyFilter() {
      const query = moduleFilterInput.value.trim().toLowerCase();
      let visibleCount = 0;

      rows.forEach((row) => {
        const text = row.textContent.toLowerCase();
        const matches = !query || text.includes(query);
        row.hidden = !matches;
        if (matches) visibleCount += 1;
      });

      if (modulesEmptyRow) {
        modulesEmptyRow.hidden = visibleCount > 0;
      }
      if (modulesCountEl) {
        modulesCountEl.textContent = `${visibleCount} de ${rows.length} módulos`;
      }
    }

    moduleFilterInput.addEventListener("input", applyFilter);

    document.addEventListener("keydown", (event) => {
      if (event.key !== "/" || event.ctrlKey || event.metaKey || event.altKey) return;
      const tag = document.activeElement ? document.activeElement.tagName : "";
      if (tag === "INPUT" || tag === "TEXTAREA" || tag === "SELECT") return;

      event.preventDefault();
      moduleFilterInput.focus();
    });
  }

  /* ------------------------------------------------------------------------
     Dialog nativo de confirmação reutilizável (data-confirm)
     ------------------------------------------------------------------------ */
  let pendingConfirmForm = null;

  if (confirmDialog && confirmMessageEl && confirmAcceptBtn && confirmCancelBtn) {
    document.addEventListener("submit", (event) => {
      const form = event.target.closest("form[data-confirm]");
      if (!form || form.dataset.confirmed === "true") return;

      event.preventDefault();
      pendingConfirmForm = form;
      confirmMessageEl.textContent = form.dataset.confirm || "Confirma esta ação?";
      confirmDialog.showModal();
    });

    confirmCancelBtn.addEventListener("click", () => {
      pendingConfirmForm = null;
      confirmDialog.close();
    });

    confirmAcceptBtn.addEventListener("click", () => {
      if (!pendingConfirmForm) return;
      const formToSubmit = pendingConfirmForm;
      pendingConfirmForm = null;
      formToSubmit.dataset.confirmed = "true";
      confirmDialog.close();
      formToSubmit.requestSubmit();
    });

    confirmDialog.addEventListener("cancel", () => {
      pendingConfirmForm = null;
    });
  }

  /* ------------------------------------------------------------------------
     Fechar alertas flash
     ------------------------------------------------------------------------ */
  document.addEventListener("click", (event) => {
    const dismissBtn = event.target.closest("[data-dismiss-alert]");
    if (!dismissBtn) return;
    const alertEl = dismissBtn.closest(".admin-alert");
    if (alertEl) alertEl.remove();
  });
})();
