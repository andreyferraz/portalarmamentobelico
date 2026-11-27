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

  /* ------------------------------------------------------------------------
     Formulário de Edições: Idioma -> Moeda reativa, Preview de Capa e Repeaters
     ------------------------------------------------------------------------ */
  const edicaoForm = document.querySelector("[data-edicao-form]");
  if (edicaoForm) {
    const maxExtraPhotos = Number(edicaoForm.dataset.maxExtraPhotos || 5);
    const pricePrefixEl = document.getElementById("edicao-price-prefix");
    const moedaDisplayEl = document.getElementById("edicao-moeda-display");
    const specIdiomaEl = document.getElementById("edicao-spec-idioma-preview");
    const idiomaRadios = edicaoForm.querySelectorAll('input[name="idioma"]');

    function syncIdiomaMoedaUI() {
      const checked = edicaoForm.querySelector('input[name="idioma"]:checked');
      if (!checked) return;
      if (pricePrefixEl) pricePrefixEl.textContent = checked.dataset.currencySymbol || "R$";
      if (moedaDisplayEl) moedaDisplayEl.textContent = checked.dataset.currencyCode || "BRL";
      if (specIdiomaEl) specIdiomaEl.textContent = checked.dataset.specLanguage || "Português (Brasil)";
    }

    idiomaRadios.forEach((radio) => radio.addEventListener("change", syncIdiomaMoedaUI));
    syncIdiomaMoedaUI();

    // Preview da capa selecionada
    const capaInput = document.getElementById("capa");
    const coverPreviewImg = document.getElementById("cover-preview-img");
    const coverPreviewPlaceholder = document.getElementById("cover-preview-placeholder");
    if (capaInput && coverPreviewImg) {
      capaInput.addEventListener("change", () => {
        const file = capaInput.files && capaInput.files[0];
        if (!file) return;
        const objectUrl = URL.createObjectURL(file);
        coverPreviewImg.src = objectUrl;
        coverPreviewImg.hidden = false;
        if (coverPreviewPlaceholder) coverPreviewPlaceholder.hidden = true;
      });
    }

    // Reindexar Entrevistas
    const interviewsList = document.getElementById("interviews-list");
    const addInterviewBtn = document.getElementById("add-interview-btn");

    function reindexInterviews() {
      if (!interviewsList) return;
      const rows = interviewsList.querySelectorAll("[data-interview-row]");
      rows.forEach((row, idx) => {
        const inputs = row.querySelectorAll("input");
        if (inputs[0]) inputs[0].name = `entrevistas[${idx}].banda`;
        if (inputs[1]) inputs[1].name = `entrevistas[${idx}].pais`;
        if (inputs[2]) inputs[2].name = `entrevistas[${idx}].estilo`;
      });
    }

    if (addInterviewBtn && interviewsList) {
      addInterviewBtn.addEventListener("click", () => {
        const idx = interviewsList.querySelectorAll("[data-interview-row]").length;
        const row = document.createElement("div");
        row.className = "repeater-row repeater-row-3";
        row.setAttribute("data-interview-row", "");
        row.innerHTML = `
          <input type="text" name="entrevistas[${idx}].banda" class="admin-input" placeholder="Banda (ex.: Diabolizer)">
          <input type="text" name="entrevistas[${idx}].pais" class="admin-input" placeholder="País (ex.: Turquia)">
          <input type="text" name="entrevistas[${idx}].estilo" class="admin-input" placeholder="Estilo (ex.: Death Metal)">
          <button type="button" class="btn btn-ghost btn-sm" data-remove-row aria-label="Remover entrevista">Remover</button>
        `;
        interviewsList.appendChild(row);
      });
    }

    // Reindexar Demais Conteúdos
    const extrasList = document.getElementById("extra-contents-list");
    const addExtraBtn = document.getElementById("add-extra-content-btn");

    function reindexExtraContents() {
      if (!extrasList) return;
      const rows = extrasList.querySelectorAll("[data-extra-content-row]");
      rows.forEach((row, idx) => {
        const input = row.querySelector("input");
        if (input) input.name = `conteudosExtras[${idx}]`;
      });
    }

    if (addExtraBtn && extrasList) {
      addExtraBtn.addEventListener("click", () => {
        const idx = extrasList.querySelectorAll("[data-extra-content-row]").length;
        const row = document.createElement("div");
        row.className = "repeater-row repeater-row-1";
        row.setAttribute("data-extra-content-row", "");
        row.innerHTML = `
          <input type="text" name="conteudosExtras[${idx}]" class="admin-input" placeholder="Ex.: Dossiê especial da cena underground">
          <button type="button" class="btn btn-ghost btn-sm" data-remove-row aria-label="Remover item">Remover</button>
        `;
        extrasList.appendChild(row);
      });
    }

    // Novas Fotos Extras (respeitando limite de 5)
    const newPhotosList = document.getElementById("new-photos-list");
    const addPhotoBtn = document.getElementById("add-extra-photo-btn");
    const photoCounterEl = document.getElementById("extra-photos-counter");

    function countActiveExtraPhotos() {
      let existingKept = 0;
      edicaoForm.querySelectorAll("[data-existing-photo]").forEach((card) => {
        const removeBox = card.querySelector("[data-remove-existing-photo]");
        if (!removeBox || !removeBox.checked) existingKept += 1;
      });
      const added = newPhotosList ? newPhotosList.querySelectorAll("[data-new-photo-row]").length : 0;
      return existingKept + added;
    }

    function syncExtraPhotosLimit() {
      const total = countActiveExtraPhotos();
      if (addPhotoBtn) {
        addPhotoBtn.disabled = total >= maxExtraPhotos;
      }
      if (photoCounterEl) {
        photoCounterEl.textContent = `${total} de ${maxExtraPhotos} fotos extras utilizadas.`;
      }
    }

    if (addPhotoBtn && newPhotosList) {
      addPhotoBtn.addEventListener("click", () => {
        if (countActiveExtraPhotos() >= maxExtraPhotos) {
          showToast(`Limite máximo de ${maxExtraPhotos} fotos extras atingido.`);
          return;
        }
        const row = document.createElement("div");
        row.className = "repeater-row repeater-row-2";
        row.setAttribute("data-new-photo-row", "");
        row.innerHTML = `
          <input type="file" name="novasFotos" accept="image/*" class="admin-input" required>
          <input type="text" name="novasFotosLegendas" class="admin-input" placeholder="Legenda da foto extra">
          <button type="button" class="btn btn-ghost btn-sm" data-remove-row aria-label="Remover foto extra">Remover</button>
        `;
        newPhotosList.appendChild(row);
        syncExtraPhotosLimit();
      });
    }

    edicaoForm.addEventListener("change", (event) => {
      if (event.target.matches("[data-remove-existing-photo]")) {
        syncExtraPhotosLimit();
      }
    });

    edicaoForm.addEventListener("click", (event) => {
      const removeBtn = event.target.closest("[data-remove-row]");
      if (!removeBtn) return;
      const row = removeBtn.closest(".repeater-row");
      if (!row) return;
      row.remove();
      reindexInterviews();
      reindexExtraContents();
      syncExtraPhotosLimit();
    });

    syncExtraPhotosLimit();
  }

  /* ------------------------------------------------------------------------
     Formulário de Notícias: Auto-Slug, Abas PT/EN, Quill 2 Local e Tags
     ------------------------------------------------------------------------ */
  const noticiaForm = document.querySelector("[data-noticia-form]");
  if (noticiaForm) {
    const maxTags = Number(noticiaForm.dataset.maxTags || 8);
    const modoEdicao = noticiaForm.dataset.modoEdicao === "true";

    // Auto-slug a partir do Título PT
    const tituloPtInput = document.getElementById("tituloPt");
    const slugInput = document.getElementById("noticia-slug");
    const slugRegenBtn = document.getElementById("noticia-slug-regen-btn");
    let slugEditadoManualmente = modoEdicao || (slugInput && slugInput.value.trim().length > 0);

    function gerarSlugJs(texto) {
      return String(texto || "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, "-")
        .replace(/^-+|-+$/g, "");
    }

    if (slugInput && tituloPtInput) {
      slugInput.addEventListener("input", () => {
        slugEditadoManualmente = slugInput.value.trim().length > 0;
      });
      tituloPtInput.addEventListener("input", () => {
        if (!slugEditadoManualmente) {
          slugInput.value = gerarSlugJs(tituloPtInput.value);
        }
      });
      if (slugRegenBtn) {
        slugRegenBtn.addEventListener("click", () => {
          slugInput.value = gerarSlugJs(tituloPtInput.value);
          slugEditadoManualmente = false;
        });
      }
    }

    // Preview da imagem de destaque
    const imgInput = document.getElementById("imagemDestaque");
    const imgPreview = document.getElementById("noticia-img-preview");
    const imgPlaceholder = document.getElementById("noticia-img-placeholder");
    if (imgInput && imgPreview) {
      imgInput.addEventListener("change", () => {
        const file = imgInput.files && imgInput.files[0];
        if (!file) return;
        imgPreview.src = URL.createObjectURL(file);
        imgPreview.hidden = false;
        if (imgPlaceholder) imgPlaceholder.hidden = true;
      });
    }

    // Abas PT / EN
    const tabBtns = noticiaForm.querySelectorAll("[data-lang-tab]");
    const tabPanels = noticiaForm.querySelectorAll("[data-lang-panel]");
    tabBtns.forEach((btn) => {
      btn.addEventListener("click", () => {
        const target = btn.dataset.langTab;
        tabBtns.forEach((b) => {
          const active = b.dataset.langTab === target;
          b.classList.toggle("is-active", active);
          b.setAttribute("aria-selected", String(active));
        });
        tabPanels.forEach((panel) => {
          panel.hidden = panel.dataset.langPanel !== target;
        });
      });
    });

    // Quill 2 Local + Blots customizados de Entrevista (.interview-question / .interview-answer)
    const quillEditors = [];
    if (typeof window.Quill !== "undefined") {
      const Block = window.Quill.import("blots/block");
      if (!window.Quill.imports["formats/interviewQuestion"]) {
        class InterviewQuestionBlot extends Block {
          static blotName = "interviewQuestion";
          static tagName = "p";
          static className = "interview-question";
        }
        window.Quill.register(InterviewQuestionBlot);
      }
      if (!window.Quill.imports["formats/interviewAnswer"]) {
        class InterviewAnswerBlot extends Block {
          static blotName = "interviewAnswer";
          static tagName = "p";
          static className = "interview-answer";
        }
        window.Quill.register(InterviewAnswerBlot);
      }

      function initQuillPair(editorId, toolbarId, textareaId, placeholder) {
        const editorEl = document.getElementById(editorId);
        const toolbarEl = document.getElementById(toolbarId);
        const textareaEl = document.getElementById(textareaId);
        if (!editorEl || !toolbarEl || !textareaEl) return;

        const quill = new window.Quill(editorEl, {
          theme: "snow",
          placeholder,
          modules: {
            toolbar: toolbarEl,
          },
        });

        const initialHtml = (textareaEl.value || "").trim();
        if (initialHtml) {
          quill.clipboard.dangerouslyPasteHTML(initialHtml);
        }

        function syncToTextarea() {
          const html = typeof quill.getSemanticHTML === "function"
            ? quill.getSemanticHTML()
            : quill.root.innerHTML;
          textareaEl.value = quill.getText().trim().length === 0 ? "" : html;
        }

        toolbarEl.querySelectorAll("[data-quill-interview]").forEach((btn) => {
          btn.addEventListener("click", () => {
            const formatName = btn.dataset.quillInterview;
            const otherFormat = formatName === "interviewQuestion" ? "interviewAnswer" : "interviewQuestion";
            const range = quill.getSelection(true);
            if (!range) return;
            const current = quill.getFormat(range);
            const willEnable = !current[formatName];
            quill.format(otherFormat, false, "user");
            quill.format(formatName, willEnable, "user");
            syncToTextarea();
          });
        });

        quill.on("selection-change", (range) => {
          const fmt = range ? quill.getFormat(range) : {};
          toolbarEl.querySelectorAll("[data-quill-interview]").forEach((btn) => {
            const isActive = Boolean(fmt[btn.dataset.quillInterview]);
            btn.classList.toggle("is-active", isActive);
            btn.setAttribute("aria-pressed", String(isActive));
          });
        });

        quill.on("text-change", syncToTextarea);
        quillEditors.push({ syncToTextarea });
      }

      initQuillPair("quill-editor-pt", "quill-toolbar-pt", "conteudoHtmlPt", "Redija o corpo da matéria em Português...");
      initQuillPair("quill-editor-en", "quill-toolbar-en", "conteudoHtmlEn", "Write the optional English version here...");

      noticiaForm.addEventListener("submit", () => {
        quillEditors.forEach((item) => item.syncToTextarea());
      });
    }

    // Repeater de Tags (até maxTags)
    const tagsList = document.getElementById("noticia-tags-list");
    const addTagBtn = document.getElementById("add-noticia-tag-btn");
    const tagsCounterEl = document.getElementById("noticia-tags-counter");

    function reindexTags() {
      if (!tagsList) return;
      const rows = tagsList.querySelectorAll("[data-tag-row]");
      rows.forEach((row, idx) => {
        const input = row.querySelector("input");
        const select = row.querySelector("select");
        if (input) input.name = `tags[${idx}].nome`;
        if (select) select.name = `tags[${idx}].estilo`;
      });
      if (addTagBtn) addTagBtn.disabled = rows.length >= maxTags;
      if (tagsCounterEl) {
        tagsCounterEl.textContent = `${rows.length} de ${maxTags} linha(s) de tag.`;
      }
    }

    if (addTagBtn && tagsList) {
      addTagBtn.addEventListener("click", () => {
        const count = tagsList.querySelectorAll("[data-tag-row]").length;
        if (count >= maxTags) {
          showToast(`Limite máximo de ${maxTags} tags atingido.`);
          return;
        }
        const row = document.createElement("div");
        row.className = "repeater-row repeater-row-2";
        row.setAttribute("data-tag-row", "");
        row.innerHTML = `
          <input type="text" name="tags[${count}].nome" class="admin-input" placeholder="Ex.: Death Metal, Edição #58, Turquia">
          <select name="tags[${count}].estilo" class="admin-select">
            <option value="DARK">Escura</option>
            <option value="BLOOD">Vermelho Sangue</option>
          </select>
          <button type="button" class="btn btn-ghost btn-sm" data-remove-tag-row aria-label="Remover tag">Remover</button>
        `;
        tagsList.appendChild(row);
        reindexTags();
      });

      tagsList.addEventListener("click", (event) => {
        const removeBtn = event.target.closest("[data-remove-tag-row]");
        if (!removeBtn) return;
        const row = removeBtn.closest("[data-tag-row]");
        if (row) {
          row.remove();
          reindexTags();
        }
      });

      reindexTags();
    }
  }

  /* ------------------------------------------------------------------------
     Módulo de Eventos: Dialog de Recusa, Lightbox de Flyer e Form Admin
     ------------------------------------------------------------------------ */
  const recusarDialog = document.getElementById("admin-recusar-dialog");
  const recusarForm = document.getElementById("admin-recusar-form");
  const recusarMessage = document.getElementById("admin-recusar-message");
  const recusarMotivoInput = document.getElementById("admin-recusar-motivo");
  const recusarCancelBtn = document.getElementById("admin-recusar-cancel");

  if (recusarDialog && recusarForm) {
    document.addEventListener("click", (event) => {
      const btn = event.target.closest("[data-open-recusar-dialog]");
      if (!btn) return;
      recusarForm.action = btn.dataset.recusarAction || recusarForm.action;
      const titulo = btn.dataset.recusarTitulo || "este evento";
      if (recusarMessage) {
        recusarMessage.textContent = `Recusar "${titulo}"? O evento permanecerá no histórico administrativo e não aparecerá na agenda pública.`;
      }
      if (recusarMotivoInput) recusarMotivoInput.value = "";
      recusarDialog.showModal();
    });

    if (recusarCancelBtn) {
      recusarCancelBtn.addEventListener("click", () => recusarDialog.close());
    }
  }

  const adminFlyerDialog = document.getElementById("admin-flyer-dialog");
  const adminFlyerImg = document.getElementById("admin-flyer-img");
  const adminFlyerTitle = document.getElementById("admin-flyer-title");
  const adminFlyerClose = document.getElementById("admin-flyer-close");

  if (adminFlyerDialog && adminFlyerImg) {
    document.addEventListener("click", (event) => {
      const btn = event.target.closest("[data-admin-flyer-src]");
      if (!btn) return;
      adminFlyerImg.src = btn.dataset.adminFlyerSrc || "";
      adminFlyerImg.alt = `Flyer ampliado: ${btn.dataset.adminFlyerTitle || ""}`;
      if (adminFlyerTitle) {
        adminFlyerTitle.textContent = btn.dataset.adminFlyerTitle || "Flyer do Evento";
      }
      adminFlyerDialog.showModal();
    });

    if (adminFlyerClose) {
      adminFlyerClose.addEventListener("click", () => adminFlyerDialog.close());
    }
    adminFlyerDialog.addEventListener("click", (event) => {
      if (event.target === adminFlyerDialog) adminFlyerDialog.close();
    });
  }

  const eventoAdminForm = document.querySelector("[data-evento-form]");
  if (eventoAdminForm) {
    const flyerInput = document.getElementById("ev-admin-flyer");
    const flyerPreview = document.getElementById("evento-flyer-preview");
    const flyerPlaceholder = document.getElementById("evento-flyer-placeholder");
    if (flyerInput && flyerPreview) {
      flyerInput.addEventListener("change", () => {
        const file = flyerInput.files && flyerInput.files[0];
        if (!file) return;
        flyerPreview.src = URL.createObjectURL(file);
        flyerPreview.hidden = false;
        if (flyerPlaceholder) flyerPlaceholder.hidden = true;
      });
    }

    const statusSelect = document.getElementById("ev-admin-status");
    const motivoWrap = document.getElementById("ev-admin-motivo-wrap");
    if (statusSelect && motivoWrap) {
      const syncMotivoVisibility = () => {
        motivoWrap.hidden = statusSelect.value !== "RECUSADO";
      };
      statusSelect.addEventListener("change", syncMotivoVisibility);
      syncMotivoVisibility();
    }
  }
})();
