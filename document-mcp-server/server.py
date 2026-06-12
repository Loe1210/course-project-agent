import argparse

from mcp.server.fastmcp import FastMCP

from services.document_parser import parse_document
from services.docx_exporter import export_report_docx

mcp = FastMCP("课设通文档工具服务")


@mcp.tool()
def parse_document_tool(file_path: str) -> dict:
    """解析 docx/pdf/md/txt 文档并返回 Markdown 与纯文本内容。"""
    return parse_document(file_path)


@mcp.tool()
def export_report_docx_tool(topic: str, artifact_type: str, title: str, content: str, output_dir: str) -> dict:
    """将报告内容导出为 docx 文件并返回文件信息。"""
    return export_report_docx(topic, artifact_type, title, content, output_dir)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="课设通文档 MCP 服务")
    parser.add_argument("--transport", default="sse", choices=["stdio", "sse", "streamable-http"])
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=8000)
    parser.add_argument("--log-level", default="INFO")
    return parser.parse_args()


if __name__ == "__main__":
    args = parse_args()
    mcp.settings.host = args.host
    mcp.settings.port = args.port
    mcp.settings.log_level = args.log_level.upper()
    mcp.run(transport=args.transport)
