from mcp.server.fastmcp import FastMCP

from services.document_parser import parse_document
from services.docx_exporter import export_report_docx

mcp = FastMCP("课设通文档工具服务")


@mcp.tool
def parse_document_tool(file_path: str) -> dict:
    """解析 docx/pdf/md/txt 文档并返回 Markdown 与纯文本内容。"""
    return parse_document(file_path)


@mcp.tool
def export_report_docx_tool(topic: str, artifact_type: str, title: str, content: str, output_dir: str) -> dict:
    """将报告内容导出为 docx 文件并返回文件信息。"""
    return export_report_docx(topic, artifact_type, title, content, output_dir)


if __name__ == "__main__":
    mcp.run(transport="stdio")
