#!/usr/bin/env python3
"""Preflight checks for the Lume Android accessibility service.

This static inspector catches common wiring/regression errors before Gradle
starts compiling. It does not replace device/emulator gesture tests.
"""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "cacador-reader" / "app"
SERVICE = APP / "src/main/java/br/com/cacadorreader/CacadorAccessibilityService.java"
CONFIG = APP / "src/main/res/xml/accessibility_service_config.xml"
MANIFEST = APP / "src/main/AndroidManifest.xml"
GRADLE = APP / "build.gradle"
checks = []
errors = []

def check(name, condition, detail=""):
    checks.append(name)
    if condition:
        print(f"PASS  {name}")
    else:
        print(f"FAIL  {name}" + (f" — {detail}" if detail else ""))
        errors.append(name)

def read(path):
    if not path.is_file():
        check(f"Arquivo existe: {path.relative_to(ROOT)}", False)
        return ""
    check(f"Arquivo existe: {path.relative_to(ROOT)}", True)
    return path.read_text(encoding="utf-8")

service = read(SERVICE)
config_text = read(CONFIG)
manifest_text = read(MANIFEST)
gradle_text = read(GRADLE)

# XML must be well formed, not merely contain expected text.
for path, label in ((CONFIG, "XML de configuração do serviço"),
                    (MANIFEST, "Manifesto Android")):
    try:
        ET.parse(path)
        check(f"{label} é XML válido", True)
    except (ET.ParseError, OSError) as exc:
        check(f"{label} é XML válido", False, str(exc))

check("Serviço declara onAccessibilityEvent", "onAccessibilityEvent(AccessibilityEvent event)" in service)
check("Serviço declara onGesture", "onGesture(int gestureId)" in service)
for label, token in (
    ("Gesto direita", "case GESTURE_SWIPE_RIGHT"),
    ("Gesto esquerda", "case GESTURE_SWIPE_LEFT"),
    ("Gesto toque duplo", "case GESTURE_DOUBLE_TAP"),
    ("Busca de foco Android", "focusSearch("),
    ("Alternativa de navegação na árvore", "collectNavigableNodes("),
    ("Ação de ativação", "ACTION_CLICK"),
    ("Ação de foco de acessibilidade", "ACTION_ACCESSIBILITY_FOCUS"),
):
    check(label + " implementado", token in service)

check("Configuração permite gestos", 'android:canPerformGestures="true"' in config_text)
check("Configuração solicita exploração por toque", 'android:canRequestTouchExplorationMode="true"' in config_text)
check("Configuração declara suporte ao toque duplo", "flagServiceHandlesDoubleTap" in config_text)
check("Manifesto registra serviço de acessibilidade", "android.accessibilityservice.AccessibilityService" in manifest_text)
check("Manifesto protege serviço com BIND_ACCESSIBILITY_SERVICE", "android.permission.BIND_ACCESSIBILITY_SERVICE" in manifest_text)
check("Manifesto aponta para XML de configuração", "@xml/accessibility_service_config" in manifest_text)
check("ID do pacote configurado", "applicationId 'br.com.cacadorreader'" in gradle_text)
check("Versão Android SDK definida", "compileSdk 35" in gradle_text)

print(f"\nResultado: {len(checks) - len(errors)}/{len(checks)} verificações passaram.")
if errors:
    print("A inspeção falhou. A compilação deve ser interrompida até corrigir os itens acima.")
    sys.exit(1)
print("Inspeção estática aprovada. Ainda é necessário executar testes em emulador/aparelho para validar gestos reais.")
