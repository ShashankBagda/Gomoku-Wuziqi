# Git Statistics Scripts

This folder contains scripts for analyzing and visualizing Git contribution statistics.

## Files

- `git-statistics.bat` - Main batch script to generate all statistics (Windows)
- `parse_git_stats.py` - Python script to parse git logs and generate markdown report
- `visualize_git_stats.py` - Python script to create visualization charts

## Usage

### Windows (Easy)

Simply double-click `git-statistics.bat` or run from project root:

```bash
scripts\git-statistics.bat
```

### Manual Execution

From the project root directory:

```bash
# Generate git log data
git -c core.quotePath=false log --all --date=iso-local --use-mailmap --numstat \
  --pretty=format:"%H%x1f%an%x1f%ae%x1f%ad%x1f%s" > gitlog_numstat.txt

# Parse and analyze
python scripts/parse_git_stats.py

# Create visualizations
python scripts/visualize_git_stats.py
```

## Output Files

The scripts generate the following files in the **project root**:

- `gitlog_numstat.txt` - Raw git log data
- `git-contribution-analysis.md` - Detailed markdown report with statistics
- `git-contribution-visualization.png` - Comprehensive visualization with 6 charts

## Requirements

- Python 3.6+
- matplotlib
- numpy

Install dependencies:

```bash
pip install matplotlib numpy
```

## Features

- **Author mapping** via `.mailmap` file (automatically consolidates different email addresses)
- **Module analysis** showing code distribution across Maven modules:
  - gateway
  - game (gomoku game logic)
  - game room
  - matching (game lobby)
  - user
  - ranking
  - common/shared
  - config/root
- **Comprehensive visualizations**:
  - Code distribution by module (pie chart)
  - Commits per contributor (bar chart)
  - Net code contribution (bar chart)
  - Module contributions by author (stacked bar)
  - Module activity - added vs deleted (grouped bar)
  - Contribution summary table

## Customization

Edit `.mailmap` in the project root to map multiple email addresses/names to a single canonical author identity.

Format:
```
Proper Name <proper@email> Commit Name <commit@email>
```
