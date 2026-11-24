/**
 * ARMAMENTO BÉLICO - UNDERGROUND ZINE SINCE 2008
 * Core JavaScript Engine: State Management, i18n, Cart, WhatsApp & PayPal checkout
 * Pure Vanilla JS - No Frameworks
 */

(function () {
  'use strict';

  // --- BASE DE DADOS DO FANZINE (PRODUTOS / EDIÇÕES) ---
  // REGRA: Edições em PT (R$) vs Edições em EN (US$)
  const ZINE_CATALOG = {
    pt: [
      {
        id: 'zine-58-pt',
        editionNumber: 58,
        detailUrl: 'edicao.html',
        title: 'Armamento Bélico #58',
        subtitle: 'Edição Nacional em Português • Especial Death & Black Metal Subterrâneo',
        price: 30.00,
        currency: 'R$',
        priceFormatted: 'R$ 30,00',
        cover: 'assets/images/capa-edicao-58.png',
        releaseMonth: 'Agosto',
        releaseYear: 2026,
        zineYear: 'Ano 18',
        date: 'Agosto / 2026 - Ano 18',
        bands: 'Diabolizer (Turquia), Mare (Noruega), Coldwinter (Brasil), Mortem Solis (Brasil), Venereal Baptism (EUA), Xiitium of Nemesis (Japão), Executor (Chile), Shaytan (Brasil), Sanctuire (Canadá), The Monolith Deathcult (Holanda)...',
        inStock: true,
        stockStatus: 'available', // Valores aceitos pelo template: 'available' | 'low' | 'soldout'
        pages: 68,
        description: '68 páginas em offset rústico pesado, capa em papel cartão com verniz fosco. Entrevistas profundas sobre a essência do Death e Black Metal obscuro, dezenas de resenhas de demos, fitas K7, zines e CDs do underground mundial.',
        // Capa + até 5 fotos extras (template suporta até 5 fotos adicionais vindas do backend)
        extraPhotos: [
          {
            src: 'assets/images/capa-edicao-57.jpg',
            caption: 'Foto extra 1/5 — Entrevista de capa: Diabolizer (ilustrativa do template)'
          },
          {
            src: 'assets/images/capa-edicao-56.jpg',
            caption: 'Foto extra 2/5 — Páginas internas e diagramação xerox (ilustrativa do template)'
          },
          {
            src: 'assets/images/capa-edicao-55.jpg',
            caption: 'Foto extra 3/5 — Seção de resenhas de demos, K7s e vinis (ilustrativa do template)'
          },
          {
            src: 'assets/images/cta-baphomet-assinatura.png',
            caption: 'Foto extra 4/5 — Arte e encarte exclusivo da edição (ilustrativa do template)'
          },
          {
            src: 'assets/images/youtube-spotlight-bg.png',
            caption: 'Foto extra 5/5 — Contracapa e detalhes de acabamento (ilustrativa do template)'
          }
        ],
        interviews: [
          { band: 'Diabolizer', country: 'Turquia', style: 'Death Metal' },
          { band: 'Mare', country: 'Noruega', style: 'Black Metal' },
          { band: 'Coldwinter', country: 'Brasil', style: 'Black Metal' },
          { band: 'Mortem Solis', country: 'Brasil', style: 'Death / Black Metal' },
          { band: 'Venereal Baptism', country: 'EUA', style: 'Black Metal' },
          { band: 'Xiitium of Nemesis', country: 'Japão', style: 'Death Metal' },
          { band: 'Executor', country: 'Chile', style: 'Thrash / Death Metal' },
          { band: 'Shaytan', country: 'Brasil', style: 'Black Metal' },
          { band: 'Sanctuire', country: 'Canadá', style: 'Black Metal' },
          { band: 'The Monolith Deathcult', country: 'Holanda', style: 'Brutal Death Metal' }
        ],
        extraContents: [
          'Mais de 70 resenhas sem filtros de demos, fitas K7, CDs, vinis e zines impressos',
          'Editorial especial celebrando 18 anos de existência ininterrupta no papel',
          'Dossiê sobre a cena Death Metal sul-americana e selos independentes',
          'Diagramação artesanal em alto contraste xerox com artes exclusivas'
        ],
        specs: {
          format: 'A5 (14,8 × 21 cm)',
          paper: 'Miolo offset rústico 90g • Capa papel cartão 250g com verniz fosco',
          language: 'Português (Brasil)',
          printRun: '300 cópias físicas numeradas'
        },
        shippingDeadline: 'Envio via Impresso Registrado em até 3 dias úteis após a confirmação do pagamento.',
        youtubeUrl: 'https://www.youtube.com/watch?v=5J1o1DHkekE',
        prevEdition: { number: 57, title: 'Armamento Bélico #57', url: 'arquivo.html' },
        nextEdition: null
      },
      {
        id: 'zine-57-pt',
        title: 'Armamento Bélico #57',
        subtitle: 'Edição Nacional em Português',
        price: 30.00,
        currency: 'R$',
        priceFormatted: 'R$ 30,00',
        cover: 'assets/images/capa-edicao-57.jpg',
        date: 'Junho / 2026 - Ano 17',
        bands: 'Litosth (Brasil), Impugner (Noruega), Zdrada Iskariota (Polônia), Ende (França), Tongues That Tell Secrets (EUA), Nihilo (Suíça), Metaphobic (EUA), Unholy Lust (EUA), Teufelnacht (Alemanha)...',
        inStock: true,
        pages: 64,
        description: 'Edição sangrenta cobrindo a ascensão do black metal sinfônico e visceral, doom e necro death metal. Acompanha encarte especial e resenhas exclusivas do underground sul-americano.'
      },
      {
        id: 'zine-56-pt',
        title: 'Armamento Bélico #56',
        subtitle: 'Edição Nacional em Português',
        price: 25.00,
        currency: 'R$',
        priceFormatted: 'R$ 25,00',
        cover: 'assets/images/capa-edicao-56.jpg',
        date: 'Janeiro / 2026 - Ano 17',
        bands: 'Lord Belial (Suécia), Pentagram Chile (Chile), Morbosidad (EUA), Incidere (México), Malefactor (Brasil), Vae Victis (Alemanha), Minenfeld (Alemanha), Disma (EUA), Clandestine Blaze (Finlândia)...',
        inStock: true,
        pages: 60,
        description: 'Conversa devastadora com os mestres suecos do Lord Belial, a história brutal do Pentagram Chile e o bestial black metal do Morbosidad. Últimos exemplares em estoque.'
      },
      {
        id: 'zine-55-pt',
        title: 'Armamento Bélico #55',
        subtitle: 'Edição Nacional em Português',
        price: 25.00,
        currency: 'R$',
        priceFormatted: 'R$ 25,00',
        cover: 'assets/images/capa-edicao-55.jpg',
        date: 'Novembro / 2025 - Ano 17',
        bands: 'Patriarkh (Polônia), Mental Horror (Brasil), Wolfslust (Brasil), Burial (Itália), Mortal Profecia (Brasil), Eisige Venen (EUA), Loits (Estônia), Shrouded in Darkness (Suécia)...',
        inStock: false, // ESGOTADO para demonstrar badge e estado
        pages: 60,
        description: 'A transição histórica do Batushka para Patriarkh com entrevista exclusiva de 6 páginas, mais o retorno furioso do Mental Horror e a necromancia sonora da Estônia e Polônia. Tiragem esgotada.'
      },
      // Edições Históricas do Arquivo
      {
        id: 'zine-54-pt',
        title: 'Armamento Bélico #54',
        subtitle: 'Edição de Colecionador',
        price: 25.00,
        currency: 'R$',
        priceFormatted: 'R$ 25,00',
        cover: 'assets/images/capa-edicao-56.jpg',
        date: 'Julho / 2025 - Ano 16',
        bands: 'Sarcófago Tribute, Gorgoroth, Mystifier, Krisiun, Incantation, Goatlord...',
        inStock: false,
        pages: 56,
        description: 'Tributo ao metal da morte sul-americano, histórias de fita cassete nos anos 80 e 90.'
      },
      {
        id: 'zine-50-pt',
        title: 'Armamento Bélico #50 - Especial Jubileu Maldito',
        subtitle: 'Edição Comemorativa 50 Lançamentos',
        price: 35.00,
        currency: 'R$',
        priceFormatted: 'R$ 35,00',
        cover: 'assets/images/capa-edicao-57.jpg',
        date: 'Dezembro / 2024 - Ano 16',
        bands: 'Mayhem, Darkthrone, Vulcano, Holocausto, Sextrash, Rotting Christ...',
        inStock: false,
        pages: 84,
        description: 'Edição gigante de 84 páginas comemorando o marco de cinquenta fanzines físicos ininterruptos.'
      }
    ],

    en: [
      {
        id: 'zine-58-en',
        editionNumber: 58,
        detailUrl: 'edicao.html',
        title: 'Armamento Bélico #58 [ENG]',
        subtitle: 'International English Print Edition • Underground Death & Black Metal Special',
        price: 12.00,
        currency: 'US$',
        priceFormatted: 'US$ 12.00',
        cover: 'assets/images/capa-edicao-58.png',
        releaseMonth: 'August',
        releaseYear: 2026,
        zineYear: 'Year 18',
        date: 'August / 2026 - Vol. 18',
        bands: 'Diabolizer (Turkey), Mare (Norway), Coldwinter (Brazil), Mortem Solis (Brazil), Venereal Baptism (USA), Xiitium of Nemesis (Japan), Executor (Chile), Sanctuire (Canada)...',
        inStock: true,
        stockStatus: 'available',
        pages: 68,
        description: 'Full English translation. 68 heavyweight printed pages of ruthless underground death/black metal madness, interviews, cult underground demos and underground release reviews.',
        extraPhotos: [
          {
            src: 'assets/images/capa-edicao-57.jpg',
            caption: 'Extra photo 1/5 — Cover interview: Diabolizer (template preview)'
          },
          {
            src: 'assets/images/capa-edicao-56.jpg',
            caption: 'Extra photo 2/5 — Inside pages & xerox layout (template preview)'
          },
          {
            src: 'assets/images/capa-edicao-55.jpg',
            caption: 'Extra photo 3/5 — Reviews section: demos, tapes & vinyl (template preview)'
          },
          {
            src: 'assets/images/cta-baphomet-assinatura.png',
            caption: 'Extra photo 4/5 — Exclusive insert & artwork (template preview)'
          },
          {
            src: 'assets/images/youtube-spotlight-bg.png',
            caption: 'Extra photo 5/5 — Back cover & print finish details (template preview)'
          }
        ],
        interviews: [
          { band: 'Diabolizer', country: 'Turkey', style: 'Death Metal' },
          { band: 'Mare', country: 'Norway', style: 'Black Metal' },
          { band: 'Coldwinter', country: 'Brazil', style: 'Black Metal' },
          { band: 'Mortem Solis', country: 'Brazil', style: 'Death / Black Metal' },
          { band: 'Venereal Baptism', country: 'USA', style: 'Black Metal' },
          { band: 'Xiitium of Nemesis', country: 'Japan', style: 'Death Metal' },
          { band: 'Executor', country: 'Chile', style: 'Thrash / Death Metal' },
          { band: 'Shaytan', country: 'Brazil', style: 'Black Metal' },
          { band: 'Sanctuire', country: 'Canada', style: 'Black Metal' },
          { band: 'The Monolith Deathcult', country: 'Netherlands', style: 'Brutal Death Metal' }
        ],
        extraContents: [
          'Over 70 unfiltered reviews of demos, cassette tapes, CDs, vinyl and printed zines',
          'Special editorial celebrating 18 years of uninterrupted physical print resistance',
          'South American underground Death Metal dossier & independent labels spotlight',
          'Old-school high-contrast xerox layout with exclusive underground artwork'
        ],
        specs: {
          format: 'A5 (14.8 × 21 cm)',
          paper: '90gsm heavy offset interior • 250gsm matte card cover',
          language: 'English (International)',
          printRun: '300 numbered physical copies'
        },
        shippingDeadline: 'Dispatched via Registered Worldwide Airmail within 3 business days after payment confirmation.',
        youtubeUrl: 'https://www.youtube.com/watch?v=5J1o1DHkekE',
        prevEdition: { number: 57, title: 'Armamento Bélico #57 [ENG]', url: 'arquivo.html' },
        nextEdition: null
      },
      {
        id: 'zine-57-en',
        title: 'Armamento Bélico #57 [ENG]',
        subtitle: 'International English Print Edition',
        price: 12.00,
        currency: 'US$',
        priceFormatted: 'US$ 12.00',
        cover: 'assets/images/capa-edicao-57.jpg',
        date: 'June / 2026 - Vol. 17',
        bands: 'Litosth (Brazil), Impugner (Norway), Zdrada Iskariota (Poland), Ende (France), Tongues That Tell Secrets (USA), Nihilo (Switzerland), Metaphobic (USA)...',
        inStock: true,
        pages: 64,
        description: 'International issue featuring deep discussions with emerging European and American extreme acts, obscure tape archives, and South American necromancy.'
      },
      {
        id: 'zine-56-en',
        title: 'Armamento Bélico #56 [ENG]',
        subtitle: 'International English Print Edition',
        price: 10.00,
        currency: 'US$',
        priceFormatted: 'US$ 10.00',
        cover: 'assets/images/capa-edicao-56.jpg',
        date: 'January / 2026 - Vol. 17',
        bands: 'Lord Belial (Sweden), Pentagram Chile (Chile), Morbosidad (USA), Incidere (Mexico), Malefactor (Brazil), Vae Victis (Germany), Disma (USA)...',
        inStock: true,
        pages: 60,
        description: 'Swedish black/death masters Lord Belial, South American pioneers Pentagram Chile, and crushing underground demo reviews. Few copies remaining.'
      },
      {
        id: 'zine-55-en',
        title: 'Armamento Bélico #55 [ENG]',
        subtitle: 'International English Print Edition',
        price: 10.00,
        currency: 'US$',
        priceFormatted: 'US$ 10.00',
        cover: 'assets/images/capa-edicao-55.jpg',
        date: 'November / 2025 - Vol. 17',
        bands: 'Patriarkh (Poland), Mental Horror (Brazil), Wolfslust (Brazil), Burial (Italy), Mortal Profecia (Brazil), Eisige Venen (USA)...',
        inStock: false,
        pages: 60,
        description: 'Crucial historical interview with Patriarkh (ex-Batushka), relentless Brazilian death metal assault and Eastern European black metal. Sold out.'
      },
      {
        id: 'zine-50-en',
        title: 'Armamento Bélico #50 [ENG] - Special Jubilee',
        subtitle: '50th Issue Commemorative Edition',
        price: 15.00,
        currency: 'US$',
        priceFormatted: 'US$ 15.00',
        cover: 'assets/images/capa-edicao-57.jpg',
        date: 'December / 2024 - Vol. 16',
        bands: 'Mayhem, Darkthrone, Vulcano, Holocausto, Sextrash, Rotting Christ...',
        inStock: false,
        pages: 84,
        description: 'Colossal 84-page anniversary issue honoring 50 consecutive printed fanzines in full English text. Archival collectors edition.'
      }
    ]
  };

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
      news_read_all: 'Ver Todas as Notícias',
      news_read_more: 'Ler Matéria Completa ☠',
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
      news_read_all: 'View All News',
      news_read_more: 'Read Full Article ☠',
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

  // Configuração fixa de frete (pronta para ser sobrescrita futuramente pelo backend)
  const SHIPPING_CONFIG = {
    'R$': {
      baseFee: 13.00,      // 1 edição: R$ 13,00
      additionalFee: 2.00  // Cada edição adicional: + R$ 2,00
    },
    'US$': {
      baseFee: 13.00,
      additionalFee: 2.00
    }
  };

  const STATE = {
    lang: localStorage.getItem('ab_lang') || 'pt',
    cart: JSON.parse(localStorage.getItem('ab_cart') || '[]'),
    checkoutAddress: savedAddress,
    isReturningCustomer: localStorage.getItem('ab_returning_customer') === 'true',
    paymentMethod: localStorage.getItem('ab_payment_method') === 'card' ? 'card' : 'pix',
    pendingOrderConfirmation: localStorage.getItem('ab_pending_order_confirmation') === 'true',
    showOrderThankYou: false,
    whatsappNumber: '5527981076157', // Número oficial para pedidos via WhatsApp
    paypalBusinessEmail: 'armamento.belico.zine@gmail.com'
  };

  // Salvar idioma
  function setLanguage(lang) {
    if (lang !== 'pt' && lang !== 'en') lang = 'pt';
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

  function addToCart(productId) {
    const currentLang = STATE.lang;
    const catalog = ZINE_CATALOG[currentLang];
    const product = catalog.find(p => p.id === productId);

    if (!product || !product.inStock) {
      showToast(currentLang === 'pt' ? 'Item esgotado ou indisponível!' : 'Item is sold out!');
      return;
    }

    const existingIndex = STATE.cart.findIndex(item => item.id === productId);
    if (existingIndex > -1) {
      STATE.cart[existingIndex].quantity += 1;
    } else {
      STATE.cart.push({
        id: product.id,
        title: product.title,
        price: product.price,
        currency: product.currency,
        cover: product.cover,
        quantity: 1,
        lang: currentLang
      });
    }

    STATE.showOrderThankYou = false;
    if (STATE.pendingOrderConfirmation) {
      setPendingOrderConfirmation(false);
    }

    saveCart();
    openCartDrawer();
    showToast(currentLang === 'pt' ? `"${product.title}" adicionado ao caixão!` : `"${product.title}" added to cart!`);
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
    const rule = SHIPPING_CONFIG[currency] || SHIPPING_CONFIG['R$'];
    return rule.baseFee + ((itemCount - 1) * rule.additionalFee);
  }

  function getCartTotal() {
    if (STATE.cart.length === 0) return 0;
    const currency = STATE.cart[0].currency;
    return getCartSubtotal() + calculateShipping(getCartItemCount(), currency);
  }

  function formatCurrency(value, currency) {
    return currency === 'R$'
      ? `R$ ${value.toFixed(2).replace('.', ',')}`
      : `US$ ${value.toFixed(2)}`;
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

    // Atualiza seletor de navegação da assinatura (Regra estrita)
    updateSubscriptionNavVisibility();

    // Reavalia se o menu cabe em linha única após mudança de idioma/itens
    if (typeof syncResponsiveNavLayout === 'function') {
      syncResponsiveNavLayout(true);
    }
  }

  // REGRA DO MENU: O link para a página "Assinatura" só deve estar visível no menu quando o idioma ativo for Português
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

    // Se estiver na página de assinatura e mudar para EN, exibe container de aviso
    const subNotice = document.getElementById('sub-intl-notice');
    const subContent = document.getElementById('subscription-content');
    if (subNotice) {
      const isEn = STATE.lang === 'en';
      subNotice.style.display = isEn ? 'block' : 'none';
      if (subContent) subContent.hidden = isEn;
    }
  }

  // --- RENDERIZAÇÃO DOS PRODUTOS / EDIÇÕES ---
  function renderProducts() {
    const productsContainer = document.getElementById('products-grid-container');
    if (!productsContainer) return;

    const currentLang = STATE.lang;
    const catalog = ZINE_CATALOG[currentLang];
    const isArchivePage = document.body.classList.contains('page-arquivo');

    // Se for na Home, exibe apenas os 4 mais recentes em estoque
    const listToRender = isArchivePage ? catalog : catalog.slice(0, 4);

    let html = '';
    listToRender.forEach(zine => {
      const stockBadge = zine.inStock
        ? `<span class="tape-tag blood">${currentLang === 'pt' ? 'EM ESTOQUE' : 'IN STOCK'}</span>`
        : `<span class="tape-tag dark">${currentLang === 'pt' ? 'ESGOTADO' : 'SOLD OUT'}</span>`;

      const buyBtn = zine.inStock
        ? `<button class="btn-brutal btn-blood btn-sm" onclick="ArmamentoBelico.addToCart('${zine.id}')">
             <span class="inverted-cross">✝</span> ${currentLang === 'pt' ? 'Comprar Cópia' : 'Buy Copy'}
           </button>`
        : `<button class="btn-sold-out" disabled>${currentLang === 'pt' ? 'Esgotado' : 'Sold Out'}</button>`;

      const coverHtml = zine.detailUrl
        ? `<a href="${zine.detailUrl}" class="product-cover-link" aria-label="${currentLang === 'pt' ? `Ver detalhes de ${zine.title}` : `View details for ${zine.title}`}">
             <img src="${zine.cover}" alt="${zine.title}" class="xerox-img" loading="lazy">
           </a>`
        : `<img src="${zine.cover}" alt="${zine.title}" class="xerox-img" loading="lazy">`;

      const titleHtml = zine.detailUrl
        ? `<h3 class="product-title pirata-title"><a href="${zine.detailUrl}">${zine.title}</a></h3>`
        : `<h3 class="product-title pirata-title">${zine.title}</h3>`;

      const detailsLinkHtml = zine.detailUrl
        ? `<a href="${zine.detailUrl}" class="product-details-link">
             ${currentLang === 'pt' ? 'Ver detalhes da edição' : 'View issue details'} <i class="fa-solid fa-arrow-right" aria-hidden="true"></i>
           </a>`
        : '';

      html += `
        <article class="product-card" data-stock="${zine.inStock ? 'instock' : 'soldout'}">
          <div class="product-badge-wrap">
            ${stockBadge}
          </div>
          <div class="product-cover-wrap">
            ${coverHtml}
          </div>
          <div class="product-info">
            <span class="product-edition-number">${zine.date}</span>
            ${titleHtml}
            <p class="product-bands-list"><strong>${currentLang === 'pt' ? 'Destaques:' : 'Featuring:'}</strong> ${zine.bands}</p>
            ${detailsLinkHtml}
            <div class="product-price-bar">
              <span class="product-price">${zine.priceFormatted}</span>
              ${buyBtn}
            </div>
          </div>
        </article>
      `;
    });

    productsContainer.innerHTML = html;
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

    if (STATE.cart.length === 0) {
      if (STATE.showOrderThankYou) {
        const isPtEmpty = STATE.lang === 'pt';
        cartItemsContainer.innerHTML = `
          <div class="cart-thank-you-box" role="status" aria-live="polite">
            <p class="cart-thank-you-icon" aria-hidden="true">☠</p>
            <h4 class="cart-thank-you-title">${isPtEmpty ? 'Obrigado pelo seu pedido!' : 'Thank you for your order!'}</h4>
            <p class="cart-thank-you-text">
              ${isPtEmpty
                ? 'Seu apoio fortalece o Armamento Bélico Zine e o verdadeiro underground. Em breve entraremos em contato para confirmar os detalhes do envio.'
                : 'Your support keeps Armamento Bélico Zine and the true underground alive. We will follow up with your shipping details shortly.'}
            </p>
          </div>
        `;
      } else {
        cartItemsContainer.innerHTML = `
          <div class="cart-empty-message">
            <p style="font-size: 2.5rem; margin-bottom: 10px;">☠</p>
            <p>${STATE.lang === 'pt' ? 'Seu caixão está vazio. Nenhum fanzine adicionado.' : 'Your coffin is empty. No zines summoned.'}</p>
          </div>
        `;
      }
      if (summaryEl) {
        summaryEl.hidden = true;
        summaryEl.innerHTML = '';
      }
      cartTotalVal.textContent = formatCurrency(0, STATE.lang === 'pt' ? 'R$' : 'US$');
      cartCheckoutArea.innerHTML = '';
      return;
    }

    let itemsHtml = '';
    const currency = STATE.cart[0].currency;

    STATE.cart.forEach(item => {
      const formattedPrice = formatCurrency(item.price, currency);

      itemsHtml += `
        <div class="cart-item-row">
          <div class="cart-item-thumb">
            <img src="${item.cover}" alt="${item.title}">
          </div>
          <div>
            <h4 class="cart-item-title">${item.title}</h4>
            <span class="cart-item-unit-price">${formattedPrice} un</span>
            <div class="cart-qty-ctrl">
              <button class="qty-btn" onclick="ArmamentoBelico.updateQuantity('${item.id}', -1)">-</button>
              <span class="qty-number">${item.quantity}</span>
              <button class="qty-btn" onclick="ArmamentoBelico.updateQuantity('${item.id}', 1)">+</button>
            </div>
          </div>
          <button class="cart-item-remove-btn" title="Remover" onclick="ArmamentoBelico.removeFromCart('${item.id}')">✕</button>
        </div>
      `;
    });

    const isPt = STATE.lang === 'pt';
    const totalQty = getCartItemCount();
    const subtotal = getCartSubtotal();
    const shipping = calculateShipping(totalQty, currency);
    const total = subtotal + shipping;

    if (summaryEl) {
      const itemUnitLabel = isPt
        ? (totalQty === 1 ? 'edição' : 'edições')
        : (totalQty === 1 ? 'issue' : 'issues');
      const subtotalLabel = isPt
        ? `Subtotal (${totalQty} ${itemUnitLabel}):`
        : `Subtotal (${totalQty} ${itemUnitLabel}):`;
      const shippingLabel = isPt
        ? 'Frete (Correios):'
        : 'Shipping (Airmail):';

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
      `;
    }

    cartItemsContainer.innerHTML = itemsHtml;
    cartTotalVal.textContent = formatCurrency(total, currency);

    if (STATE.pendingOrderConfirmation) {
      cartCheckoutArea.innerHTML = `
        <div class="cart-order-confirm-box" role="region" aria-live="polite" aria-label="${isPt ? 'Confirmação de Pedido' : 'Order Confirmation'}">
          <p class="cart-order-confirm-question">
            ${isPt ? 'O pedido foi realizado com sucesso?' : 'Was the order placed successfully?'}
          </p>
          <p class="cart-order-confirm-hint">
            ${isPt
              ? 'Se você já concluiu o envio do pedido, confirme abaixo para limpar o carrinho.'
              : 'If you completed your checkout, confirm below to clear your cart.'}
          </p>
          <div class="cart-order-confirm-actions">
            <button type="button" id="cart-confirm-yes-btn" class="btn-confirm-yes">
              <i class="fa-solid fa-check" aria-hidden="true"></i> ${isPt ? 'Sim' : 'Yes'}
            </button>
            <button type="button" id="cart-confirm-no-btn" class="btn-confirm-no">
              <i class="fa-solid fa-xmark" aria-hidden="true"></i> ${isPt ? 'Não' : 'No'}
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

    // REGRA DE CHECKOUT: PT -> WhatsApp com pedido | EN -> PayPal
    const returningLabel = isPt
      ? 'Já sou cliente (não preciso informar novamente o endereço)'
      : 'I am a returning customer (no need to enter my address again)';
    const legendText = isPt ? 'Endereço de Entrega' : 'Shipping Address';
    const errorIconLabel = isPt ? 'Erro' : 'Error';

    const labels = isPt
      ? {
          fullName: 'Nome Completo',
          fullNameErr: 'Informe seu nome completo.',
          zip: 'CEP',
          zipErr: 'Informe o CEP.',
          street: 'Rua / Logradouro',
          streetErr: 'Informe a rua ou logradouro.',
          number: 'Número',
          numberErr: 'Informe o número.',
          complement: 'Complemento (opcional)',
          neighborhood: 'Bairro',
          neighborhoodErr: 'Informe o bairro.',
          city: 'Cidade',
          cityErr: 'Informe a cidade.',
          state: 'Estado (UF)',
          stateErr: 'Informe o estado.'
        }
      : {
          fullName: 'Full Name',
          fullNameErr: 'Enter your full name.',
          zip: 'Postal Code / ZIP',
          zipErr: 'Enter your postal code.',
          street: 'Street Address',
          streetErr: 'Enter your street address.',
          number: 'Number',
          numberErr: 'Enter the street number.',
          complement: 'Apt / Suite (optional)',
          neighborhood: 'District / Neighborhood',
          neighborhoodErr: 'Enter your district or neighborhood.',
          city: 'City',
          cityErr: 'Enter your city.',
          state: 'State / Province',
          stateErr: 'Enter your state or province.'
        };

    const reqAttr = STATE.isReturningCustomer ? '' : 'required';
    const hiddenDisabledAttr = STATE.isReturningCustomer ? 'hidden disabled' : '';

    const paymentFieldsetHtml = isPt
      ? `
        <fieldset class="cart-payment-fieldset" id="cart-payment-fieldset">
          <legend>Forma de Pagamento</legend>
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
                <span class="cart-payment-label">Cartão de Crédito</span>
              </span>
            </label>
          </div>
        </fieldset>
      `
      : '';

    const submitBtnHtml = isPt
      ? `
        <button type="submit" class="btn-whatsapp-checkout">
          <i class="fa-brands fa-whatsapp" style="font-size: 1.2rem;" aria-hidden="true"></i> Finalizar Pedido via WhatsApp
        </button>
        <p class="cart-checkout-note">
          Seu pedido será gerado e enviado diretamente para o editor via WhatsApp.
        </p>
      `
      : `
        <button type="submit" class="btn-paypal-checkout">
          <i class="fa-brands fa-paypal" style="font-size: 1.2rem;" aria-hidden="true"></i> Checkout with PayPal (Worldwide)
        </button>
        <p class="cart-checkout-note">
          Safe international payments via PayPal. Registered worldwide airmail.
        </p>
      `;

    cartCheckoutArea.innerHTML = `
      <form id="cart-checkout-form" class="cart-checkout-form" novalidate aria-label="${isPt ? 'Finalizar Pedido' : 'Order Checkout'}">
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

          <div class="cart-address-grid">
            <div class="cart-address-field">
              <label for="cart-addr-zip">${labels.zip} <span aria-hidden="true">*</span></label>
              <input type="text" id="cart-addr-zip" name="postal_code" class="form-control cart-address-input" inputmode="numeric" autocomplete="postal-code" maxlength="20" data-error="err-cart-zip" data-addr-key="zip" ${reqAttr}>
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
            <label for="cart-addr-neighborhood">${labels.neighborhood} <span aria-hidden="true">*</span></label>
            <input type="text" id="cart-addr-neighborhood" name="neighborhood" class="form-control cart-address-input" autocomplete="address-level3" maxlength="80" data-error="err-cart-neighborhood" data-addr-key="neighborhood" ${reqAttr}>
            <div id="err-cart-neighborhood" class="error-msg"><i class="fa-solid fa-circle-xmark" aria-hidden="true"></i> ${labels.neighborhoodErr}</div>
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

    const syncFieldWhitespace = (input) => {
      if (STATE.isReturningCustomer || input.dataset.addrKey === 'complement') {
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
            const isOptional = input.dataset.addrKey === 'complement';
            if (!isOptional) {
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
        if (isPt) {
          checkoutWhatsApp();
        } else {
          checkoutPayPal();
        }
      });
    }
  }

  function hasValidShippingAddress() {
    if (STATE.isReturningCustomer) return true;
    const a = STATE.checkoutAddress;
    return [a.fullName, a.zip, a.street, a.number, a.neighborhood, a.city, a.state]
      .every(val => typeof val === 'string' && val.trim().length > 0);
  }

  // --- CHECKOUT VIA WHATSAPP (VERSÃO PT) ---
  function checkoutWhatsApp() {
    if (STATE.cart.length === 0) return;

    if (!hasValidShippingAddress()) {
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
    const whatsappUrl = `https://wa.me/${STATE.whatsappNumber}?text=${encodedMessage}`;
    setPendingOrderConfirmation(true);
    renderCart();
    window.open(whatsappUrl, '_blank');
  }

  // --- CHECKOUT VIA PAYPAL (VERSÃO EN) ---
  function checkoutPayPal() {
    if (STATE.cart.length === 0) return;

    if (!hasValidShippingAddress()) {
      const firstInvalid = document.querySelector('#cart-checkout-form :invalid');
      if (firstInvalid) firstInvalid.focus();
      return;
    }

    const currency = STATE.cart[0].currency || 'US$';
    const subtotal = getCartSubtotal();
    const shipping = calculateShipping(getCartItemCount(), currency);
    const total = (subtotal + shipping).toFixed(2);
    const itemsDescription = STATE.cart.map(i => `${i.quantity}x ${i.title}`).join(', ');
    const a = STATE.checkoutAddress;
    const addressSummary = STATE.isReturningCustomer
      ? 'Returning customer (address on file)'
      : `${a.fullName.trim()} — ${a.street.trim()}, ${a.number.trim()}${a.complement.trim() ? ' (' + a.complement.trim() + ')' : ''}, ${a.neighborhood.trim()}, ${a.city.trim()} - ${a.state.trim()}, ${a.zip.trim()}`;

    // Cria requisição e modal de simulação/redirecionamento oficial PayPal
    const confirmPayment = confirm(
      `[ARMAMENTO BÉLICO INTERNATIONAL ORDER]\n\nItems: ${itemsDescription}\nSubtotal: US$ ${subtotal.toFixed(2)}\nShipping: US$ ${shipping.toFixed(2)}\nTotal: US$ ${total}\nShipping Address: ${addressSummary}\n\nProceed to secure PayPal Checkout to complete transaction?`
    );

    if (confirmPayment) {
      // Redireciona para o fluxo do PayPal (ou abre URL de pagamento do PayPal Business)
      const paypalUrl = `https://www.paypal.com/cgi-bin/webscr?cmd=_xclick&business=${encodeURIComponent(STATE.paypalBusinessEmail)}&item_name=${encodeURIComponent('Armamento Belico Zine Order - ' + itemsDescription)}&amount=${total}&currency_code=USD`;
      setPendingOrderConfirmation(true);
      renderCart();
      window.open(paypalUrl, '_blank');
      showToast('Redirecting to PayPal secure gateway...');
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

    const isPt = STATE.lang === 'pt';
    const catalog = ZINE_CATALOG[STATE.lang] || ZINE_CATALOG.pt;
    const targetNum = Number(document.body.dataset.editionNumber || 58);
    const edition = catalog.find(z => z.editionNumber === targetNum) || catalog[0];
    if (!edition) return;

    editionPageState.edition = edition;

    // Monta galeria: capa principal + até 5 fotos extras (máximo 6 fotos)
    const coverCaption = isPt
      ? `Capa oficial — ${edition.title}`
      : `Official cover — ${edition.title}`;
    const extras = Array.isArray(edition.extraPhotos) ? edition.extraPhotos.slice(0, 5) : [];
    editionPageState.photos = [
      { src: edition.cover, caption: coverCaption, isCover: true },
      ...extras.map((item, i) => ({
        src: typeof item === 'string' ? item : item.src,
        caption: typeof item === 'string'
          ? (isPt ? `Foto extra ${i + 1} — ${edition.title}` : `Extra photo ${i + 1} — ${edition.title}`)
          : item.caption,
        isCover: false
      }))
    ];
    editionPageState.activeIndex = 0;

    // Título da página & Breadcrumb
    document.title = `${edition.title.toUpperCase()} | Armamento Bélico Zine`;
    const breadcrumbCurrent = document.getElementById('edition-breadcrumb-current');
    if (breadcrumbCurrent) breadcrumbCurrent.textContent = edition.title;

    const numTag = document.getElementById('edition-number-tag');
    const yearTag = document.getElementById('edition-year-tag');
    const titleEl = document.getElementById('edition-title');
    const subtitleEl = document.getElementById('edition-subtitle');

    if (numTag) numTag.textContent = isPt ? `EDIÇÃO #${edition.editionNumber}` : `ISSUE #${edition.editionNumber}`;
    if (yearTag) yearTag.textContent = (edition.zineYear || '').toUpperCase();
    if (titleEl) titleEl.textContent = edition.title;
    if (subtitleEl) subtitleEl.textContent = edition.subtitle || '';

    // Informações complementares (Mês, Ano, Ano da revista, Páginas)
    const metaMonth = document.getElementById('edition-meta-month');
    const metaYear = document.getElementById('edition-meta-year');
    const metaExistence = document.getElementById('edition-meta-existence');
    const metaPages = document.getElementById('edition-meta-pages');

    if (metaMonth) metaMonth.textContent = edition.releaseMonth || '—';
    if (metaYear) metaYear.textContent = edition.releaseYear ? String(edition.releaseYear) : '—';
    if (metaExistence) metaExistence.textContent = edition.zineYear || '—';
    if (metaPages) metaPages.textContent = `${edition.pages} ${isPt ? 'páginas' : 'pages'}`;

    // Status de disponibilidade / estoque
    const stockInfo = getStockPresentation(edition, isPt);
    const stockPill = document.getElementById('edition-stock-pill');
    const stockInline = document.getElementById('edition-stock-inline');
    const soldoutAlert = document.getElementById('edition-soldout-alert');
    const soldoutAlertText = document.getElementById('edition-soldout-alert-text');

    if (stockPill) {
      stockPill.className = `edition-stock-pill ${stockInfo.className}`;
      stockPill.innerHTML = `<i class="${stockInfo.icon}" aria-hidden="true"></i> <span id="edition-stock-pill-text">${escapeHtml(stockInfo.badgeText)}</span>`;
    }
    if (stockInline) {
      stockInline.className = `edition-stock-inline ${stockInfo.className}`;
      stockInline.textContent = stockInfo.inlineText;
    }
    if (soldoutAlert) {
      soldoutAlert.hidden = stockInfo.status !== 'soldout';
      if (soldoutAlertText) {
        soldoutAlertText.textContent = isPt
          ? 'Esta edição física encontra-se esgotada em nossa tiragem original. O registro permanece no catálogo histórico do zine.'
          : 'This physical print run is sold out. This page remains archived in the zine historical catalog.';
      }
    }

    // Valor & Botão Comprar
    const priceEl = document.getElementById('edition-price-value');
    const buyBtn = document.getElementById('edition-btn-buy');
    const buyBtnLabel = document.getElementById('edition-btn-buy-label');

    if (priceEl) priceEl.textContent = edition.priceFormatted;
    if (buyBtn && buyBtnLabel) {
      if (stockInfo.status === 'soldout') {
        buyBtn.disabled = true;
        buyBtn.classList.add('is-disabled');
        buyBtnLabel.textContent = isPt ? 'Edição Esgotada' : 'Sold Out';
      } else {
        buyBtn.disabled = false;
        buyBtn.classList.remove('is-disabled');
        buyBtnLabel.textContent = isPt
          ? `Comprar Edição #${edition.editionNumber}`
          : `Buy Issue #${edition.editionNumber}`;
      }
    }

    // Resumo de envio / frete (conforme regra do item 4)
    const shippingRule = SHIPPING_CONFIG[edition.currency] || SHIPPING_CONFIG['R$'];
    const feeTextEl = document.getElementById('edition-shipping-fee-text');
    const deadlineTextEl = document.getElementById('edition-shipping-deadline-text');

    if (feeTextEl) {
      const baseFormatted = formatCurrency(shippingRule.baseFee, edition.currency);
      const addFormatted = formatCurrency(shippingRule.additionalFee, edition.currency);
      feeTextEl.innerHTML = isPt
        ? `<strong>Frete Correios:</strong> ${baseFormatted} para 1 edição (+ ${addFormatted} por edição adicional no mesmo pedido).`
        : `<strong>Registered Airmail:</strong> ${baseFormatted} for 1 issue (+ ${addFormatted} per additional issue in the same order).`;
    }
    if (deadlineTextEl) {
      const deadline = edition.shippingDeadline || (isPt
        ? 'Envio via Impresso Registrado em até 3 dias úteis após a confirmação do pagamento.'
        : 'Dispatched via Registered Airmail within 3 business days after payment confirmation.');
      deadlineTextEl.innerHTML = isPt
        ? `<strong>Postagem:</strong> ${escapeHtml(deadline)}`
        : `<strong>Dispatch:</strong> ${escapeHtml(deadline)}`;
    }

    // Renderizar miniaturas da galeria
    const thumbsGrid = document.getElementById('edition-thumbs-grid');
    if (thumbsGrid) {
      thumbsGrid.innerHTML = editionPageState.photos.map((photo, idx) => `
        <button
          type="button"
          class="edition-thumb-btn ${idx === 0 ? 'is-active' : ''}"
          data-photo-index="${idx}"
          aria-pressed="${idx === 0 ? 'true' : 'false'}"
          aria-label="${escapeHtml(photo.caption)}"
        >
          <img src="${escapeHtml(photo.src)}" alt="${escapeHtml(photo.caption)}" loading="lazy">
          ${photo.isCover ? `<span class="edition-thumb-tag">${isPt ? 'Capa' : 'Cover'}</span>` : ''}
        </button>
      `).join('');
    }
    updateEditionGalleryView(0);

    // Descrição & Entrevistas
    const descEl = document.getElementById('edition-description-text');
    if (descEl) descEl.textContent = edition.description || '';

    const interviewsList = document.getElementById('edition-interviews-list');
    if (interviewsList && Array.isArray(edition.interviews)) {
      interviewsList.innerHTML = edition.interviews.map(item => `
        <li class="edition-interview-item">
          <span class="edition-interview-band">${escapeHtml(item.band)}</span>
          <span class="edition-interview-meta">${escapeHtml(item.country)} • ${escapeHtml(item.style)}</span>
        </li>
      `).join('');
    }

    const extrasList = document.getElementById('edition-extras-list');
    if (extrasList && Array.isArray(edition.extraContents)) {
      extrasList.innerHTML = edition.extraContents.map(text => `
        <li>${escapeHtml(text)}</li>
      `).join('');
    }

    // Especificações Técnicas
    const specsList = document.getElementById('edition-specs-list');
    if (specsList && edition.specs) {
      const specRows = isPt
        ? [
            { label: 'Formato / Dimensões', value: edition.specs.format },
            { label: 'Papel / Impressão', value: edition.specs.paper },
            { label: 'Idioma', value: edition.specs.language },
            { label: 'Tiragem', value: edition.specs.printRun },
            { label: 'Páginas', value: `${edition.pages} páginas` },
            { label: 'Lançamento', value: `${edition.releaseMonth} / ${edition.releaseYear} (${edition.zineYear})` }
          ]
        : [
            { label: 'Format / Dimensions', value: edition.specs.format },
            { label: 'Paper / Print', value: edition.specs.paper },
            { label: 'Language', value: edition.specs.language },
            { label: 'Print Run', value: edition.specs.printRun },
            { label: 'Pages', value: `${edition.pages} pages` },
            { label: 'Release', value: `${edition.releaseMonth} / ${edition.releaseYear} (${edition.zineYear})` }
          ];

      specsList.innerHTML = specRows.map(row => `
        <div class="edition-spec-row">
          <dt>${escapeHtml(row.label)}</dt>
          <dd>${escapeHtml(row.value)}</dd>
        </div>
      `).join('');
    }

    // Vídeo do YouTube incorporado
    const videoSection = document.getElementById('edition-video-section');
    const videoSlot = document.getElementById('edition-video-player-slot');
    const videoExternalLink = document.getElementById('edition-video-external-link');
    const videoSubtitle = document.getElementById('edition-video-subtitle');
    const videoId = extractYoutubeVideoId(edition.youtubeUrl);

    if (videoSection && videoSlot) {
      if (videoId) {
        videoSection.hidden = false;
        if (videoExternalLink) {
          videoExternalLink.href = edition.youtubeUrl;
        }
        if (videoSubtitle) {
          videoSubtitle.textContent = isPt
            ? 'Confira por dentro as páginas impressas, artes, diagramação e detalhes físicos desta edição diretamente no canal oficial do Armamento Bélico.'
            : 'Take a look inside the printed pages, artwork, layout and physical finish of this issue on the official Armamento Bélico YouTube channel.';
        }
        const playAria = isPt
          ? `Reproduzir vídeo: ${edition.title}`
          : `Play video: ${edition.title}`;
        videoSlot.innerHTML = `
          <div class="youtube-video-container" data-youtube-id="${escapeHtml(videoId)}" data-youtube-title="${escapeHtml(edition.title)}">
            <button type="button" class="youtube-facade-btn" aria-label="${escapeHtml(playAria)}">
              <img src="https://i.ytimg.com/vi/${encodeURIComponent(videoId)}/hqdefault.jpg" alt="${escapeHtml(edition.title)}" class="youtube-facade-thumb" loading="lazy">
              <span class="youtube-play-btn" aria-hidden="true"><i class="fa-solid fa-play"></i></span>
            </button>
          </div>
        `;
        setupYoutubeFacades(videoSlot);
      } else {
        videoSection.hidden = true;
        videoSlot.innerHTML = '';
      }
    }

    // Links de compartilhamento social
    const currentUrl = window.location.href;
    const shareText = isPt
      ? `Confira ${edition.title} — Armamento Bélico Zine:`
      : `Check out ${edition.title} — Armamento Bélico Zine:`;

    const waShare = document.getElementById('edition-share-whatsapp');
    const fbShare = document.getElementById('edition-share-facebook');
    const xShare = document.getElementById('edition-share-x');
    if (waShare) waShare.href = `https://wa.me/?text=${encodeURIComponent(`${shareText} ${currentUrl}`)}`;
    if (fbShare) fbShare.href = `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(currentUrl)}`;
    if (xShare) xShare.href = `https://twitter.com/intent/tweet?text=${encodeURIComponent(shareText)}&url=${encodeURIComponent(currentUrl)}`;

    // Navegação contextual (Anterior / Próxima)
    const prevTitleEl = document.getElementById('edition-nav-prev-title');
    const prevHintEl = document.getElementById('edition-nav-prev-hint');
    const prevLinkEl = document.getElementById('edition-nav-prev');
    const nextTitleEl = document.getElementById('edition-nav-next-title');
    const nextHintEl = document.getElementById('edition-nav-next-hint');

    if (edition.prevEdition && prevLinkEl && prevTitleEl && prevHintEl) {
      prevLinkEl.href = edition.prevEdition.url || 'arquivo.html';
      prevTitleEl.textContent = edition.prevEdition.title;
      prevHintEl.textContent = isPt ? 'Ver no catálogo geral' : 'View in full archive';
    }
    if (!edition.nextEdition && nextTitleEl && nextHintEl) {
      nextTitleEl.textContent = isPt ? 'Edição mais recente' : 'Latest release';
      nextHintEl.textContent = isPt ? 'Em breve nas prensas' : 'Coming soon to print';
    }
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

  // --- INICIALIZAÇÃO GERAL ---
  function init() {
    // 0. Marcar aba ativa no menu com base na rota atual
    updateActiveNavLink();

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

    // 5. Aplicar o estado salvo do LocalStorage
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
