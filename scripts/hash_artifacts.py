"""Writes a sha1-per-file manifest of the artifacts, so "the output did not change" is a diff of
two text files rather than a feeling.

    python scripts/hash_artifacts.py before.txt     # capture
    ... make the change, re-run the pedia ...
    python scripts/hash_artifacts.py after.txt
    diff before.txt after.txt                       # must be empty

This is the acceptance test for any refactor that is supposed to be output-neutral -- the
performance round in docs/performance.md was verified this way, 18132 files with 0 differences at
every step. `Main audit` answers a different question: it tracks a dozen metrics and would not
notice, say, one icon being cut from the wrong atlas.

Takes about 15s over ~750 MB.
"""
import hashlib
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TREES = ["output", "output_android", "json"]


def main(out_path):
    lines = []
    for tree in TREES:
        for folder, _, files in os.walk(os.path.join(ROOT, tree)):
            for name in sorted(files):
                path = os.path.join(folder, name)
                digest = hashlib.sha1()
                with open(path, "rb") as f:
                    for chunk in iter(lambda: f.read(1 << 20), b""):
                        digest.update(chunk)
                rel = os.path.relpath(path, ROOT).replace("\\", "/")
                lines.append(digest.hexdigest() + "  " + rel)
    lines.sort(key=lambda line: line[42:])          # by path, so the diff reads as a file list
    with open(out_path, "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(lines) + "\n")
    print(len(lines), "files hashed into", out_path)


if __name__ == "__main__":
    if len(sys.argv) != 2:
        sys.exit(__doc__)
    main(sys.argv[1])
