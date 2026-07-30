#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

"$ROOT_DIR/scripts/verify-license.sh"
"$ROOT_DIR/scripts/verify-gradle-wrapper.sh"
"$ROOT_DIR/scripts/verify-workflows.sh"
"$ROOT_DIR/scripts/verify-architecture.sh"
"$ROOT_DIR/scripts/verify-core-standalone.sh"
"$ROOT_DIR/scripts/verify-publishing-config.sh"
"$ROOT_DIR/scripts/test-release-bundle.sh"
"$ROOT_DIR/scripts/verify-documentation.sh"

python3 - "$ROOT_DIR" <<'PY'
from pathlib import Path
import sys
import xml.etree.ElementTree as ET
try:
    import yaml
except ImportError:
    yaml = None

root = Path(sys.argv[1])
for path in root.rglob("*.xml"):
    if any(part in {"build", ".gradle"} for part in path.parts):
        continue
    ET.parse(path)

if yaml:
    for path in (root / ".github" / "workflows").glob("*.yml"):
        yaml.safe_load(path.read_text(encoding="utf-8"))

print("Repository XML and workflow syntax verification passed.")
PY
