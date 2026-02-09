---
name: springfood-mcp
description: MCP Server configuration for SpringFood project integration
---

# MCP (Model Context Protocol) Setup

Dự án này đã được cấu hình với các MCP servers để tăng cường khả năng của Antigravity.

## 📋 Configured MCP Servers

### 1. IntelliJ MCP Server (Currently Disabled)
- **Location**: `.kiro/settings/mcp.json`
- **Status**: Disabled (set `"disabled": false` to enable)
- **URL**: `http://localhost:64342/sse`
- **Purpose**: Integrate with IntelliJ IDEA for code navigation and analysis

**To Enable:**
1. Open IntelliJ IDEA
2. Go to Tools > MCP Server
3. Enable MCP Server (checkbox)
4. Update mcp.json: set `"disabled": false`

### 2. Serena MCP Server (Enabled)
- **Location**: `.kiro/settings/mcp.json`
- **Status**: Enabled ✅
- **Command**: `uv run serena start-mcp-server`
- **Purpose**: Semantic code search and intelligent editing

**Capabilities:**
- `find_symbol` - Find symbols by name
- `find_referencing_symbols` - Find references
- `get_symbol_definition` - Get symbol definitions
- `search_for_pattern` - Pattern search in code
- `list_dir`, `read_file` - File operations

## 🔧 Activating MCP Tools

### For Serena:
Ensure you have `uv` installed:
```bash
# Install uv (if not already)
# Windows (PowerShell)
irm https://astral.sh/uv/install.ps1 | iex

# Or via pip
pip install uv
```

### Auto-Approved Tools
These tools run automatically without confirmation:
- `find_symbol`
- `find_referencing_symbols`
- `get_symbol_definition`
- `list_files`, `read_file`, `list_dir`
- `search_for_pattern`
- `onboarding`
- `write_memory`

## 📁 Related Files
- `.kiro/settings/mcp.json` - MCP server configuration
- `.serena/project.yml` - Serena project settings
- `.serena/memories/` - Serena memory store

## 💡 Usage Tips

### Using Serena for Code Search
Ask the AI to:
- "Find all classes that implement UserRepository"
- "Search for usages of OrderService"
- "Find symbol: ProductController"

### Creating Memories
Serena can store project knowledge:
- Architecture decisions
- Important patterns
- Onboarding notes

This helps maintain context across conversations.
