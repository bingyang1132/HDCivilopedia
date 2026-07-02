// Client-side pedia search. Reads window.SEARCH_INDEX (from ../../search-data.js),
// filters entries by title substring, and shows a dropdown of links.
// Entry shape: { t: title, u: link, i: icon (optional), c: category }.
// u and i are already relative to the page (../../../{lang}/...), used verbatim.
(function () {
    var input, results, items = [], shown = [], active = -1;

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

    function search(value) {
        var q = value.trim().toLowerCase();
        if (!q) {
            shown = [];
            render();
            return;
        }
        var out = [];
        for (var i = 0; i < items.length && out.length < 30; i++) {
            if (items[i].t.toLowerCase().indexOf(q) !== -1) {
                out.push(items[i]);
            }
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
