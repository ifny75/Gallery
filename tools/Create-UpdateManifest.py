"""Generate the release contract from the exact signed APK being uploaded."""
import argparse, hashlib, json, pathlib, re, subprocess
p = argparse.ArgumentParser()
p.add_argument("--apk", required=True, type=pathlib.Path)
p.add_argument("--aapt2", required=True)
p.add_argument("--variant", required=True)
p.add_argument("--output", type=pathlib.Path, default=pathlib.Path("gallery-update.json"))
a = p.parse_args()
badging = subprocess.check_output([a.aapt2, "dump", "badging", str(a.apk)], text=True, encoding="utf-8")
m = re.search(r"package: name='([^']+)' versionCode='(\d+)' versionName='([^']+)'", badging)
if not m: raise SystemExit("Cannot inspect APK package/version")
a.output.write_text(json.dumps({"schema":1,"updates":[{"applicationId":m[1],"variant":a.variant,
    "versionCode":int(m[2]),"versionName":m[3],"apk":a.apk.name,
    "sha256":hashlib.sha256(a.apk.read_bytes()).hexdigest()}]}, indent=2)+"\n", encoding="utf-8")
print(a.output)
