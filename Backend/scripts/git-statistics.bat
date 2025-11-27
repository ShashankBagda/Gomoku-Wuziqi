@echo off
echo Generating Git Statistics...
echo.

REM Change to project root directory (one level up from scripts folder)
cd /d "%~dp0.."

REM Generate detailed git log with numstat
git -c core.quotePath=false log --all --date=iso-local --use-mailmap --numstat ^
  --pretty=format:"%%H%%x1f%%an%%x1f%%ae%%x1f%%ad%%x1f%%s" ^
  > gitlog_numstat.txt

echo Git statistics exported to gitlog_numstat.txt
echo.
echo Running Python analysis scripts...
echo.

REM Run the parser
python "scripts\parse_git_stats.py"

REM Run the visualization
python "scripts\visualize_git_stats.py"

echo.
echo Done! Check git-contribution-analysis.md and git-contribution-visualization.png in project root
pause
