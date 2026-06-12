import json
import sys

from services.document_parser import parse_document
from services.docx_exporter import export_report_docx


def main() -> int:
    if hasattr(sys.stdin, "reconfigure"):
        sys.stdin.reconfigure(encoding="utf-8")
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")

    if len(sys.argv) < 2:
        return write_error("未指定文档工具动作")

    action = sys.argv[1]
    try:
        payload = json.loads(sys.stdin.read() or "{}")
    except json.JSONDecodeError as exc:
        return write_error(f"文档工具请求体不是合法 JSON：{exc}")

    try:
        if action == "parse_document":
            result = parse_document(payload.get("file_path", ""))
        elif action == "export_report_docx":
            result = export_report_docx(
                payload.get("topic", ""),
                payload.get("artifact_type", ""),
                payload.get("title", ""),
                payload.get("content", ""),
                payload.get("output_dir", ""),
            )
        else:
            return write_error(f"不支持的文档工具动作：{action}")
    except Exception as exc:
        return write_error(str(exc))

    print(json.dumps({"success": True, **result}, ensure_ascii=False))
    return 0


def write_error(message: str) -> int:
    print(json.dumps({"success": False, "error": message}, ensure_ascii=False))
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
