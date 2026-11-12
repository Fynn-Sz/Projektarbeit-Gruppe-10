// Kleine Helfer fuer die Oberflaeche. Alle Seiten funktionieren auch ohne JavaScript.
document.addEventListener("DOMContentLoaded", () => {

    // Mobiles Menue auf- und zuklappen
    const schalter = document.querySelector(".nav-schalter");
    const navigation = document.getElementById("hauptnavigation");
    if (schalter && navigation) {
        schalter.addEventListener("click", () => {
            const offen = navigation.classList.toggle("offen");
            schalter.setAttribute("aria-expanded", String(offen));
        });
    }

    // Sicherheitsabfrage: <form data-rueckfrage="Text" data-rueckfrage-titel="..." data-rueckfrage-knopf="...">
    const dialog = document.getElementById("rueckfrage");
    document.querySelectorAll("form[data-rueckfrage]").forEach((formular) => {
        formular.addEventListener("submit", (ereignis) => {
            if (formular.dataset.bestaetigt === "ja") {
                return;
            }
            ereignis.preventDefault();
            if (!dialog || typeof dialog.showModal !== "function") {
                if (window.confirm(formular.dataset.rueckfrage)) {
                    formular.dataset.bestaetigt = "ja";
                    formular.submit();
                }
                return;
            }
            dialog.querySelector("#rueckfrage-titel").textContent = formular.dataset.rueckfrageTitel || "Sind Sie sicher?";
            dialog.querySelector("#rueckfrage-text").textContent = formular.dataset.rueckfrage;
            dialog.querySelector("#rueckfrage-ok").textContent = formular.dataset.rueckfrageKnopf || "Ja, fortfahren";
            dialog.returnValue = "";
            dialog.showModal();
            dialog.addEventListener("close", () => {
                if (dialog.returnValue === "ok") {
                    formular.dataset.bestaetigt = "ja";
                    formular.submit();
                }
            }, { once: true });
        });
    });

    // Zeitraum: "bis" darf nicht vor "von" liegen - Mindestdatum mitziehen
    document.querySelectorAll("[data-zeitraum]").forEach((bereich) => {
        const von = bereich.querySelector("input[name='von']");
        const bis = bereich.querySelector("input[name='bis']");
        if (!von || !bis) {
            return;
        }
        const angleichen = () => {
            if (von.value) {
                bis.min = von.value;
                if (bis.value && bis.value < von.value) {
                    bis.value = von.value;
                }
            }
        };
        von.addEventListener("change", angleichen);
        angleichen();
    });

    // Datei-Upload: gewaehlten Dateinamen anzeigen, Drag & Drop hervorheben
    document.querySelectorAll(".ablage").forEach((ablage) => {
        const eingabe = ablage.querySelector("input[type='file']");
        const anzeige = ablage.querySelector(".ablage-datei");
        if (!eingabe) {
            return;
        }
        eingabe.addEventListener("change", () => {
            if (anzeige) {
                anzeige.textContent = eingabe.files.length ? eingabe.files[0].name : "";
            }
        });
        ["dragenter", "dragover"].forEach((art) => ablage.addEventListener(art, () => ablage.classList.add("ziehen")));
        ["dragleave", "drop"].forEach((art) => ablage.addEventListener(art, () => ablage.classList.remove("ziehen")));
    });

    // Drucken-Knopf auf der Bestaetigungsseite
    document.querySelectorAll("[data-drucken]").forEach((knopf) => {
        knopf.addEventListener("click", () => window.print());
    });
});
