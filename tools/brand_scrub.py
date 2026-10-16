from pathlib import Path
import re

root=Path("app/src")
text_ext={".java",".xml",".gradle",".properties",".txt",".json",".md",".kt"}
replacements=[
 (re.compile(r"ludo[ _-]*king",re.I),"Ludo Masters"),
 (re.compile(r"clone",re.I),"app"),
 (re.compile(r"listwinner[123]crown",re.I),"lm_shield"),
 (re.compile(r"winnercrown",re.I),"lm_shield"),
 (re.compile(r"crown",re.I),"shield")
]
for path in root.rglob("*"):
    if not path.is_file() or path.suffix.lower() not in text_ext:continue
    try:s=path.read_text(encoding="utf-8")
    except UnicodeError:continue
    new=s
    for pattern,replace in replacements:new=pattern.sub(replace,new)
    if new!=s:path.write_text(new,encoding="utf-8")
for path in (root/"main/res/drawable").glob("*crown*"):
    if path.is_file():path.unlink()
# Do not alter legitimate project repo URL; clean only application resources.
bad=[]
for p in root.rglob("*"):
    if p.is_file() and p.suffix.lower() in text_ext:
        try:
            if re.search(r"ludo[ _-]*king|clone|crown",p.read_text(encoding="utf-8"),re.I):bad.append(str(p))
        except UnicodeError:pass
    if p.is_file() and re.search(r"ludo[ _-]*king|clone|crown",p.name,re.I):bad.append(str(p))
if bad:raise SystemExit("Forbidden legacy branding remains: "+repr(bad))
print("Source and resource names audited; outdated winner bitmaps removed.")
