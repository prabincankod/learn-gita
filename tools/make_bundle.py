"""Build offline bundle for the Android app from geeta-correction data.
Curated MVP subset to keep APK small:
- all chapters + verses (sanskrit, transliteration, word meanings)
- translations: Sivananda EN, Ramsukhdas HI, Tejomayananda HI, Purohit EN
- commentaries: Sivananda EN, Ramsukhdas HI
Run: python3 tools/make_bundle.py
"""
import json, pathlib

SRC = pathlib.Path("/Users/prabinsubedi/work/prasuco/geeta-correction/data")
OUT = pathlib.Path("/Users/prabinsubedi/work/prasuco/gita-android/app/src/main/assets/gita_bundle.json")

TR_KEEP = {(16, "english"), (1, "hindi"), (17, "hindi"), (21, "english")}
CM_KEEP = {16: "Sivananda_EN", 1: "Ramsukhdas_HI"}
TR_NAMES = {16: "Sivananda_EN", 1: "Ramsukhdas_HI", 17: "Tejomayananda_HI", 21: "Purohit_EN"}

def main():
    chapters = json.loads((SRC/"chapters.json").read_text())
    verses = json.loads((SRC/"verse.json").read_text())
    translations = json.loads((SRC/"translation.json").read_text())
    commentaries = json.loads((SRC/"commentary.json").read_text())

    ch_out = []
    for c in sorted(chapters, key=lambda x: x["chapter_number"]):
        if c["chapter_number"] < 1 or c["chapter_number"] > 18:
            continue
        ch_out.append({
            "number": c["chapter_number"],
            "nameSanskrit": c.get("name", ""),
            "nameTransliterated": c.get("name_transliterated", ""),
            "nameTranslation": c.get("name_translation", ""),
            "meaning": c.get("name_meaning", ""),
            "summary": c.get("chapter_summary", ""),
            "versesCount": c.get("verses_count", 0),
        })

    v_out = []
    for v in sorted(verses, key=lambda x: x.get("verse_order", 0)):
        ch, vn = v["chapter_number"], v["verse_number"]
        if not (1 <= ch <= 18):
            continue
        v_out.append({
            "chapter": ch, "verse": vn, "order": v.get("verse_order", 0),
            "sanskrit": v.get("text", "").strip(),
            "transliteration": v.get("transliteration", ""),
            "wordMeanings": v.get("word_meanings", ""),
        })

    t_out = []
    for t in translations:
        if (t["author_id"], t["lang"]) not in TR_KEEP:
            continue
        src = TR_NAMES.get(t["author_id"], t["authorName"])
        # resolve chapter/verse from verse_id via verses lookup
        vid = t["verse_id"]
        vv = next((x for x in verses if x["id"] == vid), None)
        if not vv:
            continue
        t_out.append({"chapter": vv["chapter_number"], "verse": vv["verse_number"],
                      "source": src, "lang": t["lang"], "text": t["description"]})

    c_out = []
    for c in commentaries:
        if c["author_id"] not in CM_KEEP:
            continue
        vid = c.get("verse_id", c.get("verseNumber"))
        vv = next((x for x in verses if x["id"] == vid or x.get("verse_order") == vid), None)
        # commentary rows lack verse_id in some exports; fall back to verseNumber as order
        if not vv:
            vv = next((x for x in verses if x.get("verse_order") == c.get("verseNumber")), None)
        if not vv:
            continue
        c_out.append({"chapter": vv["chapter_number"], "verse": vv["verse_number"],
                      "source": CM_KEEP[c["author_id"]], "lang": "hindi" if c["author_id"] == 1 else "english",
                      "text": c["description"]})

    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps({"chapters": ch_out, "verses": v_out,
                               "translations": t_out, "commentaries": c_out}, ensure_ascii=False))
    print(f"chapters={len(ch_out)} verses={len(v_out)} translations={len(t_out)} commentaries={len(c_out)}")
    print(f"size={OUT.stat().st_size/1e6:.1f} MB -> {OUT}")

if __name__ == "__main__":
    main()
