#!/usr/bin/env python3
"""Smoke-test the Lume Android app on a booted emulator using adb + UI hierarchy."""
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
from pathlib import Path

ADB = ["adb"]
PACKAGE = "br.com.cacadorreader"
ACTIVITY = f"{PACKAGE}/.MainActivity"


def run(*args, timeout=30):
    return subprocess.run(ADB + list(args), check=True, text=True,
                          stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                          timeout=timeout).stdout


def hierarchy():
    run("shell", "uiautomator", "dump", "/sdcard/lume-window.xml", timeout=30)
    raw = run("shell", "cat", "/sdcard/lume-window.xml")
    start = raw.find("<?xml")
    if start < 0:
        start = raw.find("<hierarchy")
    if start < 0:
        raise RuntimeError("Não foi possível ler a árvore de acessibilidade da tela.")
    return ET.fromstring(raw[start:])


def find_node(needles):
    root = hierarchy()
    for node in root.iter("node"):
        text = node.attrib.get("text", "")
        desc = node.attrib.get("content-desc", "")
        if any(n.casefold() in text.casefold() or n.casefold() in desc.casefold()
               for n in needles):
            bounds = node.attrib.get("bounds", "")
            if bounds:
                return text or desc, bounds
    return None


def tap_label(*needles, required=True):
    for _ in range(8):
        found = find_node(needles)
        if found:
            label, bounds = found
            nums = [int(n) for n in __import__("re").findall(r"-?\d+", bounds)]
            if len(nums) != 4:
                raise RuntimeError(f"Limites inválidos para {label}: {bounds}")
            x = (nums[0] + nums[2]) // 2
            y = (nums[1] + nums[3]) // 2
            run("shell", "input", "tap", str(x), str(y))
            time.sleep(1.2)
            print(f"PASSOU: toque em '{label}'")
            return True
        time.sleep(0.8)
    if required:
        raise AssertionError(f"Elemento não encontrado na tela: {', '.join(needles)}")
    return False


def assert_visible(*needles):
    found = find_node(needles)
    if not found:
        raise AssertionError(f"Tela não apresentou o elemento esperado: {', '.join(needles)}")
    print(f"PASSOU: elemento visível '{found[0]}'")


def main():
    apk = Path(sys.argv[1] if len(sys.argv) > 1 else
               "cacador-reader/app/build/outputs/apk/debug/app-debug.apk")
    if not apk.is_file():
        raise FileNotFoundError(f"APK não encontrado: {apk}")

    run("wait-for-device", timeout=90)
    run("install", "-r", str(apk), timeout=120)
    run("shell", "am", "force-stop", PACKAGE)
    run("shell", "am", "start", "-n", ACTIVITY)
    time.sleep(3)

    # The welcome dialog appears only on first launch; dismiss it if present.
    tap_label("Começar", required=False)
    assert_visible("Lume")
    tap_label("Voz e leitura")
    assert_visible("Velocidade da fala")
    assert_visible("Testar a voz")
    tap_label("Voltar")
    assert_visible("Voz e leitura")
    print("RESULTADO: todos os testes básicos de abertura e navegação passaram.")


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print(f"FALHA NO TESTE DO EMULADOR: {exc}", file=sys.stderr)
        sys.exit(1)
