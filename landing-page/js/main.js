(() => {
  const header = document.querySelector("#header");
  const toggle = document.querySelector(".nav-toggle");
  const mobileNav = document.querySelector("#mobile-nav");
  const year = document.querySelector("#year");

  // Product app origin (CleanTrack-style: login opens the authenticated app).
  // Override via <meta name="ehealth-app-origin" content="https://…"> if needed.
  const metaOrigin = document.querySelector('meta[name="ehealth-app-origin"]');
  const APP_ORIGIN = (
    metaOrigin?.getAttribute("content") ||
    window.EHEALTH_APP_ORIGIN ||
    window.location.origin
  ).replace(/\/$/, "");

  const loginUrl = `${APP_ORIGIN}/login`;

  document.querySelectorAll('[href="/login"], #btn-login-nav, #btn-login-mobile, #btn-login-hero, #btn-login-cta, #btn-login-footer')
    .forEach((el) => {
      if (el instanceof HTMLAnchorElement) el.href = loginUrl;
    });

  if (year) year.textContent = String(new Date().getFullYear());

  const onScroll = () => {
    if (!header) return;
    header.classList.toggle("scrolled", window.scrollY > 20);
  };
  onScroll();
  window.addEventListener("scroll", onScroll, { passive: true });

  if (toggle && mobileNav) {
    const setMenuOpen = (open) => {
      if (open) {
        mobileNav.removeAttribute("hidden");
      } else {
        mobileNav.setAttribute("hidden", "");
      }
      toggle.setAttribute("aria-expanded", String(open));
      toggle.setAttribute("aria-label", open ? "Fermer le menu" : "Ouvrir le menu");
    };

    toggle.addEventListener("click", () => {
      const open = mobileNav.hasAttribute("hidden");
      setMenuOpen(open);
    });

    mobileNav.querySelectorAll("a").forEach((link) => {
      link.addEventListener("click", () => setMenuOpen(false));
    });

    window.matchMedia("(min-width: 900px)").addEventListener("change", (e) => {
      if (e.matches) setMenuOpen(false);
    });
  }

  const reveals = document.querySelectorAll(".reveal");
  const staggerGroups = document.querySelectorAll(".reveal-stagger");

  if ("IntersectionObserver" in window) {
    const io = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add("visible");
            io.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.12, rootMargin: "0px 0px -6% 0px" }
    );
    reveals.forEach((el) => io.observe(el));

    const staggerIo = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add("is-visible");
            staggerIo.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.1 }
    );
    staggerGroups.forEach((el) => staggerIo.observe(el));
  } else {
    reveals.forEach((el) => el.classList.add("visible"));
    staggerGroups.forEach((el) => el.classList.add("is-visible"));
  }

  // Signup flow (signup.html)
  const signupForm = document.querySelector("#signup-form");
  const signupStepInfo = document.querySelector("#signup-step-info");
  const signupStepPlan = document.querySelector("#signup-step-plan");
  const signupSuccess = document.querySelector("#signup-success");
  const stepBars = document.querySelectorAll(".signup-steps span");
  const note = document.querySelector("#signup-note");

  const params = new URLSearchParams(window.location.search);
  const presetPlan = params.get("plan");
  if (presetPlan) {
    const radio = document.querySelector(`input[name="plan"][value="${presetPlan}"]`);
    if (radio instanceof HTMLInputElement) radio.checked = true;
  }

  function setSignupNote(text, kind) {
    if (!note) return;
    note.textContent = text;
    note.classList.remove("is-success", "is-error");
    if (kind) note.classList.add(kind);
  }

  function showStep(step) {
    if (signupStepInfo) signupStepInfo.hidden = step !== "info";
    if (signupStepPlan) signupStepPlan.hidden = step !== "plan";
    if (signupSuccess) signupSuccess.hidden = step !== "done";
    stepBars.forEach((bar, i) => {
      const on =
        (step === "info" && i === 0) ||
        (step === "plan" && i <= 1) ||
        (step === "done" && i <= 2);
      bar.classList.toggle("is-on", on);
    });
  }

  const draft = {
    organization: "",
    adminName: "",
    adminEmail: "",
    plan: presetPlan || "free",
  };

  document.querySelector("#signup-to-plan")?.addEventListener("click", () => {
    const org = String(document.querySelector("#organization")?.value || "").trim();
    const name = String(document.querySelector("#admin-name")?.value || "").trim();
    const email = String(document.querySelector("#admin-email")?.value || "").trim();
    if (!org || !name || !email) {
      setSignupNote("Merci de remplir tous les champs.", "is-error");
      return;
    }
    draft.organization = org;
    draft.adminName = name;
    draft.adminEmail = email;
    setSignupNote("");
    showStep("plan");
  });

  document.querySelector("#signup-back-info")?.addEventListener("click", () => {
    setSignupNote("");
    showStep("info");
  });

  if (signupForm) {
    showStep("info");
    signupForm.addEventListener("submit", (e) => {
      e.preventDefault();
      const plan = String(
        document.querySelector('input[name="plan"]:checked')?.value || "free"
      );
      draft.plan = plan;

      try {
        localStorage.setItem(
          "ehealth.signup.draft",
          JSON.stringify({ ...draft, createdAt: new Date().toISOString() })
        );
      } catch {
        /* ignore quota */
      }

      const summary = document.querySelector("#signup-summary");
      if (summary) {
        summary.textContent =
          plan === "free"
            ? `Votre espace « ${draft.organization} » est prêt. Connectez-vous pour découvrir eHealth.`
            : `Demande enregistrée pour « ${draft.organization} ». Nous finalisons l’activation — vous pouvez déjà préparer la connexion.`;
      }
      showStep("done");
    });
  }
})();
