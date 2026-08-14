// Client-side pedia search. Reads window.SEARCH_INDEX (from ../../search-data.js), ranks
// entries against the query, and shows a dropdown of links.
// Entry shape: { t: title, u: link, i: icon (optional), c: category, p: pinyin (optional) }.
// u and i are already relative to the page (../../../{lang}/...), used verbatim.
//
// p holds space separated syllables ("ji chang", see tools/Pinyin.java), which is what makes
// three different latin queries work for 机场: the syllables joined ("jichang"), their first
// letters ("jc"), and a match starting at any syllable, so "jichang" also finds 国际机场.
(function () {
    var input, results, items = [], shown = [], active = -1, prepared = false;

    function ready(fn) {
        if (document.readyState !== "loading") fn();
        else document.addEventListener("DOMContentLoaded", fn);
    }

    function render() {
        results.innerHTML = "";
        active = -1;
        if (!shown.length) {
            results.style.display = "none";
            return;
        }
        for (var i = 0; i < shown.length; i++) {
            var it = shown[i];
            var a = document.createElement("a");
            a.className = "pediaSearchItem";
            a.href = it.u;
            if (it.i) {
                var img = document.createElement("img");
                img.className = "pediaSearchIcon";
                img.src = it.i;
                img.alt = "";
                a.appendChild(img);
            }
            var title = document.createElement("span");
            title.className = "pediaSearchTitle";
            title.textContent = it.t;
            a.appendChild(title);
            if (it.c) {
                var cat = document.createElement("span");
                cat.className = "pediaSearchCat";
                cat.textContent = it.c;
                a.appendChild(cat);
            }
            results.appendChild(a);
        }
        results.style.display = "block";
    }

    // Derived match fields, built once on the first keystroke rather than at load: the index is
    // parsed on every page anyway, no reason to also pay for this on pages nobody searches from.
    function prepare() {
        prepared = true;
        for (var i = 0; i < items.length; i++) {
            var e = items[i];
            e.tl = e.t.toLowerCase();
            if (!e.p) continue;
            var parts = e.p.split(" ");
            var initials = "", starts = [], at = 0;
            for (var j = 0; j < parts.length; j++) {
                initials += parts[j].charAt(0);
                starts.push(at);
                at += parts[j].length;
            }
            e.pf = parts.join("");   // syllables joined, what a whole-pinyin query looks like
            e.pi = initials;
            e.ps = starts;           // syllable offsets into pf
        }
    }

    function startsAt(haystack, needle, at) {
        return haystack.lastIndexOf(needle, at) === at;
    }

    // Lower is better; -1 means no match. The tiers are the whole point of ranking: before this
    // the first 30 entries in index order won, so an exact title could lose to a page that merely
    // contained the word.
    function rank(e, qt, qp) {
        if (e.tl === qt) return 0;
        if (startsAt(e.tl, qt, 0)) return 1;
        if (e.tl.indexOf(qt) !== -1) return 2;
        if (qp && e.pf) {
            if (startsAt(e.pf, qp, 0)) return 3;
            if (startsAt(e.pi, qp, 0)) return 4;
            // from a later syllable, so "jichang" finds 国际机场 but "ang" still matches nothing
            for (var i = 1; i < e.ps.length; i++) {
                if (startsAt(e.pf, qp, e.ps[i])) return 5;
            }
        }
        return -1;
    }

    function search(value) {
        var qt = value.trim().toLowerCase();
        if (!qt) {
            shown = [];
            render();
            return;
        }
        if (!prepared) prepare();
        // the pinyin form drops spaces so "ji chang" works, and folds v to u because the tables
        // write ü as u while input methods take either (lv / lu for 绿)
        var qp = qt.replace(/[\s']+/g, "").replace(/v/g, "u");
        var hits = [];
        for (var i = 0; i < items.length; i++) {
            var score = rank(items[i], qt, qp);
            if (score >= 0) hits.push({ e: items[i], s: score, i: i });
        }
        // shorter titles first within a tier: for "机场" that puts 机场 above 国际机场
        hits.sort(function (a, b) {
            return (a.s - b.s) || (a.e.t.length - b.e.t.length) || (a.i - b.i);
        });
        var out = [];
        for (var k = 0; k < hits.length && out.length < 30; k++) {
            out.push(hits[k].e);
        }
        shown = out;
        render();
    }

    function highlight(n) {
        var nodes = results.querySelectorAll(".pediaSearchItem");
        if (!nodes.length) return;
        if (active >= 0 && nodes[active]) nodes[active].classList.remove("pediaSearchItemActive");
        active = (n + nodes.length) % nodes.length;
        nodes[active].classList.add("pediaSearchItemActive");
        nodes[active].scrollIntoView({ block: "nearest" });
    }

    ready(function () {
        input = document.getElementById("pediaSearchInput");
        results = document.getElementById("pediaSearchResults");
        if (!input || !results) return;
        items = window.SEARCH_INDEX || [];

        input.addEventListener("input", function () { search(input.value); });
        input.addEventListener("focus", function () { if (input.value.trim()) search(input.value); });
        input.addEventListener("keydown", function (e) {
            var nodes = results.querySelectorAll(".pediaSearchItem");
            if (e.key === "ArrowDown") { e.preventDefault(); highlight(active + 1); }
            else if (e.key === "ArrowUp") { e.preventDefault(); highlight(active - 1); }
            else if (e.key === "Enter") {
                if (active >= 0 && nodes[active]) window.location.href = nodes[active].href;
                else if (nodes.length) window.location.href = nodes[0].href;
            } else if (e.key === "Escape") {
                input.value = "";
                shown = [];
                render();
                input.blur();
            }
        });

        document.addEventListener("click", function (e) {
            if (!input.contains(e.target) && !results.contains(e.target)) {
                results.style.display = "none";
            }
        });
    });
})();
