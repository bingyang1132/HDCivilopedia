import os, re, sqlite3, collections, io, json, glob, sys
BS = chr(92)
WS = "E:/SteamLibrary/steamapps/workshop/content/289070"
MODS = "C:/Users/1132/Documents/My Games/Sid Meier's Civilization VI/Mods"
roots = [MODS, WS]
tags = set()
for lang in ('en_US','zh_Hans_CN'):
    for ch in os.listdir('json/%s' % lang):
        p = 'json/%s/%s/contents.json' % (lang, ch)
        if not os.path.isfile(p): continue
        def walk(o):
            if isinstance(o, dict):
                il = o.get('iconlabel')
                if isinstance(il, dict) and not il.get('src'):
                    a = il.get('alt') or ''
                    if a.startswith('ICON_'): tags.add(a)
                for v in o.values(): walk(v)
            elif isinstance(o, list):
                for x in o: walk(x)
        walk(json.load(io.open(p, encoding='utf-8')))
cur = sqlite3.connect('database/extra.sqlite').cursor()
atlas_files = collections.defaultdict(set); best = {}
for n, fn, r, c, sz in cur.execute("select Name, FileName, IconsPerRow, IconsPerColumn, IconSize from IconTextureAtlases"):
    atlas_files[n].add(os.path.splitext(fn)[0])
    if n not in best or sz > best[n][3]: best[n] = (fn, r, c, sz)
want, tagcount, examples = {}, collections.Counter(), collections.defaultdict(list)
for t in sorted(tags):
    for (a,) in cur.execute("select Atlas from IconDefinitions where lower(Name)=lower(?)", (t,)):
        if a in atlas_files:
            tagcount[a] += 1
            if len(examples[a]) < 3: examples[a].append(t)
            for b in atlas_files[a]: want[b.lower()] = a
blps = []
for r in roots:
    for dp, dn, fn in os.walk(r):
        for f in fn:
            if f.lower().endswith('.blp'): blps.append(os.path.join(dp, f))
pat = re.compile(rb'[A-Za-z0-9_]{6,}'); found = {}
for p in blps:
    try:
        with open(p,'rb') as fh:
            tail = b''
            while True:
                c = fh.read(1<<22)
                if not c: break
                for m in pat.finditer(tail+c):
                    s = m.group().decode('ascii','ignore').lower()
                    if s in want and s not in found: found[s] = p
                tail = c[-64:]
    except Exception: pass
def modroot(path):
    q = path.replace(BS, '/')
    for r in roots:
        if q.lower().startswith(r.lower()): return q[len(r)+1:].split('/')[0]
    return '?'
def modmeta(mod):
    base = os.path.join(WS, mod) if mod.isdigit() else os.path.join(MODS, mod)
    mi = glob.glob(os.path.join(base, "*.modinfo"))
    if not mi: return "?", "?"
    try: s = open(mi[0], encoding='utf-8', errors='ignore').read()
    except Exception: return "?", "?"
    def g(tag):
        m = re.search(r'<%s>(.*?)</%s>' % (tag, tag), s, re.S)
        return re.sub(r'\s+',' ', m.group(1)).strip() if m else "?"
    return g('Name'), g('Authors')
byauthor = collections.defaultdict(lambda: collections.defaultdict(set))
modnames = {}
for b, p in found.items():
    mod = modroot(p); nm, au = modmeta(mod); modnames[mod] = nm
    byauthor[au.split(';')[0].split(',')[0].strip() or '?'][mod].add(want[b])
out = io.open(r"C:/Users/1132/AppData/Local/Temp/claude/E--hdciv-hdcivilopedia/06b561b1-db06-4ebd-a53f-167aa09330ac/scratchpad/req_body.md", "w", encoding="utf-8")
rows = sorted(byauthor.items(), key=lambda kv: -sum(tagcount[a] for m in kv[1].values() for a in m))
for au, mods in rows:
    n = sum(tagcount[a] for m in mods.values() for a in m)
    natl = sum(len(m) for m in mods.values())
    out.write("\n## %s  —  %d 张图集, 影响 %d 处条目\n" % (au, natl, n))
    for mod, atl in sorted(mods.items(), key=lambda kv: -sum(tagcount[a] for a in kv[1])):
        src = ("创意工坊 " + mod) if mod.isdigit() else ("本地 " + mod)
        out.write("\n**%s** (%s)\n\n" % (modnames.get(mod,'?')[:60], src))
        out.write("| 图集名 | 需要的文件 | 尺寸 | 影响条目 | 例子 |\n|---|---|---|---|---|\n")
        for a in sorted(atl, key=lambda x: -tagcount[x]):
            fn, r, c, sz = best.get(a, ('?',0,0,0))
            out.write("| `%s` | `%s` | %d×%d 格 @%dpx (%d×%d) | %d | %s |\n"
                      % (a, fn, r, c, sz, r*sz, c*sz, tagcount[a], ', '.join(x.replace('ICON_','') for x in examples[a][:2])))

out.close()
