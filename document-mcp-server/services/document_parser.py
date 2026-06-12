from pathlib import Path

from markitdown import MarkItDown


def parse_document(file_path: str) -> dict:
    path = Path(file_path).resolve()
    if not path.exists() or not path.is_file():
        raise FileNotFoundError(f"待解析文档不存在：{path}")

    result = MarkItDown().convert(str(path))
    title = getattr(result, "title", "") or path.name
    markdown = getattr(result, "markdown", "") or ""
    text_content = getattr(result, "text_content", "") or markdown
    if not text_content.strip():
        raise ValueError("文档解析结果为空，无法写入知识库")

    return {
        "title": title,
        "markdown": markdown,
        "text_content": text_content,
        "file_type": path.suffix.lstrip(".").lower(),
    }
