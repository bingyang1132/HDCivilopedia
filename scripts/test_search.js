// Tests the shipped search behaviour: manual/output/search.js is loaded as-is and driven through
// a minimal fake DOM, against a fixture index rather than the generated one (output/ is not in the
// repo, and a test that needs a full pedia run is a test nobody runs).
//
//     node scripts/test_search.js
//
// The ranking is what these pin down. Before it existed the first 30 entries in index order won,
// so an exact hit could lose to a substring hit that happened to be indexed earlier -- and the
// pinyin tiers have several ways to be subtly wrong: initials matching where they should not,
// mid-syllable matches, ü spelled lv or lu.

const fs = require("fs");
const path = require("path");

const ROOT = path.dirname(__dirname);

// p is what tools/Pinyin.java would produce for each title
const INDEX = [
    { t: "机场", c: "建筑", u: "a.html", p: "ji chang" },
    { t: "竞技场", c: "建筑", u: "b.html", p: "jing ji chang" },
    { t: "国际机场", c: "奇观", u: "c.html", p: "guo ji ji chang" },
    { t: "银行", c: "建筑", u: "d.html", p: "yin hang" },
    { t: "银行业", c: "市政", u: "e.html", p: "yin hang ye" },
    { t: "绿色都市", c: "市政", u: "f.html", p: "lu se du shi" },
    { t: "长城", c: "奇观", u: "g.html", p: "chang cheng" },
    { t: "战斗机", c: "单位", u: "h.html", p: "zhan dou ji" },
    { t: "P-51战斗机", c: "单位", u: "i.html", p: "p-51 zhan dou ji" },
    { t: "Airport", c: "建筑", u: "j.html" },
];

function fakeDom() {
    const handlers = {};
    const node = () => ({
        style: {}, className: "", textContent: "", href: "", value: "",
        appendChild(child) { (this.kids = this.kids || []).push(child); },
        setAttribute() {}, contains() { return false; },
        classList: { add() {}, remove() {}, contains() { return false; } },
        querySelectorAll: () => [],
        addEventListener(type, fn) { handlers[type] = fn; },
    });

    const input = node();
    const results = node();
    results.items = [];
    results.appendChild = child => results.items.push(child);
    Object.defineProperty(results, "innerHTML", {
        set() { results.items = []; }, get() { return ""; },
    });

    global.window = {};
    global.document = {
        readyState: "complete",
        getElementById: id => (id === "pediaSearchInput" ? input : results),
        createElement: () => node(),
        addEventListener() {},
    };
    return { input, handlers, results };
}

const dom = fakeDom();
global.window.SEARCH_INDEX = INDEX;
new Function(fs.readFileSync(path.join(ROOT, "manual/output/search.js"), "utf8"))();

function query(q) {
    dom.input.value = q;
    dom.handlers["input"]();
    // each result is an <a> holding an optional <img>, a title span and a category span
    return dom.results.items.map(a => a.kids.filter(k => k.className === "pediaSearchTitle")[0].textContent);
}

const cases = [
    ["机场", ["机场", "国际机场"], "标题精确命中排在子串命中之前"],
    ["jichang", ["机场", "竞技场", "国际机场"], "全拼精确 → 后续音节起点，同档短标题在前"],
    ["jc", ["机场"], "首字母；竞技场是 jjc，不应命中"],
    ["yinhang", ["银行", "银行业"], "多音字词 银行，前缀同档按长度"],
    ["yinhangye", ["银行业"], "更长的全拼只命中一条"],
    ["lvsedushi", ["绿色都市"], "ü 写成 lv"],
    ["lusedushi", ["绿色都市"], "ü 写成 lu"],
    ["ji chang", ["机场", "竞技场", "国际机场"], "查询里的空格被忽略"],
    ["zhandouji", ["战斗机", "P-51战斗机"], "从中间音节起匹配"],
    ["airport", ["Airport"], "英文标题不区分大小写，且没有 p 字段也能搜"],
    ["ang", [], "不命中音节中段（长城 是 chang cheng）"],
    ["", [], "空查询不出结果"],
];

let failed = 0;
for (const [q, want, note] of cases) {
    const got = query(q);
    const ok = got.length === want.length && got.every((t, i) => t === want[i]);
    if (!ok) {
        failed++;
        console.error(`FAIL  ${JSON.stringify(q)}  (${note})\n  want ${JSON.stringify(want)}\n  got  ${JSON.stringify(got)}`);
    } else {
        console.log(`ok    ${JSON.stringify(q)}  (${note})`);
    }
}

if (failed) {
    console.error(`\n${failed} of ${cases.length} failed`);
    process.exit(1);
}
console.log(`\n${cases.length} passed`);
