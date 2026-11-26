/**
 * ARMAMENTO BÉLICO - UNDERGROUND ZINE SINCE 2008
 * Core JavaScript Engine: State Management, i18n, Cart, WhatsApp & PayPal checkout
 * Pure Vanilla JS - No Frameworks
 */

(function () {
  'use strict';

  // --- CATÁLOGO DINÂMICO INJETADO PELO BACKEND (#catalog-page-data e #store-config-data) ---
  // REGRA OFICIAL: Edições em PT (R$ / BRL) vs Edições em EN (€ / EUR)

  // --- DICIONÁRIO DE TRADUÇÃO I18N ---
  const TRANSLATIONS = {
    pt: {
      site_tagline: 'Fanzine Impresso de Metal Extremo • Desde 2008',
      nav_home: 'Início',
      nav_history: 'História',
      nav_archive: 'Edições e loja',
      nav_news: 'Notícias',
      nav_events: 'Eventos',
      nav_subscription: 'Assinatura',
      nav_allies: 'Aliados',
      cart_title: 'Necrocarrinho',
      cart_empty: 'Seu caixão está vazio. Nenhum fanzine adicionado.',
      cart_total: 'Total da Blasfêmia:',
      cart_btn_add: 'Comprar Cópia',
      cart_btn_added: 'Adicionado!',
      cart_sold_out: 'Esgotado',
      cart_checkout_whatsapp: 'Finalizar Pedido via WhatsApp',
      hero_title: 'Armamento Bélico Zine',
      hero_manifesto: 'O fanzine físico e impresso mais ativo do underground brasileiro. Desde 2008 registrando o sangue, o suor e a podridão do Death Metal, Black Metal e demais gêneros da música extrema.',
      hero_cta_buy: 'Comprar Edição Atual',
      hero_cta_archive: 'Explorar Coleção Completa',
      hero_follow_us: 'Siga nas redes:',
      section_editions_title: 'Edições Disponíveis em Português',
      section_editions_sub: 'Tiragens físicas limitadas em papel offset pesado • Envio para todo o Brasil',
      section_editions_all: 'Conferir todas as edições',
      youtube_title: 'Canal Oficial do Armamento Bélico no YouTube',
      youtube_desc: 'Entrevistas em vídeo, audições comentadas de demos raras, resenhas de materiais obscuros e cobertura dos porões do metal extremo.',
      youtube_btn: 'Inscreva-se no Canal',
      news_section_title: 'Últimas Manifestações do Underground',
      news_section_sub: 'Resenhas, comunicados de gravadoras e anúncios exclusivos',
      news_feed_tag: 'DESPACHOS DAS TREVAS',
      news_read_all: 'Ver Todas as Notícias',
      news_read_more: 'Ler Matéria Completa ☠',
      news_back_feed: '← Voltar ao Feed de Notícias',
      news_meta_by: 'Por:',
      news_meta_category: 'Categoria:',
      news_search_label: 'Buscar notícias',
      news_search_placeholder: 'Buscar por banda, título, categoria ou tag...',
      news_search_btn: 'Buscar',
      news_clear_filters: 'Limpar filtros',
      news_empty_title: 'Nenhuma notícia encontrada',
      news_empty_text: 'Não há despachos publicados que correspondam aos filtros selecionados no momento.',
      news_page_prev: '← Anterior',
      news_page_next: 'Próxima →',
      news_page_label: 'Página',
      footer_cult: 'Desde 2008 - ' + new Date().getFullYear() + ' • Armamento Bélico Fanzine • O Verdadeiro Culto ao Underground',
      footer_dev_prefix: 'Desenvolvido com devoção e ódio por',
      footer_dev_suffix: 'Puro metal infernal.',
      footer_privacy: 'Privacidade & Cookies',
      archive_title: 'Arquivo do Necrotério - Todas as Edições',
      archive_sub: 'O registro histórico de 18 anos de publicações impressas',
      archive_filter_all: 'Todas as Edições',
      archive_filter_stock: 'Apenas em Estoque',
      archive_filter_sold: 'Esgotados',
      subscription_title: 'Assine o Armamento Bélico',
      subscription_sub: 'Receba 4 edições impressas em 12 meses, com brindes inclusos',
      subscription_warning: 'Ainda não disponível para este idioma. No momento, as assinaturas são possíveis apenas com as edições em português do Brasil.',
      history_title: 'A Trajetória do Armamento Bélico',
      history_sub: '18 Anos de Resistência, Tinta e Morte no Underground',
      history_logos_tag: 'IDENTIDADE PROFANA',
      history_logos_title: 'Evolução das Logos do Armamento Bélico',
      history_logos_sub: 'Todas as insígnias utilizadas pelo fanzine, suas respectivas edições lançadas e o período de uso de cada fase',
      allies_title: 'Pactos & Aliados do Cenário',
      allies_sub: 'Selos, gravadoras e fanzines irmãos que mantêm a chama profana acesa',
      privacy_title: 'Manifesto de Privacidade & Cookies',
      privacy_sub: 'Respeito irrestrito ao submundo e à privacidade do headbanger',
      cta_tag: 'PACTO DE APOIO CONTÍNUO',
      cta_mini_tag: 'TIRAGENS FÍSICAS LIMITADAS',
      cta_title: 'Assine o Armamento Bélico Zine',
      cta_desc: 'Faça parte do culto físico ao Metal da Morte e receba cada manifestação impressa na sua morada sem risco de esgotamento.',
      cta_price_period: '/ mês (Assinatura Anual • Pix)',
      cta_guarantee: 'Plano único de 12 meses (R$ 20/mês no Pix recorrente ou R$ 22/mês no cartão de crédito). Somente edições em português (Brasil).',
      cta_feat_1_b: '4 Edições no Ano:',
      cta_feat_1_t: '12 meses de assinatura com 4 edições entregues: 1 edição a cada 3 meses de pagamento.',
      cta_feat_2_b: 'Pix ou Cartão de Crédito:',
      cta_feat_2_t: 'R$ 20,00/mês via Pix recorrente ou R$ 22,00/mês no cartão de crédito (para cobrir taxas).',
      cta_feat_3_b: 'Envio pelos Correios:',
      cta_feat_3_t: 'Despacho após a conclusão de cada ciclo de 3 meses pagos e o lançamento da respectiva edição.',
      cta_feat_4_b: 'Brindes Inclusos:',
      cta_feat_4_t: 'A assinatura inclui brindes, que variam conforme a disponibilidade de cada remessa.',
      cta_btn: 'Garantir Assinatura • R$ 20/mês',
      cta_btn_wa: 'Selar via WhatsApp',
      cta_art_caption: 'O Baphomet aprova: sabedoria profana e metal extremo impresso em suas garras.',
      events_tag: 'AGENDA DO SUBTERRÂNEO',
      events_title: 'Eventos de Música Extrema',
      events_subtitle: 'Shows, festivais e rituais do underground nacional. Eventos futuros aparecem primeiro e passam a "Já aconteceu" automaticamente no dia seguinte.',
      events_rules_title: 'Regras Obrigatórias para Cadastro de Eventos',
      events_filter_state_label: 'Filtrar por Estado (UF):',
      events_filter_all_states: 'Todos os Estados',
      events_filter_status_label: 'Status:',
      events_cta_submit: 'Cadastrar Evento',
      events_form_title: 'Cadastrar Novo Evento',
      events_form_subtitle: 'Preencha todos os dados abaixo. Após o envio, o evento ficará aguardando aprovação manual antes de aparecer na agenda.',
      nav_suggest: 'Indique uma Horda',
      suggest_tag: 'CURADORIA SUBTERRÂNEA',
      suggest_title: 'Indique uma Horda',
      suggest_subtitle: 'Conhece alguma banda de Metal Extremo com material físico lançado que merece ser entrevistada no Armamento Bélico Zine? Envie sua indicação abaixo.',
      suggest_rules_tag: 'CRITÉRIOS DO ZINE',
      suggest_rules_title: 'Orientações para Indicação de Bandas',
      suggest_form_tag: 'SUGESTÃO PARA ENTREVISTA',
      suggest_form_title: 'Formulário de Indicação',
      suggest_form_subtitle: 'Informe os dados da horda, o título do lançamento físico e um link para audição.',
      suggest_field_name: 'Nome da Banda',
      suggest_field_country: 'País',
      suggest_field_region: 'Estado / Região (opcional)',
      suggest_field_release: 'Título do Material Físico Lançado',
      suggest_field_url: 'Link para Ouvir a Banda na Internet',
      suggest_submit_btn: 'Enviar Indicação',
      suggest_list_tag: 'REGISTRO DE INDICAÇÕES',
      suggest_list_title: 'Hordas Já Indicadas',
      edition_zoom_hint: 'Ampliar',
      edition_meta_month: 'Mês de Lançamento',
      edition_meta_year: 'Ano de Lançamento',
      edition_meta_existence: 'Ano da Revista',
      edition_meta_pages: 'Páginas',
      edition_price_label: 'Valor da Edição Impressa',
      edition_cta_subscribe: 'Assinar o Zine',
      edition_share_label: 'Compartilhar edição:',
      edition_copy_link: 'Copiar link',
      edition_desc_tag: 'CONTEÚDO DA EDIÇÃO',
      edition_desc_title: 'Descrição & Entrevistas',
      edition_interviews_title: 'Hordas & Artistas Entrevistados',
      edition_extras_title: 'Demais Conteúdos Presentes',
      edition_specs_tag: 'FICHA TÉCNICA',
      edition_specs_title: 'Especificações do Zine',
      edition_specs_note: 'Publicação 100% independente, impressa fisicamente em papel, enviada protegida contra dobras e umidade.',
      edition_video_tag: 'FOLHEANDO O ZINE • YOUTUBE',
      edition_video_title: 'Apresentação em Vídeo da Edição #58',
      edition_video_watch_yt: 'Abrir no YouTube',
      edition_nav_prev: 'Edição Anterior',
      edition_nav_catalog: 'Coleção Completa',
      edition_nav_next: 'Próxima Edição'
    },

    en: {
      site_tagline: 'Extreme Metal Printed Zine • Since 2008',
      nav_home: 'Home',
      nav_history: 'History',
      nav_archive: 'Editions & Shop',
      nav_news: 'News Feed',
      nav_events: 'Events',
      nav_subscription: 'Subscription',
      nav_allies: 'Allies',
      cart_title: 'Death Cart',
      cart_empty: 'Your coffin is empty. No zines summoned.',
      cart_total: 'Total Sacrificed:',
      cart_btn_add: 'Buy Print Copy',
      cart_btn_added: 'Added to Cart!',
      cart_sold_out: 'Sold Out',
      cart_checkout_paypal: 'Checkout with PayPal',
      hero_title: 'Armamento Bélico Zine',
      hero_manifesto: 'The most uncompromising printed underground zine from South America. Devoted since 2008 to documenting the filth, blood, and chaos of Death Metal, Black Metal, Grindcore and Doom without corporate compromise.',
      hero_cta_buy: 'Get Current Issue',
      hero_cta_archive: 'Explore Full Collection',
      hero_follow_us: 'Follow us:',
      section_editions_title: 'Available English Print Editions',
      section_editions_sub: 'Limited international physical printings • Worldwide registered shipping',
      section_editions_all: 'Check All Editions',
      youtube_title: 'Armamento Bélico Official YouTube Channel',
      youtube_desc: 'Video interviews, rare demo tape listening sessions, unboxing obscure releases and coverage from the deep catacombs of extreme metal.',
      youtube_btn: 'Subscribe on YouTube',
      news_section_title: 'Latest Underground Broadcasts',
      news_section_sub: 'Reviews, label press dispatches and exclusive declarations',
      news_feed_tag: 'DISPATCHES FROM THE DARK',
      news_read_all: 'View All News',
      news_read_more: 'Read Full Article ☠',
      news_back_feed: '← Back to News Feed',
      news_meta_by: 'By:',
      news_meta_category: 'Category:',
      news_search_label: 'Search news',
      news_search_placeholder: 'Search by band, title, category or tag...',
      news_search_btn: 'Search',
      news_clear_filters: 'Clear filters',
      news_empty_title: 'No news articles found',
      news_empty_text: 'There are no published dispatches matching your current filters.',
      news_page_prev: '← Previous',
      news_page_next: 'Next →',
      news_page_label: 'Page',
      footer_cult: 'Since 2008 - ' + new Date().getFullYear() + ' • Armamento Bélico Zine • Total Underground Worship',
      footer_dev_prefix: 'Developed with devotion and hatred by',
      footer_dev_suffix: 'Pure infernal metal.',
      footer_privacy: 'Privacy & Cookies',
      archive_title: 'The Catacomb Archive - Complete Issues',
      archive_sub: 'Historical chronicle of 18 years of printed physical destruction',
      archive_filter_all: 'All Issues',
      archive_filter_stock: 'In Stock Only',
      archive_filter_sold: 'Sold Out',
      subscription_title: 'Armamento Bélico Subscriptions',
      subscription_sub: 'Subscriptions are currently available only for the Brazilian Portuguese editions.',
      subscription_warning: 'Not yet available for this language. Subscriptions are currently only available for editions in Brazilian Portuguese.',
      history_title: 'The History of Armamento Bélico',
      history_sub: '18 Years of Resistance, Xerox and Underground Death Metal',
      history_logos_tag: 'PROFANE IDENTITY',
      history_logos_title: 'Evolution of Armamento Bélico Logos',
      history_logos_sub: 'Every emblem used by the fanzine, the issues released with each, and their active time periods',
      allies_title: 'Alliances & Underground Pacts',
      allies_sub: 'Comrades, labels, and sister zines sustaining the real underground flame',
      privacy_title: 'Privacy & Cookie Manifesto',
      privacy_sub: 'Strict non-commercial ethics and total respect for underground headbangers',
      cta_tag: 'CONTINUOUS SUPPORT PACT',
      cta_mini_tag: 'LIMITED PHYSICAL PRINT RUNS',
      cta_title: 'Subscribe to Armamento Bélico Zine',
      cta_desc: 'Join the physical Death Metal underground cult. Guarantee your copies directly from the press delivered to your catacomb.',
      cta_price_period: '/ month (Annual Plan • Pix)',
      cta_guarantee: '12-month single plan (R$ 20/mo via recurring Pix or R$ 22/mo via credit card). Brazilian Portuguese editions only.',
      cta_feat_1_b: '4 Issues a Year:',
      cta_feat_1_t: '12-month subscription with 4 issues delivered: 1 issue every 3 months of payment.',
      cta_feat_2_b: 'Pix or Credit Card:',
      cta_feat_2_t: 'R$ 20.00/mo via recurring Pix or R$ 22.00/mo via credit card (to cover fees).',
      cta_feat_3_b: 'Correios Shipping:',
      cta_feat_3_t: 'Shipped after each 3-month paid cycle is completed and the corresponding issue is released.',
      cta_feat_4_b: 'Gifts Included:',
      cta_feat_4_t: 'Includes gifts, which vary according to availability for each shipment.',
      cta_btn: 'Subscribe Now • R$ 20.00/mo',
      cta_btn_wa: 'Chat with Editor via WhatsApp',
      cta_art_caption: 'Baphomet approves: profane knowledge and extreme metal ink in demonic claws.',
      events_tag: 'UNDERGROUND AGENDA',
      events_title: 'Extreme Metal Events',
      events_subtitle: 'Underground gigs, festivals and rituals. Upcoming events appear first and automatically switch to "Already happened" the day after.',
      events_rules_title: 'Mandatory Rules for Event Submission',
      events_filter_state_label: 'Filter by State (UF):',
      events_filter_all_states: 'All States',
      events_filter_status_label: 'Status:',
      events_cta_submit: 'Submit Event',
      events_form_title: 'Submit New Event',
      events_form_subtitle: 'Fill in all fields below. After submission, the event awaits manual editor approval before appearing on the agenda.',
      nav_suggest: 'Suggest a Horde',
      suggest_tag: 'UNDERGROUND CURATION',
      suggest_title: 'Suggest a Horde',
      suggest_subtitle: 'Know an Extreme Metal band with a physical release that deserves to be interviewed in Armamento Bélico Zine? Submit your suggestion below.',
      suggest_rules_tag: 'ZINE CRITERIA',
      suggest_rules_title: 'Guidelines for Band Suggestions',
      suggest_form_tag: 'INTERVIEW SUGGESTION',
      suggest_form_title: 'Band Suggestion Form',
      suggest_form_subtitle: 'Provide the band details, physical release title, and a listening link.',
      suggest_field_name: 'Band Name',
      suggest_field_country: 'Country',
      suggest_field_region: 'State / Region (optional)',
      suggest_field_release: 'Physical Release Title',
      suggest_field_url: 'Link to Listen Online',
      suggest_submit_btn: 'Submit Suggestion',
      suggest_list_tag: 'SUGGESTIONS LOG',
      suggest_list_title: 'Already Suggested Hordes',
      edition_zoom_hint: 'Zoom',
      edition_meta_month: 'Release Month',
      edition_meta_year: 'Release Year',
      edition_meta_existence: 'Zine Year',
      edition_meta_pages: 'Pages',
      edition_price_label: 'Print Edition Price',
      edition_cta_subscribe: 'Subscribe to Zine',
      edition_share_label: 'Share this issue:',
      edition_copy_link: 'Copy link',
      edition_desc_tag: 'ISSUE CONTENTS',
      edition_desc_title: 'Description & Interviews',
      edition_interviews_title: 'Interviewed Hordes & Artists',
      edition_extras_title: 'Additional Contents',
      edition_specs_tag: 'TECHNICAL SPECS',
      edition_specs_title: 'Zine Specifications',
      edition_specs_note: '100% independent physical print zine, carefully packed to protect against bends and moisture.',
      edition_video_tag: 'FLIP-THROUGH • YOUTUBE',
      edition_video_title: 'Issue #58 Video Showcase',
      edition_video_watch_yt: 'Watch on YouTube',
      edition_nav_prev: 'Previous Issue',
      edition_nav_catalog: 'Full Collection',
      edition_nav_next: 'Next Issue'
    }
  };

  // --- GERENCIAMENTO DE ESTADO (LOCALSTORAGE) ---
  const defaultAddress = {
    fullName: '',
    country: '',
    zip: '',
    street: '',
    number: '',
    complement: '',
    neighborhood: '',
    city: '',
    state: ''
  };
  let savedAddress = defaultAddress;
  try {
    const parsed = JSON.parse(localStorage.getItem('ab_checkout_address_v2') || 'null');
    if (parsed && typeof parsed === 'object') {
      savedAddress = { ...defaultAddress, ...parsed };
    }
  } catch (_) {}

  // Configurações padrão da loja (sobrescritas por #store-config-data injetado pelo backend)
  const DEFAULT_STORE_CONFIG = {
    shipping: {
      'R$': { baseFee: 13.00, additionalFee: 2.00 },
      '€': { baseFee: 10.00, additionalFee: 3.00 }
    },
    whatsappNumber: '5527981076157',
    paypal: {
      email: 'revistadeathmetal@hotmail.com',
      feePercent: 0.064,
      fixedFeeEur: 0.35
    }
  };

  function loadStoreConfig() {
    const scriptEl = document.getElementById('store-config-data');
    if (!scriptEl) return DEFAULT_STORE_CONFIG;
    try {
      const parsed = JSON.parse(scriptEl.textContent || 'null');
      if (!parsed || typeof parsed !== 'object') return DEFAULT_STORE_CONFIG;
      return {
        shipping: {
          'R$': {
            baseFee: Number(parsed.shipping?.['R$']?.baseFee ?? DEFAULT_STORE_CONFIG.shipping['R$'].baseFee),
            additionalFee: Number(parsed.shipping?.['R$']?.additionalFee ?? DEFAULT_STORE_CONFIG.shipping['R$'].additionalFee)
          },
          '€': {
            baseFee: Number(parsed.shipping?.['€']?.baseFee ?? DEFAULT_STORE_CONFIG.shipping['€'].baseFee),
            additionalFee: Number(parsed.shipping?.['€']?.additionalFee ?? DEFAULT_STORE_CONFIG.shipping['€'].additionalFee)
          }
        },
        whatsappNumber: String(parsed.whatsappNumber || DEFAULT_STORE_CONFIG.whatsappNumber),
        paypal: {
          email: String(parsed.paypal?.email || DEFAULT_STORE_CONFIG.paypal.email),
          feePercent: Number(parsed.paypal?.feePercent ?? DEFAULT_STORE_CONFIG.paypal.feePercent),
          fixedFeeEur: Number(parsed.paypal?.fixedFeeEur ?? DEFAULT_STORE_CONFIG.paypal.fixedFeeEur)
        }
      };
    } catch (_) {
      return DEFAULT_STORE_CONFIG;
    }
  }

  let STORE_CONFIG = DEFAULT_STORE_CONFIG;
  let cachedCatalogData = undefined;

  function getCatalogData() {
    if (cachedCatalogData !== undefined) return cachedCatalogData;
    const scriptEl = document.getElementById('catalog-page-data');
    if (!scriptEl) {
      cachedCatalogData = { pt: [], en: [] };
      return cachedCatalogData;
    }
    try {
      const parsed = JSON.parse(scriptEl.textContent || 'null');
      cachedCatalogData = {
        pt: Array.isArray(parsed?.pt) ? parsed.pt : [],
        en: Array.isArray(parsed?.en) ? parsed.en : []
      };
    } catch (_) {
      cachedCatalogData = { pt: [], en: [] };
    }
    return cachedCatalogData;
  }

  function sanitizeSavedCart(rawCart) {
    if (!Array.isArray(rawCart)) return [];
    const validItems = rawCart.filter(
      item =>
        item &&
        typeof item === 'object' &&
        (item.currency === 'R$' || item.currency === '€') &&
        Number(item.quantity) > 0
    );
    if (validItems.length === 0) return [];
    const firstCurrency = validItems[0].currency;
    return validItems.filter(item => item.currency === firstCurrency);
  }

  let initialCart = [];
  try {
    const parsedCart = JSON.parse(localStorage.getItem('ab_cart') || '[]');
    initialCart = sanitizeSavedCart(parsedCart);
    if (Array.isArray(parsedCart) && parsedCart.length !== initialCart.length) {
      localStorage.setItem('ab_cart', JSON.stringify(initialCart));
    }
  } catch (_) {
    initialCart = [];
  }

  const STATE = {
    lang: localStorage.getItem('ab_lang') || 'pt',
    cart: initialCart,
    checkoutAddress: savedAddress,
    isReturningCustomer: localStorage.getItem('ab_returning_customer') === 'true',
    paymentMethod: localStorage.getItem('ab_payment_method') === 'card' ? 'card' : 'pix',
    pendingOrderConfirmation: localStorage.getItem('ab_pending_order_confirmation') === 'true',
    showOrderThankYou: false,
    whatsappNumber: DEFAULT_STORE_CONFIG.whatsappNumber,
    paypalBusinessEmail: DEFAULT_STORE_CONFIG.paypal.email
  };

  let pendingCartReplacementProduct = null;
  let pendingPayPalCheckoutUrl = null;

  let cachedServerEditionData = undefined;
  function getServerEditionData() {
    if (cachedServerEditionData !== undefined) return cachedServerEditionData;
    const scriptEl = document.getElementById('edition-page-data');
    if (!scriptEl) {
      cachedServerEditionData = null;
      return null;
    }
    try {
      cachedServerEditionData = JSON.parse(scriptEl.textContent || 'null');
    } catch (_) {
      cachedServerEditionData = null;
    }
    return cachedServerEditionData;
  }

  function updateSingleLangNotice(serverEdition, uiLang) {
    const notice = document.getElementById('edition-single-lang-notice');
    const noticeText = document.getElementById('edition-single-lang-notice-text');
    if (!notice || !noticeText || !serverEdition) return;

    if (uiLang !== serverEdition.editionLang && !serverEdition.alternateLangUrl) {
      const onlyPt = serverEdition.editionLang === 'pt';
      if (uiLang === 'en') {
        noticeText.textContent = onlyPt
          ? `This physical issue (#${serverEdition.editionNumber}) was published exclusively in Brazilian Portuguese. Interface labels have been switched to English, while the issue content remains in its original Portuguese edition.`
          : `This physical issue (#${serverEdition.editionNumber}) was published exclusively in English.`;
      } else {
        noticeText.textContent = onlyPt
          ? `Esta edição física (#${serverEdition.editionNumber}) foi publicada exclusivamente em Português.`
          : `Esta edição física (#${serverEdition.editionNumber}) foi publicada exclusivamente em Inglês. Os rótulos da interface foram ajustados para Português, mantendo o conteúdo original da edição impressa em Inglês.`;
      }
      notice.hidden = false;
    } else {
      notice.hidden = true;
    }
  }

  // Salvar idioma
  function setLanguage(lang) {
    if (lang !== 'pt' && lang !== 'en') lang = 'pt';

    const serverEdition = getServerEditionData();
    if (serverEdition) {
      if (lang !== serverEdition.editionLang && serverEdition.alternateLangUrl) {
        localStorage.setItem('ab_lang', lang);
        window.location.href = serverEdition.alternateLangUrl;
        return;
      }
      STATE.lang = lang;
      localStorage.setItem('ab_lang', lang);
      applyLanguage(lang);
      updateSingleLangNotice(serverEdition, lang);
      renderCart();
      updateSubscriptionNavVisibility();
      return;
    }

    STATE.lang = lang;
    localStorage.setItem('ab_lang', lang);
    applyLanguage(lang);
    renderCart(); // Recalcula botões do carrinho
    renderProducts(); // Atualiza produtos da página conforme idioma
    renderEditionDetailPage(); // Atualiza template da página da edição (se ativa)
    updateSubscriptionNavVisibility(); // Regra do menu assinatura
  }

  function getLanguage() {
    return STATE.lang;
  }

  // Salvar carrinho
  function saveCart() {
    localStorage.setItem('ab_cart', JSON.stringify(STATE.cart));
    if (STATE.cart.length === 0 && STATE.pendingOrderConfirmation) {
      setPendingOrderConfirmation(false);
    }
    updateCartCountBadge();
    renderCart();
  }

  function setPendingOrderConfirmation(pending) {
    STATE.pendingOrderConfirmation = Boolean(pending);
    if (STATE.pendingOrderConfirmation) {
      localStorage.setItem('ab_pending_order_confirmation', 'true');
    } else {
      localStorage.removeItem('ab_pending_order_confirmation');
    }
  }

  function confirmOrderSuccess() {
    STATE.cart = [];
    STATE.pendingOrderConfirmation = false;
    localStorage.removeItem('ab_pending_order_confirmation');
    STATE.showOrderThankYou = true;
    saveCart();
    showToast(
      STATE.lang === 'pt'
        ? 'Obrigado pelo seu pedido! Carrinho limpo.'
        : 'Thank you for your order! Cart cleared.'
    );
  }

  function dismissOrderConfirmation() {
    setPendingOrderConfirmation(false);
    renderCart();
  }

  function findProductById(productId) {
    const catalogData = getCatalogData();
    const allProducts = [...(catalogData.pt || []), ...(catalogData.en || [])];
    let product = allProducts.find(p => p.id === productId);
    if (!product && editionPageState.edition && editionPageState.edition.id === productId) {
      product = editionPageState.edition;
    }
    return product || null;
  }

  function pushProductToCart(product) {
    const existingIndex = STATE.cart.findIndex(item => item.id === product.id);
    if (existingIndex > -1) {
      STATE.cart[existingIndex].quantity += 1;
    } else {
      STATE.cart.push({
        id: product.id,
        title: product.title,
        price: Number(product.price || 0),
        currency: product.currency,
        cover: product.cover,
        quantity: 1,
        lang: product.editionLang || (product.currency === '€' ? 'en' : 'pt')
      });
    }

    STATE.showOrderThankYou = false;
    if (STATE.pendingOrderConfirmation) {
      setPendingOrderConfirmation(false);
    }

    saveCart();
    openCartDrawer();
    showToast(STATE.lang === 'pt' ? `"${product.title}" adicionado ao caixão!` : `"${product.title}" added to cart!`);
  }

  function openCurrencyConflictDialog(product) {
    const dialog = document.getElementById('cart-currency-conflict-dialog');
    const titleEl = document.getElementById('cart-conflict-title');
    const msgEl = document.getElementById('cart-conflict-message');
    const cancelBtn = document.getElementById('cart-conflict-cancel-btn');
    const replaceBtn = document.getElementById('cart-conflict-replace-btn');

    const currentCurrency = STATE.cart[0]?.currency || 'R$';
    const isPtUi = STATE.lang === 'pt';

    if (!dialog || typeof dialog.showModal !== 'function') {
      showToast(
        isPtUi
          ? `Finalize ou limpe seu carrinho em ${currentCurrency} antes de adicionar edições em ${product.currency}.`
          : `Finish or clear your ${currentCurrency} cart before adding ${product.currency} issues.`
      );
      return;
    }

    pendingCartReplacementProduct = product;

    if (titleEl) {
      titleEl.textContent = isPtUi ? 'Moedas Diferentes no Carrinho' : 'Different Currencies in Cart';
    }
    if (msgEl) {
      msgEl.textContent = isPtUi
        ? `Seu Necrocarrinho atual possui itens em ${currentCurrency}. Pedidos em R$ (Brasil • Correios/WhatsApp) e pedidos em € (Internacional • Airmail/PayPal) são fechados separadamente. Deseja substituir o carrinho atual por "${product.title}" (${product.currency})?`
        : `Your Death Cart currently holds ${currentCurrency} items. Orders in R$ (Brazil • Correios/WhatsApp) and € (International • Airmail/PayPal) must be checked out separately. Replace your current cart with "${product.title}" (${product.currency})?`;
    }
    if (cancelBtn) {
      cancelBtn.textContent = isPtUi ? 'Manter carrinho atual' : 'Keep current cart';
    }
    if (replaceBtn) {
      replaceBtn.textContent = isPtUi ? 'Substituir carrinho' : 'Replace cart';
    }

    dialog.showModal();
  }

  function addToCart(productId) {
    const currentLang = STATE.lang;
    const product = findProductById(productId);

    if (!product || !product.inStock) {
      showToast(currentLang === 'pt' ? 'Item esgotado ou indisponível!' : 'Item is sold out!');
      return;
    }

    if (product.currency !== 'R$' && product.currency !== '€') {
      showToast(currentLang === 'pt' ? 'Moeda inválida para esta edição.' : 'Invalid currency for this issue.');
      return;
    }

    if (STATE.cart.length > 0 && STATE.cart[0].currency !== product.currency) {
      openCurrencyConflictDialog(product);
      return;
    }

    pushProductToCart(product);
  }

  function removeFromCart(productId) {
    STATE.cart = STATE.cart.filter(item => item.id !== productId);
    STATE.showOrderThankYou = false;
    if (STATE.pendingOrderConfirmation) {
      setPendingOrderConfirmation(false);
    }

    saveCart();
  }

  function updateQuantity(productId, delta) {
    const item = STATE.cart.find(i => i.id === productId);
    if (item) {
      item.quantity += delta;
      if (item.quantity <= 0) {
        removeFromCart(productId);
      } else {
        STATE.showOrderThankYou = false;
        if (STATE.pendingOrderConfirmation) {
          setPendingOrderConfirmation(false);
        }

        saveCart();
      }
    }
  }

  function getCartItemCount() {
    return STATE.cart.reduce((sum, item) => sum + item.quantity, 0);
  }

  function getCartSubtotal() {
    return STATE.cart.reduce((total, item) => total + (item.price * item.quantity), 0);
  }

  function calculateShipping(itemCount, currency) {
    if (!itemCount || itemCount <= 0) return 0;
    const rule = STORE_CONFIG.shipping[currency] || STORE_CONFIG.shipping['R$'];
    return Number((rule.baseFee + ((itemCount - 1) * rule.additionalFee)).toFixed(2));
  }

  function roundHalfUp2(value) {
    return Math.round((Number(value || 0) + Number.EPSILON) * 100) / 100;
  }

  function calculatePayPalEurSummary(baseAmountEur) {
    const base = roundHalfUp2(baseAmountEur);
    if (base <= 0) {
      return { base: 0, fee: 0, total: 0 };
    }
    const pct = Number(STORE_CONFIG.paypal?.feePercent ?? 0.064);
    const fixed = Number(STORE_CONFIG.paypal?.fixedFeeEur ?? 0.35);
    const divisor = 1 - pct;
    const total = divisor > 0 ? roundHalfUp2((base + fixed) / divisor) : base;
    const fee = roundHalfUp2(total - base);
    return { base, fee, total };
  }

  function getCartTotal() {
    if (STATE.cart.length === 0) return 0;
    const currency = STATE.cart[0].currency;
    const base = getCartSubtotal() + calculateShipping(getCartItemCount(), currency);
    if (currency === '€') {
      return calculatePayPalEurSummary(base).total;
    }
    return roundHalfUp2(base);
  }

  function formatCurrency(value, currency) {
    const num = Number(value || 0);
    if (currency === 'R$') {
      return `R$ ${num.toFixed(2).replace('.', ',')}`;
    }
    if (currency === '€') {
      return STATE.lang === 'pt'
        ? `€ ${num.toFixed(2).replace('.', ',')}`
        : `€ ${num.toFixed(2)}`;
    }
    return `${currency || '€'} ${num.toFixed(2)}`;
  }

  function updateCartCountBadge() {
    const badge = document.getElementById('cart-count-badge');
    if (badge) {
      badge.textContent = getCartItemCount();
    }
  }

  // --- APLICAÇÃO DE IDIOMA NA UI (I18N) ---
  function applyLanguage(lang) {
    document.documentElement.lang = lang;
    const t = TRANSLATIONS[lang];

    // Atualiza botões de idioma no cabeçalho
    document.querySelectorAll('.lang-btn').forEach(btn => {
      if (btn.getAttribute('data-lang') === lang) {
        btn.classList.add('active');
      } else {
        btn.classList.remove('active');
      }
    });

    // Atualiza todos os elementos marcados com data-i18n
    document.querySelectorAll('[data-i18n]').forEach(el => {
      const key = el.getAttribute('data-i18n');
      if (t[key]) {
        el.textContent = t[key];
      }
    });

    // Atualiza placeholders
    document.querySelectorAll('[data-i18n-placeholder]').forEach(el => {
      const key = el.getAttribute('data-i18n-placeholder');
      if (t[key]) {
        el.placeholder = t[key];
      }
    });

    // Atualiza textos dinâmicos bilíngues (notícias, datas, legendas e selos)
    document.querySelectorAll('[data-text-pt], [data-text-en]').forEach(el => {
      const value = lang === 'en'
        ? (el.getAttribute('data-text-en') || el.getAttribute('data-text-pt'))
        : (el.getAttribute('data-text-pt') || el.getAttribute('data-text-en'));
      if (value !== null) {
        el.textContent = value;
      }
    });

    // Alterna corpo da matéria (.lang-content-pt / .lang-content-en), aviso somente PT e card CTA
    const newsArticle = document.querySelector('.news-single-article');
    if (newsArticle) {
      const ptBlock = newsArticle.querySelector('.lang-content-pt');
      const enBlock = newsArticle.querySelector('.lang-content-en');
      const ptOnlyNotice = document.getElementById('news-pt-only-notice');
      const hasEnContent = Boolean(enBlock && enBlock.textContent.trim().length > 0);

      if (lang === 'en' && hasEnContent) {
        if (ptBlock) ptBlock.style.display = 'none';
        enBlock.style.display = '';
        if (ptOnlyNotice) ptOnlyNotice.hidden = true;
      } else {
        if (ptBlock) ptBlock.style.display = '';
        if (enBlock) enBlock.style.display = 'none';
        if (ptOnlyNotice) ptOnlyNotice.hidden = lang !== 'en';
      }

      const ctaPt = newsArticle.querySelector('.news-cta-lang-pt');
      const ctaEn = newsArticle.querySelector('.news-cta-lang-en');
      if (lang === 'en' && ctaEn) {
        if (ctaPt) ctaPt.style.display = 'none';
        ctaEn.style.display = '';
      } else if (ctaPt) {
        ctaPt.style.display = '';
        if (ctaEn) ctaEn.style.display = 'none';
      }
    }

    // Atualiza seletor de navegação da assinatura (Regra estrita)
    updateSubscriptionNavVisibility();

    // Reavalia se o menu cabe em linha única após mudança de idioma/itens
    if (typeof syncResponsiveNavLayout === 'function') {
      syncResponsiveNavLayout(true);
    }
  }

  // REGRA DO MENU E HOME: Assinatura só deve estar visível quando o idioma ativo for Português
  function updateSubscriptionNavVisibility() {
    const subNavItems = document.querySelectorAll('.nav-assinatura');
    subNavItems.forEach(item => {
      if (STATE.lang === 'pt') {
        item.classList.remove('hidden');
        item.style.display = '';
      } else {
        item.classList.add('hidden');
        item.style.display = 'none';
      }
    });

    const homeSubCta = document.getElementById('cta-assinatura');
    if (homeSubCta) {
      homeSubCta.hidden = STATE.lang !== 'pt';
      homeSubCta.style.display = STATE.lang === 'pt' ? '' : 'none';
    }

    // Se estiver na página de assinatura e mudar para EN, exibe container de aviso
    const subNotice = document.getElementById('sub-intl-notice');
    const subContent = document.getElementById('subscription-content');
    if (subNotice) {
      const isEn = STATE.lang === 'en';
      subNotice.style.display = isEn ? 'block' : 'none';
      if (subContent) subContent.hidden = isEn;
    }
  }

  function applyActiveArchiveFilter() {
    const activeBtn = document.querySelector('.archive-filters .filter-btn.active');
    if (!activeBtn) return;
    const filter = activeBtn.getAttribute('data-filter') || 'all';
    document.querySelectorAll('.product-card').forEach(card => {
      const stock = card.getAttribute('data-stock');
      if (filter === 'all') {
        card.style.display = '';
      } else if (filter === 'instock' && stock === 'instock') {
        card.style.display = '';
      } else if (filter === 'soldout' && stock === 'soldout') {
        card.style.display = '';
      } else {
        card.style.display = 'none';
      }
    });
  }

  // --- RENDERIZAÇÃO DOS PRODUTOS / EDIÇÕES (DADOS REAIS DO BACKEND) ---
  function renderProducts() {
    const productsContainer = document.getElementById('products-grid-container');
    if (!productsContainer) return;

    const currentLang = STATE.lang;
    const catalogData = getCatalogData();
    const listToRender = catalogData[currentLang] || [];
    const isArchivePage = document.body.classList.contains('page-arquivo');

    if (listToRender.length === 0) {
      const emptyTitle = currentLang === 'pt'
        ? (isArchivePage ? 'Nenhuma edição publicada em Português' : 'Nenhuma edição disponível no momento')
        : (isArchivePage ? 'No published English issues yet' : 'No English issues currently in stock');
      const emptyText = currentLang === 'pt'
        ? (isArchivePage
          ? 'Ainda não há edições impressas em Português cadastradas neste catálogo. Volte em breve ou confira a versão em Inglês.'
          : 'Todas as edições em Português cadastradas estão esgotadas ou em preparo na gráfica. Explore o arquivo completo para consultar edições históricas.')
        : (isArchivePage
          ? 'There are no published international English print editions in the catalog yet. Check back soon or switch to Portuguese.'
          : 'No English print editions are currently available in stock. Browse the archive or check back soon for upcoming press runs.');
      const emptyAction = !isArchivePage
        ? `<a href="/arquivo" class="btn-brutal btn-outline-decay btn-sm">${currentLang === 'pt' ? 'Explorar Arquivo Completo' : 'Explore Full Archive'}</a>`
        : '';

      productsContainer.innerHTML = `
        <div class="catalog-empty-state" role="status">
          <div class="catalog-empty-state-icon" aria-hidden="true">☠</div>
          <h3 class="pirata-title">${escapeHtml(emptyTitle)}</h3>
          <p>${escapeHtml(emptyText)}</p>
          ${emptyAction}
        </div>
      `;
      return;
    }

    let html = '';
    listToRender.forEach(zine => {
      const isSoldOut = !zine.inStock || zine.stockStatus === 'soldout';
      const isLowStock = !isSoldOut && zine.stockStatus === 'low';

      const stockBadge = isSoldOut
        ? `<span class="tape-tag dark">${currentLang === 'pt' ? 'ESGOTADO' : 'SOLD OUT'}</span>`
        : isLowStock
          ? `<span class="tape-tag warning">${currentLang === 'pt' ? 'ÚLTIMAS UNIDADES' : 'LAST COPIES'}</span>`
          : `<span class="tape-tag blood">${currentLang === 'pt' ? 'EM ESTOQUE' : 'IN STOCK'}</span>`;

      const buyBtn = !isSoldOut
        ? `<button class="btn-brutal btn-blood btn-sm" onclick="ArmamentoBelico.addToCart('${escapeHtml(zine.id)}')">
             <span class="inverted-cross">✝</span> ${currentLang === 'pt' ? 'Comprar Cópia' : 'Buy Copy'}
           </button>`
        : `<button class="btn-sold-out" disabled>${currentLang === 'pt' ? 'Esgotado' : 'Sold Out'}</button>`;

      const safeTitle = escapeHtml(zine.title);
      const safeCover = escapeHtml(zine.cover);
      const safeDetailUrl = zine.detailUrl ? escapeHtml(zine.detailUrl) : '';
      const formattedPrice = formatCurrency(zine.price, zine.currency);

      const coverHtml = safeDetailUrl
        ? `<a href="${safeDetailUrl}" class="product-cover-link" aria-label="${currentLang === 'pt' ? `Ver detalhes de ${safeTitle}` : `View details for ${safeTitle}`}">
             <img src="${safeCover}" alt="${safeTitle}" class="xerox-img" loading="lazy">
           </a>`
        : `<img src="${safeCover}" alt="${safeTitle}" class="xerox-img" loading="lazy">`;

      const titleHtml = safeDetailUrl
        ? `<h3 class="product-title pirata-title"><a href="${safeDetailUrl}">${safeTitle}</a></h3>`
        : `<h3 class="product-title pirata-title">${safeTitle}</h3>`;

      const detailsLinkHtml = safeDetailUrl
        ? `<a href="${safeDetailUrl}" class="product-details-link">
             ${currentLang === 'pt' ? 'Ver detalhes da edição' : 'View issue details'} <i class="fa-solid fa-arrow-right" aria-hidden="true"></i>
           </a>`
        : '';

      const bandsHtml = zine.bands
        ? `<p class="product-bands-list"><strong>${currentLang === 'pt' ? 'Destaques:' : 'Featuring:'}</strong> ${escapeHtml(zine.bands)}</p>`
        : '';

      html += `
        <article class="product-card" data-stock="${isSoldOut ? 'soldout' : 'instock'}">
          <div class="product-badge-wrap">
            ${stockBadge}
          </div>
          <div class="product-cover-wrap">
            ${coverHtml}
          </div>
          <div class="product-info">
            <span class="product-edition-number">${escapeHtml(zine.date || '')}</span>
            ${titleHtml}
            ${bandsHtml}
            ${detailsLinkHtml}
            <div class="product-price-bar">
              <span class="product-price">${escapeHtml(formattedPrice)}</span>
              ${buyBtn}
            </div>
          </div>
        </article>
      `;
    });

    productsContainer.innerHTML = html;
    if (isArchivePage) {
      applyActiveArchiveFilter();
    }
  }

  function ensureCartSummaryElement(cartTotalVal) {
    let summaryEl = document.getElementById('cart-summary-breakdown');
    if (!summaryEl) {
      const totalLine = cartTotalVal.closest('.cart-total-line');
      if (totalLine && totalLine.parentNode) {
        summaryEl = document.createElement('div');
        summaryEl.id = 'cart-summary-breakdown';
        summaryEl.className = 'cart-summary-breakdown';
        totalLine.parentNode.insertBefore(summaryEl, totalLine);
      }
    }
    return summaryEl;
  }

  // --- RENDERIZAÇÃO DO CARRINHO ---
  function renderCart() {
    const cartItemsContainer = document.getElementById('cart-items-container');
    const cartTotalVal = document.getElementById('cart-total-val');
    const cartCheckoutArea = document.getElementById('cart-checkout-actions');

    if (!cartItemsContainer || !cartTotalVal || !cartCheckoutArea) return;

    const summaryEl = ensureCartSummaryElement(cartTotalVal);
    const isPtUi = STATE.lang === 'pt';

    if (STATE.cart.length === 0) {
      if (STATE.showOrderThankYou) {
        cartItemsContainer.innerHTML = `
          <div class="cart-thank-you-box" role="status" aria-live="polite">
            <p class="cart-thank-you-icon" aria-hidden="true">☠</p>
            <h4 class="cart-thank-you-title">${isPtUi ? 'Obrigado pelo seu pedido!' : 'Thank you for your order!'}</h4>
            <p class="cart-thank-you-text">
              ${isPtUi
                ? 'Seu apoio fortalece o Armamento Bélico Zine e o verdadeiro underground. Em breve entraremos em contato para confirmar os detalhes do envio.'
                : 'Your support keeps Armamento Bélico Zine and the true underground alive. We will follow up with your shipping details shortly.'}
            </p>
          </div>
        `;
      } else {
        cartItemsContainer.innerHTML = `
          <div class="cart-empty-message">
            <p style="font-size: 2.5rem; margin-bottom: 10px;">☠</p>
            <p>${isPtUi ? 'Seu caixão está vazio. Nenhum fanzine adicionado.' : 'Your coffin is empty. No zines summoned.'}</p>
          </div>
        `;
      }
      if (summaryEl) {
        summaryEl.hidden = true;
        summaryEl.innerHTML = '';
      }
      cartTotalVal.textContent = formatCurrency(0, isPtUi ? 'R$' : '€');
      cartCheckoutArea.innerHTML = '';
      return;
    }

    let itemsHtml = '';
    const currency = STATE.cart[0].currency === '€' ? '€' : 'R$';
    const isEurCart = currency === '€';

    STATE.cart.forEach(item => {
      const formattedPrice = formatCurrency(item.price, currency);
      const safeId = escapeHtml(item.id);
      const safeTitle = escapeHtml(item.title);
      const safeCover = escapeHtml(item.cover);

      itemsHtml += `
        <div class="cart-item-row">
          <div class="cart-item-thumb">
            <img src="${safeCover}" alt="${safeTitle}">
          </div>
          <div>
            <h4 class="cart-item-title">${safeTitle}</h4>
            <span class="cart-item-unit-price">${escapeHtml(formattedPrice)} ${isPtUi ? 'un' : 'ea'}</span>
            <div class="cart-qty-ctrl">
              <button class="qty-btn" onclick="ArmamentoBelico.updateQuantity('${safeId}', -1)">-</button>
              <span class="qty-number">${item.quantity}</span>
              <button class="qty-btn" onclick="ArmamentoBelico.updateQuantity('${safeId}', 1)">+</button>
            </div>
          </div>
          <button class="cart-item-remove-btn" title="${isPtUi ? 'Remover' : 'Remove'}" onclick="ArmamentoBelico.removeFromCart('${safeId}')">✕</button>
        </div>
      `;
    });

    const totalQty = getCartItemCount();
    const subtotal = getCartSubtotal();
    const shipping = calculateShipping(totalQty, currency);
    const paypalSummary = isEurCart ? calculatePayPalEurSummary(subtotal + shipping) : null;
    const total = isEurCart ? paypalSummary.total : roundHalfUp2(subtotal + shipping);

    if (summaryEl) {
      const itemUnitLabel = isPtUi
        ? (totalQty === 1 ? 'edição' : 'edições')
        : (totalQty === 1 ? 'issue' : 'issues');
      const subtotalLabel = `Subtotal (${totalQty} ${itemUnitLabel}):`;
      const shippingLabel = isEurCart
        ? (isPtUi ? 'Frete Internacional (Airmail):' : 'Shipping (Registered Airmail):')
        : (isPtUi ? 'Frete (Correios):' : 'Shipping (Brazil Correios):');
      const paypalFeeLineHtml = isEurCart
        ? `
          <div class="cart-summary-line cart-summary-line--fee">
            <span>${isPtUi ? 'Taxa PayPal (Internacional):' : 'PayPal Fee:'}</span>
            <strong id="cart-paypal-fee-val">${formatCurrency(paypalSummary.fee, currency)}</strong>
          </div>
        `
        : '';

      summaryEl.hidden = false;
      summaryEl.innerHTML = `
        <div class="cart-summary-line">
          <span>${subtotalLabel}</span>
          <strong id="cart-subtotal-val">${formatCurrency(subtotal, currency)}</strong>
        </div>
        <div class="cart-summary-line">
          <span>${shippingLabel}</span>
          <strong id="cart-shipping-val">${formatCurrency(shipping, currency)}</strong>
        </div>
        ${paypalFeeLineHtml}
      `;
    }

    cartItemsContainer.innerHTML = itemsHtml;
    cartTotalVal.textContent = formatCurrency(total, currency);

    if (STATE.pendingOrderConfirmation) {
      cartCheckoutArea.innerHTML = `
        <div class="cart-order-confirm-box" role="region" aria-live="polite" aria-label="${isPtUi ? 'Confirmação de Pedido' : 'Order Confirmation'}">
          <p class="cart-order-confirm-question">
            ${isPtUi ? 'O pedido foi realizado com sucesso?' : 'Was the order placed successfully?'}
          </p>
          <p class="cart-order-confirm-hint">
            ${isPtUi
              ? 'Se você já concluiu o envio do pedido, confirme abaixo para limpar o carrinho.'
              : 'If you completed your checkout, confirm below to clear your cart.'}
          </p>
          <div class="cart-order-confirm-actions">
            <button type="button" id="cart-confirm-yes-btn" class="btn-confirm-yes">
              <i class="fa-solid fa-check" aria-hidden="true"></i> ${isPtUi ? 'Sim' : 'Yes'}
            </button>
            <button type="button" id="cart-confirm-no-btn" class="btn-confirm-no">
              <i class="fa-solid fa-xmark" aria-hidden="true"></i> ${isPtUi ? 'Não' : 'No'}
            </button>
          </div>
        </div>
      `;

      const yesBtn = document.getElementById('cart-confirm-yes-btn');
      const noBtn = document.getElementById('cart-confirm-no-btn');
      if (yesBtn) yesBtn.addEventListener('click', confirmOrderSuccess);
      if (noBtn) noBtn.addEventListener('click', dismissOrderConfirmation);
      return;
    }

    // REGRA DE CHECKOUT GUIADA PELA MOEDA DO CARRINHO: R$ -> WhatsApp (Brasil) | € -> PayPal (Internacional)
    const returningLabel = isPtUi
      ? 'Já sou cliente (não preciso informar novamente o endereço)'
      : 'I am a returning customer (no need to enter my address again)';
    const legendText = isEurCart
      ? (isPtUi ? 'Endereço de Entrega Internacional' : 'International Shipping Address')
      : (isPtUi ? 'Endereço de Entrega' : 'Shipping Address (Brazil)');

    const labels = isPtUi
      ? {
          fullName: 'Nome Completo',
          fullNameErr: 'Informe seu nome completo.',
          country: 'País',
          countryErr: 'Informe o país de destino.',
          zip: isEurCart ? 'Código Postal / ZIP' : 'CEP',
          zipErr: isEurCart ? 'Informe o código postal.' : 'Informe o CEP.',
          street: 'Rua / Logradouro',
          streetErr: 'Informe a rua ou logradouro.',
          number: 'Número',
          numberErr: 'Informe o número.',
          complement: 'Complemento (opcional)',
          neighborhood: isEurCart ? 'Bairro / Distrito (opcional)' : 'Bairro',
          neighborhoodErr: 'Informe o bairro.',
          city: 'Cidade',
          cityErr: 'Informe a cidade.',
          state: isEurCart ? 'Estado / Província' : 'Estado (UF)',
          stateErr: 'Informe o estado.'
        }
      : {
          fullName: 'Full Name',
          fullNameErr: 'Enter your full name.',
          country: 'Country',
          countryErr: 'Enter your destination country.',
          zip: 'Postal Code / ZIP',
          zipErr: 'Enter your postal code.',
          street: 'Street Address',
          streetErr: 'Enter your street address.',
          number: 'Number',
          numberErr: 'Enter the street number.',
          complement: 'Apt / Suite (optional)',
          neighborhood: isEurCart ? 'District / Neighborhood (optional)' : 'District / Neighborhood',
          neighborhoodErr: 'Enter your district or neighborhood.',
          city: 'City',
          cityErr: 'Enter your city.',
          state: 'State / Province',
          stateErr: 'Enter your state or province.'
        };

    const reqAttr = STATE.isReturningCustomer ? '' : 'required';
    const neighborhoodReqAttr = (!STATE.isReturningCustomer && !isEurCart) ? 'required' : '';
    const hiddenDisabledAttr = STATE.isReturningCustomer ? 'hidden disabled' : '';

    const countryFieldHtml = isEurCart
      ? `
          <div class="cart-address-field">
            <label for="cart-addr-country">${labels.country} <span aria-hidden="true">*</span></label>
            <input type="text" id="cart-addr-country" name="country" class="form-control cart-address-input" autocomplete="country-name" maxlength="80" data-error="err-cart-country" data-addr-key="country" ${reqAttr}>
            <div id="err-cart-country" class="error-msg"><i class="fa-solid fa-circle-xmark" aria-hidden="true"></i> ${labels.countryErr}</div>
          </div>
        `
      : '';

    const paymentFieldsetHtml = !isEurCart
      ? `
        <fieldset class="cart-payment-fieldset" id="cart-payment-fieldset">
          <legend>${isPtUi ? 'Forma de Pagamento' : 'Payment Method'}</legend>
          <div class="cart-payment-options">
            <label class="cart-payment-option">
              <input type="radio" name="payment_method" value="pix" ${STATE.paymentMethod === 'pix' ? 'checked' : ''} required>
              <span class="cart-payment-card">
                <i class="fa-brands fa-pix cart-payment-icon" aria-hidden="true"></i>
                <span class="cart-payment-label">Pix</span>
              </span>
            </label>
            <label class="cart-payment-option">
              <input type="radio" name="payment_method" value="card" ${STATE.paymentMethod === 'card' ? 'checked' : ''}>
              <span class="cart-payment-card">
                <i class="fa-solid fa-credit-card cart-payment-icon" aria-hidden="true"></i>
                <span class="cart-payment-label">${isPtUi ? 'Cartão de Crédito' : 'Credit Card'}</span>
              </span>
            </label>
          </div>
        </fieldset>
      `
      : '';

    const submitBtnHtml = !isEurCart
      ? `
        <button type="submit" class="btn-whatsapp-checkout">
          <i class="fa-brands fa-whatsapp" style="font-size: 1.2rem;" aria-hidden="true"></i> ${isPtUi ? 'Finalizar Pedido via WhatsApp' : 'Order via WhatsApp (BRL)'}
        </button>
        <p class="cart-checkout-note">
          ${isPtUi
            ? 'Seu pedido será gerado e enviado diretamente para o editor via WhatsApp.'
            : 'Your Brazilian order will be sent directly to the editor via WhatsApp.'}
        </p>
      `
      : `
        <button type="submit" class="btn-paypal-checkout">
          <i class="fa-brands fa-paypal" style="font-size: 1.2rem;" aria-hidden="true"></i> ${isPtUi ? 'Pagar com PayPal (Euro • €)' : 'Checkout with PayPal (EUR • Worldwide)'}
        </button>
        <p class="cart-checkout-note">
          ${isPtUi
            ? 'Pagamento internacional em Euro (€) via PayPal com taxa inclusa. Envio registrado mundial.'
            : 'Safe international payment in Euro (€) via PayPal (fee included). Registered worldwide airmail.'}
        </p>
      `;

    cartCheckoutArea.innerHTML = `
      <form id="cart-checkout-form" class="cart-checkout-form" novalidate aria-label="${isPtUi ? 'Finalizar Pedido' : 'Order Checkout'}">
        <div class="checkbox-inline cart-returning-option">
          <input type="checkbox" id="cart-returning-customer" name="returning_customer" ${STATE.isReturningCustomer ? 'checked' : ''}>
          <label for="cart-returning-customer">${returningLabel}</label>
        </div>

        <fieldset id="cart-address-fieldset" class="cart-address-fieldset" ${hiddenDisabledAttr}>
          <legend>${legendText}</legend>

          <div class="cart-address-field">
            <label for="cart-addr-name">${labels.fullName} <span aria-hidden="true">*</span></label>
            <input type="text" id="cart-addr-name" name="full_name" class="form-control cart-address-input" autocomplete="name" maxlength="120" data-error="err-cart-name" data-addr-key="fullName" ${reqAttr}>
            <div id="err-cart-name" class="error-msg"><i class="fa-solid fa-circle-xmark" aria-hidden="true"></i> ${labels.fullNameErr}</div>
          </div>

          ${countryFieldHtml}

          <div class="cart-address-grid">
            <div class="cart-address-field">
              <label for="cart-addr-zip">${labels.zip} <span aria-hidden="true">*</span></label>
              <input type="text" id="cart-addr-zip" name="postal_code" class="form-control cart-address-input" inputmode="${isEurCart ? 'text' : 'numeric'}" autocomplete="postal-code" maxlength="20" data-error="err-cart-zip" data-addr-key="zip" ${reqAttr}>
              <div id="err-cart-zip" class="error-msg"><i class="fa-solid fa-circle-xmark" aria-hidden="true"></i> ${labels.zipErr}</div>
            </div>

            <div class="cart-address-field">
              <label for="cart-addr-number">${labels.number} <span aria-hidden="true">*</span></label>
              <input type="text" id="cart-addr-number" name="number" class="form-control cart-address-input" autocomplete="off" maxlength="20" data-error="err-cart-number" data-addr-key="number" ${reqAttr}>
              <div id="err-cart-number" class="error-msg"><i class="fa-solid fa-circle-xmark" aria-hidden="true"></i> ${labels.numberErr}</div>
            </div>
          </div>

          <div class="cart-address-field">
            <label for="cart-addr-street">${labels.street} <span aria-hidden="true">*</span></label>
            <input type="text" id="cart-addr-street" name="street" class="form-control cart-address-input" autocomplete="address-line1" maxlength="140" data-error="err-cart-street" data-addr-key="street" ${reqAttr}>
            <div id="err-cart-street" class="error-msg"><i class="fa-solid fa-circle-xmark" aria-hidden="true"></i> ${labels.streetErr}</div>
          </div>

          <div class="cart-address-field">
            <label for="cart-addr-complement">${labels.complement}</label>
            <input type="text" id="cart-addr-complement" name="complement" class="form-control cart-address-input" autocomplete="address-line2" maxlength="100" data-addr-key="complement">
          </div>

          <div class="cart-address-field">
            <label for="cart-addr-neighborhood">${labels.neighborhood}${isEurCart ? '' : ' <span aria-hidden="true">*</span>'}</label>
            <input type="text" id="cart-addr-neighborhood" name="neighborhood" class="form-control cart-address-input" autocomplete="address-level3" maxlength="80" ${isEurCart ? '' : 'data-error="err-cart-neighborhood"'} data-addr-key="neighborhood" ${neighborhoodReqAttr}>
            ${isEurCart ? '' : `<div id="err-cart-neighborhood" class="error-msg"><i class="fa-solid fa-circle-xmark" aria-hidden="true"></i> ${labels.neighborhoodErr}</div>`}
          </div>

          <div class="cart-address-grid">
            <div class="cart-address-field">
              <label for="cart-addr-city">${labels.city} <span aria-hidden="true">*</span></label>
              <input type="text" id="cart-addr-city" name="city" class="form-control cart-address-input" autocomplete="address-level2" maxlength="80" data-error="err-cart-city" data-addr-key="city" ${reqAttr}>
              <div id="err-cart-city" class="error-msg"><i class="fa-solid fa-circle-xmark" aria-hidden="true"></i> ${labels.cityErr}</div>
            </div>

            <div class="cart-address-field">
              <label for="cart-addr-state">${labels.state} <span aria-hidden="true">*</span></label>
              <input type="text" id="cart-addr-state" name="state" class="form-control cart-address-input" autocomplete="address-level1" maxlength="40" data-error="err-cart-state" data-addr-key="state" ${reqAttr}>
              <div id="err-cart-state" class="error-msg"><i class="fa-solid fa-circle-xmark" aria-hidden="true"></i> ${labels.stateErr}</div>
            </div>
          </div>
        </fieldset>

        ${paymentFieldsetHtml}

        ${submitBtnHtml}
      </form>
    `;

    const checkoutForm = document.getElementById('cart-checkout-form');
    const returningCheckbox = document.getElementById('cart-returning-customer');
    const addressFieldset = document.getElementById('cart-address-fieldset');
    const addressInputs = checkoutForm ? [...checkoutForm.querySelectorAll('[data-addr-key]')] : [];
    const paymentRadios = checkoutForm ? [...checkoutForm.querySelectorAll('input[name="payment_method"]')] : [];

    paymentRadios.forEach(radio => {
      radio.addEventListener('change', () => {
        if (radio.checked) {
          STATE.paymentMethod = radio.value === 'card' ? 'card' : 'pix';
          localStorage.setItem('ab_payment_method', STATE.paymentMethod);
        }
      });
    });

    addressInputs.forEach(input => {
      const key = input.dataset.addrKey;
      input.value = STATE.checkoutAddress[key] || '';
    });

    const isOptionalAddressKey = (key) => key === 'complement' || (isEurCart && key === 'neighborhood');

    const syncFieldWhitespace = (input) => {
      if (STATE.isReturningCustomer || isOptionalAddressKey(input.dataset.addrKey)) {
        input.setCustomValidity('');
        return;
      }
      if (input.value.length > 0 && input.value.trim().length === 0) {
        const errEl = document.getElementById(input.dataset.error || '');
        input.setCustomValidity(errEl ? errEl.textContent.trim() : 'Campo obrigatório.');
      } else {
        input.setCustomValidity('');
      }
    };

    if (checkoutForm) {
      setupAccessibleFormValidation(checkoutForm);

      if (returningCheckbox && addressFieldset) {
        returningCheckbox.addEventListener('change', () => {
          STATE.isReturningCustomer = returningCheckbox.checked;
          localStorage.setItem('ab_returning_customer', String(STATE.isReturningCustomer));
          addressFieldset.hidden = STATE.isReturningCustomer;
          addressFieldset.disabled = STATE.isReturningCustomer;

          addressInputs.forEach(input => {
            if (!isOptionalAddressKey(input.dataset.addrKey)) {
              input.required = !STATE.isReturningCustomer;
            }
            if (STATE.isReturningCustomer) {
              input.setCustomValidity('');
              input.classList.remove('user-invalid-fallback');
              input.setAttribute('aria-invalid', 'false');
            } else {
              syncFieldWhitespace(input);
            }
          });
        });
      }

      addressInputs.forEach(input => {
        input.addEventListener('input', () => {
          STATE.checkoutAddress[input.dataset.addrKey] = input.value;
          localStorage.setItem('ab_checkout_address_v2', JSON.stringify(STATE.checkoutAddress));
          syncFieldWhitespace(input);
        });
        input.addEventListener('blur', () => syncFieldWhitespace(input));
      });

      checkoutForm.addEventListener('submit', (e) => {
        e.preventDefault();
        addressInputs.forEach(syncFieldWhitespace);
        if (!checkoutForm.checkValidity()) {
          const firstInvalid = checkoutForm.querySelector(':invalid');
          if (firstInvalid) firstInvalid.focus();
          return;
        }
        if (isEurCart) {
          checkoutPayPal();
        } else {
          checkoutWhatsApp();
        }
      });
    }
  }

  function hasValidShippingAddress(isEurCart = false) {
    if (STATE.isReturningCustomer) return true;
    const a = STATE.checkoutAddress;
    const requiredValues = isEurCart
      ? [a.fullName, a.country, a.zip, a.street, a.number, a.city, a.state]
      : [a.fullName, a.zip, a.street, a.number, a.neighborhood, a.city, a.state];
    return requiredValues.every(val => typeof val === 'string' && val.trim().length > 0);
  }

  // --- CHECKOUT VIA WHATSAPP (VERSÃO PT / BRL) ---
  function checkoutWhatsApp() {
    if (STATE.cart.length === 0) return;

    if (!hasValidShippingAddress(false)) {
      const firstInvalid = document.querySelector('#cart-checkout-form :invalid');
      if (firstInvalid) firstInvalid.focus();
      return;
    }

    let message = `*👺 PEDIDO DE FANZINES - ARMAMENTO BÉLICO 👺*\n\n`;
    message += `Olá! Gostaria de encomendar os seguintes exemplares impressos:\n\n`;

    const currency = STATE.cart[0].currency || 'R$';
    let subtotal = 0;
    STATE.cart.forEach((item, index) => {
      const itemSubtotal = item.price * item.quantity;
      subtotal += itemSubtotal;
      message += `${index + 1}. *${item.title}*\n`;
      message += `   Qtd: ${item.quantity}x | Subtotal: ${formatCurrency(itemSubtotal, currency)}\n`;
    });

    const totalQty = getCartItemCount();
    const shipping = calculateShipping(totalQty, currency);
    const total = subtotal + shipping;

    message += `\n*SUBTOTAL: ${formatCurrency(subtotal, currency)}*\n`;
    message += `*FRETE (CORREIOS - ${totalQty} ${totalQty === 1 ? 'edição' : 'edições'}): ${formatCurrency(shipping, currency)}*\n`;
    message += `*TOTAL DO PEDIDO: ${formatCurrency(total, currency)}*\n`;
    if (STATE.isReturningCustomer) {
      message += `\n*ENDEREÇO DE ENTREGA:*\nJá sou cliente (endereço já informado anteriormente).\n`;
    } else {
      const a = STATE.checkoutAddress;
      message += `\n*ENDEREÇO DE ENTREGA:*\n`;
      message += `• *Destinatário:* ${a.fullName.trim()}\n`;
      message += `• *Rua:* ${a.street.trim()}, nº ${a.number.trim()}\n`;
      if (a.complement.trim()) {
        message += `• *Complemento:* ${a.complement.trim()}\n`;
      }
      message += `• *Bairro:* ${a.neighborhood.trim()}\n`;
      message += `• *Cidade/UF:* ${a.city.trim()} - ${a.state.trim()}\n`;
      message += `• *CEP:* ${a.zip.trim()}\n`;
    }
    const isCard = STATE.paymentMethod === 'card';
    const paymentLabel = isCard ? 'Cartão de Crédito' : 'Pix';
    message += `\n*FORMA DE PAGAMENTO:* ${paymentLabel}\n`;
    message += isCard
      ? `\nPor favor, envie o link para pagamento via Cartão de Crédito!\n`
      : `\nPor favor, informe a chave PIX para pagamento!\n`;
    message += `Obrigado pelo suporte ao Metal subterrâneo!`;

    const encodedMessage = encodeURIComponent(message);
    const whatsappUrl = `https://wa.me/${STORE_CONFIG.whatsappNumber}?text=${encodedMessage}`;
    setPendingOrderConfirmation(true);
    renderCart();
    window.open(whatsappUrl, '_blank');
  }

  // --- CHECKOUT VIA PAYPAL (VERSÃO EN / EUR COM TAXA GROSS-UP) ---
  function checkoutPayPal() {
    if (STATE.cart.length === 0) return;

    if (!hasValidShippingAddress(true)) {
      const firstInvalid = document.querySelector('#cart-checkout-form :invalid');
      if (firstInvalid) firstInvalid.focus();
      return;
    }

    const currency = '€';
    const subtotal = getCartSubtotal();
    const shipping = calculateShipping(getCartItemCount(), currency);
    const paypalSummary = calculatePayPalEurSummary(subtotal + shipping);
    const totalStr = paypalSummary.total.toFixed(2);
    const itemsDescription = STATE.cart.map(i => `${i.quantity}x ${i.title}`).join(', ');
    const a = STATE.checkoutAddress;

    const addressParts = STATE.isReturningCustomer
      ? []
      : [
          a.fullName.trim(),
          `${a.street.trim()}, ${a.number.trim()}${a.complement.trim() ? ' (' + a.complement.trim() + ')' : ''}`,
          a.neighborhood.trim(),
          `${a.city.trim()} - ${a.state.trim()}`,
          a.zip.trim(),
          a.country.trim()
        ].filter(Boolean);

    const addressSummary = STATE.isReturningCustomer
      ? (STATE.lang === 'pt' ? 'Cliente recorrente (endereço já cadastrado)' : 'Returning customer (address on file)')
      : addressParts.join(', ');

    const paypalParams = new URLSearchParams({
      cmd: '_xclick',
      business: STORE_CONFIG.paypal.email,
      item_name: `Armamento Belico Zine - ${itemsDescription}`.slice(0, 127),
      amount: totalStr,
      currency_code: 'EUR',
      no_shipping: '2',
      custom: `${itemsDescription} | ${addressSummary}`.slice(0, 255)
    });

    if (!STATE.isReturningCustomer) {
      paypalParams.set('first_name', a.fullName.trim().slice(0, 32));
      paypalParams.set('address1', `${a.street.trim()}, ${a.number.trim()}`.slice(0, 100));
      const addr2 = [a.complement.trim(), a.neighborhood.trim()].filter(Boolean).join(' - ');
      if (addr2) paypalParams.set('address2', addr2.slice(0, 100));
      paypalParams.set('city', a.city.trim().slice(0, 40));
      paypalParams.set('state', a.state.trim().slice(0, 40));
      paypalParams.set('zip', a.zip.trim().slice(0, 32));
    }

    const paypalUrl = `https://www.paypal.com/cgi-bin/webscr?${paypalParams.toString()}`;
    const dialog = document.getElementById('paypal-checkout-confirm-dialog');

    if (!dialog || typeof dialog.showModal !== 'function') {
      setPendingOrderConfirmation(true);
      renderCart();
      window.open(paypalUrl, '_blank');
      showToast(STATE.lang === 'pt' ? 'Redirecionando para o PayPal...' : 'Redirecting to PayPal secure gateway...');
      return;
    }

    pendingPayPalCheckoutUrl = paypalUrl;
    const isPtUi = STATE.lang === 'pt';
    const titleEl = document.getElementById('paypal-confirm-title');
    const summaryEl = document.getElementById('paypal-confirm-summary');
    const recipientEl = document.getElementById('paypal-confirm-recipient');
    const cancelBtn = document.getElementById('paypal-confirm-cancel-btn');
    const proceedLabel = document.getElementById('paypal-confirm-proceed-label');

    if (titleEl) {
      titleEl.textContent = isPtUi ? 'Confirmar Pedido Internacional (PayPal)' : 'Confirm International PayPal Order';
    }
    if (summaryEl) {
      summaryEl.innerHTML = `
        <div class="paypal-summary-row">
          <span>${isPtUi ? 'Itens:' : 'Items:'}</span>
          <strong>${escapeHtml(itemsDescription)}</strong>
        </div>
        <div class="paypal-summary-row">
          <span>Subtotal:</span>
          <strong>${escapeHtml(formatCurrency(subtotal, currency))}</strong>
        </div>
        <div class="paypal-summary-row">
          <span>${isPtUi ? 'Frete (Registered Airmail):' : 'Shipping (Registered Airmail):'}</span>
          <strong>${escapeHtml(formatCurrency(shipping, currency))}</strong>
        </div>
        <div class="paypal-summary-row">
          <span>${isPtUi ? 'Taxa PayPal:' : 'PayPal Fee:'}</span>
          <strong>${escapeHtml(formatCurrency(paypalSummary.fee, currency))}</strong>
        </div>
        <div class="paypal-summary-row paypal-summary-row--total">
          <span>${isPtUi ? 'Total em Euro:' : 'Total (EUR):'}</span>
          <strong>${escapeHtml(formatCurrency(paypalSummary.total, currency))}</strong>
        </div>
        <div class="paypal-summary-address">
          <strong>${isPtUi ? 'Entrega:' : 'Shipping to:'}</strong> ${escapeHtml(addressSummary)}
        </div>
      `;
    }
    if (recipientEl) {
      recipientEl.textContent = isPtUi
        ? `Conta PayPal recebedora: ${STORE_CONFIG.paypal.email} (Moeda: EUR)`
        : `PayPal recipient: ${STORE_CONFIG.paypal.email} (Currency: EUR)`;
    }
    if (cancelBtn) {
      cancelBtn.textContent = isPtUi ? 'Cancelar' : 'Cancel';
    }
    if (proceedLabel) {
      proceedLabel.textContent = isPtUi ? 'Ir para o PayPal' : 'Proceed to PayPal';
    }

    dialog.showModal();
  }

  function initStoreModals() {
    const conflictDialog = document.getElementById('cart-currency-conflict-dialog');
    const conflictClose = document.getElementById('cart-conflict-close-btn');
    const conflictCancel = document.getElementById('cart-conflict-cancel-btn');
    const conflictReplace = document.getElementById('cart-conflict-replace-btn');

    const closeConflict = () => {
      pendingCartReplacementProduct = null;
      if (conflictDialog?.open) conflictDialog.close();
    };
    if (conflictClose) conflictClose.addEventListener('click', closeConflict);
    if (conflictCancel) conflictCancel.addEventListener('click', closeConflict);
    if (conflictReplace) {
      conflictReplace.addEventListener('click', () => {
        const nextProduct = pendingCartReplacementProduct;
        pendingCartReplacementProduct = null;
        if (conflictDialog?.open) conflictDialog.close();
        if (nextProduct) {
          STATE.cart = [];
          pushProductToCart(nextProduct);
        }
      });
    }

    const paypalDialog = document.getElementById('paypal-checkout-confirm-dialog');
    const paypalClose = document.getElementById('paypal-confirm-close-btn');
    const paypalCancel = document.getElementById('paypal-confirm-cancel-btn');
    const paypalProceed = document.getElementById('paypal-confirm-proceed-btn');

    const closePayPal = () => {
      pendingPayPalCheckoutUrl = null;
      if (paypalDialog?.open) paypalDialog.close();
    };
    if (paypalClose) paypalClose.addEventListener('click', closePayPal);
    if (paypalCancel) paypalCancel.addEventListener('click', closePayPal);
    if (paypalProceed) {
      paypalProceed.addEventListener('click', () => {
        const targetUrl = pendingPayPalCheckoutUrl;
        pendingPayPalCheckoutUrl = null;
        if (paypalDialog?.open) paypalDialog.close();
        if (targetUrl) {
          setPendingOrderConfirmation(true);
          renderCart();
          window.open(targetUrl, '_blank');
          showToast(STATE.lang === 'pt' ? 'Redirecionando para o PayPal...' : 'Redirecting to PayPal secure gateway...');
        }
      });
    }
  }

  // --- TOAST DE NOTIFICAÇÃO ---
  function showToast(message) {
    let toast = document.getElementById('toast-notice');
    if (!toast) {
      toast = document.createElement('div');
      toast.id = 'toast-notice';
      toast.className = 'toast-notice';
      document.body.appendChild(toast);
    }
    toast.textContent = message;
    toast.classList.add('show');
    setTimeout(() => {
      toast.classList.remove('show');
    }, 3200);
  }

  // --- CONTROLES DO MODAL / DRAWER DO CARRINHO ---
  function openCartDrawer() {
    const drawer = document.getElementById('cart-drawer');
    const overlay = document.getElementById('cart-overlay');
    if (drawer && overlay) {
      drawer.classList.add('open');
      overlay.classList.add('open');
      renderCart();
    }
  }

  function closeCartDrawer() {
    const drawer = document.getElementById('cart-drawer');
    const overlay = document.getElementById('cart-overlay');
    if (drawer && overlay) {
      drawer.classList.remove('open');
      overlay.classList.remove('open');
    }
    if (STATE.showOrderThankYou) {
      STATE.showOrderThankYou = false;
    }
  }

  // --- FILTROS DA PÁGINA ARQUIVO ---
  function setupArchiveFilters() {
    const filterButtons = document.querySelectorAll('.filter-btn');
    if (!filterButtons.length) return;

    filterButtons.forEach(btn => {
      btn.addEventListener('click', () => {
        filterButtons.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');

        const filter = btn.getAttribute('data-filter');
        const cards = document.querySelectorAll('.product-card');

        cards.forEach(card => {
          const stock = card.getAttribute('data-stock');
          if (filter === 'all') {
            card.style.display = '';
          } else if (filter === 'instock' && stock === 'instock') {
            card.style.display = '';
          } else if (filter === 'soldout' && stock === 'soldout') {
            card.style.display = '';
          } else {
            card.style.display = 'none';
          }
        });
      });
    });
  }

  // --- BASE DE EVENTOS APROVADOS (DEMONSTRAÇÃO) ---
  const EVENTS_CATALOG = [
    {
      id: 'ev-1',
      title: 'Necro Ritual Fest VI - Noite de Blasfêmia',
      flyer: 'assets/images/capa-edicao-58.png',
      city: 'São Paulo',
      state: 'SP',
      date: '2026-12-12',
      time: '20:00',
      organizerContact: '(11) 99123-4567 / @necroritualprod',
      publisherWhatsapp: '(11) 99123-4567',
      publisherRole: 'organizer'
    },
    {
      id: 'ev-2',
      title: 'Subterrâneo Profano: Death & Black Metal',
      flyer: 'assets/images/capa-edicao-57.jpg',
      city: 'Belo Horizonte',
      state: 'MG',
      date: '2026-11-28',
      time: '19:30',
      organizerContact: 'contato@subterraneoprofano.com.br',
      publisherWhatsapp: '(31) 98444-1122',
      publisherRole: 'helper'
    },
    {
      id: 'ev-3',
      title: 'Massacre Sonoro Sul-Americano (Beneficente Cena Underground)',
      flyer: 'assets/images/capa-edicao-56.jpg',
      city: 'Curitiba',
      state: 'PR',
      date: '2027-01-16',
      time: '18:00',
      organizerContact: '(41) 99777-3300',
      publisherWhatsapp: '(41) 99777-3300',
      publisherRole: 'organizer'
    },
    {
      id: 'ev-4',
      title: 'Culto das Catacumbas - Edição V',
      flyer: 'assets/images/capa-edicao-55.jpg',
      city: 'Rio de Janeiro',
      state: 'RJ',
      date: '2026-10-18',
      time: '21:00',
      organizerContact: '(21) 98888-2211',
      publisherWhatsapp: '(21) 97777-9900',
      publisherRole: 'helper'
    },
    {
      id: 'ev-5',
      title: 'Morte & Podridão Open Air',
      flyer: 'assets/images/capa-edicao-56.jpg',
      city: 'Porto Alegre',
      state: 'RS',
      date: '2026-09-05',
      time: '17:00',
      organizerContact: '@mortepodridaprod',
      publisherWhatsapp: '(51) 99333-4455',
      publisherRole: 'organizer'
    }
  ];

  // --- PÁGINA EVENTOS: TEMPORAL, FILTROS, FLYER MODAL E VALIDAÇÃO ACESSÍVEL ---
  function getTodayIsoCalendarDate() {
    if (typeof Temporal !== 'undefined' && Temporal.Now) {
      return Temporal.Now.plainDateISO().toString();
    }
    const now = new Date();
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, '0');
    const d = String(now.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  function isEventPast(eventDateStr, todayStr) {
    if (typeof Temporal !== 'undefined' && Temporal.PlainDate) {
      const evDate = Temporal.PlainDate.from(eventDateStr);
      const todayDate = Temporal.PlainDate.from(todayStr);
      return Temporal.PlainDate.compare(evDate, todayDate) < 0;
    }
    return eventDateStr < todayStr;
  }

  function formatDisplayDate(dateStr) {
    if (typeof Temporal !== 'undefined' && Temporal.PlainDate) {
      const d = Temporal.PlainDate.from(dateStr);
      return `${String(d.day).padStart(2, '0')}/${String(d.month).padStart(2, '0')}/${d.year}`;
    }
    const [y, m, d] = dateStr.split('-');
    return `${d}/${m}/${y}`;
  }

  function escapeHtml(str) {
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function renderEventsList() {
    const grid = document.getElementById('events-grid');
    if (!grid) return;

    const stateFilter = document.getElementById('event-state-filter')?.value || 'all';
    const statusFilter = document.getElementById('event-status-filter')?.value || 'all';
    const countEl = document.getElementById('events-count-indicator');
    const todayStr = getTodayIsoCalendarDate();

    const enriched = EVENTS_CATALOG.map(ev => ({
      ...ev,
      isPast: isEventPast(ev.date, todayStr)
    }));

    // Ordenação: eventos que ainda vão acontecer primeiro (data mais próxima -> mais distante),
    // depois eventos que já aconteceram (mais recente -> mais antigo).
    enriched.sort((a, b) => {
      if (a.isPast !== b.isPast) return a.isPast ? 1 : -1;
      if (!a.isPast) return a.date.localeCompare(b.date);
      return b.date.localeCompare(a.date);
    });

    const filtered = enriched.filter(ev => {
      if (stateFilter !== 'all' && ev.state !== stateFilter) return false;
      if (statusFilter === 'upcoming' && ev.isPast) return false;
      if (statusFilter === 'past' && !ev.isPast) return false;
      return true;
    });

    if (countEl) {
      countEl.textContent = filtered.length === 1
        ? '1 evento encontrado'
        : `${filtered.length} eventos encontrados`;
    }

    if (filtered.length === 0) {
      grid.innerHTML = `
        <div class="events-empty-state">
          <p>Nenhum evento aprovado encontrado para os filtros selecionados.</p>
        </div>
      `;
      return;
    }

    grid.innerHTML = filtered.map(ev => {
      const statusLabel = ev.isPast ? 'Já aconteceu' : 'Vai acontecer';
      const statusClass = ev.isPast ? 'past' : 'upcoming';
      const roleLabel = ev.publisherRole === 'organizer'
        ? 'Organizador do evento'
        : 'Apenas ajudando na divulgação';

      return `
        <article class="event-card ${ev.isPast ? 'is-past' : ''}">
          <div class="event-flyer-wrap">
            <span class="event-status-badge ${statusClass}">${statusLabel}</span>
            <button
              type="button"
              class="event-flyer-btn"
              data-flyer-src="${escapeHtml(ev.flyer)}"
              data-flyer-title="${escapeHtml(ev.title)}"
              aria-label="Ampliar flyer de ${escapeHtml(ev.title)}"
            >
              <img src="${escapeHtml(ev.flyer)}" alt="Flyer do evento ${escapeHtml(ev.title)}" class="xerox-img" loading="lazy">
            </button>
          </div>
          <div class="event-card-body">
            <h3 class="pirata-title event-card-title">${escapeHtml(ev.title)}</h3>
            <ul class="event-meta-list">
              <li>
                <span class="event-meta-label">Local:</span>
                <span>${escapeHtml(ev.city)} / <strong>${escapeHtml(ev.state)}</strong></span>
              </li>
              <li>
                <span class="event-meta-label">Data:</span>
                <span>${formatDisplayDate(ev.date)}</span>
              </li>
              <li>
                <span class="event-meta-label">Horário:</span>
                <span>${escapeHtml(ev.time)}</span>
              </li>
              <li>
                <span class="event-meta-label">Organizador:</span>
                <span>${escapeHtml(ev.organizerContact)}</span>
              </li>
            </ul>
            <div class="event-publisher-info">
              <span><strong>WhatsApp de quem publicou:</strong> ${escapeHtml(ev.publisherWhatsapp)}</span>
              <span class="event-role-tag">${roleLabel}</span>
            </div>
          </div>
        </article>
      `;
    }).join('');
  }

  function setupAccessibleFormValidation(form) {
    if (!form) return;

    const linkError = (el, shown) => {
      const errorId = el.dataset.error;
      if (!errorId) return;
      const ids = (el.getAttribute('aria-describedby') || '').split(/\s+/).filter(id => id && id !== errorId);
      if (shown) ids.push(errorId);
      if (ids.length) el.setAttribute('aria-describedby', ids.join(' '));
      else el.removeAttribute('aria-describedby');
    };

    let announcer = document.getElementById('ab-form-announcer');
    if (!announcer) {
      announcer = document.createElement('div');
      announcer.id = 'ab-form-announcer';
      announcer.setAttribute('aria-live', 'polite');
      announcer.classList.add('visually-hidden');
      document.body.append(announcer);
    }
    let announceTimer;
    let announcedField;

    const spokenText = (node) => {
      const copy = node.cloneNode(true);
      copy.querySelectorAll('[aria-hidden="true"]').forEach(n => n.remove());
      copy.querySelectorAll('[role="img"][aria-label]').forEach(n => n.replaceWith(`${n.getAttribute('aria-label')}: `));
      return copy.textContent.replace(/\s+/g, ' ').trim();
    };

    const announceError = (el) => {
      const error = document.getElementById(el.dataset.error);
      if (!error) return;
      const label = el.labels?.[0] ? `${spokenText(el.labels[0])}: ` : '';
      clearTimeout(announceTimer);
      announcer.textContent = '';
      announcedField = el;
      announceTimer = setTimeout(() => {
        announcer.textContent = label + spokenText(error);
      }, 500);
    };

    const supportsUserInvalid = typeof CSS !== 'undefined' && CSS.supports('selector(:user-invalid)');
    const flagged = new WeakSet();
    const announced = new WeakSet();
    const dirtyState = new WeakMap();

    const syncAria = (el, announce = false) => {
      if (!el.matches?.('input, select, textarea')) return;
      const invalid = supportsUserInvalid
        ? el.matches(':user-invalid')
        : (dirtyState.get(el)?.hasInteracted && dirtyState.get(el)?.hasBlurred && !el.validity.valid);

      if (!supportsUserInvalid && dirtyState.get(el)?.hasBlurred) {
        el.classList.toggle('user-invalid-fallback', !el.validity.valid);
      }

      if (invalid) {
        if (announce && !announced.has(el)) {
          announceError(el);
          announced.add(el);
        }
        el.setAttribute('aria-invalid', 'true');
        linkError(el, true);
        flagged.add(el);
      } else if (flagged.has(el) || el.getAttribute('aria-invalid') !== 'true') {
        el.setAttribute('aria-invalid', 'false');
        linkError(el, false);
        flagged.delete(el);
        announced.delete(el);
        if (announcedField === el) {
          clearTimeout(announceTimer);
          announcer.textContent = '';
        }
      }
    };

    form.querySelectorAll('input, select, textarea').forEach(el => syncAria(el));

    form.addEventListener('blur', (e) => {
      const st = dirtyState.get(e.target) || { hasInteracted: false, hasBlurred: false };
      st.hasBlurred = true;
      dirtyState.set(e.target, st);
      syncAria(e.target, true);
    }, true);

    form.addEventListener('input', (e) => {
      const st = dirtyState.get(e.target) || { hasInteracted: false, hasBlurred: false };
      st.hasInteracted = true;
      dirtyState.set(e.target, st);
      if (e.target.hasAttribute('aria-invalid')) syncAria(e.target);
    });

    form.addEventListener('change', (e) => {
      const st = dirtyState.get(e.target) || { hasInteracted: false, hasBlurred: false };
      st.hasInteracted = true;
      dirtyState.set(e.target, st);
      syncAria(e.target, true);
    }, true);

    form.addEventListener('invalid', (e) => {
      dirtyState.set(e.target, { hasInteracted: true, hasBlurred: true });
      announced.add(e.target);
      syncAria(e.target);
    }, true);

    form.addEventListener('reset', () => {
      setTimeout(() => {
        [...form.elements].forEach(el => {
          dirtyState.delete(el);
          el.classList.remove('user-invalid-fallback');
          syncAria(el);
        });
      });
    }, true);
  }

  function initEventsPage() {
    if (!document.body.classList.contains('page-eventos')) return;

    // Carrega polyfill do Temporal para navegadores sem suporte nativo (ex: Safari)
    if (typeof Temporal === 'undefined') {
      import('https://esm.sh/temporal-polyfill')
        .then(mod => {
          globalThis.Temporal = mod.Temporal;
          renderEventsList();
        })
        .catch(() => {
          // Fallback silencioso caso esteja offline
        });
    }

    renderEventsList();

    const stateFilter = document.getElementById('event-state-filter');
    const statusFilter = document.getElementById('event-status-filter');
    const filterForm = document.getElementById('events-filter-form');

    if (stateFilter) stateFilter.addEventListener('change', renderEventsList);
    if (statusFilter) statusFilter.addEventListener('change', renderEventsList);
    if (filterForm) filterForm.addEventListener('submit', e => { e.preventDefault(); renderEventsList(); });

    // Lightbox do Flyer via <dialog>
    const flyerDialog = document.getElementById('flyer-dialog');
    const flyerDialogImg = document.getElementById('flyer-dialog-img');
    const flyerDialogTitle = document.getElementById('flyer-dialog-title');
    const flyerDialogClose = document.getElementById('flyer-dialog-close');
    const eventsGrid = document.getElementById('events-grid');

    if (eventsGrid && flyerDialog && flyerDialogImg && flyerDialogTitle) {
      eventsGrid.addEventListener('click', (e) => {
        const btn = e.target.closest('.event-flyer-btn');
        if (!btn) return;
        flyerDialogImg.src = btn.dataset.flyerSrc || '';
        flyerDialogImg.alt = `Flyer ampliado: ${btn.dataset.flyerTitle || ''}`;
        flyerDialogTitle.textContent = btn.dataset.flyerTitle || 'Flyer do Evento';
        if (typeof flyerDialog.showModal === 'function') {
          flyerDialog.showModal();
        }
      });
    }

    if (flyerDialogClose && flyerDialog) {
      flyerDialogClose.addEventListener('click', () => flyerDialog.close());
      flyerDialog.addEventListener('click', (e) => {
        if (e.target === flyerDialog) flyerDialog.close();
      });
    }

    // Preview do flyer no formulário
    const flyerInput = document.getElementById('ev-flyer');
    const previewWrap = document.getElementById('ev-flyer-preview-wrap');
    const previewImg = document.getElementById('ev-flyer-preview');

    if (flyerInput && previewWrap && previewImg) {
      flyerInput.addEventListener('change', () => {
        const file = flyerInput.files?.[0];
        if (!file) {
          previewWrap.hidden = true;
          previewImg.src = '';
          return;
        }
        const reader = new FileReader();
        reader.onload = () => {
          previewImg.src = reader.result;
          previewWrap.hidden = false;
        };
        reader.readAsDataURL(file);
      });
    }

    // Validação acessível e envio do formulário
    const eventForm = document.getElementById('event-submit-form');
    const feedbackEl = document.getElementById('event-submit-feedback');

    if (eventForm) {
      setupAccessibleFormValidation(eventForm);

      eventForm.addEventListener('submit', (e) => {
        e.preventDefault();
        if (!eventForm.checkValidity()) {
          const firstInvalid = eventForm.querySelector(':invalid');
          if (firstInvalid) firstInvalid.focus();
          return;
        }

        const formData = new FormData(eventForm);
        const title = formData.get('title');
        const city = formData.get('city');
        const state = formData.get('state');

        if (feedbackEl) {
          feedbackEl.hidden = false;
          feedbackEl.textContent = `☠ Evento "${title}" (${city}/${state}) enviado com sucesso! Ele está aguardando análise e aprovação manual do editor antes de ser publicado na agenda.`;
          feedbackEl.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }

        eventForm.reset();
        if (previewWrap && previewImg) {
          previewWrap.hidden = true;
          previewImg.src = '';
        }
      });
    }
  }

  // --- ETAPA 5: PÁGINA INDIQUE UMA HORDA (indique.html) ---
  const BAND_INDICATIONS_STORAGE_KEY = 'ab_band_indications_v1';
  const BAND_INDICATIONS_SEED = [
    {
      name: 'Diabolizer',
      country: 'Turquia',
      region: 'Istambul',
      releaseTitle: 'Khalkedonian Death (CD / Vinil)',
      listenUrl: 'https://diabolizer.bandcamp.com'
    },
    {
      name: 'Coldwinter',
      country: 'Brasil',
      region: 'SC',
      releaseTitle: 'The Winter Has Come (Fita K7)',
      listenUrl: 'https://bandcamp.com'
    },
    {
      name: 'Mortem Solis',
      country: 'Brasil',
      region: 'SP',
      releaseTitle: 'Necro Rites of Desecration (CD)',
      listenUrl: 'https://bandcamp.com'
    }
  ];

  function normalizeBandKeyPart(value) {
    return String(value || '')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim()
      .toLowerCase()
      .replace(/\s+/g, ' ');
  }

  function loadBandIndications() {
    try {
      const saved = JSON.parse(localStorage.getItem(BAND_INDICATIONS_STORAGE_KEY) || 'null');
      if (Array.isArray(saved) && saved.length > 0) {
        return saved;
      }
    } catch (_) {
      // Fallback para lista inicial
    }
    return [...BAND_INDICATIONS_SEED];
  }

  function saveBandIndications(list) {
    try {
      localStorage.setItem(BAND_INDICATIONS_STORAGE_KEY, JSON.stringify(list));
    } catch (_) {
      // Ignora erro de quota do localStorage
    }
  }

  function isDuplicateBandIndication(list, bandName, country) {
    const normName = normalizeBandKeyPart(bandName);
    const normCountry = normalizeBandKeyPart(country);
    return list.some(
      item =>
        normalizeBandKeyPart(item.name) === normName &&
        normalizeBandKeyPart(item.country) === normCountry
    );
  }

  function isSafeHttpUrl(urlStr) {
    try {
      const parsed = new URL(urlStr);
      return parsed.protocol === 'http:' || parsed.protocol === 'https:';
    } catch (_) {
      return false;
    }
  }

  function renderIndicatedBands(list) {
    const grid = document.getElementById('indicated-bands-grid');
    const countEl = document.getElementById('indicated-bands-count');
    if (!grid) return;

    if (countEl) {
      countEl.textContent =
        list.length === 1
          ? '1 horda registrada para avaliação do zine.'
          : `${list.length} hordas registradas para avaliação do zine.`;
    }

    const fragment = document.createDocumentFragment();
    list.forEach(band => {
      const card = document.createElement('article');
      card.className = 'indicated-band-card';

      const header = document.createElement('div');
      header.className = 'indicated-band-header';

      const title = document.createElement('h3');
      title.className = 'indicated-band-name pirata-title';
      title.textContent = band.name;

      const badge = document.createElement('span');
      badge.className = 'tape-tag dark';
      badge.textContent = band.country;

      header.append(title, badge);

      const origin = document.createElement('p');
      origin.className = 'indicated-band-origin';
      origin.textContent = band.region ? `${band.country} • ${band.region}` : band.country;

      const release = document.createElement('p');
      release.className = 'indicated-band-release';
      const releaseStrong = document.createElement('strong');
      releaseStrong.textContent = 'Material físico: ';
      release.append(releaseStrong, document.createTextNode(band.releaseTitle));

      card.append(header, origin, release);

      if (isSafeHttpUrl(band.listenUrl)) {
        const link = document.createElement('a');
        link.className = 'indicated-band-link';
        link.href = band.listenUrl;
        link.target = '_blank';
        link.rel = 'noopener noreferrer';
        const icon = document.createElement('i');
        icon.className = 'fa-solid fa-headphones';
        icon.setAttribute('aria-hidden', 'true');
        link.append(icon, document.createTextNode(' Ouvir Horda'));
        card.append(link);
      }

      fragment.append(card);
    });

    grid.replaceChildren(fragment);
  }

  function initIndiquePage() {
    if (!document.body.classList.contains('page-indique')) return;

    let indications = loadBandIndications();
    renderIndicatedBands(indications);

    const form = document.getElementById('band-suggest-form');
    const feedbackEl = document.getElementById('band-submit-feedback');
    const nameInput = document.getElementById('band-name');
    const countryInput = document.getElementById('band-country');

    if (!form) return;

    setupAccessibleFormValidation(form);

    const clearDuplicateFeedback = () => {
      if (feedbackEl && feedbackEl.classList.contains('is-error')) {
        feedbackEl.hidden = true;
        feedbackEl.classList.remove('is-error');
        feedbackEl.textContent = '';
      }
    };

    if (nameInput) nameInput.addEventListener('input', clearDuplicateFeedback);
    if (countryInput) countryInput.addEventListener('input', clearDuplicateFeedback);

    form.addEventListener('submit', (e) => {
      e.preventDefault();

      if (!form.checkValidity()) {
        const firstInvalid = form.querySelector(':invalid');
        if (firstInvalid) firstInvalid.focus();
        return;
      }

      const formData = new FormData(form);
      const bandName = String(formData.get('band_name') || '').trim();
      const country = String(formData.get('country') || '').trim();
      const region = String(formData.get('region') || '').trim();
      const releaseTitle = String(formData.get('release_title') || '').trim();
      const listenUrl = String(formData.get('listen_url') || '').trim();

      indications = loadBandIndications();

      if (isDuplicateBandIndication(indications, bandName, country)) {
        if (feedbackEl) {
          feedbackEl.hidden = false;
          feedbackEl.classList.add('is-error');
          feedbackEl.setAttribute('role', 'alert');
          feedbackEl.textContent = `❌ Essa banda já foi indicada! Já existe um registro para "${bandName}" (${country}) no Armamento Bélico Zine.`;
          feedbackEl.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }
        if (nameInput) {
          nameInput.focus();
        }
        return;
      }

      const newIndication = {
        name: bandName,
        country,
        region,
        releaseTitle,
        listenUrl
      };

      indications.unshift(newIndication);
      saveBandIndications(indications);
      renderIndicatedBands(indications);

      if (feedbackEl) {
        feedbackEl.hidden = false;
        feedbackEl.classList.remove('is-error');
        feedbackEl.setAttribute('role', 'status');
        feedbackEl.textContent = `☠ Indicação recebida! A horda "${bandName}" (${country}) foi registrada com sucesso para avaliação de entrevista.`;
        feedbackEl.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }

      form.reset();
    });
  }

  // --- ETAPA 8: PÁGINA DA EDIÇÃO (TEMPLATE BASE PARA BACKEND — EDIÇÃO #58) ---
  let editionPageState = {
    edition: null,
    photos: [],
    activeIndex: 0,
    listenersBound: false
  };

  function extractYoutubeVideoId(urlOrId) {
    if (!urlOrId) return '';
    const trimmed = String(urlOrId).trim();
    if (/^[a-zA-Z0-9_-]{11}$/.test(trimmed)) return trimmed;
    try {
      const parsed = new URL(trimmed);
      if (parsed.hostname.includes('youtu.be')) {
        return parsed.pathname.replace(/^\/+/, '').split('/')[0] || '';
      }
      if (parsed.searchParams.has('v')) {
        return parsed.searchParams.get('v') || '';
      }
      const match = parsed.pathname.match(/\/(?:embed|shorts|live)\/([a-zA-Z0-9_-]{11})/);
      if (match) return match[1];
    } catch (_) {
      return '';
    }
    return '';
  }

  function getStockPresentation(edition, isPt) {
    const status = edition.stockStatus || (edition.inStock ? 'available' : 'soldout');
    if (status === 'soldout' || !edition.inStock) {
      return {
        status: 'soldout',
        className: 'is-soldout',
        icon: 'fa-solid fa-skull',
        badgeText: isPt ? 'Esgotada' : 'Sold Out',
        inlineText: isPt ? 'Tiragem esgotada' : 'Print run sold out'
      };
    }
    if (status === 'low') {
      return {
        status: 'low',
        className: 'is-low',
        icon: 'fa-solid fa-fire',
        badgeText: isPt ? 'Últimas unidades' : 'Last copies',
        inlineText: isPt ? 'Últimas unidades em estoque' : 'Last copies in stock'
      };
    }
    return {
      status: 'available',
      className: 'is-available',
      icon: 'fa-solid fa-circle-check',
      badgeText: isPt ? 'Disponível' : 'Available',
      inlineText: isPt ? 'Disponível em estoque' : 'In stock & ready to ship'
    };
  }

  function updateEditionGalleryView(index) {
    const { photos, edition } = editionPageState;
    if (!photos.length) return;
    const safeIndex = ((index % photos.length) + photos.length) % photos.length;
    editionPageState.activeIndex = safeIndex;
    const current = photos[safeIndex];

    const mainPhoto = document.getElementById('edition-main-photo');
    const captionEl = document.getElementById('edition-photo-caption');
    const counterEl = document.getElementById('edition-photo-counter');
    const thumbsGrid = document.getElementById('edition-thumbs-grid');

    if (mainPhoto) {
      mainPhoto.src = current.src;
      mainPhoto.alt = current.caption || (edition ? edition.title : '');
    }
    if (captionEl) captionEl.textContent = current.caption;
    if (counterEl) counterEl.textContent = `${safeIndex + 1} / ${photos.length}`;

    if (thumbsGrid) {
      thumbsGrid.querySelectorAll('.edition-thumb-btn').forEach((btn, idx) => {
        const isActive = idx === safeIndex;
        btn.classList.toggle('is-active', isActive);
        btn.setAttribute('aria-pressed', isActive ? 'true' : 'false');
      });
    }

    // Sincroniza lightbox caso esteja aberto
    const lightboxImg = document.getElementById('edition-lightbox-img');
    const lightboxCaption = document.getElementById('edition-lightbox-caption');
    const lightboxCounter = document.getElementById('edition-lightbox-counter');
    const lightboxTitle = document.getElementById('edition-lightbox-title');
    if (lightboxImg) {
      lightboxImg.src = current.src;
      lightboxImg.alt = current.caption || (edition ? edition.title : '');
    }
    if (lightboxCaption) lightboxCaption.textContent = current.caption;
    if (lightboxCounter) lightboxCounter.textContent = `${safeIndex + 1} / ${photos.length}`;
    if (lightboxTitle && edition) lightboxTitle.textContent = edition.title;
  }

  function renderEditionDetailPage() {
    if (!document.body.classList.contains('page-edicao')) return;

    const serverEdition = getServerEditionData();
    if (!serverEdition) return;

    const isPt = serverEdition.editionLang === 'pt';
    editionPageState.edition = serverEdition;

    const coverCaption = isPt
      ? `Capa oficial — ${serverEdition.title}`
      : `Official cover — ${serverEdition.title}`;
    const extras = Array.isArray(serverEdition.extraPhotos) ? serverEdition.extraPhotos.slice(0, 5) : [];
    editionPageState.photos = [
      { src: serverEdition.cover, caption: coverCaption, isCover: true },
      ...extras.map((item, i) => ({
        src: typeof item === 'string' ? item : item.src,
        caption: (typeof item === 'object' && item && item.caption)
          ? item.caption
          : (isPt ? `Foto extra ${i + 1} — ${serverEdition.title}` : `Extra photo ${i + 1} — ${serverEdition.title}`),
        isCover: false
      }))
    ];
    editionPageState.activeIndex = 0;

    // Atualiza links de compartilhamento com a URL canônica atual
    const currentUrl = window.location.href;
    const shareText = isPt
      ? `Confira ${serverEdition.title} — Armamento Bélico Zine:`
      : `Check out ${serverEdition.title} — Armamento Bélico Zine:`;

    const waShare = document.getElementById('edition-share-whatsapp');
    const fbShare = document.getElementById('edition-share-facebook');
    const xShare = document.getElementById('edition-share-x');
    if (waShare) waShare.href = `https://wa.me/?text=${encodeURIComponent(`${shareText} ${currentUrl}`)}`;
    if (fbShare) fbShare.href = `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(currentUrl)}`;
    if (xShare) xShare.href = `https://twitter.com/intent/tweet?text=${encodeURIComponent(shareText)}&url=${encodeURIComponent(currentUrl)}`;

    updateSingleLangNotice(serverEdition, STATE.lang);
    setupYoutubeFacades(document.getElementById('edition-video-player-slot') || document);
  }

  function initEditionDetailPage() {
    if (!document.body.classList.contains('page-edicao')) return;

    renderEditionDetailPage();

    if (editionPageState.listenersBound) return;
    editionPageState.listenersBound = true;

    // Troca de imagem ao clicar nas miniaturas
    const thumbsGrid = document.getElementById('edition-thumbs-grid');
    if (thumbsGrid) {
      thumbsGrid.addEventListener('click', (e) => {
        const btn = e.target.closest('.edition-thumb-btn');
        if (!btn) return;
        const idx = Number(btn.dataset.photoIndex || 0);
        updateEditionGalleryView(idx);
      });
    }

    // Lightbox nativo via <dialog>
    const mainPhotoBtn = document.getElementById('edition-main-photo-btn');
    const lightboxDialog = document.getElementById('edition-lightbox-dialog');
    const lightboxClose = document.getElementById('edition-lightbox-close');
    const lightboxPrev = document.getElementById('edition-lightbox-prev');
    const lightboxNext = document.getElementById('edition-lightbox-next');

    if (mainPhotoBtn && lightboxDialog) {
      mainPhotoBtn.addEventListener('click', () => {
        updateEditionGalleryView(editionPageState.activeIndex);
        if (typeof lightboxDialog.showModal === 'function') {
          lightboxDialog.showModal();
        }
      });
    }

    if (lightboxClose && lightboxDialog) {
      lightboxClose.addEventListener('click', () => lightboxDialog.close());
    }
    if (lightboxPrev) {
      lightboxPrev.addEventListener('click', () => updateEditionGalleryView(editionPageState.activeIndex - 1));
    }
    if (lightboxNext) {
      lightboxNext.addEventListener('click', () => updateEditionGalleryView(editionPageState.activeIndex + 1));
    }

    // Navegação por setas do teclado enquanto o lightbox estiver aberto
    if (lightboxDialog) {
      lightboxDialog.addEventListener('keydown', (e) => {
        if (!lightboxDialog.open) return;
        if (e.key === 'ArrowLeft') {
          e.preventDefault();
          updateEditionGalleryView(editionPageState.activeIndex - 1);
        } else if (e.key === 'ArrowRight') {
          e.preventDefault();
          updateEditionGalleryView(editionPageState.activeIndex + 1);
        }
      });

      // Fallback de light-dismiss para navegadores sem suporte a <dialog closedby="any"> (ex: Safari)
      if (!('closedBy' in HTMLDialogElement.prototype)) {
        const isOutsideDialog = (e, dialog) => {
          const r = dialog.getBoundingClientRect();
          return e.clientX < r.left || e.clientX > r.right || e.clientY < r.top || e.clientY > r.bottom;
        };
        let pressedOutside = false;
        lightboxDialog.addEventListener('pointerdown', (e) => {
          pressedOutside = isOutsideDialog(e, lightboxDialog);
        });
        lightboxDialog.addEventListener('pointerup', (e) => {
          if (pressedOutside && isOutsideDialog(e, lightboxDialog)) {
            lightboxDialog.close();
          }
          pressedOutside = false;
        });
      }
    }

    // Botão Comprar (adiciona edição ativa ao carrinho)
    const buyBtn = document.getElementById('edition-btn-buy');
    if (buyBtn) {
      buyBtn.addEventListener('click', () => {
        if (editionPageState.edition && !buyBtn.disabled) {
          addToCart(editionPageState.edition.id);
        }
      });
    }

    // Botão Copiar Link
    const copyBtn = document.getElementById('edition-copy-link-btn');
    const copyLabel = document.getElementById('edition-copy-link-label');
    let copyResetTimer;

    if (copyBtn) {
      copyBtn.addEventListener('click', async () => {
        const urlToCopy = window.location.href;
        let copied = false;

        if (navigator.clipboard && typeof navigator.clipboard.writeText === 'function') {
          try {
            await navigator.clipboard.writeText(urlToCopy);
            copied = true;
          } catch (_) {
            copied = false;
          }
        }

        if (!copied) {
          try {
            const tempInput = document.createElement('textarea');
            tempInput.value = urlToCopy;
            tempInput.setAttribute('readonly', '');
            tempInput.style.position = 'fixed';
            tempInput.style.opacity = '0';
            document.body.appendChild(tempInput);
            tempInput.select();
            copied = document.execCommand('copy');
            tempInput.remove();
          } catch (_) {
            copied = false;
          }
        }

        const isPt = STATE.lang === 'pt';
        if (copied) {
          showToast(isPt ? 'Link da edição copiado!' : 'Issue link copied!');
          if (copyLabel) {
            copyLabel.textContent = isPt ? 'Link copiado!' : 'Link copied!';
            clearTimeout(copyResetTimer);
            copyResetTimer = setTimeout(() => {
              copyLabel.textContent = TRANSLATIONS[STATE.lang].edition_copy_link;
            }, 2500);
          }
        } else {
          showToast(isPt ? 'Não foi possível copiar o link.' : 'Could not copy link.');
        }
      });
    }
  }

  // --- FACADE DE VÍDEO DO YOUTUBE (EVITA FOCO/SCROLL AUTOMÁTICO NO LOAD) ---
  function setupYoutubeFacades(root = document) {
    root.querySelectorAll('.youtube-video-container[data-youtube-id]').forEach(container => {
      if (container.dataset.facadeBound === 'true') return;
      const btn = container.querySelector('.youtube-facade-btn');
      if (!btn) return;
      container.dataset.facadeBound = 'true';

      btn.addEventListener('click', () => {
        const videoId = container.getAttribute('data-youtube-id');
        const title = container.getAttribute('data-youtube-title') || 'YouTube Video';
        if (!videoId) return;

        const iframe = document.createElement('iframe');
        iframe.src = `https://www.youtube.com/embed/${encodeURIComponent(videoId)}?autoplay=1`;
        iframe.title = title;
        iframe.allow = 'accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share';
        iframe.referrerPolicy = 'strict-origin-when-cross-origin';
        iframe.allowFullscreen = true;

        container.replaceChildren(iframe);
      }, { once: true });
    });
  }

  // --- NAVEGAÇÃO RESPONSIVA (POPOVER NATIVO + MEDIÇÃO DINÂMICA SEM BREAKPOINT FIXO) ---
  let syncResponsiveNavLayout = null;

  function isPopoverOpen(el) {
    try {
      return el.matches(':popover-open') || el.classList.contains(':popover-open');
    } catch {
      return el.classList.contains(':popover-open');
    }
  }

  function initResponsiveNavigation() {
    // Polyfill condicional apenas quando o navegador não suporta Popover API nativamente
    if (typeof HTMLElement !== 'undefined' && !Object.hasOwn(HTMLElement.prototype, 'popover')) {
      const polyfillScript = document.createElement('script');
      polyfillScript.src = 'https://unpkg.com/@oddbird/popover-polyfill@latest/dist/popover.min.js';
      polyfillScript.defer = true;
      document.head.appendChild(polyfillScript);
    }

    const siteHeader = document.querySelector('.site-header');
    const headerContainer = siteHeader ? siteHeader.querySelector('.header-container') : null;
    const navList = document.getElementById('nav-list');
    const mobileToggle = document.getElementById('mobile-toggle');

    if (!siteHeader || !headerContainer || !navList || !mobileToggle) return;

    let lastHeaderWidth = 0;
    let rafId = 0;

    function updateToggleState(open) {
      mobileToggle.setAttribute('aria-expanded', open ? 'true' : 'false');
      const isPt = STATE.lang === 'pt';
      mobileToggle.setAttribute(
        'aria-label',
        open ? (isPt ? 'Fechar Menu' : 'Close Menu') : (isPt ? 'Abrir Menu' : 'Open Menu')
      );
      const icon = mobileToggle.querySelector('i');
      if (icon) {
        icon.classList.toggle('fa-bars', !open);
        icon.classList.toggle('fa-xmark', open);
      }
    }

    function updateHeaderHeightFallback() {
      const rect = siteHeader.getBoundingClientRect();
      if (rect.height > 0) {
        siteHeader.style.setProperty('--header-height', `${Math.ceil(rect.height)}px`);
      }
    }

    syncResponsiveNavLayout = function (force = false) {
      const currentWidth = siteHeader.clientWidth;
      const isCurrentlyWide = siteHeader.classList.contains('is-wide');

      // Enquanto colapsado, evita re-medição ao encolher (previne layout thrashing)
      if (!force && !isCurrentlyWide && currentWidth <= lastHeaderWidth) {
        updateHeaderHeightFallback();
        lastHeaderWidth = currentWidth;
        return;
      }

      const wasOpen = !isCurrentlyWide && isPopoverOpen(navList);

      siteHeader.classList.add('is-measuring');
      navList.removeAttribute('popover');
      siteHeader.classList.add('is-wide');

      // Compara largura natural dos filhos com o espaço disponível no header
      const fitsInline = headerContainer.scrollWidth <= headerContainer.clientWidth;

      if (fitsInline) {
        siteHeader.classList.add('is-wide');
        navList.removeAttribute('popover');
        updateToggleState(false);
      } else {
        siteHeader.classList.remove('is-wide');
        navList.setAttribute('popover', 'auto');
        updateHeaderHeightFallback();

        if (wasOpen && typeof navList.showPopover === 'function') {
          try {
            navList.showPopover();
          } catch {
            // Ignora caso o popover já esteja aberto ou ainda carregando polyfill
          }
        }
        updateToggleState(isPopoverOpen(navList));
      }

      lastHeaderWidth = currentWidth;

      if (rafId) cancelAnimationFrame(rafId);
      rafId = requestAnimationFrame(() => {
        siteHeader.classList.remove('is-measuring');
      });
    };

    navList.addEventListener('toggle', (event) => {
      const isOpen = event.newState ? event.newState === 'open' : isPopoverOpen(navList);
      updateToggleState(isOpen);
    });

    if (typeof ResizeObserver !== 'undefined') {
      const navObserver = new ResizeObserver(() => {
        syncResponsiveNavLayout(false);
      });
      navObserver.observe(siteHeader);
      navObserver.observe(navList);
    } else {
      window.addEventListener('resize', () => syncResponsiveNavLayout(true));
    }

    if (document.fonts && document.fonts.ready) {
      document.fonts.ready.then(() => syncResponsiveNavLayout(true));
    }

    syncResponsiveNavLayout(true);
  }

  // --- DESTAQUE DINÂMICO DO ITEM ATIVO NO MENU DE NAVEGAÇÃO ---
  function normalizeNavPath(pathname) {
    let normalized = (pathname || '/').trim().toLowerCase();
    normalized = normalized.replace(/\/+$/, '') || '/';
    if (normalized === '/index.html') return '/';
    if (normalized.endsWith('.html')) {
      normalized = normalized.slice(0, -5) || '/';
    }
    return normalized;
  }

  function updateActiveNavLink() {
    const navLinks = document.querySelectorAll('#main-nav .nav-link');
    if (!navLinks.length) return;

    const currentPath = normalizeNavPath(window.location.pathname);

    let bestMatch = null;
    let bestMatchLength = -1;

    navLinks.forEach(link => {
      const href = link.getAttribute('href');
      if (!href || href.startsWith('#') || href.startsWith('javascript:')) return;

      let linkPath;
      try {
        linkPath = normalizeNavPath(new URL(link.href, window.location.origin).pathname);
      } catch (_) {
        return;
      }

      const isExactMatch = currentPath === linkPath;
      const isNestedMatch = linkPath !== '/' && currentPath.startsWith(`${linkPath}/`);

      if ((isExactMatch || isNestedMatch) && linkPath.length > bestMatchLength) {
        bestMatch = link;
        bestMatchLength = linkPath.length;
      }
    });

    navLinks.forEach(link => {
      const isActive = link === bestMatch;
      link.classList.toggle('active', isActive);
      if (isActive) {
        link.setAttribute('aria-current', 'page');
      } else {
        link.removeAttribute('aria-current');
      }
    });
  }

  // --- MODAL DE LOGIN ADMINISTRATIVO ---
  function initAdminLoginModal() {
    const dialog = document.getElementById('admin-login-dialog');
    if (!dialog) return;

    // Fallback de light-dismiss para navegadores sem suporte a closedby="any"
    if (!('closedBy' in HTMLDialogElement.prototype)) {
      const isOutside = (event) => {
        const rect = dialog.getBoundingClientRect();
        return (
          event.clientX < rect.left ||
          event.clientX > rect.right ||
          event.clientY < rect.top ||
          event.clientY > rect.bottom
        );
      };
      let pressedOutside = false;
      dialog.addEventListener('pointerdown', (event) => {
        pressedOutside = isOutside(event);
      });
      dialog.addEventListener('pointerup', (event) => {
        if (pressedOutside && isOutside(event)) {
          dialog.close();
        }
      });
    }

    // Toggle de visualização de senha
    const passwordInput = document.getElementById('admin-password');
    const passwordToggle = document.getElementById('admin-password-toggle');
    if (passwordInput && passwordToggle) {
      passwordToggle.addEventListener('click', () => {
        const showing = passwordInput.type === 'text';
        passwordInput.type = showing ? 'password' : 'text';
        passwordToggle.setAttribute('aria-pressed', String(!showing));
        passwordToggle.setAttribute('aria-label', showing ? 'Mostrar senha' : 'Ocultar senha');
        const icon = passwordToggle.querySelector('i');
        if (icon) {
          icon.classList.toggle('fa-eye', showing);
          icon.classList.toggle('fa-eye-slash', !showing);
        }
      });
    }

    // Abre automaticamente o modal ao ser redirecionado de /admin ou após erro de login
    const params = new URLSearchParams(window.location.search);
    if (params.has('login') || params.has('loginError')) {
      if (!dialog.open && typeof dialog.showModal === 'function') {
        dialog.showModal();
      }
      params.delete('login');
      params.delete('loginError');
      const nextSearch = params.toString();
      const nextUrl = window.location.pathname + (nextSearch ? `?${nextSearch}` : '') + window.location.hash;
      window.history.replaceState({}, '', nextUrl);
    }
  }

  // --- INICIALIZAÇÃO GERAL ---
  function init() {
    // 0. Carregar configurações da loja (#store-config-data) e marcar aba ativa
    STORE_CONFIG = loadStoreConfig();
    STATE.whatsappNumber = STORE_CONFIG.whatsappNumber;
    STATE.paypalBusinessEmail = STORE_CONFIG.paypal.email;

    updateActiveNavLink();
    initAdminLoginModal();
    initStoreModals();

    // 1. Configurar listeners de idioma
    document.querySelectorAll('.lang-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const selectedLang = btn.getAttribute('data-lang');
        setLanguage(selectedLang);
      });
    });

    // 2. Configurar listeners do carrinho
    const cartTrigger = document.getElementById('cart-trigger-btn');
    if (cartTrigger) {
      cartTrigger.addEventListener('click', openCartDrawer);
    }

    const cartClose = document.getElementById('cart-close-btn');
    if (cartClose) {
      cartClose.addEventListener('click', closeCartDrawer);
    }

    const cartOverlay = document.getElementById('cart-overlay');
    if (cartOverlay) {
      cartOverlay.addEventListener('click', closeCartDrawer);
    }

    document.addEventListener('click', (event) => {
      const addBtn = event.target.closest('[data-add-to-cart-id]');
      if (!addBtn) return;
      const productId = addBtn.getAttribute('data-add-to-cart-id');
      if (productId) {
        addToCart(productId);
      }
    });

    const handleReturnToTab = () => {
      if (document.visibilityState === 'hidden') return;
      if (STATE.pendingOrderConfirmation && STATE.cart.length > 0) {
        openCartDrawer();
      }
    };
    document.addEventListener('visibilitychange', handleReturnToTab);
    window.addEventListener('focus', handleReturnToTab);

    // 3. Menu Responsivo Dinâmico (Popover Nativo + Medição sem Breakpoint Fixo)
    initResponsiveNavigation();

    // 4. Injetar ano dinâmico no footer
    const yearSpan = document.getElementById('current-year-span');
    if (yearSpan) {
      yearSpan.textContent = new Date().getFullYear();
    }

    // 5. Sincronizar idioma com a edição aberta (se página pública dinâmica) e aplicar estado
    const serverEdition = getServerEditionData();
    if (serverEdition && (serverEdition.editionLang === 'pt' || serverEdition.editionLang === 'en')) {
      STATE.lang = serverEdition.editionLang;
      localStorage.setItem('ab_lang', STATE.lang);
    }
    applyLanguage(STATE.lang);
    updateCartCountBadge();
    renderProducts();
    setupArchiveFilters();
    initEventsPage();
    initIndiquePage();
    initEditionDetailPage();
    setupYoutubeFacades();
  }

  // Expõe API para ser chamada por atributos inline (onclick)
  window.ArmamentoBelico = {
    setLanguage,
    getLanguage,
    addToCart,
    removeFromCart,
    updateQuantity,
    openCartDrawer,
    closeCartDrawer,
    checkoutWhatsApp,
    checkoutPayPal
  };

  // Carrega ao término do parse do DOM
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

})();
